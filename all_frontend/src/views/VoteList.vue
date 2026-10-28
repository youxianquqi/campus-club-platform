<template>
  <div class="page">
    <SiteHeader />
    <main class="main">
      <div class="hero-section">
        <div class="hero-content">
          <h1>社团投票</h1>
          <p class="hero-desc">参与投票，支持你喜欢的选项，共同决策社团事务</p>
          <router-link v-if="canCreate" to="/votes/create" class="hero-btn">
            <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor">
              <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z" />
            </svg>
            创建投票
          </router-link>
        </div>
        <div class="hero-decoration">
          <div class="deco-circle circle-1"></div>
          <div class="deco-circle circle-2"></div>
          <div class="deco-circle circle-3"></div>
        </div>
      </div>

      <div class="filter-section">
        <div class="filter-tabs">
          <button
            v-for="tab in filterTabs"
            :key="tab.value"
            class="filter-tab"
            :class="{ active: currentFilter === tab.value }"
            @click="currentFilter = tab.value"
          >
            {{ tab.label }}
            <span class="tab-count">{{ getCountByStatus(tab.value) }}</span>
          </button>
        </div>
      </div>

      <div class="content-section">
        <p v-if="loading" class="muted">
          <LoadingSpinner size="32px" />
          <span>加载中…</span>
        </p>
        <p v-else-if="error" class="muted error-text">{{ error }}</p>
        <div v-else-if="!filteredVotes.length" class="empty-state">
          <EmptyState
            icon="🗳️"
            title="暂无投票"
            :description="currentFilter === 'all' ? '还没有发布任何投票' : '该状态下暂无投票'"
          />
          <router-link
            v-if="canCreate && currentFilter === 'all'"
            to="/votes/create"
            class="empty-action"
          >
            立即创建第一个投票
          </router-link>
        </div>
        <div v-else class="vote-grid">
          <div v-for="v in filteredVotes" :key="v.id" class="v-card" @click="goToDetail(v.id)">
            <div class="v-header">
              <span class="v-type" :class="v.voteType === 1 ? 'single' : 'multiple'">
                {{ v.voteType === 1 ? '单选' : '多选' }}
              </span>
              <span class="v-status" :class="'status-' + v.status">
                {{ getStatusLabel(v.status) }}
              </span>
              <span v-if="v.allowAnonymous" class="v-anonymous" title="允许匿名投票">
                <svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor">
                  <path
                    d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"
                  />
                </svg>
              </span>
            </div>
            <div class="v-club" v-if="v.clubName">
              <img :src="v.clubLogo || defaultClubLogo" class="club-avatar" alt="" />
              <span class="club-name">{{ v.clubName }}</span>
            </div>
            <h3 class="v-title">{{ v.title }}</h3>
            <p class="v-desc" v-if="v.description">{{ v.description }}</p>
            <div class="v-footer">
              <div class="v-meta">
                <span class="meta-item">
                  <svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor">
                    <path
                      d="M11.99 2C6.47 2 2 6.48 2 12s4.47 10 9.99 10C17.52 22 22 17.52 22 12S17.52 2 11.99 2zM12 20c-4.42 0-8-3.58-8-8s3.58-8 8-8 8 3.58 8 8-3.58 8-8 8zm.5-13H11v6l5.25 3.15.75-1.23-4.5-2.67z"
                    />
                  </svg>
                  <span v-if="v.endTime">{{ formatTime(v.endTime) }} 截止</span>
                  <span v-else>长期有效</span>
                </span>
                <span class="meta-item participant-count" v-if="v.totalVotes">
                  <svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor">
                    <path
                      d="M16 11c1.66 0 2.99-1.34 2.99-3S17.66 5 16 5c-1.66 0-3 1.34-3 3s1.34 3 3 3zm-8 0c1.66 0 2.99-1.34 2.99-3S9.66 5 8 5C6.34 5 5 6.34 5 8s1.34 3 3 3zm0 2c-2.33 0-7 1.17-7 3.5V19h14v-2.5c0-2.33-4.67-3.5-7-3.5zm8 0c-.29 0-.62.02-.97.05 1.16.84 1.97 1.97 1.97 3.45V19h6v-2.5c0-2.33-4.67-3.5-7-3.5z"
                    />
                  </svg>
                  {{ v.totalVotes }} 人参与
                </span>
              </div>
              <div class="v-action">
                <span v-if="isVoted(v.id)" class="voted-badge">已投票</span>
                <svg viewBox="0 0 24 24" width="20" height="20" class="arrow-icon">
                  <path fill="currentColor" d="M10 6L8.59 7.41 13.17 12l-4.58 4.59L10 18l6-6z" />
                </svg>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script>
