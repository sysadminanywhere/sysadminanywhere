package com.sysadminanywhere.directory.service;

import com.sysadminanywhere.common.directory.model.ComputerEntry;
import com.sysadminanywhere.common.directory.model.UserAccountControls;
import com.sysadminanywhere.common.directory.dto.BulkOperationResult;
import lombok.SneakyThrows;
import org.apache.directory.api.ldap.model.entry.DefaultEntry;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.message.ModifyRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ComputersService {

    private final LdapService ldapService;

    ResolveService<ComputerEntry> resolveService = new ResolveService<>(ComputerEntry.class);

    public ComputersService(LdapService ldapService) {
        this.ldapService = ldapService;
    }

    @SneakyThrows
    public Page<ComputerEntry> getAll(Pageable pageable, String filters, String... attributes) {
        Page<Entry> result = ldapService.searchPage("(&(objectClass=computer)" + filters + ")", pageable.getSort(), pageable, attributes);
        return resolveService.getADPage(result);
    }

    public List<ComputerEntry> getAll(String filters, String... attributes) {
        List<Entry> result = ldapService.searchWithAttributes("(&(objectClass=computer)" + filters + ")", attributes);
        return resolveService.getADList(result);
    }

    public ComputerEntry getByCN(String cn) {
        List<Entry> result = ldapService.search("(&(objectClass=computer)(cn=" + cn + "))");
        Optional<Entry> entry = result.stream().findFirst();

        return entry.map(attributes -> resolveService.getADValue(attributes)).orElse(null);
    }

    @SneakyThrows
    public ComputerEntry add(String distinguishedName, String cn, String description, String location, boolean isEnabled) {
        String dn;

        if (distinguishedName == null || distinguishedName.isEmpty()) {
            dn = "cn=" + cn + "," + ldapService.getComputersContainer();
        } else {
            dn = "cn=" + cn + "," + distinguishedName;
        }

        Entry entry = new DefaultEntry(
                dn,
                "sAMAccountName", cn,
                "objectClass:computer",
                "cn", cn
        );

        ldapService.add(entry);

        ComputerEntry newComputer = getByCN(cn);

        int userAccountControl = newComputer.getUserAccountControl();

        if (!isEnabled) {
            if ((userAccountControl & UserAccountControls.ACCOUNTDISABLE.getValue()) != UserAccountControls.ACCOUNTDISABLE.getValue())
                userAccountControl = userAccountControl & UserAccountControls.ACCOUNTDISABLE.getValue();
        } else {
            if ((userAccountControl & UserAccountControls.ACCOUNTDISABLE.getValue()) == UserAccountControls.ACCOUNTDISABLE.getValue())
                userAccountControl = userAccountControl & ~UserAccountControls.ACCOUNTDISABLE.getValue();
        }

        ldapService.updateProperty(newComputer.getDistinguishedName(), "userAccountControl", String.valueOf(userAccountControl));

        if (description != null && !description.isEmpty())
            ldapService.updateProperty(newComputer.getDistinguishedName(), "description", description);

        if (location != null && !location.isEmpty())
            ldapService.updateProperty(newComputer.getDistinguishedName(), "location", location);

        return newComputer;
    }

    public ComputerEntry update(ComputerEntry computer) {
        ModifyRequest modifyRequest = resolveService.getModifyRequest(computer, getByCN(computer.getCn()));
        ldapService.update(modifyRequest);

        return getByCN(computer.getCn());
    }

    @SneakyThrows
    public void delete(String distinguishedName) {
        Entry entry = new DefaultEntry(distinguishedName);
        ldapService.delete(entry);
    }

    public BulkOperationResult bulkChangeAccountStatus(List<String> distinguishedNames, boolean disabled) {
        int updated = 0;
        List<String> failures = new ArrayList<>();
        for (String distinguishedName : distinguishedNames) {
            try {
                List<Entry> entries = ldapService.search(new org.apache.directory.api.ldap.model.name.Dn(distinguishedName),
                        "(objectClass=computer)", org.apache.directory.api.ldap.model.message.SearchScope.OBJECT);
                if (entries == null || entries.isEmpty() || entries.get(0).get("useraccountcontrol") == null) {
                    failures.add(distinguishedName);
                    continue;
                }
                int current = Integer.parseInt(entries.get(0).get("useraccountcontrol").getString());
                int next = disabled
                        ? current | UserAccountControls.ACCOUNTDISABLE.getValue()
                        : current & ~UserAccountControls.ACCOUNTDISABLE.getValue();
                ldapService.updateProperty(distinguishedName, "userAccountControl", String.valueOf(next));
                updated++;
            } catch (Exception exception) {
                failures.add(distinguishedName);
            }
        }
        return new BulkOperationResult(updated, failures);
    }

    public BulkOperationResult bulkDelete(List<String> distinguishedNames) {
        int updated = 0;
        List<String> failures = new ArrayList<>();
        for (String distinguishedName : distinguishedNames) {
            try {
                delete(distinguishedName);
                updated++;
            } catch (Exception exception) {
                failures.add(distinguishedName);
            }
        }
        return new BulkOperationResult(updated, failures);
    }

    public UserAccountControls getUserControl(int userAccountControl) {
        return UserAccountControls.fromValue(userAccountControl);
    }

    public String getDefaultContainer() {
        return ldapService.getComputersContainer();
    }

}
