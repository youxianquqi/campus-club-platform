<template>
  <div class="page">
    <SiteHeader />

    <main class="main">
      <div class="hero-section">
        <div class="hero-content">
          <div class="hero-icon">
            <svg viewBox="0 0 24 24" width="48" height="48" fill="currentColor">
              <path
                d="M12 3L1 9l4 2.18v6L12 21l7-3.82v-6l2-1.09V17h2V9L12 3zm6.82 6L12 12.72 5.18 9 12 5.28 18.82 9zM17 15.99l-5 2.73-5-2.73v-3.72L12 15l5-2.73v3.72z"
              />
            </svg>
          </div>
          <h1>集思广益</h1>
          <p class="hero-subtitle">问卷调查 · 投票决策 · 共同参与</p>
          <div class="hero-stats">
            <div class="stat-item">
              <span class="stat-number">{{ questionnaireCount }}</span>
              <span class="stat-label">问卷</span>
            </div>
            <div class="stat-divider"></div>
            <div class="stat-item">
              <span class="stat-number">{{ voteCount }}</span>
              <span class="stat-label">投票</span>
            </div>
            <div class="stat-divider"></div>
            <div class="stat-item">
              <span class="stat-number">{{ totalParticipants }}</span>
              <span class="stat-label">参与人次</span>
            </div>
          </div>
        </div>
        <div class="hero-decoration">
          <div class="deco-shape shape-1"></div>
          <div class="deco-shape shape-2"></div>
          <div class="deco-shape shape-3"></div>
        </div>
      </div>

      <div class="tab-navigation">
        <div class="tab-wrapper">
          <button
            class="tab-btn"
            :class="{ active: activeTab === 'questionnaire' }"
            @click="switchTab('questionnaire')"
          >
            <svg viewBox="0 0 24 24" width="22" height="22" fill="currentColor">
              <path
                d="M14 2H6c-1.1 0-1.99.9-1.99 2L4 20c0 1.1.89 2 1.99 2H18c1.1 0 2-.9 2-2V8l-6-6zm2 16H8v-2h8v2zm0-4H8v-2h8v2zm-3-5V3.5L18.5 9H13z"
              />
            </svg>
            <span>问卷调查</span>
            <span class="tab-count">{{ questionnaireCount }}</span>
          </button>
          <button
            class="tab-btn"
            :class="{ active: activeTab === 'vote' }"
            @click="switchTab('vote')"
          >
            <svg viewBox="0 0 24 24" width="22" height="22" fill="currentColor">
              <path
                d="M18 13h-5v5c0 .55-.45 1-1 1s-1-.45-1-1v-5H6c-.55 0-1-.45-1-1s.45-1 1-1h5V6c0-.55.45-1 1-1s1 .45 1 1v5h5c.55 0 1 .45 1 1s-.45 1-1 1zM19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2z"
              />
            </svg>
            <span>投票决策</span>
            <span class="tab-count">{{ voteCount }}</span>
          </button>
          <div class="tab-indicator" :class="activeTab"></div>
        </div>
      </div>

      <div class="content-section">
        <transition name="fade" mode="out-in">
          <div v-if="activeTab === 'questionnaire'" key="questionnaire" class="tab-content">
            <div class="content-header">
              <h2>
                <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor">
                  <path
                    d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-5 14H7v-2h7v2zm3-4H7v-2h10v2zm0-4H7V7h10v2z"
                  />
                </svg>
                问卷列表
              </h2>
              <router-link to="/questionnaires" class="view-all">
                查看全部
                <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor">
                  <path d="M10 6L8.59 7.41 13.17 12l-4.58 4.59L10 18l6-6z" />
                </svg>
              </router-link>
            </div>

            <div v-if="loadingQuestionnaires" class="loading-state">
              <LoadingSpinner size="40px" />
              <p>加载中...</p>
            </div>

            <div v-else-if="questionnaires.length === 0" class="empty-state">
              <EmptyState
                icon="📋"
                title="暂无问卷"
                description="还没有发布任何问卷，快来创建第一个吧"
              />
              <router-link to="/questionnaires/create" class="empty-action"> 创建问卷 </router-link>
            </div>

            <div v-else class="items-grid">
              <div
                v-for="q in questionnaires"
                :key="q.id"
                class="item-card questionnaire-card"
                @click="goToQuestionnaire(q.id)"
              >
                <div class="card-header">
                  <span class="item-status" :class="'status-' + q.status">
                    {{ getQuestionnaireStatusLabel(q.status) }}
                  </span>
                  <span v-if="q.allowAnonymous" class="anonymous-badge">
                    <svg viewBox="0 0 24 24" width="12" height="12" fill="currentColor">
                      <path
                        d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"
                      />
                    </svg>
                    匿名
                  </span>
                </div>
                <div class="card-club" v-if="q.clubName">
                  <img :src="q.clubLogo || defaultLogo" class="club-avatar" alt="" />
                  <span>{{ q.clubName }}</span>
                </div>
                <h3 class="card-title">{{ q.title }}</h3>
                <p class="card-desc" v-if="q.description">{{ q.description }}</p>
                <div class="card-footer">
                  <span class="card-time">
                    <svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor">
                      <path
                        d="M11.99 2C6.47 2 2 6.48 2 12s4.47 10 9.99 10C17.52 22 22 17.52 22 12S17.52 2 11.99 2zM12 20c-4.42 0-8-3.58-8-8s3.58-8 8-8 8 3.58 8 8-3.58 8-8 8zm.5-13H11v6l5.25 3.15.75-1.23-4.5-2.67z"
                      />
                    </svg>
                    {{ q.endTime ? formatDate(q.endTime) + ' 截止' : '长期有效' }}
                  </span>
                  <span v-if="isQuestionnaireResponded(q.id)" class="responded-tag">已填写</span>
                </div>
              </div>
            </div>
          </div>

          <div v-else key="vote" class="tab-content">
            <div class="content-header">
              <h2>
                <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor">
                  <path
                    d="M18 13h-5v5c0 .55-.45 1-1 1s-1-.45-1-1v-5H6c-.55 0-1-.45-1-1s.45-1 1-1h5V6c0-.55.45-1 1-1s1 .45 1 1v5h5c.55 0 1 .45 1 1s-.45 1-1 1z"
                  />
                </svg>
                投票列表
              </h2>
              <router-link to="/votes" class="view-all">
                查看全部
                <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor">
                  <path d="M10 6L8.59 7.41 13.17 12l-4.58 4.59L10 18l6-6z" />
                </svg>
              </router-link>
            </div>

            <div v-if="loadingVotes" class="loading-state">
              <LoadingSpinner size="40px" />
              <p>加载中...</p>
            </div>

            <div v-else-if="votes.length === 0" class="empty-state">
              <EmptyState
                icon="🗳️"
                title="暂无投票"
                description="还没有发起任何投票，快来创建第一个吧"
              />
              <router-link to="/votes/create" class="empty-action"> 创建投票 </router-link>
            </div>

            <div v-else class="items-grid">
              <div
                v-for="v in votes"
                :key="v.id"
                class="item-card vote-card"
                @click="goToVote(v.id)"
              >
                <div class="card-header">
                  <span class="vote-type-badge" :class="v.voteType === 1 ? 'single' : 'multiple'">
                    {{ v.voteType === 1 ? '单选' : '多选' }}
                  </span>
                  <span class="item-status" :class="'status-' + v.status">
                    {{ getVoteStatusLabel(v.status) }}
                  </span>
                </div>
                <div class="card-club" v-if="v.clubName">
                  <img :src="v.clubLogo || defaultLogo" class="club-avatar" alt="" />
                  <span>{{ v.clubName }}</span>
                </div>
                <h3 class="card-title">{{ v.title }}</h3>
                <p class="card-desc" v-if="v.description">{{ v.description }}</p>
                <div class="card-footer">
                  <span class="card-meta">
                    <svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor">
                      <path
                        d="M16 11c1.66 0 2.99-1.34 2.99-3S17.66 5 16 5c-1.66 0-3 1.34-3 3s1.34 3 3 3zm-8 0c1.66 0 2.99-1.34 2.99-3S9.66 5 8 5C6.34 5 5 6.34 5 8s1.34 3 3 3zm0 2c-2.33 0-7 1.17-7 3.5V19h14v-2.5c0-2.33-4.67-3.5-7-3.5z"
                      />
                    </svg>
                    {{ v.totalVotes || 0 }} 人参与
                  </span>
                  <span v-if="isVoteResponded(v.id)" class="voted-tag">已投票</span>
                </div>
              </div>
            </div>
          </div>
        </transition>
      </div>
    </main>

    <transition name="fab">
      <router-link v-if="canCreate" :to="fabLink" class="fab-button" :class="activeTab">
        <svg viewBox="0 0 24 24" width="24" height="24" fill="currentColor">
          <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z" />
        </svg>
        <span>{{ activeTab === 'questionnaire' ? '创建问卷' : '创建投票' }}</span>
      </router-link>
    </transition>
  </div>
