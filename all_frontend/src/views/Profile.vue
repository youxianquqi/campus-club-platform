<template>
  <div class="page">
    <SiteHeader />
    <main class="main">
      <div v-if="!uid" class="login-hint">
        <p>请先 <router-link to="/login">登录</router-link></p>
      </div>

      <template v-else>
        <section class="user-card">
          <div class="user-info">
            <div class="avatar-wrapper">
              <img v-if="profile.avatar" :src="profile.avatar" alt="头像" class="avatar" />
              <div v-else class="avatar-placeholder">{{ profile.nickname?.[0] || 'U' }}</div>
            </div>
            <div class="user-details">
              <h1 class="nickname">{{ profile.nickname || '未设置昵称' }}</h1>
              <p class="user-role" :class="'role-' + rawUser?.role">
                {{ roleText(rawUser?.role) }}
              </p>
              <p v-if="profile.realName" class="real-name">{{ profile.realName }}</p>
            </div>
          </div>
          <div class="user-stats">
            <div class="stat-item">
              <span class="stat-number">{{ apps.length }}</span>
              <span class="stat-label">申请社团</span>
            </div>
            <div class="stat-item">
              <span class="stat-number">{{ totalSelectedCount }}</span>
              <span class="stat-label">兴趣标签</span>
            </div>
            <div class="stat-item">
              <span class="stat-number">{{ inviteCodes.length }}</span>
              <span class="stat-label">邀请码</span>
            </div>
          </div>
        </section>

        <section v-if="rawUser && rawUser.role === 3" class="admin-card">
          <p>您当前为 <strong>平台管理员</strong>。</p>
          <router-link to="/admin" class="admin-link">进入管理后台</router-link>
        </section>

        <div class="modules-grid">
          <div
            class="module-card"
            :class="{ active: expandedSection === 'profile' }"
            @click="toggleSection('profile')"
          >
            <div class="module-icon">📝</div>
            <h3>编辑资料</h3>
            <p class="module-desc">修改个人信息</p>
          </div>

          <div
            class="module-card"
            :class="{ active: expandedSection === 'invite' }"
            @click="toggleSection('invite')"
          >
            <div class="module-icon">🎫</div>
            <h3>邀请码</h3>
            <p class="module-desc">管理邀请码</p>
          </div>

          <div
            class="module-card"
            :class="{ active: expandedSection === 'tags' }"
            @click="toggleSection('tags')"
          >
            <div class="module-icon">🏷️</div>
            <h3>兴趣标签</h3>
            <p class="module-desc">选择感兴趣的标签</p>
          </div>

          <div
            class="module-card"
            :class="{ active: expandedSection === 'clubs' }"
            @click="toggleSection('clubs')"
          >
            <div class="module-icon">🏛️</div>
            <h3>我的社团</h3>
            <p class="module-desc">查看入社申请</p>
          </div>

          <div class="module-card" @click="$router.push('/footprint')">
            <div class="module-icon">👣</div>
            <h3>我的足迹</h3>
            <p class="module-desc">参加过的活动</p>
          </div>
        </div>

        <transition name="slide">
          <section v-if="expandedSection === 'profile'" class="detail-section">
            <div class="section-header">
              <h2>编辑资料</h2>
              <button class="close-btn" @click="expandedSection = null">×</button>
            </div>
            <div class="form">
              <label>昵称</label>
              <input v-model="profile.nickname" type="text" class="inp" />
              <label>手机号</label>
              <input v-model="profile.phone" type="text" class="inp" />
              <label>学院</label>
              <input v-model="profile.college" type="text" class="inp" />
              <label>专业</label>
              <input v-model="profile.major" type="text" class="inp" />
              <label>真实姓名</label>
              <input v-model="profile.realName" type="text" class="inp" />
              <label>学号</label>
              <input v-model="profile.studentId" type="text" class="inp" />
              <label>年级（数字，如 1–4）</label>
              <input
                v-model.number="profile.grade"
                type="number"
                min="1"
                max="10"
                class="inp"
                placeholder="可选"
              />
              <label>头像 URL</label>
              <input v-model="profile.avatar" type="text" class="inp" placeholder="https://..." />
              <button type="button" class="save" :disabled="saving" @click="saveProfile">
                {{ saving ? '保存中...' : '保存' }}
              </button>
            </div>
          </section>
        </transition>

        <transition name="slide">
          <section v-if="expandedSection === 'invite'" class="detail-section">
            <div class="section-header">
              <h2>邀请码管理</h2>
              <button class="close-btn" @click="expandedSection = null">×</button>
            </div>

            <div class="invite-create">
              <h3>创建邀请码</h3>
              <div class="invite-form">
                <div class="form-row">
                  <label>关联社团ID（可选）</label>
                  <input
                    v-model.number="inviteForm.clubId"
                    type="number"
                    class="inp"
                    placeholder="留空表示通用"
                  />
                </div>
                <div class="form-row">
                  <label>最大使用次数</label>
                  <input
                    v-model.number="inviteForm.maxUses"
                    type="number"
                    class="inp"
                    min="1"
                    max="100"
                  />
                </div>
                <div class="form-row">
                  <label>有效期（天数）</label>
                  <input
                    v-model.number="inviteForm.days"
                    type="number"
                    class="inp"
                    min="1"
                    placeholder="默认30天"
                  />
                </div>
                <button
                  type="button"
                  class="btn primary"
                  :disabled="inviteCreating"
                  @click="createInviteCode"
                >
                  {{ inviteCreating ? '生成中…' : '生成邀请码' }}
                </button>
              </div>
            </div>

            <div class="invite-list">
              <h3>我的邀请码</h3>
              <p v-if="inviteLoading" class="muted">加载中…</p>
              <p v-else-if="inviteCodes.length === 0" class="muted">暂无邀请码</p>
              <div v-else class="code-list">
                <div v-for="code in inviteCodes" :key="code.id" class="code-card">
                  <div class="code-info">
                    <div class="code-value">
                      <span class="code">{{ code.code }}</span>
                      <button type="button" class="copy-btn" @click="copyInviteCode(code.code)">
                        复制
                      </button>
                    </div>
                    <p class="code-meta">
                      <span>已使用 {{ code.usedCount }}/{{ code.maxUses }} 次</span>
                      <span>·</span>
                      <span>有效期至 {{ formatTime(code.expiresAt) }}</span>
                    </p>
                  </div>
                  <button type="button" class="del-btn" @click="deleteInviteCode(code.id)">
                    删除
                  </button>
                </div>
              </div>
            </div>
          </section>
        </transition>

        <transition name="slide">
          <section v-if="expandedSection === 'tags'" class="detail-section">
            <div class="section-header">
              <h2>我的兴趣标签</h2>
              <button class="close-btn" @click="expandedSection = null">×</button>
            </div>
            <p class="hint">选择你感兴趣的标签，保存后将同步到账号</p>

            <div class="tags-section">
              <h3>热门标签</h3>
              <div class="tags-grid">
                <button
                  v-for="tag in hotTags"
                  :key="tag.id"
                  type="button"
                  class="tag-btn"
                  :class="{ selected: isTagSelected(tag.id) }"
                  @click="toggleTag(tag)"
                >
                  <span class="tag-icon">{{ tag.icon || '🏷️' }}</span>
                  <span class="tag-name">{{ tag.name }}</span>
                </button>
              </div>
            </div>

            <div class="tags-section">
              <h3>自定义标签</h3>
              <div class="custom-input">
                <input
                  v-model="customTagInput"
                  type="text"
                  placeholder="输入自定义标签..."
                  class="inp"
                  @keyup.enter="addCustomTag"
                />
                <button type="button" class="add-btn" @click="addCustomTag">添加</button>
              </div>
              <div class="custom-tags">
                <span v-for="tag in customTags" :key="tag" class="custom-tag">
                  {{ tag }}
                  <button type="button" class="remove-btn" @click="removeCustomTag(tag)">×</button>
                </span>
              </div>
            </div>

            <div class="selected-info">
              已选择 <strong>{{ totalSelectedCount }}</strong> 个标签
            </div>

            <button type="button" class="save" :disabled="savingTags" @click="saveTags">
              {{ savingTags ? '保存中...' : '保存标签' }}
            </button>
          </section>
        </transition>

        <transition name="slide">
          <section v-if="expandedSection === 'clubs'" class="detail-section">
            <div class="section-header">
              <h2>我的入社申请</h2>
              <button class="close-btn" @click="expandedSection = null">×</button>
            </div>
            <div v-if="!apps.length" class="muted">暂无记录</div>
            <ul v-else class="list">
              <li v-for="a in apps" :key="a.id">
                <span>社团 #{{ a.clubId }}</span>
                <span class="status" :class="'s' + a.status">{{ statusText(a.status) }}</span>
                <span class="time">{{ formatTimeApp(a.createdAt) }}</span>
              </li>
            </ul>
          </section>
        </transition>
      </template>
    </main>
  </div>
