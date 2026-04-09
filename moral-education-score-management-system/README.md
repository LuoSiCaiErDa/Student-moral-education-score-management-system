# 学生德育分管理系统

本项目是一个基于 Spring Boot 的学生德育分管理系统示例，参考“学生成绩管理系统”的设计思路，专注于德育分录入、汇总、查询和审核。

## 功能模块

- 学生管理
- 德育分类别管理
- 德育分记录管理
- 学生德育分汇总查询
- 管理员审核与维护

## 目录结构

- `src/main/java/com/example/moral/` - Java 源代码
- `src/main/resources/application.yml` - 数据库与应用配置
- `db/schema.sql` - 数据库表结构示例

## 运行方式

### 先决条件

- 安装 JDK 17 或更高版本
- 安装 Apache Maven
- 将 `java` 和 `mvn` 添加到系统 `PATH`
- 创建 MySQL 数据库 `moral_education`

### 本地运行

1. 修改 `application.yml` 中的 MySQL 连接信息。
2. 使用 Maven 构建：
   ```bash
   mvn clean package
   ```
3. 运行：
   ```bash
   java -jar target/moral-education-score-management-system-0.0.1-SNAPSHOT.jar
   ```
4. 打开浏览器访问 `http://localhost:8080/`。

### VS Code 运行

- 打开工作区后，按 `Ctrl+Shift+B` 执行 `Build Moral Education Project`。
- 任务使用 `mvn clean package`。
- 你也可以直接运行 `Run Moral Education Project` 任务启动应用。

## 默认示例账号

- 管理员：`admin001` / `admin123`
- 教师：`teacher001` / `teacher123`
- 学生：`student001` / `student123`

## 说明

本示例包含后端权限模块、审核流程、德育类别管理和 Vue.js 前端页面。可继续扩展教师信息、审核记录、报表导出以及更完善的登录认证。