package com.sysadminanywhere.directory.service;

import com.sysadminanywhere.common.directory.dto.AuditDto;
import com.sysadminanywhere.common.directory.dto.EntryDto;
import com.sysadminanywhere.common.directory.dto.JwtResponse;
import com.sysadminanywhere.common.directory.model.Container;
import com.sysadminanywhere.common.directory.model.Containers;
import io.jsonwebtoken.security.Keys;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.directory.api.ldap.model.cursor.SearchCursor;
import org.apache.directory.api.ldap.model.entry.*;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.message.*;
import org.apache.directory.api.ldap.model.message.controls.*;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.ldap.client.api.*;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class LdapService {

    private final JwtService jwtService;
    private final VaultService vaultService;

    private final LdapConnectionPool sharedPool;
    private final UserConnectionManager userConnectionManager;

    private String domainName;
    private String defaultNamingContext;
    private Dn baseDn;
    private Entry domainEntry;

    private static final String SECRET = "MySuperSecretKeyForJWTValidation123456";
    private static final Key KEY = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

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
    public LdapService(JwtService jwtService,
                       VaultService vaultService,
                       LdapConnectionPool sharedPool,
                       UserConnectionManager userConnectionManager) {

        this.jwtService = jwtService;
        this.vaultService = vaultService;
        this.sharedPool = sharedPool;
        this.userConnectionManager = userConnectionManager;

        domainEntry = getRootDse();
        baseDn = new Dn(domainEntry.get("rootdomainnamingcontext").get().getString());
        defaultNamingContext = baseDn.getName();
        domainName = defaultNamingContext.toUpperCase().replace("DC=", "").replace(",", ".").toLowerCase();
    }

    public Entry getRootDse() {
        return execute(conn -> {
            Entry root = conn.getRootDse();
            return (root != null) ? (Entry) root.clone() : null;
        });
    }

    public String getDefaultNamingContext() {
        return defaultNamingContext;
    }

    public String getDomainName() {
        return domainName;
    }

    @SneakyThrows
    public Entry getDomainEntry() {
        return domainEntry;
    }

    public Dn getBaseDn() {
        return baseDn;
    }

    public EntryDto convertEntry(Entry entry) {
        EntryDto entryDto = new EntryDto();

        entryDto.setDn(entry.getDn().getName());
        Map<String, Object> map = new HashMap<>();

        for (Attribute attribute : entry.getAttributes()) {
            List<Object> values = new ArrayList<>();

            for (Value value : attribute) {
                if (value.isHumanReadable()) {
                    values.add(value.getString());
                } else {
                    values.add(value.getBytes());
                }
            }

            if (values.size() == 1) {
                map.put(attribute.getId(), values.get(0));
            } else {
                map.put(attribute.getId(), values);
            }
        }

        entryDto.setAttributes(map);

        return entryDto;
    }

    public List<EntryDto> convertEntryList(List<Entry> entries) {
        List<EntryDto> list = new ArrayList<>();

        for (Entry entry : entries) {
            list.add(convertEntry(entry));
        }

        return list;
    }

    @SneakyThrows
    public Page<Entry> searchPage(String filter, Sort sort, Pageable pageable, String... attributes) {
        return searchPage(baseDn, filter, SearchScope.SUBTREE, pageable, attributes);
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
    public Long count(String filter) {
        return count(baseDn, filter, SearchScope.SUBTREE);
    }

    @SneakyThrows
    public Long count(Dn dn, String filter, SearchScope searchScope) {
        return executeAsUser(conn -> {
            long totalElements = 0;

            SearchRequest countRequest = new SearchRequestImpl();
            countRequest.setBase(dn);
            countRequest.setFilter(filter);
            countRequest.setScope(searchScope);

            // Оптимизация сетевого трафика:
            countRequest.setTypesOnly(true);
            // Запрашиваем только спец. атрибут "1.1" (означает "без атрибутов")
            countRequest.addAttributes("1.1");

            countRequest.setTimeLimit(0); // Без ограничений по времени (осторожно!)

            try (SearchCursor countCursor = conn.search(countRequest)) {
                while (countCursor.next()) {
                    // Нам не нужно делать .get(), просто считаем наличие следующей записи
                    totalElements++;
                }
            }

            return totalElements;
        });
    }

    @SneakyThrows
    public List<Entry> search(Dn dn, String filter, SearchScope searchScope, Sort sort) {
        return search(dn, filter, searchScope, sort, "*");
    }

    @SneakyThrows
    public List<Entry> searchWithAttributes(String filter, String... attributes) {
        return search(baseDn, filter, SearchScope.SUBTREE, null, attributes);
    }

    @SneakyThrows
    public List<Entry> searchWithAttributes(Dn dn, String filter, SearchScope searchScope, String... attributes) {
        return search(dn, filter, searchScope, null, attributes);
    }

    @SneakyThrows
    public List<Entry> search(Dn dn, String filter, SearchScope searchScope, Sort sort, String... attributes) {
        return executeAsUser(conn -> {
            List<Entry> list = new ArrayList<>();
            int pageSize = 100;

            SearchRequest searchRequest = new SearchRequestImpl();
            searchRequest.setBase(dn);
            searchRequest.setFilter(filter);
            searchRequest.setScope(searchScope);
            searchRequest.addAttributes(attributes);
            searchRequest.setTimeLimit(0);

            // 1. Настройка сортировки
            String sortKey = "cn";
            boolean reverseOrder = false;
            if (sort != null && sort.isSorted()) {
                Sort.Order order = sort.iterator().next();
                sortKey = order.getProperty();
                reverseOrder = order.isDescending();
            }

            SortRequest sortControl = new SortRequestImpl();
            sortControl.addSortKey(new SortKey(sortKey, "2.5.13.1", reverseOrder)); // 2.5.13.1 - стандартное правило сопоставления
            searchRequest.addControl(sortControl);

            // 2. Настройка пагинации
            PagedResults pagedControl = new PagedResultsImpl();
            pagedControl.setSize(pageSize);
            searchRequest.addControl(pagedControl);

            byte[] cookie = null;

            do {
                // ОБЯЗАТЕЛЬНО обновляем куку в контроле перед каждым поиском
                pagedControl.setCookie(cookie);

                try (SearchCursor cursor = conn.search(searchRequest)) {
                    while (cursor.next()) {
                        Response response = cursor.get();
                        if (response instanceof SearchResultEntry) {
                            list.add((Entry) ((SearchResultEntry) response).getEntry().clone());
                        }
                    }

                    // 3. Получение новой куки из ответа сервера
                    SearchResultDone done = cursor.getSearchResultDone();
                    PagedResults responseControl = (PagedResults) done.getControl(PagedResults.OID);

                    if (responseControl != null) {
                        cookie = responseControl.getCookie();
                    } else {
                        cookie = null;
                    }
                }
            } while (cookie != null && cookie.length > 0);

            return list;
        });
    }

    @SneakyThrows
    public Page<Entry> searchPage(Dn dn, String filter, SearchScope searchScope, Pageable pageable, String... attributes) {
        // 1. Получаем общее количество (Total)
        long totalElements = count(dn, filter, searchScope);

        if (totalElements == 0) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        // 2. Получаем контент страницы
        List<Entry> content = executeAsUser(conn -> {
            List<Entry> pageList = new ArrayList<>();
            int pageSize = pageable.getPageSize();
            int offset = (int) pageable.getOffset();
            int currentCount = 0;

            SearchRequest request = new SearchRequestImpl();
            request.setBase(dn);
            request.setFilter(filter);
            request.setScope(searchScope);
            request.addAttributes(attributes);
            request.setTimeLimit(0);

            // Настройка сортировки (важно для стабильности страниц)
            String sortKey = "cn";
            boolean isDesc = false;
            if (pageable.getSort().isSorted()) {
                Sort.Order order = pageable.getSort().iterator().next();
                sortKey = order.getProperty();
                isDesc = order.isDescending();
            }
            SortRequest sortControl = new SortRequestImpl();
            sortControl.addSortKey(new SortKey(sortKey, "2.5.13.1", isDesc));
            request.addControl(sortControl);

            // Настройка пагинации
            PagedResults pagedControl = new PagedResultsImpl();
            pagedControl.setSize(pageSize);
            request.addControl(pagedControl);

            byte[] cookie = null;

            outerLoop: // Метка для выхода из всех циклов сразу
            do {
                pagedControl.setCookie(cookie);
                request.removeControl(pagedControl);
                pagedControl.setCookie(cookie);
                request.addControl(pagedControl);

                try (SearchCursor cursor = conn.search(request)) {
                    while (cursor.next()) {
                        // Если мы дошли до нужного смещения (offset)
                        if (currentCount >= offset) {
                            Response response = cursor.get();
                            if (response instanceof SearchResultEntry) {
                                Entry entry = ((SearchResultEntry) response).getEntry();
                                pageList.add((Entry) entry.clone());
                            }
                        }

                        currentCount++;

                        // Если страница заполнена - ПРЕРЫВАЕМ всё
                        if (pageList.size() >= pageSize) {
                            break outerLoop;
                        }
                    }

                    SearchResultDone done = cursor.getSearchResultDone();
                    PagedResults responseControl = (PagedResults) done.getControl(PagedResults.OID);
                    cookie = (responseControl != null) ? responseControl.getCookie() : null;
                }
            } while (cookie != null && cookie.length > 0);

            return pageList;
        });

        return new PageImpl<>(content, pageable, totalElements);
    }

    @SneakyThrows
    public void add(Entry entry) {
        executeAsUser(conn -> {
            AddRequest addRequest = new AddRequestImpl();
            addRequest.setEntry(entry);
            addRequest.addControl(new ManageDsaITImpl());
            conn.add(addRequest);
            return null;
        });
    }

    @SneakyThrows
    public void update(ModifyRequest modifyRequest) {
        executeAsUser(conn -> {
            conn.modify(modifyRequest);
            return null;
        });
    }

    @SneakyThrows
    public void delete(Entry entry) {
        executeAsUser(conn -> {
            conn.delete(entry.getDn());
            return null;
        });
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
        executeAsUser(conn -> {
            Attribute attribute = new DefaultAttribute(name, value);
            Modification modification = new DefaultModification(ModificationOperation.REPLACE_ATTRIBUTE, attribute);
            conn.modify(dn, modification);
            return null;
        });
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
    public Page<AuditDto> getAudit(Pageable pageable, Map<String, String> filters) {
        List<AuditDto> list = getAuditList(filters);

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
    public List<AuditDto> getAuditList(Map<String, String> filters) {

        LocalDate startDateFilter = filters.get("startDate") != null ? LocalDate.parse(filters.get("startDate")) : LocalDate.now();
        LocalDate endDateFilter = filters.get("endDate") != null ? LocalDate.parse(filters.get("endDate")) : LocalDate.now();

        String startDate = startDateFilter.atStartOfDay(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyyMMdd000000.0Z"));
        String endDate = endDateFilter.atStartOfDay(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyyMMdd235959.0Z"));

        List<Entry> list = search(baseDn, "(&(whenChanged>=" + startDate + ")(whenChanged<=" + endDate + "))", SearchScope.SUBTREE);
        List<AuditDto> content = new ArrayList<>();

        if (!list.isEmpty()) {
            for (Entry entry : list) {
                AuditDto item = new AuditDto();

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
        content.sort(Comparator.comparing(AuditDto::getWhenChanged).reversed());
        return content;
    }

    public boolean deleteMember(String dn, String group) {
        return executeAsUser(conn -> {
            Modification removeMember = new DefaultModification(
                    ModificationOperation.REMOVE_ATTRIBUTE, "member", dn
            );

            ModifyRequest modifyRequest = new ModifyRequestImpl();
            modifyRequest.setName(new Dn(group));

            modifyRequest.addModification(removeMember);

            ModifyResponse response = conn.modify(modifyRequest);

            return true;
        });
    }

    public boolean addMember(String dn, String group) {
        return executeAsUser(conn -> {
            Modification removeMember = new DefaultModification(
                    ModificationOperation.ADD_ATTRIBUTE, "member", dn
            );

            ModifyRequest modifyRequest = new ModifyRequestImpl();
            modifyRequest.setName(new Dn(group));

            modifyRequest.addModification(removeMember);
            ModifyResponse response = conn.modify(modifyRequest);

            return true;
        });
    }

    @SneakyThrows
    public JwtResponse authenticate(String username, String password) {
        boolean authenticated = execute(conn -> {
            conn.bind(createBindRequest(username, password));
            vaultService.savePassword(username, password);
            return true;
        });

        String jwt = null;
        List<String> roles = new ArrayList<>();

        if (authenticated) {
            roles = List.of("ROLE_ADMIN");
            jwt = jwtService.generateToken(username, roles);
        }

        return new JwtResponse(jwt, username, roles);
    }

    private BindRequest createBindRequest(String username, String password) {
        BindRequest bindRequest = new BindRequestImpl();
        bindRequest.setName(username);
        bindRequest.setCredentials(password);
        bindRequest.setSimple(true);
        return bindRequest;
    }

    private <T> T execute(LdapConnectionOperation<T> operation) {
        LdapConnection connection = null;
        try {
            connection = sharedPool.getConnection();

            // Базовая проверка состояния (пул обычно это делает сам, но для надежности)
            if (!connection.isConnected()) {
                connection.connect();
            }

            return operation.execute(connection);

        } catch (Exception e) {
            log.error("LDAP operation failed", e);
            throw new RuntimeException("LDAP error", e);
        } finally {
            if (connection != null) {
                try {
                    sharedPool.releaseConnection(connection);
                } catch (Exception e) {
                    log.warn("Failed to release connection to pool", e);
                }
            }
        }
    }

    private <T> T executeAsUser(LdapConnectionOperation<T> operation) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        String password = vaultService.getPassword(username);

        LdapConnection connection = null;
        try {
            // Пул сам сделает connect и bind под этим юзером
            connection = userConnectionManager.getConnection(username, password);
            return operation.execute(connection);
        } catch (Exception e) {
            log.error("LDAP error", e);
            throw new RuntimeException(e);
        } finally {
            if (connection != null) {
                // ВАЖНО: просто закрываем, чтобы вернуть в пул.
                // НЕ вызывайте unBind() здесь!
                try { connection.close(); } catch (Exception e) { /* ignore */ }
            }
        }
    }

    // Функциональный интерфейс для лямбда-выражений
    @FunctionalInterface
    interface LdapConnectionOperation<T> {
        T execute(LdapConnection connection) throws Exception;
    }

}