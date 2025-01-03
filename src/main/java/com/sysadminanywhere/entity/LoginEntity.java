package com.sysadminanywhere.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "logins")
public class LoginEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "displayname", nullable = false)
    private String displayName;

    @Column(name = "objectguid", nullable = false, unique = true)
    private UUID objectGuid;

    @Column(name = "lastlogin", nullable = false)
    private LocalDateTime lastLogin;

}
