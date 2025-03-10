package com.sysadminanywhere.model.ad;

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
public class PrinterEntry {

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

    @AD(name = "adspath")
    private String adsPath;

    @AD(name = "name")
    private String name;

    @AD(name = "instancetype")
    private int instanceType;

    @AD(name = "printspooling")
    private String printSpooling;

    @AD(name = "url")
    private String url;

    @AD(name = "shortservername")
    private String shortServerName;

    @AD(name = "drivername")
    private String driverName;

    @AD(name = "printlanguage")
    private String printLanguage;

    @AD(name = "servername")
    private String serverName;

    @AD(name = "printorientationssupported")
    private List<String> printOrientationsSupported;

    @AD(name = "printrateunit")
    private String printRateUnit;

    @AD(name = "printmediaready")
    private String printMediaReady;

    @AD(name = "printmediasupported")
    private List<String> printMediaSupported;

    @AD(name = "printername")
    private String printerName;

    @AD(name = "printbinnames")
    private List<String> printBinNames;

    @AD(name = "uncname")
    private String uncName;

    @AD(name = "printmaxyextent")
    private int printMaxyExtent;

    @AD(name = "printkeepprintedjobs")
    private boolean printKeepPrintedJobs;

    @AD(name = "printminyextent")
    private int printMinyExtent;

    @AD(name = "printstaplingsupported")
    private boolean printStaplingSupported;

    @AD(name = "printnumberup")
    private int printNumberUp;

    @AD(name = "driverversion")
    private int driverVersion;

    @AD(name = "printmemory")
    private int printMemory;

    @AD(name = "printmaxxextent")
    private int printMaxxExtent;

    @AD(name = "printcollate")
    private boolean printCollate;

    @AD(name = "versionnumber")
    private int versionNumber;

    @AD(name = "printrate")
    private int printRate;

    @AD(name = "portname")
    private String portName;

    @AD(name = "printsharename")
    private String printShareName;

    @AD(name = "printpagesperminute")
    private int printPagesPerMinute;

    @AD(name = "printminxextent")
    private int printMinxExtent;

    @AD(name = "flags")
    private int flags;

    @AD(name = "printmaxresolutionsupported")
    private int printMaxResolutionSupported;

    @AD(name = "priority")
    private int priority;

    @AD(name = "rintduplexsupported")
    private boolean printDuplexSupported;

    @AD(name = "printcolor")
    private boolean printColor;

    @AD(name = "printendtime")
    private LocalDateTime printendTime;

    @AD(name = "printstarttime")
    private LocalDateTime printStartTime;

}