<template>
  <div class="login-page">
    <div class="login-container">
      <div class="login-header">
        <h1>社团之家</h1>
        <p>发现你的社团，结交志同道合的朋友</p>
      </div>

      <div class="login-form">
        <h2>登录</h2>

        <div class="form-group">
          <input
            type="text"
            v-model="form.username"
            placeholder="用户名 / 学号 / 手机号"
            class="form-input"
          />
        </div>

        <div class="form-group">
          <input
            :type="showPassword ? 'text' : 'password'"
            v-model="form.password"
            placeholder="请输入密码"
            class="form-input"
          />
          <button class="toggle-password" @click="showPassword = !showPassword">
            {{ showPassword ? '隐藏' : '显示' }}
          </button>
        </div>

        <div class="form-options">
          <label class="checkbox-label">
            <input type="checkbox" v-model="form.remember" />
            <span>记住我</span>
          </label>
          <a href="#" class="forgot-link">忘记密码？</a>
        </div>

        <button class="login-btn" @click="handleLogin" :disabled="loading">
          {{ loading ? '登录中...' : '登录' }}
        </button>

        <div class="divider">
          <span>或</span>
        </div>

        <div class="social-login">
          <button class="social-btn qq-btn" @click="handleQQLogin">
            <svg viewBox="0 0 24 24" width="20" height="20">
              <path
                fill="#12B7F5"
                d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm3.5 14.5c-1.5 1-3.5 1.5-5.5 1.5s-4-.5-5.5-1.5c-.5-.5-.5-1.5 0-2 1-1 3-1.5 4.5-1.5h2c1.5 0 3.5.5 4.5 1.5.5.5.5 1.5 0 2z"
              />
              <circle cx="9" cy="10" r="1.5" fill="#fff" />
              <circle cx="15" cy="10" r="1.5" fill="#fff" />
            </svg>
            使用 QQ 登录
          </button>
        </div>

        <p class="register-tip">
          还没有账号？
          <router-link to="/register" class="register-link">立即注册</router-link>
        </p>
      </div>
    </div>

    <div class="login-bg">
      <div class="bg-content">
        <h2>加入社团之家</h2>
        <p>探索校园社团，发现你的热爱</p>
        <ul class="feature-list">
          <li>🎪 发现精彩社团活动</li>
          <li>🤝 结识志同道合的朋友</li>
          <li>📝 分享你的社团经验</li>
          <li>🎉 参与丰富的校园生活</li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script>
import { userApi, userTagApi } from '../utils/api.js';

export default {
  name: 'Login',
  data() {
    return {
      form: {
        username: '',
        password: '',
        remember: false,
      },
      showPassword: false,
      loading: false,
    };
  },
  methods: {
    async handleLogin() {
      if (!this.form.username || !this.form.password) {
        alert('请输入用户名和密码');
        return;
      }

      this.loading = true;
      try {
        const res = await userApi.login(this.form.username, this.form.password);
        const user = res.data;
        if (user?.token) {
          localStorage.setItem('token', user.token);
        }
        localStorage.setItem('isLoggedIn', 'true');
        localStorage.setItem('userId', String(user.userId));
        if (user.accountUserId != null) {
          localStorage.setItem('accountUserId', String(user.accountUserId));
        } else {
          localStorage.removeItem('accountUserId');
        }
        localStorage.setItem('username', user.username || '');
        localStorage.setItem('nickname', user.nickname || '');
        localStorage.setItem('userRole', String(user.role != null ? user.role : 1));
        if (user.avatar) localStorage.setItem('avatar', user.avatar);
        else localStorage.removeItem('avatar');

        const redir = this.$route.query.redirect;
        if (redir && typeof redir === 'string' && redir.startsWith('/')) {
          this.$router.replace(redir);
          return;
        }

        try {
          const tagRes = await userTagApi.hasSelectedTags();
          const hasTags = tagRes.data?.hasTags;
          if (!hasTags) {
            alert('登录成功！请先选择你的兴趣标签');
            this.$router.push('/tag-selection');
          } else {
            alert('登录成功！');
            this.$router.push('/');
          }
        } catch (e) {
          console.warn('检查标签状态失败，跳转到标签选择页', e);
          this.$router.push('/tag-selection');
        }
      } catch (error) {
        alert(error.message || '登录失败，请检查用户名与密码');
      } finally {
        this.loading = false;
      }
    },
    handleQQLogin() {
      // QQ登录需要后端支持，这里先提示用户
      alert('QQ登录功能即将上线，请使用账号密码登录');
      // 实际实现时需要：
      // 1. 跳转到QQ授权页面或弹出QQ登录窗口
      // 2. 后端需要实现QQ登录回调接口
      // 3. 处理QQ登录后的用户信息同步
    },
  },
};
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  background: var(--color-bg-muted);
}

