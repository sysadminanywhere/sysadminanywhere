package com.sysadminanywhere.directory.service;

import com.sysadminanywhere.common.directory.model.ComputerEntry;
import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.entry.DefaultEntry;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.entry.ModificationOperation;
import org.apache.directory.api.ldap.model.message.ModifyRequest;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ResolveServiceTest {

    private final ResolveService<ComputerEntry> resolveService = new ResolveService<>(ComputerEntry.class);

    @Test
    void getADValue_populatesAnnotatedFields() throws Exception {
        Entry entry = new DefaultEntry("cn=test,dc=example,dc=com");
        entry.add("cn", "test");
        entry.add("description", "Test computer");
        entry.add("objectClass", "top", "computer");
        entry.add("whenCreated", "20240101010101.0Z");
        entry.add("userAccountControl", "512");
        entry.add("servicePrincipalName", "spn/one", "spn/two");
        entry.add("objectSID", new byte[]{1, 2, 3});
        entry.add("operatingSystem", "Windows Server 2022");

        ComputerEntry computerEntry = resolveService.getADValue(entry);

        assertThat(computerEntry.getCn()).isEqualTo("test");
        assertThat(computerEntry.getDistinguishedName()).isEqualTo("cn=test,dc=example,dc=com");
        assertThat(computerEntry.getDescription()).isEqualTo("Test computer");
        assertThat(computerEntry.getObjectClass()).containsExactly("top", "computer");
        assertThat(computerEntry.getCreated()).isEqualTo(LocalDateTime.of(2024, 1, 1, 1, 1, 1));
        assertThat(computerEntry.getUserAccountControl()).isEqualTo(512);
        assertThat(computerEntry.getServicePrincipalNames()).containsExactly("spn/one", "spn/two");
        assertThat(computerEntry.getLocation()).isEqualTo("");
    }

    @Test
    void getEntry_createsEntryWithAnnotatedValues() throws Exception {
        ComputerEntry computerEntry = new ComputerEntry();
        computerEntry.setCn("test");
        computerEntry.setDistinguishedName("cn=test,dc=example,dc=com");
        computerEntry.setDescription("Updated description");
        computerEntry.setServicePrincipalNames(List.of("spn/one", "spn/two"));
        computerEntry.setUserAccountControl(66048);

        Entry entry = resolveService.getEntry(computerEntry);

        assertThat(entry.getDn().getName()).isEqualTo("cn=test,dc=example,dc=com");
        assertThat(entry.get("description").getString()).isEqualTo("Updated description");

        Attribute servicePrincipalName = entry.get("servicePrincipalName");
        List<String> values = new ArrayList<>();
        servicePrincipalName.forEach(value -> values.add(value.getString()));
        assertThat(values).containsExactly("spn/one", "spn/two");

        assertThat(entry.get("userAccountControl").getString()).isEqualTo("66048");
        assertThat(entry.get("cn").getString()).isEqualTo("test");
    }

    @Test
    void getModifyRequest_detectsAddReplaceAndRemoveOperations() throws Exception {
        Entry oldEntry = new DefaultEntry("cn=test,dc=example,dc=com");
        oldEntry.add("cn", "test");
        oldEntry.add("description", "Old description");
        oldEntry.add("location", "HQ");

        Entry newEntry = new DefaultEntry("cn=test,dc=example,dc=com");
        newEntry.add("cn", "test");
        newEntry.add("description", "New description");
        newEntry.add("managedBy", "CN=Manager,DC=example,DC=com");

        ModifyRequest modifyRequest = resolveService.getModifyRequest(newEntry, oldEntry);

        assertThat(modifyRequest.getName().getName()).isEqualTo("cn=test,dc=example,dc=com");
        assertThat(modifyRequest.getModifications()).hasSize(3);

        assertThat(modifyRequest.getModifications())
                .anySatisfy(modification -> {
                    assertThat(modification.getAttribute().getId()).isEqualToIgnoringCase("description");
                    assertThat(modification.getOperation()).isEqualTo(ModificationOperation.REPLACE_ATTRIBUTE);
                    assertThat(modification.getAttribute().getString()).isEqualTo("New description");
                })
                .anySatisfy(modification -> {
                    assertThat(modification.getAttribute().getId()).isEqualToIgnoringCase("managedBy");
                    assertThat(modification.getOperation()).isEqualTo(ModificationOperation.ADD_ATTRIBUTE);
                    assertThat(modification.getAttribute().getString()).isEqualTo("CN=Manager,DC=example,DC=com");
                })
                .anySatisfy(modification -> {
                    assertThat(modification.getAttribute().getId()).isEqualToIgnoringCase("location");
                    assertThat(modification.getOperation()).isEqualTo(ModificationOperation.REMOVE_ATTRIBUTE);
                    assertThat(modification.getAttribute().getString()).isEqualTo("HQ");
                });
    }

}