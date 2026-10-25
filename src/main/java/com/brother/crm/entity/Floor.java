package com.brother.crm.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
    name = "floors",
    uniqueConstraints = @UniqueConstraint(columnNames = {"building_id", "floor_no"}))
public class Floor {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "building_id", nullable = false)
  private Long buildingId;

  @Column(name = "floor_no", nullable = false)
  private Integer floorNo;

  @Column(length = 255)
  private String remark;
}
