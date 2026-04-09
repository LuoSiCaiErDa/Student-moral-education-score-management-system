CREATE DATABASE IF NOT EXISTS moral_education CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE moral_education;

CREATE TABLE IF NOT EXISTS students (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  student_number VARCHAR(64) NOT NULL UNIQUE,
  name VARCHAR(128) NOT NULL,
  grade VARCHAR(64),
  class_name VARCHAR(64),
  total_score INT DEFAULT 0,
  create_time DATETIME,
  update_time DATETIME
);

CREATE TABLE IF NOT EXISTS users (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(64) NOT NULL UNIQUE,
  password VARCHAR(128) NOT NULL,
  role VARCHAR(32) NOT NULL,
  display_name VARCHAR(128)
);

CREATE TABLE IF NOT EXISTS teachers (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  job_number VARCHAR(64) NOT NULL UNIQUE,
  name VARCHAR(128) NOT NULL,
  email VARCHAR(128),
  phone VARCHAR(64)
);

CREATE TABLE IF NOT EXISTS moral_categories (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(128) NOT NULL UNIQUE,
  description VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS moral_score_records (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  student_id BIGINT NOT NULL,
  category VARCHAR(128) NOT NULL,
  score INT NOT NULL,
  term VARCHAR(64),
  teacher_name VARCHAR(128),
  remark VARCHAR(255),
  status VARCHAR(32),
  audit_comment VARCHAR(255),
  auditor VARCHAR(128),
  submitted_at DATETIME,
  audited_at DATETIME,
  create_time DATETIME,
  update_time DATETIME,
  CONSTRAINT fk_student_record FOREIGN KEY (student_id) REFERENCES students(id)
);
