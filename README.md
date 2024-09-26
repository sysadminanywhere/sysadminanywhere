### Sysadmin Anywhere


![GitHub Actions Workflow Status](https://img.shields.io/github/actions/workflow/status/sysadminanywhere/sysadminanywhere/maven.yml)


**Docker command:**
> docker push sysadminanywhere/sysadminanywhere:latest

**Environment variables:**
- DB_ADDRESS=postgres
- DB_PORT=5432
- DB_BASE=sysadminanywhere
- DB_USER=sysadminanywhere
- DB_PASSWORD=sysadminanywhere
- LDAP_SERVER=192.168.1.1
- LDAP_PORT=636
- LDAP_GROUPS_ALLOWED=CN=Domain Admins,CN=Users,DC=example,DC=com


**[Docker compose file examle](docker/docker-compose.yml)**
