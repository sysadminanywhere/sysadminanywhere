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
public class ContactEntry {

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

    @AD(name = "wwwhomepage")
    private String homePage;

    @AD(name = "homephone")
    private String homePhone;

    @AD(name = "initials")
    private String initials;

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

    @AD(name = "postofficebox")
    private String pOBox;

    @AD(name = "postalcode")
    private String postalCode;

    @AD(name = "st")
    private String state;

    @AD(name = "streetaddress")
    private String streetAddress;

    @AD(name = "sn")
    private String lastName;

    @AD(name = "title")
    private String title;

    @AD(name = "countrycode")
    private int countryCode;

    @AD(name = "adspath")
    private String adsPath;

    @AD(name = "name")
    private String name;

    @AD(name = "instancetype")
    private int instanceType;

}