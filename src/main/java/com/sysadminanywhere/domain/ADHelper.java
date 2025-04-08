package com.sysadminanywhere.domain;

import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.entry.Value;

import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class ADHelper {

    public static String ExtractCN(String dn)
    {
        String[] parts = dn.split(",");

        for (int i = 0; i < parts.length; i++)
        {
            var p = parts[i];
            var elems = p.split("=");
            var t = elems[0].trim().toUpperCase();
            var v = elems[1].trim();
            if (t.equals("CN"))
            {
                return v;
            }
        }
        return dn;
    }

    public static String getPrimaryGroup(int id)
    {
    /*
        512   Domain Admins
        513   Domain Users
        514   Domain Guests
        515   Domain Computers
        516   Domain Controllers
    */

        switch (id)
        {
            case 512:
                return "Domain Admins";

            case 513:
                return "Domain Users";

            case 514:
                return "Domain Guests";

            case 515:
                return "Domain Computers";

            case 516:
                return "Domain Controllers";

            default:
                return "";
        }
    }

    public static String getGroupType(long groupType)
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

    public static String getAttributeAsCommaSeparated(Entry entry, String attributeName) {
        if (!entry.containsAttribute(attributeName)) return "";

        Attribute attr = entry.get(attributeName);

        return StreamSupport.stream(attr.spliterator(), false)
                .map(Value::getString)
                .collect(Collectors.joining(", "));
    }

}