</template>

<script>
import SiteHeader from '../components/SiteHeader.vue';
import { getCurrentUserId, userApi, clubApplyApi, clubTypeApi, userTagApi } from '../utils/api.js';
import { inviteCodeApi } from '../api/modules/newFeatures.js';

export default {
  name: 'Profile',
  components: { SiteHeader },
  data() {
    return {
      uid: null,
      rawUser: null,
      profile: {
        id: null,
        nickname: '',
        phone: '',
        college: '',
        major: '',
        realName: '',
        studentId: '',
        grade: null,
        avatar: '',
      },
      apps: [],
      saving: false,
      hotTags: [],
      selectedTagIds: [],
      customTags: [],
      customTagInput: '',
      savingTags: false,
      expandedSection: null,
      inviteCodes: [],
      inviteLoading: true,
      inviteCreating: false,
      inviteForm: {
        clubId: null,
        maxUses: 10,
        days: 30,
      },
    };
  },
  computed: {
    totalSelectedCount() {
      return this.selectedTagIds.length + this.customTags.length;
    },
  },
  methods: {
    roleText(role) {
      if (role === 3) return '平台管理员';
      if (role === 2) return '社团管理员';
      return '普通用户';
    },
    statusText(s) {
      if (s === 1) return '已通过';
      if (s === 2) return '已拒绝';
      return '待审核';
    },
    formatTimeApp(iso) {
      if (!iso) return '';
      return new Date(iso).toLocaleString();
    },
    toggleSection(section) {
      if (this.expandedSection === section) {
        this.expandedSection = null;
      } else {
        this.expandedSection = section;
        if (section === 'invite') {
          this.loadInviteCodes();
        }
        if (section === 'tags') {
          this.loadUserTags();
          this.loadHotTags();
        }
      }
    },
    async load() {
      this.uid = getCurrentUserId();
      if (!this.uid) return;
      try {
        const u = await userApi.getCurrentUser();
        const d = u.data || {};
        this.rawUser = d;
        this.profile = {
          id: d.id,
          nickname: d.nickname || '',
          phone: d.phone || '',
          college: d.college || '',
          major: d.major || '',
          realName: d.realName || '',
          studentId: d.studentId || '',
          grade: d.grade != null ? d.grade : null,
          avatar: d.avatar || '',
        };
        if (d.role != null) localStorage.setItem('userRole', String(d.role));
        const ar = await clubApplyApi.getByUserId(this.uid);
        this.apps = ar.data || [];
      } catch (e) {
        console.error(e);
      }
    },
    async loadHotTags() {
      try {
        const res = await clubTypeApi.list();
        const types = res.data || [];
        const tagIcons = {
          文艺类: '🎨',
          科技类: '💻',
          体育类: '⚽',
          公益类: '❤️',
          学术类: '📚',
          实践类: '🔧',
          娱乐类: '🎮',
          音乐类: '🎵',
          舞蹈类: '💃',
          摄影类: '📷',
        };
        this.hotTags = types.map((t) => ({
          id: t.id,
          name: t.name,
          icon: tagIcons[t.name] || '🏷️',
        }));
      } catch (e) {
        console.error('加载热门标签失败:', e);
        this.hotTags = [
          { id: 1, name: '文艺', icon: '🎨' },
          { id: 2, name: '科技', icon: '💻' },
          { id: 3, name: '体育', icon: '⚽' },
          { id: 4, name: '音乐', icon: '🎵' },
          { id: 5, name: '摄影', icon: '📷' },
          { id: 6, name: '编程', icon: '👨‍💻' },
          { id: 7, name: '舞蹈', icon: '💃' },
          { id: 8, name: '篮球', icon: '🏀' },
          { id: 9, name: '公益', icon: '❤️' },
          { id: 10, name: '文学', icon: '📖' },
        ];
      }
    },
    async loadUserTags() {
      if (!this.uid) return;
      try {
        const res = await userTagApi.listByUser();
        const raw = res.data || [];
        this.selectedTagIds = [];
        this.customTags = [];
        if (raw.length && typeof raw[0] === 'number') {
          raw.forEach((id) => {
            const n = Number(id);
            if (!Number.isNaN(n) && !this.selectedTagIds.some((x) => Number(x) === n))
              this.selectedTagIds.push(n);
          });
          return;
        }
        raw.forEach((tag) => {
          if (tag.tagType === 'custom') {
            this.customTags.push(tag.tagName);
          } else {
            const found = this.hotTags.find((t) => t.name === tag.tagName);
            if (found) {
              const fid = Number(found.id);
              if (Number.isFinite(fid) && !this.selectedTagIds.some((x) => Number(x) === fid)) {
                this.selectedTagIds.push(fid);
              }
            }
          }
        });
      } catch (e) {
        console.error('加载用户标签失败:', e);
      }
    },
    async loadInviteCodes() {
      if (!this.uid) return;
      this.inviteLoading = true;
      try {
        const list = await inviteCodeApi.myList();
        this.inviteCodes = Array.isArray(list) ? list : [];
      } catch (e) {
        console.warn(e);
        this.inviteCodes = [];
      } finally {
        this.inviteLoading = false;
      }
    },
    isTagSelected(tagId) {
      const id = Number(tagId);
      if (!Number.isFinite(id)) return false;
      return this.selectedTagIds.some((x) => Number(x) === id);
    },
    toggleTag(tag) {
      const id = Number(tag?.id);
      if (!Number.isFinite(id)) return;
      const index = this.selectedTagIds.findIndex((x) => Number(x) === id);
      if (index > -1) {
        this.selectedTagIds.splice(index, 1);
      } else {
        this.selectedTagIds.push(id);
      }
    },
    addCustomTag() {
      const tag = this.customTagInput.trim();
      if (!tag) return;
      if (this.customTags.includes(tag)) {
        alert('该标签已添加');
        return;
      }
      if (this.customTags.length >= 10) {
        alert('最多添加10个自定义标签');
        return;
      }
      this.customTags.push(tag);
      this.customTagInput = '';
    },
    removeCustomTag(tag) {
      const index = this.customTags.indexOf(tag);
      if (index > -1) {
        this.customTags.splice(index, 1);
      }
    },
    async saveTags() {
      if (!this.uid) return;
      this.savingTags = true;
      try {
        const tagIds = this.selectedTagIds.map(Number).filter((n) => !Number.isNaN(n));
        await userTagApi.updateTagIds(tagIds);
        alert('标签保存成功！');
      } catch (e) {
        alert(e.message || '保存失败');
      } finally {
        this.savingTags = false;
      }
    },
    async saveProfile() {
      if (!this.rawUser) return;
      this.saving = true;
      try {
        await userApi.update({
          realName: this.profile.realName || null,
          nickname: this.profile.nickname,
          phone: this.profile.phone,
          college: this.profile.college,
          major: this.profile.major,
          grade:
            this.profile.grade != null && this.profile.grade !== ''
              ? Number(this.profile.grade)
              : null,
          specialty: this.rawUser?.specialty ?? null,
          hobby: this.rawUser?.hobby ?? null,
          expectation: this.rawUser?.expectation ?? null,
          avatar: this.profile.avatar || null,
        });
        if (this.profile.nickname) localStorage.setItem('nickname', this.profile.nickname);
        if (this.profile.avatar) localStorage.setItem('avatar', this.profile.avatar);
        alert('已保存');
      } catch (e) {
        alert(e.message || '保存失败');
      } finally {
        this.saving = false;
      }
    },
    formatTime(iso) {
      if (!iso) return '永久有效';
      return new Date(iso).toLocaleDateString();
    },
    copyInviteCode(code) {
      navigator.clipboard
        .writeText(code)
        .then(() => {
          alert('邀请码已复制：' + code);
        })
        .catch(() => {
          prompt('请手动复制邀请码：', code);
        });
    },
    async createInviteCode() {
      this.inviteCreating = true;
      try {
        const data = {};
        if (this.inviteForm.clubId) data.clubId = this.inviteForm.clubId;
        data.maxUses = this.inviteForm.maxUses || 10;
        if (this.inviteForm.days) {
          const exp = new Date();
          exp.setDate(exp.getDate() + this.inviteForm.days);
          data.expiresAt = exp.toISOString();
        }
        await inviteCodeApi.create(data);
        await this.loadInviteCodes();
        this.inviteForm = { clubId: null, maxUses: 10, days: 30 };
      } catch (e) {
        alert(e.message || '创建失败');
      } finally {
        this.inviteCreating = false;
      }
    },
    async deleteInviteCode(id) {
      if (!confirm('确定删除此邀请码？')) return;
      try {
        await inviteCodeApi.delete(id);
        await this.loadInviteCodes();
      } catch (e) {
        alert(e.message || '删除失败');
      }
    },
  },
  async mounted() {
    await this.load();
    await this.loadHotTags();
    await this.loadUserTags();
  },
};
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: linear-gradient(135deg, #fdf2f8 0%, #faf5ff 100%);
}
.main {
  max-width: 960px;
  margin: 0 auto;
  padding: 24px 16px 48px;
}
.login-hint {
  padding: 48px;
  text-align: center;
  color: #666;
  font-size: 16px;
}
.login-hint a {
  color: #ff6b9d;
  text-decoration: none;
  font-weight: 500;
}

