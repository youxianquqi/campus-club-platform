<template>
  <div class="home-container">
    <SiteHeader />

    <main class="main-content">
      <HomeSkeleton v-if="loading && !hasLoaded" />

      <template v-else>
        <section class="banner-section">
          <div class="banner-wrapper">
            <div class="banner-main">
              <Transition name="banner-fade" mode="out-in">
                <div class="banner-slide active" :key="currentBannerIndex">
                  <img
                    :src="currentBanner"
                    class="banner-bg-img"
                    alt=""
                    loading="lazy"
                    @error="onImgError"
                  />
                  <div class="banner-overlay">
                    <h2>{{ bannerTitle }}</h2>
                    <p>{{ bannerDesc }}</p>
                  </div>
                </div>
              </Transition>
            </div>
            <div class="banner-side">
              <div class="ranking-side-wrapper">
                <div class="ranking-box-side">
                  <div class="ranking-header-side">
                    <h4 class="ranking-title-side">🔥 社团热度</h4>
                  </div>
                  <div class="ranking-list-side">
                    <TransitionGroup name="list-item">
                      <div
                        class="ranking-item-side clickable"
                        v-for="(club, index) in clubRanking.slice(0, 5)"
                        :key="club.id"
                        @click="goToClub(club.id)"
                      >
                        <span class="rank-num-side" :class="{ top: index < 3 }">{{
                          index + 1
                        }}</span>
                        <span class="rank-name-side">{{ club.name }}</span>
                        <span class="rank-heat-side">{{ club.heat }}</span>
                      </div>
                    </TransitionGroup>
                  </div>
                </div>
                <div class="ranking-box-side">
                  <div class="ranking-header-side">
                    <h4 class="ranking-title-side">📅 活动热度</h4>
                  </div>
                  <div class="ranking-list-side">
                    <TransitionGroup name="list-item">
                      <div
                        class="ranking-item-side clickable"
                        v-for="(activity, index) in activityRanking.slice(0, 5)"
                        :key="activity.id"
                        @click="goToActivity(activity.id)"
                      >
                        <span class="rank-num-side" :class="{ top: index < 3 }">{{
                          index + 1
                        }}</span>
                        <span class="rank-name-side">{{ activity.name }}</span>
                        <span class="rank-heat-side">{{ activity.heat }}</span>
                      </div>
                    </TransitionGroup>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div class="banner-dots">
            <span
              v-for="(banner, index) in banners"
              :key="index"
              class="dot"
              :class="{ active: currentBannerIndex === index }"
              @click="switchBanner(index)"
            ></span>
          </div>
        </section>

        <section class="club-section">
          <div class="section-header">
            <h3 class="section-title">
              <span class="title-icon">🔥</span>
              热门社团
            </h3>
            <router-link to="/clubs" class="more-link">查看更多 &gt;</router-link>
          </div>
          <TransitionGroup name="card-grid" tag="div" class="club-grid">
            <div
              class="club-card"
              v-for="club in hotClubs"
              :key="club.id"
              @click="goToClub(club.id)"
            >
              <div class="card-cover">
                <img :src="club.cover" :alt="club.name" loading="lazy" @error="onImgError" />
                <div class="card-stats">
                  <span class="stat-item">
                    <svg viewBox="0 0 24 24" width="14" height="14">
                      <path
                        fill="currentColor"
                        d="M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z"
                      />
                    </svg>
                    {{ club.views }}
                  </span>
                  <span class="stat-item">
                    <svg viewBox="0 0 24 24" width="14" height="14">
                      <path
                        fill="currentColor"
                        d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"
                      />
                    </svg>
                    {{ club.likes }}
                  </span>
                </div>
                <div class="card-duration">{{ club.memberCount }}人</div>
              </div>
              <div class="card-info">
                <h4 class="card-title">{{ club.name }}</h4>
                <div class="card-meta">
                  <img
                    :src="club.avatar"
                    class="author-avatar"
                    loading="lazy"
                    @error="onImgError"
                  />
                  <span class="author-name">{{ club.author }}</span>
                  <span class="publish-time">{{ club.time }}</span>
                </div>
                <div class="card-actions">
                  <button class="contact-btn" @click.stop="contactClub(club)">
                    <svg viewBox="0 0 24 24" width="14" height="14">
                      <path
                        fill="currentColor"
                        d="M20 2H4c-1.1 0-1.99.9-1.99 2L2 22l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zM6 9h12v2H6V9zm8 5H6v-2h8v2zm4-6H6V6h12v2z"
                      />
                    </svg>
                    联系
                  </button>
                  <button class="apply-btn" @click.stop="applyClub(club)">立即报名</button>
                </div>
              </div>
            </div>
          </TransitionGroup>
        </section>

        <section class="activity-section">
          <div class="section-header">
            <h3 class="section-title">
              <span class="title-icon">📅</span>
              最新活动
            </h3>
            <router-link to="/activities" class="more-link">查看更多 &gt;</router-link>
          </div>
          <TransitionGroup name="card-grid" tag="div" class="activity-list">
            <div
              class="activity-card"
              v-for="activity in activities"
              :key="activity.id"
              @click="goToActivity(activity.id)"
            >
              <div class="activity-cover">
                <img
                  :src="activity.cover"
                  :alt="activity.title"
                  loading="lazy"
                  @error="onImgError"
                />
                <span class="activity-status" :class="activity.statusClass">{{
                  activity.statusText
                }}</span>
              </div>
              <div class="activity-info">
                <h4 class="activity-title">{{ activity.title }}</h4>
                <p class="activity-desc">{{ activity.description }}</p>
                <div class="activity-meta">
                  <span class="meta-item">
                    <svg viewBox="0 0 24 24" width="14" height="14">
                      <path
                        fill="currentColor"
                        d="M11.99 2C6.47 2 2 6.48 2 12s4.47 10 9.99 10C17.52 22 22 17.52 22 12S17.52 2 11.99 2zM12 20c-4.42 0-8-3.58-8-8s3.58-8 8-8 8 3.58 8 8-3.58 8-8 8zm.5-13H11v6l5.25 3.15.75-1.23-4.5-2.67z"
                      />
                    </svg>
                    {{ activity.time }}
                  </span>
                  <span class="meta-item">
                    <svg viewBox="0 0 24 24" width="14" height="14">
                      <path
                        fill="currentColor"
                        d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7zm0 9.5c-1.38 0-2.5-1.12-2.5-2.5s1.12-2.5 2.5-2.5 2.5 1.12 2.5 2.5-1.12 2.5-2.5 2.5z"
                      />
                    </svg>
                    {{ activity.location || '地点待定' }}
                  </span>
                </div>
                <button
                  v-if="isLoggedIn"
                  type="button"
                  class="activity-signup-btn"
                  @click.stop="signupActivity(activity)"
                >
                  报名参加
                </button>
              </div>
            </div>
          </TransitionGroup>
        </section>

        <section
          v-if="
            isLoggedIn &&
            (recommendedActivities.length ||
              recommendedVotes.length ||
              recommendedRecruitments.length)
          "
          class="recommendation-section"
        >
          <div class="section-header">
            <h3 class="section-title">
              <span class="title-icon">✨</span>
              为你推荐
            </h3>
            <span class="recommendation-hint">基于你的兴趣标签匹配</span>
          </div>

          <div v-if="recommendedActivities.length" class="recommendation-group">
            <h4 class="recommendation-group-title">📅 活动推荐</h4>
            <div class="recommendation-list">
              <div
                v-for="item in recommendedActivities"
                :key="'activity-' + item.id"
                class="recommendation-card"
                @click="goToActivity(item.id)"
              >
                <img :src="item.cover" class="recommendation-cover" @error="onImgError" />
                <div class="recommendation-info">
                  <h5 class="recommendation-title">{{ item.title }}</h5>
                  <p class="recommendation-meta">{{ item.clubName }} · {{ item.location }}</p>
                  <div class="recommendation-tags">
                    <span v-for="tag in item.tags.slice(0, 3)" :key="tag" class="tag">{{
                      tag
                    }}</span>
                  </div>
                </div>
                <div class="recommendation-score">
                  <span class="score">{{ Math.round(item.similarity * 100) }}%</span>
                  <span class="score-label">匹配</span>
                </div>
              </div>
            </div>
          </div>

          <div v-if="recommendedRecruitments.length" class="recommendation-group">
            <h4 class="recommendation-group-title">🎯 招募推荐</h4>
            <div class="recommendation-list">
              <div
                v-for="item in recommendedRecruitments"
                :key="'recruitment-' + item.id"
                class="recommendation-card"
                @click="goToRecruitment(item.id)"
              >
                <div class="recommendation-info">
                  <h5 class="recommendation-title">{{ item.title }}</h5>
                  <p class="recommendation-meta">{{ item.clubName }} · {{ item.typeName }}</p>
                  <div class="recommendation-tags">
                    <span v-for="tag in item.tags.slice(0, 3)" :key="tag" class="tag">{{
                      tag
                    }}</span>
                  </div>
                </div>
                <div class="recommendation-score">
                  <span class="score">{{ Math.round(item.similarity * 100) }}%</span>
                  <span class="score-label">匹配</span>
                </div>
              </div>
            </div>
          </div>

          <div v-if="recommendedVotes.length" class="recommendation-group">
            <h4 class="recommendation-group-title">🗳️ 投票推荐</h4>
            <div class="recommendation-list">
              <div
                v-for="item in recommendedVotes"
                :key="'vote-' + item.id"
                class="recommendation-card"
                @click="goToVote(item.id)"
              >
                <div class="recommendation-info">
                  <h5 class="recommendation-title">{{ item.title }}</h5>
                  <p class="recommendation-meta">{{ item.clubName }}</p>
                  <div class="recommendation-tags">
                    <span v-for="tag in item.tags.slice(0, 3)" :key="tag" class="tag">{{
                      tag
                    }}</span>
                  </div>
                </div>
                <div class="recommendation-score">
                  <span class="score">{{ Math.round(item.similarity * 100) }}%</span>
                  <span class="score-label">匹配</span>
                </div>
              </div>
            </div>
          </div>
        </section>
      </template>
    </main>

    <footer class="footer">
      <div class="footer-content">
        <div class="footer-links">
          <a href="#">关于我们</a>
          <a href="#">联系方式</a>
          <a href="#">帮助中心</a>
          <a href="#">隐私政策</a>
        </div>
        <p class="copyright">© 2026 社团之家 - 让校园生活更精彩</p>
      </div>
    </footer>

    <div v-if="pairSignupVisible" class="pair-signup-overlay" @click.self="closePairSignup">
      <Transition name="modal-fade">
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
      </Transition>
    </div>
  </div>
