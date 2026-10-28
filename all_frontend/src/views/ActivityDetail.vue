<template>
  <div class="page">
    <SiteHeader />
    <main class="main" v-if="activity">
      <div class="hero">
        <img :src="activity.cover || ph" class="hero-img" alt="" @error="onImgError" />
        <div class="hero-overlay">
          <span class="status-badge" :class="statusClass">{{ statusText }}</span>
          <h1>{{ activity.title }}</h1>
          <p class="club-name" v-if="club">{{ club.name }}</p>
        </div>
      </div>

      <section class="section info-section">
        <div class="info-grid">
          <div class="info-item">
            <svg viewBox="0 0 24 24" width="20" height="20">
              <path
                fill="currentColor"
                d="M11.99 2C6.47 2 2 6.48 2 12s4.47 10 9.99 10C17.52 22 22 17.52 22 12S17.52 2 11.99 2zM12 20c-4.42 0-8-3.58-8-8s3.58-8 8-8 8 3.58 8 8-3.58 8-8 8zm.5-13H11v6l5.25 3.15.75-1.23-4.5-2.67z"
              />
            </svg>
            <div>
              <span class="label">开始时间</span>
              <span class="value">{{ formatTime(activity.startTime) }}</span>
            </div>
          </div>
          <div class="info-item">
            <svg viewBox="0 0 24 24" width="20" height="20">
              <path
                fill="currentColor"
                d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7zm0 9.5c-1.38 0-2.5-1.12-2.5-2.5s1.12-2.5 2.5-2.5 2.5 1.12 2.5 2.5-1.12 2.5-2.5 2.5z"
              />
            </svg>
            <div>
              <span class="label">活动地点</span>
              <span class="value">{{ activity.location || '地点待定' }}</span>
            </div>
          </div>
          <div class="info-item">
            <svg viewBox="0 0 24 24" width="20" height="20">
              <path
                fill="currentColor"
                d="M16 11c1.66 0 2.99-1.34 2.99-3S17.66 5 16 5c-1.66 0-3 1.34-3 3s1.34 3 3 3zm-8 0c1.66 0 2.99-1.34 2.99-3S9.66 5 8 5C6.34 5 5 6.34 5 8s1.34 3 3 3zm0 2c-2.33 0-7 1.17-7 3.5V19h14v-2.5c0-2.33-4.67-3.5-7-3.5zm8 0c-.29 0-.62.02-.97.05 1.16.84 1.97 1.97 1.97 3.45V19h6v-2.5c0-2.33-4.67-3.5-7-3.5z"
              />
            </svg>
            <div>
              <span class="label">报名人数</span>
              <span class="value"
                >{{ activity.currentParticipants || 0 }} /
                {{ activity.maxParticipants || '不限' }}</span
              >
            </div>
          </div>
        </div>
      </section>

      <section class="section">
        <h2>活动简介</h2>
        <p class="text">{{ activity.description || activity.content || '暂无简介' }}</p>
      </section>

      <section class="section participants-section" v-if="signups.length > 0 || loadingSignups">
        <h2>参加者列表（{{ signups.length }}人）</h2>
        <p v-if="loadingSignups" class="muted">加载中…</p>
        <div v-else class="participants-list">
          <div
            v-for="signup in (showAllSignups ? signups : signups.slice(0, 20))"
            :key="signup.id"
            class="participant-card"
          >
            <img
              :src="signup.avatar || ph"
              class="participant-avatar"
              alt=""
              @error="onImgError"
            />
            <div class="participant-info">
              <span class="participant-name">{{
                signup.nickname || signup.realName || signup.username || '匿名用户'
              }}</span>
              <span class="participant-time">{{ formatTime(signup.signupTime) }}</span>
            </div>
          </div>
        </div>
        <button
          v-if="signups.length > 20"
          type="button"
          class="btn show-more-btn"
          @click="showAllSignups = !showAllSignups"
        >
          {{ showAllSignups ? '收起' : `查看全部 (${signups.length}人)` }}
        </button>
      </section>

      <section class="section works-section" v-if="activity?.id">
        <h2>作品展示（{{ works.length }}件作品）</h2>
        <p v-if="loadingWorks" class="muted">加载中…</p>
        <div v-else>
          <div v-if="uid && !myWork" class="work-submit-hint">
            <p>参与活动，展示您的作品</p>
            <button type="button" class="btn primary" @click="openWorkForm">提交作品</button>
          </div>
          <div v-else-if="uid && myWork" class="my-work-section">
            <div class="my-work-header">
              <h3>我的作品</h3>
              <button type="button" class="btn edit-btn" @click="openWorkForm">修改作品</button>
            </div>
            <div class="my-work-card">
              <h4>{{ myWork.title }}</h4>
              <p class="work-content">{{ myWork.content }}</p>
              <div class="work-meta">
                <span class="work-author">{{
                  myWork.nickname || myWork.realName || myWork.username || '匿名用户'
                }}</span>
                <span class="work-time">{{ formatTime(myWork.createdAt) }}</span>
              </div>
            </div>
          </div>
          <div v-if="works.length > 0" class="works-list">
            <div
              v-for="work in (showAllWorks ? works : works.slice(0, 10))"
              :key="work.id"
              class="work-card"
            >
              <h4 class="work-title">{{ work.title }}</h4>
              <p class="work-content">{{ work.content }}</p>
              <div class="work-meta">
                <span class="work-author">{{
                  work.nickname || work.realName || work.username || '匿名用户'
                }}</span>
                <span class="work-time">{{ formatTime(work.createdAt) }}</span>
              </div>
            </div>
          </div>
          <div v-else-if="!uid" class="works-empty">
            <p class="muted">登录后可查看和提交作品</p>
          </div>
          <div v-else class="works-empty">
            <p class="muted">暂无作品，快来提交第一个作品吧！</p>
          </div>
          <button v-if="works.length > 10" type="button" class="btn show-more-btn" @click="showAllWorks = !showAllWorks">
            {{ showAllWorks ? '收起' : `查看全部 (${works.length}件)` }}
          </button>
        </div>
      </section>

      <section class="section groups-section" v-if="groups.length > 0 || loadingGroups">
        <h2>活动组合</h2>
        <p v-if="loadingGroups" class="muted">加载中…</p>
        <div v-else class="groups-list">
          <div v-for="g in groups" :key="g.id" class="group-card">
            <div class="group-info">
              <h3 class="group-name">{{ g.name }}</h3>
              <p class="group-desc" v-if="g.description">{{ g.description }}</p>
              <div class="group-meta">
                <span class="member-count">
                  <svg viewBox="0 0 24 24" width="14" height="14">
                    <path
                      fill="currentColor"
                      d="M16 11c1.66 0 2.99-1.34 2.99-3S17.66 5 16 5c-1.66 0-3 1.34-3 3s1.34 3 3 3zm-8 0c1.66 0 2.99-1.34 2.99-3S9.66 5 8 5C6.34 5 5 6.34 5 8s1.34 3 3 3zm0 2c-2.33 0-7 1.17-7 3.5V19h14v-2.5c0-2.33-4.67-3.5-7-3.5z"
                    />
                  </svg>
                  {{ g.memberCount || 0 }}/{{ g.maxSize }}人
                </span>
                <span v-if="g.memberCount >= g.maxSize" class="full-badge">已满</span>
                <span v-else class="available-badge">可加入</span>
              </div>
            </div>
            <div class="group-actions" v-if="uid && g.memberCount < g.maxSize">
              <button
                v-if="isInGroup(g.id)"
                type="button"
                class="btn leave-btn"
                @click="leaveGroup(g.id)"
              >
                退出
              </button>
              <button v-else type="button" class="btn join-btn" @click="joinGroup(g.id)">
                加入
              </button>
            </div>
            <div class="group-actions" v-else-if="uid && isInGroup(g.id)">
              <button type="button" class="btn leave-btn" @click="leaveGroup(g.id)">退出</button>
            </div>
          </div>
        </div>
      </section>

      <section class="section" v-if="uid && activity.clubId && isClubAdmin">
        <h2>管理活动组合</h2>
        <div class="create-group-form">
          <input v-model="newGroup.name" type="text" class="inp" placeholder="组合名称" />
          <input
            v-model="newGroup.description"
            type="text"
            class="inp"
            placeholder="组合描述（可选）"
          />
          <div class="size-inputs">
            <input
              v-model.number="newGroup.minSize"
              type="number"
              class="inp size-inp"
              placeholder="最小人数"
              min="1"
            />
            <input
              v-model.number="newGroup.maxSize"
              type="number"
              class="inp size-inp"
              placeholder="最大人数"
              min="1"
            />
          </div>
          <button
            type="button"
            class="btn primary-btn"
            @click="createGroup"
            :disabled="creatingGroup"
          >
            {{ creatingGroup ? '创建中…' : '创建组合' }}
          </button>
        </div>
      </section>

      <div class="actions">
        <span v-if="activity.status === 2" class="btn disabled" title="后端暂未开放活动报名"
          >活动报名暂未开放</span
        >
        <span v-else-if="isFull" class="btn disabled">名额已满</span>
        <span v-else class="btn disabled">{{ statusText }}</span>
        <router-link v-if="club" :to="'/club/' + club.id" class="btn">查看社团</router-link>
        <router-link to="/activities" class="btn ghost">返回列表</router-link>
      </div>
    </main>

    <div v-if="workFormVisible" class="work-form-overlay" @click.self="workFormVisible = false">
      <div class="work-form-dialog">
        <h3 class="work-form-title">{{ myWork ? '更新作品' : '提交作品' }}</h3>
        <p class="work-form-hint">
          请填写您的作品信息。提交后，其他参与者可以看到您的作品。
        </p>
        <label class="work-form-label">作品标题</label>
        <input
          v-model.trim="workForm.title"
          type="text"
          class="work-form-input"
          placeholder="请输入作品标题"
          maxlength="100"
        />
        <label class="work-form-label">作品内容</label>
        <textarea
          v-model.trim="workForm.content"
          class="work-form-textarea"
          placeholder="请输入作品内容"
          rows="8"
        ></textarea>
        <div class="work-form-actions">
          <button type="button" class="btn" @click="workFormVisible = false">取消</button>
          <button
            type="button"
            class="btn primary"
            :disabled="submittingWork"
            @click="submitWork"
          >
            {{ submittingWork ? '提交中…' : '提交' }}
          </button>
        </div>
      </div>
    </div>

    <div v-else-if="err" class="err">{{ err }}</div>
    <div v-else class="loading">加载中…</div>
  </div>
