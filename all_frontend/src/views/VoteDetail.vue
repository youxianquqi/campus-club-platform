<template>
  <div class="page">
    <SiteHeader />
    <main class="main" v-if="vote">
      <div class="hero-section">
        <div class="hero-content">
          <div class="hero-badges">
            <span class="hero-type" :class="vote.voteType === 1 ? 'single' : 'multiple'">
              {{ vote.voteType === 1 ? '单选投票' : '多选投票' }}
            </span>
            <span class="hero-status" :class="'status-' + vote.status">
              {{ getStatusLabel(vote.status) }}
            </span>
            <span v-if="vote.allowAnonymous" class="hero-badge">
              <svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor">
                <path
                  d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"
                />
              </svg>
              允许匿名
            </span>
          </div>
          <h1>{{ vote.title }}</h1>
          <p class="hero-desc" v-if="vote.description">{{ vote.description }}</p>
          <div class="hero-meta">
            <div class="club-info" v-if="vote.clubName">
              <img :src="vote.clubLogo || defaultLogo" class="club-avatar" alt="" />
              <span>{{ vote.clubName }}</span>
            </div>
            <div class="time-info">
              <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor">
                <path
                  d="M11.99 2C6.47 2 2 6.48 2 12s4.47 10 9.99 10C17.52 22 22 17.52 22 12S17.52 2 11.99 2zM12 20c-4.42 0-8-3.58-8-8s3.58-8 8-8 8 3.58 8 8-3.58 8-8 8zm.5-13H11v6l5.25 3.15.75-1.23-4.5-2.67z"
                />
              </svg>
              <span v-if="vote.endTime">
                {{ formatDateTime(vote.startTime) }} - {{ formatDateTime(vote.endTime) }}
              </span>
              <span v-else>长期有效</span>
            </div>
            <div class="participant-info" v-if="totalVotes > 0">
              <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor">
                <path
                  d="M16 11c1.66 0 2.99-1.34 2.99-3S17.66 5 16 5c-1.66 0-3 1.34-3 3s1.34 3 3 3zm-8 0c1.66 0 2.99-1.34 2.99-3S9.66 5 8 5C6.34 5 5 6.34 5 8s1.34 3 3 3zm0 2c-2.33 0-7 1.17-7 3.5V19h14v-2.5c0-2.33-4.67-3.5-7-3.5z"
                />
              </svg>
              <span>{{ totalVotes }} 人已投票</span>
            </div>
          </div>
        </div>
        <div class="hero-decoration">
          <div class="deco-circle circle-1"></div>
          <div class="deco-circle circle-2"></div>
        </div>
      </div>

      <div class="content-wrapper">
        <div v-if="hasVoted" class="voted-card">
          <div class="voted-icon">
            <svg viewBox="0 0 24 24" width="48" height="48" fill="currentColor">
              <path d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z" />
            </svg>
          </div>
          <h2>已成功投票</h2>
          <p>感谢您的参与！您已为此投票贡献了一份力量</p>
          <router-link to="/votes" class="btn-back">返回投票列表</router-link>
        </div>

        <div v-else-if="vote.status === 1" class="vote-form">
          <div class="form-intro" v-if="vote.voteType === 2">
            <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor">
              <path
                d="M11 7h2v2h-2zm0 4h2v6h-2zm1-9C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8z"
              />
            </svg>
            <span>您最多可以选择 {{ vote.maxOptions || 2 }} 个选项</span>
          </div>

          <div class="options-list">
            <div
              v-for="option in options"
              :key="option.id"
              class="option-card"
              :class="{
                selected: isSelected(option.id),
                disabled: !canSelect(option.id),
              }"
              @click="toggleOption(option)"
            >
              <div class="option-checkbox">
                <div class="checkbox-indicator" v-if="vote.voteType === 2">
                  <svg
                    v-if="isSelected(option.id)"
                    viewBox="0 0 24 24"
                    width="16"
                    height="16"
                    fill="currentColor"
                  >
                    <path d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z" />
                  </svg>
                </div>
                <div class="radio-indicator" v-else>
                  <div class="radio-inner" v-if="isSelected(option.id)"></div>
                </div>
              </div>
              <div class="option-content">
                <img v-if="option.imageUrl" :src="option.imageUrl" class="option-image" alt="" />
                <span class="option-text">{{ option.optionText }}</span>
              </div>
            </div>
          </div>

          <div class="form-actions">
            <button
              class="submit-btn"
              @click="submitVote"
              :disabled="selectedOptions.length === 0 || submitting"
            >
              <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor">
                <path d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z" />
              </svg>
              {{ submitting ? '提交中...' : '确认投票' }}
            </button>
          </div>
        </div>

        <div v-else-if="vote.status === 2" class="results-card">
          <div class="results-header">
            <h2>投票结果</h2>
            <span class="results-total">共 {{ totalVotes }} 人参与</span>
          </div>

          <div class="results-list">
            <div
              v-for="option in sortedOptions"
              :key="option.id"
              class="result-item"
              :class="{ winner: isWinner(option) }"
            >
              <div class="result-header">
                <span class="result-text">{{ option.optionText }}</span>
                <span class="result-count">{{ option.voteCount }} 票</span>
              </div>
              <div class="result-bar">
                <div
                  class="result-fill"
                  :style="{ width: getPercentage(option.voteCount) + '%' }"
                ></div>
              </div>
              <span class="result-percentage">{{ getPercentage(option.voteCount) }}%</span>
            </div>
          </div>
        </div>

        <div v-else class="notice-card">
          <svg viewBox="0 0 24 24" width="32" height="32" fill="currentColor">
            <path
              d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z"
            />
          </svg>
          <p>此投票尚未开始或已结束</p>
        </div>
      </div>
    </main>
    <div v-else-if="loading" class="loading-state">
      <LoadingSpinner size="48px" />
      <p>加载中...</p>
    </div>
    <div v-else-if="error" class="error-state">
      <p>{{ error }}</p>
      <router-link to="/votes" class="btn-back">返回投票列表</router-link>
    </div>
  </div>