</template>

<script>
import SiteHeader from '../components/SiteHeader.vue';
import HomeSkeleton from '../components/skeleton/HomeSkeleton.vue';
import {
  homeApi,
  activityApi,
  getCurrentUserId,
  IMG_PLACEHOLDER,
  onImgError,
  resolveBackendUrl,
  unwrapRecommendationItems,
} from '../utils/api.js';
import { recommendationApi } from '../api/index.js';

export default {
  name: 'Home',
  components: { SiteHeader, HomeSkeleton },
  data() {
    return {
      placeholder: IMG_PLACEHOLDER,
      isLoggedIn: false,
      loading: false,
      hasLoaded: false,
      currentBannerIndex: 0,
      banners: [
        { image: IMG_PLACEHOLDER, title: '春季招新进行中', desc: '百团大战，等你来战！' },
        { image: IMG_PLACEHOLDER, title: '社团文化节', desc: '精彩活动不容错过' },
        { image: IMG_PLACEHOLDER, title: '新人福利', desc: '加入社团领取专属礼包' },
      ],
      clubRanking: [],
      activityRanking: [],
      hotClubs: [],
      activities: [],
      recommendedActivities: [],
      recommendedVotes: [],
      recommendedRecruitments: [],
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
  computed: {
    currentBanner() {
      return this.banners[this.currentBannerIndex].image;
    },
    bannerTitle() {
      return this.banners[this.currentBannerIndex].title;
    },
    bannerDesc() {
      return this.banners[this.currentBannerIndex].desc;
    },
  },
  // 从个人中心/标签页返回首页时需重新拉取推荐；仅 mounted 会在同一路由复用时漏刷新
  watch: {
    $route: {
      immediate: true,
      handler(to) {
        if (to.name !== 'Home' && to.path !== '/') return;
        this.checkLoginStatus();
        if (this.isLoggedIn) {
          this.loadRecommendations();
        }
      },
    },
  },
  methods: {
    onImgError,
    formatRelative(iso) {
      if (!iso) return '';
      const d = new Date(iso);
      if (Number.isNaN(d.getTime())) return '';
      const sec = (Date.now() - d.getTime()) / 1000;
      if (sec < 60) return '刚刚';
      if (sec < 3600) return `${Math.floor(sec / 60)}分钟前`;
      if (sec < 86400) return `${Math.floor(sec / 3600)}小时前`;
      if (sec < 86400 * 7) return `${Math.floor(sec / 86400)}天前`;
      return d.toLocaleDateString();
    },
    mapClub(c) {
      const rating = c.rating != null ? Number(c.rating) : 5;
      const memberCount = c.memberCount != null ? Number(c.memberCount) : 0;
      const syntheticHeat = Math.round(rating * 2000 + memberCount * 80);
      const heatNum = c.heat != null && c.heat !== '' ? Number(c.heat) : syntheticHeat;
      const viewsNum = c.views != null && c.views !== '' ? Number(c.views) : heatNum;
      const heatDisplay = Number.isFinite(heatNum) ? heatNum : syntheticHeat;
      const viewsDisplay = Number.isFinite(viewsNum) ? viewsNum : heatDisplay;
      return {
        id: c.id,
        leaderId: c.leaderId ?? c.adminId,
        name: c.name,
        cover: c.cover ? resolveBackendUrl(c.cover) : IMG_PLACEHOLDER,
        avatar: c.logo || c.avatar ? resolveBackendUrl(c.logo || c.avatar) : IMG_PLACEHOLDER,
        author: '社团',
        views: `${viewsDisplay}`,
        likes: `${Math.round(rating * 1000)}`,
        memberCount,
        time: this.formatRelative(c.createdAt),
        heat: `${(heatDisplay / 10000).toFixed(1)}万`,
      };
    },
    mapActivity(a) {
      const st = a.status;
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
      const start = a.startTime ? new Date(a.startTime) : null;
      const timeStr =
        start && !Number.isNaN(start.getTime())
          ? `${start.getMonth() + 1}月${start.getDate()}日 ${String(start.getHours()).padStart(2, '0')}:${String(start.getMinutes()).padStart(2, '0')}`
          : '时间待定';
      return {
        id: a.id,
        title: a.title,
        description: a.description || '',
        cover: a.cover ? resolveBackendUrl(a.cover) : IMG_PLACEHOLDER,
        time: timeStr,
        location: a.location,
        statusClass,
        statusText,
        raw: a,
      };
    },
    async loadHomeData() {
      this.loading = true;
      try {
        const raw = await homeApi.overview(8, 9);
        const o = raw != null && typeof raw === 'object' && raw.data != null ? raw.data : raw || {};
        if (o.banners && o.banners.length) {
          this.banners = o.banners.map((b) => ({
            image: b.image,
            title: b.title,
            desc: b.desc,
          }));
        }
        const clubs = (o.hotClubs || []).map((c) => this.mapClub(c));
        this.hotClubs = clubs;
        if (o.clubRanking && o.clubRanking.length) {
          this.clubRanking = o.clubRanking.map((r) => ({
            id: r.id,
            name: r.name,
            heat: r.heat,
          }));
        } else {
          this.clubRanking = [...clubs]
            .sort((a, b) => Number(b.views) - Number(a.views))
            .slice(0, 8)
            .map((c) => ({ id: c.id, name: c.name, heat: c.heat }));
        }
        const records = o.recentActivities || o.activities || [];
        const mapped = records.map((a) => this.mapActivity(a));
        this.activities = mapped.slice(0, 6);
        if (o.activityRanking && o.activityRanking.length) {
          this.activityRanking = o.activityRanking.map((r) => ({
            id: r.id,
            name: r.name,
            heat: r.heat,
          }));
        } else {
          this.activityRanking = [...mapped]
            .sort((x, y) => (y.raw.views || 0) - (x.raw.views || 0))
            .slice(0, 8)
            .map((x) => ({
              id: x.id,
              name: x.title,
              heat: `${((x.raw.views || 0) / 10000).toFixed(1)}万`,
            }));
        }
        this.hasLoaded = true;
      } catch (e) {
        console.error(e);
      } finally {
        this.loading = false;
      }
    },
    async loadRecommendations() {
      if (!this.isLoggedIn) return;
      try {
        const [activitiesRes, votesRes, recruitmentsRes] = await Promise.all([
          recommendationApi.getActivities(6),
          recommendationApi.getVotes(6),
          recommendationApi.getRecruitments(6),
        ]);
        this.recommendedActivities = unwrapRecommendationItems(activitiesRes, 'activities').map(
          this.mapRecommendedActivity,
        );
        this.recommendedVotes = unwrapRecommendationItems(votesRes, 'votes').map(
          this.mapRecommendedVote,
        );
        this.recommendedRecruitments = unwrapRecommendationItems(
          recruitmentsRes,
          'recruitments',
        ).map(this.mapRecommendedRecruitment);
      } catch (e) {
        console.warn('Failed to load recommendations:', e);
      }
    },
    mapRecommendedActivity(a) {
      return {
        id: a.id,
        title: a.title,
        description: a.content || '',
        cover: a.imageUrl ? resolveBackendUrl(a.imageUrl) : IMG_PLACEHOLDER,
        clubName: a.clubName || '',
        time: a.activityTime ? new Date(a.activityTime).toLocaleString() : '时间待定',
        location: a.location || '地点待定',
        tags: a.tags || [],
        similarity: a.similarity || 0,
      };
    },
    mapRecommendedVote(v) {
      return {
        id: v.id,
        title: v.title,
        description: v.description || '',
        clubName: v.clubName || '',
        tags: v.tags || [],
        similarity: v.similarity || 0,
        endTime: v.endTime ? new Date(v.endTime).toLocaleString() : '无截止',
      };
    },
    mapRecommendedRecruitment(r) {
      return {
        id: r.id,
        title: r.title,
        description: r.content || '',
        clubName: r.clubName || '',
        tags: r.tags || [],
        similarity: r.similarity || 0,
        typeName: r.typeName || '其他',
        deadline: r.deadline ? new Date(r.deadline).toLocaleString() : '无截止',
      };
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
        await this.loadHomeData();
      } catch (err) {
        alert(err.message || '报名失败');
      } finally {
        this.pairSignupLoading = false;
      }
    },
    switchBanner(index) {
      this.currentBannerIndex = index;
    },
    goToClub(id) {
      this.$router.push(`/club/${id}`);
    },
    contactClub(club) {
      const uid = getCurrentUserId();
      if (!uid) {
        alert('请先登录');
        this.$router.push('/login');
        return;
      }
      this.$router.push({ path: '/chat', query: { peerClub: String(club.id) } });
    },
    applyClub(club) {
      this.$router.push(`/club-apply/${club.id}`);
    },
    goToActivity(id) {
      this.$router.push('/activity/' + id);
    },
    goToRecruitment(id) {
      this.$router.push('/recruitment/' + id);
    },
    goToVote(id) {
      this.$router.push('/vote/' + id);
    },
    checkLoginStatus() {
      this.isLoggedIn = localStorage.getItem('isLoggedIn') === 'true';
    },
  },
  mounted() {
    this.checkLoginStatus();
    this.loadHomeData();
    setInterval(() => {
      this.currentBannerIndex = (this.currentBannerIndex + 1) % this.banners.length;
    }, 5000);
  },
};
</script>

<style scoped>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.home-container {
  min-height: 100vh;
  background-color: #f4f4f4;
  font-family:
    -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  overflow-x: hidden;
}

.banner-fade-enter-active,
.banner-fade-leave-active {
  transition: opacity 0.5s ease;
}

.banner-fade-enter-from,
.banner-fade-leave-to {
  opacity: 0;
}

.modal-fade-enter-active,
.modal-fade-leave-active {
  transition: all 0.3s ease;
}

.modal-fade-enter-from,
.modal-fade-leave-to {
  opacity: 0;
  transform: scale(0.95);
}

.list-item-enter-active,
.list-item-leave-active {
  transition: all 0.3s ease;
}

.list-item-enter-from,
.list-item-leave-to {
  opacity: 0;
  transform: translateX(-20px);
}

.list-item-move {
  transition: transform 0.3s ease;
}

.card-grid-enter-active {
  transition: all 0.4s ease-out;
}

.card-grid-leave-active {
  transition: all 0.3s ease-in;
  position: absolute;
}

.card-grid-enter-from {
  opacity: 0;
  transform: translateY(30px);
}

.card-grid-leave-to {
  opacity: 0;
  transform: scale(0.95);
}

.card-grid-move {
  transition: all 0.4s ease;
}

/* 主内容区 */
.main-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 16px 16px 24px;
  width: 100%;
  box-sizing: border-box;
}

