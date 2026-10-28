<template>
  <div class="page">
    <SiteHeader />
    <main class="main" v-if="post">
      <div class="detail-card">
        <div class="card-header">
          <h1 class="card-title">{{ post.title || '招募' }}</h1>
          <div class="card-tags">
            <span class="recruit-tag" :class="'recruit-tag-' + getTypeClass(post.type)">{{
              getTypeLabel(post.type)
            }}</span>
            <span
              class="recruit-tag"
              :class="post.status === 1 ? 'recruit-tag-active' : 'recruit-tag-closed'"
              >{{ getStatusLabel(post.status) }}</span
            >
          </div>
        </div>

        <div class="card-content">
          <p>{{ post.content }}</p>
        </div>

        <div v-if="post.tags" class="card-tags-section">
          <span class="tag" v-for="tag in post.tags.split(',')" :key="tag">{{ tag.trim() }}</span>
        </div>

        <div class="card-footer">
          <div class="author">
            <span class="author-name">{{ post.nickname || '匿名' }}</span>
            <span class="post-time">{{ formatTime(post.createdAt) }}</span>
          </div>

          <button
            v-if="accountUid != null && post.userId !== accountUid && post.status === 1"
            class="join-btn"
            @click="showApplyModal = true"
          >
            加入团队
          </button>

          <button
            v-if="accountUid != null && post.userId === accountUid"
            class="manage-btn"
            @click="showManage = !showManage"
          >
            {{ showManage ? '收起' : '管理' }}
          </button>
        </div>

        <div
          v-if="showManage && accountUid != null && post.userId === accountUid"
          class="manage-section"
        >
          <div class="section-title">申请列表 ({{ applications.length }})</div>
          <div v-if="applicationsLoading" class="loading">加载中...</div>
          <div v-else-if="!applications.length" class="empty">暂无申请</div>
          <div v-else class="app-list">
            <div v-for="app in applications" :key="app.id" class="app-item">
              <div class="app-info">
                <span class="app-name">{{ app.nickname }}</span>
                <span class="app-status" :class="'status-' + app.status">{{ app.statusText }}</span>
              </div>
              <div v-if="app.status === 0" class="app-actions">
                <button class="approve-btn" @click="review(app.id, 1)">通过</button>
                <button class="reject-btn" @click="review(app.id, 2)">拒绝</button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <button class="back-btn" @click="$router.push('/recruitment')">← 返回</button>
    </main>

    <div v-if="showApplyModal" class="modal" @click.self="showApplyModal = false">
      <div class="modal-content">
        <h3>申请加入</h3>
        <textarea
          v-model="applyMsg"
          class="apply-textarea"
          rows="3"
          placeholder="简单介绍一下自己（选填）..."
        ></textarea>
        <div class="modal-actions">
          <button class="cancel-btn" @click="showApplyModal = false">取消</button>
          <button class="submit-btn" :disabled="submitting" @click="submitApply">提交</button>
        </div>
      </div>
    </div>

    <div v-if="err" class="error">{{ err }}</div>
    <div v-if="!post && !err" class="loading">加载中...</div>
  </div>
</template>

<script>
import SiteHeader from '../components/SiteHeader.vue';
import { getCurrentAccountUserId, recruitmentPostApi } from '../utils/api.js';

const TYPE_MAP = { 1: 'tech', 2: 'ops', 3: 'design' };
const TYPE_LABELS = { 1: '技术', 2: '运营', 3: '设计', 4: '其他' };
const STATUS_LABELS = { 1: '进行中', 2: '已截止' };

export default {
  name: 'RecruitmentDetail',
  components: { SiteHeader },
  data() {
    return {
      post: null,
      accountUid: null,
      err: '',
      showApplyModal: false,
      applyMsg: '',
      submitting: false,
      showManage: false,
      applications: [],
      applicationsLoading: false,
    };
  },
  methods: {
    getTypeClass(type) {
      return TYPE_MAP[type] || 'other';
    },
    getTypeLabel(type) {
      return TYPE_LABELS[type] || '其他';
    },
    getStatusLabel(status) {
      return STATUS_LABELS[status] || '进行中';
    },
    formatTime(time) {
      if (!time) return '';
      const d = new Date(time);
      return `${d.getMonth() + 1}/${d.getDate()} ${d.getHours()}:${String(d.getMinutes()).padStart(2, '0')}`;
    },
    async load() {
      const id = this.$route.params.id;
      if (!id) {
        this.err = '缺少ID';
        return;
      }
      try {
        const res = await recruitmentPostApi.getById(id);
        this.post = res.data || res;
      } catch (e) {
        this.err = e.message || '加载失败';
      }
    },
    async loadApps() {
      if (!this.post) return;
      this.applicationsLoading = true;
      try {
        const list = await recruitmentPostApi.getApplications(this.post.id);
        this.applications = Array.isArray(list) ? list : [];
      } catch (e) {
        console.warn(e);
      } finally {
        this.applicationsLoading = false;
      }
    },
    async submitApply() {
      this.submitting = true;
      try {
        await recruitmentPostApi.apply(this.post.id, this.applyMsg);
        this.showApplyModal = false;
        this.applyMsg = '';
        alert('申请已提交');
      } catch (e) {
        alert(e.message || '申请失败');
      } finally {
        this.submitting = false;
      }
    },
    async review(appId, action) {
      try {
        await recruitmentPostApi.reviewApplication(this.post.id, appId, action);
        this.loadApps();
      } catch (e) {
        alert(e.message || '操作失败');
      }
    },
  },
  watch: {
    showManage(val) {
      if (val) this.loadApps();
    },
  },
  mounted() {
    this.accountUid = getCurrentAccountUserId();
    this.load();
  },
};
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: var(--color-bg-muted);
}

