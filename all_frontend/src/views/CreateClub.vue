<template>
  <div class="page-container">
    <SiteHeader />

    <!-- 主内容区 -->
    <main class="main-content">
      <div class="form-container">
        <div class="form-header">
          <h2>创建新社团</h2>
          <p>填写以下信息，提交社团创建申请</p>
        </div>

        <form class="create-form" @submit.prevent="submitForm">
          <!-- 基本信息 -->
          <div class="form-section">
            <h3 class="section-title">基本信息</h3>

            <div class="form-group">
              <label class="form-label required">社团名称</label>
              <input
                type="text"
                v-model="form.name"
                placeholder="请输入社团名称"
                class="form-input"
                maxlength="30"
              />
              <span class="form-hint">{{ form.name.length }}/30</span>
            </div>

            <div class="form-group">
              <label class="form-label required">社团类型</label>
              <div class="type-selector">
                <div
                  v-for="type in clubTypes"
                  :key="type.id"
                  class="type-item"
                  :class="{ active: form.type === type.id }"
                  @click="form.type = type.id"
                >
                  <span class="type-icon">{{ type.icon || '📁' }}</span>
                  <span class="type-name">{{ type.name }}</span>
                </div>
              </div>
            </div>

            <div class="form-group">
              <label class="form-label required">社团简介</label>
              <textarea
                v-model="form.description"
                placeholder="请简要介绍社团的宗旨、特色等"
                class="form-textarea"
                maxlength="200"
                rows="4"
              ></textarea>
              <span class="form-hint">{{ form.description.length }}/200</span>
            </div>

            <div class="form-group">
              <label class="form-label">社团Logo</label>
              <input
                ref="logoInput"
                type="file"
                accept="image/*"
                class="file-inp"
                @change="onLogoFile"
              />
              <div class="upload-area">
                <div class="upload-preview" v-if="form.logo">
                  <img :src="form.logo" alt="社团Logo" @error="onImgError" />
                  <button type="button" class="remove-btn" @click="form.logo = ''">×</button>
                </div>
                <div class="upload-box" v-else @click="$refs.logoInput.click()">
                  <svg viewBox="0 0 24 24" width="32" height="32">
                    <path fill="#ccc" d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z" />
                  </svg>
                  <span>上传Logo</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 详细信息 -->
          <div class="form-section">
            <h3 class="section-title">详细信息</h3>

            <div class="form-row">
              <div class="form-group half">
                <label class="form-label required">负责人姓名</label>
                <input
                  type="text"
                  v-model="form.leaderName"
                  placeholder="请输入负责人姓名"
                  class="form-input"
                />
              </div>
              <div class="form-group half">
                <label class="form-label required">联系电话</label>
                <input
                  type="tel"
                  v-model="form.leaderPhone"
                  placeholder="请输入联系电话"
                  class="form-input"
                />
              </div>
            </div>

            <div class="form-row">
              <div class="form-group half">
                <label class="form-label">指导老师</label>
                <input
                  type="text"
                  v-model="form.teacher"
                  placeholder="请输入指导老师姓名（选填）"
                  class="form-input"
                />
              </div>
              <div class="form-group half">
                <label class="form-label">预计人数</label>
                <input
                  type="number"
                  v-model="form.expectedMembers"
                  placeholder="预计招募人数"
                  class="form-input"
                  min="1"
                />
              </div>
            </div>

            <div class="form-group">
              <label class="form-label required">活动计划</label>
              <textarea
                v-model="form.activityPlan"
                placeholder="请描述社团的日常活动安排、年度计划等"
                class="form-textarea"
                maxlength="500"
                rows="6"
              ></textarea>
              <span class="form-hint">{{ form.activityPlan.length }}/500</span>
            </div>

            <div class="form-group">
              <label class="form-label">招新要求</label>
              <textarea
                v-model="form.requirements"
                placeholder="请描述对社员的要求（选填）"
                class="form-textarea"
                maxlength="300"
                rows="3"
              ></textarea>
              <span class="form-hint">{{ form.requirements.length }}/300</span>
            </div>
          </div>

          <!-- 兴趣标签 -->
          <div class="form-section">
            <h3 class="section-title">兴趣标签</h3>
            <p class="section-desc">选择与社团相关的标签，有助于同学发现你的社团</p>

            <div class="tags-selector">
              <div
                v-for="tag in availableTags"
                :key="tag.id"
                class="tag-item"
                :class="{ active: form.tags.includes(tag.id) }"
                @click="toggleTag(tag.id)"
              >
                {{ tag.name }}
              </div>
            </div>
          </div>

          <!-- 提交按钮 -->
          <div class="form-actions">
            <button type="button" class="btn-secondary" @click="saveDraft">保存草稿</button>
            <button type="submit" class="btn-primary" :disabled="submitting">
              {{ submitting ? '提交中...' : '提交申请' }}
            </button>
          </div>
        </form>
      </div>
    </main>
  </div>