.user-card {
  background: linear-gradient(135deg, #ff6b9d 0%, #ff8e53 100%);
  border-radius: 16px;
  padding: 28px;
  margin-bottom: 24px;
  box-shadow: 0 8px 24px rgba(255, 107, 157, 0.3);
  color: white;
}
.user-info {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 20px;
}
.avatar-wrapper {
  flex-shrink: 0;
}
.avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  object-fit: cover;
  border: 3px solid rgba(255, 255, 255, 0.9);
}
.avatar-placeholder {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  font-weight: 700;
  border: 3px solid rgba(255, 255, 255, 0.9);
}
.user-details {
  flex: 1;
}
.nickname {
  font-size: 24px;
  font-weight: 700;
  margin: 0 0 6px 0;
}
.user-role {
  font-size: 13px;
  margin: 0 0 4px 0;
  opacity: 0.9;
}
.real-name {
  font-size: 14px;
  margin: 0;
  opacity: 0.85;
}
.user-stats {
  display: flex;
  gap: 24px;
  padding-top: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.2);
}
.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}
.stat-number {
  font-size: 24px;
  font-weight: 700;
}
.stat-label {
  font-size: 12px;
  opacity: 0.85;
}

.admin-card {
  background: linear-gradient(135deg, rgba(255, 107, 157, 0.08), rgba(0, 161, 214, 0.08));
  border: 1px solid rgba(255, 107, 157, 0.25);
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 24px;
}
.admin-card p {
  margin: 0 0 12px 0;
  font-size: 14px;
  color: #333;
}
.admin-link {
  display: inline-block;
  padding: 8px 18px;
  border-radius: 8px;
  background: #1f2937;
  color: #fff;
  font-size: 14px;
  text-decoration: none;
  transition: opacity 0.2s;
}
.admin-link:hover {
  opacity: 0.92;
}