/* 轮播图区域 */
.banner-section {
  margin-bottom: 30px;
}

.banner-wrapper {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(240px, 300px);
  gap: 16px;
  align-items: stretch;
  min-height: 220px;
}

.banner-main {
  min-width: 0;
  border-radius: 12px;
  overflow: hidden;
}

.banner-slide {
  width: 100%;
  min-height: 220px;
  height: 100%;
  position: relative;
  background: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 50%, #90caf9 100%);
}

.banner-bg-img {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  opacity: 0;
}

.banner-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 16px 20px 20px;
  background: linear-gradient(transparent, rgba(0, 0, 0, 0.72));
  color: white;
}

.banner-overlay h2 {
  font-size: clamp(18px, 2.5vw, 28px);
  margin-bottom: 6px;
  line-height: 1.25;
}

.banner-overlay p {
  font-size: 14px;
  opacity: 0.9;
}

.banner-side {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
  min-height: 220px;
}

.ranking-side-wrapper {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  flex: 1;
  min-height: 0;
}

.ranking-box-side {
  min-width: 0;
  background: white;
  border-radius: 12px;
  padding: 10px 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.ranking-list-side {
  display: flex;
  flex-direction: column;
  gap: 6px;
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
}

.ranking-header-side {
  margin-bottom: 8px;
  padding-bottom: 8px;
  border-bottom: 1px solid #f0f0f0;
}

.ranking-title-side {
  font-size: 13px;
  font-weight: 600;
  color: #333;
}

.ranking-item-side {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 0;
  cursor: pointer;
  transition: all 0.3s;
}
.ranking-item-side.clickable:hover {
  background: linear-gradient(135deg, rgba(100, 181, 246, 0.1) 0%, rgba(66, 165, 245, 0.2) 100%);
  margin: 0 -8px;
  padding: 4px 8px;
  border-radius: 4px;
}

.rank-num-side {
  width: 16px;
  height: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 10px;
  font-weight: 600;
  color: #999;
  background: #f0f0f0;
  border-radius: 3px;
  flex-shrink: 0;
}

.rank-num-side.top {
  color: white;
  background: linear-gradient(135deg, #ff6b9d 0%, #ff8e53 100%);
}

.rank-name-side {
  flex: 1;
  font-size: 12px;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.rank-heat-side {
  font-size: 10px;
  color: #999;
  flex-shrink: 0;
}

.banner-dots {
  display: flex;
  justify-content: center;
  gap: 8px;
  margin-top: 16px;
}

.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #ddd;
  cursor: pointer;
  transition: all 0.3s;
}

.dot.active {
  width: 24px;
  border-radius: 4px;
  background: linear-gradient(135deg, #ff6b9d 0%, #00a1d6 100%);
}

/* 区块标题 */
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-title {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  display: flex;
  align-items: center;
  gap: 8px;
}

.title-icon {
  font-size: 22px;
}

.more-link {
  color: #999;
  text-decoration: none;
  font-size: 14px;
  transition: color 0.3s;
}

.more-link:hover {
  color: #ff6b9d;
}

/* 社团卡片网格 */
.club-section {
  margin-bottom: 40px;
}

.club-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 20px;
}

.club-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.club-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.card-cover {
  position: relative;
  aspect-ratio: 16/10;
  overflow: hidden;
  background: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 50%, #90caf9 100%);
}

.card-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
  opacity: 0;
}

.club-card:hover .card-cover img {
  transform: scale(1.05);
}

.card-stats {
  position: absolute;
  bottom: 8px;
  left: 8px;
  display: flex;
  gap: 12px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  color: white;
  font-size: 12px;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.5);
}

