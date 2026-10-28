<template>
  <div class="page-container">
    <SiteHeader />

    <!-- 主内容区 -->
    <main class="main-content">
      <div class="apply-container">
        <!-- 社团信息卡片 -->
        <div class="club-info-card">
          <div class="club-banner">
            <img :src="club.cover || PL" alt="社团封面" @error="onImgError" />
            <div class="club-overlay">
              <img :src="club.logo || PL" class="club-logo" @error="onImgError" />
              <div class="club-basic">
                <h2>{{ club.name }}</h2>
                <div class="club-meta">
                  <span class="meta-item">
                    <svg viewBox="0 0 24 24" width="16" height="16">
                      <path
                        fill="currentColor"
                        d="M16 11c1.66 0 2.99-1.34 2.99-3S17.66 5 16 5c-1.66 0-3 1.34-3 3s1.34 3 3 3zm-8 0c1.66 0 2.99-1.34 2.99-3S9.66 5 8 5C6.34 5 5 6.34 5 8s1.34 3 3 3zm0 2c-2.33 0-7 1.17-7 3.5V19h14v-2.5c0-2.33-4.67-3.5-7-3.5zm8 0c-.29 0-.62.02-.97.05 1.16.84 1.97 1.97 1.97 3.45V19h6v-2.5c0-2.33-4.67-3.5-7-3.5z"
                      />
                    </svg>
                    {{ club.memberCount }} 成员
                  </span>
                  <span class="meta-item">
                    <svg viewBox="0 0 24 24" width="16" height="16">
                      <path
                        fill="currentColor"
                        d="M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z"
                      />
                    </svg>
                    {{ club.rating }} 评分
                  </span>
                  <span class="meta-item">{{ club.typeName || '社团' }}</span>
                </div>
              </div>
            </div>
          </div>
          <div class="club-desc">
            <p>{{ club.description }}</p>
          </div>
          <div class="club-actions-bar">
            <button class="contact-btn" @click="contactClub">
              <svg viewBox="0 0 24 24" width="16" height="16">
                <path
                  fill="currentColor"
                  d="M20 2H4c-1.1 0-1.99.9-1.99 2L2 22l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zM6 9h12v2H6V9zm8 5H6v-2h8v2zm4-6H6V6h12v2z"
                />
              </svg>
              联系社团
            </button>
          </div>
        </div>

        <!-- 申请表单 -->
        <div class="apply-form-card">
          <div class="form-header">
            <h3>📝 加入社团申请</h3>
            <p>请认真填写以下信息，我们将尽快审核您的申请</p>
          </div>

          <form class="apply-form" @submit.prevent="submitApply">
            <div class="form-group">
              <label class="form-label required">姓名</label>
              <input
                type="text"
                v-model="form.name"
                placeholder="请输入您的真实姓名"
                class="form-input"
              />
            </div>

            <div class="form-row">
              <div class="form-group half">
                <label class="form-label required">学号</label>
                <input
                  type="text"
                  v-model="form.studentId"
                  placeholder="请输入学号"
                  class="form-input"
                />
              </div>
              <div class="form-group half">
                <label class="form-label required">年级</label>
                <select v-model="form.grade" class="form-select">
                  <option value="">请选择年级</option>
                  <option value="1">大一</option>
                  <option value="2">大二</option>
                  <option value="3">大三</option>
                  <option value="4">大四</option>
                  <option value="graduate">研究生</option>
                </select>
              </div>
            </div>

            <div class="form-row">
              <div class="form-group half">
                <label class="form-label required">学院</label>
                <input
                  type="text"
                  v-model="form.college"
                  placeholder="请输入学院"
                  class="form-input"
                />
              </div>
              <div class="form-group half">
                <label class="form-label required">专业</label>
                <input
                  type="text"
                  v-model="form.major"
                  placeholder="请输入专业"
                  class="form-input"
                />
              </div>
            </div>

            <div class="form-group">
              <label class="form-label required">联系电话</label>
              <input
                type="tel"
                v-model="form.phone"
                placeholder="请输入联系电话"
                class="form-input"
              />
            </div>

            <div class="form-group">
              <label class="form-label required">自我介绍</label>
              <textarea
                v-model="form.intro"
                placeholder="请简单介绍自己，包括兴趣爱好、特长等（200字以内）"
                class="form-textarea"
                maxlength="200"
                rows="4"
              ></textarea>
              <span class="form-hint">{{ form.intro.length }}/200</span>
            </div>

            <div class="form-group">
              <label class="form-label">申请理由</label>
              <textarea
                v-model="form.reason"
                placeholder="请说明您想加入本社团的原因（选填）"
                class="form-textarea"
                maxlength="300"
                rows="3"
              ></textarea>
              <span class="form-hint">{{ form.reason.length }}/300</span>
            </div>

            <div class="form-group">
              <label class="form-label">相关经历</label>
              <textarea
                v-model="form.experience"
                placeholder="如有相关社团或活动经历，请简要说明（选填）"
                class="form-textarea"
                maxlength="300"
                rows="3"
              ></textarea>
              <span class="form-hint">{{ form.experience.length }}/300</span>
            </div>

            <div class="form-actions">
              <button type="button" class="btn-secondary" @click="goBack">返回</button>
              <button type="submit" class="btn-primary" :disabled="!isFormValid || submitting">
                {{ submitting ? '提交中...' : '提交申请' }}
              </button>
            </div>
          </form>
        </div>

        <!-- 申请须知 -->
        <div class="notice-card">
          <h4>📋 申请须知</h4>
          <ul>
            <li>请如实填写个人信息，我们将对您的信息严格保密</li>
            <li>申请提交后，社团负责人将在3个工作日内进行审核</li>
            <li>审核结果将通过系统通知和短信方式告知您</li>
            <li>如有疑问，请联系社团负责人或管理员</li>
          </ul>
        </div>
      </div>
    </main>
  </div>