</template>

<script>
import SiteHeader from '../components/SiteHeader.vue';
import {
  activityApi,
  clubApi,
  activityGroupApi,
  getCurrentUserId,
  getCurrentAccountUserId,
  IMG_PLACEHOLDER,
  onImgError,
} from '../utils/api.js';

export default {
  name: 'ActivityDetail',
  components: { SiteHeader },
  data() {
    return {
      ph: IMG_PLACEHOLDER,
      activity: null,
      club: null,
      err: '',
      uid: null,
      accountUid: null,
      groups: [],
      loadingGroups: false,
      creatingGroup: false,
      newGroup: {
        name: '',
        description: '',
        minSize: 1,
        maxSize: 10,
      },
      myGroupIds: [],
      signups: [],
      loadingSignups: false,
      showAllSignups: false,
      works: [],
      loadingWorks: false,
      showAllWorks: false,
      myWork: null,
      loadingMyWork: false,
      workFormVisible: false,
      workForm: {
        title: '',
        content: '',
      },
      submittingWork: false,
    };
  },
  computed: {
    statusClass() {
      const st = this.activity?.status;
      if (st === 3) return 'ongoing';
      if (st === 4 || st === 5) return 'ended';
      return 'upcoming';
    },
    statusText() {
      const st = this.activity?.status;
      if (st === 3) return '进行中';
      if (st === 4) return '已结束';
      if (st === 5) return '已取消';
      return '报名中';
    },
    isFull() {
      const a = this.activity;
      return a?.maxParticipants != null && (a.currentParticipants || 0) >= a.maxParticipants;
    },
    isClubAdmin() {
      if (!this.accountUid || !this.activity?.clubId) return false;
      return Number(localStorage.getItem('userRole') || '0') >= 2;
    },
  },
  methods: {
    onImgError,
    formatTime(iso) {
      if (!iso) return '时间待定';
      const d = new Date(iso);
      if (Number.isNaN(d.getTime())) return '时间待定';
      return d.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
      });
    },
    isInGroup(groupId) {
      return this.myGroupIds.includes(groupId);
    },
    async load() {
      const id = this.$route.params.id;
      if (!id) {
        this.err = '缺少活动 ID';
        return;
      }
      try {
        const res = await activityApi.getById(id);
        this.activity = res.data;
        if (this.activity?.clubId) {
          const cRes = await clubApi.getById(this.activity.clubId);
          this.club = cRes.data;
        }
      } catch (e) {
        this.err = e.message || '加载失败';
      }
    },
    async loadGroups() {
      if (!this.activity?.clubId) return;
      this.loadingGroups = true;
      try {
        const list = await activityGroupApi.listByClub(this.activity.clubId);
        this.groups = Array.isArray(list) ? list : [];
      } catch (e) {
        console.warn(e);
      } finally {
        this.loadingGroups = false;
      }
    },
    async loadMyGroups() {
      try {
        const list = await activityGroupApi.myGroups();
        this.myGroupIds = Array.isArray(list) ? list.map((g) => g.id) : [];
      } catch (e) {
        console.warn(e);
      }
    },
    async loadSignups() {
      if (!this.activity?.id) return;
      this.loadingSignups = true;
      try {
        const signups = await activityApi.getSignups(this.activity.id);
        this.signups = Array.isArray(signups) ? signups : [];
      } catch (e) {
        console.warn('加载参与者列表失败', e);
        this.signups = [];
      } finally {
        this.loadingSignups = false;
      }
    },
    async loadWorks() {
      if (!this.activity?.id) return;
      this.loadingWorks = true;
      try {
        const works = await activityApi.getWorks(this.activity.id);
        // unwrapApiData 可能返回数组或 { data: [...] } 对象
        const worksData = works?.data || works;
        this.works = Array.isArray(worksData) ? worksData : [];
      } catch (e) {
        console.warn('加载作品列表失败', e);
        this.works = [];
      } finally {
        this.loadingWorks = false;
      }
    },
    async loadMyWork() {
      if (!this.activity?.id) return;
      this.loadingMyWork = true;
      try {
        const result = await activityApi.getMyWork(this.activity.id);
        // envelopeData 包装后返回 { code: 200, data: { hasWork, work } }
        const data = result?.data || result;
        this.myWork = data?.work || null;
      } catch (e) {
        console.warn('加载我的作品失败', e);
        this.myWork = null;
      } finally {
        this.loadingMyWork = false;
      }
    },
    openWorkForm() {
      this.workForm = {
        title: this.myWork?.title || '',
        content: this.myWork?.content || '',
      };
      this.workFormVisible = true;
    },
    async submitWork() {
      if (!this.workForm.title.trim()) {
        alert('请输入作品标题');
        return;
      }
      if (!this.workForm.content.trim()) {
        alert('请输入作品内容');
        return;
      }

      this.submittingWork = true;
      try {
        if (this.myWork) {
          await activityApi.updateWork(this.activity.id, {
            title: this.workForm.title.trim(),
            content: this.workForm.content.trim(),
          });
          alert('作品更新成功');
        } else {
          await activityApi.createWork(this.activity.id, {
            title: this.workForm.title.trim(),
            content: this.workForm.content.trim(),
          });
          alert('作品提交成功');
        }
        this.workFormVisible = false;
        await this.loadWorks();
        await this.loadMyWork();
      } catch (e) {
        alert(e.message || '提交失败');
      } finally {
        this.submittingWork = false;
      }
    },
    async createGroup() {
      if (!this.newGroup.name.trim()) {
        alert('请输入组合名称');
        return;
      }
      this.creatingGroup = true;
      try {
        await activityGroupApi.create({
          clubId: this.activity.clubId,
          name: this.newGroup.name.trim(),
          description: this.newGroup.description.trim() || undefined,
          minSize: this.newGroup.minSize || 1,
          maxSize: this.newGroup.maxSize || 10,
        });
        this.newGroup = { name: '', description: '', minSize: 1, maxSize: 10 };
        await this.loadGroups();
      } catch (e) {
        alert(e.message || '创建失败');
      } finally {
        this.creatingGroup = false;
      }
    },
    async joinGroup(groupId) {
      try {
        await activityGroupApi.join(groupId);
        await this.loadGroups();
        await this.loadMyGroups();
      } catch (e) {
        alert(e.message || '加入失败');
      }
    },
    async leaveGroup(groupId) {
      if (!confirm('确定退出此组合？')) return;
      try {
        await activityGroupApi.leave(groupId);
        await this.loadGroups();
        await this.loadMyGroups();
      } catch (e) {
        alert(e.message || '退出失败');
      }
    },
  },
  async mounted() {
    this.uid = getCurrentUserId();
    this.accountUid = getCurrentAccountUserId();
    await this.load();
    if (this.activity?.id) {
      this.loadMyGroups();
      this.loadSignups();
      this.loadWorks();
      this.loadMyWork();
    }
  },
  watch: {
    async '$route.params.id'() {
      await this.load();
      if (this.activity?.id) {
        this.loadSignups();
        this.loadWorks();
        this.loadMyWork();
      }
    },
    'activity.clubId'() {
      if (this.activity?.clubId) {
        this.loadGroups();
      }
    },
  },
};
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f4f4f4;
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
  margin-bottom: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
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
  padding: 24px;
  background: linear-gradient(transparent, rgba(0, 0, 0, 0.75));
  color: #fff;
}
.status-badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  margin-bottom: 8px;
}
.status-badge.upcoming {
  background: #00a1d6;
}
.status-badge.ongoing {
  background: #67c23a;
}
.status-badge.ended {
  background: #999;
}
.hero-overlay h1 {
  font-size: 24px;
  margin-bottom: 4px;
}
.club-name {
  font-size: 14px;
  opacity: 0.9;
}
.section {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 16px;
}
.section h2 {
  font-size: 16px;
  color: #333;
  margin-bottom: 12px;
}
.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
}
.info-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}
.info-item svg {
  color: #ff6b9d;
  flex-shrink: 0;
  margin-top: 2px;
}
.info-item .label {
  display: block;
  font-size: 12px;
  color: #999;
  margin-bottom: 2px;
}
.info-item .value {
  display: block;
  font-size: 14px;
  color: #333;
}
.text {
  font-size: 14px;
  color: #666;
  line-height: 1.7;
}
.groups-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.group-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  background: #f8f9ff;
  border: 1px solid #e8efff;
  border-radius: 12px;
}
.group-info {
  flex: 1;
}
.group-name {
  margin: 0 0 4px;
  font-size: 15px;
  font-weight: 500;
  color: #333;
}
.group-desc {
  margin: 0 0 8px;
  font-size: 13px;
  color: #666;
}
.group-meta {
  display: flex;
  align-items: center;
  gap: 10px;
}
.member-count {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #666;
}
.full-badge {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 10px;
  background: #f56c6c;
  color: #fff;
}
.available-badge {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 10px;
  background: #67c23a;
  color: #fff;
}
.group-actions {
  margin-left: 16px;
}
.btn {
  padding: 8px 16px;
  border-radius: 18px;
  font-size: 13px;
  border: 1px solid #e0e0e0;
  background: #fff;
  color: #333;
  cursor: pointer;
  transition: all 0.2s;
}
.btn:hover {
  background: #f5f5f5;
}
.join-btn {
  background: #5b7fc9;
  color: #fff;
  border-color: #5b7fc9;
}
.join-btn:hover {
  background: #4a6bb5;
}
.leave-btn {
  color: #f56c6c;
  border-color: #f56c6c;
}
.leave-btn:hover {
  background: #fef0f0;
}
.create-group-form {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
}
.create-group-form .inp {
  padding: 8px 12px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
}
.size-inputs {
  display: flex;
  gap: 8px;
}
.size-inp {
  width: 100px;
}
.primary-btn {
  background: linear-gradient(135deg, #ff6b9d 0%, #ff8e53 100%);
  color: #fff;
  border: none;
}
.primary-btn:hover {
  opacity: 0.9;
}
.primary-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
.actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}
.btn {
  display: inline-block;
  padding: 10px 20px;
  border-radius: 24px;
  text-decoration: none;
  font-size: 14px;
  border: 1px solid #e0e0e0;
  background: #fff;
  color: #333;
  cursor: pointer;
}
.btn.ghost {
  border-color: #ccc;
  background: #fafafa;
}
.btn.disabled {
  background: #f5f5f5;
  color: #999;
  cursor: not-allowed;
}
.muted {
  color: #999;
  font-size: 14px;
}
.err,
.loading {
  text-align: center;
  padding: 48px;
  color: #999;
}
.participants-section .section h2 {
  margin-bottom: 16px;
}
.participants-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 12px;
  margin-bottom: 16px;
}
.participant-card {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px;
  background: #f8f9fa;
  border-radius: 8px;
}
.participant-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
  background: #e0e0e0;
}
.participant-info {
  flex: 1;
  min-width: 0;
}
.participant-name {
  display: block;
  font-size: 14px;
  color: #333;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.participant-time {
  display: block;
  font-size: 12px;
  color: #999;
  margin-top: 2px;
}
.show-more-btn {
  display: block;
  width: 100%;
  padding: 10px;
  text-align: center;
  background: #f0f0f0;
  border: none;
  border-radius: 8px;
  color: #666;
  cursor: pointer;
  font-size: 14px;
}
.show-more-btn:hover {
  background: #e0e0e0;
}
.works-section .section h2 {
  margin-bottom: 16px;
}
.work-submit-hint {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  background: #f0f7ff;
  border-radius: 8px;
  margin-bottom: 16px;
}
.work-submit-hint p {
  font-size: 14px;
  color: #666;
  margin: 0;
}
.my-work-section {
  margin-bottom: 20px;
}
.my-work-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}
.my-work-header h3 {
  font-size: 16px;
  color: #333;
  margin: 0;
}
.edit-btn {
  padding: 6px 14px;
  font-size: 13px;
  background: #fff;
  border: 1px solid #667eea;
  color: #667eea;
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.2s;
}
.edit-btn:hover {
  background: #667eea;
  color: #fff;
}
.my-work-card {
  padding: 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  color: #fff;
}
.my-work-card h4 {
  font-size: 18px;
  margin-bottom: 8px;
}
.my-work-card .work-content {
  font-size: 14px;
  line-height: 1.6;
  opacity: 0.95;
}
.works-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 16px;
}
.works-empty {
  text-align: center;
  padding: 32px 20px;
  background: #f8f9fa;
  border-radius: 12px;
  margin-bottom: 16px;
}
.works-empty .muted {
  font-size: 14px;
  color: #999;
}
.work-card {
  padding: 16px;
  background: #f8f9fa;
  border-radius: 12px;
  border-left: 4px solid #667eea;
}
.work-title {
  font-size: 16px;
  color: #333;
  margin-bottom: 8px;
}
.work-card .work-content {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
  margin-bottom: 12px;
}
.work-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 12px;
  color: #999;
}
.work-author {
  font-weight: 500;
}
.work-form-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
}
.work-form-dialog {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  max-width: 600px;
  width: 100%;
  max-height: 90vh;
  overflow-y: auto;
}
.work-form-title {
  font-size: 20px;
  color: #333;
  margin-bottom: 8px;
}
.work-form-hint {
  font-size: 14px;
  color: #666;
  margin-bottom: 20px;
  line-height: 1.5;
}
.work-form-label {
  display: block;
  font-size: 14px;
  color: #333;
  margin-bottom: 8px;
  font-weight: 500;
}
.work-form-input {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 14px;
  margin-bottom: 16px;
  box-sizing: border-box;
}
.work-form-textarea {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 14px;
  margin-bottom: 20px;
  resize: vertical;
  min-height: 150px;
  box-sizing: border-box;
}
.work-form-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}
</style>
