<template>
  <div class="page">
    <SiteHeader />
    <main class="main" v-if="club">
      <div class="hero">
        <img :src="club.cover || ph" class="hero-img" alt="" @error="onImgError" />
        <div class="hero-overlay">
          <img :src="club.logo || ph" class="logo" alt="" @error="onImgError" />
          <div class="hero-info">
            <h1>{{ club.name }}</h1>
            <div class="hero-meta">
              <span class="meta-item">
                <svg viewBox="0 0 24 24" width="14" height="14">
                  <path
                    fill="currentColor"
                    d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"
                  />
                </svg>
                {{ typeName }}
              </span>
              <span class="meta-item">
                <svg viewBox="0 0 24 24" width="14" height="14">
                  <path
                    fill="currentColor"
                    d="M16 11c1.66 0 2.99-1.34 2.99-3S17.66 5 16 5c-1.66 0-3 1.34-3 3s1.34 3 3 3zm-8 0c1.66 0 2.99-1.34 2.99-3S9.66 5 8 5C6.34 5 5 6.34 5 8s1.34 3 3 3zm0 2c-2.33 0-7 1.17-7 3.5V19h14v-2.5c0-2.33-4.67-3.5-7-3.5zm8 0c-.29 0-.62.02-.97.05 1.16.84 1.97 1.97 1.97 3.45V19h6v-2.5c0-2.33-4.67-3.5-7-3.5z"
                  />
                </svg>
                {{ club.memberCount || 0 }} 人
              </span>
              <span class="meta-item">
                <svg viewBox="0 0 24 24" width="14" height="14">
                  <path
                    fill="currentColor"
                    d="M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z"
                  />
                </svg>
                {{ club.rating ?? '—' }}
              </span>
            </div>
            <div v-if="club.teacher" class="teacher">指导老师：{{ club.teacher }}</div>
          </div>
        </div>
      </div>

      <div v-if="clubTags.length" class="tags-section">
        <span class="tag" v-for="tag in clubTags" :key="tag">{{ tag }}</span>
      </div>

      <section class="section">
        <h2>社团简介</h2>
        <p class="text">{{ club.description || '暂无简介' }}</p>
      </section>

      <section v-if="club.requirements" class="section">
        <h2>招新要求</h2>
        <p class="text">{{ club.requirements }}</p>
      </section>

      <section v-if="club.activityPlan" class="section">
        <h2>活动计划</h2>
        <p class="text">{{ club.activityPlan }}</p>
      </section>

      <section class="section">
        <h2>AI 运营总结</h2>
        <div v-if="!aiSummaryLoaded && !loadingAiSummary" class="lazy-load-prompt">
          <p class="prompt-text">点击按钮查看AI生成的运营总结</p>
          <button
            type="button"
            class="btn primary-btn"
            @click="loadAiSummary"
            :disabled="loadingAiSummary"
          >
            加载AI总结
          </button>
        </div>
        <div v-if="loadingAiSummary" class="muted">加载中…</div>
        <div v-else-if="aiSummary" class="ai-summary-content">
          <div class="summary-actions">
            <div class="summary-meta" v-if="aiSummary.generatedAt">
              <span class="summary-type">{{ getSummaryTypeLabel(aiSummary.summaryType) }}</span>
              <span class="summary-time">生成于 {{ formatTime(aiSummary.generatedAt) }}</span>
            </div>
            <button
              v-if="canManage"
              type="button"
              class="refresh-btn"
              @click="refreshAiSummary"
              :disabled="generatingSummary"
            >
              {{ generatingSummary ? '生成中…' : '刷新总结' }}
            </button>
          </div>
          <div class="summary-body" v-html="renderSummary(aiSummary.summaryContent)"></div>
          <button
            v-if="canManage"
            type="button"
            class="btn primary-btn"
            @click="generateAiSummary"
            :disabled="generatingSummary"
          >
            {{ generatingSummary ? '生成中…' : '生成AI总结' }}
          </button>
        </div>
        <div v-else-if="aiSummaryLoaded" class="muted">
          暂无AI总结{{ canManage ? '，请点击上方按钮生成' : '' }}
        </div>
      </section>

      <section class="section" v-if="activities.length">
        <div class="section-header">
          <h2>社团活动（{{ activities.length }}）</h2>
          <router-link
            v-if="activities.length > 4"
            :to="'/activities?clubId=' + club.id"
            class="view-more"
          >
            查看全部 →
          </router-link>
        </div>
        <div class="activity-grid">
          <div
            v-for="act in activities.slice(0, 4)"
            :key="act.id"
            class="activity-card"
            @click="goToActivity(act.id)"
          >
            <div class="activity-status" :class="'status-' + act.status">
              {{ getStatusText(act.status) }}
            </div>
            <h4 class="activity-title">{{ act.title }}</h4>
            <div class="activity-info">
              <span v-if="act.startTime">
                <svg viewBox="0 0 24 24" width="12" height="12">
                  <path
                    fill="currentColor"
                    d="M11.99 2C6.47 2 2 6.48 2 12s4.47 10 9.99 10C17.52 22 22 17.52 22 12S17.52 2 11.99 2zM12 20c-4.42 0-8-3.58-8-8s3.58-8 8-8 8 3.58 8 8-3.58 8-8 8zm.5-13H11v6l5.25 3.15.75-1.23-4.5-2.67z"
                  />
                </svg>
                {{ formatActivityTime(act.startTime) }}
              </span>
              <span v-if="act.location">
                <svg viewBox="0 0 24 24" width="12" height="12">
                  <path
                    fill="currentColor"
                    d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7zm0 9.5c-1.38 0-2.5-1.12-2.5-2.5s1.12-2.5 2.5-2.5 2.5 1.12 2.5 2.5-1.12 2.5-2.5 2.5z"
                  />
                </svg>
                {{ act.location }}
              </span>
            </div>
          </div>
        </div>
      </section>
      <section v-else class="section">
        <div class="section-header">
          <h2>社团活动</h2>
        </div>
        <div class="empty-state">暂无活动</div>
      </section>

      <section class="section" v-if="members.length">
        <div class="section-header">
          <h2>成员列表（{{ members.length }}）</h2>
        </div>
        <div class="member-grid">
          <div v-for="m in members.slice(0, 8)" :key="m.id" class="member-card">
            <div class="member-avatar">
              <img v-if="m.userAvatar" :src="m.userAvatar" :alt="m.userName" @error="onImgError" />
              <div v-else class="avatar-placeholder">{{ (m.userName || 'U')[0] }}</div>
            </div>
            <div class="member-info">
              <span class="member-name">{{ m.userName || '用户' + m.userId }}</span>
              <span class="member-role" :class="'role-' + m.role">{{ roleText(m.role) }}</span>
            </div>
          </div>
        </div>
        <div v-if="members.length > 8" class="more-members">
          还有 {{ members.length - 8 }} 位成员...
        </div>
      </section>

      <section class="section">
        <h2>相似社团</h2>
        <p class="similar-hint muted">
          与「全部社团 → 社团匹配」相同：按两社<strong>在社成员</strong>兴趣标签（user_tag）两两 Jaccard
          的<strong>平均值</strong>排序。
        </p>
        <div v-if="!similarClubsLoaded && !loadingSimilar" class="lazy-load-prompt">
          <p class="prompt-text">点击按钮查看与本社团相似的其他社团</p>
          <button
            type="button"
            class="btn primary-btn"
            @click="loadSimilarClubs"
            :disabled="loadingSimilar"
          >
            加载相似社团
          </button>
        </div>
        <div v-if="loadingSimilar" class="loading-state">加载中...</div>
        <template v-else-if="similarClubsLoaded">
          <div v-if="similarClubs.length" class="section-header">
            <span class="section-count">为您找到 {{ similarClubs.length }} 个相似社团</span>
            <router-link to="/clubs" class="view-more">查看更多 →</router-link>
          </div>
          <div v-else class="empty-state">暂无相似社团</div>
          <div class="similar-clubs-grid">
            <div
              v-for="sc in similarClubs"
              :key="sc.id"
              class="similar-club-card"
              @click="$router.push('/club/' + sc.id)"
            >
              <div class="club-logo">
                <img v-if="sc.logo" :src="sc.logo" :alt="sc.name" @error="onImgError" />
                <div v-else class="logo-placeholder">{{ (sc.name || 'C')[0] }}</div>
              </div>
              <div class="club-info">
                <h4 class="club-name">{{ sc.name }}</h4>
                <p v-if="sc.description" class="club-desc">{{ sc.description }}</p>
                <div v-if="sc.tags && sc.tags.length" class="club-tags">
                  <span v-for="tag in sc.tags.slice(0, 2)" :key="tag" class="club-tag">{{
                    tag
                  }}</span>
                </div>
              </div>
              <div class="similarity-badge">
                <span class="similarity-value">{{ Math.round(sc.similarity * 100) }}%</span>
                <span class="similarity-label">成员匹配度</span>
              </div>
            </div>
          </div>
        </template>
      </section>

      <section v-if="canManage" class="section admin">
        <h2>社团管理</h2>
        <div class="admin-actions">
          <button type="button" class="admin-btn" @click="showEditModal = true">
            <svg viewBox="0 0 24 24" width="18" height="18">
              <path
                fill="currentColor"
                d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z"
              />
            </svg>
            编辑社团信息
          </button>
        </div>
        <div class="admin-block">
          <h3>发布活动</h3>
          <div class="grid-form">
            <label>标题</label>
            <input v-model="actForm.title" type="text" class="inp" placeholder="活动标题" />
            <label>简介</label>
            <textarea v-model="actForm.description" class="txa" rows="2" placeholder="简短介绍" />
            <label>地点</label>
            <input v-model="actForm.location" type="text" class="inp" placeholder="活动地点" />
            <label>人数上限</label>
            <input v-model.number="actForm.maxParticipants" type="number" min="1" class="inp" />
            <label>开始时间</label>
            <input v-model="actForm.startTime" type="datetime-local" class="inp" />
            <label>结束时间</label>
            <input v-model="actForm.endTime" type="datetime-local" class="inp" />
          </div>
          <button
            type="button"
            class="btn primary sm"
            :disabled="actSubmitting"
            @click="submitActivity"
          >
            {{ actSubmitting ? '提交中…' : '发布活动' }}
          </button>
        </div>
        <div class="admin-block">
          <h3>入社申请审核</h3>
          <p v-if="!pendingApplies.length" class="muted">暂无待审核申请</p>
          <ul v-else class="apply-list">
            <li v-for="a in pendingApplies" :key="a.id" class="apply-row">
              <div class="apply-info">
                <strong>{{ a.studentName || '申请人' }}</strong>
                <span class="subline"
                  >学号 {{ a.studentStudentId || '—' }} · 手机 {{ a.phone || '—' }}</span
                >
                <p class="intro">{{ a.selfIntro || '无自我介绍' }}</p>
                <span v-if="a.createdAt" class="apply-time"
                  >申请时间：{{ formatTime(a.createdAt) }}</span
                >
              </div>
              <div class="apply-actions">
                <button type="button" class="btn primary sm" @click="approveApply(a.id)">
                  通过
                </button>
                <button type="button" class="btn ghost sm" @click="rejectApply(a.id)">拒绝</button>
              </div>
            </li>
          </ul>
        </div>
      </section>

      <div class="actions">
        <router-link v-if="!isClubMember" :to="'/club-apply/' + club.id" class="btn primary"
          >申请加入</router-link
        >
        <router-link :to="chatTo" class="btn">社团间联系</router-link>
        <router-link to="/clubs" class="btn ghost">返回列表</router-link>
      </div>
    </main>
    <div v-else-if="err" class="err">{{ err }}</div>
    <div v-else class="loading">加载中…</div>

    <div v-if="showEditModal" class="modal" @click.self="showEditModal = false">
      <div class="modal-content">
        <h3>编辑社团信息</h3>
        <form @submit.prevent="saveClubInfo" class="edit-form">
          <div class="form-group">
            <label>社团名称</label>
            <input v-model="editForm.name" type="text" class="inp" required />
          </div>
          <div class="form-group">
            <label>社团简介</label>
            <textarea v-model="editForm.description" class="txa" rows="3"></textarea>
          </div>
          <div class="form-group">
            <label>招新要求</label>
            <textarea v-model="editForm.requirements" class="txa" rows="2"></textarea>
          </div>
          <div class="form-group">
            <label>指导老师</label>
            <input v-model="editForm.teacher" type="text" class="inp" />
          </div>
          <div class="form-group">
            <label>封面图片URL</label>
            <input v-model="editForm.cover" type="text" class="inp" placeholder="图片URL" />
          </div>
          <div class="form-group">
            <label>Logo图片URL</label>
            <input v-model="editForm.logo" type="text" class="inp" placeholder="Logo URL" />
          </div>
          <div class="form-actions">
            <button type="button" class="btn ghost" @click="showEditModal = false">取消</button>
            <button type="submit" class="btn primary" :disabled="saving">
              {{ saving ? '保存中...' : '保存' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script>
import SiteHeader from '../components/SiteHeader.vue';
import {
  clubApi,
  clubTypeApi,
  clubMemberApi,
  clubHiddenTagApi,
  applicationApi,
  activityApi,
  clubAiSummaryApi,
  getCurrentUserId,
  getCurrentAccountUserId,
  IMG_PLACEHOLDER,
  onImgError,
} from '../utils/api.js';

export default {
  name: 'ClubDetail',
  components: { SiteHeader },
  data() {
    return {
      ph: IMG_PLACEHOLDER,
      club: null,
      typeName: '',
      members: [],
      activities: [],
      clubTags: [],
      applies: [],
      err: '',
      actForm: {
        title: '',
        description: '',
        location: '',
        maxParticipants: 50,
        startTime: '',
        endTime: '',
      },
      actSubmitting: false,
      aiSummary: null,
      loadingAiSummary: false,
      generatingSummary: false,
      aiSummaryLoaded: false,
      showEditModal: false,
      editForm: {
        name: '',
        description: '',
        requirements: '',
        teacher: '',
        cover: '',
        logo: '',
      },
      saving: false,
      similarClubs: [],
      loadingSimilar: false,
      similarClubsLoaded: false,
    };
  },
  computed: {
    chatTo() {
      if (this.club?.id) {
        return { path: '/chat', query: { peerClub: String(this.club.id) } };
      }
      return '/chat';
    },
    isClubMember() {
      const uid = getCurrentUserId();
      return !!(uid && this.members.some((m) => Number(m.userId) === Number(uid)));
    },
    isLeader() {
      const uid = getCurrentUserId();
      return !!(uid && this.club && Number(this.club.leaderId) === Number(uid));
    },
    isAdmin() {
      const role = Number(localStorage.getItem('userRole') || '0');
      return role >= 2;
    },
    canManage() {
      return this.isLeader || this.isAdmin;
    },
    pendingApplies() {
      return (this.applies || []).filter((a) => a.status === 0);
    },
  },
  methods: {
    onImgError,
    roleText(r) {
      if (r === 3) return '负责人';
      if (r === 2) return '管理员';
      return '成员';
    },
    getSummaryTypeLabel(type) {
      const map = { monthly: '月度总结', quarterly: '季度总结', annual: '年度总结' };
      return map[type] || '运营总结';
    },
    getStatusText(status) {
      const map = { 0: '草稿', 1: '进行中', 2: '已结束' };
      return map[status] || '未知';
    },
    formatTime(iso) {
      if (!iso) return '';
      return new Date(iso).toLocaleString();
    },
    formatActivityTime(iso) {
      if (!iso) return '';
      const d = new Date(iso);
      return d.toLocaleDateString('zh-CN', {
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
      });
    },
    renderSummary(content) {
      if (!content) return '';
      return content
        .replace(/\*\*(.+?)\*\*/g, '<h3>$1</h3>')
        .replace(/^- (.+)$/gm, '<li>$1</li>')
        .replace(/(<li>.*<\/li>)/gs, '<ul>$1</ul>')
        .replace(/\n\n/g, '</p><p>')
        .replace(/^(.+)$/gm, (match) => {
          if (match.startsWith('<')) return match;
          return match;
        });
    },
    goToActivity(id) {
      this.$router.push('/activity/' + id);
    },
    async load() {
      const id = this.$route.params.id;
      if (!id) {
        this.err = '缺少社团 ID';
        return;
      }
      try {
        const [cRes, tRes, mRes, aRes] = await Promise.all([
          clubApi.getById(id),
          clubTypeApi.list(),
          clubMemberApi.listByClub(id).catch(() => []),
          activityApi.getByClubId(id).catch(() => []),
        ]);
        this.club = cRes.data;
        const types = tRes.data || [];
        const t = types.find((x) => x.id === this.club.typeId);
        this.typeName = t?.name || '未分类';
        this.members = Array.isArray(mRes) ? mRes : mRes.data || [];
        this.activities = Array.isArray(aRes) ? aRes : aRes.data || [];

        if (this.canManage) {
          const ar = await applicationApi.getByClubId(id);
          this.applies = ar.data || [];
          try {
            const tagsRes = await clubHiddenTagApi.getVisible(id);
            this.clubTags = Array.isArray(tagsRes) ? tagsRes : [];
          } catch (e) {
            console.warn('获取标签失败', e);
            this.clubTags = [];
          }
        } else {
          this.applies = [];
          this.clubTags = [];
        }
      } catch (e) {
        this.err = e.message || '加载失败';
      }
    },
    async loadAiSummary() {
      if (!this.club?.id) return;
      this.loadingAiSummary = true;
      try {
        const res = await clubAiSummaryApi.get(this.club.id);
        const data = res?.data || res;
        if (data && data.hasSummary) {
          this.aiSummary = data;
        } else {
          this.aiSummary = null;
        }
        this.aiSummaryLoaded = true;
      } catch (e) {
        console.error('加载AI总结失败', e);
        this.aiSummary = null;
        this.aiSummaryLoaded = true;
      } finally {
        this.loadingAiSummary = false;
      }
    },
    async loadSimilarClubs() {
      if (!this.club?.id) return;
      this.loadingSimilar = true;
      try {
        const res = await clubApi.getSimilarClubs(this.club.id);
        const data = Array.isArray(res) ? res : [];
        this.similarClubs = data.slice(0, 4);
        this.similarClubsLoaded = true;
      } catch (e) {
        console.warn('加载相似社团失败', e);
        this.similarClubs = [];
        this.similarClubsLoaded = true;
      } finally {
        this.loadingSimilar = false;
      }
    },
    async generateAiSummary() {
      if (!this.club?.id) return;
      this.generatingSummary = true;
      try {
        const res = await clubAiSummaryApi.generate(this.club.id, 'monthly');
        const data = res.data || res;
        if (data.success && data.summaryContent) {
          this.aiSummary = data;
        } else {
          alert(data.message || '生成失败');
        }
      } catch (e) {
        alert(e.message || '生成失败');
      } finally {
        this.generatingSummary = false;
      }
    },
    async refreshAiSummary() {
      if (!confirm('确定刷新AI总结？这将重新生成新的总结内容。')) return;
      this.generatingSummary = true;
      try {
        const res = await clubAiSummaryApi.refresh(this.club.id, 'monthly');
        const data = res.data || res;
        if (data.success && data.summaryContent) {
          this.aiSummary = data;
        } else {
          alert(data.message || '刷新失败');
        }
      } catch (e) {
        alert(e.message || '刷新失败');
      } finally {
        this.generatingSummary = false;
      }
    },
    padLocalDateTime(s) {
      if (!s) return null;
      return s.length === 16 ? `${s}:00` : s;
    },
    async submitActivity() {
      if (!this.actForm.title.trim()) {
        alert('请填写活动标题');
        return;
      }
      this.actSubmitting = true;
      try {
        await activityApi.create({
          clubId: this.club.id,
          title: this.actForm.title.trim(),
          description: this.actForm.description || '',
          content: this.actForm.description || '',
          location: this.actForm.location || null,
          maxParticipants: Number(this.actForm.maxParticipants) || 50,
          startTime: this.padLocalDateTime(this.actForm.startTime),
          endTime: this.padLocalDateTime(this.actForm.endTime),
        });
        alert('已提交，等待平台管理员审核通过后将在前台展示');
        this.actForm = {
          title: '',
          description: '',
          location: '',
          maxParticipants: 50,
          startTime: '',
          endTime: '',
        };
        await this.load();
      } catch (e) {
        alert(e.message || '发布失败');
      } finally {
        this.actSubmitting = false;
      }
    },
    async approveApply(applyId) {
      try {
        await applicationApi.approve(applyId);
        alert('已通过');
        await this.load();
      } catch (e) {
        alert(e.message || '操作失败');
      }
    },
    async rejectApply(applyId) {
      const comment = window.prompt('拒绝原因（可选）', '') ?? '';
      try {
        await applicationApi.reject(applyId, comment || '');
        alert('已拒绝');
        await this.load();
      } catch (e) {
        alert(e.message || '操作失败');
      }
    },
    openEditModal() {
      this.editForm = {
        name: this.club.name || '',
        description: this.club.description || '',
        requirements: this.club.requirements || '',
        teacher: this.club.teacher || '',
        cover: this.club.cover || '',
        logo: this.club.logo || '',
      };
      this.showEditModal = true;
    },
    async saveClubInfo() {
      this.saving = true;
      try {
        await clubApi.update({
          id: this.club.id,
          name: this.editForm.name,
          description: this.editForm.description,
          requirements: this.editForm.requirements,
          teacher: this.editForm.teacher,
          cover: this.editForm.cover,
          logo: this.editForm.logo,
        });
        alert('保存成功');
        this.showEditModal = false;
        await this.load();
      } catch (e) {
        alert(e.message || '保存失败');
      } finally {
        this.saving = false;
      }
    },
  },
  mounted() {
    this.load();
  },
  watch: {
    '$route.params.id'() {
      this.load();
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
  max-width: 900px;
  margin: 0 auto;
  padding: 20px 16px 48px;
}
.hero {
  position: relative;
  border-radius: 16px;
  overflow: hidden;
  margin-bottom: 16px;
  box-shadow: var(--shadow-card);
}
.hero-img {
  width: 100%;
  height: 240px;
  object-fit: cover;
  display: block;
}
.hero-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  align-items: flex-end;
  gap: 20px;
  padding: 24px;
  background: linear-gradient(transparent, rgba(0, 0, 0, 0.8));
  color: #fff;
}
.logo {
  width: 80px;
  height: 80px;
  border-radius: 16px;
  object-fit: cover;
  border: 4px solid #fff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}
.hero-info {
  flex: 1;
}
.hero-info h1 {
  font-size: 26px;
  margin-bottom: 8px;
  font-weight: 700;
}
.hero-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  font-size: 14px;
  opacity: 0.95;
}
.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
}
.teacher {
  margin-top: 8px;
  font-size: 13px;
  opacity: 0.85;
}
.tags-section {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 16px;
}
.tag {
  padding: 8px 16px;
  background: rgba(91, 127, 201, 0.12);
  color: var(--recruit-primary);
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
}
.section {
  background: var(--color-bg-card);
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 16px;
  box-shadow: var(--shadow-card);
}
.section.admin {
  border: 2px solid rgba(255, 107, 157, 0.3);
}
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.section h2 {
  font-size: 18px;
  color: var(--color-text-primary);
  font-weight: 600;
  margin: 0;
}
.view-more {
  font-size: 14px;
  color: var(--recruit-primary);
  text-decoration: none;
  transition: color var(--transition-fast);
}
.view-more:hover {
  color: var(--recruit-primary-hover);
}
.refresh-btn {
  padding: 6px 14px;
  font-size: 12px;
  border-radius: 14px;
  border: 1px solid var(--recruit-primary);
  background: #fff;
  color: var(--recruit-primary);
  cursor: pointer;
  transition: background var(--transition-fast);
}
.refresh-btn:hover {
  background: rgba(91, 127, 201, 0.08);
}
.refresh-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
.ai-summary-content {
  background: rgba(91, 127, 201, 0.06);
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 16px;
}
.summary-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid rgba(91, 127, 201, 0.15);
}
.summary-type {
  font-size: 12px;
  padding: 4px 10px;
  border-radius: 10px;
  background: var(--recruit-primary);
  color: #fff;
  font-weight: 500;
}
.summary-time {
  font-size: 13px;
  color: var(--color-text-muted);
}
.summary-body {
  font-size: 14px;
  line-height: 1.8;
  color: var(--color-text-body);
}
.summary-body :deep(h3) {
  font-size: 16px;
  color: var(--color-text-primary);
  margin: 18px 0 10px;
  font-weight: 600;
}
.summary-body :deep(h3:first-child) {
  margin-top: 0;
}
.summary-body :deep(ul) {
  margin: 10px 0;
  padding-left: 22px;
}
.summary-body :deep(li) {
  margin: 6px 0;
}
.summary-body :deep(p) {
  margin: 10px 0;
}
.text {
  font-size: 15px;
  color: var(--color-text-body);
  line-height: 1.8;
  margin: 0;
}
.activity-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}
.activity-card {
  background: rgba(91, 127, 201, 0.06);
  border-radius: 12px;
  padding: 20px;
  cursor: pointer;
  transition: all var(--transition-fast);
  border: 1px solid transparent;
}
.activity-card:hover {
  border-color: var(--recruit-primary);
  transform: translateY(-3px);
  box-shadow: 0 4px 16px rgba(91, 127, 201, 0.15);
}
.activity-status {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 10px;
  font-size: 11px;
  font-weight: 600;
  margin-bottom: 10px;
}
.activity-status.status-0 {
  background: rgba(250, 173, 20, 0.15);
  color: var(--color-warning);
}
.activity-status.status-1 {
  background: rgba(82, 196, 26, 0.15);
  color: var(--color-success);
}
.activity-status.status-2 {
  background: rgba(153, 153, 153, 0.15);
  color: var(--color-text-muted);
}
.activity-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin: 0 0 12px;
  line-height: 1.4;
}
.activity-info {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 13px;
  color: var(--color-text-muted);
}
.activity-info span {
  display: flex;
  align-items: center;
  gap: 6px;
}
.member-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}
.member-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 16px;
  background: rgba(91, 127, 201, 0.04);
  border-radius: 12px;
  transition: all var(--transition-fast);
}
.member-card:hover {
  background: rgba(91, 127, 201, 0.08);
}
.member-avatar {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}
.member-avatar img {
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
  font-size: 22px;
  font-weight: 600;
}
.member-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  text-align: center;
}
.member-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 100%;
}
.member-role {
  font-size: 11px;
  padding: 3px 8px;
  border-radius: 8px;
  font-weight: 500;
}
.member-role.role-3 {
  background: rgba(255, 107, 157, 0.15);
  color: var(--color-primary);
}
.member-role.role-2 {
  background: rgba(91, 127, 201, 0.15);
  color: var(--recruit-primary);
}
.member-role.role-1 {
  background: rgba(153, 153, 153, 0.12);
  color: var(--color-text-muted);
}
.more-members {
  text-align: center;
  margin-top: 16px;
  font-size: 14px;
  color: var(--color-text-muted);
}
.admin-actions {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}
.admin-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  border-radius: 12px;
  border: 1px solid var(--color-border);
  background: var(--color-bg-card);
  color: var(--color-text-primary);
  font-size: 14px;
  cursor: pointer;
  transition: all var(--transition-fast);
}
.admin-btn:hover {
  border-color: var(--recruit-primary);
  color: var(--recruit-primary);
}
.admin-block {
  margin-bottom: 24px;
  padding-top: 20px;
  border-top: 1px solid var(--color-border-light);
}
.admin-block:last-child {
  margin-bottom: 0;
}
.admin-block h3 {
  font-size: 15px;
  color: var(--color-text-primary);
  margin-bottom: 14px;
  font-weight: 600;
}
.grid-form {
  display: grid;
  grid-template-columns: 100px 1fr;
  gap: 12px 16px;
  align-items: center;
  margin-bottom: 16px;
  max-width: 560px;
}
.grid-form label {
  font-size: 14px;
  color: var(--color-text-secondary);
  font-weight: 500;
}
.inp,
.txa {
  padding: 10px 12px;
  border: 1px solid var(--color-border-light);
  border-radius: 10px;
  font-size: 14px;
  width: 100%;
  box-sizing: border-box;
  transition: border-color var(--transition-fast);
}
.inp:focus,
.txa:focus {
  outline: none;
  border-color: var(--recruit-primary);
}
.txa {
  resize: vertical;
}
.similar-hint {
  margin: -0.25rem 0 1rem;
  font-size: 0.875rem;
  line-height: 1.5;
}
.muted {
  color: var(--color-text-muted);
  font-size: 14px;
}
.apply-list {
  list-style: none;
}
.apply-row {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  gap: 16px;
  padding: 16px 0;
  border-bottom: 1px solid var(--color-border-light);
}
.apply-info {
  flex: 1;
  min-width: 220px;
}
.apply-info strong {
  display: block;
  margin-bottom: 6px;
  font-size: 15px;
  color: var(--color-text-primary);
}
.subline {
  font-size: 13px;
  color: var(--color-text-muted);
  display: block;
  margin-top: 4px;
}
.intro {
  font-size: 14px;
  color: var(--color-text-body);
  margin-top: 10px;
  line-height: 1.6;
}
.apply-time {
  display: block;
  font-size: 12px;
  color: var(--color-text-muted);
  margin-top: 8px;
}
.apply-actions {
  display: flex;
  gap: 10px;
  align-items: flex-start;
}
.actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}
.btn {
  display: inline-block;
  padding: 12px 24px;
  border-radius: 24px;
  text-decoration: none;
  font-size: 14px;
  border: 1px solid var(--color-border);
  background: var(--color-bg-card);
  color: var(--color-text-primary);
  cursor: pointer;
  transition: all var(--transition-fast);
}
.btn:hover {
  border-color: var(--recruit-primary);
  color: var(--recruit-primary);
}
.btn.sm {
  padding: 8px 16px;
  font-size: 13px;
  border-radius: 16px;
}
.btn.primary {
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-light) 100%);
  color: #fff;
  border: none;
  box-shadow: 0 4px 12px rgba(255, 107, 157, 0.25);
}
.btn.primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(255, 107, 157, 0.35);
  color: #fff;
}
.btn.primary-btn {
  padding: 12px 24px;
  border-radius: 24px;
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-light) 100%);
  color: #fff;
  border: none;
  font-size: 14px;
  cursor: pointer;
  transition: all var(--transition-fast);
}
.btn.primary-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
.btn.ghost {
  border-color: var(--color-border);
  background: var(--color-bg-card);
}
.empty-state {
  text-align: center;
  padding: 40px 20px;
  color: var(--color-text-muted);
  font-size: 15px;
}
.loading-state {
  text-align: center;
  padding: 30px 20px;
  color: var(--color-text-muted);
  font-size: 14px;
}
.lazy-load-prompt {
  text-align: center;
  padding: 30px 20px;
}
.prompt-text {
  color: var(--color-text-muted);
  font-size: 14px;
  margin-bottom: 16px;
}
.section-count {
  font-size: 14px;
  color: var(--color-text-secondary);
}
.summary-actions {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 16px;
}
.similar-clubs-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}
.similar-club-card {
  position: relative;
  display: flex;
  gap: 14px;
  padding: 18px;
  background: rgba(91, 127, 201, 0.04);
  border-radius: 14px;
  cursor: pointer;
  transition: all var(--transition-fast);
  border: 1px solid transparent;
}
.similar-club-card:hover {
  background: rgba(91, 127, 201, 0.08);
  border-color: var(--recruit-primary);
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(91, 127, 201, 0.15);
}
.club-logo {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  overflow: hidden;
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}
.club-logo img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.logo-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-light) 100%);
  color: white;
  font-size: 24px;
  font-weight: 700;
}
.club-info {
  flex: 1;
  min-width: 0;
}
.club-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin: 0 0 6px;
  line-height: 1.3;
}
.club-meta {
  display: flex;
  gap: 12px;
  font-size: 13px;
  color: var(--color-text-muted);
  margin-bottom: 8px;
}
.club-meta .meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
}
.club-desc {
  font-size: 13px;
  color: var(--color-text-secondary);
  line-height: 1.5;
  margin: 0 0 8px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.club-tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}
