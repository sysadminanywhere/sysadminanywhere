# SysadminAnywhere REST API

OpenAPI examples are in [ai-openapi.yaml](ai-openapi.yaml). The filename is retained for existing links; the file now documents the implemented Directory, Incident, and Inventory REST APIs.

## Service URLs

Local development uses these addresses:

| Service | Base URL | Swagger UI | OpenAPI JSON |
| --- | --- | --- | --- |
| Directory | `http://localhost:8081` | `/swagger-ui/index.html` | `/v3/api-docs` |
| Inventory | `http://localhost:8082` | `/swagger-ui/index.html` | `/v3/api-docs` |
| Incident | `http://localhost:8083` | `/swagger-ui/index.html` | `/v3/api-docs` |

Production URLs depend on the reverse proxy and Docker network. Do not expose internal service ports to the internet without TLS and network restrictions.

## Authentication

Obtain a JWT from the Directory Service using an Active Directory account:

```http
POST http://localhost:8081/api/ldap/authenticate
Content-Type: application/json

{"username":"administrator","password":"<password>","service":"main"}
```

The response contains `token`, `username`, and `roles`. Send the token with protected requests:

```http
Authorization: Bearer <token>
```

Regular AD authentication issues a one-hour JWT and currently assigns `ROLE_ADMIN` to each successful login. For integrations, use dedicated restricted API tokens from `/settings/api-tokens`: permissions can be limited by service and operation type, and a token can be revoked immediately. The token secret is shown only when created. OpenAPI and Swagger UI endpoints are public.

| Scope | Access |
| --- | --- |
| `directory:read` | Read directory objects, LDAP search, and audit data |
| `directory:write` | Create, update, delete, and bulk-change directory objects |
| `remote:execute` | Remote WMI queries and commands |
| `inventory:read` | Read inventory data and health |
| `incidents:read` | View incidents |
| `incidents:write` | Create, update, and close incidents |

API tokens can live from 1 to 365 days (30 by default). The directory stores only the token's SHA-256 fingerprint; the full secret is returned only at creation. Services check token status with the Directory Service, so revocation takes effect immediately.

## Examples

### Search Active Directory

`searchScope`: `0` means the base entry, `1` means one level, and `2` means the full subtree.

```http
POST http://localhost:8081/api/ldap/search
Authorization: Bearer <token>
Content-Type: application/json

{"distinguishedName":"DC=example,DC=com","filter":"(objectClass=user)","searchScope":2,"attributes":["cn","mail","sAMAccountName"]}
```

### Page through users

Provide `filters` and `attributes`. Repeat the `attributes` query parameter for each requested attribute:

```http
GET http://localhost:8081/api/users?page=0&size=20&filters=%28objectClass%3Duser%29&attributes=cn&attributes=mail
Authorization: Bearer <token>
```

### Bulk disable users

```http
POST http://localhost:8081/api/users/bulk/change-status
Authorization: Bearer <token>
Content-Type: application/json

{"distinguishedNames":["CN=Alex Morgan,OU=Users,DC=example,DC=com"],"accountDisabled":true}
```

The response includes the number of processed entries and any per-object failures.

### Inventory health

```http
GET http://localhost:8082/api/inventory/health?staleDays=30
Authorization: Bearer <token>
```

### List incidents

Provide `severity` and `status`; use `ALL` when no specific value is needed for that filter.

```http
GET http://localhost:8083/api/incidents?page=0&size=20&severity=ALL&status=OPEN
Authorization: Bearer <token>
```

## Access and permissions

Directory management, incident, and inventory operations require a valid JWT with the administrator role. Review the DNs and request contents before bulk changes; the API executes the supplied operations immediately and reports per-object results and failures.
