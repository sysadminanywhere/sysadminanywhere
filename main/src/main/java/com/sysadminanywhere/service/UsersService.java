package com.sysadminanywhere.service;

import com.sysadminanywhere.client.directory.UsersServiceClient;
import com.sysadminanywhere.common.directory.dto.AddUserDto;
import com.sysadminanywhere.common.directory.dto.ChangeUserAccountControlDto;
import com.sysadminanywhere.common.directory.dto.ResetPasswordDto;
import com.sysadminanywhere.common.directory.model.UserEntry;
import lombok.SneakyThrows;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsersService {

    private final LdapService ldapService;
    private final UsersServiceClient usersServiceClient;

    ResolveService<UserEntry> resolveService = new ResolveService<>(UserEntry.class);

    public UsersService(LdapService ldapService, UsersServiceClient usersServiceClient) {
        this.ldapService = ldapService;
        this.usersServiceClient = usersServiceClient;
    }

    @SneakyThrows
    public Page<UserEntry> getAll(Pageable pageable, String filters) {
        return usersServiceClient.getAll(pageable, filters);
    }

    public List<UserEntry> getAll(String filters) {
        return usersServiceClient.getList(filters);
    }

    public UserEntry getByCN(String cn) {
        return usersServiceClient.getList("(&(objectClass=user)(objectCategory=person)(cn=" + cn + "))").getFirst();
    }

    @SneakyThrows
    public UserEntry add(String distinguishedName,
                         UserEntry user,
                         String password,
                         boolean isCannotChangePassword,
                         boolean isPasswordNeverExpires,
                         boolean isAccountDisabled,
                         boolean isMustChangePassword) {

        return usersServiceClient.add(new AddUserDto(distinguishedName,
                user,
                password,
                isCannotChangePassword,
                isPasswordNeverExpires,
                isAccountDisabled,
                isMustChangePassword));
    }

    @SneakyThrows
    public UserEntry update(UserEntry user) {
        return usersServiceClient.update(user);
    }

    @SneakyThrows
    public void delete(String distinguishedName) {
        usersServiceClient.delete(distinguishedName);
    }

    public void changeUserAccountControl(UserEntry user, boolean isCannotChangePassword, boolean isPasswordNeverExpires, boolean isAccountDisabled, boolean isMustChangePassword) {
        usersServiceClient.changeUserAccountControl(new ChangeUserAccountControlDto(user,
                isCannotChangePassword,
                isPasswordNeverExpires,
                isAccountDisabled,
                isMustChangePassword));
    }

    public String getDefaultContainer() {
        return ldapService.getUsersContainer();
    }

    public void resetPassword(UserEntry user, String password) {
        usersServiceClient.resetPassword(new ResetPasswordDto(user.getDistinguishedName(), password));
    }

    public LdapService getLdapService() {
        return ldapService;
    }

}