</template>

<script>
import SiteHeader from '../components/SiteHeader.vue';
import LoadingSpinner from '../components/LoadingSpinner.vue';
import EmptyState from '../components/EmptyState.vue';
import { questionnaireApi, voteApi } from '../api/index.js';

export default {
  name: 'BrainstormHub',
  components: {
    SiteHeader,
    LoadingSpinner,
    EmptyState,
  },
  data() {
    return {
      activeTab: 'questionnaire',
      questionnaires: [],
      votes: [],
      loadingQuestionnaires: true,
      loadingVotes: false,
      respondedQuestionnaires: new Set(),
      respondedVotes: new Set(),
      defaultLogo:
        'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCAxMDAgMTAwIj48Y2lyY2xlIGN4PSI1MCIgY3k9IjUwIiByPSI0NSIgZmlsbD0iI2U4ZWZmZiIvPjx0ZXh0IHg9IjUwIiB5PSI1NSIgdGV4dC1hbmNob3I9Im1pZGRsZSIgZm9udC1mYW1pbHk9IkF2ZW5pciIgZm9udC1zaXplPSIyMCIgZmlsbD0iI2M3N2EyYyI+WzwvdGV4dD48L3N2Zz4=',
    };
  },
  computed: {
    canCreate() {
      return localStorage.getItem('isLoggedIn') === 'true';
    },
    questionnaireCount() {
      return this.questionnaires.length;
    },
    voteCount() {
      return this.votes.length;
    },
    totalParticipants() {
      const qParticipants = this.questionnaires.reduce((sum, q) => sum + (q.responseCount || 0), 0);
      const vParticipants = this.votes.reduce((sum, v) => sum + (v.totalVotes || 0), 0);
      return qParticipants + vParticipants;
    },
    fabLink() {
      return this.activeTab === 'questionnaire' ? '/questionnaires/create' : '/votes/create';
    },
  },
  mounted() {
    this.loadQuestionnaires();
    this.loadRespondedStatus();
  },
  methods: {
    switchTab(tab) {
      if (this.activeTab === tab) return;
      this.activeTab = tab;

      if (tab === 'vote' && this.votes.length === 0) {
        this.loadVotes();
      }
    },
    async loadQuestionnaires() {
      this.loadingQuestionnaires = true;
      try {
        const res = await questionnaireApi.listByClub(0);
        this.questionnaires = Array.isArray(res) ? res.slice(0, 6) : [];
      } catch (e) {
        console.error('加载问卷失败', e);
      } finally {
        this.loadingQuestionnaires = false;
      }
    },
    async loadVotes() {
      this.loadingVotes = true;
      try {
        const res = await voteApi.listByClub(0);
        this.votes = Array.isArray(res) ? res.slice(0, 6) : [];
      } catch (e) {
        console.error('加载投票失败', e);
      } finally {
        this.loadingVotes = false;
      }
    },
    async loadRespondedStatus() {
      try {
        const qResponded = JSON.parse(localStorage.getItem('questionnaireResponded') || '[]');
        const vResponded = JSON.parse(localStorage.getItem('voteResponded') || '[]');
        this.respondedQuestionnaires = new Set(qResponded);
        this.respondedVotes = new Set(vResponded);
      } catch (e) {
        console.warn(e);
      }
    },
    isQuestionnaireResponded(id) {
      return this.respondedQuestionnaires.has(id);
    },
    isVoteResponded(id) {
      return this.respondedVotes.has(id);
    },
    getQuestionnaireStatusLabel(status) {
      const labels = { 0: '草稿', 1: '进行中', 2: '已结束' };
      return labels[status] || '未知';
    },
    getVoteStatusLabel(status) {
      const labels = { 0: '未开始', 1: '进行中', 2: '已结束' };
      return labels[status] || '未知';
    },
    formatDate(iso) {
      if (!iso) return '';
      const date = new Date(iso);
      return date.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' });
    },
    goToQuestionnaire(id) {
      this.$router.push(`/questionnaires/${id}`);
    },
    goToVote(id) {
      this.$router.push(`/votes/${id}`);
    },
  },
};
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: linear-gradient(180deg, rgba(255, 107, 157, 0.06) 0%, var(--color-bg-page) 100%);
  position: relative;
}

