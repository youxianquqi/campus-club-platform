<template>
  <div class="dash">
    <h1 class="title">工作台</h1>
    <p class="sub">平台管理员可在此处理新社团资质与活动发布审核。</p>
    <div class="cards">
      <router-link to="/admin/clubs/pending" class="card">
        <span class="card-num">{{ pendingClubs }}</span>
        <span class="card-label">待审核社团</span>
        <span class="card-hint">点击进入处理</span>
      </router-link>
      <router-link to="/admin/activities/pending" class="card">
        <span class="card-num">{{ pendingActivities }}</span>
        <span class="card-label">待审核活动</span>
        <span class="card-hint">点击进入处理</span>
      </router-link>
    </div>
    <p v-if="loadErr" class="err">{{ loadErr }}</p>
  </div>
</template>

<script>
import { adminApi } from '../../utils/api.js';

export default {
  name: 'AdminDashboard',
  data() {
    return {
      pendingClubs: 0,
      pendingActivities: 0,
      loadErr: '',
    };
  },
  async mounted() {
    try {
      const [clubsRes, actRes] = await Promise.all([
        adminApi.pendingClubs(1, 1),
        adminApi.pendingActivities(1, 1),
      ]);
      this.pendingClubs = clubsRes.data?.total ?? 0;
      this.pendingActivities = actRes.data?.total ?? 0;
    } catch (e) {
      this.loadErr = e.message || '无法加载统计';
    }
  },
};
</script>

<style scoped>
.title {
  font-size: var(--font-size-xl);
  font-weight: 600;
  color: var(--color-text-primary);
  margin-bottom: 8px;
}

.sub {
  color: var(--color-text-secondary);
  font-size: var(--font-size-base);
  margin-bottom: 28px;
}

.cards {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}

.card {
  display: flex;
  flex-direction: column;
  width: 220px;
  padding: var(--spacing-xl);
  background: var(--color-bg-card);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
  text-decoration: none;
  color: inherit;
  transition:
    box-shadow var(--transition-normal),
    transform var(--transition-normal);
}

.card:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}

.card-num {
  font-size: 36px;
  font-weight: 700;
  color: var(--color-primary);
  line-height: 1;
}

.card-label {
  margin-top: var(--spacing-md);
  font-size: 15px;
  font-weight: 500;
  color: var(--color-text-primary);
}

.card-hint {
  margin-top: 8px;
  font-size: var(--font-size-xs);
  color: var(--color-text-muted);
}

.err {
  margin-top: var(--spacing-lg);
  color: var(--color-error);
  font-size: var(--font-size-base);
}
</style>
