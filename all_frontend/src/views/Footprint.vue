<template>
  <div class="page">
    <SiteHeader />
    <main class="main">
      <h1>我的足迹</h1>
      <p class="intro">记录你参与过的活动</p>

      <div class="filters">
        <button
          v-for="f in filters"
          :key="f.value"
          type="button"
          class="filter-btn"
          :class="{ active: filterType === f.value }"
          @click="setFilter(f.value)"
        >
          {{ f.label }}
        </button>
      </div>

      <p v-if="loading" class="muted">加载中…</p>
      <p v-else-if="error" class="muted warn-text">{{ error }}</p>
      <div v-else-if="!footprints.length" class="empty">
        <p>暂无足迹记录</p>
        <p class="hint">去参加活动吧！</p>
      </div>
      <div v-else class="footprint-list">
        <div
          v-for="fp in footprints"
          :key="fp.id"
          class="footprint-card"
          @click="goToActivity(fp.activityId)"
        >
          <div class="fp-icon" :class="'type-' + fp.actionType">
            <svg viewBox="0 0 24 24" width="24" height="24">
              <path
                v-if="fp.actionType === 1"
                fill="currentColor"
                d="M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z"
              />
              <path
                v-else-if="fp.actionType === 2"
                fill="currentColor"
                d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-2 10h-4v4h-2v-4H7v-2h4V7h2v4h4v2z"
              />
              <path
                v-else-if="fp.actionType === 3"
                fill="currentColor"
                d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z"
              />
              <path
                v-else
                fill="currentColor"
                d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"
              />
            </svg>
          </div>
          <div class="fp-info">
            <h3 class="fp-title">{{ fp.activityTitle || '活动 #' + fp.activityId }}</h3>
            <p class="fp-meta">
              <span class="action-tag" :class="'type-' + fp.actionType">{{ fp.actionName }}</span>
              <span v-if="fp.activityTime" class="time">{{ formatTime(fp.activityTime) }}</span>
              <span v-if="fp.activityLocation" class="location">{{ fp.activityLocation }}</span>
            </p>
          </div>
          <svg class="arrow" viewBox="0 0 24 24" width="20" height="20">
            <path fill="currentColor" d="M8.59 16.59L13.17 12 8.59 7.41 10 6l6 6-6 6z" />
          </svg>
        </div>
      </div>
    </main>
  </div>
</template>

<script>
import SiteHeader from '../components/SiteHeader.vue';
import { footprintApi, getCurrentUserId } from '../utils/api.js';

const FILTERS = [
  { value: null, label: '全部' },
  { value: 1, label: '浏览' },
  { value: 2, label: '报名' },
  { value: 3, label: '签到' },
  { value: 4, label: '参与' },
  { value: 5, label: '收藏' },
];

export default {
  name: 'Footprint',
  components: { SiteHeader },
  data() {
    return {
      uid: null,
      footprints: [],
      filterType: null,
      loading: true,
      error: '',
      filters: FILTERS,
    };
  },
  mounted() {
    this.uid = getCurrentUserId();
    this.load();
  },
  methods: {
    setFilter(value) {
      this.filterType = value;
      this.load();
    },
    formatTime(iso) {
      if (!iso) return '';
      return new Date(iso).toLocaleString();
    },
    goToActivity(id) {
      this.$router.push(`/activity/${id}`);
    },
    async load() {
      if (!this.uid) {
        this.error = '请先登录';
        this.loading = false;
        return;
      }
      this.loading = true;
      this.error = '';
      try {
        const list = await footprintApi.list(this.filterType);
        this.footprints = Array.isArray(list) ? list : [];
      } catch (e) {
        console.warn(e);
        this.error = e.message || '加载足迹失败';
        this.footprints = [];
      } finally {
        this.loading = false;
      }
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
  max-width: 720px;
  margin: 0 auto;
  padding: 24px 16px 48px;
}
h1 {
  font-size: 22px;
  color: var(--color-text-primary);
  margin-bottom: 4px;
}
.intro {
  font-size: 14px;
  color: var(--color-text-secondary);
  margin-bottom: 20px;
}
.filters {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 20px;
}
.filter-btn {
  padding: 6px 14px;
  border: 1px solid var(--color-border);
  border-radius: 16px;
  background: var(--color-bg-card);
  color: var(--color-text-primary);
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}
.filter-btn.active {
  background: var(--recruit-primary);
  color: var(--header-text-on-gradient);
  border-color: var(--recruit-primary);
}
.muted {
  color: var(--color-text-muted);
  font-size: 14px;
  text-align: center;
  padding: 32px;
}
.warn-text {
  color: var(--recruit-type-design);
}
.empty {
  text-align: center;
  padding: 48px;
  color: var(--color-text-muted);
}
.hint {
  font-size: 13px;
  margin-top: 8px;
}
.footprint-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.footprint-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px;
  background: var(--color-bg-card);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: var(--shadow-card);
}
.footprint-card:hover {
  transform: translateX(4px);
  box-shadow: var(--shadow-card-hover);
}
.fp-icon {
  width: 44px;
  height: 44px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  background: rgba(91, 127, 201, 0.12);
  color: var(--recruit-primary);
}
.fp-icon.type-1 {
  background: rgba(91, 127, 201, 0.12);
  color: var(--recruit-primary);
}
.fp-icon.type-2 {
  background: rgba(230, 162, 60, 0.15);
  color: var(--recruit-type-ops);
}
.fp-icon.type-3 {
  background: rgba(103, 194, 58, 0.12);
  color: var(--recruit-type-tech);
}
.fp-icon.type-4 {
  background: rgba(245, 108, 108, 0.12);
  color: var(--recruit-type-design);
}
.fp-icon.type-5 {
  background: rgba(144, 147, 153, 0.15);
  color: var(--recruit-type-other);
}

.fp-info {
  flex: 1;
  min-width: 0;
}
.fp-title {
  margin: 0 0 6px;
  font-size: 15px;
  font-weight: 500;
  color: var(--color-text-primary);
}
.fp-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  font-size: 12px;
  color: var(--color-text-muted);
  margin: 0;
}
.action-tag {
  padding: 2px 8px;
  border-radius: 8px;
  font-size: 11px;
}
.action-tag.type-1 {
  background: rgba(91, 127, 201, 0.12);
  color: var(--recruit-primary);
}
.action-tag.type-2 {
  background: rgba(230, 162, 60, 0.15);
  color: var(--recruit-type-ops);
}
.action-tag.type-3 {
  background: rgba(103, 194, 58, 0.12);
  color: var(--recruit-type-tech);
}
.action-tag.type-4 {
  background: rgba(245, 108, 108, 0.12);
  color: var(--recruit-type-design);
}
.action-tag.type-5 {
  background: rgba(144, 147, 153, 0.15);
  color: var(--recruit-type-other);
}

.arrow {
  color: var(--color-text-disabled);
  flex-shrink: 0;
}
</style>