</template>

<script>
import SiteHeader from '../components/SiteHeader.vue';
import LoadingSpinner from '../components/LoadingSpinner.vue';
import { voteApi } from '../api/index.js';

export default {
  name: 'VoteDetail',
  components: { SiteHeader, LoadingSpinner },
  data() {
    return {
      vote: null,
      options: [],
      loading: true,
      error: '',
      submitting: false,
      hasVoted: false,
      selectedOptions: [],
      defaultLogo:
        'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCAxMDAgMTAwIj48Y2lyY2xlIGN4PSI1MCIgY3k9IjUwIiByPSI0NSIgZmlsbD0iI2U4ZWZmZiIvPjx0ZXh0IHg9IjUwIiB5PSI1NSIgdGV4dC1hbmNob3I9Im1pZGRsZSIgZm9udC1mYW1pbHk9IkF2ZW5pciIgZm9udC1zaXplPSIyMCIgZmlsbD0iI2M3N2EyYyI+WzwvdGV4dD48L3N2Zz4=',
    };
  },
  computed: {
    totalVotes() {
      return this.options.reduce((sum, opt) => sum + (opt.voteCount || 0), 0);
    },
    sortedOptions() {
      return [...this.options].sort((a, b) => (b.voteCount || 0) - (a.voteCount || 0));
    },
    maxSelections() {
      return this.vote?.maxOptions || (this.vote?.voteType === 1 ? 1 : 2);
    },
  },
  mounted() {
    this.load();
    this.checkVotedStatus();
  },
  methods: {
    getStatusLabel(status) {
      if (status === 1) return '进行中';
      if (status === 2) return '已结束';
      return '未开始';
    },
    formatDateTime(iso) {
      if (!iso) return '';
      const date = new Date(iso);
      return date.toLocaleDateString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
      });
    },
    async load() {
      const id = this.$route.params.id;
      if (!id) {
        this.error = '缺少投票 ID';
        this.loading = false;
        return;
      }

      try {
        const res = await voteApi.getById(id);
        const data = res.data || res;
        this.vote = data;

        if (data.options) {
          this.options = typeof data.options === 'string' ? JSON.parse(data.options) : data.options;
        } else if (data.optionList) {
          this.options = data.optionList;
        }

        this.options = this.options.map((opt) => ({
          id: opt.id,
          optionText: opt.optionText,
          imageUrl: opt.imageUrl,
          voteCount: opt.voteCount || 0,
        }));
      } catch (e) {
        this.error = e.message || '加载失败';
      } finally {
        this.loading = false;
      }
    },
    checkVotedStatus() {
      const voted = JSON.parse(localStorage.getItem('voteResponded') || '[]');
      this.hasVoted = voted.includes(Number(this.$route.params.id));
    },
    isSelected(optionId) {
      return this.selectedOptions.includes(optionId);
    },
    canSelect(optionId) {
      if (this.isSelected(optionId)) return true;
      return this.selectedOptions.length < this.maxSelections;
    },
    toggleOption(option) {
      if (this.hasVoted || this.vote.status !== 1) return;
      if (!this.canSelect(option.id)) return;

      const index = this.selectedOptions.indexOf(option.id);
      if (index > -1) {
        this.selectedOptions.splice(index, 1);
      } else {
        this.selectedOptions.push(option.id);
      }
    },
    async submitVote() {
      if (this.selectedOptions.length === 0) return;
      if (this.hasVoted) return;

      this.submitting = true;
      try {
        const data = {
          selectedOptions: this.selectedOptions,
        };

        if (!this.vote.allowAnonymous) {
          data.userId = localStorage.getItem('userId');
        }

        await voteApi.submit(this.vote.id, data);

        const voted = JSON.parse(localStorage.getItem('voteResponded') || '[]');
        voted.push(this.vote.id);
        localStorage.setItem('voteResponded', JSON.stringify(voted));
        this.hasVoted = true;

        this.options = this.options.map((opt) => ({
          ...opt,
          voteCount: this.selectedOptions.includes(opt.id)
            ? (opt.voteCount || 0) + 1
            : opt.voteCount,
        }));
      } catch (e) {
        alert(e.message || '提交失败，请重试');
      } finally {
        this.submitting = false;
      }
    },
    getPercentage(count) {
      if (this.totalVotes === 0) return 0;
      return Math.round((count / this.totalVotes) * 100);
    },
    isWinner(option) {
      if (this.totalVotes === 0) return false;
      const maxCount = Math.max(...this.options.map((o) => o.voteCount || 0));
      return option.voteCount === maxCount;
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
  max-width: 900px;
  margin: 0 auto;
  padding: 24px 16px 48px;
}

