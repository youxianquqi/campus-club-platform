<template>
  <header class="site-header">
    <div class="header-inner">
      <button class="mobile-menu-btn" @click="toggleMobileMenu" aria-label="菜单">
        <span class="hamburger" :class="{ active: mobileMenuOpen }"></span>
      </button>

      <router-link to="/" class="logo">社团之家</router-link>

      <nav class="nav" :class="{ 'mobile-open': mobileMenuOpen }">
        <router-link
          v-for="item in mainNav"
          :key="item.to"
          :to="item.to"
          class="nav-link"
          :class="{ active: isActive(item.to) }"
          @click="closeMobileMenu"
        >
          {{ item.label }}
        </router-link>
      </nav>

      <div class="right">
        <router-link to="/clubs" class="nav-link subtle desktop-only">社团</router-link>
        <router-link to="/activities" class="nav-link subtle desktop-only">活动</router-link>
        <router-link to="/recruitment" class="nav-link subtle desktop-only">招募</router-link>
        <router-link to="/knowledge" class="nav-link subtle desktop-only">📚 知识库</router-link>
        <router-link to="/create-club" class="nav-link subtle desktop-only">创建社团</router-link>
        <router-link to="/chat" class="nav-link subtle desktop-only">社团联系</router-link>
        <template v-if="loggedIn">
          <router-link v-if="isAdmin" to="/admin" class="nav-link subtle desktop-only"
            >管理后台</router-link
          >
          <router-link to="/profile" class="nav-link subtle desktop-only">我的</router-link>
          <button type="button" class="btn-text desktop-only" @click="logout">退出</button>
        </template>
        <template v-else class="desktop-only">
          <router-link to="/login" class="btn-outline">登录</router-link>
          <router-link to="/register" class="btn-solid">注册</router-link>
        </template>
      </div>

      <div class="mobile-auth mobile-only">
        <ThemeToggle compact />
        <template v-if="loggedIn">
          <router-link to="/profile" class="nav-link" @click="closeMobileMenu">我的</router-link>
          <router-link v-if="isAdmin" to="/admin" class="nav-link" @click="closeMobileMenu"
            >管理后台</router-link
          >
          <button type="button" class="btn-text" @click="logout">退出</button>
        </template>
        <template v-else>
          <router-link to="/login" class="btn-outline" @click="closeMobileMenu">登录</router-link>
          <router-link to="/register" class="btn-solid" @click="closeMobileMenu">注册</router-link>
        </template>
      </div>
    </div>

    <div v-if="mobileMenuOpen" class="mobile-menu-overlay" @click="closeMobileMenu"></div>
  </header>
</template>

<script>
export default {
  name: 'SiteHeader',
  data() {
    return {
      mainNav: [
        { to: '/', label: '首页' },
        { to: '/recruitment', label: '招募' },
        { to: '/brainstorm', label: '集思广益' },
      ],
      mobileMenuOpen: false,
    };
  },
  computed: {
    loggedIn() {
      return localStorage.getItem('isLoggedIn') === 'true';
    },
    isAdmin() {
      return Number(localStorage.getItem('userRole') || '0') === 3;
    },
  },
  methods: {
    isActive(path) {
      if (path === '/') return this.$route.path === '/';
      return this.$route.path === path || this.$route.path.startsWith(path + '/');
    },
    toggleMobileMenu() {
      this.mobileMenuOpen = !this.mobileMenuOpen;
    },
    closeMobileMenu() {
      this.mobileMenuOpen = false;
    },
    logout() {
      localStorage.removeItem('isLoggedIn');
      localStorage.removeItem('userId');
      localStorage.removeItem('username');
      localStorage.removeItem('nickname');
      localStorage.removeItem('avatar');
      localStorage.removeItem('userRole');
      localStorage.removeItem('accountUserId');
      this.closeMobileMenu();
      this.$router.push('/login');
    },
  },
  watch: {
    $route() {
      this.closeMobileMenu();
    },
  },
};
</script>