.club-tag {
  padding: 3px 8px;
  background: rgba(91, 127, 201, 0.1);
  color: var(--recruit-primary);
  border-radius: 8px;
  font-size: 11px;
  font-weight: 500;
}
.similarity-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 6px 10px;
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-light) 100%);
  border-radius: 10px;
  color: white;
}
.similarity-value {
  font-size: 14px;
  font-weight: 700;
  line-height: 1;
}
.similarity-label {
  font-size: 10px;
  opacity: 0.9;
  margin-top: 2px;
}
.modal {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
}
.modal-content {
  background: var(--color-bg-card);
  border-radius: 20px;
  padding: 28px;
  width: 100%;
  max-width: 560px;
  max-height: 90vh;
  overflow-y: auto;
}
.modal-content h3 {
  margin: 0 0 24px;
  font-size: 20px;
  font-weight: 600;
  color: var(--color-text-primary);
}
.edit-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.form-group label {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-primary);
}
.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 8px;
}
.err,
.loading {
  text-align: center;
  padding: 60px;
  color: var(--color-text-muted);
  font-size: 15px;
}

@media (max-width: 768px) {
  .main {
    padding: 16px;
  }
  .hero-img {
    height: 180px;
  }
  .hero-overlay {
    padding: 16px;
    gap: 14px;
  }
  .logo {
    width: 64px;
    height: 64px;
  }
  .hero-info h1 {
    font-size: 20px;
  }
  .hero-meta {
    gap: 12px;
    font-size: 13px;
  }
  .activity-grid {
    grid-template-columns: 1fr;
  }
  .member-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }
  .member-card {
    padding: 12px;
  }
  .member-avatar {
    width: 48px;
    height: 48px;
  }
  .grid-form {
    grid-template-columns: 1fr;
    gap: 8px;
  }
  .grid-form label {
    font-size: 13px;
  }
  .admin-actions {
    flex-direction: column;
  }
  .admin-btn {
    width: 100%;
    justify-content: center;
  }
  .modal-content {
    padding: 20px;
  }
  .similar-clubs-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }
  .similar-club-card {
    padding: 14px;
  }
  .club-logo {
    width: 50px;
    height: 50px;
  }
  .club-name {
    font-size: 15px;
  }
  .club-desc {
    font-size: 12px;
  }
  .similarity-badge {
    padding: 5px 8px;
  }
  .similarity-value {
    font-size: 12px;
  }
  .similarity-label {
    font-size: 9px;
  }
}
</style>
