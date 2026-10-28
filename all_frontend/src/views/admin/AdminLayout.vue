<template>
  <div class="admin-layout">
    <aside class="side">
      <div class="brand">
        <router-link to="/admin">社团之家 · 管理</router-link>
      </div>
      <nav class="nav">
        <router-link to="/admin" class="nav-item" active-class="active">工作台</router-link>
        <router-link to="/admin/clubs/pending" class="nav-item" active-class="active"
          >待审核社团</router-link
        >
        <router-link to="/admin/activities/pending" class="nav-item" active-class="active"
          >待审核活动</router-link
        >
      </nav>
      <div class="side-foot">
        <router-link to="/" class="link-front">← 返回前台</router-link>
        <button type="button" class="btn-out" @click="logout">退出登录</button>
      </div>
    </aside>
    <div class="content">
      <router-view />
    </div>
  </div>
</template>

<script>
export default {
  name: 'AdminLayout',
  methods: {
    logout() {
      localStorage.removeItem('isLoggedIn');
      localStorage.removeItem('userId');
      localStorage.removeItem('username');
      localStorage.removeItem('nickname');
      localStorage.removeItem('avatar');
      localStorage.removeItem('userRole');
      this.$router.replace('/login');
    },
  },
};
</script>

<style scoped>
.admin-layout {
  min-height: 100vh;
  display: flex;
  background: var(--admin-layout-bg);
}

.side {
  width: 220px;
  flex-shrink: 0;
  background: var(--admin-sidebar-bg);
  color: var(--admin-sidebar-text);
  display: flex;
  flex-direction: column;
  padding: 20px 0;
}

.brand a {
  display: block;
  padding: 0 var(--spacing-xl) var(--spacing-xl);
  font-size: var(--font-size-lg);
  font-weight: 600;
  color: var(--header-text-on-gradient);
  text-decoration: none;
}

.nav {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.nav-item {
  padding: var(--spacing-md) var(--spacing-xl);
  color: var(--admin-sidebar-muted);
  text-decoration: none;
  font-size: 14px;
  border-left: 3px solid transparent;
}

.nav-item:hover {
  color: var(--header-text-on-gradient);
  background: rgba(255, 255, 255, 0.06);
}

.nav-item.active {
  color: var(--header-text-on-gradient);
  background: var(--admin-accent-soft);
  border-left-color: var(--color-primary);
}

.side-foot {
  padding: var(--spacing-lg) var(--spacing-xl);
  border-top: 1px solid var(--admin-sidebar-border);
}

.link-front {
  font-size: var(--font-size-sm);
  color: var(--admin-sidebar-muted);
  text-decoration: none;
}

.link-front:hover {
  color: var(--header-text-on-gradient);
}

.btn-out {
  display: block;
  width: 100%;
  margin-top: var(--spacing-md);
  padding: var(--spacing-sm) var(--spacing-md);
  font-size: var(--font-size-sm);
  color: #fca5a5;
  background: transparent;
  border: 1px solid var(--admin-sidebar-border);
  border-radius: var(--radius-sm);
  cursor: pointer;
}

.btn-out:hover {
  color: #fecaca;
  border-color: var(--admin-sidebar-muted);
}

.content {
  flex: 1;
  min-width: 0;
  padding: var(--spacing-xl) 28px;
  box-sizing: border-box;
}
</style>
