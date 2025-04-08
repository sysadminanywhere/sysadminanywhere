# Sysadmin Anywhere

Free web-based application revolutionizes Active Directory network administration by offering a unified tool that simplifies every task you need to perform.

![GitHub Actions Workflow Status](https://img.shields.io/github/actions/workflow/status/sysadminanywhere/sysadminanywhere/maven.yml)
![Static Badge](https://img.shields.io/badge/version-2.1.2-blue)

![Sysadmin Screenshot](images/Screen02.png)

### Features

- Add, edit and delete objects in Active Directory
- Audit
- View software and hardware
- Events, processes, services on computers
- Add and delete objects from groups
- Reset a user's password
- Add photos
- Import users from csv file
- Restart and shutdown remote computers
- Computers performance
- Patterns for add new users
- Reports (20+)
- No limitation on the number of objects
- Monitoring system with alert rules - including Ping, password expiration notifier, object change detection, incorrect password attempts

### License

This software is distributed under the terms of the MIT License (MIT).


**[Docker compose file examle](docker/docker-compose.yml)**

**Sysadmin Anywhere environment variables:**
```
- DB_ADDRESS=postgres
- DB_PORT=5432
- DB_BASE=sysadminanywhere
- DB_USER=sysadminanywhere
- DB_PASSWORD=sysadminanywhere
- LDAP_SERVER=192.168.1.1
- LDAP_PORT=389
- LDAP_USE_SSL=false
- LDAP_GROUPS_ALLOWED=CN=Domain Admins,CN=Users,DC=example,DC=com

- LDAP_USERNAME=<username>
- LDAP_PASSWORD=<password>
- 
- INVENTORY_ENABLED=true
- INVENTORY_SCAN_CRON=0 0 * * * *
```
Use the "username" and "password' of a user who has rights to read from the Active Directory and from the WMI service on domain computers.
