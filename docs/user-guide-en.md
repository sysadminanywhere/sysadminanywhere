# Sysadmin Anywhere User Guide

## 1. Service Purpose
Sysadmin Anywhere is a web interface for Active Directory administration and related tasks: AD object management, auditing, software/hardware inventory, incident handling, automation execution and monitoring (n8n), and report generation.


## 2. Login and General Navigation

### 2.1 Login
- Login page: `/login`.
- Standard username/password form is used.
- "Forgot password" button is disabled.
- After successful authentication, the dashboard opens (`/`).

Every AD login has read access. Direct members of the domain's `Domain Admins` group can also change objects and use administrative features. Administrators can configure other group DNs or exact login names through `LDAP_ADMIN_GROUP_DNS` and `LDAP_ADMIN_USERS` in the Directory service. Readers can browse the dashboard, directory, inventory, incidents, reports, and help; editing controls and bulk selection are unavailable. WMI inventory scanning needs `LDAP_WMI_READ_USERS` set to its service login. Sign in again after a role change.

If an inventory scan reports `ConnectException` for `DIRECTORY_SERVICE`, Directory is unavailable at the configured address (for local runs, normally `http://localhost:8081`). Start Directory or correct the URL. Inventory retries temporary connection failures; the number of attempts and delay are controlled by `DIRECTORY_AUTH_MAX_ATTEMPTS` and `DIRECTORY_AUTH_RETRY_DELAY_MS`.

### 2.2 Interface Layout
The interface consists of:
- left vertical top-level panel (Dashboard, Management, Incidents, Automation, Inventory, Reports, Account, Settings);
- submenu of the selected section;
- workspace with the page;
- top page action menu (if the page supports action menus).

### 2.3 Common Work Patterns
Many lists use similar mechanics:
- filters (block with fields + Search/Reset buttons);
- table (Grid) with navigation to details card on row click;
- top action menu (refresh, create, import, delete, etc.);
- modal dialogs (Dialog) for input/editing;
- confirmation dialogs (ConfirmDialog) for dangerous operations.

## 3. Dashboard Section

### 3.1 Dashboard (`/`, `/dashboard`, `/domain/dashboard`)
Purpose: summary overview of AD state.

Displayed charts:
- overall summary by object count (Computers, Users, Groups, Printers, Contacts);
- user breakdown (total, disabled, locked, expired, never expires);
- computer breakdown (total, disabled, workstations, servers, domain controllers);
- group breakdown (total, security, distribution, built-in).

### 3.2 Domain (`/domain/info`)
Purpose: domain and controller information.

Contents:
- domain name and DN;
- Domain Controllers card (links to computer cards);
- Details card (forest/domain/DC functional level, SASL, LDAP versions, current time, max password age).

### 3.3 Audit (`/domain/audit`)
The page shows AD changes for the selected period. Use filters for object name, DN and action, then click a row to inspect available LDAP metadata. This AD audit source does not provide the application user or historical before/after values.
Purpose: viewing AD change audit.

Filters:
- Date range (from/to),
- Action,
- (Name field is provided in code).

Table:
- Name,
- Distinguished Name,
- Action,
- When changed.

### 3.4 Application change history (`/domain/change-history`)
This page shows operations performed through Sysadmin Anywhere. Each entry contains the object, DN, action, application user, timestamp and available before/after snapshots. Filter by object, DN, operator, action and date, then click a row for details.

The journal is stored in a JSON file. Configure it with `directory.audit.journal-path` or `DIRECTORY_AUDIT_JOURNAL_PATH` (local default `./data/change-journal.json`, Docker default `/data/change-journal.json`). The maximum number of entries is controlled by `DIRECTORY_AUDIT_JOURNAL_MAX_ENTRIES` and defaults to 10,000. Passwords and other sensitive attributes are replaced with `[redacted]`. Changes made directly in AD remain in the Audit page and do not have application before/after snapshots.

### 3.5 Domain health (`/domain/health`)
The page performs operational checks for LDAP, domain controller discovery, DNS resolution, SMB reachability for SYSVOL/NETLOGON, LDAP time drift and the LDAP certificate when SSL is enabled. Each check is shown separately as Healthy, Warning, Error or Not checked.

