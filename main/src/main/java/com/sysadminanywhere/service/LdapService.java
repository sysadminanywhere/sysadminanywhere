package com.sysadminanywhere.service;

import com.sysadminanywhere.client.directory.LdapServiceClient;
import com.sysadminanywhere.common.directory.dto.AuditDto;
import com.sysadminanywhere.common.directory.dto.EntryDto;
import com.sysadminanywhere.domain.DirectorySetting;
import com.sysadminanywhere.common.directory.model.Container;
import com.sysadminanywhere.common.directory.model.Containers;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.directory.api.ldap.model.cursor.EntryCursor;
import org.apache.directory.api.ldap.model.cursor.SearchCursor;
import org.apache.directory.api.ldap.model.entry.*;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.message.*;
import org.apache.directory.api.ldap.model.message.controls.*;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class LdapService {

    private final LdapConnection connection;
    private final DirectorySetting directorySetting;

    @Autowired
    private LdapServiceClient ldapServiceClient;

    private String domainName;
    private String defaultNamingContext;
    private Dn baseDn;

    private final String ContainerMicrosoft = "B:32:F4BE92A4C777485E878E9421D53087DB:";                 //NOSONAR CN=Microsoft,CN=Program Data,DC=example,DC=com
    private final String ContainerProgramData = "B:32:09460C08AE1E4A4EA0F64AEE7DAA1E5A:";               //NOSONAR CN=Program Data,DC=example,DC=com
    private final String ContainerForeignSecurityPrincipals = "B:32:22B70C67D56E4EFB91E9300FCA3DC1AA:"; //NOSONAR CN=ForeignSecurityPrincipals,DC=example,DC=com
    private final String ContainerDeletedObjects = "B:32:18E2EA80684F11D2B9AA00C04F79F805:";            //NOSONAR CN=Deleted Objects,DC=example,DC=com
    private final String ContainerInfrastructure = "B:32:2FBAC1870ADE11D297C400C04FD8D5CD:";            //NOSONAR CN=Infrastructure,DC=example,DC=com
    private final String ContainerLostAndFound = "B:32:AB8153B7768811D1ADED00C04FD8D5CD:";              //NOSONAR CN=LostAndFound,DC=example,DC=com
    private final String ContainerSystem = "B:32:AB1D30F3768811D1ADED00C04FD8D5CD:";                    //NOSONAR CN=System,DC=example,DC=com
    private final String ContainerDomainControllers = "B:32:A361B2FFFFD211D1AA4B00C04FD7D83A:";         //NOSONAR OU=Domain Controllers,DC=example,DC=com
    private final String ContainerComputers = "B:32:AA312825768811D1ADED00C04FD8D5CD:";                 //NOSONAR CN=Computers,DC=example,DC=com
    private final String ContainerUsers = "B:32:A9D1CA15768811D1ADED00C04FD8D5CD:";                     //NOSONAR CN=Users,DC=example,DC=com
    private final String ContainerNTDSQuotas = "B:32:6227F0AF1FC2410D8E3BB10615BB5B0F:";                //NOSONAR CN=NTDS Quotas,DC=example,DC=com


    @SneakyThrows
    public LdapService(LdapConnection connection, DirectorySetting directorySetting) {
        this.connection = connection;
        this.directorySetting = directorySetting;

        this.connection.bind();

        if(this.connection.isConnected()) {
            Entry entry = connection.getRootDse();
            baseDn = new Dn(entry.get("rootdomainnamingcontext").get().getString());
            defaultNamingContext = baseDn.getName();
            domainName = defaultNamingContext.toUpperCase().replace("DC=", "").replace(",", ".").toLowerCase();
        }
    }

    public String getDefaultNamingContext() {
        return defaultNamingContext;
    }

    public String getDomainName() {
        return domainName;
    }

    @SneakyThrows
    public Entry getDomainEntry() {
        return connection.getRootDse();
    }

    public LdapConnection getConnection() {
        return connection;
    }

    @Cacheable(value = "maxPwdAge")
    public long getMaxPwdAge() {
        List<Entry> list = search("(objectclass=*)", SearchScope.ONELEVEL);
        Optional<Entry> entry = list.stream().filter(c -> c.get("cn").get().getString().equalsIgnoreCase("Builtin")).findFirst();
        if (entry.isPresent()) {
            long result = Long.parseLong(entry.get().get("maxPwdAge").get().getString());
            return result;
        }
        return 0;
    }

    public long getMaxPwdAgeDays() {
        long maxPwdAgeIntervals = getMaxPwdAge();
        long maxPwdAgeSeconds = Math.abs(maxPwdAgeIntervals) / 10_000_000L;
        long maxPwdAgeDays = maxPwdAgeSeconds / (60 * 60 * 24);
        return maxPwdAgeDays;
    }

    public List<EntryDto> search(String filter) {
        return ldapServiceClient.getSearch(filter);
    }

    @SneakyThrows
    public List<Entry> search(String filter, SearchScope searchScope) {
        return search(baseDn, filter, searchScope, null);
    }

    @SneakyThrows
    public List<Entry> search(Dn dn, String filter, SearchScope searchScope) {
        return search(dn, filter, searchScope, null);
    }

    @SneakyThrows
    public List<Entry> search(Dn dn, String filter, SearchScope searchScope, Sort sort) {

        List<Entry> list = new ArrayList<>();

        try {
            SearchRequest searchRequest = new SearchRequestImpl();
            searchRequest.setScope(searchScope);
            searchRequest.addAttributes("*");
            searchRequest.setTypesOnly(false);
            searchRequest.setTimeLimit(0);
            searchRequest.setBase(dn);
            searchRequest.setFilter(filter);

            int pageSize = 100;
            String sortKey = "cn";

            if (sort != null && !sort.isEmpty()) {
                Optional<Sort.Order> order = sort.get().findFirst();
                if (order.isPresent())
                    sortKey = order.get().getProperty();
            }

            SortRequest sortRequest = new SortRequestImpl();
            sortRequest.addSortKey(new SortKey(sortKey));
            searchRequest.addControl(sortRequest);

            PagedResults pagedResults = new PagedResultsImpl();
            pagedResults.setSize(pageSize);
            searchRequest.addControl(pagedResults);

            while (true) {
                try (SearchCursor searchCursor = connection.search(searchRequest)) {
                    while (searchCursor.next()) {
                        Response response = searchCursor.get();
                        if (response instanceof SearchResultEntry) {
                            Entry resultEntry = ((SearchResultEntry) response).getEntry();
                            list.add(resultEntry);
                        }
                    }
                    SearchResultDone resultDone = searchCursor.getSearchResultDone();
                    if (resultDone != null) {
                        PagedResults pageResultResponseControl = (PagedResults) resultDone.getControl(PagedResults.OID);
                        if (pageResultResponseControl == null || pageResultResponseControl.getCookie().length == 0) {
                            break;
                        } else {
                            pagedResults.setCookie(pageResultResponseControl.getCookie());
                        }
                    }
                }
            }

        } catch (LdapException le) {
            log.error("LdapException: {}", le);
        }

        return list;
    }

    @SneakyThrows
    public void add(Entry entry) {
        AddRequest addRequest = new AddRequestImpl();
        addRequest.setEntry(entry);
        addRequest.addControl(new ManageDsaITImpl());

        connection.add(addRequest);
    }

    @SneakyThrows
    public void update(ModifyRequest modifyRequest) {
        if (modifyRequest.getModifications().size() > 0)
            connection.modify(modifyRequest);
    }

    @SneakyThrows
    public void delete(Entry entry) {
        connection.delete(entry.getDn());
    }

    public String getComputersContainer() {
        String result = getWellKnownObjects().stream().filter(c -> c.startsWith(ContainerComputers)).collect(Collectors.toList()).get(0);
        return result.replace(ContainerComputers, "");
    }

    public String getUsersContainer() {
        String result = getWellKnownObjects().stream().filter(c -> c.startsWith(ContainerUsers)).collect(Collectors.toList()).get(0);
        return result.replace(ContainerUsers, "");
    }

    public List<String> getWellKnownObjects() {
        List<String> list = new ArrayList<>();

        List<Entry> result = search("(objectclass=domain)", SearchScope.OBJECT);
        Optional<Entry> entry = result.stream().findFirst();

        if (entry.isPresent()) {
            for (Value v : entry.get().get("wellknownobjects")) {
                list.add(v.toString());
            }
        }

        return list;
    }

    @SneakyThrows
    public void updateProperty(String dn, String name, String value) {
        Attribute attribute = new DefaultAttribute(name, value);
        Modification modification = new DefaultModification(ModificationOperation.REPLACE_ATTRIBUTE, attribute);
        connection.modify(dn, modification);
    }

    public Boolean login(String userName, String password) {

        BindRequest bindRequest = new BindRequestImpl();
        bindRequest.setCredentials(password);
        bindRequest.setSimple(true);
        bindRequest.setName(userName);

        try {
            connection.bind(bindRequest);

            if (connection.isAuthenticated()) {
                if (directorySetting.getGroupsAllowed() != null && !directorySetting.getGroupsAllowed().isEmpty()) {
                    try (EntryCursor cursor = connection.search(baseDn, "(&(objectClass=user)(objectCategory=person)(cn=" + userName + "))", SearchScope.SUBTREE)) {
                        for (Entry entry : cursor) {
                            for (Value v : entry.get("memberof")) {
                                for (String item : directorySetting.getGroupsAllowed()) {
                                    if (v.getString().equalsIgnoreCase(item)) {
                                        return true;
                                    }
                                }
                            }
                            return false;
                        }
                    }
                } else {
                    return true;
                }
            }

        } catch (LdapException | IOException e) {
            log.error("Connection error: {}", e);
        }

        return false;
    }

    @Cacheable(value = "containers")
    public Containers getContainers() {
        Containers containers = new Containers();

        List<Entry> list = search("(objectclass=*)", SearchScope.ONELEVEL);
        for (Entry entry : list) {
            if (entry.get("name") != null
                    && (entry.get("showInAdvancedViewOnly") == null
                    || entry.get("showInAdvancedViewOnly").get().getString().equalsIgnoreCase("false"))) {

                Container container = new Container(entry.get("name").get().getString(), entry.get("distinguishedName").get().getString(), null);
                containers.getContainers().add(container);

                getChild(containers, container);
            }
        }

        return containers;
    }

    @SneakyThrows
    private void getChild(Containers containers, Container parent) {

        List<Entry> list = search(new Dn(parent.getDistinguishedName()), "(objectclass=*)", SearchScope.ONELEVEL);
        for (Entry entry : list) {

            boolean organizationalUnit = false;
            for (Value v : entry.get("objectClass")) {
                if (v.getString().equalsIgnoreCase("organizationalUnit"))
                    organizationalUnit = true;
            }

            if (entry.get("name") != null && organizationalUnit) {
                Container container = new Container(entry.get("name").get().getString(), entry.get("distinguishedName").get().getString(), parent);
                containers.getContainers().add(container);
                getChild(containers, container);
            }
        }
    }

    @SneakyThrows
    public Page<AuditDto> getAudit(Pageable pageable, Map<String, Object> filters) {
        return ldapServiceClient.getAudit(pageable,filters);
    }

    @SneakyThrows
    public List<AuditDto> getAuditList(Map<String, Object> filters) {
        return ldapServiceClient.getAuditList(filters);
    }

    public boolean deleteMember(String dn, String group) {
        try {
            Modification removeMember = new DefaultModification(
                    ModificationOperation.REMOVE_ATTRIBUTE, "member", dn
            );

            ModifyRequest modifyRequest = new ModifyRequestImpl();
            modifyRequest.setName(new Dn(group));

            modifyRequest.addModification(removeMember);
            ModifyResponse response = connection.modify(modifyRequest);

            return true;
        } catch (Exception ex) {
            Notification notification = Notification.show(ex.getMessage());
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);

            return false;
        }
    }

    public boolean addMember(String dn, String group) {
        try {
            Modification removeMember = new DefaultModification(
                    ModificationOperation.ADD_ATTRIBUTE, "member", dn
            );

            ModifyRequest modifyRequest = new ModifyRequestImpl();
            modifyRequest.setName(new Dn(group));

            modifyRequest.addModification(removeMember);
            ModifyResponse response = connection.modify(modifyRequest);

            return true;
        } catch (Exception ex) {
            Notification notification = Notification.show(ex.getMessage());
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);

            return false;
        }
    }


}