package com.sysadminanywhere.directory.service;

import com.sysadminanywhere.common.directory.model.UserAccountControls;
import com.sysadminanywhere.common.directory.model.UserEntry;
import lombok.SneakyThrows;
import org.apache.directory.api.ldap.model.entry.DefaultEntry;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsersService {

    private final LdapService ldapService;

    ResolveService<UserEntry> resolveService = new ResolveService<>(UserEntry.class);

    public UsersService(LdapService ldapService) {
        this.ldapService = ldapService;
    }

    @SneakyThrows
    public Page<UserEntry> getAll(@ParameterObject Pageable pageable, String filters, String... attributes) {
        Page<Entry> result = ldapService.searchPage("(&(objectClass=user)(objectCategory=person)" + filters + ")", pageable.getSort(), pageable, attributes);
        return resolveService.getADPage(result);
    }

    public List<UserEntry> getAll(String filters, String... attributes) {
        List<Entry> result = ldapService.searchWithAttributes("(&(objectClass=user)(objectCategory=person)" + filters + ")", attributes);
        return resolveService.getADList(result);
    }

    public UserEntry getByCN(String cn) {
        List<Entry> result = ldapService.search("(&(objectClass=user)(objectCategory=person)(cn=" + cn + "))");
        Optional<Entry> entry = result.stream().findFirst();

        if (entry.isPresent())
            return resolveService.getADValue(entry.get());
        else
            return null;
    }

    @SneakyThrows
    public UserEntry add(String distinguishedName,
                         String cn,
                         String displayName,
                         String firstName,
                         String lastName,
                         String initials,
                         String password,
                         boolean isCannotChangePassword,
                         boolean isPasswordNeverExpires,
                         boolean isAccountDisabled,
                         boolean isMustChangePassword) {

        String dn;

        if (distinguishedName == null || distinguishedName.isEmpty()) {
            dn = "cn=" + cn + "," + ldapService.getUsersContainer();
        } else {
            dn = "cn=" + cn + "," + distinguishedName;
        }

        Entry entry = new DefaultEntry(
                dn,
                "displayName", displayName,
                "givenName", firstName,
                "sn", lastName,
                "sAMAccountName", cn,
                "userPrincipalName", cn + "@" + ldapService.getDomainName(),
                "objectClass:user",
                "objectClass:person",
                "cn", cn,
                "userPassword", password
        );

        ldapService.add(entry);

        UserEntry newUser = getByCN(cn);

        changeUserAccountControl(newUser, isCannotChangePassword, isPasswordNeverExpires, isAccountDisabled, isMustChangePassword);

        if (initials != null && !initials.isEmpty())
            ldapService.updateProperty(newUser.getDistinguishedName(), "initials", initials);

        return newUser;
    }

    public void changeUserAccountControl(UserEntry user, boolean isCannotChangePassword, boolean isPasswordNeverExpires, boolean isAccountDisabled, boolean isMustChangePassword) {
        int userAccountControl = user.getUserAccountControl();

        if (isCannotChangePassword)
            userAccountControl = userAccountControl | UserAccountControls.PASSWD_CANT_CHANGE.getValue();
        else
            userAccountControl = userAccountControl & ~UserAccountControls.PASSWD_CANT_CHANGE.getValue();

        if (isPasswordNeverExpires)
            userAccountControl = userAccountControl | UserAccountControls.DONT_EXPIRE_PASSWD.getValue();
        else
            userAccountControl = userAccountControl & ~UserAccountControls.DONT_EXPIRE_PASSWD.getValue();

        if (isAccountDisabled)
            userAccountControl = userAccountControl | UserAccountControls.ACCOUNTDISABLE.getValue();
        else
            userAccountControl = userAccountControl & ~UserAccountControls.ACCOUNTDISABLE.getValue();

        ldapService.updateProperty(user.getDistinguishedName(), "userAccountControl", String.valueOf(userAccountControl));

        if (isMustChangePassword)
            MustChangePasswordAsync(user);
    }

    private void MustChangePasswordAsync(UserEntry user) {
        ldapService.updateProperty(user.getDistinguishedName(), "pwdlastset", "0");
    }

    @SneakyThrows
    public UserEntry update(UserEntry user) {
        Entry entry = resolveService.getEntry(user);
        Entry oldEntry = resolveService.getEntry(getByCN(user.getCn()));

        ldapService.update(resolveService.getModifyRequest(entry, oldEntry));
        return getByCN(user.getCn());
    }

    @SneakyThrows
    public void delete(String distinguishedName) {
        Entry entry = new DefaultEntry(distinguishedName);
        ldapService.delete(entry);
    }

    public UserAccountControls getUserControl(int userAccountControl) {
        return UserAccountControls.fromValue(userAccountControl);
    }

    public String getDefaultContainer() {
        return ldapService.getUsersContainer();
    }

    public void resetPassword(String distinguishedName, String password) {
        ldapService.updateProperty(distinguishedName, "userPassword", password);
        ldapService.updateProperty(distinguishedName, "pwdLastSet", "0");
    }

    public LdapService getLdapService() {
        return ldapService;
    }

}