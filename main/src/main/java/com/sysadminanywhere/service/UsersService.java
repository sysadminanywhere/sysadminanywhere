package com.sysadminanywhere.service;

import com.sysadminanywhere.model.ad.UserAccountControls;
import com.sysadminanywhere.model.ad.UserEntry;
import lombok.SneakyThrows;
import org.apache.directory.api.ldap.model.entry.DefaultEntry;
import org.apache.directory.api.ldap.model.entry.Entry;
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
    public Page<UserEntry> getAll(Pageable pageable, String filters) {
        List<Entry> result = ldapService.search("(&(objectClass=user)(objectCategory=person)" + filters + ")", pageable.getSort());
        return resolveService.getADPage(result, pageable);
    }

    public List<UserEntry> getAll(String filters) {
        List<Entry> result = ldapService.search("(&(objectClass=user)(objectCategory=person)" + filters + ")");
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
                         UserEntry user,
                         String password,
                         boolean isCannotChangePassword,
                         boolean isPasswordNeverExpires,
                         boolean isAccountDisabled,
                         boolean isMustChangePassword) {

        if (user.getSamAccountName() == null || user.getSamAccountName().isEmpty())
            user.setSamAccountName(user.getCn());

        if (user.getUserPrincipalName() == null || user.getUserPrincipalName().isEmpty())
            user.setUserPrincipalName(user.getSamAccountName() + "@" + ldapService.getDomainName());

        String dn;

        if (distinguishedName == null || distinguishedName.isEmpty()) {
            dn = "cn=" + user.getCn() + "," + ldapService.getUsersContainer();
        } else {
            dn = "cn=" + user.getCn() + "," + distinguishedName;
        }

        Entry entry = new DefaultEntry(
                dn,
                "displayName", user.getDisplayName(),
                "givenName", user.getFirstName(),
                "sn", user.getLastName(),
                "sAMAccountName", user.getSamAccountName(),
                "userPrincipalName", user.getUserPrincipalName(),
                "objectClass:user",
                "objectClass:person",
                "cn", user.getCn(),
                "userPassword", password
        );

        ldapService.add(entry);

        UserEntry newUser = getByCN(user.getCn());

        ChangeUserAccountControl(newUser, isCannotChangePassword, isPasswordNeverExpires, isAccountDisabled);

        if (isMustChangePassword)
            MustChangePasswordAsync(newUser);

        if (user.getInitials() != null && !user.getInitials().isEmpty())
            ldapService.updateProperty(newUser.getDistinguishedName(), "initials", user.getInitials());

        return newUser;
    }

    public void ChangeUserAccountControl(UserEntry user, boolean isCannotChangePassword, boolean isPasswordNeverExpires, boolean isAccountDisabled) {
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

    public void resetPassword(UserEntry user, String password) {
        ldapService.updateProperty(user.getDistinguishedName(), "userPassword", password);
        ldapService.updateProperty(user.getDistinguishedName(), "pwdLastSet", "0");
    }

    public LdapService getLdapService() {
        return ldapService;
    }

}