## 4. Management Section

### 4.1 Containers (`/management/containers`)
Purpose: working with AD objects in a selected container.

Interface:
- container tree on the left;
- container object list on the right (cn/type/description).

Top menu actions:
- Refresh;
- New -> User / Computer / Group / Contact.

Navigation:
- click on object row opens details card (user/computer/group/printer/contact).

### 4.2 Users (`/management/users`)
Purpose: AD user list.

Filters:
- `cn`;
- status filter (All/Disabled/Locked/Expired/Never expires).

In the table, blue icons indicate enabled users and gray icons indicate disabled accounts.

Top menu actions:
- Refresh;
- New (user creation dialog);
- Import (import users from CSV).

Bulk operations:
- select multiple users using the checkboxes in the table;
- choose `Enable selected` or `Disable selected` from the page menu;
- confirm the operation. Changes apply to the selected users on the current page and are reflected in the directory audit.

Navigation:
- click a user to open `User Details`;
- use the checkboxes for multi-selection.

#### New User Dialog
Fields:
- Container,
- Display name,
- First name / Initials / Last name,
- Account name,
- Password / Confirm password,
- account flags (must change password, cannot change, never expires, disabled).

Features:
- name/login templates and default password from settings are used.

#### Import Users Dialog (CSV)
Functions:
- CSV upload,
- container selection,
- batch user import,
- help button with link to wiki for CSV template.

#### User Details (`/management/users/:id/details`)
Contents:
- general user information,
- photo (avatar),
- Member Of block (groups).

Actions:
- Update,
- Photo,
- Options (account parameters),
- Reset password,
- Delete (with confirmation).

### 4.3 Computers (`/management/computers`)
Purpose: AD computer list.

Filters:
- `cn`,
- availability/status (via combined filter).

In the table, blue icons indicate enabled computers and gray icons indicate disabled computers.

Top menu actions:
- Refresh,
- New (add computer),
- enable/disable selected computers,
- delete selected computers.

Use row checkboxes for bulk actions and confirm the operation. A single row click still opens the computer details.

Selected computers can also be added to or removed from a group. Choose the target group in the dialog and confirm the operation.

#### Computer Details (`/management/computers/:id/details`)
Contents:
- basic information (hostname, OS, version, service pack, IP, manufacturer, model, location),
- Member Of block.

Actions:
- Update,
- Delete (confirmation),
- Management (submenu):
  - Processes,
  - Services,
  - Events,
  - Software,
  - Hardware,
  - Performance,
  - Reboot (confirmation),
  - Shutdown (confirmation).

#### Processes (`/management/computers/:id/processes`)
- process list with filters;
- process details window;
- process stop operation (from dialog);
- Refresh from page menu.

#### Services (`/management/computers/:id/services`)
- service list with filters;
- service details window;
- Start/Stop operations;
- Refresh.

#### Events (`/management/computers/:id/events`)
- filters: source, event type, date;
- event list;
- event details view dialog;
- Refresh.

#### Software (`/management/computers/:id/software`)
- list of installed software on selected computer;
- filters by name/vendor.

#### Hardware (`/management/computers/:id/hardware`)
- hardware entity cards (computer system, BIOS, baseboard, disk, processor, video, memory, etc.);
- tabular view with tabs.

#### Performance (`/management/computers/:id/performance`)
- computer performance page (system metrics/status).

### 4.4 Groups (`/management/groups`)
Purpose: AD group list.

Filters:
- `cn`,
- availability/type conditions (via page filters).

Actions:
- Refresh,
- New (create group),
- delete selected groups.

Select rows with checkboxes; deletion requires confirmation and reports successful and failed objects.

Users also support bulk add-to-group and remove-from-group operations.

#### New Group Dialog
Fields:
- Container,
- Name,
- Description,
- Group scope (Global/Local/Universal),
- Group type (Security/Distribution).

#### Group Details (`/management/groups/:id/details`)
Contents:
- general information,
- group type,
- membership blocks (Member Of and member composition).

Actions:
- Update,
- Delete (confirmation).

