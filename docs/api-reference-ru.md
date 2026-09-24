# REST API SysadminAnywhere

OpenAPI-примеры находятся в [ai-openapi.yaml](ai-openapi.yaml). Название файла сохранено ради существующих ссылок; сейчас он описывает реальные REST API каталога, инцидентов и инвентаризации.

## Адреса сервисов

При локальном запуске используются:

| Сервис | Адрес | Swagger UI | OpenAPI JSON |
| --- | --- | --- | --- |
| Directory | `http://localhost:8081` | `/swagger-ui/index.html` | `/v3/api-docs` |
| Inventory | `http://localhost:8082` | `/swagger-ui/index.html` | `/v3/api-docs` |
| Incident | `http://localhost:8083` | `/swagger-ui/index.html` | `/v3/api-docs` |

В production адреса зависят от reverse proxy и Docker-сети. Не публикуйте внутренние порты сервисов в интернет без TLS и сетевых ограничений.

## Аутентификация

Получите JWT в Directory Service, передав учётные данные Active Directory:

```http
POST http://localhost:8081/api/ldap/authenticate
Content-Type: application/json

{"username":"administrator","password":"<пароль>","service":"main"}
```

Ответ содержит `token`, `username` и `roles`. Передавайте токен в защищённые запросы:

```http
Authorization: Bearer <token>
```

Обычная LDAP-аутентификация выдаёт JWT сроком на один час. Каждый вошедший получает `ROLE_READER`; прямое членство в `CN=Domain Admins,CN=Users,<базовый DN домена>` дополнительно даёт `ROLE_ADMIN`. Другие точные DN групп задаются через `LDAP_ADMIN_GROUP_DNS` (разделитель `;`), точные имена учётных записей — через `LDAP_ADMIN_USERS` (разделитель `,`). Чтение доступно роли читателя через GET и LDAP search/count; изменение требует администратора. `LDAP_WMI_READ_USERS` разрешает указанным учётным записям только запросы `/api/wmi/execute` для инвентаризации, но не команды и вызовы методов. После изменения членства необходимо войти повторно. Для интеграций используйте отдельные ограниченные API-токены в разделе `/settings/api-tokens`: права можно разделить по сервису и типу операции, токен можно немедленно отозвать. Секрет токена показывается только при создании. Эндпоинты OpenAPI и Swagger UI доступны без токена.

| Право | Доступ |
| --- | --- |
| `directory:read` | Чтение объектов каталога, поиск LDAP и аудит |
| `directory:write` | Создание, изменение, удаление и массовые операции с объектами каталога |
| `remote:execute` | Удалённые WMI-запросы и команды |
| `inventory:read` | Чтение инвентаризации и её состояния |
| `incidents:read` | Просмотр инцидентов |
| `incidents:write` | Создание, обновление и закрытие инцидентов |

Срок API-токена — от 1 до 365 дней (по умолчанию 30). Каталог хранит только SHA-256-отпечаток токена; целиком секрет возвращается только при создании. Сервисы проверяют статус токена в Directory Service, поэтому отзыв действует сразу.

## Примеры

### Поиск в Active Directory

`searchScope`: `0` — одна запись, `1` — один уровень, `2` — поддерево.

```http
POST http://localhost:8081/api/ldap/search
Authorization: Bearer <token>
Content-Type: application/json

{"distinguishedName":"DC=example,DC=com","filter":"(objectClass=user)","searchScope":2,"attributes":["cn","mail","sAMAccountName"]}
```

### Постраничный список пользователей

Передавайте `filters` и `attributes`. Для нескольких атрибутов повторяйте query-параметр `attributes`:

```http
GET http://localhost:8081/api/users?page=0&size=20&filters=%28objectClass%3Duser%29&attributes=cn&attributes=mail
Authorization: Bearer <token>
```

### Массовое отключение пользователей

```http
POST http://localhost:8081/api/users/bulk/change-status
Authorization: Bearer <token>
Content-Type: application/json

{"distinguishedNames":["CN=Alex Morgan,OU=Users,DC=example,DC=com"],"accountDisabled":true}
```

Результат содержит количество обработанных записей и список ошибок по объектам.

### Состояние инвентаризации

```http
GET http://localhost:8082/api/inventory/health?staleDays=30
Authorization: Bearer <token>
```

### Список инцидентов

Укажите `severity` и `status`; для выборки без фильтра по конкретному значению используйте `ALL`.

```http
GET http://localhost:8083/api/incidents?page=0&size=20&severity=ALL&status=OPEN
Authorization: Bearer <token>
```

## Доступ и права

Операции управления каталогом, инцидентами и инвентаризацией требуют действующий JWT с ролью администратора. Для массовых изменений сначала проверьте DN и состав запроса: API выполнит переданные операции сразу и вернёт результаты отдельно для обработанных объектов и ошибок.
