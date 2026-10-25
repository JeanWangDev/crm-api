package com.brother.crm.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "dormitories")
public class Dormitory {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 64)
  private String buildingName;

  @Column(nullable = false, length = 32)
  private String roomNo;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 16)
  private DormStatus status = DormStatus.FREE;

  @Column(length = 255)
  private String remark;
}