### 4.5 Contacts (`/management/contacts`)
Purpose: AD contact list.

Actions:
- Refresh,
- New (add contact),
- delete selected contacts.

#### New Contact Dialog
Fields:
- Container,
- Display name,
- First name,
- Initials,
- Last name.

#### Contact Details (`/management/contacts/:id/details`)
Contents:
- contact card,
- contact fields (company, title, email, phones).

Actions:
- Update,
- Delete (confirmation).

### 4.6 Printers (`/management/printers`)
Purpose: AD printer list.

Filter:
- `cn`.

Navigation:
- click on printer -> printer card.

Select multiple printers with checkboxes and delete them in one confirmed operation.

The object list inside a selected container also supports bulk deletion for supported types (users, computers, groups, contacts and printers). System and unknown types are not deleted by the bulk operation.

Selected supported objects can also be moved to another container. Choose the destination container and confirm before execution.

#### Printer Details (`/management/printers/:id/details`)
Actions:
- Delete (confirmation).

## 5. Incidents Section

### 5.1 Incidents (`/incidents`)
Purpose: monitoring and processing incidents.

Filters:
- severity,
- status.

Table:
- createdAt,
- name,
- machineName,
- severity,
- status,
- recommendation.

### 5.2 Incident Dialog
Opens on incident click.

Shows:
- incident metadata (time, machine, recommendations, event count, etc.).

Allows:
- change severity/status,
- save changes,
- close incident separately (status Closed),
- cancel changes.

## 6. Automation Section

### 6.1 Workflows List (`/automation/workflows`)
Purpose: viewing available workflows.

Table:
- ID,
- Name,
- Description.

Actions:
- Refresh,
- New (opens n8n editor in new tab: `http://localhost:5678/workflow/new`).

Navigation:
- click on workflow -> `Workflow Details`.

### 6.2 Workflow Details (`/automation/workflows/:id/details`)
Contents:
- workflow name and description,
- visual preview of n8n schema,
- table of recent runs (id, started, run time, status, error).

Actions:
- Edit (open workflow in n8n),
- Execute (manual run),
- Delete (confirmation).

## 7. Inventory Section

### 7.1 Software Inventory (`/inventory/software`)
Purpose: aggregated catalog of installed software.

Filters:
- name,
- vendor.

Table:
- name,
- vendor,
- version,
- count.

Navigation:
- click on row -> list of computers with this software.

### 7.2 Computers With Software (`/inventory/software/:id/computer`)
Purpose: list of computers where selected software is installed.

Filter:
- name.

Table:
- computer name.

### 7.3 Hardware Inventory (`/inventory/hardware`)
Purpose: aggregated catalog of hardware components.

Filters:
- hardware type,
- name.

Table:
- name,
- type.

Navigation:
- click on row -> component properties.

### 7.4 Hardware Properties (`/inventory/hardware/:id/details`)
Purpose: viewing detailed properties of selected hardware model.

Display:
- table property -> value for specific object.

### 7.5 Inventory health (`/inventory/health`)
Inventory health shows tracked computers whose last inventory scan is older than the selected threshold. It also reports computers that have never been scanned. Click a row to open the corresponding computer details page.
Use **Create incident** to record an overdue scan with the computer name and last scan time in the incident context.

## 8. Reports Section

### 8.1 Report Lists
- `/reports/users`
- `/reports/computers`
- `/reports/groups`

Each page contains a list of reports with name and description.
On click, report opens in preview mode.

### 8.2 Preview (`/reports/report?entry=...&id=...`)
Purpose: generating and viewing PDF report.

Features:
- report is generated based on specified filters and columns;
- displayed via embedded PDF viewer;
- supports typical pre-configured reports (including disabled, locked, password expiry, servers, domain controllers, etc.).

### 8.3 Scheduled reports (`/reports/scheduled`)
Administrators can create daily or weekly schedules for a selected users, computers, groups, printers or contacts report. Each schedule supports PDF or CSV output, an enabled switch, and optional comma-separated email recipients. Use **Run now** to test a schedule; generated files and the last 100 run results are persisted under the configured scheduler data directory. Email delivery uses the optional SMTP settings (`MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_SMTP_AUTH`, `MAIL_SMTP_STARTTLS`).

