package com.sysadminanywhere.common.inventory.model;

public record ComputerPatchStatus(String computer, String lastPatchDate, boolean stale) { }
