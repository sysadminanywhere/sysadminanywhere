package com.sysadminanywhere.directory.controller.dto;

import com.sysadminanywhere.directory.model.ComputerEntry;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddComputerDto {
    @NotNull
    private String distinguishedName;

    @NotNull
    private ComputerEntry computer;

    private boolean isEnabled;
}