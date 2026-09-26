# Sysadmin Anywhere: Inventory
![GitHub Actions Workflow Status](https://img.shields.io/github/actions/workflow/status/sysadminanywhere/inventory/maven.yml)

Part of **[Sysadmin Anywhere](https://github.com/sysadminanywhere/sysadminanywhere)** service.

## Hardware inventory

The `/inventory/hardware` page groups discovered hardware by component type and model. Each row reports the number of computers where that model was found. Open a model to see the computers using it and the history of changes; selecting a computer shows its current hardware inventory and computer-specific history.

The page uses the same filter-and-grid layout as `/inventory/software`: filter by model name or component category, then use Search or Reset. On narrow screens, open the Filters panel to access those controls.

The inventory service records a component model appearing or disappearing and a same-model disk, memory module or baseboard replacement when a reliable serial number changes. A model-name change with the same serial number (or stable processor/video identifier) updates the model association without a hardware event. Routine WMI property updates are kept in the current snapshot but are not hardware change events; older property-change rows are hidden from the hardware history. Existing components are marked **first recorded** when they are first seen by the change journal; their actual installation date cannot be recovered from the current snapshot. A device missing from a scan is removed from the current inventory and logged only when all hardware WMI queries succeeded. A failed/incomplete WMI scan preserves the previous inventory so connectivity failures do not look like removals. The current schema keeps one row per computer and model, so removal of one of several identical devices or a same-model replacement without a reliable serial number cannot be determined. When no stable identifier is supplied, a model rename can still appear as a removal and installation.

Change history begins with the first successful scan after this feature is deployed. A removal can be useful evidence for an administrator to investigate, but by itself does not establish theft.
