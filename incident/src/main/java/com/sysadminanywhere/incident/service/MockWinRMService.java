package com.sysadminanywhere.incident.service;

import com.sysadminanywhere.incident.model.Event;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
public class MockWinRMService {

    private final AtomicLong recordCounter = new AtomicLong(1);

    private static final List<String> MACHINES = List.of(
            "DC01",
            "DC02",
            "SRV01",
            "SRV02",
            "WEB01"
    );

    public List<Event> generateEvents() {

        List<Event> events = new ArrayList<>();

        OffsetDateTime baseTime = OffsetDateTime.now(ZoneOffset.UTC).minusHours(1);

        events.addAll(generateBruteForce(baseTime));
        events.addAll(generateDistributedAttack(baseTime.plusMinutes(10)));
        events.addAll(generateRdpAttack(baseTime.plusMinutes(20)));
        events.addAll(generatePrivilegeEscalation(baseTime.plusMinutes(30)));
        events.addAll(generateUserCreation(baseTime.plusMinutes(40)));
        events.addAll(generateLogCleared(baseTime.plusMinutes(50)));
        events.addAll(generateRepeatedIncidents(baseTime.plusMinutes(55)));

        return events;
    }

    // =========================
    // 1️⃣ Brute force + success
    // =========================
    private List<Event> generateBruteForce(OffsetDateTime time) {

        List<Event> list = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            list.add(event(
                    4625,
                    time.plusMinutes(i),
                    "DC01",
                    "An account failed to log on.",
                    Map.of(
                            "TargetUserName", "admin",
                            "IpAddress", "10.0.0.50",
                            "LogonType", "3"
                    )
            ));
        }

        // success after fails
        list.add(event(
                4624,
                time.plusMinutes(6),
                "DC01",
                "An account was successfully logged on.",
                Map.of(
                        "TargetUserName", "admin",
                        "IpAddress", "10.0.0.50",
                        "LogonType", "3"
                )
        ));

        return list;
    }

    // =========================
    // 2️⃣ Distributed brute force
    // =========================
    private List<Event> generateDistributedAttack(OffsetDateTime time) {

        List<Event> list = new ArrayList<>();

        for (int i = 0; i < MACHINES.size(); i++) {
            list.add(event(
                    4625,
                    time.plusMinutes(i),
                    MACHINES.get(i),
                    "Distributed failed logon attempt.",
                    Map.of(
                            "TargetUserName", "administrator",
                            "IpAddress", "192.168.1." + (10 + i),
                            "LogonType", "3"
                    )
            ));
        }

        return list;
    }

    // =========================
    // 3️⃣ RDP attack
    // =========================
    private List<Event> generateRdpAttack(OffsetDateTime time) {

        List<Event> list = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            list.add(event(
                    4624,
                    time.plusMinutes(i),
                    "SRV01",
                    "Remote Desktop logon.",
                    Map.of(
                            "TargetUserName", "rdpuser",
                            "IpAddress", "10.10.10.10",
                            "LogonType", "10"
                    )
            ));
        }

        return list;
    }

    // =========================
    // 4️⃣ Privilege escalation
    // =========================
    private List<Event> generatePrivilegeEscalation(OffsetDateTime time) {

        return List.of(
                event(
                        4728,
                        time,
                        "DC01",
                        "User added to Domain Admins.",
                        Map.of(
                                "SubjectUserName", "administrator",
                                "MemberName", "eviluser",
                                "GroupName", "Domain Admins"
                        )
                )
        );
    }

    // =========================
    // 5️⃣ User creation
    // =========================
    private List<Event> generateUserCreation(OffsetDateTime time) {

        return List.of(
                event(
                        4720,
                        time,
                        "DC02",
                        "User account created.",
                        Map.of(
                                "SubjectUserName", "administrator",
                                "TargetUserName", "backdoor"
                        )
                )
        );
    }

    // =========================
    // 6️⃣ Log cleared
    // =========================
    private List<Event> generateLogCleared(OffsetDateTime time) {

        return List.of(
                event(
                        1102,
                        time,
                        "DC01",
                        "Security log cleared.",
                        Map.of(
                                "SubjectUserName", "administrator"
                        )
                )
        );
    }

    // =========================
    // 7️⃣ Repeated incidents (meta-signal)
    // =========================
    private List<Event> generateRepeatedIncidents(OffsetDateTime time) {

        List<Event> list = new ArrayList<>();

        // Три одинаковых атаки через промежутки времени
        for (int round = 0; round < 3; round++) {
            for (int i = 0; i < 4; i++) {
                list.add(event(
                        4625,
                        time.plusMinutes(round * 15 + i),
                        "WEB01",
                        "Repeated brute force attempt.",
                        Map.of(
                                "TargetUserName", "service",
                                "IpAddress", "172.16.0.10",
                                "LogonType", "3"
                        )
                ));
            }
        }

        return list;
    }

    // =========================
    // Factory method
    // =========================
    private Event event(
            Integer eventId,
            OffsetDateTime time,
            String machine,
            String message,
            Map<String, String> data) {

        return new Event(
                recordCounter.getAndIncrement(),
                eventId,
                time,
                machine,
                "Information",
                message,
                data
        );
    }

}