</template>

<script>
import SiteHeader from '../components/SiteHeader.vue';
import {
  clubApi,
  clubTypeApi,
  getCurrentUserId,
  uploadApi,
  resolveBackendUrl,
  onImgError,
} from '../utils/api.js';

export default {
  name: 'CreateClub',
  components: { SiteHeader },
  data() {
    return {
      form: {
        name: '',
        type: '',
        description: '',
        logo: '',
        leaderName: '',
        leaderPhone: '',
        teacher: '',
        expectedMembers: 30,
        activityPlan: '',
        requirements: '',
        tags: [],
      },
      clubTypes: [],
      availableTags: [
        { id: 1, name: '音乐' },
        { id: 2, name: '舞蹈' },
        { id: 3, name: '摄影' },
        { id: 4, name: '绘画' },
        { id: 5, name: '书法' },
        { id: 6, name: '篮球' },
        { id: 7, name: '足球' },
        { id: 8, name: '编程' },
        { id: 9, name: '机器人' },
        { id: 10, name: '辩论' },
        { id: 11, name: '阅读' },
        { id: 12, name: '志愿者' },
        { id: 13, name: '动漫' },
        { id: 14, name: '游戏' },
        { id: 15, name: '电影' },
      ],
      submitting: false,
    };
  },
  methods: {
    onImgError,
    async loadTypes() {
      try {
        const res = await clubTypeApi.list();
        const list = res.data || [];
        this.clubTypes = list.length
          ? list.map((t, i) => ({
              id: t.id,
              name: t.name,
              icon: ['🎨', '⚽', '💻', '📚', '❤️', '🎮'][i % 6],
            }))
          : [
              { id: 1, name: '文艺类', icon: '🎨' },
              { id: 2, name: '体育类', icon: '⚽' },
              { id: 3, name: '科技类', icon: '💻' },
            ];
      } catch (e) {
        console.error(e);
        this.clubTypes = [{ id: 1, name: '默认类型', icon: '📁' }];
      }
    },
    async onLogoFile(e) {
      const f = e.target.files?.[0];
      e.target.value = '';
      if (!f) return;
      try {
        const res = await uploadApi.uploadImage(f);
        const url = res.data;
        this.form.logo = typeof url === 'string' ? url : url?.url || res.data;
        if (this.form.logo && !String(this.form.logo).startsWith('http')) {
          this.form.logo = resolveBackendUrl(this.form.logo);
        }
      } catch (err) {
        alert(err.message || '上传失败');
      }
    },
    toggleTag(tagId) {
      const index = this.form.tags.indexOf(tagId);
      if (index > -1) {
        this.form.tags.splice(index, 1);
      } else {
        if (this.form.tags.length < 5) {
          this.form.tags.push(tagId);
        }
      }
    },
    saveDraft() {
      localStorage.setItem('createClubDraft', JSON.stringify(this.form));
      alert('草稿已保存');
    },
    async submitForm() {
      if (!this.validateForm()) return;
      const uid = getCurrentUserId();
      if (!uid) {
        alert('请先登录');
        this.$router.push('/login');
        return;
      }
      this.submitting = true;
      try {
        const typeId = Number(this.form.type);
        const tagIds = [
          ...new Set([typeId, ...this.form.tags].filter((x) => x != null && !Number.isNaN(x))),
        ];
        await clubApi.create({
          name: this.form.name.trim(),
          description: this.form.description.trim(),
          tags: tagIds,
          typeId,
          logo: this.form.logo || undefined,
          activityPlan: this.form.activityPlan?.trim() || undefined,
          requirements: this.form.requirements?.trim() || undefined,
        });
        alert('社团创建成功。请重新登录后再使用顶部「社团管理」维护社团信息与活动。');
        localStorage.removeItem('createClubDraft');
        this.$router.push('/clubs');
      } catch (error) {
        alert(error.message || '提交失败，请重试');
      } finally {
        this.submitting = false;
      }
    },
    validateForm() {
      if (!this.form.name) {
        alert('请输入社团名称');
        return false;
      }
      if (!this.form.type) {
        alert('请选择社团类型');
        return false;
      }
      if (!this.form.description) {
        alert('请输入社团简介');
        return false;
      }
      if (!this.form.leaderName) {
        alert('请输入负责人姓名');
        return false;
      }
      if (!this.form.leaderPhone) {
        alert('请输入联系电话');
        return false;
      }
      if (!this.form.activityPlan) {
        alert('请输入活动计划');
        return false;
      }
      return true;
    },
  },
  mounted() {
    this.loadTypes();
    const draft = localStorage.getItem('createClubDraft');
    if (draft) {
      try {
        Object.assign(this.form, JSON.parse(draft));
      } catch (e) {}
    }
  },
};
</script>

