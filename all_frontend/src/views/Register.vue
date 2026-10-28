<template>
  <div class="register-page">
    <div class="register-container">
      <div class="register-header">
        <h1>社团之家</h1>
        <p>发现你的社团，结交志同道合的朋友</p>
      </div>

      <div class="register-form">
        <h2>注册</h2>

        <div class="form-group">
          <input type="text" v-model="form.username" placeholder="设置用户名" class="form-input" />
        </div>

        <div class="form-group">
          <input type="email" v-model="form.email" placeholder="邮箱地址" class="form-input" />
        </div>

        <div class="form-group">
          <input type="tel" v-model="form.phone" placeholder="手机号码" class="form-input" />
        </div>

        <div class="form-group">
          <input
            :type="showPassword ? 'text' : 'password'"
            v-model="form.password"
            placeholder="设置密码（至少6位）"
            class="form-input"
          />
          <button class="toggle-password" @click="showPassword = !showPassword">
            {{ showPassword ? '隐藏' : '显示' }}
          </button>
        </div>

        <div class="form-group">
          <input
            :type="showPassword ? 'text' : 'password'"
            v-model="form.confirmPassword"
            placeholder="确认密码"
            class="form-input"
          />
        </div>

        <div class="form-options">
          <label class="checkbox-label">
            <input type="checkbox" v-model="form.agree" />
            <span
              >我已阅读并同意 <a href="#" class="agree-link">《用户协议》</a> 和
              <a href="#" class="agree-link">《隐私政策》</a></span
            >
          </label>
        </div>

        <button class="register-btn" @click="handleRegister" :disabled="!canRegister || loading">
          {{ loading ? '注册中...' : '注册' }}
        </button>

        <p class="login-tip">
          已有账号？
          <router-link to="/login" class="login-link">立即登录</router-link>
        </p>
      </div>
    </div>

    <div class="register-bg">
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
import { userApi } from '../utils/api.js';

export default {
  name: 'Register',
  data() {
    return {
      form: {
        username: '',
        email: '',
        phone: '',
        password: '',
        confirmPassword: '',
        agree: false,
      },
      showPassword: false,
      loading: false,
    };
  },
  computed: {
    canRegister() {
      return (
        this.form.username &&
        this.form.email &&
        this.form.phone &&
        this.form.password &&
        this.form.confirmPassword &&
        this.form.password === this.form.confirmPassword &&
        this.form.agree &&
        this.form.password.length >= 6
      );
    },
  },
  methods: {
    async handleRegister() {
      if (!this.canRegister) {
        if (!this.form.agree) {
          alert('请阅读并同意用户协议和隐私政策');
          return;
        }
        if (this.form.password !== this.form.confirmPassword) {
          alert('两次输入的密码不一致');
          return;
        }
        if (this.form.password.length < 6) {
          alert('密码长度至少6位');
          return;
        }
        return;
      }

      this.loading = true;
      try {
        await userApi.register({
          username: this.form.username,
          password: this.form.password,
          email: this.form.email,
          phone: this.form.phone,
        });
        alert('注册成功！请登录');
        this.$router.push('/login');
      } catch (error) {
        alert(error.message || '注册失败，用户名或邮箱可能已存在');
      } finally {
        this.loading = false;
      }
    },
  },
};
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  display: flex;
  background: var(--color-bg-muted);
}

.register-container {
  width: 50%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 60px;
}

.register-header {
  text-align: center;
  margin-bottom: 40px;
}

.register-header h1 {
  font-size: 32px;
  background: var(--header-gradient);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 12px;
}

.register-header p {
  font-size: var(--font-size-lg);
  color: var(--color-text-muted);
}

.register-form {
  background: var(--color-bg-card);
  border-radius: var(--radius-lg);
  padding: 40px;
  box-shadow: var(--shadow-md);
}

.register-form h2 {
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
  margin-bottom: 24px;
}

.checkbox-label {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
  cursor: pointer;
}

.checkbox-label input {
  width: 16px;
  height: 16px;
  accent-color: var(--color-primary);
  margin-top: 2px;
}

.agree-link {
  color: var(--color-primary);
  text-decoration: none;
}

.register-btn {
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

.register-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(255, 107, 157, 0.3);
}

.register-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.login-tip {
  text-align: center;
  margin-top: 24px;
  font-size: var(--font-size-base);
  color: var(--color-text-secondary);
}

.login-link {
  color: var(--color-primary);
  text-decoration: none;
  font-weight: 500;
}

.register-bg {
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
  .register-page {
    flex-direction: column;
  }

  .register-container,
  .register-bg {
    width: 100%;
  }

  .register-bg {
    display: none;
  }
}
</style>
