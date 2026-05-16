# API 接口文档

## 1. 通用规范

### 1.1 响应信封

当前已实现接口统一返回以下结构：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {}
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `code` | int | 业务状态码，200 表示成功 |
| `message` | string | 操作结果描述 |
| `data` | any | 响应数据，错误时为 `null` |

实现位置：`src/main/java/cn/edu/scnu/hospitalmanagementinformationsystem/dto/ApiResponse.java`

### 1.2 当前已使用的错误码

| code | 说明 |
|------|------|
| 200 | 成功 |
| 400 | 参数缺失或请求体非法 |
| 401 | 未登录 / 账号密码错误 |
| 500 | 运行时异常，当前未统一包装 |

说明：`403`、`404`、`409` 仍属于总体设计中的通用规范，但在当前 Controller 实现中未形成统一返回约定。

### 1.3 认证方式

基于 Session 的账号密码登录。登录成功后，后续请求依赖浏览器自动携带 Session Cookie。

当前代码层面的已实现认证规则：

- `/api/auth/login` 不要求登录
- `/api/auth/me` 与 `/api/auth/menus` 会在 Controller 中直接检查 Session
- 业务接口文档按“应有权限”标注，但当前源码中未看到统一登录拦截器或权限拦截器

### 1.4 菜单与权限现状

`AuthService.ALL_MENUS` 当前定义了以下菜单权限：

| 菜单 | 页面 | 权限 |
|------|------|------|
| 首页工作台 | `/index.html` | 无 |
| 科室管理 | `/departments.html` | `admin:*` |
| 医生管理 | `/doctors.html` | `admin:*` |
| 病人管理 | `/patients.html` | `admin:*` |
| 药品管理 | `/medicines.html` | `admin:*` |
| 病房管理 | `/wards.html` | `admin:*` |
| 病床管理 | `/beds.html` | `admin:*` |
| 排班日历 | `/schedules.html` | `doctor:schedule:view` |
| 挂号管理 | `/registrations.html` | `patient:registration:manage` |
| 接诊管理 | `/visits.html` | `doctor:visit:manage` |
| 处方管理 | `/prescriptions.html` | `doctor:prescription:manage` |
| 我的住院档案 | `/admissions.html` | `patient:admission:view` |
| 预缴管理 | `/prepaid.html` | `patient:prepaid:manage` |
| 账单管理 | `/bills.html` | `patient:bill:view` |
| 统计分析 | `/statistics.html` | `doctor:statistics:view` |

注意：`admissions.html`、`prepaid.html`、`bills.html`、`statistics.html` 当前并不存在于 `src/main/resources/static/`。

---

## 2. Auth 模块

Base: `/api/auth`

### 2.1 登录

```
POST /api/auth/login
权限: 无
```

**请求体：**

```json
{
  "username": "admin",
  "password": "123456"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `username` | string | 是 | 登录账号 |
| `password` | string | 是 | 登录密码 |

**成功响应：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "username": "admin",
    "role": "ADMIN",
    "permissions": ["admin:*"]
  }
}
```

**失败响应（400）：**

```json
{
  "code": 400,
  "message": "账号和密码不能为空",
  "data": null
}
```

**失败响应（401）：**

```json
{
  "code": 401,
  "message": "账号或密码错误",
  "data": null
}
```

### 2.2 获取当前用户

```
GET /api/auth/me
权限: 已登录
```

**成功响应：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "username": "admin",
    "role": "ADMIN",
    "permissions": ["admin:*"]
  }
}
```

**失败响应（401）：**

```json
{
  "code": 401,
  "message": "未登录",
  "data": null
}
```

### 2.3 获取菜单

```
GET /api/auth/menus
权限: 已登录
```

返回当前用户有权看到的菜单列表。

**成功响应：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    { "title": "首页工作台", "href": "/index.html", "permission": "" },
    { "title": "科室管理", "href": "/departments.html", "permission": "admin:*" },
    { "title": "排班日历", "href": "/schedules.html", "permission": "doctor:schedule:view" }
  ]
}
```

### 2.4 退出登录

