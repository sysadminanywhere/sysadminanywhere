package com.sysadminanywhere.common.directory.model;

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
public class GroupEntry {

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

    @AD(name = "managedby")
    private String managedBy;

    @AD(name = "memberof", IsReadOnly = true)
    private List<String> memberOf;

    @AD(name = "member", IsReadOnly = true)
    private List<String> members;

    @AD(name = "grouptype")
    private long groupType;

    @AD(name = "iscriticalsystemobject")
    private boolean isCriticalSystemObject;

    @AD(name = "samaccounttype")
    private int samAccountType;

    @AD(name = "systemflags")
    private int systemFlags;

    @AD(name = "adspath")
    private String adsPath;

    @AD(name = "name")
    private String name;

    @AD(name = "instancetype")
    private int instanceType;

    @AD(name = "admincount")
    private int adminCount;

    @AD(name = "primarygroupid")
    private int primaryGroupId;

    public String getADGroupType() {
        return getGroupType(groupType);
    }

    public boolean isSecurity() {
        return getADGroupType().contains("security");
    }

    public boolean isDistribution() {
        return getADGroupType().contains("distribution");
    }

    public boolean isBuiltIn() {
        return getADGroupType().contains("BuiltIn");
    }

    private String getGroupType(long groupType)
    {
        switch ((int) groupType)
        {
            case 2:
                return "Global distribution group";

            case 4:
                return "Domain local distribution group";

            case 8:
                return "Universal distribution group";

            case -2147483646:
                return "Global security group";

            case -2147483644:
                return "Domain local security group";

            case -2147483640:
                return "Universal security group";

            case -2147483643:
                return "BuiltIn group";

            default:
                return "";
        }
    }

}