import SiteHeader from '../components/SiteHeader.vue';
import LoadingSpinner from '../components/LoadingSpinner.vue';
import EmptyState from '../components/EmptyState.vue';
import { voteApi } from '../api/index.js';

export default {
  name: 'VoteList',
  components: { SiteHeader, LoadingSpinner, EmptyState },
  data() {
    return {
      votes: [],
      loading: true,
      error: '',
      currentFilter: 'all',
      votedIds: new Set(),
      defaultClubLogo:
        'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCAxMDAgMTAwIj48Y2lyY2xlIGN4PSI1MCIgY3k9IjUwIiByPSI0NSIgZmlsbD0iI2U4ZWZmZiIvPjx0ZXh0IHg9IjUwIiB5PSI1NSIgdGV4dC1hbmNob3I9Im1pZGRsZSIgZm9udC1mYW1pbHk9IkF2ZW5pciIgZm9udC1zaXplPSIyMCIgZmlsbD0iI2M3N2EyYyI+WzwvdGV4dD48L3N2Zz4=',
    };
  },
  computed: {
    canCreate() {
      return localStorage.getItem('isLoggedIn') === 'true';
    },
    filterTabs() {
      return [
        { label: '全部', value: 'all' },
        { label: '进行中', value: 1 },
        { label: '已结束', value: 2 },
        { label: '草稿', value: 0 },
      ];
    },
    filteredVotes() {
      if (this.currentFilter === 'all') {
        return this.votes;
      }
      return this.votes.filter((v) => v.status === this.currentFilter);
    },
  },
  mounted() {
    const clubId = this.$route.params.clubId;
    if (clubId) {
      this.loadByClub(clubId);
    } else {
      this.loadAll();
    }
    this.loadVotedStatus();
  },
  methods: {
    getStatusLabel(status) {
      if (status === 1) return '进行中';
      if (status === 2) return '已结束';
      return '草稿';
    },
    getCountByStatus(status) {
      if (status === 'all') return this.votes.length;
      return this.votes.filter((v) => v.status === status).length;
    },
    formatTime(iso) {
      if (!iso) return '';
      const date = new Date(iso);
      return date.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' });
    },
    goToDetail(id) {
      this.$router.push(`/votes/${id}`);
    },
    isVoted(id) {
      return this.votedIds.has(id);
    },
    async loadAll() {
      this.loading = true;
      this.error = '';
      try {
        const res = await voteApi.listByClub(0);
        this.votes = Array.isArray(res) ? res : [];
      } catch (e) {
        this.error = e.message || '加载失败';
      } finally {
        this.loading = false;
      }
    },
    async loadByClub(clubId) {
      this.loading = true;
      this.error = '';
      try {
        const list = await voteApi.listByClub(clubId);
        this.votes = Array.isArray(list) ? list : [];
      } catch (e) {
        this.error = e.message || '加载失败';
      } finally {
        this.loading = false;
      }
    },
    async loadVotedStatus() {
      try {
        const uid = localStorage.getItem('userId');
        if (uid) {
          const voted = JSON.parse(localStorage.getItem('voteResponded') || '[]');
          this.votedIds = new Set(voted);
        }
      } catch (e) {
        console.warn(e);
      }
    },
  },
};
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: linear-gradient(180deg, #e8f5e9 0%, #f7f8fa 100%);
}

.main {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 16px 48px;
}

.hero-section {
  position: relative;
  background: linear-gradient(135deg, #67c23a 0%, #95d475 100%);
  border-radius: 20px;
  padding: 48px 32px;
  margin-bottom: 32px;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(103, 194, 58, 0.3);
}

.hero-content {
  position: relative;
  z-index: 2;
  color: white;
}

.hero-content h1 {
  font-size: 32px;
  margin-bottom: 8px;
  color: white;
}

.hero-desc {
  font-size: 16px;
  opacity: 0.95;
  margin-bottom: 24px;
}

.hero-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  background: white;
  color: #67c23a;
  padding: 12px 28px;
  border-radius: 24px;
  font-weight: 600;
  text-decoration: none;
  transition: all 0.25s ease;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

.hero-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 24px rgba(0, 0, 0, 0.2);
  color: #67c23a;
}