<style scoped>
.site-header {
  background: var(--header-gradient);
  position: sticky;
  top: 0;
  z-index: 1000;
  box-shadow: var(--header-shadow);
}

.header-inner {
  max-width: var(--header-inner-max-width);
  margin: 0 auto;
  padding: 0 var(--spacing-lg);
  height: 56px;
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  min-width: 0;
}

.mobile-menu-btn {
  display: none;
  width: 40px;
  height: 40px;
  background: rgba(255, 255, 255, 0.2);
  border: none;
  border-radius: var(--radius-sm);
  cursor: pointer;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.hamburger {
  display: block;
  width: 20px;
  height: 2px;
  background: white;
  position: relative;
  transition: all 0.3s;
}

.hamburger::before,
.hamburger::after {
  content: '';
  position: absolute;
  width: 20px;
  height: 2px;
  background: white;
  left: 0;
  transition: all 0.3s;
}

.hamburger::before {
  top: -6px;
}

.hamburger::after {
  top: 6px;
}

.hamburger.active {
  background: transparent;
}

.hamburger.active::before {
  top: 0;
  transform: rotate(45deg);
}

.hamburger.active::after {
  top: 0;
  transform: rotate(-45deg);
}

.logo {
  font-size: 20px;
  font-weight: 700;
  color: var(--header-text-on-gradient);
  text-decoration: none;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.15);
  flex-shrink: 0;
}

.nav {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

.nav-link {
  color: rgba(255, 255, 255, 0.88);
  text-decoration: none;
  font-size: 14px;
  padding: 6px 10px;
  border-radius: 6px;
  white-space: nowrap;
  transition: background 0.2s;
}

.nav-link:hover,
.nav-link.active {
  color: var(--header-text-on-gradient);
  background: rgba(255, 255, 255, 0.2);
}

.nav-link.subtle {
  font-size: 13px;
  padding: 6px 8px;
}

.right {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
  justify-content: flex-end;
  min-width: 0;
}

.mobile-auth {
  display: none;
}

.btn-outline {
  padding: 6px 14px;
  border: 1px solid rgba(255, 255, 255, 0.75);
  border-radius: 20px;
  color: var(--header-text-on-gradient);
  font-size: 13px;
  text-decoration: none;
}

.btn-solid {
  padding: 6px 14px;
  background: var(--color-bg-card);
  border-radius: 20px;
  color: var(--color-primary);
  font-size: 13px;
  text-decoration: none;
  font-weight: 500;
}

.btn-text {
  background: none;
  border: none;
  color: rgba(255, 255, 255, 0.9);
  font-size: 13px;
  cursor: pointer;
  padding: 6px 8px;
}

.mobile-menu-overlay {
  display: none;
}

@media (max-width: 1100px) {
  .nav-link.subtle.desktop-only {
    display: none;
  }
}

@media (max-width: 768px) {
  .mobile-menu-btn {
    display: flex;
  }

  .desktop-only {
    display: none !important;
  }

  .nav {
    position: fixed;
    top: 56px;
    left: 0;
    right: 0;
    background: var(--header-gradient);
    flex-direction: column;
    padding: 16px;
    gap: 8px;
    transform: translateY(-100%);
    opacity: 0;
    visibility: hidden;
    transition: all 0.3s ease;
    z-index: 999;
  }

  .nav.mobile-open {
    transform: translateY(0);
    opacity: 1;
    visibility: visible;
  }

  .mobile-menu-overlay {
    display: block;
    position: fixed;
    top: 56px;
    left: 0;
    right: 0;
    bottom: 0;
    background: var(--header-overlay-scrim);
    z-index: 998;
  }

  .mobile-auth {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-left: auto;
  }

  .mobile-auth .nav-link {
    color: rgba(255, 255, 255, 0.88);
    font-size: 14px;
    padding: 6px 10px;
  }

  .right {
    display: none;
  }
}
</style>
