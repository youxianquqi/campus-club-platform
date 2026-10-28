<template>
  <div class="page">
    <SiteHeader />
    <main class="main">
      <div class="page-header">
        <h1>招募广场</h1>
        <button v-if="uid" class="publish-btn" @click="showCompose = !showCompose">
          {{ showCompose ? '收起' : '发布招募' }}
        </button>
      </div>

      <div v-if="uid && showCompose" class="compose-section">
        <input
          v-model.trim="form.title"
          type="text"
          class="compose-title"
          placeholder="招募标题..."
        />
        <textarea
          v-model.trim="form.content"
          class="compose-content"
          rows="3"
          placeholder="详细说明你的需求..."
        ></textarea>
        <div class="compose-row">
          <select v-model="form.type" class="compose-select">
            <option :value="0">选择类型</option>
            <option :value="1">技术岗</option>
            <option :value="2">运营岗</option>
            <option :value="3">设计岗</option>
            <option :value="4">其他</option>
          </select>
          <input
            v-model.trim="form.tags"
            type="text"
            class="compose-tags"
            placeholder="标签（逗号分隔）"
          />
        </div>
        <div class="compose-actions">
          <button class="submit-btn" :disabled="submitting || !form.content" @click="submit">
            发布
          </button>
        </div>
      </div>

      <div class="filter-bar">
        <button
          v-for="t in typeOptions"
          :key="t.value"
          class="filter-btn"
          :class="{ active: filterType === t.value }"
          @click="
            filterType = t.value;
            loadPosts();
          "
        >
          {{ t.label }}
        </button>
      </div>

      <div v-if="listLoading" class="loading">加载中...</div>
      <div v-else-if="!posts.length" class="empty">暂无招募</div>
      <div v-else class="card-grid">
        <div v-for="p in posts" :key="p.id" class="recruit-card" @click="goToDetail(p.id)">
          <div class="card-header">
            <div class="author-avatar">
              <img v-if="p.avatar" :src="p.avatar" :alt="p.nickname || '用户'" />
              <div v-else class="avatar-placeholder">{{ (p.nickname || 'U')[0] }}</div>
            </div>
            <div class="author-info">
              <span class="author-name">{{ p.nickname || '匿名用户' }}</span>
              <span class="post-time">{{ formatTime(p.createdAt) }}</span>
            </div>
          </div>
          <h3 class="card-title">{{ p.title || '招募' }}</h3>
          <div class="card-tags">
            <span
              class="tag"
              v-for="tag in (p.tags || '').split(',').filter((t) => t.trim())"
              :key="tag"
              >{{ tag.trim() }}</span
            >
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script>
import SiteHeader from '../components/SiteHeader.vue';
import { getCurrentUserId, recruitmentPostApi } from '../utils/api.js';

const TYPE_OPTIONS = [
  { value: null, label: '全部' },
  { value: 1, label: '技术' },
  { value: 2, label: '运营' },
  { value: 3, label: '设计' },
];

export default {
  name: 'Recruitment',
  components: { SiteHeader },
  data() {
    return {
      uid: null,
      form: { title: '', content: '', type: 0, tags: '' },
      posts: [],
      listLoading: true,
      submitting: false,
      filterType: null,
      typeOptions: TYPE_OPTIONS,
      showCompose: false,
    };
  },
  mounted() {
    this.uid = getCurrentUserId();
    this.loadPosts();
  },
  methods: {
    async loadPosts() {
      this.listLoading = true;
      try {
        const arr = await recruitmentPostApi.list(this.filterType, null);
        this.posts = Array.isArray(arr) ? arr : [];
      } catch (e) {
        console.warn(e);
        this.posts = [];
      } finally {
        this.listLoading = false;
      }
    },
    async submit() {
      if (!this.form.content || !this.uid) return;
      this.submitting = true;
      try {
        const data = { content: this.form.content };
        if (this.form.title) data.title = this.form.title;
        if (this.form.type > 0) data.type = this.form.type;
        if (this.form.tags) data.tags = this.form.tags;
        await recruitmentPostApi.create(data);
        this.form = { title: '', content: '', type: 0, tags: '' };
        this.showCompose = false;
        await this.loadPosts();
      } catch (e) {
        alert(e.message || '发布失败');
      } finally {
        this.submitting = false;
      }
    },
    goToDetail(id) {
      this.$router.push(`/recruitment/${id}`);
    },
    formatTime(time) {
      if (!time) return '';
      const d = new Date(time);
      const now = new Date();
      const diff = now - d;
      const days = Math.floor(diff / (1000 * 60 * 60 * 24));
      if (days === 0) return '今天';
      if (days === 1) return '昨天';
      if (days < 7) return `${days}天前`;
      return d.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' });
    },
  },
};
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: var(--color-bg-muted);
}

