# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概览

这是一个 Java 21 + Maven Wrapper 的 Spring Boot 项目。

- 入口类：`src/main/java/cn/edu/scnu/hospitalmanagementinformationsystem/HospitalManagementInformationSystemApplication.java`
- 构建工具：Maven Wrapper（`mvnw` / `mvnw.cmd`），Wrapper 配置使用 Maven 3.9.15
- Spring Boot 父工程：`org.springframework.boot:spring-boot-starter-parent:4.0.6`
- 当前依赖：`spring-boot-starter`、`spring-boot-starter-test`、MySQL Connector/J

## 常用命令

从仓库根目录运行：

```bash
./mvnw test
./mvnw -Dtest=HospitalManagementInformationSystemApplicationTests test
./mvnw compile
./mvnw package
./mvnw spring-boot:run
```

当前没有在 `pom.xml` 中配置专用 lint 或 formatter 命令。

## 当前验证状态

- `./mvnw test` 当前通过。
- 测试仅包含默认的 Spring Boot `contextLoads()`。

## 代码结构

- `src/main/java/.../HospitalManagementInformationSystemApplication.java` 负责启动 Spring Boot 应用。
- `src/main/resources/application.properties` 配置应用名和 MySQL 数据源。
- `src/main/resources/templates/index.html` 与 `src/main/resources/static/css/index.css` 当前为空占位文件。
- `src/test/java/.../HospitalManagementInformationSystemApplicationTests.java` 是默认上下文加载测试。

## 配置注意事项

`application.properties` 当前配置 MySQL 数据库 `hospital`：

```properties
spring.datasource.driverClassName=com.mysql.cj.jdbc.Driver
spring.datasource.url=jdbc:mysql:///hospital?useUnicode=true&useSSL=false&characterEncoding=UTF-8
spring.datasource.username=root
spring.datasource.password=123456
```

当前 `pom.xml` 只有 MySQL 驱动，没有 JDBC、JPA、MyBatis、MyBatis Plus、Web 或 Thymeleaf starter；实现相关功能时只添加实际需要的 starter。

## 数据库设计资料

- 原始需求：`doc/2026《数据库系统原理与实践》综合设计实验指导书.docx`
- 设计文档：`doc/数据库设计文档.md`
- 表结构说明：`doc/表结构说明.md`
- 建表脚本：`doc/schema.sql`
- 模拟数据：`doc/seed.sql`

数据库设计约束：不使用物理外键；密码明文存储为 `users.password`；无 `payments` 表；`bills.status` 和 `prescriptions.status` 仅使用 `UNPAID` / `PAID`。

```bash
mysql -uroot -p123456 < doc/schema.sql
mysql -uroot -p123456 < doc/seed.sql
```

## 本地文件

`.gitignore` 已忽略 `.claude/` 和 `.omc/`，Claude/OMC 会话产物应保持为本地文件。