.main {
  max-width: var(--page-max-width);
  margin: 0 auto;
  padding: 0 16px 80px;
}

.hero-section {
  position: relative;
  background: var(--header-gradient);
  border-radius: 24px;
  padding: 56px 40px;
  margin: 24px 0 32px;
  overflow: hidden;
  box-shadow: 0 12px 40px rgba(255, 107, 157, 0.25);
}

.hero-content {
  position: relative;
  z-index: 2;
  text-align: center;
  color: white;
}

.hero-icon {
  width: 80px;
  height: 80px;
  margin: 0 auto 20px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.hero-content h1 {
  font-size: 36px;
  margin-bottom: 8px;
  color: white;
  letter-spacing: 2px;
}

.hero-subtitle {
  font-size: 16px;
  opacity: 0.95;
  margin-bottom: 28px;
}

.hero-stats {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 32px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.stat-number {
  font-size: 28px;
  font-weight: 700;
}

.stat-label {
  font-size: 13px;
  opacity: 0.85;
}

.stat-divider {
  width: 1px;
  height: 40px;
  background: rgba(255, 255, 255, 0.3);
}

.hero-decoration {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
}

.deco-shape {
  position: absolute;
  background: rgba(255, 255, 255, 0.08);
  border-radius: 50%;
}

.shape-1 {
  width: 300px;
  height: 300px;
  top: -100px;
  right: -80px;
}

.shape-2 {
  width: 200px;
  height: 200px;
  bottom: -80px;
  left: -60px;
}

.shape-3 {
  width: 150px;
  height: 150px;
  top: 50%;
  right: 15%;
  transform: translateY(-50%);
}

.tab-navigation {
  margin-bottom: 28px;
}

.tab-wrapper {
  display: flex;
  gap: 8px;
  background: var(--color-bg-card);
  padding: 8px;
  border-radius: 16px;
  box-shadow: var(--shadow-card);
  position: relative;
}

.tab-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 14px 24px;
  border: none;
  background: transparent;
  border-radius: 12px;
  font-size: 15px;
  font-weight: 500;
  color: var(--color-text-secondary);
  cursor: pointer;
  transition: all 0.25s ease;
  position: relative;
  z-index: 2;
}

.tab-btn:hover {
  color: var(--color-text-primary);
}

.tab-btn.active {
  color: var(--header-text-on-gradient);
}

.tab-btn svg {
  flex-shrink: 0;
}

.tab-count {
  background: rgba(0, 0, 0, 0.08);
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
  font-weight: 600;
}

.tab-btn.active .tab-count {
  background: rgba(255, 255, 255, 0.25);
}

.tab-indicator {
  position: absolute;
  top: 8px;
  bottom: 8px;
  width: calc(50% - 8px);
  background: var(--header-gradient);
  border-radius: 12px;
  transition: all 0.3s ease;
  z-index: 1;
}

.tab-indicator.questionnaire {
  left: 8px;
}

.tab-indicator.vote {
  left: calc(50%);
}

.content-section {
  min-height: 500px;
}

.tab-content {
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.content-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.content-header h2 {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 18px;
  color: var(--color-text-primary);
  margin: 0;
}

.content-header h2 svg {
  color: var(--color-primary);
}

.view-all {
  display: flex;
  align-items: center;
  gap: 6px;
  color: var(--color-primary);
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  transition: color 0.2s;
}

.view-all:hover {
  color: var(--color-primary-light);
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 32px;
  text-align: center;
  color: var(--color-text-muted);
  gap: 16px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 64px 32px;
  background: var(--color-bg-card);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-card);
}

.empty-action {
  margin-top: 24px;
  padding: 12px 28px;
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-light) 100%);
  color: var(--header-text-on-gradient);
  text-decoration: none;
  border-radius: 24px;
  font-weight: 500;
  transition: all 0.25s ease;
}

