# Endtiem 后端项目

**版本**: 1.0  
**更新日期**: 2026-03-28

---

## 项目简介

Endtiem 校园社团平台的后端服务，基于 Spring Boot 3.2.12 构建，采用多模块Maven架构。

---

## 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.2.12 | 应用框架 |
| MyBatis-Plus | 3.5.6 | ORM框架 |
| MySQL | 8.0 | 主数据库 |
| Redis | 7.0 | 缓存（可选） |
| JWT | 0.12.5 | 认证 |
| JDK | 17 | Java版本 |

---

## 项目结构

```
backend/
├── backend-common/               # 公共基础模块
│   ├── src/main/java/com/endtiem/common/
│   │   ├── annotation/        # 自定义注解
│   │   ├── aop/              # 切面编程
│   │   ├── api/               # 统一响应
│   │   ├── config/            # 配置类
│   │   ├── dto/               # 数据传输对象
│   │   ├── exception/          # 异常处理
│   │   └── util/              # 工具类
│   └── pom.xml
│
├── backend-club/                # 社团管理模块
│   ├── src/main/java/com/endtiem/club/
│   │   ├── controller/        # 控制器层
│   │   ├── service/           # 服务层
│   │   ├── mapper/            # 数据访问层
│   │   ├── entity/            # 实体类
│   │   └── dto/               # 数据传输对象
│   └── pom.xml
│
├── backend-recruitment/          # 招新管理模块
│   ├── src/main/java/com/endtiem/recruitment/
│   │   ├── controller/
│   │   ├── service/
│   │   ├── mapper/
│   │   ├── entity/
│   │   └── dto/
│   └── pom.xml
│
├── backend-platform/             # 平台功能模块
│   ├── src/main/java/com/endtiem/platform/
│   │   ├── controller/
│   │   ├── service/
│   │   ├── mapper/
│   │   ├── entity/
│   │   └── dto/
│   └── pom.xml
│
├── backend-admin/                # 管理后台模块
│   ├── src/main/java/com/endtiem/admin/
│   │   ├── controller/
│   │   ├── service/
│   │   ├── mapper/
│   │   └── entity/
│   └── pom.xml
│
├── backend-application/         # 主应用启动模块
│   ├── src/main/java/com/endtiem/
│   │   └── EndtiemApplication.java  # 启动类
│   ├── src/main/resources/
│   │   ├── application.yml          # 主配置
│   │   ├── application-dev.yml      # 开发环境
│   │   ├── application-prod.yml     # 生产环境
│   │   └── db/                     # 数据库脚本
│   └── pom.xml
│
└── pom.xml                      # 父POM
```

---

## 模块说明

### backend-common

公共基础模块，提供通用功能：

- `JwtUtil`: JWT工具类
- `Result`: 统一响应封装
- `GlobalExceptionHandler`: 全局异常处理
- `MybatisPlusConfig`: MyBatis-Plus配置

### backend-club

社团管理模块，负责：

- 社团基本信息管理
- 社团活动管理
- 社团标签管理
- 社团问答管理

### backend-recruitment

招新管理模块，负责：

- 学生信息管理
- 社团成员管理
- 入会申请管理
- 学生标签管理
- 招募帖管理

### backend-platform

平台功能模块，负责：

- AI智能推荐
- 实时聊天
- 系统公告
- 用户行为跟踪
- 同伴互评

### backend-admin

管理后台模块，负责：

- 用户管理
- 认证管理
- 招募帖管理
- 管理员功能

---

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.6+
- MySQL 8.0+

### 编译项目

```bash
# 使用Maven Wrapper
.\mvnw.cmd clean compile

# 或使用Maven
mvn clean compile
```

### 启动项目

```bash
# 开发环境启动
.\mvnw.cmd spring-boot:run

# 打包后启动
.\mvnw.cmd clean package
java -jar backend-application/target/backend-application-0.0.1-SNAPSHOT.jar
```

### 代码格式化

```bash
# 检查代码格式
.\mvnw.cmd spotless:check

# 格式化代码
.\mvnw.cmd spotless:apply
```

---

## 配置说明

### 数据库配置

文件: `backend-application/src/main/resources/application.yml`

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/endtiem?useUnicode=true&characterEncoding=utf8
    username: root
    password: your_password
  jpa:
    show-sql: true
```

### Redis配置（可选）

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
```

### JWT配置

```yaml
jwt:
  secret: your-secret-key
  expiration: 86400000  # 24小时
```

---

## API接口

### 认证接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/auth/login` | POST | 用户登录 |
| `/api/auth/register` | POST | 用户注册 |

### 社团接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/clubs` | GET | 获取社团列表 |
| `/api/clubs/{id}` | GET | 获取社团详情 |
| `/api/clubs` | POST | 创建社团 |
| `/api/clubs/{id}` | PUT | 更新社团 |

### 活动接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/activities` | GET | 获取活动列表 |
| `/api/activities/{id}` | GET | 获取活动详情 |

---

## 开发指南

### 代码规范

遵循Google Java Format (AOSP风格)：

- 4空格缩进
- 左大括号不换行
- 每行不超过120字符

详见: `CODE_STYLE_GUIDE.md`

### Service层开发

新增Service接口和实现：

```java
// 接口
public interface ClubService {
    Club getById(Long id);
}

// 实现
@Service
public class ClubServiceImpl implements ClubService {
    // ...
}
```

### 数据库操作

使用MyBatis-Plus进行数据库操作：

```java
// 继承IService
public interface ClubMapper extends BaseMapper<Club> {
    // 自定义方法
}
```

---

## 项目整理记录

本项目已完成以下整理工作：

- ✅ 阶段一：项目结构分析
- ✅ 阶段二：文件分类整理
- ✅ 阶段三：代码规范统一
- ✅ 阶段四：冗余文件清理
- ✅ 阶段五：文档完善

详见相关文档：

- `PROJECT_STRUCTURE_ANALYSIS.md`
- `ARCHITECTURE_ANALYSIS.md`
- `STRUCTURE_STANDARD.md`
- `CODE_STYLE_GUIDE.md`

---

## 版本历史

| 版本 | 日期 | 说明 |
|------|------|------|
| 1.0 | 2026-03-28 | 初始版本，完成项目整理 |

---

## 许可证

本项目仅供学习交流使用。

---

**祝你开发愉快！** 🎉
