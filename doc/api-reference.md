# API 接口文档

## 1. 通用规范

### 1.1 响应信封

所有 API 响应统一使用以下结构：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {}
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `code` | int | 状态码，200 表示成功 |
| `message` | string | 操作结果描述 |
| `data` | any | 响应数据，错误时为 `null` |

### 1.2 错误码

| code | 说明 |
|------|------|
| 200 | 成功 |
| 400 | 参数校验失败 |
| 401 | 未登录 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 409 | 业务冲突（名称重复、资源被占用等） |
| 500 | 服务器内部错误 |

### 1.3 认证方式

基于 Session 的账号密码登录。登录成功后，后续请求自动携带 Session Cookie。所有业务 API 需登录后访问，由拦截器统一校验。

### 1.4 权限模型

| 角色 | 标识 | 权限范围 |
|------|------|----------|
| 管理员 | `ADMIN` | `admin:*` 全部权限 |
| 医生 | `DOCTOR` | `doctor:schedule:view`, `doctor:visit:manage`, `doctor:prescription:manage`, `doctor:inpatient:view`, `doctor:inpatient:manage`, `doctor:statistics:view` |
| 病人 | `PATIENT` | `patient:registration:manage`, `patient:registration:cancel`, `patient:visit:view`, `patient:prescription:view`, `patient:bill:view`, `patient:bill:pay`, `patient:admission:view`, `patient:prepaid:manage`, `patient:prepaid:view` |

基础资料管理模块（科室/医生/病人/药品/病房/病床）的增删改查仅限管理员访问。

---

## 2. Auth 模块

Base: `/api/auth`

### 2.1 登录

```
POST /api/auth/login
权限: 无
```

**请求体:**

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

**成功响应 (200):**

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

**失败响应 (400):** 账号或密码为空

```json
{
  "code": 400,
  "message": "账号和密码不能为空",
  "data": null
}
```

**失败响应 (401):** 账号不存在或密码错误

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

**成功响应 (200):**

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

**失败响应 (401):** 未登录

### 2.3 获取菜单

```
GET /api/auth/menus
权限: 已登录
```

根据当前用户权限返回可见菜单列表。

**成功响应 (200):**

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

| 字段 | 类型 | 说明 |
|------|------|------|
| `title` | string | 菜单名称 |
| `href` | string | 页面路径 |
| `permission` | string | 所需权限标识，空字符串表示无需特殊权限 |

**失败响应 (401):** 未登录

### 2.4 退出登录

```
POST /api/auth/logout
权限: 已登录
```

销毁当前 Session。

**成功响应 (200):**

```json
{
  "code": 200,
  "message": "操作成功",
  "data": null
}
```

---

## 3. 基础资料模块

基础资料模块每个资源的 API 模式统一：列表查询支持可选关键词搜索，增删改查路径一致。

### 3.1 科室管理