.main {
  max-width: var(--page-max-width);
  margin: 0 auto;
  padding: var(--spacing-xl);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h1 {
  font-size: var(--font-size-xl);
  font-weight: 600;
  color: var(--color-text-primary);
  margin: 0;
}

.publish-btn {
  padding: 8px 20px;
  border: none;
  border-radius: 20px;
  background: var(--color-primary);
  color: var(--header-text-on-gradient);
  font-size: var(--font-size-base);
  font-weight: 500;
  cursor: pointer;
  transition: background var(--transition-fast);
}

.publish-btn:hover {
  background: var(--color-primary-pressed);
}

.compose-section {
  background: var(--color-bg-card);
  border-radius: var(--radius-md);
  padding: 20px;
  margin-bottom: 20px;
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
  box-shadow: var(--shadow-sm);
}

.compose-title {
  width: 100%;
  padding: 12px 16px;
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-sm);
  font-size: var(--font-size-lg);
  box-sizing: border-box;
}

.compose-title:focus {
  outline: none;
  border-color: var(--color-primary);
}

.compose-content {
  width: 100%;
  padding: 12px 16px;
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-sm);
  font-size: var(--font-size-base);
  resize: vertical;
  box-sizing: border-box;
}

.compose-content:focus {
  outline: none;
  border-color: var(--color-primary);
}

.compose-row {
  display: flex;
  gap: var(--spacing-md);
}

.compose-select,
.compose-tags {
  padding: 8px 12px;
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-sm);
  font-size: var(--font-size-base);
}

.compose-select {
  width: 120px;
}

.compose-tags {
  flex: 1;
}

.compose-actions {
  display: flex;
  justify-content: flex-end;
}

.submit-btn {
  padding: 8px 24px;
  border: none;
  border-radius: 20px;
  background: var(--color-primary);
  color: var(--header-text-on-gradient);
  font-size: var(--font-size-base);
  cursor: pointer;
}

.submit-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.filter-bar {
  display: flex;
  gap: 8px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.filter-btn {
  padding: 6px 16px;
  border: 1px solid var(--color-border);
  border-radius: 16px;
  background: var(--color-bg-card);
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
  cursor: pointer;
  transition:
    background var(--transition-fast),
    border-color var(--transition-fast),
    color var(--transition-fast);
}

.filter-btn.active {
  background: var(--color-primary);
  color: var(--header-text-on-gradient);
  border-color: var(--color-primary);
}

.loading,
.empty {
  text-align: center;
  padding: 60px;
  color: var(--color-text-muted);
  font-size: var(--font-size-base);
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.recruit-card {
  background: var(--color-bg-card);
  border-radius: var(--radius-lg);
  padding: 24px;
  cursor: pointer;
  transition:
    border-color var(--transition-fast),
    box-shadow var(--transition-fast),
    transform var(--transition-fast);
  border: 1px solid transparent;
  box-shadow: var(--shadow-card);
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.recruit-card:hover {
  border-color: var(--color-primary);
  box-shadow: 0 8px 24px rgba(255, 107, 157, 0.15);
  transform: translateY(-4px);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 12px;
}

.author-avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.author-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-light) 100%);
  color: white;
  font-size: 18px;
  font-weight: 600;
}

.author-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.author-name {
  font-size: var(--font-size-base);
  font-weight: 600;
  color: var(--color-text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.post-time {
  font-size: var(--font-size-xs);
  color: var(--color-text-muted);
}

.card-title {
  font-size: var(--font-size-xl);
  font-weight: 700;
  color: var(--color-text-primary);
  margin: 0;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: auto;
}

.tag {
  padding: 6px 12px;
  background: rgba(91, 127, 201, 0.12);
  color: var(--recruit-primary);
  border-radius: 12px;
  font-size: var(--font-size-sm);
  font-weight: 500;
}

@media (max-width: 768px) {
  .main {
    padding: 16px;
  }

  .card-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }

  .recruit-card {
    padding: 20px;
  }

  .author-avatar {
    width: 40px;
    height: 40px;
  }

  .card-title {
    font-size: var(--font-size-lg);
  }

  .compose-row {
    flex-direction: column;
  }

  .compose-select {
    width: 100%;
  }
}
</style>
