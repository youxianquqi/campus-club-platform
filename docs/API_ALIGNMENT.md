# API 接口对接文档

## 一、接口对接概览

### 对接范围
本文档涵盖 `F:\enditeam\backend\` 后端与 `f:\enditeam\frontend\` 前端之间的所有 API 接口对接。

### 对接标准
1. **基础路径**：所有 API 以 `/api` 为前缀
2. **认证方式**：Bearer Token（存储于 `sessionStorage.token`）
3. **请求格式**：JSON (`Content-Type: application/json`)
4. **响应格式**：直接返回数据对象或数组

### 角色说明
| 角色值 | 说明 |
|-------|------|
| 0 | 普通学生 |
| 1 | 系统管理员 |
| 2+ | 社团管理员（值为 clubId + 1） |

---

## 二、模块接口对照表

### 1. 认证模块 (AuthController)

| 接口 | 方法 | 路径 | 前端封装 | 说明 |
|-----|------|------|---------|------|
| 登录 | POST | `/api/auth/login` | `auth.login()` | 用户登录认证 |

### 2. 社团模块 (ClubController)

| 接口 | 方法 | 路径 | 前端封装 | 说明 |
|-----|------|------|---------|------|
| 社团列表 | GET | `/api/clubs` | `club.listClubs()` | 分页获取社团列表 |
| 社团详情 | GET | `/api/clubs/{id}` | `club.getClub(id)` | 获取单个社团信息 |
| 我的社团 | GET | `/api/clubs/my` | `club.getMyClub()` | 获取当前用户管理的社团 |
| 创建社团 | POST | `/api/clubs` | `club.createClub(body)` | 创建新社团（管理员） |
| 更新社团 | PUT | `/api/clubs/{id}` | `club.updateClub(id, body)` | 更新社团信息 |
| 删除社团 | DELETE | `/api/clubs/{id}` | `club.deleteClub(id)` | 删除社团 |
| 搜索社团 | GET | `/api/clubs/search` | `club.searchClubs(keyword)` | 按关键词搜索社团 |
| 更新Logo | PUT | `/api/clubs/{id}/logo` | `club.updateClubLogo(clubId, logoUrl)` | 更新社团Logo |
| 更新QQ群 | PUT | `/api/clubs/{id}/qq-group` | `club.updateClubQqGroup(clubId, qqGroup)` | 更新QQ群号 |
| 推荐学生 | GET | `/api/clubs/{id}/recommendations` | `club.listClubRecommendations(clubId)` | 获取社团推荐学生 |
| 更新推荐状态 | PUT | `/api/clubs/recommendations/{recId}/status` | `club.updateRecommendationStatus(recId, body)` | 更新推荐状态 |

### 3. 标签模块 (TagController)

| 接口 | 方法 | 路径 | 前端封装 | 说明 |
|-----|------|------|---------|------|
| 标签列表 | GET | `/api/tags` | `tags.listTags()` | 获取所有标签 |

### 4. 学生模块 (StudentController)

| 接口 | 方法 | 路径 | 前端封装 | 说明 |
|-----|------|------|---------|------|
| 学生信息 | GET | `/api/student/info` | `student.getStudentInfo()` | 获取当前学生信息 |
| 更新信息 | PUT | `/api/student/info` | `student.updateStudentInfo(body)` | 更新学生信息 |
| 更新头像 | PUT | `/api/student/avatar` | `student.updateAvatar(avatarUrl)` | 更新学生头像 |
| 标签检查 | GET | `/api/student/tags/check` | `student.checkTagsSelected()` | 检查是否已选标签 |
| 获取标签 | GET | `/api/student/tags` | `student.getStudentTagIds()` | 获取学生已选标签 |
| 保存标签 | POST | `/api/student/tags` | `student.saveStudentTags(body)` | 保存学生标签 |
| 推荐加分 | PUT | `/api/student/recommend-boost` | `student.saveRecommendBoostClub(clubId)` | 设置推荐加分社团 |

### 5. 报名申请模块 (ApplicationController)

| 接口 | 方法 | 路径 | 前端封装 | 说明 |
|-----|------|------|---------|------|
| 创建报名 | POST | `/api/applications` | `application.createApplication(body)` | 提交报名申请 |
| 我的报名 | GET | `/api/applications/my` | - | 获取我的报名记录 |
| 社团报名列表 | GET | `/api/applications/club/{clubId}` | `application.listByClub(clubId)` | 获取社团所有报名 |
| 待审核报名 | GET | `/api/applications/club/{clubId}/pending` | `application.listPendingByClub(clubId)` | 获取待审核报名 |
| 通过报名 | PUT | `/api/applications/{id}/approve` | `application.approveApplication(id)` | 通过报名申请 |
| 拒绝报名 | PUT | `/api/applications/{id}/reject` | `application.rejectApplication(id, rejectReason)` | 拒绝报名申请 |
| 更新状态 | PUT | `/api/applications/{id}/status` | - | 更新报名状态 |
| 检查成员 | GET | `/api/applications/check` | `application.checkApplication(clubId)` | 检查是否为成员 |

### 6. 社团申请模块 (ClubApplicationController)

| 接口 | 方法 | 路径 | 前端封装 | 说明 |
|-----|------|------|---------|------|
| 创建申请 | POST | `/api/club-applications` | `clubApplication.createClubApplication(body)` | 提交社团创建申请 |
| 我的申请 | GET | `/api/club-applications/my` | - | 获取我的社团申请 |
| 待审核申请 | GET | `/api/club-applications/pending` | - | 获取待审核社团申请 |
| 全部申请 | GET | `/api/club-applications` | `clubApplication.listClubApplications()` | 获取所有社团申请 |
| 通过申请 | PUT | `/api/club-applications/{id}/approve` | `clubApplication.approveClubApplication(id)` | 通过社团申请 |
| 拒绝申请 | PUT | `/api/club-applications/{id}/reject` | `clubApplication.rejectClubApplication(id, rejectReason)` | 拒绝社团申请 |

### 7. 活动模块 (ClubActivityController)

| 接口 | 方法 | 路径 | 前端封装 | 说明 |
|-----|------|------|---------|------|
| 活动列表 | GET | `/api/activities` | `activity.listActivities(params)` | 分页获取活动列表 |
| 活动详情 | GET | `/api/activities/{id}` | `activity.getActivity(id)` | 获取活动详情 |
| 社团活动 | GET | `/api/activities/club/{clubId}` | `activity.listActivitiesByClub(clubId)` | 获取社团活动 |
| 待审批活动 | GET | `/api/activities/pending` | `activity.listPendingActivities()` | 获取待审批活动 |
| 创建活动 | POST | `/api/activities` | `activity.createActivity(body)` | 创建活动 |
| 更新活动 | PUT | `/api/activities/{id}` | `activity.updateActivity(id, body)` | 更新活动 |
| 删除活动 | DELETE | `/api/activities/{id}` | `activity.deleteActivity(id)` | 删除活动 |
| 通过活动 | PUT | `/api/activities/{id}/approve` | `activity.approveActivity(id)` | 通过活动申请 |
| 拒绝活动 | PUT | `/api/activities/{id}/reject` | `activity.rejectActivity(id, rejectReason)` | 拒绝活动申请 |

### 8. 公告模块 (AnnouncementController)

| 接口 | 方法 | 路径 | 前端封装 | 说明 |
|-----|------|------|---------|------|
| 公告列表 | GET | `/api/announcements` | `announcement.listAnnouncements()` | 获取所有公告 |
| 公告详情 | GET | `/api/announcements/{id}` | - | 获取公告详情 |
| 带状态列表 | GET | `/api/announcements/with-read-status` | `announcement.listAnnouncementsWithReadStatus()` | 获取带阅读状态的公告 |
| 未读数量 | GET | `/api/announcements/unread-count` | - | 获取未读公告数量 |
| 标记已读 | POST | `/api/announcements/{id}/read` | `announcement.markAnnouncementRead(id)` | 标记公告已读 |
| 创建公告 | POST | `/api/announcements` | `announcement.createAnnouncement(body)` | 创建公告 |
| 更新公告 | PUT | `/api/announcements/{id}` | `announcement.updateAnnouncement(id, body)` | 更新公告 |
| 删除公告 | DELETE | `/api/announcements/{id}` | `announcement.deleteAnnouncement(id)` | 删除公告 |

### 9. 聊天模块 (ChatController)

| 接口 | 方法 | 路径 | 前端封装 | 说明 |
|-----|------|------|---------|------|
| 消息列表 | GET | `/api/chat/messages` | `chat.listMessages(params)` | 分页获取消息 |
| 发送消息 | POST | `/api/chat/messages` | `chat.sendMessage(body)` | 发送消息 |

### 10. 评价模块 (PeerEvaluationController)

| 接口 | 方法 | 路径 | 前端封装 | 说明 |
|-----|------|------|---------|------|
| 邻近学生 | GET | `/api/evaluations/neighbors` | `evaluation.listNeighborStudents()` | 获取邻近学生列表 |
| 我的评价 | GET | `/api/evaluations/my-evaluations` | `evaluation.listMyEvaluations()` | 获取我发出的评价 |
| 收到的评价 | GET | `/api/evaluations/received` | `evaluation.listReceivedEvaluations()` | 获取收到的评价 |
| 提交评价 | POST | `/api/evaluations` | `evaluation.submitEvaluation(body)` | 提交评价 |

### 11. 邀请模块 (InvitationController)

| 接口 | 方法 | 路径 | 前端封装 | 说明 |
|-----|------|------|---------|------|
| 创建邀请 | POST | `/api/invitations` | `invitation.createInvitation(body)` | 发送邀请 |
| 我的邀请 | GET | `/api/invitations/my` | `invitation.listMyInvitations()` | 获取收到的邀请 |
| 社团邀请 | GET | `/api/invitations/club/{clubId}` | - | 获取社团发出的邀请 |
| 接受邀请 | PUT | `/api/invitations/{id}/accept` | `invitation.acceptInvitation(id)` | 接受邀请 |
| 拒绝邀请 | PUT | `/api/invitations/{id}/reject` | `invitation.rejectInvitation(id)` | 拒绝邀请 |
| 完成邀请 | PUT | `/api/invitations/{id}/complete` | `invitation.completeInvitation(id)` | 完成邀请 |
| 待处理数量 | GET | `/api/invitations/pending-count` | `invitation.pendingInvitationCount()` | 获取待处理邀请数量 |

### 12. 推荐模块 (RecommendationController)

| 接口 | 方法 | 路径 | 前端封装 | 说明 |
|-----|------|------|---------|------|
| 推荐列表 | GET | `/api/recommendations` | `recommendation.listRecommendations(params)` | 获取推荐社团列表 |

### 13. AI模块 (AIController)

| 接口 | 方法 | 路径 | 前端封装 | 说明 |
|-----|------|------|---------|------|
| AI推荐 | POST | `/api/ai/recommend` | `ai.recommendClubs(body)` | AI推荐社团 |

### 14. 问答模块 (ClubQuestionController)

| 接口 | 方法 | 路径 | 前端封装 | 说明 |
|-----|------|------|---------|------|
| 社团问题 | GET | `/api/questions/club/{clubId}` | `question.listQuestionsByClub(clubId)` | 获取社团问题列表 |
| 我的问题 | GET | `/api/questions/my` | - | 获取我的问题 |
| 提问 | POST | `/api/questions` | `question.createQuestion(body)` | 提交问题 |
| 回答 | POST | `/api/questions/{questionId}/answer` | `question.answerQuestion(questionId, body)` | 回答问题 |

### 15. 隐藏标签模块 (ClubHiddenTagController)

| 接口 | 方法 | 路径 | 前端封装 | 说明 |
|-----|------|------|---------|------|
| 隐藏标签 | GET | `/api/club-hidden-tags/club/{clubId}` | `clubHiddenTags.getHiddenTags(clubId)` | 获取社团隐藏标签 |
| 可见标签 | GET | `/api/club-hidden-tags/visible/club/{clubId}` | `clubHiddenTags.getVisibleTags(clubId)` | 获取社团可见标签 |
| 全部标签 | GET | `/api/club-hidden-tags/all/club/{clubId}` | `clubHiddenTags.getAllTagIdsForMatching(clubId)` | 获取匹配用标签 |
| 添加隐藏标签 | POST | `/api/club-hidden-tags/add` | `clubHiddenTags.addHiddenTag(body)` | 添加隐藏标签 |
| 删除隐藏标签 | DELETE | `/api/club-hidden-tags/remove` | `clubHiddenTags.removeHiddenTag(body)` | 删除隐藏标签 |

### 16. 用户行为模块 (UserBehaviorController)

| 接口 | 方法 | 路径 | 前端封装 | 说明 |
|-----|------|------|---------|------|
| 记录进入 | POST | `/api/behavior/enter` | `behavior.recordEnter(body)` | 记录页面进入 |
| 记录退出 | PUT | `/api/behavior/exit/{id}` | `behavior.recordExit(behaviorId)` | 记录页面退出 |
| 我的行为 | GET | `/api/behavior/my` | - | 获取我的行为记录 |
| 按页面查询 | GET | `/api/behavior/my/page/{pageName}` | - | 按页面查询行为 |
| 页面统计 | GET | `/api/behavior/stats/page/{pageName}` | - | 获取页面统计 |

### 17. 用户管理模块 (AdminController)

| 接口 | 方法 | 路径 | 前端封装 | 说明 |
|-----|------|------|---------|------|
| 用户列表 | GET | `/api/admin/users` | `admin.listUsers()` | 获取所有用户 |
| 更新角色 | PUT | `/api/admin/users/{userId}/role` | `admin.updateUserRole(userId, role)` | 更新用户角色 |

### 18. 用户模块 (UserController)

| 接口 | 方法 | 路径 | 前端封装 | 说明 |
|-----|------|------|---------|------|
| 修改密码 | PUT | `/api/user/password` | `user.changePassword(payload)` | 修改密码 |

### 19. 文件上传模块 (FileUploadController)

| 接口 | 方法 | 路径 | 前端封装 | 说明 |
|-----|------|------|---------|------|
| 活动图片 | POST | `/api/upload/activity-image` | `upload.uploadFile('activity-image', file)` | 上传活动图片 |
| 社团Logo | POST | `/api/upload/club-logo` | `upload.uploadFile('club-logo', file)` | 上传社团Logo |
| 头像 | POST | `/api/upload/avatar` | `upload.uploadFile('avatar', file)` | 上传头像 |

---

## 三、详细接口说明

### 1. 登录接口

**请求**
```
POST /api/auth/login
Content-Type: application/json

