package com.sysadminanywhere.domain;

public class ADHelper {

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
                return "BuiltIn Group";

            default:
                return "";
        }
    }

}