.card-duration {
  position: absolute;
  bottom: 8px;
  right: 8px;
  background: rgba(0, 0, 0, 0.7);
  color: white;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.card-info {
  padding: 12px;
}

.card-title {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.4;
}

.card-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.card-actions {
  display: flex;
  gap: 8px;
}

.contact-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  padding: 8px 12px;
  background: white;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 12px;
  color: #666;
  cursor: pointer;
  transition: all 0.3s;
}

.contact-btn:hover {
  border-color: #ff6b9d;
  color: #ff6b9d;
}

.apply-btn {
  flex: 1;
  padding: 8px 12px;
  background: linear-gradient(135deg, #ff6b9d 0%, #ff8e53 100%);
  border: none;
  border-radius: 8px;
  font-size: 12px;
  color: white;
  cursor: pointer;
  transition: all 0.3s;
}

.apply-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(255, 107, 157, 0.3);
}

.author-avatar {
  width: 24px;
  height: 24px;
  border-radius: 50%;
}

.author-name {
  font-size: 12px;
  color: #999;
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.publish-time {
  font-size: 12px;
  color: #ccc;
}

/* 活动区 */
.activity-section {
  margin-bottom: 40px;
}

.activity-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.activity-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.activity-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.activity-cover {
  position: relative;
  aspect-ratio: 16/9;
  overflow: hidden;
  background: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 50%, #90caf9 100%);
}

.activity-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  opacity: 0;
}