{
  "studentNo": "2024001",
  "password": "010001"
}
```

**成功响应**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "username": "张三",
  "role": 0,
  "relatedId": 1
}
```

**错误响应**
- 用户不存在：抛出异常 "用户不存在"
- 密码错误：抛出异常 "密码错误"

### 2. 社团列表接口

**请求**
```
GET /api/clubs?page=0&size=10&tagIds=1,2,3
Authorization: Bearer {token}
```

**响应**
```json
{
  "clubs": [
    {
      "id": 1,
      "name": "篮球社",
      "description": "热爱篮球的同学聚集地",
      "qqGroup": "12345678",
      "logoUrl": "/club-logos/xxx.png",
      "adminId": 5,
      "adminName": "李四",
      "memberCount": 30,
      "tags": [1, 2],
      "createdAt": "2024-01-01T00:00:00"
    }
  ],
  "total": 100,
  "pages": 10,
  "currentPage": 0
}
```

### 3. 报名申请接口

**请求**
```
POST /api/applications
Authorization: Bearer {token}
Content-Type: application/json

{
  "clubId": 1,
  "intro": "我对这个社团很感兴趣...",
  "phone": "13800138000"
}
```

**响应**
```json
{
  "id": 1,
  "studentId": 1,
  "clubId": 1,
  "selfIntro": "我对这个社团很感兴趣...",
  "phone": "13800138000",
  "status": 0,
  "submittedAt": "2024-01-01T00:00:00"
}
```