### 8.4 Security audit (`/security/audit`)
The security audit scans standard Active Directory attributes and highlights protected or `adminCount=1` users and groups, users and computers with service principal names, and users missing an email address or manager. Findings are informational and should be reviewed with the domain policy. MFA enrollment is not available through standard AD attributes and must be checked in the identity provider.
Use **Create incident** on a finding to open an incident with the affected user or computer attached. The action requires confirmation and the resulting item is available in the Incidents section.

The Groups page also supports CSV import. Required columns are `name`, `scope` (`Local`, `Global` or `Universal`) and `type` (`Security` or `Distribution`); `description` is optional. Rows are validated before import and failed rows remain visible.

## 9. Account Section

### 9.1 Me (`/account/me`)
Purpose: current user profile.

Shows:
- display name,
- company,
- title,
- email,
- mobile/office/home phone,
- profile photo.

Actions:
- Edit,
- Photo,
- Sign out.

## 10. Settings Section

### 10.1 Settings (`/settings/settings`)
Theme section:
- interface color theme toggle.

User Patterns section:
- `Display Name` template for auto-filling new users;
- `Login` template;
- default password for user creation.

Language section:
- interface language selection (`en`, `ru`, `de`, `fr`, `es`, `pt`, `zh`, `ja`).

Action:
- Save (saves settings and shows confirmation).

### 10.2 Webhooks (`/settings/webhooks`)
Configure outbound HTTP notifications for incident lifecycle events (`incident.created`, `incident.updated`, `incident.closed`) and directory changes (`user.created`, `user.updated`, `user.deleted`, `group.created`, `group.updated`, `group.deleted`). Each delivery includes the event header and an HMAC SHA-256 signature when a secret is configured. Failed requests are retried up to three times. Use `*` in the events field to subscribe to all current and future events.

### 10.3 API tokens (`/settings/api-tokens`)
Create dedicated integration tokens, set an expiry between 1 and 365 days, and select permissions: read or change directory objects, read inventory, and read or change incidents. The table lists active and revoked tokens. The secret is shown only once; revoked tokens are rejected by all services immediately.

### 10.4 About (`/settings/about`)
Service "About" page with product/version information.

## 11. Dialogs and Confirmations (Summary)

### 11.1 Creation Dialogs
- New User,
- New Computer,
- New Group,
- New Contact,
- Import Users CSV.

### 11.2 Editing Dialogs
- Update User / User Photo / User Options / Reset Password,
- Update Computer,
- Update Group,
- Update Contact,
- Incident details/update,
- Process/service/event details view.

### 11.3 ConfirmDialog (critical actions)
- deletion of user/group/computer/contact/printer/workflow;
- computer reboot/shutdown.

## 12. Typical User Scenarios

### 12.1 Creating a User
1. Open `Management -> Users` or `Management -> Containers`.
2. Click `New User`.
3. Select container and fill required fields.
4. Specify password and account parameters.
5. Click `Save`.

### 12.2 Importing Users from CSV
1. `Management -> Users`.
2. Click `Import`.
3. Select container and upload CSV.
4. Verify upload success.
5. Click `Import`.

### 12.3 Remote Action on Computer
1. `Management -> Computers` -> open computer card.
2. In `Management` menu select required operation:
   - view processes/services/events,
   - reboot/shutdown.
3. Confirm action in dialog.

### 12.4 Working with Incident
1. Open `Incidents`.
2. Find incident via severity/status filters.
3. Open incident row.
4. Change severity/status and save, or close incident.

### 12.5 Generating Report
1. Go to `Reports` and select type (Users/Computers/Groups).
2. Select specific report.
3. View generated PDF.

## 13. Limitations and Operational Notes
- `Incidents`, `Inventory`, `Automation` sections depend on availability of corresponding backend services; on unavailability, `service unavailable` error is shown.
- Some actions open n8n in separate tab at `http://localhost:5678`.
- Most pages are optimized for responsive mode: filters collapse in mobile view.
