package com.sysadminanywhere.directory.controller;

import com.sysadminanywhere.common.directory.dto.AddComputerDto;
import com.sysadminanywhere.common.directory.model.ComputerEntry;
import com.sysadminanywhere.directory.service.ComputersService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ComputersControllerTest {

    @Mock
    private ComputersService computersService;

    @InjectMocks
    private ComputersController computersController;

    @Test
    void getAll_returnsPageOfComputers() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);
        String[] attributes = {"cn"};
        Page<ComputerEntry> page = new PageImpl<>(List.of(createComputerEntry("PC01")));
        when(computersService.getAll(pageable, "", attributes)).thenReturn(page);

        var response = computersController.getAll(pageable, "", attributes);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isSameAs(page);
        verify(computersService).getAll(pageable, "", attributes);
    }

    @Test
    void getList_returnsListOfComputers() {
        ComputerEntry computer = createComputerEntry("PC01");

        when(computersService.getAll(anyString(), any(String[].class))).thenReturn(List.of(computer));

        var response = computersController.getList("", new String[]{"cn"});

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).containsExactly(computer);
        verify(computersService).getAll(eq(""), any(String[].class));
    }

    @Test
    void getByCN_returnsComputer() {
        ComputerEntry computer = createComputerEntry("PC01");

        when(computersService.getByCN("PC01")).thenReturn(computer);

        var response = computersController.getByCN("PC01");

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isSameAs(computer);
        verify(computersService).getByCN("PC01");
    }

    @Test
    void add_createsComputer() {
        AddComputerDto addComputerDto = new AddComputerDto();
        addComputerDto.setCn("PC01");
        addComputerDto.setDescription("Test computer");
        addComputerDto.setLocation("HQ");
        addComputerDto.setEnabled(true);

        ComputerEntry createdComputer = createComputerEntry("PC01");

        when(computersService.add(isNull(), eq("PC01"), eq("Test computer"), eq("HQ"), eq(true)))
                .thenReturn(createdComputer);

        var response = computersController.add(addComputerDto);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isSameAs(createdComputer);
        verify(computersService).add(isNull(), eq("PC01"), eq("Test computer"), eq("HQ"), eq(true));
    }

    @Test
    void update_modifiesComputer() {
        ComputerEntry computer = createComputerEntry("PC01");
        computer.setDescription("Updated description");

        when(computersService.update(any(ComputerEntry.class))).thenReturn(computer);

        var response = computersController.update(computer);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isSameAs(computer);
        verify(computersService).update(any(ComputerEntry.class));
    }

    @Test
    void delete_removesComputer() {
        doNothing().when(computersService).delete(anyString());

        var response = computersController.delete("cn=PC01,CN=Computers,DC=example,DC=com");

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        verify(computersService).delete("cn=PC01,CN=Computers,DC=example,DC=com");
    }

    private ComputerEntry createComputerEntry(String cn) {
        ComputerEntry computer = new ComputerEntry();
        computer.setCn(cn);
        computer.setDistinguishedName("cn=" + cn + ",CN=Computers,DC=example,DC=com");
        computer.setOperatingSystem("");
        return computer;
    }
}
