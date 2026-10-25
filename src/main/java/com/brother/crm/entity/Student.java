package com.brother.crm.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.Instant;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "students")
public class Student {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long ownerId;

  private Long fromLeadId;

  @Column(nullable = false, length = 64)
  private String name;

  @Column(nullable = false, length = 32)
  private String phone;

  @Enumerated(EnumType.STRING)
  @Column(length = 16)
  private Gender gender;

  @Column(nullable = false, length = 255)
  private String homeAddress;

  @Column(nullable = false, length = 64)
  private String emergencyName;

  @Column(nullable = false, length = 32)
  private String emergencyPhone;

  @Column(nullable = false)
  private LocalDate planJoinDate;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 16)
  private StudentStatus studentStatus = StudentStatus.WAITING;

  private Instant checkedInAt;

  @Column(nullable = false, updatable = false)
  private Instant createdAt = Instant.now();

  @Column(nullable = false)
  private Instant updatedAt = Instant.now();

  @Transient
  private String ownerName;
}
