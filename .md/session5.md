# Session 5：新增员工与员工分页查询

## 这次做了什么

这次我们继续完善员工管理模块，完成了三个核心能力：

```text
1. 用 ThreadLocal 保存当前登录员工 id
2. 新增员工接口
3. 员工分页查询接口
```

同时我们发现并修复了一个重要问题：

```text
分页查询不能把 password 返回给前端
```

所以我们新增了 `EmployeePageVO`，专门控制分页接口返回字段。

## 一、BaseContext 和 ThreadLocal

路径：

```text
src/main/java/com/sky/sky_server/context/BaseContext.java
```

作用：

```text
在一次请求过程中，保存当前登录员工 id。
```

数据流：

```text
前端请求带 token
  -> JwtTokenAdminInterceptor 解析 token
  -> 得到 employeeId
  -> BaseContext.setCurrentId(employeeId)
  -> 后续 Service 可以 BaseContext.getCurrentId()
```

为什么需要它：

后面新增员工、修改员工时，需要知道：

```text
当前是谁在操作？
```

比如这些数据库字段：

```text
create_user
update_user
```

就可以用当前登录员工 id 自动填充。

## 二、新增员工接口

接口：

```text
POST /admin/employee
```

请求体示例：

```json
{
  "username": "zhangsan",
  "name": "zhangsan",
  "phone": "13800138000",
  "sex": "1",
  "idNumber": "110101199001011234"
}
```

### EmployeeDTO

路径：

```text
src/main/java/com/sky/sky_server/dto/EmployeeDTO.java
```

作用：

```text
接收前端新增员工时传来的 JSON 数据。
```

字段：

```text
username
name
phone
sex
idNumber
```

### EmployeeService.save

路径：

```text
src/main/java/com/sky/sky_server/service/EmployeeService.java
```

新增方法：

```java
void save(EmployeeDTO employeeDTO);
```

### EmployeeServiceImpl.save

路径：

```text
src/main/java/com/sky/sky_server/service/impl/EmployeeServiceImpl.java
```

主要逻辑：

```text
1. 创建 Employee 实体对象
2. 把 EmployeeDTO 里的字段复制到 Employee
3. 设置默认密码 123456 的 MD5
4. 设置账号状态 status = 1
5. 设置 createTime / updateTime
6. 设置 createUser / updateUser 为当前登录员工 id
7. 调用 employeeMapper.insert(employee)
```

关键代码思路：

```java
employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
employee.setStatus(1);
employee.setCreateUser(BaseContext.getCurrentId());
employee.setUpdateUser(BaseContext.getCurrentId());
```

### EmployeeMapper.insert

路径：

```text
src/main/java/com/sky/sky_server/mapper/EmployeeMapper.java
```

作用：

```text
把 Employee 插入 employee 表。
```

这里要注意数据库字段和 Java 属性的区别：

```text
数据库：id_number
Java：idNumber
```

MyBatis 插入时使用 Java 属性：

```java
#{idNumber}
```

### EmployeeController.save

路径：

```text
src/main/java/com/sky/sky_server/controller/EmployeeController.java
```

新增接口：

```java
@PostMapping
public Result<String> save(@RequestBody EmployeeDTO employeeDTO)
```

因为类上有：

```java
@RequestMapping("/admin/employee")
```

所以最终路径是：

```text
POST /admin/employee
```

## 三、重复用户名处理

新增员工时，`username` 不能重复。

推荐数据库层加唯一索引：

```sql
alter table employee add unique index idx_username(username);
```

如果重复插入，会触发数据库唯一约束异常。

我们在全局异常处理器里捕获：

```text
SQLIntegrityConstraintViolationException
```

然后返回统一响应：

```json
{
  "code": 0,
  "msg": "username already exists",
  "data": null
}
```

这体现了一个常见后端思路：

```text
数据库保证最终一致性
全局异常处理器负责把底层错误翻译成前端能理解的结果
```

## 四、员工分页查询

接口：

```text
GET /admin/employee/page
```

