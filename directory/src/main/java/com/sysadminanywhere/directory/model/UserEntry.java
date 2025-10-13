package com.sysadminanywhere.directory.model;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserEntry {

    @NotNull
    @AD(name = "cn", IsReadOnly = true)
    private String cn;

    @AD(name = "whencreated", IsReadOnly = true)
    private LocalDateTime created;

    @AD(name = "description")
    private String description;

    @NotNull
    @AD(name = "distinguishedname", IsReadOnly = true)
    private String distinguishedName;

    @AD(name = "displayname")
    private String displayName;

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

    @AD(name = "lockouttime")
    private LocalDateTime accountLockoutTime;

    @AD(name = "l")
    private String city;

    @AD(name = "company")
    private String company;

    @AD(name = "c")
    private String country;

    @AD(name = "department")
    private String department;

    @AD(name = "division")
    private String division;

    @AD(name = "mail")
    private String emailAddress;

    @AD(name = "employeeid")
    private String employeeID;

    @AD(name = "employeenumber")
    private String employeeNumber;

    @AD(name = "facsimiletelephonenumber")
    private String fax;

    @AD(name = "givenName")
    private String firstName;

    @AD(name = "homedirectory")
    private String homeDirectory;

    @AD(name = "homedrive")
    private String homeDrive;

    @AD(name = "wwwhomepage")
    private String homePage;

    @AD(name = "homephone")
    private String homePhone;

    @AD(name = "initials")
    private String initials;

    @AD(name = "userworkstations")
    private String logonWorkstations;

    @AD(name = "manager")
    private String manager;

    @AD(name = "memberof", IsReadOnly = true)
    private List<String> memberOf;

    @AD(name = "mobile")
    private String mobilePhone;

    @AD(name = "physicaldeliveryofficename")
    private String office;

    @AD(name = "telephonenumber")
    private String officePhone;

    @AD(name = "o")
    private String organization;

    @AD(name = "middlename")
    private String otherName;

    @AD(name = "pwdlastset")
    private LocalDateTime passwordLastSet;

    @AD(name = "postofficebox")
    private String pOBox;

    @AD(name = "postalcode")
    private String postalCode;

    @AD(name = "profilepath")
    private String profilePath;

    @AD(name = "scriptpath")
    private String scriptPath;

    @AD(name = "st")
    private String state;

    @AD(name = "streetaddress")
    private String streetAddress;

    @AD(name = "sn")
    private String lastName;

    @AD(name = "title")
    private String title;

    @AD(name = "userprincipalname")
    private String userPrincipalName;

    @AD(name = "primarygroupid")
    private int primaryGroupId;

    @AD(name = "useraccountcontrol")
    private int userAccountControl;

    @AD(name = "iscriticalsystemobject")
    private boolean isCriticalSystemObject;

    @AD(name = "samaccounttype")
    private int samAccountType;

    @AD(name = "countrycode")
    private int countryCode;

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

    @AD(name = "admincount")
    private int adminCount;

    @AD(name = "managedobjects")
    private List<String> managedObjects;

    @AD(name = "serviceprincipalname")
    private String servicePrincipalName;

    @AD(name = "logonhours")
    private byte[] logonHours;

    @AD(name = "jpegphoto")
    private byte[] jpegPhoto;

}