.main {
  max-width: 800px;
  margin: 0 auto;
  padding: var(--spacing-xl);
}

.detail-card {
  background: var(--color-bg-card);
  border-radius: var(--radius-lg);
  padding: 28px;
  margin-bottom: 20px;
  box-shadow: var(--shadow-card);
}

.card-header {
  margin-bottom: 20px;
}

.card-title {
  font-size: 22px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin: 0 0 12px;
}

.card-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.card-content {
  margin-bottom: 20px;
}

.card-content p {
  font-size: 15px;
  line-height: 1.8;
  color: var(--color-text-body);
  margin: 0;
  white-space: pre-wrap;
}

.card-tags-section {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 20px;
  padding: var(--spacing-lg);
  background: rgba(91, 127, 201, 0.08);
  border-radius: var(--radius-md);
}

.tag {
  padding: 4px 10px;
  background: rgba(0, 161, 214, 0.12);
  color: var(--color-secondary);
  border-radius: 12px;
  font-size: var(--font-size-xs);
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: var(--spacing-lg);
  border-top: 1px solid var(--color-border-light);
}

.author {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.author-name {
  font-size: var(--font-size-base);
  font-weight: 500;
  color: var(--color-text-primary);
}

.post-time {
  font-size: var(--font-size-xs);
  color: var(--color-text-muted);
}

.join-btn {
  padding: 10px 24px;
  border: none;
  border-radius: 20px;
  background: var(--color-primary);
  color: var(--header-text-on-gradient);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
}

.manage-btn {
  padding: 8px 16px;
  border: 1px solid var(--color-border);
  border-radius: 16px;
  background: var(--color-bg-card);
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
  cursor: pointer;
}

.manage-section {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid var(--color-border-light);
}

.section-title {
  font-size: var(--font-size-base);
  font-weight: 600;
  color: var(--color-text-primary);
  margin-bottom: 12px;
}

.loading,
.empty {
  text-align: center;
  padding: 20px;
  color: var(--color-text-muted);
  font-size: var(--font-size-sm);
}

.app-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.app-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  background: rgba(91, 127, 201, 0.08);
  border-radius: var(--radius-sm);
}

.app-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.app-name {
  font-size: var(--font-size-base);
  font-weight: 500;
  color: var(--color-text-primary);
}

.app-status {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 10px;
}

.app-status.status-0 {
  background: rgba(250, 173, 20, 0.15);
  color: var(--color-warning);
}
.app-status.status-1 {
  background: rgba(82, 196, 26, 0.12);
  color: var(--color-success);
}
.app-status.status-2 {
  background: rgba(255, 77, 79, 0.12);
  color: var(--color-error);
}

.app-actions {
  display: flex;
  gap: 8px;
}

.approve-btn,
.reject-btn {
  padding: 6px 12px;
  border: none;
  border-radius: 6px;
  font-size: 12px;
  cursor: pointer;
}

.approve-btn {
  background: var(--color-success);
  color: var(--header-text-on-gradient);
}

.reject-btn {
  background: var(--color-error);
  color: var(--header-text-on-gradient);
}

.back-btn {
  padding: 10px 20px;
  border: 1px solid var(--color-border);
  border-radius: 20px;
  background: var(--color-bg-card);
  font-size: var(--font-size-base);
  color: var(--color-text-secondary);
  cursor: pointer;
}

.modal {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
}

.modal-content {
  background: var(--color-bg-card);
  border-radius: var(--radius-lg);
  padding: var(--spacing-xl);
  width: 90%;
  max-width: 400px;
}

.modal-content h3 {
  margin: 0 0 16px;
  font-size: var(--font-size-xl);
  color: var(--color-text-primary);
}

.apply-textarea {
  width: 100%;
  padding: var(--spacing-md);
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-sm);
  font-size: 14px;
  resize: vertical;
  box-sizing: border-box;
}

.modal-actions {
  display: flex;
  gap: 10px;
  margin-top: 16px;
  justify-content: flex-end;
}

.cancel-btn,
.submit-btn {
  padding: 8px 20px;
  border-radius: 16px;
  font-size: 14px;
  cursor: pointer;
}

.cancel-btn {
  border: 1px solid var(--color-border);
  background: var(--color-bg-card);
  color: var(--color-text-secondary);
}

.submit-btn {
  border: none;
  background: var(--color-primary);
  color: var(--header-text-on-gradient);
}

.submit-btn:disabled {
  opacity: 0.6;
}

.error,
.loading {
  text-align: center;
  padding: 60px;
  color: var(--color-text-muted);
  font-size: var(--font-size-base);
}

@media (max-width: 768px) {
  .main {
    padding: 16px;
  }
  .detail-card {
    padding: 20px;
  }
  .card-title {
    font-size: 18px;
  }
}
</style>