请求示例：

```text
/admin/employee/page?page=1&pageSize=10
/admin/employee/page?page=1&pageSize=10&name=zhang
```

返回结构：

```json
{
  "code": 1,
  "msg": "success",
  "data": {
    "total": 3,
    "records": []
  }
}
```

### PageHelper

我们在 `pom.xml` 中添加了分页插件：

```xml
<dependency>
    <groupId>com.github.pagehelper</groupId>
    <artifactId>pagehelper-spring-boot-starter</artifactId>
    <version>2.1.0</version>
</dependency>
```

作用：

```text
帮助 MyBatis 自动处理分页 SQL。
```

也就是自动处理：

```sql
limit offset, pageSize
```

### EmployeePageQueryDTO

路径：

```text
src/main/java/com/sky/sky_server/dto/EmployeePageQueryDTO.java
```

作用：

```text
接收分页查询参数。
```

字段：

```text
name
page
pageSize
```

GET 请求不需要 `@RequestBody`。

Spring 会自动把 URL 参数封装成 DTO：

```text
?page=1&pageSize=10&name=zhang
```

### PageResult

路径：

```text
src/main/java/com/sky/sky_server/result/PageResult.java
```

作用：

```text
统一分页返回结果。
```

字段：

```text
total：总记录数
records：当前页数据
```

### EmployeeMapper.pageQuery

路径：

```text
src/main/java/com/sky/sky_server/mapper/EmployeeMapper.java
```

使用 MyBatis 动态 SQL：

```text
如果 name 不为空，就按 name 模糊查询；
如果 name 为空，就查全部。
```

注意：

```text
分页查询不能 select *
```

因为 `employee` 表里有 `password` 字段。

现在查询字段明确写成：

```sql
select id, name, username, phone, sex, id_number, status, create_time, update_time, create_user, update_user
from employee
```

这样可以避免把 `password` 查出来。

## 五、EmployeePageVO

路径：

```text
src/main/java/com/sky/sky_server/vo/EmployeePageVO.java
```

作用：

```text
控制分页接口返回给前端的数据结构。
```

它和 `Employee` 的区别：

```text
Employee 是数据库实体，可以包含 password。
EmployeePageVO 是接口返回对象，不能包含 password。
```

这是 DTO / Entity / VO 分层的意义：

```text
DTO：前端传给后端
Entity：后端映射数据库表
VO：后端返回给前端
```

## 六、测试命令

启动项目：

```powershell
mvn spring-boot:run
```

登录拿 token：

```powershell
$loginResult = Invoke-RestMethod -Uri "http://localhost:8088/admin/employee/login" -Method Post -ContentType "application/json" -Body '{"username":"admin","password":"123456"}'
$token = $loginResult.data.token
```

新增员工：

```powershell
Invoke-RestMethod -Uri "http://localhost:8088/admin/employee" -Method Post -ContentType "application/json" -Headers @{token=$token} -Body '{"username":"zhangsan","name":"zhangsan","phone":"13800138000","sex":"1","idNumber":"110101199001011234"}' | ConvertTo-Json -Depth 5
```

分页查询：

```powershell
Invoke-RestMethod -Uri "http://localhost:8088/admin/employee/page?page=1&pageSize=10" -Method Get -Headers @{token=$token} | ConvertTo-Json -Depth 5
```

按姓名查询：

```powershell
Invoke-RestMethod -Uri "http://localhost:8088/admin/employee/page?page=1&pageSize=10&name=zhang" -Method Get -Headers @{token=$token} | ConvertTo-Json -Depth 5
```

## 七、当前状态

已经完成：

```text
员工登录
JWT token 生成
JWT 拦截器校验
BaseContext 保存当前员工 id
新增员工
重复用户名异常处理
员工分页查询
分页结果去掉 password
```

下一步建议：

```text
启用 / 禁用员工账号
```

也就是修改员工的：

```text
status
```

接口一般设计为：

```text
POST /admin/employee/status/{status}?id=xxx
```

