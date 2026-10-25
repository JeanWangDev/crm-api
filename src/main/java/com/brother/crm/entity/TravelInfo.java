package com.brother.crm.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "travel_info")
public class TravelInfo {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private Long studentId;

  private LocalDate arriveDate;

  private LocalTime arriveTime;

  @Enumerated(EnumType.STRING)
  @Column(length = 16)
  private TrafficType trafficType;

  @Column(nullable = false)
  private Boolean needPickup = false;

  @Column(length = 64)
  private String station;

  private Long pickupUserId;

  @Enumerated(EnumType.STRING)
  @Column(length = 16)
  private PickupStatus pickupStatus = PickupStatus.WAITING;

  @Column(length = 255)
  private String remark;

  @Column(nullable = false)
  private Instant updatedAt = Instant.now();
}
