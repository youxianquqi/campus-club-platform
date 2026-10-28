# Endtiem 前端项目

**版本**: 1.0  
**更新日期**: 2026-03-28

---

## 项目简介

Endtiem 校园社团平台的前端应用，基于 Vue 3 + Vite 构建，为学生和社团管理员提供友好的Web界面。

---

## 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.4.0 | 框架 |
| Vite | 5.0.0 | 构建工具 |
| Vue Router | 4.2.5 | 路由 |
| Axios | 1.13.6 | HTTP客户端 |

---

## 项目结构

```
all_frontend/
├── src/
│   ├── api/                    # API接口模块
│   │   ├── modules/           # API模块化
│   │   │   ├── activity.js    # 活动API
│   │   │   ├── club.js        # 社团API
│   │   │   ├── platform.js    # 平台API
│   │   │   └── ...
│   │   ├── client.js         # API客户端
│   │   ├── http.js           # HTTP拦截器
│   │   └── index.js          # API统一导出
│   │
│   ├── assets/                # 资源文件
│   │   └── images/           # 图片资源
│   │
│   ├── components/            # 通用组件
│   │   ├── common/           # 通用业务组件
│   │   │   ├── EmptyState.vue      # 空状态
│   │   │   ├── LoadingSpinner.vue  # 加载指示器
│   │   │   ├── ProgressBar.vue     # 进度条
│   │   │   ├── SiteHeader.vue     # 站点头部
│   │   │   └── Toast.vue          # 消息提示
│   │   ├── skeleton/              # 骨架屏组件
│   │   │   ├── HomeSkeleton.vue
│   │   │   └── ClubListSkeleton.vue
│   │   └── layout/               # 布局组件
│   │
│   ├── constants/              # 常量定义
│   │   └── tagIcons.js          # 标签图标映射
│   │
│   ├── directives/             # 自定义指令
│   │   └── lazyLoad.js         # 懒加载指令
│   │
│   ├── mock/                  # 模拟数据
│   │   └── data.js
│   │
│   ├── router/                 # 路由配置
│   │   └── index.js
│   │
│   ├── store/                 # 状态管理
│   │   └── index.js
│   │
│   ├── utils/                  # 工具函数
│   │   ├── api.js            # API工具
│   │   ├── auth.js           # 认证工具
│   │   └── debounce.js        # 防抖工具
│   │
│   ├── views/                 # 页面视图
│   │   ├── Home.vue           # 首页
│   │   ├── Login.vue          # 登录页
│   │   ├── Register.vue       # 注册页
│   │   ├── ClubList.vue       # 社团列表
│   │   ├── ClubDetail.vue     # 社团详情
│   │   ├── CreateClub.vue     # 创建社团
│   │   ├── ClubManage.vue     # 社团管理
│   │   ├── ClubApply.vue      # 社团申请
│   │   ├── Activities.vue      # 活动列表
│   │   ├── ActivityDetail.vue  # 活动详情
│   │   ├── Recruitment.vue    # 招新页面
│   │   ├── Profile.vue       # 个人中心
│   │   ├── TagSelection.vue   # 标签选择
│   │   ├── Chat.vue           # 聊天页面
│   │   └── admin/            # 管理后台
│   │       ├── AdminDashboard.vue
│   │       ├── AdminClubAudit.vue
│   │       ├── AdminActivityAudit.vue
│   │       └── AdminLayout.vue
│   │
│   ├── App.vue                # 根组件
│   ├── main.js                # 应用入口
│   └── style.css              # 全局样式
│
├── public/                    # 公共资源
│   ├── favicon.svg
│   ├── icons.svg
│   └── img-placeholder.svg
│
├── index.html                 # HTML模板
├── package.json               # 项目配置
├── vite.config.js             # Vite配置
├── .eslintrc.js              # ESLint配置
├── .prettierrc              # Prettier配置
└── .editorconfig             # 编辑器配置
```

---

## 页面路由

