# Sysadmin Anywhere: Inventory
![GitHub Actions Workflow Status](https://img.shields.io/github/actions/workflow/status/sysadminanywhere/inventory/maven.yml)

Part of **[Sysadmin Anywhere](https://github.com/sysadminanywhere/sysadminanywhere)** service.

## Hardware inventory

The `/inventory/hardware` page groups discovered hardware by component type and model. Each row reports the number of computers where that model was found. Open a model to see the computers using it and the history of changes; selecting a computer shows its current hardware inventory and computer-specific history.

The inventory service records component installation, removal and changed WMI properties during scans. Existing components are marked **first recorded** when they are first seen by the change journal; their actual installation date cannot be recovered from the current snapshot. A device missing from a scan is removed from the current inventory and logged only when all hardware WMI queries succeeded. A failed/incomplete WMI scan preserves the previous inventory so connectivity failures do not look like removals.

Change history begins with the first successful scan after this feature is deployed. A removal can be useful evidence for an administrator to investigate, but by itself does not establish theft.
