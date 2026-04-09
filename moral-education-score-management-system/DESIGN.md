# 学生德育分管理系统设计说明

## 目标

构建一个面向学校德育管理的系统，用于学生德育分记录、汇总、查询和教师/管理员审核管理。系统参考原“学生成绩管理系统”的模块化设计，将业务拆分为学生、德育分记录和管理员三类主体。

## 核心功能

1. 学生管理
   - 录入学生信息
   - 查询学生基础信息
   - 更新学生年级、班级等信息
   - 查看学生德育分汇总

2. 德育分记录管理
   - 按德育类别记录分值（如行为表现、志愿服务、文明礼仪等）
   - 可保存学期、教师、备注信息
   - 自动更新学生德育总分
   - 删除或调整记录时同步更新总分

3. 管理端扩展（可继续实现）
   - 德育类别管理
   - 数据审核与异常处理
   - 导出报表、打印记录
   - 权限控制与登录认证

## 技术栈

- Java 17
- Spring Boot 3.x
- Spring Data JPA
- MySQL
- RESTful API

## 系统结构

- `entity`：核心实体类 `Student`、`MoralScoreRecord`
- `repository`：JPA 数据访问接口
- `controller`：REST 控制器提供 CRUD 接口
- `resources/application.yml`：数据库连接与 JPA 配置

## 数据库设计

### students

- id
- student_number
- name
- grade
- class_name
- total_score
- create_time
- update_time

### moral_score_records

- id
- student_id
- category
- score
- term
- teacher_name
- remark
- create_time
- update_time

## 扩展建议

- 添加 `Teacher`、`Admin` 实体与登录认证
- 支持前端页面（Vue.js/Thymeleaf）
- 增加 `MoralCategory` 实体，规范德育分类别
- 增加权限控制与审计日志
- 增加统计报表接口