.hero-decoration {
  position: absolute;
  top: 0;
  right: 0;
  bottom: 0;
  left: 0;
  pointer-events: none;
}

.deco-circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
}

.circle-1 {
  width: 200px;
  height: 200px;
  top: -60px;
  right: -40px;
}

.circle-2 {
  width: 150px;
  height: 150px;
  bottom: -50px;
  right: 120px;
}

.circle-3 {
  width: 100px;
  height: 100px;
  top: 50%;
  right: 50px;
}

.filter-section {
  margin-bottom: 24px;
}

.filter-tabs {
  display: flex;
  gap: 12px;
  background: white;
  padding: 8px;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.filter-tab {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px 16px;
  border: none;
  background: transparent;
  border-radius: 12px;
  font-size: 14px;
  color: #666;
  cursor: pointer;
  transition: all 0.2s ease;
  font-weight: 500;
}

.filter-tab:hover {
  background: #f0f9f0;
  color: #67c23a;
}

.filter-tab.active {
  background: linear-gradient(135deg, #67c23a 0%, #95d475 100%);
  color: white;
  box-shadow: 0 4px 16px rgba(103, 194, 58, 0.3);
}

.tab-count {
  background: rgba(0, 0, 0, 0.08);
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
  min-width: 24px;
  text-align: center;
}

.filter-tab.active .tab-count {
  background: rgba(255, 255, 255, 0.3);
}

.content-section {
  min-height: 400px;
}

.muted {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: #999;
  text-align: center;
  padding: 64px;
  font-size: 15px;
}

.error-text {
  color: #f56c6c;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 64px 32px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.empty-action {
  margin-top: 24px;
  color: #67c23a;
  text-decoration: none;
  font-weight: 500;
  transition: color 0.2s;
}

.empty-action:hover {
  color: #95d475;
}

.vote-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
}

.v-card {
  background: white;
  border-radius: 16px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  border: 2px solid transparent;
}

.v-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 32px rgba(103, 194, 58, 0.15);
  border-color: rgba(103, 194, 58, 0.2);
}

.v-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.v-type {
  font-size: 12px;
  padding: 4px 12px;
  border-radius: 12px;
  font-weight: 600;
}

.v-type.single {
  background: #e8f0fe;
  color: #5b7fc9;
}

.v-type.multiple {
  background: #fff3e0;
  color: #e6a23c;
}

.v-status {
  font-size: 12px;
  padding: 4px 12px;
  border-radius: 12px;
  font-weight: 600;
}

.v-status.status-1 {
  background: #e8f5e9;
  color: #67c23a;
}

.v-status.status-2 {
  background: #f4f4f5;
  color: #909399;
}

.v-status.status-0 {
  background: #fff3e0;
  color: #e6a23c;
}

.v-anonymous {
  color: #909399;
  display: flex;
  align-items: center;
  margin-left: auto;
}

.v-club {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.club-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  object-fit: cover;
}

.club-name {
  font-size: 13px;
  color: #666;
  font-weight: 500;
}

.v-title {
  margin: 0 0 8px;
  font-size: 17px;
  font-weight: 600;
  color: #333;
  line-height: 1.4;
}

.v-desc {
  margin: 0 0 16px;
  font-size: 14px;
  color: #666;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.v-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.v-meta {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #999;
}

.participant-count {
  color: #666;
}

.v-action {
  display: flex;
  align-items: center;
  gap: 8px;
}

.voted-badge {
  background: #e8f5e9;
  color: #67c23a;
  font-size: 11px;
  padding: 3px 10px;
  border-radius: 10px;
  font-weight: 500;
}

.arrow-icon {
  color: #ccc;
  transition: all 0.2s;
}

.v-card:hover .arrow-icon {
  color: #67c23a;
  transform: translateX(4px);
}

@media (max-width: 768px) {
  .hero-section {
    padding: 32px 20px;
    border-radius: 16px;
  }

  .hero-content h1 {
    font-size: 26px;
  }

  .filter-tabs {
    flex-wrap: wrap;
  }

  .filter-tab {
    flex: 0 0 calc(50% - 6px);
  }

  .vote-grid {
    grid-template-columns: 1fr;
  }
}
</style>