.empty-action:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(255, 107, 157, 0.3);
}

.items-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(340px, 1fr));
  gap: 20px;
}

.item-card {
  background: var(--color-bg-card);
  border-radius: var(--radius-lg);
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: var(--shadow-card);
  border: 2px solid transparent;
}

.item-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 32px rgba(255, 107, 157, 0.12);
}

.questionnaire-card:hover {
  border-color: rgba(255, 107, 157, 0.2);
}

.vote-card:hover {
  border-color: rgba(103, 194, 58, 0.2);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.item-status {
  font-size: 12px;
  padding: 4px 12px;
  border-radius: 12px;
  font-weight: 600;
}

.item-status.status-1 {
  background: rgba(103, 194, 58, 0.12);
  color: var(--recruit-status-active);
}

.item-status.status-2 {
  background: var(--color-border-light);
  color: var(--recruit-status-closed);
}

.item-status.status-0 {
  background: rgba(230, 162, 60, 0.15);
  color: var(--recruit-type-ops);
}

.anonymous-badge,
.vote-type-badge {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 11px;
  padding: 3px 8px;
  border-radius: 8px;
  font-weight: 500;
}

.anonymous-badge {
  background: var(--color-border-light);
  color: var(--recruit-type-other);
}

.vote-type-badge.single {
  background: rgba(91, 127, 201, 0.12);
  color: var(--recruit-primary);
}

.vote-type-badge.multiple {
  background: rgba(230, 162, 60, 0.15);
  color: var(--recruit-type-ops);
}

.card-club {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--color-border-light);
}

