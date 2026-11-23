# Upgrade and recovery

The production Compose file uses one `SYSADMINANYWHERE_VERSION` for all four application images. Keep a copy of the old Compose file, its environment configuration and exact image tags or digests so you can restart the old release with restored data.

## Back up before upgrading

1. From `docker/prod`, stop the application and integration services while leaving PostgreSQL running with `docker compose stop sysadminanywhere directory inventory incident n8n vault`.
2. Dump each database in custom format, using your configured PostgreSQL username and database names. By default the databases are `sysadminanywhere`, `inventory`, `incident` and `n8n`. For example, in a shell with binary-safe redirection, run `docker compose exec -T db pg_dump -U postgres -Fc inventory > inventory.dump`.
3. Archive the named volumes `directory_change_journal`, `report_scheduler_data`, `n8n_data` and `vault_data`. Compose usually prefixes volume names with the project name. For example, use `docker run --rm -v PROJECT_report_scheduler_data:/data:ro postgres:17-alpine tar -C /data -cf - . > report_scheduler_data.tar`. Store the archives, database dumps, Compose configuration and environment securely together. Include any external credential store or mounted configuration that your installation uses. Vault dev mode keeps its state in memory and requires independent provisioning of any required credentials.

## Deploy 5.1.0

Set `SYSADMINANYWHERE_VERSION=5.1.0`, then run `docker compose pull` and `docker compose up -d`. The application services adopt an existing 5.0.5 PostgreSQL schema as Flyway version 1, then apply newer migrations. On an empty database Flyway creates the schema from version 1. Hibernate validates the result at startup. For an older installation, use a staging copy to reach 5.0.5 and verify startup before deploying 5.1.0.

Check the startup logs for Flyway and schema validation, sign in, confirm inventory history is present, and open Settings to verify the scan schedule. If you configured a schedule through the 5.0.5 UI, re-enter it after upgrading. That version kept UI changes only in memory.

## Restore

Restore into an isolated Compose project or a separate host. The example production configuration uses explicit container names and published ports. Adjust those when rehearsing on the same host. Start the database and restore each dump to its corresponding empty database with `docker compose exec -T db pg_restore -U postgres --exit-on-error --dbname=inventory < inventory.dump`, substituting names as needed. Restore each tar archive into its corresponding empty replacement volume with `docker run --rm -i -v PROJECT_report_scheduler_data:/data postgres:17-alpine tar -C /data -xf - < report_scheduler_data.tar`. Restore configuration and credentials, then start the saved application version. Verify login, inventory and incidents before switching users back.

The CI smoke test restores synthetic 5.0.5 database dumps and a volume archive, starts the upgraded services, verifies preserved data and schedule persistence, then runs Playwright against the UI with a local directory fixture. To run the browser checks against a test domain, set `E2E_BASE_URL`, `E2E_USERNAME`, `E2E_PASSWORD`, `E2E_SEARCH_NAME` and `E2E_HARDWARE_NAME`, then run `mvn -pl e2e-tests -Plocal -Dtest=SmokeTest -De2e.smoke.enabled=true test`. Append `-De2e.headed=true` to show the browser. The existing full scenario remains separately selectable with `-Dtest=E2ETest -De2e.tests.enabled=true`.
