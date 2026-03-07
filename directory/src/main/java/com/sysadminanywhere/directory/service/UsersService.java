package com.sysadminanywhere.directory.service;

import com.sysadminanywhere.common.directory.model.UserAccountControls;
import com.sysadminanywhere.common.directory.model.UserEntry;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.directory.api.ldap.model.entry.DefaultEntry;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class UsersService {

    @Getter
    private final LdapService ldapService;

    ResolveService<UserEntry> resolveService = new ResolveService<>(UserEntry.class);

    public UsersService(LdapService ldapService) {
        this.ldapService = ldapService;
    }

    /**
     * Получение всех пользователей с постраничным выводом и фильтрацией
     */
    @SneakyThrows
    public Page<UserEntry> getAll(@ParameterObject Pageable pageable, String filters, String... attributes) {
        try {
            Page<Entry> result = ldapService.searchPage("(&(objectClass=user)(objectCategory=person)" + filters + ")", pageable.getSort(), pageable, attributes);
            log.info("Retrieved users page with filters");
            return resolveService.getADPage(result);
        } catch (Exception e) {
            log.error("Error retrieving users page: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Получение всех пользователей без постраничного вывода
     */
    public List<UserEntry> getAll(String filters, String... attributes) {
        try {
            List<Entry> result = ldapService.searchWithAttributes("(&(objectClass=user)(objectCategory=person)" + filters + ")", attributes);
            log.info("Retrieved users list with filters");
            return resolveService.getADList(result);
        } catch (Exception e) {
            log.error("Error retrieving users list: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Получение пользователя по CN (Common Name)
     */
    public UserEntry getByCN(String cn) {
        try {
            List<Entry> result = ldapService.search("(&(objectClass=user)(objectCategory=person)(cn=" + cn + "))");
            Optional<Entry> entry = result.stream().findFirst();

            if (entry.isPresent()) {
                log.info("Retrieved user by CN: {}", cn);
                return resolveService.getADValue(entry.get());
            } else {
                log.warn("User not found by CN: {}", cn);
                return null;
            }
        } catch (Exception e) {
            log.error("Error retrieving user by CN {}: {}", cn, e.getMessage());
            throw e;
        }
    }

    /**
     * Создание нового пользователя
     */
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

        try {
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

            log.info("User created: {}", cn);
            return newUser;
        } catch (Exception e) {
            log.error("Error creating user {}: {}", cn, e.getMessage());
            throw e;
        }
    }

    /**
     * Изменение параметров учетной записи пользователя
     */
    public void changeUserAccountControl(UserEntry user, boolean isCannotChangePassword, boolean isPasswordNeverExpires, boolean isAccountDisabled, boolean isMustChangePassword) {
        try {
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

            log.info("User account control updated for: {}", user.getDistinguishedName());
        } catch (Exception e) {
            log.error("Error changing user account control for {}: {}", user.getDistinguishedName(), e.getMessage());
            throw e;
        }
    }

    /**
     * Установка флага обязательной смены пароля
     */
    private void MustChangePasswordAsync(UserEntry user) {
        ldapService.updateProperty(user.getDistinguishedName(), "pwdlastset", "0");
    }

    /**
     * Обновление пользователя
     */
    @SneakyThrows
    public UserEntry update(UserEntry user) {
        try {
            Entry entry = resolveService.getEntry(user);
            Entry oldEntry = resolveService.getEntry(getByCN(user.getCn()));

            ldapService.update(resolveService.getModifyRequest(entry, oldEntry));
            log.info("User updated: {}", user.getDistinguishedName());
            return getByCN(user.getCn());
        } catch (Exception e) {
            log.error("Error updating user {}: {}", user.getDistinguishedName(), e.getMessage());
            throw e;
        }
    }

    /**
     * Удаление пользователя
     */
    @SneakyThrows
    public void delete(String distinguishedName) {
        try {
            Entry entry = new DefaultEntry(distinguishedName);
            ldapService.delete(entry);
            log.info("User deleted: {}", distinguishedName);
        } catch (Exception e) {
            log.error("Error deleting user {}: {}", distinguishedName, e.getMessage());
            throw e;
        }
    }

    /**
     * Получение параметров управления учетной записью
     */
    public UserAccountControls getUserControl(int userAccountControl) {
        return UserAccountControls.fromValue(userAccountControl);
    }

    /**
     * Получение контейнера пользователей по умолчанию
     */
    public String getDefaultContainer() {
        return ldapService.getUsersContainer();
    }

    /**
     * Сброс пароля пользователя
     */
    public void resetPassword(String distinguishedName, String password) {
        try {
            ldapService.updateProperty(distinguishedName, "userPassword", password);
            ldapService.updateProperty(distinguishedName, "pwdLastSet", "0");
            log.info("Password reset for user: {}", distinguishedName);
        } catch (Exception e) {
            log.error("Error resetting password for {}: {}", distinguishedName, e.getMessage());
            throw e;
        }
    }

}