.activity-status {
  position: absolute;
  top: 12px;
  right: 12px;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.activity-status.ongoing {
  background: #ff6b9d;
  color: white;
}

.activity-status.upcoming {
  background: #00a1d6;
  color: white;
}

.activity-status.ended {
  background: #999;
  color: white;
}

.activity-signup-btn {
  margin-top: 12px;
  width: 100%;
  padding: 8px 12px;
  border: none;
  border-radius: 8px;
  font-size: 13px;
  color: white;
  cursor: pointer;
  background: linear-gradient(135deg, #ff6b9d 0%, #ff8e53 100%);
}

.activity-signup-btn:hover {
  opacity: 0.92;
}

.activity-info {
  padding: 16px;
}

.activity-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}

.activity-desc {
  font-size: 13px;
  color: #999;
  margin-bottom: 12px;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.activity-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 16px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #999;
  min-width: 0;
}

/* 双人报名弹层 */
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
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 16px 48px rgba(0, 0, 0, 0.18);
}

.pair-signup-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}

.pair-signup-sub {
  font-size: 14px;
  color: #666;
  margin-bottom: 12px;
  line-height: 1.4;
}

.pair-signup-hint {
  font-size: 12px;
  color: #888;
  line-height: 1.5;
  margin-bottom: 16px;
}

