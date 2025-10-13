package com.sysadminanywhere.inventory.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ComputerEntry {

    @AD(name = "cn", IsReadOnly = true)
    private String cn;

    @AD(name = "whencreated", IsReadOnly = true)
    private LocalDateTime created;

    @AD(name = "description")
    private String description;

    @AD(name = "distinguishedname", IsReadOnly = true)
    private String distinguishedName;

    @AD(name = "whenchanged", IsReadOnly = true)
    private LocalDateTime modified;

    @AD(name = "objectcategory", IsReadOnly = true)
    private String objectCategory;

    @AD(name = "objectclass", IsReadOnly = true)
    private List<String> objectClass;

    @AD(name = "objectguid", IsReadOnly = true)
    private UUID objectGUID;

    @AD(name = "objectsid", IsReadOnly = true)
    private ADSID sid;

    @AD(name = "samaccountname")
    private String samAccountName;

    @AD(name = "accountexpires")
    private LocalDateTime accountExpirationDate;

    @AD(name = "badpwdcount", IsReadOnly = true)
    private int badLogonCount;

    @AD(name = "badpasswordtime", IsReadOnly = true)
    private LocalDateTime lastBadPasswordAttempt;

    @AD(name = "lastlogon", IsReadOnly = true)
    private LocalDateTime lastLogon;

    @AD(name = "location")
    private String location;

    @AD(name = "managedby")
    private String managedBy;

    @AD(name = "memberof", IsReadOnly = true)
    private List<String> memberOf;

    @AD(name = "operatingsystem", IsReadOnly = true)
    private String operatingSystem;

    @AD(name = "operatingsystemhotfix", IsReadOnly = true)
    private String operatingSystemHotfix;

    @AD(name = "operatingsystemservicepack", IsReadOnly = true)
    private String operatingSystemServicePack;

    @AD(name = "operatingsystemversion", IsReadOnly = true)
    private String operatingSystemVersion;

    @AD(name = "pwdlastset")
    private LocalDateTime passwordLastSet;

    @AD(name = "serviceprincipalname")
    private List<String> servicePrincipalNames;

    @AD(name = "primarygroupid")
    private int primaryGroupId;

    @AD(name = "useraccountcontrol")
    private int userAccountControl;

    @AD(name = "msds-supportedencryptiontypes")
    private int msdsSupportedEncryptionTypes;

    @AD(name = "iscriticalsystemobject")
    private boolean isCriticalSystemObject;

    @AD(name = "dnshostname")
    private String dnsHostName;

    @AD(name = "samaccounttype")
    private int samAccountType;

    @AD(name = "countrycode")
    private int countryCode;

    @AD(name = "localpolicyflags")
    private int localPolicyFlags;

    @AD(name = "logoncount")
    private int logonCount;

    @AD(name = "adspath")
    private String adsPath;

    @AD(name = "name")
    private String name;

    @AD(name = "lastlogoff")
    private int lastLogoff;

    @AD(name = "instancetype")
    private int instanceType;

    @AD(name = "codepage")
    private int codepage;

    public boolean isDisabled() {
        return ((userAccountControl & UserAccountControls.ACCOUNTDISABLE.getValue()) != 0);
    }

    public boolean isWorkstation() {
        return !operatingSystem.contains("Server");
    }

    public boolean isServer() {
        return operatingSystem.contains("Server");
    }

    public boolean isDomainController() {
        return primaryGroupId == 516;
    }

}