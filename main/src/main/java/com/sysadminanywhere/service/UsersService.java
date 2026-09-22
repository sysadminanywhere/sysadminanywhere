package com.sysadminanywhere.service;

import com.sysadminanywhere.common.PageResponse;

import com.sysadminanywhere.client.directory.UsersServiceClient;
import com.sysadminanywhere.common.directory.dto.AddUserDto;
import com.sysadminanywhere.common.directory.dto.ChangeUserAccountControlDto;
import com.sysadminanywhere.common.directory.dto.BulkOperationResult;
import com.sysadminanywhere.common.directory.dto.BulkUserAccountStatusDto;
import com.sysadminanywhere.common.directory.dto.BulkDeleteDto;
import com.sysadminanywhere.common.directory.dto.EntryDto;
import com.sysadminanywhere.common.directory.dto.ResetPasswordDto;
import com.sysadminanywhere.common.directory.model.UserEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class UsersService {

    private final LdapService ldapService;
    private final UsersServiceClient usersServiceClient;
    private final WebhookService webhookService;

    public UsersService(LdapService ldapService, UsersServiceClient usersServiceClient, WebhookService webhookService) {
        this.ldapService = ldapService;
        this.usersServiceClient = usersServiceClient;
        this.webhookService = webhookService;
    }

    public Page<UserEntry> getAll(Pageable pageable, String filters, String... attributes) {
        try {
            PageResponse<UserEntry> response = usersServiceClient.getAll(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                pageable.getSort().toString(),
                filters,
                attributes
            );
            return new PageImpl<>(response.content(), PageRequest.of(response.page(), response.size()), response.totalElements());
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

        UserEntry created = usersServiceClient.add(new AddUserDto(distinguishedName,
                user.getCn(),
                user.getDisplayName(),
                user.getFirstName(),
                user.getLastName(),
                user.getInitials(),
                password,
                isCannotChangePassword,
                isPasswordNeverExpires,
                isAccountDisabled,
                isMustChangePassword));
        if (webhookService != null && created != null) webhookService.publish("user.created", created);
        return created;
    }

    public UserEntry update(UserEntry user) {
        UserEntry updated = usersServiceClient.update(user);
        if (webhookService != null && updated != null) webhookService.publish("user.updated", updated);
        return updated;
    }

    public void delete(String distinguishedName) {
        usersServiceClient.delete(distinguishedName);
        if (webhookService != null) webhookService.publish("user.deleted", Map.of("distinguishedName", String.valueOf(distinguishedName)));
    }

    public void changeUserAccountControl(UserEntry user, boolean isCannotChangePassword, boolean isPasswordNeverExpires, boolean isAccountDisabled, boolean isMustChangePassword) {
        usersServiceClient.changeUserAccountControl(new ChangeUserAccountControlDto(user,
                isCannotChangePassword,
                isPasswordNeverExpires,
                isAccountDisabled,
                isMustChangePassword));
    }

    public BulkOperationResult bulkChangeAccountStatus(List<UserEntry> users, boolean disabled) {
        List<String> distinguishedNames = users.stream()
                .map(UserEntry::getDistinguishedName)
                .filter(name -> name != null && !name.isBlank())
                .toList();
        return usersServiceClient.bulkChangeAccountStatus(
                new BulkUserAccountStatusDto(distinguishedNames, disabled));
    }

    public BulkOperationResult bulkDelete(List<UserEntry> users) {
        List<String> distinguishedNames = users.stream()
                .map(UserEntry::getDistinguishedName)
                .filter(name -> name != null && !name.isBlank())
                .toList();
        return usersServiceClient.bulkDelete(new BulkDeleteDto(distinguishedNames));
    }

    public BulkOperationResult bulkDeleteDistinguishedNames(List<String> distinguishedNames) {
        return usersServiceClient.bulkDelete(new BulkDeleteDto(distinguishedNames));
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