<style scoped>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.page-container {
  min-height: 100vh;
  background-color: #f4f4f4;
  font-family:
    -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

.file-inp {
  position: absolute;
  width: 0;
  height: 0;
  opacity: 0;
  pointer-events: none;
}

/* 主内容区 */
.main-content {
  max-width: 800px;
  margin: 0 auto;
  padding: 40px 20px;
}

.form-container {
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.form-header {
  padding: 30px;
  text-align: center;
  background: linear-gradient(135deg, #ff6b9d 0%, #00a1d6 100%);
  color: white;
}

.form-header h2 {
  font-size: 24px;
  margin-bottom: 8px;
}

.form-header p {
  font-size: 14px;
  opacity: 0.9;
}

.create-form {
  padding: 30px;
}

.form-section {
  margin-bottom: 32px;
  padding-bottom: 24px;
  border-bottom: 1px solid #f0f0f0;
}

.form-section:last-of-type {
  border-bottom: none;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}

.section-desc {
  font-size: 13px;
  color: #999;
  margin-bottom: 16px;
}

.form-group {
  margin-bottom: 20px;
}

.form-row {
  display: flex;
  gap: 20px;
}

.form-group.half {
  flex: 1;
}

.form-label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 8px;
}

.form-label.required::after {
  content: '*';
  color: #ff6b9d;
  margin-left: 4px;
}

.form-input {
  width: 100%;
  padding: 12px 16px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
  outline: none;
  transition: all 0.3s;
}

.form-input:focus {
  border-color: #ff6b9d;
  box-shadow: 0 0 0 3px rgba(255, 107, 157, 0.1);
}

.form-textarea {
  width: 100%;
  padding: 12px 16px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 14px;
  outline: none;
  resize: vertical;
  transition: all 0.3s;
  font-family: inherit;
}

.form-textarea:focus {
  border-color: #ff6b9d;
  box-shadow: 0 0 0 3px rgba(255, 107, 157, 0.1);
}

.form-hint {
  display: block;
  text-align: right;
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

/* 类型选择器 */
.type-selector {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.type-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px;
  border: 2px solid #e0e0e0;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
}

.type-item:hover {
  border-color: #ff6b9d;
  background: #fff5f8;
}

.type-item.active {
  border-color: #ff6b9d;
  background: linear-gradient(135deg, rgba(255, 107, 157, 0.1) 0%, rgba(0, 161, 214, 0.1) 100%);
}

.type-icon {
  font-size: 28px;
}

.type-name {
  font-size: 14px;
  color: #333;
}

/* 上传区域 */
.upload-area {
  display: flex;
  gap: 16px;
}

.upload-box {
  width: 100px;
  height: 100px;
  border: 2px dashed #e0e0e0;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.upload-box:hover {
  border-color: #ff6b9d;
  background: #fff5f8;
}

.upload-box span {
  font-size: 12px;
  color: #999;
}

.upload-preview {
  position: relative;
  width: 100px;
  height: 100px;
}

.upload-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 12px;
}

.remove-btn {
  position: absolute;
  top: -8px;
  right: -8px;
  width: 24px;
  height: 24px;
  background: #ff4d4f;
  color: white;
  border: none;
  border-radius: 50%;
  font-size: 16px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 标签选择器 */
.tags-selector {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.tag-item {
  padding: 8px 16px;
  background: #f5f5f5;
  border-radius: 20px;
  font-size: 14px;
  color: #666;
  cursor: pointer;
  transition: all 0.3s;
}

.tag-item:hover {
  background: #ffe4ec;
  color: #ff6b9d;
}

.tag-item.active {
  background: linear-gradient(135deg, #ff6b9d 0%, #00a1d6 100%);
  color: white;
}

/* 提交按钮 */
.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 16px;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}

.btn-primary {
  padding: 12px 32px;
  background: linear-gradient(135deg, #ff6b9d 0%, #ff8e53 100%);
  color: white;
  border: none;
  border-radius: 24px;
  font-size: 15px;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(255, 107, 157, 0.3);
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-secondary {
  padding: 12px 32px;
  background: white;
  color: #666;
  border: 1px solid #e0e0e0;
  border-radius: 24px;
  font-size: 15px;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-secondary:hover {
  border-color: #ff6b9d;
  color: #ff6b9d;
}

/* 响应式 */
@media (max-width: 768px) {
  .nav-links {
    display: none;
  }

  .search-box {
    width: 160px;
  }

  .type-selector {
    grid-template-columns: repeat(2, 1fr);
  }

  .form-row {
    flex-direction: column;
    gap: 0;
  }

  .form-actions {
    flex-direction: column;
  }

  .btn-primary,
  .btn-secondary {
    width: 100%;
  }
}
</style>
