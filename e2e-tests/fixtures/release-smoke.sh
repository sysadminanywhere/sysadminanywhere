#!/usr/bin/env bash
set -euo pipefail

work="${RUNNER_TEMP:-${TMPDIR:-/tmp}}/sysadmin-smoke"
mkdir -p "$work"
version=$(tr -d '[:space:]' < VERSION)
db() { docker exec -i "$POSTGRES_CONTAINER" "$@"; }
sql() { db psql -X -U postgres -v ON_ERROR_STOP=1 "$@"; }

for module in main inventory incident; do
    database="$module"
    if [[ "$module" == main ]]; then database=sysadminanywhere; fi
    db createdb -U postgres "${database}_old"
    sql -d "${database}_old" < "$module/src/main/resources/db/migration/V1__baseline_5_0_5.sql"
    case "$module" in
        main) sql -d "${database}_old" -c "INSERT INTO users (username) VALUES ('legacy-user')" ;;
        inventory)
            sql -d "${database}_old" -c "INSERT INTO hardware_models (name, hardware_type) VALUES ('Smoke Processor', 'Processor')"
            sql -d "${database}_old" -c "INSERT INTO computers (name, checking_date) VALUES ('Smoke Workstation', CURRENT_TIMESTAMP)"
            sql -d "${database}_old" -c "INSERT INTO computer_hardwares (computer_id, hardware_model_id) SELECT c.id, h.id FROM computers c CROSS JOIN hardware_models h"
            ;;
        incident) sql -d "${database}_old" -c "INSERT INTO incidents (name, signal_id, severity, status, created_at) VALUES ('legacy-incident', 'SMOKE', 'LOW', 'OPEN', CURRENT_TIMESTAMP)" ;;
    esac
    db pg_dump -U postgres --format=custom "${database}_old" > "$work/$database.dump"
    db createdb -U postgres "$database"
    db pg_restore -U postgres --exit-on-error --dbname="$database" < "$work/$database.dump"
done

# Rehearse restoring a persistent application volume into an empty replacement.
source_volume="sysadmin-smoke-source-$BASHPID"
restore_volume="sysadmin-smoke-restore-$BASHPID"
fixture_pid= main_pid= inventory_pid= incident_pid=
cleanup() {
    for pid in "$fixture_pid" "$main_pid" "$inventory_pid" "$incident_pid"; do
        if [[ -n "$pid" ]]; then kill "$pid" 2>/dev/null || true; fi
    done
    docker volume rm "$source_volume" "$restore_volume" >/dev/null 2>&1 || true
}
trap cleanup EXIT
docker run --rm -v "$source_volume:/data" postgres:17-alpine sh -ec 'printf "%s\n" "{"fixture":"report"}" > /data/scheduled-reports.json; tar -C /data -cf - .' > "$work/volume.tar"
docker run --rm -i -v "$restore_volume:/data" postgres:17-alpine tar -C /data -xf - < "$work/volume.tar"
[[ "$(docker run --rm -v "$restore_volume:/data:ro" postgres:17-alpine cat /data/scheduled-reports.json)" == '{"fixture":"report"}' ]]

wait_for() {
    for ((attempt = 0; attempt < 90; attempt++)); do
        if curl --silent --fail "$1" >/dev/null; then return 0; fi
        sleep 2
    done
    printf 'Timed out waiting for %s\n' "$1" >&2
    return 1
}

python3 e2e-tests/fixtures/directory.py > "$work/directory.log" 2>&1 &
fixture_pid=$!
java -jar "incident/target/incident-$version.jar" > "$work/incident.log" 2>&1 &
incident_pid=$!
start_inventory() {
    LDAP_USERNAME="$E2E_USERNAME" LDAP_PASSWORD="$E2E_PASSWORD" \
        java -jar "inventory/target/inventory-$version.jar" "$@" >> "$work/inventory.log" 2>&1 &
    inventory_pid=$!
    wait_for http://localhost:8082/ping
}
start_inventory
java -jar "main/target/main-$version.jar" --vaadin.launch-browser=false > "$work/main.log" 2>&1 &
main_pid=$!
wait_for http://localhost:8080/login
wait_for http://localhost:8083/ping

[[ "$(sql -At -d sysadminanywhere -c "SELECT username FROM users WHERE username='legacy-user'")" == legacy-user ]]
[[ "$(sql -At -d incident -c "SELECT name FROM incidents WHERE signal_id='SMOKE'")" == legacy-incident ]]
[[ "$(sql -At -d inventory -c "SELECT version FROM flyway_schema_history WHERE success ORDER BY installed_rank DESC LIMIT 1")" == 2 ]]

token=$(curl --silent --fail -X POST -H 'Content-Type: application/json' \
    -d "$(python3 -c 'import json, os; print(json.dumps({"username": os.environ["E2E_USERNAME"], "password": os.environ["E2E_PASSWORD"]}))')" \
    http://localhost:8081/api/ldap/authenticate | python3 -c 'import json,sys; print(json.load(sys.stdin)["token"])')
schedule_url=http://localhost:8082/api/inventory/schedule
curl --silent --show-error --fail -X PUT -H "Authorization: Bearer $token" -H 'Content-Type: application/json' \
    -d '{"cron":"0 15 3 * * *","enabled":false}' "$schedule_url" > /dev/null
kill "$inventory_pid"
wait "$inventory_pid" || true
start_inventory '--cron.expression=0 0 8 * * *'
curl --silent --show-error --fail -H "Authorization: Bearer $token" "$schedule_url" |
    python3 -c 'import json,sys; s=json.load(sys.stdin); assert s["cron"] == "0 15 3 * * *" and not s["enabled"] and s["nextRun"] is None'

PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=1 mvn -B -ntp -pl e2e-tests -Plocal -Dtest=SmokeTest -De2e.smoke.enabled=true test

remediation_url=http://localhost:8082/api/inventory/hardware/patches/remediation
curl --silent --show-error --fail -X PUT -H "Authorization: Bearer $token" -H 'Content-Type: application/json' \
    -d '{"computer":"Smoke Workstation","owner":"Smoke Operator","dueDate":"2030-01-01"}' "$remediation_url" > /dev/null
curl --silent --show-error --fail -X POST -H "Authorization: Bearer $token" -H 'Content-Type: application/json' \
    -d '{"computerNames":["Smoke Workstation"]}' http://localhost:8082/api/inventory/scan > /dev/null
for ((attempt = 0; attempt < 30; attempt++)); do
    if [[ "$(sql -At -d inventory -c 'SELECT verified_at IS NOT NULL FROM patch_remediation')" == t ]]; then exit 0; fi
    sleep 2
done
printf 'The completed fixture scan did not verify the assigned patch finding\n' >&2
exit 1
