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
    name = "dorm_rooms",
    uniqueConstraints = @UniqueConstraint(columnNames = {"floor_id", "room_no"}))
public class DormRoom {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "floor_id", nullable = false)
  private Long floorId;

  @Column(name = "room_no", nullable = false, length = 32)
  private String roomNo;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 16)
  private RoomGender genderLimit = RoomGender.ANY;

  @Column(length = 255)
  private String remark;
}