```
POST /api/auth/logout
权限: 已登录
```

**成功响应：**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": null
}
```

---

## 3. 基础资料模块

以下 6 组接口均已在当前代码中实现，接口风格统一：

- `GET /api/<resource>`：列表查询
- `POST /api/<resource>`：新增
- `PUT /api/<resource>/{id}`：编辑
- `DELETE /api/<resource>/{id}`：删除

说明：以下“权限”是菜单配置和设计目标；当前未看到统一后端权限拦截器。

### 3.1 科室管理

Base: `/api/departments`
权限目标：`admin:*`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/departments` | 查询科室列表 |
| POST | `/api/departments` | 新增科室 |
| PUT | `/api/departments/{id}` | 编辑科室 |
| DELETE | `/api/departments/{id}` | 删除科室 |

**查询参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `keyword` | string | 否 | 按名称模糊搜索 |

**Department 实体：**

```json
{
  "id": 1,
  "name": "内科",
  "location": "门诊楼二层"
}
```

### 3.2 医生管理

Base: `/api/doctors`
权限目标：`admin:*`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/doctors` | 查询医生列表 |
| POST | `/api/doctors` | 新增医生 |
| PUT | `/api/doctors/{id}` | 编辑医生 |
| DELETE | `/api/doctors/{id}` | 删除医生 |

**查询参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `keyword` | string | 否 | 匹配医生姓名或工号 |

**Doctor 实体：**

```json
{
  "id": 1,
  "userId": 2,
  "departmentId": 1,
  "titleId": 1,
  "employeeNo": "D001",
  "name": "张三",
  "gender": "MALE",
  "phone": "13800000000"
}
```

### 3.3 病人管理

Base: `/api/patients`
权限目标：`admin:*`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/patients` | 查询病人列表 |
| POST | `/api/patients` | 新增病人 |
| PUT | `/api/patients/{id}` | 编辑病人 |
| DELETE | `/api/patients/{id}` | 删除病人 |

**查询参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `keyword` | string | 否 | 匹配病人姓名或病历号 |

**Patient 实体：**

```json
{
  "id": 1,
  "userId": 3,
  "medicalRecordNo": "MR001",
  "name": "李四",
  "gender": "FEMALE",
  "phone": "13900000000",
  "address": "广东省广州市"
}
```

### 3.4 药品管理

Base: `/api/medicines`
权限目标：`admin:*`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/medicines` | 查询药品列表 |
| POST | `/api/medicines` | 新增药品 |
| PUT | `/api/medicines/{id}` | 编辑药品 |
| DELETE | `/api/medicines/{id}` | 删除药品 |

**查询参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `keyword` | string | 否 | 匹配药品名称或编码 |

**Medicine 实体：**

```json
{
  "id": 1,
  "code": "M001",
  "name": "阿莫西林",
  "specification": "0.25g×24粒",
  "unit": "盒",
  "unitPrice": 12.5,
  "stockQuantity": 100,
  "status": "ACTIVE"
}
```

### 3.5 病房管理

Base: `/api/wards`
权限目标：`admin:*`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/wards` | 查询病房列表 |
| POST | `/api/wards` | 新增病房 |
| PUT | `/api/wards/{id}` | 编辑病房 |
| DELETE | `/api/wards/{id}` | 删除病房 |

**查询参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `keyword` | string | 否 | 匹配病房编号 |

**Ward 实体：**

```json
{
  "id": 1,
  "departmentId": 1,
  "wardNo": "W001",
  "location": "3楼A区",
  "dailyCharge": 200.0
}
```

### 3.6 病床管理

Base: `/api/beds`
权限目标：`admin:*`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/beds` | 查询病床列表 |
| POST | `/api/beds` | 新增病床 |
| PUT | `/api/beds/{id}` | 编辑病床 |
| DELETE | `/api/beds/{id}` | 删除病床 |

**查询参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `keyword` | string | 否 | 匹配病床编号 |

**Bed 实体：**

```json
{
  "id": 1,
  "wardId": 1,
  "bedNo": "B001",
  "status": "AVAILABLE"
}
```

---

## 4. 门诊模块

### 4.1 医生职称管理

Base: `/api/doctor-titles`
当前页面：`/doctor-titles.html`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/doctor-titles` | 查询职称列表 |
| POST | `/api/doctor-titles` | 新增职称 |
| PUT | `/api/doctor-titles/{id}` | 编辑职称 |
| DELETE | `/api/doctor-titles/{id}` | 删除职称 |