</template>

<script>
import SiteHeader from '../components/SiteHeader.vue';
import {
  clubApi,
  clubTypeApi,
  clubApplyApi,
  getCurrentUserId,
  IMG_PLACEHOLDER,
  onImgError,
} from '../utils/api.js';

export default {
  name: 'ClubApply',
  components: { SiteHeader },
  data() {
    return {
      PL: IMG_PLACEHOLDER,
      club: {
        id: null,
        name: '',
        logo: '',
        cover: '',
        typeName: '',
        memberCount: 0,
        rating: null,
        description: '',
      },
      form: {
        name: '',
        studentId: '',
        grade: '',
        college: '',
        major: '',
        phone: '',
        intro: '',
        reason: '',
        experience: '',
      },
      submitting: false,
    };
  },
  computed: {
    isFormValid() {
      return (
        this.form.name &&
        this.form.studentId &&
        this.form.grade &&
        this.form.college &&
        this.form.major &&
        this.form.phone &&
        this.form.intro
      );
    },
  },
  methods: {
    onImgError,
    mapGrade(v) {
      if (v === 'graduate') return 5;
      const n = Number(v);
      return Number.isNaN(n) ? 1 : n;
    },
    async loadClub() {
      const id = this.$route.params.id;
      if (!id) {
        this.$router.replace('/clubs');
        return;
      }
      try {
        const [cRes, tRes] = await Promise.all([clubApi.getById(id), clubTypeApi.list()]);
        const c = cRes.data;
        const types = tRes.data || [];
        const tn = types.find((t) => t.id === c.typeId);
        this.club = {
          id: c.id,
          leaderId: c.leaderId,
          name: c.name,
          logo: c.logo || PL,
          cover: c.cover || PL,
          typeName: tn?.name || '',
          memberCount: c.memberCount,
          rating: c.rating,
          description: c.description || '',
        };
      } catch (e) {
        alert(e.message || '加载社团失败');
        this.$router.push('/clubs');
      }
    },
    async submitApply() {
      if (!this.isFormValid) return;
      const uid = getCurrentUserId();
      if (!uid) {
        alert('请先登录');
        this.$router.push('/login');
        return;
      }
      this.submitting = true;
      try {
        await clubApplyApi.apply({
          clubId: this.club.id,
          userId: uid,
          name: this.form.name,
          studentId: this.form.studentId,
          grade: this.mapGrade(this.form.grade),
          college: this.form.college,
          major: this.form.major,
          phone: this.form.phone,
          intro: this.form.intro,
          reason: this.form.reason || null,
          experience: this.form.experience || null,
        });
        alert('申请提交成功！请等待审核结果。');
        this.$router.push('/profile');
      } catch (error) {
        alert(error.message || '提交失败，请稍后重试');
      } finally {
        this.submitting = false;
      }
    },
    goBack() {
      this.$router.back();
    },
    contactClub() {
      const uid = getCurrentUserId();
      if (!uid) {
        alert('请先登录');
        this.$router.push('/login');
        return;
      }
      this.$router.push({ path: '/chat', query: { peerClub: String(this.club.id) } });
    },
  },
  mounted() {
    this.loadClub();
  },
  watch: {
    '$route.params.id'() {
      this.loadClub();
    },
  },
};
</script>

