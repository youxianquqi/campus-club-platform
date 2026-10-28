<template>
  <div class="page">
    <SiteHeader />
    <main class="main">
      <div v-if="loading" class="loading">加载中...</div>
      <div v-else-if="!myClub" class="no-club">
        <h2>您还不是社团负责人</h2>
        <p>请先创建或加入一个社团成为负责人后才能管理</p>
        <router-link to="/create-club" class="create-btn">创建社团</router-link>
      </div>
      <template v-else>
        <div class="header">
          <h1>管理社团：{{ myClub.name }}</h1>
          <span class="status" :class="'s' + myClub.status">
            {{ statusText(myClub.status) }}
          </span>
        </div>

        <div class="tabs">
          <button
            v-for="tab in tabs"
            :key="tab.key"
            type="button"
            class="tab"
            :class="{ active: activeTab === tab.key }"
            @click="activeTab = tab.key"
          >
            {{ tab.label }}
          </button>
        </div>

        <div class="content">
          <section v-show="activeTab === 'info'" class="section">
            <h2>基本信息</h2>
            <div class="form">
              <div class="form-group">
                <label>社团名称</label>
                <input v-model="clubForm.name" type="text" class="inp" />
              </div>
              <div class="form-group">
                <label>社团简介</label>
                <textarea v-model="clubForm.description" class="inp" rows="4"></textarea>
              </div>
              <div class="form-group">
                <label>社团分类</label>
                <select v-model="clubForm.typeId" class="inp">
                  <option v-for="t in types" :key="t.id" :value="t.id">{{ t.name }}</option>
                </select>
              </div>
              <div class="form-group">
                <label>封面图片</label>
                <input v-model="clubForm.cover" type="text" class="inp" placeholder="图片URL" />
              </div>
              <button type="button" class="save-btn" :disabled="saving" @click="saveClubInfo">
                {{ saving ? '保存中...' : '保存修改' }}
              </button>
            </div>
          </section>

          <section v-show="activeTab === 'members'" class="section">
            <h2>成员管理</h2>
            <div class="member-list">
              <div v-for="m in members" :key="m.id" class="member-item">
                <img :src="m.userAvatar || ph" class="avatar" @error="onImgError" />
                <div class="info">
                  <span class="name">{{ m.userName }}</span>
                  <span class="role">{{ m.role === 1 ? '负责人' : '成员' }}</span>
                </div>
                <div class="actions">
                  <button
                    v-if="m.role !== 1"
                    type="button"
                    class="btn-remove"
                    @click="removeMember(m)"
                  >
                    移除
                  </button>
                </div>
              </div>
              <div v-if="!members.length" class="empty">暂无成员</div>
            </div>
          </section>

          <section v-show="activeTab === 'invite'" class="section">
            <h2>学生邀请</h2>
            <p class="hint">
              与后端 <code>/api/invitations</code> 一致：向指定学生（用户主键 ID）发出入社邀请。
            </p>
            <div class="invite-codes">
              <div v-for="inv in inviteCodes" :key="inv.id" class="code-item">
                <div class="code-info">
                  <span class="code">{{ inv.userName || inv.studentName || '用户' }}</span>
                  <span class="usage"
                    >用户ID {{ inv.userId ?? inv.studentId ?? '—' }} · 状态
                    {{ inv.status || '—' }}</span
                  >
                  <span class="expire" v-if="inv.createdAt"
                    >创建于 {{ formatDate(inv.createdAt) }}</span
                  >
                  <p v-if="inv.message" class="invite-msg">{{ inv.message }}</p>
                </div>
              </div>
              <div v-if="!inviteCodes.length" class="empty">暂无邀请记录</div>
            </div>
            <div class="create-code">
              <input
                v-model.number="newInviteUserId"
                type="number"
                min="1"
                class="inp small"
                placeholder="被邀请学生用户 ID"
              />
              <input
                v-model="newInviteMessage"
                type="text"
                class="inp"
                placeholder="附言（可选）"
              />
              <button type="button" class="btn-create" @click="createInviteCode">发送邀请</button>
            </div>
          </section>

          <section v-show="activeTab === 'aiReco'" class="section">
            <h2>AI 推荐学生</h2>
            <p class="hint">
              与后端 <code>/api/clubs/{{ myClub.id }}/recommendations</code> 一致；邀请时会附带当前推荐记录 ID，
              便于链路追溯。
            </p>
            <p v-if="aiRecoLoading" class="empty">加载中…</p>
            <template v-else>
              <div v-if="!aiRecos.length" class="empty">暂无 AI 推荐记录（需后端写入推荐数据）</div>
              <div v-else class="invite-codes">
                <div v-for="rec in aiRecos" :key="rec.id" class="code-item ai-rec-item">
                  <div class="code-info">
                    <span class="code">{{ rec.studentName || `用户 #${rec.studentDbId}` }}</span>
                    <span class="usage"
                      >学号 {{ rec.studentId || '—' }} · 相似度
                      {{
                        rec.similarity != null ? Number(rec.similarity).toFixed(2) : '—'
                      }}
                      · 状态 {{ rec.status || '—' }}</span
                    >
                    <p v-if="rec.specialty || rec.hobby" class="invite-msg">
                      <span v-if="rec.specialty">专业 {{ rec.specialty }}</span>
                      <span v-if="rec.specialty && rec.hobby"> · </span>
                      <span v-if="rec.hobby">兴趣 {{ rec.hobby }}</span>
                    </p>
                    <p v-if="rec.recommendationText" class="invite-msg reco-text">{{ rec.recommendationText }}</p>
                  </div>
                  <button
                    type="button"
                    class="btn-create sm"
                    :disabled="invitingRecoId === rec.id"
                    @click="inviteFromAiReco(rec)"
                  >
                    {{ invitingRecoId === rec.id ? '发送中…' : '发送邀请' }}
                  </button>
                </div>
              </div>
            </template>
          </section>

          <section v-show="activeTab === 'tags'" class="section">
            <h2>隐藏标签管理</h2>
            <p class="hint">隐藏标签用于社团匹配算法，用户不可见</p>
            <div class="tags-list">
              <div v-for="tag in hiddenTags" :key="tag.id" class="tag-item">
                <span class="tag-name">{{ tag.tagName }}</span>
                <input
                  v-model.number="tag.weight"
                  type="number"
                  min="0"
                  max="2"
                  step="0.1"
                  class="weight-input"
                />
                <button type="button" class="btn-remove-tag" @click="removeTag(tag)">删除</button>
              </div>
            </div>
            <div class="add-tag">
              <input v-model="newTagName" type="text" class="inp" placeholder="标签名称" />
              <input
                v-model.number="newTagWeight"
                type="number"
                min="0"
                max="2"
                step="0.1"
                class="inp small"
                placeholder="权重"
              />
              <button type="button" class="btn-add" @click="addTag">添加标签</button>
            </div>
          </section>

          <section v-show="activeTab === 'applies'" class="section">
            <h2>入社申请</h2>
            <div class="apply-list">
              <div v-for="a in applies" :key="a.id" class="apply-item">
                <div class="apply-info">
                  <span class="user">{{ a.studentName || `学生 #${a.studentId}` }}</span>
                  <span class="time">{{ formatDate(a.submittedAt) }}</span>
                  <p class="intro">{{ a.selfIntro || '无自我介绍' }}</p>
                </div>
                <div class="apply-actions">
                  <button
                    v-if="a.status === 0"
                    type="button"
                    class="btn-approve"
                    @click="reviewApply(a, 1)"
                  >
                    通过
                  </button>
                  <button
                    v-if="a.status === 0"
                    type="button"
                    class="btn-reject"
                    @click="reviewApply(a, 2)"
                  >
                    拒绝
                  </button>
                  <span v-else class="status-badge" :class="'s' + a.status">
                    {{ a.status === 1 ? '已通过' : '已拒绝' }}
                  </span>
                </div>
              </div>
              <div v-if="!applies.length" class="empty">暂无申请</div>
            </div>
          </section>
        </div>
      </template>
    </main>
  </div>