**查询参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `keyword` | string | 否 | 匹配职称名称 |

**DoctorTitle 实体：**

```json
{
  "id": 1,
  "name": "主任医师",
  "consultationFee": 30.0
}
```

### 4.2 排班管理

Base: `/api/schedules`
当前页面：`/schedules.html`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/schedules` | 查询排班列表 |
| POST | `/api/schedules` | 新增排班 |
| PUT | `/api/schedules/{id}` | 编辑排班 |
| DELETE | `/api/schedules/{id}` | 删除排班 |

**查询参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `doctorId` | Long | 否 | 按医生筛选 |
| `departmentId` | Long | 否 | 按科室筛选 |

**DoctorSchedule 实体：**

```json
{
  "id": 1,
  "doctorId": 1,
  "departmentId": 1,
  "scheduleType": "OUTPATIENT",
  "startTime": "2026-05-16T09:00:00",
  "endTime": "2026-05-16T12:00:00",
  "room": "门诊201"
}
```

说明：当前实现未包含排班冲突校验、按角色自动过滤和历史排班只读规则。

### 4.3 挂号管理

Base: `/api/registrations`
当前页面：`/registrations.html`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/registrations` | 查询挂号列表 |
| POST | `/api/registrations` | 新增挂号 |
| PUT | `/api/registrations/{id}` | 编辑挂号 |
| DELETE | `/api/registrations/{id}` | 取消挂号 |

**查询参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `patientId` | Long | 否 | 按病人筛选 |

**Registration 实体：**

```json
{
  "id": 1,
  "patientId": 1,
  "doctorId": 1,
  "departmentId": 1,
  "scheduleId": 1,
  "visitType": "FIRST",
  "registeredAt": "2026-05-16T09:30:00",
  "status": "REGISTERED"
}
```

说明：当前删除逻辑会调用取消方法；是否仅允许 `REGISTERED` 取消，取决于底层 SQL 与后续业务约束，Controller/Service 层未显式校验。

### 4.4 接诊管理

Base: `/api/visits`
当前页面：`/visits.html`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/visits` | 查询接诊列表 |
| POST | `/api/visits` | 新增接诊 |
| PUT | `/api/visits/{id}` | 编辑接诊 |

**查询参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `doctorId` | Long | 否 | 按医生筛选 |
| `patientId` | Long | 否 | 按病人筛选 |

**OutpatientVisit 实体：**

```json
{
  "id": 1,
  "registrationId": 1,
  "patientId": 1,
  "doctorId": 1,
  "symptomDescription": "发热咳嗽",
  "diagnosis": "上呼吸道感染",
  "visitedAt": "2026-05-16T10:00:00"
}
```

说明：当前创建接诊时会把对应挂号状态更新为 `VISITED`。

### 4.5 处方管理

Base: `/api/prescriptions`
当前页面：`/prescriptions.html`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/prescriptions` | 查询处方列表 |
| POST | `/api/prescriptions` | 新增处方 |
| PUT | `/api/prescriptions/{id}` | 编辑处方头信息 |
| DELETE | `/api/prescriptions/{id}` | 作废处方 |
| GET | `/api/prescriptions/{id}/items` | 查询处方明细 |

**查询参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `patientId` | Long | 否 | 按病人筛选 |
| `doctorId` | Long | 否 | 按医生筛选 |

**创建请求体：**

```json
{
  "visitId": 1,
  "doctorId": 1,
  "patientId": 1,
  "consultationFee": 30.0,
  "items": [
    {
      "medicineId": 1,
      "quantity": 2,
      "usageInstruction": "每日3次，每次1粒"
    }
  ]
}
```

**Prescription 实体：**

