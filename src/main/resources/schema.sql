-- ============================================================================
-- crm-api 数据库建表脚本（MySQL 8 / TiDB）
-- 参考文档：JPA Entity 定义；日常联调可由 Hibernate ddl-auto=update 维护
-- 手动建库时可执行：mysql -u root -p jdys-crm < src/main/resources/schema.sql
-- ============================================================================

CREATE TABLE IF NOT EXISTS users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  account VARCHAR(64) NOT NULL UNIQUE,
  name VARCHAR(64) NOT NULL,
  phone VARCHAR(32) NULL UNIQUE,
  password_hash VARCHAR(100) NOT NULL,
  role VARCHAR(16) NOT NULL,
  enabled TINYINT(1) NOT NULL DEFAULT 1,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS leads (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  owner_id BIGINT NOT NULL,
  student_name VARCHAR(64) NOT NULL,
  student_phone VARCHAR(32) NOT NULL,
  wechat VARCHAR(64) NULL,
  source VARCHAR(64) NULL,
  status VARCHAR(16) NOT NULL,
  remark VARCHAR(500) NULL,
  converted_student_id BIGINT NULL,
  created_by_id BIGINT NULL,
  updated_by_id BIGINT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS lead_followups (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  lead_id BIGINT NOT NULL,
  content VARCHAR(1000) NOT NULL,
  operator_id BIGINT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS students (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  owner_id BIGINT NOT NULL,
  from_lead_id BIGINT NULL,
  name VARCHAR(64) NOT NULL,
  phone VARCHAR(32) NOT NULL,
  gender VARCHAR(16) NULL,
  home_address VARCHAR(255) NOT NULL,
  emergency_name VARCHAR(64) NOT NULL,
  emergency_phone VARCHAR(32) NOT NULL,
  plan_join_date DATE NOT NULL,
  student_status VARCHAR(16) NOT NULL,
  checked_in_at TIMESTAMP NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS student_followups (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  student_id BIGINT NOT NULL,
  content VARCHAR(1000) NOT NULL,
  operator_id BIGINT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS travel_info (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  student_id BIGINT NOT NULL UNIQUE,
  arrive_date DATE NULL,
  arrive_time VARCHAR(16) NULL,
  traffic_type VARCHAR(16) NULL,
  need_pickup TINYINT(1) NULL,
  station VARCHAR(64) NULL,
  pickup_user_id BIGINT NULL,
  pickup_status VARCHAR(16) NULL,
  remark VARCHAR(500) NULL
);

CREATE TABLE IF NOT EXISTS dorm_places (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(64) NOT NULL,
  address VARCHAR(255) NULL,
  remark VARCHAR(255) NULL
);

CREATE TABLE IF NOT EXISTS buildings (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  place_id BIGINT NULL,
  campus_name VARCHAR(64) NULL,
  name VARCHAR(64) NOT NULL,
  remark VARCHAR(255) NULL
);

CREATE TABLE IF NOT EXISTS floors (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  building_id BIGINT NOT NULL,
  floor_no INT NOT NULL,
  remark VARCHAR(255) NULL
);

CREATE TABLE IF NOT EXISTS dorm_rooms (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  floor_id BIGINT NOT NULL,
  room_no VARCHAR(32) NOT NULL,
  gender_limit VARCHAR(16) NOT NULL,
  remark VARCHAR(255) NULL
);

CREATE TABLE IF NOT EXISTS beds (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  room_id BIGINT NOT NULL,
  bed_no INT NOT NULL,
  status VARCHAR(16) NOT NULL
);

CREATE TABLE IF NOT EXISTS student_beds (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  student_id BIGINT NOT NULL UNIQUE,
  bed_id BIGINT NOT NULL UNIQUE,
  assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
