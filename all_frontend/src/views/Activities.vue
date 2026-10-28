<template>
  <div class="page">
    <SiteHeader />
    <main class="main">
      <div class="head-row">
        <div class="title-tabs">
          <h1>活动中心</h1>
          <div class="tabs">
            <button
              type="button"
              class="tab-btn"
              :class="{ active: activeTab === 'all' }"
              @click="switchTab('all')"
            >
              全部活动
            </button>
            <button
              type="button"
              class="tab-btn"
              :class="{ active: activeTab === 'my' }"
              @click="switchTab('my')"
            >
              我的活动
            </button>
          </div>
        </div>
        <div class="pager" v-if="activeTab === 'all'">
          <button
            type="button"
            class="pg"
            :disabled="page <= 1"
            @click="
              page--;
              load();
            "
          >
            上一页
          </button>
          <span class="pg-info">第 {{ page }} 页</span>
          <button
            type="button"
            class="pg"
            :disabled="!hasMore"
            @click="
              page++;
              load();
            "
          >
            下一页
          </button>
        </div>
      </div>
      <p v-if="loading" class="muted">加载中…</p>
      <p v-else-if="loadingMyActivities" class="muted">加载中…</p>
      <div v-else-if="activeTab === 'my' && !uid" class="empty-state">
        <p class="muted">请先登录查看我的活动</p>
        <router-link to="/login" class="btn primary">去登录</router-link>
      </div>
      <div v-else-if="activeTab === 'my' && myActivities.length === 0" class="empty-state">
        <p class="muted">您还没有报名任何活动</p>
        <button type="button" class="btn primary" @click="switchTab('all')">浏览活动</button>
      </div>
      <div v-else-if="activeTab === 'my'" class="list">
        <article v-for="a in myActivities" :key="a.id" class="card" @click="goToActivity(a.id)">
          <div class="cover" v-if="a.cover">
            <img :src="a.cover" alt="" @error="onImgError" />
          </div>
          <div class="body">
            <h2>{{ a.title }}</h2>
            <p class="desc">{{ a.description }}</p>
            <div class="meta">
              <span>{{ a.time }}</span>
              <span>{{ a.location || '地点待定' }}</span>
              <span class="st" :class="a.statusClass">{{ a.statusText }}</span>
            </div>
            <div class="foot">
              <span class="num"
                >报名 {{ a.currentParticipants }}/{{ a.maxParticipants ?? '—' }}</span
              >
              <span class="signed-badge">已报名</span>
            </div>
          </div>
        </article>
        <p v-if="!myActivities.length" class="muted">暂无活动</p>
      </div>
      <div v-else class="list">
        <article v-for="a in items" :key="a.id" class="card" @click="goToActivity(a.id)">
          <div class="cover" v-if="a.cover">
            <img :src="a.cover" alt="" @error="onImgError" />
          </div>
          <div class="body">
            <h2>{{ a.title }}</h2>
            <p class="desc">{{ a.description }}</p>
            <div class="meta">
              <span>{{ a.time }}</span>
              <span>{{ a.location || '地点待定' }}</span>
              <span class="st" :class="a.statusClass">{{ a.statusText }}</span>
            </div>
            <div class="foot">
              <span class="num"
                >报名 {{ a.currentParticipants }}/{{ a.maxParticipants ?? '—' }}</span
              >
              <button
                v-if="uid"
                type="button"
                class="btn signup-btn"
                @click.stop="signupActivity(a)"
              >
                报名参加
              </button>
              <router-link v-else to="/login" class="btn">登录</router-link>
            </div>
          </div>
        </article>
        <p v-if="!items.length" class="muted">暂无活动</p>
      </div>
    </main>

    <!-- 与首页一致：活动双人报名 -->
    <div v-if="pairSignupVisible" class="pair-signup-overlay" @click.self="closePairSignup">
      <div class="pair-signup-dialog">
        <h3 class="pair-signup-title">活动报名</h3>
        <p v-if="pairSignupActivity" class="pair-signup-sub">
          {{ pairSignupActivity.title }}
        </p>
        <p class="pair-signup-hint">
          填写<strong>第二位报名人</strong>（可选）即可双人报名；邀请码<strong>最多可填两个</strong>（可选填1个或2个），也可不填。
        </p>
        <label class="pair-signup-label">第二位报名人（用户名或学号，可不填）</label>
        <input
          v-model.trim="pairSignupForm.partnerAccount"
          type="text"
          class="pair-signup-input"
          placeholder="选填，填写后为双人报名"
        />
        <label class="pair-signup-label">社团邀请码 1（可选）</label>
        <input
          v-model.trim="pairSignupForm.clubKey1"
          type="text"
          class="pair-signup-input"
          placeholder="选填"
        />
        <label class="pair-signup-label">社团邀请码 2（可选）</label>
        <input
          v-model.trim="pairSignupForm.clubKey2"
          type="text"
          class="pair-signup-input"
          placeholder="选填"
        />
        <div class="pair-signup-actions">
          <button type="button" class="pair-signup-cancel" @click="closePairSignup">取消</button>
          <button
            type="button"
            class="pair-signup-submit"
            :disabled="pairSignupLoading"
            @click="submitPairSignup"
          >
            {{ pairSignupLoading ? '提交中…' : '确认报名' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import SiteHeader from '../components/SiteHeader.vue';
import {
  activityApi,
  getCurrentUserId,
  IMG_PLACEHOLDER,
  onImgError,
  resolveBackendUrl,
} from '../utils/api.js';

export default {
  name: 'Activities',
  components: { SiteHeader },
  data() {
    return {
      uid: null,
      page: 1,
      size: 8,
      items: [],
      loading: false,
      hasMore: true,
      activeTab: 'all',
      myActivities: [],
      loadingMyActivities: false,
      pairSignupVisible: false,
      pairSignupActivity: null,
      pairSignupLoading: false,
      pairSignupForm: {
        partnerAccount: '',
        clubKey1: '',
        clubKey2: '',
      },
    };
  },
  methods: {
    onImgError,
    mapActivity(raw) {
      const st = raw.status;
      let statusClass = 'upcoming';
      let statusText = '报名中';
      if (st === 3) {
        statusClass = 'ongoing';
        statusText = '进行中';
      } else if (st === 4) {
        statusClass = 'ended';
        statusText = '已结束';
      } else if (st === 5) {
        statusText = '已取消';
        statusClass = 'ended';
      }
      const start = raw.startTime ? new Date(raw.startTime) : null;
      const timeStr =
        start && !Number.isNaN(start.getTime())
          ? `${start.getMonth() + 1}月${start.getDate()}日 ${String(start.getHours()).padStart(2, '0')}:${String(start.getMinutes()).padStart(2, '0')}`
          : '时间待定';
      const max = raw.maxParticipants;
      const cur = raw.currentParticipants || 0;
      const cover = raw.cover || raw.imageUrl;
      return {
        id: raw.id,
        raw,
        title: raw.title,
        description: raw.description || raw.content || '',
        cover: cover ? resolveBackendUrl(cover) : IMG_PLACEHOLDER,
        time: timeStr,
        location: raw.location,
        statusClass,
        statusText,
        currentParticipants: cur,
        maxParticipants: max,
      };
    },
    async load() {
      this.loading = true;
      try {
        const res = await activityApi.page(this.page - 1, this.size);
        const records = res.data?.records || [];
        this.items = records.map((r) => this.mapActivity(r));
        this.hasMore = records.length >= this.size;
      } catch (e) {
        console.error(e);
        this.items = [];
      } finally {
        this.loading = false;
      }
    },
    async loadMyActivities() {
      const uid = getCurrentUserId();
      if (!uid) {
        this.myActivities = [];
        return;
      }
      this.loadingMyActivities = true;
      try {
        const activities = await activityApi.getMyActivities();
        this.myActivities = Array.isArray(activities) ? activities.map((r) => this.mapActivity(r)) : [];
      } catch (e) {
        console.error('加载我的活动失败', e);
        this.myActivities = [];
      } finally {
        this.loadingMyActivities = false;
      }
    },
    switchTab(tab) {
      this.activeTab = tab;
      if (tab === 'my') {
        this.loadMyActivities();
      }
    },
    goToActivity(id) {
      this.$router.push('/activity/' + id);
    },
    signupActivity(activity) {
      const uid = getCurrentUserId();
      if (!uid) {
        alert('请先登录');
        this.$router.push('/login');
        return;
      }
      this.pairSignupActivity = activity;
      this.pairSignupForm = { partnerAccount: '', clubKey1: '', clubKey2: '' };
      this.pairSignupVisible = true;
    },
    closePairSignup() {
      this.pairSignupVisible = false;
      this.pairSignupActivity = null;
      this.pairSignupLoading = false;
    },
    async submitPairSignup() {
      const uid = getCurrentUserId();
      if (!uid) {
        alert('请先登录');
        this.$router.push('/login');
        return;
      }
      const act = this.pairSignupActivity;
      if (!act) return;
      const { partnerAccount, clubKey1, clubKey2 } = this.pairSignupForm;
      this.pairSignupLoading = true;
      try {
        await activityApi.signupPair(act.id, {
          userId: uid,
          partnerAccount: partnerAccount || null,
          clubKey1: clubKey1 || null,
          clubKey2: clubKey2 || null,
        });
        alert('报名成功');
        this.closePairSignup();
        await this.load();
      } catch (err) {
        alert(err.message || '报名失败');
      } finally {
        this.pairSignupLoading = false;
      }
    },
  },
  mounted() {
    this.uid = getCurrentUserId();
    this.load();
    if (this.uid && this.activeTab === 'my') {
      this.loadMyActivities();
    }
  },
};
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: var(--color-bg-muted);
}
.main {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px 16px 48px;
}
.head-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 20px;
}
.title-tabs {
  display: flex;
  align-items: center;
  gap: 20px;
  flex-wrap: wrap;
}
.tabs {
  display: flex;
  gap: 8px;
}
.tab-btn {
  padding: 8px 16px;
  border: 1px solid var(--color-border);
  border-radius: 20px;
  background: var(--color-bg-card);
  color: var(--color-text-secondary);
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}
.tab-btn:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
}
.tab-btn.active {
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-light) 100%);
  border-color: var(--color-primary);
  color: #fff;
}
h1 {
  font-size: 22px;
  color: var(--color-text-primary);
}
.pager {
  display: flex;
  align-items: center;
  gap: 10px;
}
.pg {
  padding: 6px 14px;
  border-radius: var(--radius-sm);
  border: 1px solid var(--color-border);
  background: var(--color-bg-card);
  color: var(--color-text-primary);
  cursor: pointer;
  font-size: 13px;
}
.pg:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}
.pg-info {
  font-size: 13px;
  color: var(--color-text-secondary);
}
.muted {
  color: var(--color-text-muted);
  font-size: 14px;
}
.empty-state {
  text-align: center;
  padding: 48px 20px;
  background: var(--color-bg-card);
  border-radius: var(--radius-md);
}
.empty-state .muted {
  margin-bottom: 16px;
  font-size: 16px;
}
.signed-badge {
  display: inline-block;
  padding: 6px 12px;
  background: var(--color-success);
  color: #fff;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}
.list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.card {
  display: flex;
  gap: 16px;
  background: var(--color-bg-card);
  border-radius: var(--radius-md);
  overflow: hidden;
  box-shadow: var(--shadow-card);
  cursor: pointer;
}
.cover {
  width: 160px;
  flex-shrink: 0;
}
.cover img {
  width: 100%;
  height: 100%;
  min-height: 120px;
  object-fit: cover;
}
.body {
  flex: 1;
  padding: 16px 16px 16px 0;
  min-width: 0;
}
.body h2 {
  font-size: 17px;
  color: var(--color-text-primary);
  margin-bottom: 8px;
}
.desc {
  font-size: 14px;
  color: var(--color-text-secondary);
  line-height: 1.5;
  margin-bottom: 10px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  font-size: 13px;
  color: var(--color-text-secondary);
  margin-bottom: 12px;
}
.st.upcoming {
  color: var(--color-secondary);
}
.st.ongoing {
  color: var(--recruit-status-active);
}
.st.ended {
  color: var(--color-text-muted);
}
.foot {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}
.num {
  font-size: 13px;
  color: var(--color-text-secondary);
}
.btn {
  display: inline-block;
  padding: 8px 18px;
  border-radius: 20px;
  font-size: 13px;
  text-decoration: none;
  border: 1px solid var(--color-border);
  background: var(--color-bg-card);
  color: var(--color-text-primary);
  cursor: pointer;
}
.signup-btn {
  border: none;
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-light) 100%);
  color: var(--header-text-on-gradient);
}
.signup-btn:hover {
  opacity: 0.92;
}
.btn.primary {
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-light) 100%);
  color: var(--header-text-on-gradient);
  border: none;
}
.btn.ghost {
  border-color: var(--color-border);
}