```json
{
  "id": 1,
  "visitId": 1,
  "doctorId": 1,
  "patientId": 1,
  "consultationFee": 30.0,
  "medicineAmount": 25.0,
  "totalAmount": 55.0,
  "status": "UNPAID",
  "createdAt": "2026-05-16T10:30:00"
}
```

**PrescriptionItem 实体：**

```json
{
  "id": 1,
  "prescriptionId": 1,
  "medicineId": 1,
  "medicineName": "阿莫西林",
  "unitPrice": 12.5,
  "quantity": 2,
  "usageInstruction": "每日3次，每次1粒",
  "amount": 25.0
}
```

当前已实现行为：

- 支持空 `items`，即纯诊疗处方
- 创建时按当前药品单价生成明细快照
- 创建时直接扣减药品库存
- 库存不足时抛出异常

当前未实现行为：

- 自动生成账单 / 账单明细
- 支付后把处方改为 `PAID`
- 处方不可编辑的业务约束
- 已接诊未开方记录的专门查询接口

---

## 5. 当前未实现的规划模块

以下模块仍在总体设计或 PRD 中，但当前仓库未发现对应 Controller / Service / 静态页面：

- 住院档案
- 住院记录
- 预缴管理
- 账单管理
- 支付流水
- 统计分析

如需补充这些接口文档，应在代码落地后再写入“已实现 API”部分，避免把规划误写成现状。

---

## 6. 接口汇总

| 模块 | 方法 | 路径 |
|------|------|------|
| Auth | POST | `/api/auth/login` |
| Auth | GET | `/api/auth/me` |
| Auth | GET | `/api/auth/menus` |
| Auth | POST | `/api/auth/logout` |
| 科室 | GET | `/api/departments` |
| 科室 | POST | `/api/departments` |
| 科室 | PUT | `/api/departments/{id}` |
| 科室 | DELETE | `/api/departments/{id}` |
| 医生 | GET | `/api/doctors` |
| 医生 | POST | `/api/doctors` |
| 医生 | PUT | `/api/doctors/{id}` |
| 医生 | DELETE | `/api/doctors/{id}` |
| 病人 | GET | `/api/patients` |
| 病人 | POST | `/api/patients` |
| 病人 | PUT | `/api/patients/{id}` |
| 病人 | DELETE | `/api/patients/{id}` |
| 药品 | GET | `/api/medicines` |
| 药品 | POST | `/api/medicines` |
| 药品 | PUT | `/api/medicines/{id}` |
| 药品 | DELETE | `/api/medicines/{id}` |
| 病房 | GET | `/api/wards` |
| 病房 | POST | `/api/wards` |
| 病房 | PUT | `/api/wards/{id}` |
| 病房 | DELETE | `/api/wards/{id}` |
| 病床 | GET | `/api/beds` |
| 病床 | POST | `/api/beds` |
| 病床 | PUT | `/api/beds/{id}` |
| 病床 | DELETE | `/api/beds/{id}` |
| 医生职称 | GET | `/api/doctor-titles` |
| 医生职称 | POST | `/api/doctor-titles` |
| 医生职称 | PUT | `/api/doctor-titles/{id}` |
| 医生职称 | DELETE | `/api/doctor-titles/{id}` |
| 排班 | GET | `/api/schedules` |
| 排班 | POST | `/api/schedules` |
| 排班 | PUT | `/api/schedules/{id}` |
| 排班 | DELETE | `/api/schedules/{id}` |
| 挂号 | GET | `/api/registrations` |
| 挂号 | POST | `/api/registrations` |
| 挂号 | PUT | `/api/registrations/{id}` |
| 挂号 | DELETE | `/api/registrations/{id}` |
| 接诊 | GET | `/api/visits` |
| 接诊 | POST | `/api/visits` |
| 接诊 | PUT | `/api/visits/{id}` |
| 处方 | GET | `/api/prescriptions` |
| 处方 | POST | `/api/prescriptions` |
| 处方 | PUT | `/api/prescriptions/{id}` |
| 处方 | DELETE | `/api/prescriptions/{id}` |
| 处方明细 | GET | `/api/prescriptions/{id}/items` |