.club-avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  object-fit: cover;
}

.card-club span {
  font-size: 13px;
  color: var(--color-text-secondary);
  font-weight: 500;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin-bottom: 8px;
  line-height: 1.4;
}

.card-desc {
  font-size: 14px;
  color: var(--color-text-secondary);
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin-bottom: 16px;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid var(--color-border-light);
}

.card-time,
.card-meta {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--color-text-muted);
}

.responded-tag,
.voted-tag {
  font-size: 11px;
  padding: 3px 10px;
  border-radius: 10px;
  font-weight: 500;
  background: rgba(103, 194, 58, 0.12);
  color: var(--recruit-status-active);
}

.fab-button {
  position: fixed;
  bottom: 32px;
  right: 32px;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 16px 28px;
  border-radius: 28px;
  font-size: 15px;
  font-weight: 600;
  text-decoration: none;
  color: var(--header-text-on-gradient);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
  transition: all 0.3s ease;
  z-index: 1000;
}

.fab-button.questionnaire {
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-light) 100%);
}

.fab-button.vote {
  background: linear-gradient(135deg, var(--recruit-type-tech) 0%, #95d475 100%);
}

.fab-button:hover {
  transform: translateY(-4px) scale(1.02);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.25);
}

.fab-enter-active,
.fab-leave-active {
  transition: all 0.3s ease;
}

.fab-enter-from,
.fab-leave-to {
  opacity: 0;
  transform: translateY(20px) scale(0.9);
}

@media (max-width: 768px) {
  .hero-section {
    padding: 40px 24px;
    margin: 16px 0 24px;
    border-radius: 20px;
  }

  .hero-content h1 {
    font-size: 28px;
  }

  .hero-stats {
    gap: 20px;
  }

  .stat-number {
    font-size: 24px;
  }

  .tab-btn {
    padding: 12px 16px;
    font-size: 14px;
  }

  .tab-btn span:not(.tab-count) {
    display: none;
  }

  .items-grid {
    grid-template-columns: 1fr;
  }

  .fab-button {
    bottom: 24px;
    right: 24px;
    padding: 14px 24px;
  }
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.25s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