<style scoped>
.page-container {
  min-height: 100vh;
  background-color: var(--color-bg-muted);
  font-family:
    -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

/* 顶部导航栏 */
.header {
  background: var(--header-gradient);
  position: sticky;
  top: 0;
  z-index: 1000;
  box-shadow: var(--header-shadow);
}

.header-content {
  max-width: var(--header-inner-max-width);
  margin: 0 auto;
  padding: 0 20px;
  height: 60px;
  display: flex;
  align-items: center;
  gap: 30px;
}

.logo {
  font-size: 24px;
  font-weight: bold;
  color: white;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.nav-links {
  display: flex;
  gap: 24px;
}

.nav-item {
  color: rgba(255, 255, 255, 0.85);
  text-decoration: none;
  font-size: 14px;
  padding: 8px 12px;
  border-radius: 6px;
  transition: all 0.3s;
}

.nav-item:hover,
.nav-item.active {
  color: white;
  background: rgba(255, 255, 255, 0.2);
}

.user-section {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 12px;
}

.search-box {
  width: 240px;
  display: flex;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 20px;
  overflow: hidden;
}

.search-input {
  flex: 1;
  padding: 8px 16px;
  border: none;
  background: transparent;
  font-size: 14px;
  outline: none;
}

.search-btn {
  padding: 8px 16px;
  background: transparent;
  border: none;
  cursor: pointer;
  color: var(--color-primary);
}

.user-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  overflow: hidden;
  border: 2px solid rgba(255, 255, 255, 0.5);
  cursor: pointer;
}

.user-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 主内容区 */
.main-content {
  max-width: 900px;
  margin: 0 auto;
  padding: 30px 20px;
}

.apply-container {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* 社团信息卡片 */
.club-info-card {
  background: var(--color-bg-card);
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: var(--shadow-md);
}

.club-banner {
  position: relative;
  height: 180px;
}

.club-banner img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.club-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 20px 24px;
  background: linear-gradient(transparent, rgba(0, 0, 0, 0.7));
  display: flex;
  align-items: flex-end;
  gap: 20px;
}

.club-logo {
  width: 80px;
  height: 80px;
  border-radius: 12px;
  border: 3px solid white;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.club-basic {
  flex: 1;
  color: white;
}

.club-basic h2 {
  font-size: 24px;
  margin-bottom: 8px;
}

.club-meta {
  display: flex;
  gap: 20px;
  font-size: 14px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  opacity: 0.9;
}

.club-desc {
  padding: 20px 24px;
  color: var(--color-text-secondary);
  line-height: 1.8;
  font-size: 14px;
}

/* 申请表单卡片 */
.apply-form-card {
  background: var(--color-bg-card);
  border-radius: var(--radius-lg);
  padding: 24px;
  box-shadow: var(--shadow-md);
}

.form-header {
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--color-border-light);
}

.form-header h3 {
  font-size: 20px;
  color: var(--color-text-primary);
  margin-bottom: 8px;
}

.form-header p {
  font-size: 14px;
  color: var(--color-text-muted);
}

.apply-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-row {
  display: flex;
  gap: 20px;
}

.form-group.half {
  flex: 1;
}

.form-label {
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text-primary);
}

.form-label.required::after {
  content: '*';
  color: var(--color-error);
  margin-left: 4px;
}

.form-input,
.form-select,
.form-textarea {
  padding: 12px 16px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  font-size: 14px;
  transition: all 0.3s;
  outline: none;
}

.form-input:focus,
.form-select:focus,
.form-textarea:focus {
  border-color: var(--color-primary);
  box-shadow: var(--focus-ring-primary);
}

.form-textarea {
  resize: vertical;
  min-height: 80px;
}

.form-hint {
  font-size: 12px;
  color: var(--color-text-muted);
  text-align: right;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 16px;
  padding-top: 20px;
  border-top: 1px solid var(--color-border-light);
  margin-top: 10px;
}

.btn-primary {
  padding: 12px 32px;
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-light) 100%);
  color: var(--header-text-on-gradient);
  border: none;
  border-radius: 24px;
  font-size: 15px;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(255, 107, 157, 0.3);
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-secondary {
  padding: 12px 32px;
  background: var(--color-bg-card);
  color: var(--color-text-secondary);
  border: 1px solid var(--color-border);
  border-radius: 24px;
  font-size: 15px;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-secondary:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
}

/* 申请须知 */
.notice-card {
  background: rgba(250, 173, 20, 0.12);
  border-radius: var(--radius-md);
  padding: 20px 24px;
  border: 1px solid rgba(250, 173, 20, 0.35);
}

.notice-card h4 {
  font-size: 15px;
  color: var(--color-warning);
  margin-bottom: 12px;
}

.notice-card ul {
  list-style: none;
}

.notice-card li {
  padding: 6px 0;
  font-size: 13px;
  color: var(--color-text-secondary);
  position: relative;
  padding-left: 16px;
}

.notice-card li::before {
  content: '•';
  position: absolute;
  left: 0;
  color: var(--color-warning);
}

/* 响应式 */
@media (max-width: 768px) {
  .nav-links {
    display: none;
  }

  .search-box {
    width: 160px;
  }

  .form-row {
    flex-direction: column;
    gap: 20px;
  }

  .club-banner {
    height: 140px;
  }

  .club-logo {
    width: 60px;
    height: 60px;
  }

  .club-basic h2 {
    font-size: 18px;
  }

  .club-meta {
    flex-wrap: wrap;
    gap: 12px;
    font-size: 12px;
  }
}
</style>