.modules-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}
.module-card {
  background: white;
  border-radius: 12px;
  padding: 24px 16px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  border: 2px solid transparent;
}
.module-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
}
.module-card.active {
  border-color: #ff6b9d;
  box-shadow: 0 4px 16px rgba(255, 107, 157, 0.2);
}
.module-icon {
  font-size: 36px;
  margin-bottom: 12px;
}
.module-card h3 {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin: 0 0 6px 0;
}
.module-desc {
  font-size: 12px;
  color: #999;
  margin: 0;
}

.detail-section {
  background: white;
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
}
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 2px solid #f0f0f0;
}
.section-header h2 {
  font-size: 20px;
  font-weight: 700;
  color: #333;
  margin: 0;
}
.close-btn {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  border: none;
  background: #f5f5f5;
  color: #666;
  font-size: 20px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}
.close-btn:hover {
  background: #e8e8e8;
  color: #333;
}

.form label {
  display: block;
  font-size: 13px;
  color: #666;
  margin-bottom: 4px;
  margin-top: 12px;
}
.inp {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
  box-sizing: border-box;
  transition: border-color 0.2s;
}
.inp:focus {
  outline: none;
  border-color: #ff6b9d;
}
.save {
  margin-top: 20px;
  padding: 10px 28px;
  border: none;
  border-radius: 24px;
  background: linear-gradient(135deg, #ff6b9d 0%, #ff8e53 100%);
  color: #fff;
  font-size: 14px;
  cursor: pointer;
  transition: opacity 0.2s;
}
.save:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.invite-create {
  margin-bottom: 24px;
  padding-bottom: 20px;
  border-bottom: 1px solid #f0f0f0;
}
.invite-create h3,
.invite-list h3 {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin: 0 0 12px 0;
}
.invite-form {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: flex-end;
}
.form-row {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.form-row label {
  margin-top: 0;
  font-size: 12px;
  color: #666;
}
.form-row .inp {
  min-width: 140px;
}
.btn {
  padding: 10px 20px;
  border-radius: 20px;
  font-size: 14px;
  cursor: pointer;
  border: none;
  transition: all 0.2s;
}
.btn.primary {
  background: linear-gradient(135deg, #ff6b9d 0%, #ff8e53 100%);
  color: #fff;
}
.btn.primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.muted {
  color: #999;
  font-size: 14px;
  text-align: center;
  padding: 24px;
}
.code-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.code-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px;
  background: #f8f9ff;
  border: 1px solid #e8efff;
  border-radius: 10px;
  transition: all 0.2s;
}
.code-card:hover {
  box-shadow: 0 2px 8px rgba(91, 127, 201, 0.1);
}
.code-info {
  flex: 1;
}
.code-value {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 6px;
}
.code {
  font-size: 18px;
  font-weight: 600;
  font-family: monospace;
  color: #5b7fc9;
  letter-spacing: 2px;
}
.copy-btn {
  padding: 4px 10px;
  font-size: 12px;
  border-radius: 8px;
  border: 1px solid #5b7fc9;
  background: #fff;
  color: #5b7fc9;
  cursor: pointer;
  transition: all 0.2s;
}
.copy-btn:hover {
  background: #f0f5ff;
}
.code-meta {
  font-size: 12px;
  color: #999;
  margin: 0;
  display: flex;
  gap: 6px;
}
.del-btn {
  padding: 6px 12px;
  font-size: 12px;
  border-radius: 8px;
  border: 1px solid #f56c6c;
  background: #fff;
  color: #f56c6c;
  cursor: pointer;
  transition: all 0.2s;
}
.del-btn:hover {
  background: #fef0f0;
}

.hint {
  font-size: 13px;
  color: #999;
  margin-bottom: 16px;
}
.tags-section {
  margin-bottom: 20px;
}
.tags-section h3 {
  font-size: 14px;
  color: #333;
  margin-bottom: 12px;
}
.tags-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
.tag-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  background: #f5f5f5;
  border: 2px solid transparent;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 13px;
}
.tag-btn:hover {
  background: #e8e8e8;
}
.tag-btn.selected {
  background: linear-gradient(135deg, #ff6b9d 0%, #ff8e53 100%);
  color: white;
  border-color: #ff6b9d;
}
.tag-icon {
  font-size: 14px;
}
.tag-name {
  font-weight: 500;
}
.custom-input {
  display: flex;
  gap: 10px;
  margin-bottom: 12px;
}
.custom-input .inp {
  flex: 1;
}
.add-btn {
  padding: 10px 20px;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 13px;
  cursor: pointer;
  transition: background 0.2s;
}
.add-btn:hover {
  background: #5a6fd6;
}
.custom-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.custom-tag {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 16px;
  font-size: 12px;
}
.remove-btn {
  background: none;
  border: none;
  color: white;
  font-size: 16px;
  cursor: pointer;
  opacity: 0.8;
  line-height: 1;
}
.remove-btn:hover {
  opacity: 1;
}
.selected-info {
  font-size: 13px;
  color: #666;
  margin-bottom: 16px;
}
.selected-info strong {
  color: #ff6b9d;
}

.list {
  list-style: none;
  padding: 0;
  margin: 0;
}
.list li {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 16px;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
  font-size: 14px;
}
.list li:last-child {
  border-bottom: none;
}
.status {
  padding: 2px 8px;
  border-radius: 8px;
  font-size: 12px;
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
.time {
  color: #999;
  font-size: 12px;
}

.slide-enter-active,
.slide-leave-active {
  transition: all 0.3s ease;
  overflow: hidden;
}
.slide-enter-from,
.slide-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

@media (max-width: 768px) {
  .user-card {
    padding: 20px;
  }
  .user-info {
    flex-direction: column;
    text-align: center;
  }
  .user-stats {
    justify-content: center;
  }
  .modules-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }
  .module-card {
    padding: 16px 12px;
  }
  .module-icon {
    font-size: 28px;
  }
  .detail-section {
    padding: 16px;
  }
  .invite-form {
    flex-direction: column;
  }
  .form-row .inp {
    min-width: 100%;
  }
}
</style>
