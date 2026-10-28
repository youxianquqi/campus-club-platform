<template>
  <div class="page">
    <SiteHeader />
    <main class="main">
      <div class="page-header">
        <router-link to="/votes" class="back-link">
          <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor">
            <path d="M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z" />
          </svg>
          返回
        </router-link>
        <h1>创建投票</h1>
        <p class="page-desc">发起投票，收集成员意见，共同决策</p>
      </div>

      <div class="create-form">
        <section class="form-section basic-info">
          <h2>
            <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor">
              <path
                d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z"
              />
            </svg>
            投票基本信息
          </h2>

          <div class="form-group">
            <label>投票标题 <span class="required">*</span></label>
            <input
              v-model="form.title"
              type="text"
              class="form-input"
              placeholder="请输入投票标题"
              maxlength="100"
            />
            <span class="char-count">{{ form.title.length }}/100</span>
          </div>

          <div class="form-group">
            <label>投票说明</label>
            <textarea
              v-model="form.description"
              class="form-textarea"
              placeholder="简要说明投票目的（可选）"
              rows="3"
              maxlength="500"
            ></textarea>
            <span class="char-count">{{ form.description.length }}/500</span>
          </div>

          <div class="form-row">
            <div class="form-group">
              <label>开始时间</label>
              <input v-model="form.startTime" type="datetime-local" class="form-input" />
            </div>
            <div class="form-group">
              <label>截止时间</label>
              <input v-model="form.endTime" type="datetime-local" class="form-input" />
            </div>
          </div>

          <div class="form-group">
            <label>投票类型</label>
            <div class="vote-type-selector">
              <div
                class="type-option"
                :class="{ active: form.voteType === 1 }"
                @click="form.voteType = 1"
              >
                <div class="type-icon">
                  <svg viewBox="0 0 24 24" width="24" height="24" fill="currentColor">
                    <path
                      d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"
                    />
                  </svg>
                </div>
                <div class="type-content">
                  <h4>单选投票</h4>
                  <p>每个参与者只能选择一个选项</p>
                </div>
              </div>
              <div
                class="type-option"
                :class="{ active: form.voteType === 2 }"
                @click="form.voteType = 2"
              >
                <div class="type-icon">
                  <svg viewBox="0 0 24 24" width="24" height="24" fill="currentColor">
                    <path
                      d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm0 16H5V5h14v14z"
                    />
                    <path d="M7 12h2v5H7zm4-3h2v8h-2zm4-3h2v11h-2z" />
                  </svg>
                </div>
                <div class="type-content">
                  <h4>多选投票</h4>
                  <p>每个参与者可选择多个选项</p>
                </div>
              </div>
            </div>
          </div>

          <div v-if="form.voteType === 2" class="form-group">
            <label>最多可选数量</label>
            <div class="max-options-selector">
              <button
                v-for="n in 5"
                :key="n"
                class="option-btn"
                :class="{ active: form.maxOptions === n }"
                @click="form.maxOptions = n"
              >
                {{ n }}
              </button>
            </div>
            <p class="form-hint">参与者最多可以选择 {{ form.maxOptions }} 个选项</p>
          </div>

          <div class="form-group">
            <label class="checkbox-label">
              <input type="checkbox" v-model="form.allowAnonymous" class="checkbox-input" />
              <span class="checkbox-custom"></span>
              允许匿名投票
            </label>
            <p class="form-hint">开启后，参与者可以匿名投票</p>
          </div>

          <div class="form-group">
            <label class="checkbox-label">
              <input
                type="checkbox"
                v-model="form.showResults"
                class="checkbox-input"
                :checked="true"
              />
              <span class="checkbox-custom"></span>
              实时显示投票结果
            </label>
            <p class="form-hint">开启后，参与者在投票前可以看到当前的投票统计</p>
          </div>
        </section>

        <section class="form-section options-section">
          <div class="section-header">
            <h2>
              <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor">
                <path
                  d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-5 14H7v-2h7v2zm3-4H7v-2h10v2zm0-4H7V7h10v2z"
                />
              </svg>
              投票选项
            </h2>
            <button class="btn-add-option" @click="addOption">
              <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor">
                <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z" />
              </svg>
              添加选项
            </button>
          </div>

          <div v-if="options.length === 0" class="empty-options">
            <svg viewBox="0 0 24 24" width="48" height="48" fill="currentColor">
              <path
                d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-5 14H7v-2h7v2zm3-4H7V7h10v2z"
              />
            </svg>
            <p>还没有添加选项</p>
            <button class="btn-add-first" @click="addOption">添加第一个选项</button>
          </div>

          <div v-else class="options-list">
            <div v-for="(option, index) in options" :key="option.id" class="option-item">
              <div class="option-header">
                <div class="option-number">{{ index + 1 }}</div>
                <div class="option-actions">
                  <button class="btn-icon" @click="moveOption(index, -1)" :disabled="index === 0">
                    <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor">
                      <path d="M7.41 15.41L12 10.83l4.59 4.58L18 14l-6-6-6 6z" />
                    </svg>
                  </button>
                  <button
                    class="btn-icon"
                    @click="moveOption(index, 1)"
                    :disabled="index === options.length - 1"
                  >
                    <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor">
                      <path d="M7.41 8.59L12 13.17l4.59-4.58L18 10l-6 6-6-6z" />
                    </svg>
                  </button>
                  <button class="btn-icon delete" @click="deleteOption(index)">
                    <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor">
                      <path
                        d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"
                      />
                    </svg>
                  </button>
                </div>
              </div>
              <div class="option-body">
                <div class="form-group">
                  <input
                    v-model="option.text"
                    type="text"
                    class="form-input"
                    placeholder="请输入选项内容"
                  />
                </div>
                <div class="form-group">
                  <label>选项图片（可选）</label>
                  <div class="image-upload">
                    <img
                      v-if="option.imageUrl"
                      :src="option.imageUrl"
                      class="preview-image"
                      alt=""
                    />
                    <label class="upload-btn" v-if="!option.imageUrl">
                      <svg viewBox="0 0 24 24" width="24" height="24" fill="currentColor">
                        <path
                          d="M21 19V5c0-1.1-.9-2-2-2H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2zM8.5 13.5l2.5 3.01L14.5 12l4.5 6H5l3.5-4.5z"
                        />
                      </svg>
                      <span>添加图片</span>
                      <input
                        type="file"
                        accept="image/*"
                        @change="handleImageUpload($event, option)"
                      />
                    </label>
                    <button v-else class="btn-remove-image" @click="option.imageUrl = ''">
                      移除图片
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </section>

        <div class="form-actions">
          <button class="btn-preview" @click="showPreview = true">
            <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor">
              <path
                d="M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z"
              />
            </svg>
            预览投票
          </button>
          <button class="btn-submit" @click="submitVote" :disabled="submitting || !isValid">
            <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor">
              <path d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z" />
            </svg>
            {{ submitting ? '创建中...' : '创建投票' }}
          </button>
        </div>
      </div>
    </main>

    <div v-if="showPreview" class="preview-modal" @click.self="showPreview = false">
      <div class="preview-content">
        <div class="preview-header">
          <h2>投票预览</h2>
          <button class="btn-close" @click="showPreview = false">
            <svg viewBox="0 0 24 24" width="24" height="24" fill="currentColor">
              <path
                d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"
              />
            </svg>
          </button>
        </div>
        <div class="preview-body">
          <h1>{{ form.title || '未填写标题' }}</h1>
          <p v-if="form.description" class="preview-desc">{{ form.description }}</p>
          <div class="preview-meta">
            <span class="meta-badge">{{
              form.voteType === 1 ? '单选' : '多选（最多' + form.maxOptions + '个）'
            }}</span>
            <span v-if="form.endTime" class="meta-time">
              截止: {{ formatDateTime(form.endTime) }}
            </span>
          </div>
          <div class="preview-options">
            <div v-for="(opt, index) in options" :key="index" class="preview-option">
              <div class="option-marker">{{ String.fromCharCode(65 + index) }}</div>
              <img v-if="opt.imageUrl" :src="opt.imageUrl" class="option-image" alt="" />
              <span class="option-text">{{ opt.text || '未填写内容' }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import SiteHeader from '../components/SiteHeader.vue';
import { voteApi } from '../api/index.js';
import { clubApi } from '../api/index.js';

export default {
  name: 'CreateVote',
  components: { SiteHeader },
  data() {
    return {
      form: {
        title: '',
        description: '',
        startTime: '',
        endTime: '',
        voteType: 1,
        maxOptions: 2,
        allowAnonymous: false,
        showResults: true,
      },
      options: [],
      submitting: false,
      showPreview: false,
      clubs: [],
      selectedClubId: null,
    };
  },
  computed: {
    isValid() {
      if (!this.form.title.trim()) return false;
      if (this.options.length < 2) return false;
      return this.options.some((opt) => opt.text.trim());
    },
  },
  mounted() {
    this.loadMyClubs();
  },
  methods: {
    async loadMyClubs() {
      try {
        const res = await clubApi.getByCurrentUser();
        const data = res.data || res;
        this.clubs = Array.isArray(data) ? data : [data];
        if (this.clubs.length > 0) {
          this.selectedClubId = this.clubs[0].id;
        }
      } catch (e) {
        console.warn('加载社团失败', e);
      }
    },
    addOption() {
      this.options.push({
        id: Date.now(),
        text: '',
        imageUrl: '',
      });
    },
    deleteOption(index) {
      this.options.splice(index, 1);
    },
    moveOption(index, direction) {
      const newIndex = index + direction;
      if (newIndex < 0 || newIndex >= this.options.length) return;
      const temp = this.options[index];
      this.$set(this.options, index, this.options[newIndex]);
      this.$set(this.options, newIndex, temp);
    },
    handleImageUpload(event, option) {
      const file = event.target.files[0];
      if (!file) return;

      const reader = new FileReader();
      reader.onload = (e) => {
        option.imageUrl = e.target.result;
      };
      reader.readAsDataURL(file);
    },
    formatDateTime(iso) {
      if (!iso) return '';
      const date = new Date(iso);
      return date.toLocaleDateString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
      });
    },
    async submitVote() {
      if (!this.isValid) {
        alert('请完善投票信息');
        return;
      }

      this.submitting = true;
      try {
        const data = {
          title: this.form.title.trim(),
          description: this.form.description.trim() || null,
          startTime: this.form.startTime || null,
          endTime: this.form.endTime || null,
          voteType: this.form.voteType,
          maxOptions: this.form.voteType === 1 ? 1 : this.form.maxOptions,
          allowAnonymous: this.form.allowAnonymous ? 1 : 0,
          showResults: this.form.showResults ? 1 : 0,
          clubId: this.selectedClubId,
          options: this.options
            .filter((opt) => opt.text.trim())
            .map((opt, index) => ({
              optionText: opt.text.trim(),
              imageUrl: opt.imageUrl || null,
              sortOrder: index,
            })),
        };

        await voteApi.create(data);
        alert('投票创建成功！');
        this.$router.push('/votes');
      } catch (e) {
        alert(e.message || '创建失败，请重试');
      } finally {
        this.submitting = false;
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
  max-width: 900px;
  margin: 0 auto;
  padding: 24px 16px 48px;
}

.page-header {
  margin-bottom: 32px;
}

.back-link {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: #666;
  text-decoration: none;
  font-size: 14px;
  margin-bottom: 16px;
  transition: color 0.2s;
}

.back-link:hover {
  color: #67c23a;
}

.page-header h1 {
  font-size: 28px;
  color: #333;
  margin-bottom: 8px;
}

.page-desc {
  font-size: 15px;
  color: #666;
}

.create-form {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.form-section {
  background: white;
  border-radius: 16px;
  padding: 28px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.form-section h2 {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 18px;
  color: #333;
  margin-bottom: 24px;
}

.form-section h2 svg {
  color: #67c23a;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.section-header h2 {
  margin-bottom: 0;
}

.btn-add-option {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background: linear-gradient(135deg, #67c23a 0%, #95d475 100%);
  color: white;
  border: none;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.25s ease;
}

.btn-add-option:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(103, 194, 58, 0.3);
}

.form-group {
  margin-bottom: 20px;
  position: relative;
}

.form-group:last-child {
  margin-bottom: 0;
}

.form-group label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 8px;
}

.required {
  color: #f56c6c;
}

.form-input,
.form-textarea {
  width: 100%;
  padding: 12px 16px;
  border: 2px solid #e8efff;
  border-radius: 10px;
  font-size: 14px;
  transition: all 0.2s;
  background: white;
}

.form-input:focus,
.form-textarea:focus {
  outline: none;
  border-color: #67c23a;
  box-shadow: 0 0 0 3px rgba(103, 194, 58, 0.1);
}

.form-textarea {
  resize: vertical;
  min-height: 80px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.char-count {
  position: absolute;
  right: 12px;
  bottom: 12px;
  font-size: 12px;
  color: #999;
}

.vote-type-selector {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.type-option {
  padding: 20px;
  border: 2px solid #e8efff;
  border-radius: 14px;
  cursor: pointer;
  transition: all 0.25s ease;
  display: flex;
  gap: 16px;
}

.type-option:hover {
  border-color: #67c23a;
  background: #f0f9f0;
}

.type-option.active {
  border-color: #67c23a;
  background: #e8f5e9;
}

.type-icon {
  width: 48px;
  height: 48px;
  background: #e8f0fe;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #67c23a;
  flex-shrink: 0;
}

.type-option.active .type-icon {
  background: #67c23a;
  color: white;
}

.type-content h4 {
  font-size: 15px;
  color: #333;
  margin-bottom: 4px;
}

.type-content p {
  font-size: 13px;
  color: #666;
}

.max-options-selector {
  display: flex;
  gap: 10px;
}

.option-btn {
  width: 48px;
  height: 48px;
  border: 2px solid #e8efff;
  border-radius: 12px;
  background: white;
  font-size: 16px;
  font-weight: 600;
  color: #666;
  cursor: pointer;
  transition: all 0.2s;
}

.option-btn:hover {
  border-color: #67c23a;
  color: #67c23a;
}

.option-btn.active {
  background: linear-gradient(135deg, #67c23a 0%, #95d475 100%);
  border-color: #67c23a;
  color: white;
}

.checkbox-label {
  display: flex !important;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  user-select: none;
}

.checkbox-input {
  display: none;
}

.checkbox-custom {
  width: 20px;
  height: 20px;
  border: 2px solid #d0d7e2;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.checkbox-input:checked + .checkbox-custom {
  background: linear-gradient(135deg, #67c23a 0%, #95d475 100%);
  border-color: #67c23a;
}

.checkbox-input:checked + .checkbox-custom::after {
  content: '✓';
  color: white;
  font-size: 12px;
  font-weight: bold;
}

.form-hint {
  font-size: 12px;
  color: #999;
  margin-top: 6px;
  margin-left: 30px;
}

.empty-options {
  text-align: center;
  padding: 48px 24px;
  color: #999;
}

.empty-options svg {
  margin-bottom: 16px;
  opacity: 0.5;
}

.empty-options p {
  margin-bottom: 20px;
}

.btn-add-first {
  padding: 10px 24px;
  background: linear-gradient(135deg, #67c23a 0%, #95d475 100%);
  color: white;
  border: none;
  border-radius: 20px;
  font-size: 14px;
  cursor: pointer;
}

.options-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.option-item {
  background: #f8f9ff;
  border-radius: 14px;
  overflow: hidden;
}

.option-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: #f0f9f0;
}

.option-number {
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, #67c23a 0%, #95d475 100%);
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 14px;
}

.option-actions {
  display: flex;
  gap: 8px;
}

.btn-icon {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: white;
  border-radius: 8px;
  cursor: pointer;
  color: #666;
  transition: all 0.2s;
}

.btn-icon:hover:not(:disabled) {
  background: #e8efff;
  color: #67c23a;
}

.btn-icon.delete:hover:not(:disabled) {
  background: #fef0f0;
  color: #f56c6c;
}

.btn-icon:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.option-body {
  padding: 20px;
}

.image-upload {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.preview-image {
  width: 120px;
  height: 80px;
  object-fit: cover;
  border-radius: 8px;
  border: 2px solid #e8efff;
}

.upload-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  width: 120px;
  height: 80px;
  border: 2px dashed #d0d7e2;
  border-radius: 8px;
  cursor: pointer;
  color: #666;
  font-size: 12px;
  transition: all 0.2s;
}

.upload-btn:hover {
  border-color: #67c23a;
  color: #67c23a;
}

.upload-btn input {
  display: none;
}

.btn-remove-image {
  padding: 8px 16px;
  background: #fef0f0;
  border: 1px solid #f56c6c;
  border-radius: 8px;
  color: #f56c6c;
  font-size: 12px;
  cursor: pointer;
}

.form-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
  padding-top: 16px;
}

.btn-preview {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 28px;
  background: white;
  border: 2px solid #67c23a;
  color: #67c23a;
  border-radius: 24px;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.25s ease;
}

.btn-preview:hover {
  background: #f0f9f0;
}

.btn-submit {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 36px;
  background: linear-gradient(135deg, #67c23a 0%, #95d475 100%);
  color: white;
  border: none;
  border-radius: 24px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.25s ease;
  box-shadow: 0 4px 16px rgba(103, 194, 58, 0.3);
}

.btn-submit:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 24px rgba(103, 194, 58, 0.4);
}

.btn-submit:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.preview-modal {
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

.preview-content {
  background: white;
  border-radius: 16px;
  max-width: 700px;
  width: 100%;
  max-height: 80vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #f0f0f0;
}

.preview-header h2 {
  margin: 0;
  font-size: 18px;
}

.btn-close {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: #f5f5f5;
  border-radius: 50%;
  cursor: pointer;
  color: #666;
}

.preview-body {
  padding: 24px;
  overflow-y: auto;
}

.preview-body h1 {
  font-size: 22px;
  margin-bottom: 12px;
}

.preview-desc {
  color: #666;
  margin-bottom: 16px;
}

.preview-meta {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
  padding-bottom: 24px;
  border-bottom: 1px solid #f0f0f0;
}

.meta-badge {
  background: #e8f5e9;
  color: #67c23a;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.meta-time {
  color: #999;
  font-size: 13px;
  display: flex;
  align-items: center;
}

.preview-options {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.preview-option {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: #f8f9ff;
  border-radius: 12px;
}

.option-marker {
  width: 32px;
  height: 32px;
  background: #67c23a;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 14px;
  flex-shrink: 0;
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
}

@media (max-width: 768px) {
  .form-row,
  .vote-type-selector {
    grid-template-columns: 1fr;
  }

  .form-actions {
    flex-direction: column;
  }

  .btn-preview,
  .btn-submit {
    width: 100%;
    justify-content: center;
  }
}
</style>