.pair-signup-label {
  display: block;
  font-size: 13px;
  color: #555;
  margin-bottom: 6px;
  margin-top: 12px;
}

.pair-signup-label:first-of-type {
  margin-top: 0;
}

.pair-signup-input {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
  box-sizing: border-box;
}

.pair-signup-input:focus {
  outline: none;
  border-color: #00a1d6;
}

.pair-signup-actions {
  display: flex;
  gap: 12px;
  margin-top: 20px;
  justify-content: flex-end;
}

.pair-signup-cancel {
  padding: 10px 18px;
  border-radius: 8px;
  border: 1px solid #ddd;
  background: #fff;
  font-size: 14px;
  cursor: pointer;
  color: #666;
}

.pair-signup-submit {
  padding: 10px 18px;
  border-radius: 8px;
  border: none;
  background: linear-gradient(135deg, #ff6b9d 0%, #ff8e53 100%);
  color: #fff;
  font-size: 14px;
  cursor: pointer;
}

.pair-signup-submit:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 页脚 */
.footer {
  background: #333;
  padding: 30px 0;
}

.footer-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 20px;
  text-align: center;
}

.footer-links {
  display: flex;
  justify-content: center;
  gap: 30px;
  margin-bottom: 16px;
}

.footer-links a {
  color: #999;
  text-decoration: none;
  font-size: 14px;
  transition: color 0.3s;
}

