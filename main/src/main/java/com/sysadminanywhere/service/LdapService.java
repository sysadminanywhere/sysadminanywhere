package com.sysadminanywhere.service;

import com.sysadminanywhere.client.directory.LdapServiceClient;
import com.sysadminanywhere.common.directory.dto.AuditDto;
import com.sysadminanywhere.common.directory.dto.EntryDto;
import com.sysadminanywhere.common.directory.dto.SearchDto;
import com.sysadminanywhere.common.directory.model.Container;
import com.sysadminanywhere.common.directory.model.Containers;
import com.sysadminanywhere.domain.SearchScope;
import com.sysadminanywhere.model.Entry;
import com.vaadin.flow.spring.security.AuthenticationContext;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class LdapService {

    private final LdapServiceClient ldapServiceClient;

    private EntryDto rootDse;

    @Autowired
    private AuthenticationContext authenticationContext;

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
    public LdapService(LdapServiceClient ldapServiceClient) {
        this.ldapServiceClient = ldapServiceClient;
    }

    @SneakyThrows
    public String getBaseDn() {
        if (getRootDse() != null)
            return getRootDse().getAttributes().get("rootdomainnamingcontext").toString();
        else
            return "";
    }

    public String getDefaultNamingContext() {
        return getBaseDn();
    }

    public String getDomainName() {
        if (!getDefaultNamingContext().isEmpty())
            return getDefaultNamingContext().toUpperCase().replace("DC=", "").replace(",", ".").toLowerCase();
        else
            return "";
    }

    public EntryDto getRootDse() {
        try {
            if (rootDse == null) {
                rootDse = ldapServiceClient.getRootDse();
            }
            return rootDse;
        } catch (feign.FeignException fx) {
            log.error("Feign error with status: {}", fx.status(), fx);
            if (fx.status() == 401) {
                authenticationContext.logout();
            }
        } catch (NullPointerException ne) {
        }
        return null;
    }

    @Cacheable(value = "maxPwdAge")
    public long getMaxPwdAge() {
        List<EntryDto> list = search("(objectclass=*)", SearchScope.ONELEVEL);
        Optional<EntryDto> entry = list.stream().filter(c -> c.getAttributes().get("cn").toString().equalsIgnoreCase("builtin")).findFirst();
        if (entry.isPresent()) {
            long result = Long.parseLong(entry.get().getAttributes().get("maxpwdage").toString());
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
        return search(getBaseDn(), filter, SearchScope.SUBTREE, null);
    }

    public List<EntryDto> searchWithAttributes(String filter, String... attributes) {
        try {
            return ldapServiceClient.getSearch(new SearchDto(getBaseDn(), filter, SearchScope.SUBTREE.ordinal(), attributes));
        } catch (Exception e) {
            return null;
        }
    }

    @SneakyThrows
    public List<EntryDto> search(String filter, SearchScope searchScope) {
        return search(getBaseDn(), filter, searchScope, null);
    }

    @SneakyThrows
    public List<EntryDto> search(String dn, String filter, SearchScope searchScope) {
        return search(dn, filter, searchScope, null);
    }

    @SneakyThrows
    public List<EntryDto> search(String dn, String filter, SearchScope searchScope, Sort sort) {
        try {
            return ldapServiceClient.getSearch(new SearchDto(dn, filter, searchScope.ordinal()));
        } catch (Exception e) {
            return null;
        }
    }

    public Page<Entry> search(Pageable pageable, String dn, String filter, SearchScope searchScope, Sort sort) {
        try {
//            Page<EntryDto> dtos = ldapServiceClient.getSearch(pageable, new SearchDto(dn, filter, searchScope.ordinal()));
//            return dtos.map(this::convertToEntity);
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        } catch (Exception e) {
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }
    }

    private Entry convertToEntity(EntryDto dto) {
        return Entry.builder()
                .cn(dto.getAttributes().get("cn").toString())
                .type(dto.getAttributes().get("objectClass").toString())
                .description(dto.getAttributes().get("description") != null ? dto.getAttributes().get("description").toString() : "")
                .build();
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

        List<EntryDto> result = search("(objectclass=domain)", SearchScope.OBJECT);
        Optional<EntryDto> entry = result.stream().findFirst();

        if (entry.isPresent()) {
            Object attr = entry.get().getAttributes().get("wellknownobjects");
            if (attr instanceof List) {
                List<Object> lst = (List<Object>) attr;
                for (Object v : lst) {
                    list.add(v.toString());
                }
            }
        }

        return list;
    }

    public Boolean login(String userName, String password) {
        return false;
    }

    @Cacheable(value = "containers")
    public Containers getContainers() {
        Containers containers = new Containers();

        List<EntryDto> list = search("(objectclass=*)", SearchScope.ONELEVEL);
        for (EntryDto entry : list) {
            if (entry.getAttributes().get("name") != null
                    && (entry.getAttributes().get("showinadvancedviewonly") == null
                    || entry.getAttributes().get("showinadvancedviewonly").toString().equalsIgnoreCase("false"))) {

                Container container = new Container(entry.getAttributes().get("name").toString(), entry.getAttributes().get("distinguishedname").toString(), null);
                containers.getContainers().add(container);

                getChild(containers, container);
            }
        }

        return containers;
    }

    @SneakyThrows
    private void getChild(Containers containers, Container parent) {

        List<EntryDto> list = search(parent.getDistinguishedName(), "(objectclass=*)", SearchScope.ONELEVEL);
        for (EntryDto entry : list) {

            boolean organizationalUnit = false;
            Object attr = entry.getAttributes().get("objectclass");
            if (attr instanceof List) {
                List<Object> lst = (List<Object>) attr;
                for (Object v : lst) {
                    if (v.toString().equalsIgnoreCase("organizationalunit"))
                        organizationalUnit = true;
                }
            }

            if (entry.getAttributes().get("name") != null && organizationalUnit) {
                Container container = new Container(entry.getAttributes().get("name").toString(), entry.getAttributes().get("distinguishedname").toString(), parent);
                containers.getContainers().add(container);
                getChild(containers, container);
            }
        }
    }

    public Page<AuditDto> getAudit(Pageable pageable, Map<String, Object> filters) {
        try {
            return ldapServiceClient.getAudit(pageable, filters);
        } catch (Exception e) {
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }
    }

    public List<AuditDto> getAuditList(Map<String, Object> filters) {
        try {
            return ldapServiceClient.getAuditList(filters);
        } catch (Exception e) {
            return null;
        }
    }

    public boolean deleteMember(String dn, String group) {
        try {
            return ldapServiceClient.deleteMember(dn, group);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean addMember(String dn, String group) {
        try {
            return ldapServiceClient.addMember(dn, group);
        } catch (Exception e) {
            return false;
        }
    }

}