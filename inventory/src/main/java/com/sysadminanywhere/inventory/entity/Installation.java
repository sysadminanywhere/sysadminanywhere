package com.sysadminanywhere.inventory.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(
        name = "installations",
        indexes = {
                @Index(name = "idx_installations_computer", columnList = "computer_id"),
                @Index(name = "idx_installations_software", columnList = "software_id"),
                @Index(name = "idx_installations_computer_software", columnList = "computer_id, software_id", unique = true)
        }
)
public class Installation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "computer_id", nullable = false)
    private Computer computer;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "software_id", nullable = false)
    private Software software;

    @Column(nullable = false)
    private LocalDateTime installDate;

}