.hero-section {
  position: relative;
  background: linear-gradient(135deg, #67c23a 0%, #95d475 100%);
  border-radius: 20px;
  padding: 40px 32px;
  margin-bottom: 28px;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(103, 194, 58, 0.3);
}

.hero-content {
  position: relative;
  z-index: 2;
}

.hero-badges {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 16px;
}

.hero-type {
  font-size: 12px;
  padding: 4px 12px;
  border-radius: 12px;
  font-weight: 600;
}

.hero-type.single {
  background: rgba(255, 255, 255, 0.3);
  color: white;
}

.hero-type.multiple {
  background: rgba(255, 255, 255, 0.3);
  color: white;
}

.hero-status {
  font-size: 12px;
  padding: 4px 12px;
  border-radius: 12px;
  font-weight: 600;
}

.hero-status.status-1 {
  background: white;
  color: #67c23a;
}

.hero-status.status-2 {
  background: rgba(255, 255, 255, 0.5);
  color: white;
}

.hero-status.status-0 {
  background: rgba(255, 255, 255, 0.5);
  color: white;
}

.hero-badge {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  padding: 4px 12px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.2);
  color: white;
}

.hero-content h1 {
  font-size: 28px;
  margin-bottom: 12px;
  color: white;
}

.hero-desc {
  font-size: 15px;
  color: rgba(255, 255, 255, 0.95);
  margin-bottom: 20px;
  line-height: 1.6;
}

.hero-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  color: rgba(255, 255, 255, 0.9);
  font-size: 14px;
}

.club-info,
.time-info,
.participant-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.club-avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  border: 2px solid white;
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
  top: -80px;
  right: -40px;
}

.circle-2 {
  width: 120px;
  height: 120px;
  bottom: -40px;
  right: 100px;
}

.content-wrapper {
  max-width: 720px;
  margin: 0 auto;
}