.pair-signup-overlay {
  position: fixed;
  inset: 0;
  z-index: 2000;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}
.pair-signup-dialog {
  width: 100%;
  max-width: 420px;
  background: var(--color-bg-card);
  border-radius: var(--radius-lg);
  padding: 24px;
  box-shadow: var(--shadow-lg);
}
.pair-signup-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin-bottom: 8px;
}
.pair-signup-sub {
  font-size: 14px;
  color: var(--color-text-secondary);
  margin-bottom: 12px;
  line-height: 1.4;
}
.pair-signup-hint {
  font-size: 12px;
  color: var(--color-text-secondary);
  line-height: 1.5;
  margin-bottom: 16px;
}
.pair-signup-label {
  display: block;
  font-size: 13px;
  color: var(--color-text-primary);
  margin-bottom: 6px;
  margin-top: 12px;
}
.pair-signup-label:first-of-type {
  margin-top: 0;
}
.pair-signup-input {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  font-size: 14px;
  box-sizing: border-box;
  background: var(--color-bg-card);
  color: var(--color-text-primary);
}
.pair-signup-input:focus {
  outline: none;
  border-color: var(--color-secondary);
}
.pair-signup-actions {
  display: flex;
  gap: 12px;
  margin-top: 20px;
  justify-content: flex-end;
}
.pair-signup-cancel {
  padding: 10px 18px;
  border-radius: var(--radius-sm);
  border: 1px solid var(--color-border);
  background: var(--color-bg-card);
  font-size: 14px;
  cursor: pointer;
  color: var(--color-text-secondary);
}
.pair-signup-submit {
  padding: 10px 18px;
  border-radius: var(--radius-sm);
  border: none;
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-light) 100%);
  color: var(--header-text-on-gradient);
  font-size: 14px;
  cursor: pointer;
}
.pair-signup-submit:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

@media (max-width: 640px) {
  .card {
    flex-direction: column;
  }
  .cover {
    width: 100%;
  }
  .body {
    padding: 0 16px 16px;
  }
}
</style>