Base: `/api/departments`
权限: `admin:*`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/departments` | 查询科室列表 |
| POST | `/api/departments` | 新增科室 |
| PUT | `/api/departments/{id}` | 编辑科室 |
| DELETE | `/api/departments/{id}` | 删除科室 |

**Department 实体:**

```json
{
  "id": 1,
  "name": "内科",
  "description": "内科科室",
  "status": "ACTIVE"
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | Long | 主键 |
| `name` | String | 科室名称 |
| `description` | String | 科室描述 |
| `status` | String | 状态（ACTIVE / DISABLED） |

**GET /api/departments** — 列表查询

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `keyword` | string | 否 | 搜索关键词，匹配科室名称 |

```json
// GET /api/departments
{ "code": 200, "message": "操作成功", "data": [ { "id": 1, "name": "内科", "description": "内科科室", "status": "ACTIVE" } ] }

// GET /api/departments?keyword=内科
{ "code": 200, "message": "操作成功", "data": [ { "id": 1, "name": "内科", "description": "内科科室", "status": "ACTIVE" } ] }
```

**POST /api/departments** — 新增

请求体（`id` 字段无需传递，由数据库自动生成）:

```json
{
  "name": "内科",
  "description": "内科科室",
  "status": "ACTIVE"
}
```

**PUT /api/departments/{id}** — 编辑

请求体需包含完整字段：

```json
{
  "name": "内科",
  "description": "更新后的描述",
  "status": "ACTIVE"
}
```

**DELETE /api/departments/{id}** — 删除

软删除，将状态标记为已删除。

---

### 3.2 医生管理

Base: `/api/doctors`
权限: `admin:*`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/doctors` | 查询医生列表 |
| POST | `/api/doctors` | 新增医生 |
| PUT | `/api/doctors/{id}` | 编辑医生 |
| DELETE | `/api/doctors/{id}` | 删除医生 |

**Doctor 实体:**

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

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | Long | 主键 |
| `userId` | Long | 关联 users 表 ID |
| `departmentId` | Long | 所属科室 ID |
| `titleId` | Long | 职称 ID |
| `employeeNo` | String | 工号 |
| `name` | String | 医生姓名 |
| `gender` | String | 性别：`MALE` / `FEMALE` |
| `phone` | String | 联系电话 |

**GET /api/doctors** — 列表查询

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `keyword` | string | 否 | 搜索关键词，匹配医生姓名或工号 |

**POST /api/doctors** — 新增

请求体（`id` 字段无需传递）:

```json
{
  "userId": 2,
  "departmentId": 1,
  "titleId": 1,
  "employeeNo": "D001",
  "name": "张三",
  "gender": "MALE",
  "phone": "13800000000"
}
```

**PUT /api/doctors/{id}** — 编辑

请求体需包含完整字段。

**DELETE /api/doctors/{id}** — 删除

物理删除。

---

### 3.3 病人管理

Base: `/api/patients`
权限: `admin:*`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/patients` | 查询病人列表 |
| POST | `/api/patients` | 新增病人 |
| PUT | `/api/patients/{id}` | 编辑病人 |
| DELETE | `/api/patients/{id}` | 删除病人 |

**Patient 实体:**

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

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | Long | 主键 |
| `userId` | Long | 关联 users 表 ID |
| `medicalRecordNo` | String | 病历号 |
| `name` | String | 病人姓名 |
| `gender` | String | 性别：`MALE` / `FEMALE` |
| `phone` | String | 联系电话 |
| `address` | String | 地址 |

**GET /api/patients** — 列表查询

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `keyword` | string | 否 | 搜索关键词，匹配病人姓名或病历号 |

**POST /api/patients** — 新增

请求体（`id` 字段无需传递）:

```json
{
  "userId": 3,
  "medicalRecordNo": "MR001",
  "name": "李四",
  "gender": "FEMALE",
  "phone": "13900000000",
  "address": "广东省广州市"
}
```

**PUT /api/patients/{id}** — 编辑

请求体需包含完整字段。

**DELETE /api/patients/{id}** — 删除

物理删除。

---

### 3.4 药品管理

Base: `/api/medicines`
权限: `admin:*`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/medicines` | 查询药品列表 |
| POST | `/api/medicines` | 新增药品 |
| PUT | `/api/medicines/{id}` | 编辑药品 |
| DELETE | `/api/medicines/{id}` | 删除药品 |

**Medicine 实体:**

```json
{
  "id": 1,
  "code": "M001",
  "name": "阿莫西林",
  "specification": "0.25g×24粒",
  "unit": "盒",
  "unitPrice": 12.50,
  "stockQuantity": 100,
  "status": "ACTIVE"
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | Long | 主键 |
| `code` | String | 药品编码 |
| `name` | String | 药品名称 |
| `specification` | String | 规格 |
| `unit` | String | 单位 |
| `unitPrice` | BigDecimal | 单价（元，保留两位小数） |
| `stockQuantity` | Integer | 库存数量 |
| `status` | String | 状态：`ACTIVE` / `DISABLED` |

**GET /api/medicines** — 列表查询

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `keyword` | string | 否 | 搜索关键词，匹配药品名称或编码 |

**POST /api/medicines** — 新增

请求体（`id` 字段无需传递）:

```json
{
  "code": "M001",
  "name": "阿莫西林",
  "specification": "0.25g×24粒",
  "unit": "盒",
  "unitPrice": 12.50,
  "stockQuantity": 100,
  "status": "ACTIVE"
}
```

**PUT /api/medicines/{id}** — 编辑

请求体需包含完整字段。

**DELETE /api/medicines/{id}** — 删除

物理删除。

---

### 3.5 病房管理

Base: `/api/wards`
权限: `admin:*`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/wards` | 查询病房列表 |
| POST | `/api/wards` | 新增病房 |
| PUT | `/api/wards/{id}` | 编辑病房 |
| DELETE | `/api/wards/{id}` | 删除病房 |

**Ward 实体:**

```json
{
  "id": 1,
  "departmentId": 1,
  "wardNo": "W001",
  "location": "3楼A区",
  "dailyCharge": 200.00
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | Long | 主键 |
| `departmentId` | Long | 所属科室 ID |
| `wardNo` | String | 病房编号 |
| `location` | String | 位置 |
| `dailyCharge` | BigDecimal | 每日费用（元，保留两位小数） |

**GET /api/wards** — 列表查询

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `keyword` | string | 否 | 搜索关键词，匹配病房编号 |

**POST /api/wards** — 新增

请求体（`id` 字段无需传递）:

```json
{
  "departmentId": 1,
  "wardNo": "W001",
  "location": "3楼A区",
  "dailyCharge": 200.00
}
```

**PUT /api/wards/{id}** — 编辑

请求体需包含完整字段。

**DELETE /api/wards/{id}** — 删除

物理删除。

---

### 3.6 病床管理

Base: `/api/beds`
权限: `admin:*`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/beds` | 查询病床列表 |
| POST | `/api/beds` | 新增病床 |
| PUT | `/api/beds/{id}` | 编辑病床 |
| DELETE | `/api/beds/{id}` | 删除病床 |

**Bed 实体:**

```json
{
  "id": 1,
  "wardId": 1,
  "bedNo": "B001",
  "status": "AVAILABLE"
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | Long | 主键 |
| `wardId` | Long | 所属病房 ID |
| `bedNo` | String | 病床编号 |
| `status` | String | 状态：`AVAILABLE` / `OCCUPIED` / `MAINTENANCE` |

**GET /api/beds** — 列表查询

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `keyword` | string | 否 | 搜索关键词，匹配病床编号 |

**POST /api/beds** — 新增

请求体（`id` 字段无需传递）:

```json
{
  "wardId": 1,
  "bedNo": "B001",
  "status": "AVAILABLE"
}
```

**PUT /api/beds/{id}** — 编辑

请求体需包含完整字段。

**DELETE /api/beds/{id}** — 删除

物理删除。

---

## 4. 接口汇总

| 模块 | 方法 | 路径 | 权限 |
|------|------|------|------|
| Auth | POST | `/api/auth/login` | 无 |
| Auth | GET | `/api/auth/me` | 已登录 |
| Auth | GET | `/api/auth/menus` | 已登录 |
| Auth | POST | `/api/auth/logout` | 已登录 |
| 科室 | GET | `/api/departments` | `admin:*` |
| 科室 | POST | `/api/departments` | `admin:*` |
| 科室 | PUT | `/api/departments/{id}` | `admin:*` |
| 科室 | DELETE | `/api/departments/{id}` | `admin:*` |
| 医生 | GET | `/api/doctors` | `admin:*` |
| 医生 | POST | `/api/doctors` | `admin:*` |
| 医生 | PUT | `/api/doctors/{id}` | `admin:*` |
| 医生 | DELETE | `/api/doctors/{id}` | `admin:*` |
| 病人 | GET | `/api/patients` | `admin:*` |
| 病人 | POST | `/api/patients` | `admin:*` |
| 病人 | PUT | `/api/patients/{id}` | `admin:*` |
| 病人 | DELETE | `/api/patients/{id}` | `admin:*` |
| 药品 | GET | `/api/medicines` | `admin:*` |
| 药品 | POST | `/api/medicines` | `admin:*` |
| 药品 | PUT | `/api/medicines/{id}` | `admin:*` |
| 药品 | DELETE | `/api/medicines/{id}` | `admin:*` |
| 病房 | GET | `/api/wards` | `admin:*` |
| 病房 | POST | `/api/wards` | `admin:*` |
| 病房 | PUT | `/api/wards/{id}` | `admin:*` |
| 病房 | DELETE | `/api/wards/{id}` | `admin:*` |
| 病床 | GET | `/api/beds` | `admin:*` |
| 病床 | POST | `/api/beds` | `admin:*` |
| 病床 | PUT | `/api/beds/{id}` | `admin:*` |
| 病床 | DELETE | `/api/beds/{id}` | `admin:*` |
