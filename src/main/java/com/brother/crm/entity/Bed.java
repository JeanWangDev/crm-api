package com.brother.crm.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
    name = "beds",
    uniqueConstraints = @UniqueConstraint(columnNames = {"room_id", "bed_no"}))
public class Bed {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "room_id", nullable = false)
  private Long roomId;

  @Column(name = "bed_no", nullable = false)
  private Integer bedNo;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 16)
  private DormStatus status = DormStatus.FREE;
}