---

## 四、对接问题记录

### 已发现并修复的问题

| 问题 | 描述 | 解决方案 | 状态 |
|-----|------|---------|------|
| admin.js 路径错误 | 原代码使用 `/admin/users/${userId}/role`，后端实际路径为 `/admin/users/{userId}/role` | 已修正为正确路径 | ✅ 已修复 |
| 类型定义不完整 | 前端 types.d.ts 类型定义不完整 | 已补充完整类型定义 | ✅ 已修复 |

### 当前状态

- ✅ 所有后端接口已分析完成
- ✅ 前端 API 封装已完善
- ✅ 前端代码已统一使用 API 封装模块
- ✅ 类型定义已完善

---

## 五、前端 API 文件结构

```
src/api/
├── http.js           # Axios 实例配置
├── index.js          # 统一导出
├── types.d.ts        # TypeScript 类型定义
├── auth.js           # 认证相关
├── club.js           # 社团相关
├── tags.js           # 标签相关
├── student.js        # 学生相关
├── application.js    # 报名申请相关
├── clubApplication.js # 社团申请相关
├── activity.js       # 活动相关
├── announcement.js   # 公告相关
├── chat.js           # 聊天相关
├── evaluation.js     # 评价相关
├── invitation.js     # 邀请相关
├── recommendation.js # 推荐相关
├── ai.js             # AI 相关
├── question.js       # 问答相关
├── clubHiddenTags.js # 隐藏标签相关
├── behavior.js       # 用户行为相关
├── admin.js          # 管理员相关
├── user.js           # 用户相关
└── upload.js         # 文件上传相关
```

---

## 六、后续维护建议

1. **新增接口时**：
   - 后端新增 Controller 或接口后，同步更新对应的前端 API 文件
   - 在 types.d.ts 中添加对应的类型定义

2. **接口变更时**：
   - 更新本文档中的接口说明
   - 检查前端调用代码是否需要同步修改

3. **测试验证**：
   - 每次修改后运行前端项目验证接口调用
   - 检查浏览器控制台是否有 API 错误

---

*文档生成时间：2026-03-23*