| 路由 | 页面 | 权限 |
|------|------|------|
| `/` | Home.vue | 公开 |
| `/login` | Login.vue | 公开 |
| `/register` | Register.vue | 公开 |
| `/clubs` | ClubList.vue | 公开 |
| `/club/:id` | ClubDetail.vue | 公开 |
| `/activities` | Activities.vue | 公开 |
| `/activity/:id` | ActivityDetail.vue | 公开 |
| `/recruitment` | Recruitment.vue | 公开 |
| `/profile` | Profile.vue | 需登录 |
| `/create-club` | CreateClub.vue | 需登录 |
| `/club-manage` | ClubManage.vue | 需登录 |
| `/club-apply/:id` | ClubApply.vue | 需登录 |
| `/tag-selection` | TagSelection.vue | 需登录 |
| `/chat` | Chat.vue | 需登录 |
| `/admin` | AdminDashboard.vue | 管理员 |

---

## 快速开始

### 环境要求

- Node.js 16+
- npm 或 yarn

### 安装依赖

```bash
# 进入前端目录
cd all_frontend

# 安装依赖
npm install
```

### 开发服务器

```bash
# 启动开发服务器
npm run dev

# 访问地址
# http://localhost:5173/
```

### 构建生产版本

```bash
# 构建生产版本
npm run build

# 预览生产版本
npm run preview
```

---

## 代码格式化

### 安装开发依赖

```bash
npm install eslint prettier eslint-plugin-vue --save-dev
```

### 格式化命令

```bash
# 检查代码格式
npm run lint

# 自动修复格式问题
npm run lint:fix

# 格式化所有文件
npm run format
```

### IDE配置

推荐使用VS Code，配置 `settings.json`：

```json
{
  "editor.formatOnSave": true,
  "editor.defaultFormatter": "esbenp.prettier-vscode",
  "editor.codeActionsOnSave": {
    "source.fixAll.eslint": true
  }
}
```

---

## API接口

### 配置说明

API基础配置位于 `src/api/client.js`：

```javascript
const client = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  timeout: 10000,
})
```

### HTTP拦截器

请求和响应拦截器位于 `src/api/http.js`：
- 自动添加Token到请求头
- 统一处理响应错误
- 错误提示

### API模块

按功能模块组织的API：

```javascript
// 导入
import { clubApi } from '@/api/modules/club'
import { activityApi } from '@/api/modules/activity'

// 使用
clubApi.list().then(res => {
  // 处理响应
})
```

---

## 组件使用

### 通用组件

```vue
<template>
  <!-- 空状态 -->
  <EmptyState message="暂无数据" />

  <!-- 加载指示器 -->
  <LoadingSpinner v-if="loading" />

  <!-- 进度条 -->
  <ProgressBar :value="60" />

  <!-- 站点头部 -->
  <SiteHeader />
</template>

<script setup>
import EmptyState from '@/components/common/EmptyState.vue'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'
import ProgressBar from '@/components/common/ProgressBar.vue'
import SiteHeader from '@/components/common/SiteHeader.vue'
</script>
```

### 骨架屏

```vue
<template>
  <!-- 首页骨架屏 -->
  <HomeSkeleton v-if="loading" />

  <!-- 社团列表骨架屏 -->
  <ClubListSkeleton v-if="loading" />
</template>

<script setup>
import HomeSkeleton from '@/components/skeleton/HomeSkeleton.vue'
import ClubListSkeleton from '@/components/skeleton/ClubListSkeleton.vue'
</script>
```

---

## 状态管理

项目使用简单的状态管理，位于 `src/store/index.js`：

```javascript
import { createStore } from 'vuex'

export default createStore({
  state: {
    user: null,
    token: localStorage.getItem('token'),
  },
  mutations: {
    setUser(state, user) {
      state.user = user
    },
    setToken(state, token) {
      state.token = token
      localStorage.setItem('token', token)
    },
  },
  actions: {
    login({ commit }, { token, user }) {
      commit('setToken', token)
      commit('setUser', user)
    },
  },
})
```

---

## 开发指南

### 代码规范

遵循项目代码风格规范：

- 2空格缩进
- 单引号字符串
- 行尾分号
- 尾随逗号

详见: `CODE_STYLE_GUIDE.md`

### 组件命名

```vue
<!-- PascalCase -->
<template>
  <UserProfile />
  <ClubDetail />
  <ActivityCard />
</template>
```

### 路由守卫

```javascript
// src/router/index.js
router.beforeEach((to, from, next) => {
  const isLoggedIn = localStorage.getItem('isLoggedIn') === 'true'

  if (to.meta.requiresAuth && !isLoggedIn) {
    next({ path: '/login' })
  } else {
    next()
  }
})
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