.footer-links a:hover {
  color: #ff6b9d;
}

.copyright {
  color: #666;
  font-size: 12px;
}

/* 个性化推荐区域 */
.recommendation-section {
  margin-bottom: 40px;
}

.recommendation-hint {
  font-size: 12px;
  color: #999;
  font-weight: normal;
}

.recommendation-group {
  margin-bottom: 24px;
}

.recommendation-group-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
}

.recommendation-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.recommendation-card {
  display: flex;
  align-items: center;
  gap: 16px;
  background: white;
  border-radius: 12px;
  padding: 12px;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.recommendation-card:hover {
  transform: translateX(4px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.recommendation-cover {
  width: 80px;
  height: 60px;
  object-fit: cover;
  border-radius: 8px;
  flex-shrink: 0;
}

.recommendation-info {
  flex: 1;
  min-width: 0;
}

.recommendation-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.recommendation-meta {
  font-size: 12px;
  color: #999;
  margin-bottom: 6px;
}

.recommendation-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.tag {
  display: inline-block;
  padding: 2px 8px;
  background: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%);
  color: #1976d2;
  border-radius: 12px;
  font-size: 11px;
}

.recommendation-score {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 8px 12px;
  background: linear-gradient(135deg, #ff6b9d 0%, #ff8e53 100%);
  border-radius: 8px;
  flex-shrink: 0;
}

.recommendation-score .score {
  font-size: 16px;
  font-weight: 700;
  color: white;
}

.recommendation-score .score-label {
  font-size: 10px;
  color: rgba(255, 255, 255, 0.8);
}

/* 响应式设计 */
@media (max-width: 1100px) {
  .banner-wrapper {
    grid-template-columns: minmax(0, 1fr) minmax(200px, 260px);
  }
}

@media (max-width: 992px) {
  .banner-wrapper {
    grid-template-columns: 1fr;
    min-height: 200px;
  }

  .banner-slide {
    min-height: 200px;
  }

  .banner-side {
    display: none;
  }
}

@media (max-width: 768px) {
  .main-content {
    padding: 12px 12px 20px;
  }
}
</style>
