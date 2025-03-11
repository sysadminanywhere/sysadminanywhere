package com.sysadminanywhere.service;

import com.sysadminanywhere.domain.DirectorySetting;
import com.sysadminanywhere.model.AuditItem;
import com.sysadminanywhere.model.Container;
import com.sysadminanywhere.model.Containers;
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
import org.apache.directory.ldap.client.api.LdapConnectionConfig;
import org.springframework.cache.annotation.CacheEvict;
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
    private final LdapConnectionConfig ldapConnectionConfig;
    private final DirectorySetting directorySetting;

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
    public LdapService(LdapConnection connection, LdapConnectionConfig ldapConnectionConfig, DirectorySetting directorySetting) {
        this.connection = connection;
        this.ldapConnectionConfig = ldapConnectionConfig;
        this.directorySetting = directorySetting;

        Entry entry = connection.getRootDse();
        baseDn = new Dn(entry.get("rootdomainnamingcontext").get().getString());
        defaultNamingContext = baseDn.getName();
        domainName = defaultNamingContext.toUpperCase().replace("DC=", "").replace(",", ".").toLowerCase();
    }

    public String getDefaultNamingContext() {
        return defaultNamingContext;
    }

    public String getDomainName() {
        return domainName;
    }

    @SneakyThrows
    public List<Entry> search(String filter, Sort sort) {
        return search(baseDn, filter, SearchScope.SUBTREE, sort);
    }

    @SneakyThrows
    public List<Entry> search(String filter) {
        return search(baseDn, filter, SearchScope.SUBTREE, null);
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
    public Page<AuditItem> getAudit(Pageable pageable, Map<String, Object> filters) {
        List<AuditItem> list = getAudit(filters);

        if (list.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), list.size());

        if (start >= list.size()) {
            return new PageImpl<>(Collections.emptyList(), pageable, list.size());
        }

        return new PageImpl<>(list.subList(start, end), pageable, list.size());
    }

    @SneakyThrows
    @Cacheable(value = "ldap_audit", key = "{#filters}")
    private List<AuditItem> getAudit(Map<String, Object> filters) {

        LocalDate startDateFilter = filters.get("startDate") != null ? (LocalDate) filters.get("startDate") : LocalDate.now();
        LocalDate endDateFilter = filters.get("endDate") != null ? (LocalDate) filters.get("endDate") : LocalDate.now();

        String startDate = startDateFilter.atStartOfDay(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyyMMdd000000.0Z"));
        String endDate = endDateFilter.atStartOfDay(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyyMMdd235959.0Z"));

        List<Entry> list = search(baseDn, "(&(whenChanged>=" + startDate + ")(whenChanged<=" + endDate + "))", SearchScope.SUBTREE);
        List<AuditItem> content = new ArrayList<>();

        if (!list.isEmpty()) {
            for (Entry entry : list) {
                AuditItem item = new AuditItem();

                item.setName(entry.get("name").getString());
                item.setDistinguishedName(entry.getDn().getName());

                Value whenCreatedValue = entry.get("whencreated") != null ? entry.get("whencreated").get() : null;
                Value whenChangedValue = entry.get("whenchanged") != null ? entry.get("whenchanged").get() : null;

                if (whenChangedValue != null && whenChangedValue != null) {

                    String whenCreated = whenCreatedValue.getString();
                    String whenChanged = whenChangedValue.getString();

                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss'.0Z'");

                    item.setWhenCreated(LocalDateTime.parse(whenCreated, formatter));
                    item.setWhenChanged(LocalDateTime.parse(whenChanged, formatter));

                    String action = item.getWhenChanged().isAfter(item.getWhenCreated()) ? "Changed" : "Created";
                    item.setAction(action);

                    content.add(item);
                }

            }
        }
        return content;
    }

}