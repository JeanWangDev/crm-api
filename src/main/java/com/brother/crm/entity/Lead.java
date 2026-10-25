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
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "leads")
public class Lead {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long ownerId;

  @Column(nullable = false, length = 64)
  private String studentName;

  @Column(nullable = false, length = 32)
  private String studentPhone;

  /** 微信号，选填 */
  @Column(length = 64)
  private String wechat;

  @Column(length = 64)
  private String source;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 16)
  private LeadStatus status = LeadStatus.PENDING;

  @Column(length = 500)
  private String remark;

  private Long convertedStudentId;

  /** 创建人 */
  private Long createdById;

  /** 最近更新人 */
  private Long updatedById;

  @Column(nullable = false, updatable = false)
  private Instant createdAt = Instant.now();

  @Column(nullable = false)
  private Instant updatedAt = Instant.now();

  @Transient
  private String ownerName;

  @Transient
  private String createdByName;

  @Transient
  private String updatedByName;
}