.voted-card,
.notice-card,
.results-card {
  background: white;
  border-radius: 16px;
  padding: 48px 32px;
  text-align: center;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.voted-icon,
.submitted-icon {
  width: 80px;
  height: 80px;
  margin: 0 auto 24px;
  background: linear-gradient(135deg, #67c23a 0%, #95d475 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.voted-card h2,
.notice-card p {
  color: #333;
  margin-bottom: 8px;
}

.voted-card p,
.notice-card p {
  color: #666;
  margin-bottom: 24px;
}

.btn-back {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 12px 28px;
  background: linear-gradient(135deg, #67c23a 0%, #95d475 100%);
  color: white;
  border-radius: 24px;
  text-decoration: none;
  font-weight: 500;
  transition: all 0.25s ease;
}

.btn-back:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(103, 194, 58, 0.3);
}

.vote-form {
  background: white;
  border-radius: 16px;
  padding: 28px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.form-intro {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 18px;
  background: #f0f9f0;
  border-radius: 12px;
  margin-bottom: 24px;
  color: #67c23a;
  font-size: 14px;
}

.options-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.option-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 18px 20px;
  background: #f8f9ff;
  border: 2px solid #e8efff;
  border-radius: 14px;
  cursor: pointer;
  transition: all 0.25s ease;
}

.option-card:hover:not(.disabled) {
  border-color: #67c23a;
  background: #f0f9f0;
}

.option-card.selected {
  border-color: #67c23a;
  background: #e8f5e9;
}

.option-card.disabled:not(.selected) {
  opacity: 0.5;
  cursor: not-allowed;
}

.option-checkbox {
  flex-shrink: 0;
}

.checkbox-indicator,
.radio-indicator {
  width: 24px;
  height: 24px;
  border-radius: 6px;
  border: 2px solid #d0d7e2;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.radio-indicator {
  border-radius: 50%;
}

.option-card.selected .checkbox-indicator,
.option-card.selected .radio-indicator {
  background: #67c23a;
  border-color: #67c23a;
  color: white;
}

.radio-inner {
  width: 12px;
  height: 12px;
  background: white;
  border-radius: 50%;
}

.option-content {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 12px;
}

.option-image {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  object-fit: cover;
}

.option-text {
  font-size: 15px;
  color: #333;
  font-weight: 500;
}

.form-actions {
  margin-top: 32px;
  display: flex;
  justify-content: center;
}

.submit-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 14px 48px;
  background: linear-gradient(135deg, #67c23a 0%, #95d475 100%);
  color: white;
  border: none;
  border-radius: 28px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.25s ease;
  box-shadow: 0 4px 20px rgba(103, 194, 58, 0.35);
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 28px rgba(103, 194, 58, 0.45);
}

.submit-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.results-card {
  padding: 32px;
}

.results-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 28px;
}

.results-header h2 {
  font-size: 20px;
  color: #333;
  margin: 0;
}

.results-total {
  font-size: 14px;
  color: #666;
  background: #f0f0f0;
  padding: 6px 14px;
  border-radius: 12px;
}

.results-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.result-item {
  padding: 16px 20px;
  background: #f8f9ff;
  border-radius: 12px;
  border: 2px solid transparent;
}

.result-item.winner {
  border-color: #67c23a;
  background: #f0f9f0;
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.result-text {
  font-size: 15px;
  font-weight: 500;
  color: #333;
}

.result-count {
  font-size: 14px;
  color: #666;
}

.result-bar {
  height: 8px;
  background: #e0e7ff;
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 6px;
}

.result-fill {
  height: 100%;
  background: linear-gradient(90deg, #67c23a, #95d475);
  border-radius: 4px;
  transition: width 0.6s ease;
}

.result-percentage {
  font-size: 13px;
  color: #666;
  font-weight: 500;
}

.notice-card {
  padding: 48px 32px;
}

.notice-card svg {
  color: #909399;
  margin-bottom: 16px;
}

.loading-state,
.error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 32px;
  text-align: center;
  color: #666;
}

@media (max-width: 768px) {
  .hero-section {
    padding: 28px 20px;
    border-radius: 16px;
  }

  .hero-content h1 {
    font-size: 22px;
  }

  .hero-meta {
    flex-direction: column;
    gap: 12px;
  }

  .vote-form {
    padding: 20px;
    border-radius: 16px;
  }

  .option-card {
    padding: 14px 16px;
  }

  .submit-btn {
    width: 100%;
    padding: 14px 32px;
  }
}
</style>
