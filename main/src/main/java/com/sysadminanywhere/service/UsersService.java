package com.sysadminanywhere.service;

import com.sysadminanywhere.client.directory.UsersServiceClient;
import com.sysadminanywhere.common.directory.dto.AddUserDto;
import com.sysadminanywhere.common.directory.dto.ChangeUserAccountControlDto;
import com.sysadminanywhere.common.directory.dto.EntryDto;
import com.sysadminanywhere.common.directory.dto.ResetPasswordDto;
import com.sysadminanywhere.common.directory.model.UserEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UsersService {

    private final LdapService ldapService;
    private final UsersServiceClient usersServiceClient;

    public UsersService(LdapService ldapService, UsersServiceClient usersServiceClient) {
        this.ldapService = ldapService;
        this.usersServiceClient = usersServiceClient;
    }

    public Page<UserEntry> getAll(Pageable pageable, String filters, String... attributes) {
        try {
            return usersServiceClient.getAll(pageable, filters, attributes);
        } catch (Exception e) {
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }
    }

    public List<UserEntry> getAll(String filters, String... attributes) {
        try {
            return usersServiceClient.getList(filters, attributes);
        } catch (Exception e) {
            return null;
        }
    }

    public List<UserEntry> getAll() {
        List<EntryDto> list = ldapService.searchWithAttributes("(&(objectClass=user)(objectCategory=person))",
                "cn", "useraccountcontrol");

        List<UserEntry> items = new ArrayList<>();

        if(list != null) {
            for (EntryDto entryDto : list) {
                UserEntry item = new UserEntry();
                item.setCn(entryDto.getAttributes().get("cn").toString());
                item.setUserAccountControl(Integer.parseInt(entryDto.getAttributes().get("useraccountcontrol").toString()));
                items.add(item);
            }
        }

        return items;
    }

    public UserEntry getByCN(String cn) {
        return usersServiceClient.getByCN(cn);
    }

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

    public UserEntry update(UserEntry user) {
        return usersServiceClient.update(user);
    }

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