</template>

<script>
import SiteHeader from '../components/SiteHeader.vue';
import {
  clubApi,
  clubTypeApi,
  clubMemberApi,
  applicationApi,
  clubHiddenTagApi,
  clubInviteApi,
  IMG_PLACEHOLDER,
  onImgError,
  getCurrentUserId,
} from '../utils/api.js';

export default {
  name: 'ClubManage',
  components: { SiteHeader },
  data() {
    return {
      ph: IMG_PLACEHOLDER,
      loading: true,
      myClub: null,
      types: [],
      tabs: [
        { key: 'info', label: '基本信息' },
        { key: 'members', label: '成员管理' },
        { key: 'invite', label: '邀请码' },
        { key: 'aiReco', label: 'AI推荐学生' },
        { key: 'tags', label: '隐藏标签' },
        { key: 'applies', label: '入社申请' },
      ],
      activeTab: 'info',
      clubForm: {
        name: '',
        description: '',
        typeId: null,
        cover: '',
      },
      saving: false,
      members: [],
      inviteCodes: [],
      newInviteUserId: null,
      newInviteMessage: '',
      hiddenTags: [],
      newTagName: '',
      newTagWeight: 1.0,
      applies: [],
      aiRecos: [],
      aiRecoLoading: false,
      invitingRecoId: null,
    };
  },
  watch: {
    activeTab(val) {
      if (val === 'aiReco' && this.myClub) this.loadAiRecos();
    },
  },
  methods: {
    onImgError,
    statusText(s) {
      if (s === 1) return '待审核';
      if (s === 2) return '正常';
      if (s === 3) return '已禁用';
      return '未知';
    },
    formatDate(iso) {
      if (!iso) return '';
      return new Date(iso).toLocaleString();
    },
    async loadTypes() {
      try {
        const res = await clubTypeApi.list();
        this.types = res.data || [];
      } catch (e) {
        console.error(e);
      }
    },
    async loadMyClub() {
      const userId = getCurrentUserId();
      if (!userId) return;
      try {
        const res = await clubApi.getByLeaderId(userId);
        if (res.data) {
          this.myClub = res.data;
          this.clubForm = {
            name: res.data.name,
            description: res.data.description || '',
            typeId: res.data.typeId,
            cover: res.data.cover || '',
          };
          await Promise.all([
            this.loadMembers(),
            this.loadInviteCodes(),
            this.loadHiddenTags(),
            this.loadApplies(),
          ]);
        }
      } catch (e) {
        console.error(e);
      }
    },
    async loadMembers() {
      if (!this.myClub) return;
      try {
        const res = await clubMemberApi.listByClub(this.myClub.id);
        this.members = res.data || [];
      } catch (e) {
        console.error(e);
      }
    },
    async loadInviteCodes() {
      if (!this.myClub) return;
      try {
        const res = await clubInviteApi.listByClub(this.myClub.id);
        this.inviteCodes = Array.isArray(res) ? res : res.data || [];
      } catch (e) {
        console.error(e);
      }
    },
    async loadHiddenTags() {
      if (!this.myClub) return;
      try {
        const list = await clubHiddenTagApi.listByClub(this.myClub.id);
        this.hiddenTags = Array.isArray(list) ? list : [];
      } catch (e) {
        console.error(e);
      }
    },
    async loadApplies() {
      if (!this.myClub) return;
      try {
        const res = await applicationApi.getByClubId(this.myClub.id);
        this.applies = res.data || [];
      } catch (e) {
        console.error(e);
      }
    },
    async saveClubInfo() {
      if (!this.myClub) return;
      this.saving = true;
      try {
        await clubApi.update({
          id: this.myClub.id,
          ...this.clubForm,
        });
        alert('保存成功');
      } catch (e) {
        alert(e.message || '保存失败');
      } finally {
        this.saving = false;
      }
    },
    async removeMember(m) {
      if (!confirm(`确定移除成员 ${m.userName}？`)) return;
      try {
        await clubMemberApi.remove(m.clubId, m.userId);
        await this.loadMembers();
        alert('已移除');
      } catch (e) {
        alert(e.message || '移除失败');
      }
    },
    async createInviteCode() {
      if (!this.myClub) return;
      const uid = Number(this.newInviteUserId);
      if (!Number.isFinite(uid) || uid <= 0) {
        alert('请填写被邀请用户的用户主键 ID（正整数）');
        return;
      }
      try {
        await clubInviteApi.create({
          clubId: this.myClub.id,
          userId: uid,
          message: this.newInviteMessage || undefined,
        });
        await this.loadInviteCodes();
        this.newInviteUserId = null;
        this.newInviteMessage = '';
        alert('邀请已发送');
      } catch (e) {
        alert(e.message || '发送失败');
      }
    },
    async loadAiRecos() {
      if (!this.myClub) return;
      this.aiRecoLoading = true;
      try {
        const list = await clubApi.getRecommendations(this.myClub.id);
        this.aiRecos = Array.isArray(list) ? list : [];
      } catch (e) {
        console.error(e);
        this.aiRecos = [];
      } finally {
        this.aiRecoLoading = false;
      }
    },
    async inviteFromAiReco(rec) {
      if (!this.myClub || !rec) return;
      const uid = Number(rec.studentDbId);
      if (!Number.isFinite(uid) || uid <= 0) {
        alert('推荐记录缺少有效用户主键');
        return;
      }
      const name = rec.studentName || `用户 #${uid}`;
      if (!confirm(`向「${name}」发送入社邀请？`)) return;
      this.invitingRecoId = rec.id;
      try {
        await clubInviteApi.create({
          clubId: this.myClub.id,
          userId: uid,
          aiRecommendationId: rec.id,
        });
        await this.loadInviteCodes();
        alert('邀请已发送');
      } catch (e) {
        alert(e.message || '发送失败');
      } finally {
        this.invitingRecoId = null;
      }
    },
    async addTag() {
      if (!this.myClub || !this.newTagName.trim()) return;
      try {
        await clubHiddenTagApi.add({
          clubId: this.myClub.id,
          tagName: this.newTagName.trim(),
          weight: this.newTagWeight || 1.0,
        });
        await this.loadHiddenTags();
        this.newTagName = '';
        this.newTagWeight = 1.0;
        alert('标签已添加');
      } catch (e) {
        alert(e.message || '添加失败');
      }
    },
    async removeTag(tag) {
      if (!confirm(`确定删除标签 ${tag.tagName}？`)) return;
      try {
        await clubHiddenTagApi.removeByName(this.myClub.id, tag.tagName);
        await this.loadHiddenTags();
      } catch (e) {
        alert(e.message || '删除失败');
      }
    },
    async reviewApply(apply, status) {
      const action = status === 1 ? '通过' : '拒绝';
      if (!confirm(`确定${action}该申请？`)) return;
      let reason = '';
      if (status === 2) {
        reason = window.prompt('拒绝原因（可选）', '') ?? '';
      }
      try {
        if (status === 1) await applicationApi.approve(apply.id);
        else await applicationApi.reject(apply.id, reason);
        await this.loadApplies();
        await this.loadMembers();
        alert(`已${action}`);
      } catch (e) {
        alert(e.message || '操作失败');
      }
    },
  },
  async mounted() {
    await this.loadTypes();
    await this.loadMyClub();
    this.loading = false;
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
  padding: 24px 16px 48px;
}
.loading,
.no-club {
  text-align: center;
  padding: 60px 20px;
  color: #666;
}
.no-club h2 {
  font-size: 20px;
  margin-bottom: 12px;
}
.create-btn {
  display: inline-block;
  margin-top: 20px;
  padding: 12px 28px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 8px;
  text-decoration: none;
}
.header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}
.header h1 {
  font-size: 22px;
  color: #333;
}
.status {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
}
.status.s1 {
  background: #fff7e6;
  color: #d48806;
}
.status.s2 {
  background: #f6ffed;
  color: #52c41a;
}
.status.s3 {
  background: #fff1f0;
  color: #cf1322;
}
.tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}
.tab {
  padding: 10px 20px;
  background: white;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
}
.tab.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-color: transparent;
}
.content {
  background: white;
  border-radius: 12px;
  padding: 24px;
}
.section h2 {
  font-size: 16px;
  margin-bottom: 16px;
  color: #333;
}
.form {
  max-width: 500px;
}
.form-group {
  margin-bottom: 16px;
}
.form-group label {
  display: block;
  font-size: 13px;
  color: #666;
  margin-bottom: 6px;
}
.inp {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
  box-sizing: border-box;
}
.inp.small {
  width: 120px;
}
textarea.inp {
  resize: vertical;
}
.save-btn {
  padding: 12px 32px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
}
.save-btn:disabled {
  opacity: 0.6;
}
.hint {
  font-size: 13px;
  color: #999;
  margin-bottom: 16px;
}
.member-list,
.apply-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.member-item,
.apply-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #f9f9f9;
  border-radius: 8px;
}
.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
}
.member-item .info {
  flex: 1;
}
.member-item .name {
  font-weight: 500;
}
.member-item .role {
  margin-left: 8px;
  font-size: 12px;
  color: #999;
}
.btn-remove {
  padding: 6px 12px;
  background: #ff4d4f;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 12px;
  cursor: pointer;
}
.empty {
  text-align: center;
  color: #999;
  padding: 20px;
}
.invite-codes {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 20px;
}
.code-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  background: #f9f9f9;
  border-radius: 8px;
}
.ai-rec-item {
  align-items: flex-start;
}
.ai-rec-item .code-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.ai-rec-item .usage {
  margin-left: 0;
}
.ai-rec-item .invite-msg.reco-text {
  font-family: inherit;
}
.btn-create.sm {
  flex-shrink: 0;
  margin-left: 12px;
  padding: 8px 14px;
  font-size: 13px;
}
.code {
  font-family: monospace;
  font-size: 16px;
  font-weight: bold;
  color: #667eea;
}
.usage,
.expire {
  font-size: 12px;
  color: #999;
  margin-left: 12px;
}
.code-actions {
  display: flex;
  gap: 8px;
}
.btn-copy,
.btn-disable {
  padding: 6px 12px;
  border: none;
  border-radius: 6px;
  font-size: 12px;
  cursor: pointer;
}
.btn-copy {
  background: #667eea;
  color: white;
}
.btn-disable {
  background: #ff4d4f;
  color: white;
}
.create-code {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}
.btn-create {
  padding: 10px 20px;
  background: #52c41a;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
}
.tags-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 16px;
}
.tag-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px;
  background: #f9f9f9;
  border-radius: 8px;
}
.tag-name {
  flex: 1;
  font-weight: 500;
}
.weight-input {
  width: 80px;
  padding: 6px 10px;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
}
.btn-remove-tag {
  padding: 4px 10px;
  background: #ff4d4f;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 12px;
  cursor: pointer;
}
.add-tag {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}
.btn-add {
  padding: 10px 20px;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
}
.apply-info {
  flex: 1;
}
.apply-info .user {
  font-weight: 500;
}
.apply-info .time {
  font-size: 12px;
  color: #999;
  margin-left: 12px;
}
.apply-info .intro {
  margin-top: 8px;
  font-size: 13px;
  color: #666;
}
.apply-actions {
  display: flex;
  gap: 8px;
}
.btn-approve,
.btn-reject {
  padding: 6px 16px;
  border: none;
  border-radius: 6px;
  font-size: 13px;
  cursor: pointer;
}
.btn-approve {
  background: #52c41a;
  color: white;
}
.btn-reject {
  background: #ff4d4f;
  color: white;
}
.status-badge {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
}
.status-badge.s2 {
  background: #f6ffed;
  color: #52c41a;
}
.status-badge.s3 {
  background: #fff1f0;
  color: #cf1322;
}
</style>
