package com.sysadminanywhere.domain;

import com.sysadminanywhere.common.directory.dto.EntryDto;
import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.entry.Value;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class ADHelper {

    public static String ExtractCN(String dn) {
        String[] parts = dn.split(",");

        for (int i = 0; i < parts.length; i++) {
            var p = parts[i];
            var elems = p.split("=");
            var t = elems[0].trim().toUpperCase();
            var v = elems[1].trim();
            if (t.equals("CN")) {
                return v;
            }
        }
        return dn;
    }

    public static String getPrimaryGroup(int id) {
    /*
        512   Domain Admins
        513   Domain Users
        514   Domain Guests
        515   Domain Computers
        516   Domain Controllers
    */

        switch (id) {
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

    public static String getGroupType(long groupType) {
        switch ((int) groupType) {
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

    public static String getAttributeAsCommaSeparated(EntryDto entry, String attributeName) {
        if (!entry.getAttributes().containsKey(attributeName)) return "";

        Object attr = entry.getAttributes().get(attributeName);

        if (attr instanceof List) {
            List<Object> list = (List<Object>) attr;

            return list.stream()
                    .map(Object::toString)
                    .collect(Collectors.joining(", "));
        } else {
            return "";
        }
    }

}