.login-container {
  width: 50%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 60px;
}

.login-header {
  text-align: center;
  margin-bottom: 40px;
}

.login-header h1 {
  font-size: 32px;
  background: var(--header-gradient);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 12px;
}

.login-header p {
  font-size: var(--font-size-lg);
  color: var(--color-text-muted);
}

.login-form {
  background: var(--color-bg-card);
  border-radius: var(--radius-lg);
  padding: 40px;
  box-shadow: var(--shadow-md);
}

.login-form h2 {
  font-size: 24px;
  color: var(--color-text-primary);
  text-align: center;
  margin-bottom: 32px;
}

.form-group {
  position: relative;
  margin-bottom: 20px;
}

.form-input {
  width: 100%;
  padding: 14px 16px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  font-size: 15px;
  outline: none;
  transition:
    border-color var(--transition-fast),
    box-shadow var(--transition-fast);
}

.form-input:focus {
  border-color: var(--color-primary);
  box-shadow: var(--focus-ring-primary);
}

.toggle-password {
  position: absolute;
  right: 16px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  color: var(--color-text-muted);
  font-size: 13px;
  cursor: pointer;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: var(--font-size-base);
  color: var(--color-text-secondary);
  cursor: pointer;
}

.checkbox-label input {
  width: 16px;
  height: 16px;
  accent-color: var(--color-primary);
}

.forgot-link {
  font-size: var(--font-size-base);
  color: var(--color-primary);
  text-decoration: none;
}

.login-btn {
  width: 100%;
  padding: 14px;
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-light) 100%);
  color: var(--header-text-on-gradient);
  border: none;
  border-radius: var(--radius-md);
  font-size: var(--font-size-lg);
  font-weight: 500;
  cursor: pointer;
  transition:
    transform var(--transition-normal),
    box-shadow var(--transition-normal);
}

.login-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(255, 107, 157, 0.3);
}

.login-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.divider {
  display: flex;
  align-items: center;
  margin: 24px 0;
}

.divider::before,
.divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background: var(--color-border);
}

.divider span {
  padding: 0 16px;
  font-size: var(--font-size-sm);
  color: var(--color-text-muted);
}

.social-login {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.social-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 12px;
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  font-size: var(--font-size-base);
  color: var(--color-text-primary);
  cursor: pointer;
  transition: border-color var(--transition-fast);
}

.social-btn:hover {
  border-color: var(--color-primary);
}

.qq-btn {
  background: #12B7F5;
  border-color: #12B7F5;
  color: #fff;
}

.qq-btn:hover {
  background: #0ea5e0;
  border-color: #0ea5e0;
}

.qq-btn svg {
  background: #fff;
  border-radius: 50%;
  padding: 2px;
}

.register-tip {
  text-align: center;
  margin-top: 24px;
  font-size: var(--font-size-base);
  color: var(--color-text-secondary);
}

.register-link {
  color: var(--color-primary);
  text-decoration: none;
  font-weight: 500;
}

.login-bg {
  width: 50%;
  background: var(--header-gradient);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px;
}

.bg-content {
  text-align: center;
  color: white;
}

.bg-content h2 {
  font-size: 36px;
  margin-bottom: 16px;
}

.bg-content > p {
  font-size: 18px;
  opacity: 0.9;
  margin-bottom: 40px;
}

.feature-list {
  list-style: none;
  text-align: left;
}

.feature-list li {
  font-size: 16px;
  padding: 12px 0;
  opacity: 0.9;
}

@media (max-width: 992px) {
  .login-page {
    flex-direction: column;
  }

  .login-container,
  .login-bg {
    width: 100%;
  }

  .login-bg {
    display: none;
  }
}
</style>
