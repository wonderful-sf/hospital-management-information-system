# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概览

Java 21 + Spring Boot 4.0.6 医院管理信息系统，Maven Wrapper 构建，MyBatis 持久层，MySQL 数据库。

- 入口类：`src/main/java/cn/edu/scnu/hospitalmanagementinformationsystem/HospitalManagementInformationSystemApplication.java`
- 前端：HTML + CSS + 原生 JavaScript，统一后台管理框架（侧边栏 + 顶部栏 + 内容区），不引入前端框架
- 认证：Session + 账号密码登录，`users.permissions` 存 JSON 权限数组

## 常用命令

```bash
./mvnw compile
./mvnw test
./mvnw -Dtest=ClassName test
./mvnw spring-boot:run
mysql -uroot -p123456 < doc/schema.sql
mysql -uroot -p123456 < doc/seed.sql
```

## 数据库设计

20 张表，无物理外键，应用层维护一致性，密码明文存储。

核心表：`users`、`departments`、`doctor_titles`、`doctors`、`patients`、`medicines`、`doctor_schedules`、`registrations`、`outpatient_visits`、`prescriptions`、`prescription_items`、`wards`、`beds`、`admissions`、`inpatient_records`、`inpatient_record_items`、`bills`、`bill_items`、`payments`、`prepaid_records`。

完整表结构见 `doc/表结构说明.md`，建表见 `doc/schema.sql`，模拟数据见 `doc/seed.sql`。

## PRD 文档

| 文档 | 内容 |
|------|------|
| `doc/00-总体设计文档.md` | 技术架构、数据库 20 表、权限模型、API 规范 |
| `doc/01-基础资料管理.md` | 病人/科室/医生/药品/病房/病床 CRUD |
| `doc/02-门诊管理.md` | 排班日历、挂号、接诊、处方（含纯诊疗） |
| `doc/03-住院管理.md` | 住院建档、预缴扣费、每日记录、出院结算 |
| `doc/04-费用管理.md` | 账单、模拟支付、支付流水 |
| `doc/05-统计分析.md` | 排班/工作量/治疗统计 + CSS 条形图 |

## 关键业务规则

- 门诊：排班→挂号→接诊→处方（允许不开药），开方时扣库存，自动生成账单
- 住院：建档→预缴→每日记录扣费→余额<0 自动 SUSPENDED→充值恢复→出院多退少补
- 费用：门诊/住院统一走 bills + bill_items，支付走 payments 表，预缴走 prepaid_records 表
- 权限：管理员全权限，医生只看自己的排班/挂号/处方/住院病人，病人只看自己的数据
- 快照：开方时药品单价和诊疗费复制到明细表，后续调价不影响历史
- 编号：住院号`AD{yyyyMMdd}{序号}`，账单`B{yyyyMMdd}{序号}`，支付`PAY{yyyyMMdd}{序号}`，预缴`PRE{yyyyMMdd}{序号}`

## 配置

`application.properties`：
```properties
spring.datasource.driverClassName=com.mysql.cj.jdbc.Driver
spring.datasource.url=jdbc:mysql:///hospital?useUnicode=true&useSSL=false&characterEncoding=UTF-8
spring.datasource.username=root
spring.datasource.password=123456
```
