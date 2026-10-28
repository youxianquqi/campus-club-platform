<template>
  <div class="page">
    <SiteHeader />
    <main class="main">
      <div class="page-header">
        <router-link to="/questionnaires" class="back-link">
          <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor">
            <path d="M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z" />
          </svg>
          返回
        </router-link>
        <h1>创建问卷</h1>
        <p class="page-desc">设计问卷题目，收集成员意见</p>
      </div>

      <div class="create-form">
        <section class="form-section basic-info">
          <h2>
            <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor">
              <path
                d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z"
              />
            </svg>
            问卷基本信息
          </h2>

          <div class="form-group">
            <label>问卷标题 <span class="required">*</span></label>
            <input
              v-model="form.title"
              type="text"
              class="form-input"
              placeholder="请输入问卷标题"
              maxlength="100"
            />
            <span class="char-count">{{ form.title.length }}/100</span>
          </div>

          <div class="form-group">
            <label>问卷描述</label>
            <textarea
              v-model="form.description"
              class="form-textarea"
              placeholder="简要描述问卷目的（可选）"
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
            <label class="checkbox-label">
              <input type="checkbox" v-model="form.allowAnonymous" class="checkbox-input" />
              <span class="checkbox-custom"></span>
              允许匿名提交
            </label>
            <p class="form-hint">开启后，参与者可以匿名提交问卷</p>
          </div>
        </section>

        <section class="form-section questions-section">
          <div class="section-header">
            <h2>
              <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor">
                <path
                  d="M11 18h2v-2h-2v2zm1-16C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8zm0-14c-2.21 0-4 1.79-4 4h2c0-1.1.9-2 2-2s2 .9 2 2c0 2-3 1.75-3 5h2c0-2.25 3-2.5 3-5 0-2.21-1.79-4-4-4z"
                />
              </svg>
              问卷题目
            </h2>
            <button class="btn-add-question" @click="addQuestion">
              <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor">
                <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z" />
              </svg>
              添加题目
            </button>
          </div>

          <div v-if="questions.length === 0" class="empty-questions">
            <svg viewBox="0 0 24 24" width="48" height="48" fill="currentColor">
              <path
                d="M11 7h2v2h-2zm0 4h2v6h-2zm1-9C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8z"
              />
            </svg>
            <p>还没有添加题目</p>
            <button class="btn-add-first" @click="addQuestion">添加第一个题目</button>
          </div>

          <div v-else class="questions-list">
            <div v-for="(question, index) in questions" :key="question.id" class="question-item">
              <div class="question-header">
                <div class="question-number">{{ index + 1 }}</div>
                <div class="question-actions">
                  <button class="btn-icon" @click="moveQuestion(index, -1)" :disabled="index === 0">
                    <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor">
                      <path d="M7.41 15.41L12 10.83l4.59 4.58L18 14l-6-6-6 6z" />
                    </svg>
                  </button>
                  <button
                    class="btn-icon"
                    @click="moveQuestion(index, 1)"
                    :disabled="index === questions.length - 1"
                  >
                    <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor">
                      <path d="M7.41 8.59L12 13.17l4.59-4.58L18 10l-6 6-6-6z" />
                    </svg>
                  </button>
                  <button class="btn-icon delete" @click="deleteQuestion(index)">
                    <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor">
                      <path
                        d="M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"
                      />
                    </svg>
                  </button>
                </div>
              </div>

              <div class="question-body">
                <div class="form-group">
                  <label>题目内容 <span class="required">*</span></label>
                  <input
                    v-model="question.text"
                    type="text"
                    class="form-input"
                    placeholder="请输入题目内容"
                  />
                </div>

                <div class="form-row">
                  <div class="form-group">
                    <label>题目类型</label>
                    <select v-model="question.type" class="form-select">
                      <option :value="1">单选题</option>
                      <option :value="2">多选题</option>
                      <option :value="3">填空题</option>
                    </select>
                  </div>
                  <div class="form-group">
                    <label class="checkbox-label">
                      <input type="checkbox" v-model="question.required" class="checkbox-input" />
                      <span class="checkbox-custom"></span>
                      必填
                    </label>
                  </div>
                </div>

                <div v-if="question.type !== 3" class="options-group">
                  <label>选项设置</label>
                  <div class="options-list">
                    <div
                      v-for="(option, optIndex) in question.options"
                      :key="optIndex"
                      class="option-item"
                    >
                      <span class="option-marker">{{ String.fromCharCode(65 + optIndex) }}</span>
                      <input
                        v-model="question.options[optIndex]"
                        type="text"
                        class="form-input"
                        placeholder="选项内容"
                      />
                      <button
                        class="btn-icon delete"
                        @click="removeOption(question, optIndex)"
                        :disabled="question.options.length <= 2"
                      >
                        <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor">
                          <path d="M19 13H5v-2h14v2z" />
                        </svg>
                      </button>
                    </div>
                  </div>
                  <button
                    class="btn-add-option"
                    @click="addOption(question)"
                    :disabled="question.options.length >= 10"
                  >
                    <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor">
                      <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z" />
                    </svg>
                    添加选项
                  </button>
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
            预览问卷
          </button>
          <button
            class="btn-submit"
            @click="submitQuestionnaire"
            :disabled="submitting || !isValid"
          >
            <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor">
              <path d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z" />
            </svg>
            {{ submitting ? '创建中...' : '创建问卷' }}
          </button>
        </div>
      </div>
    </main>

    <div v-if="showPreview" class="preview-modal" @click.self="showPreview = false">
      <div class="preview-content">
        <div class="preview-header">
          <h2>问卷预览</h2>
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
          <div class="preview-questions">
            <div v-for="(q, index) in questions" :key="index" class="preview-question">
              <p class="preview-q-text">
                {{ index + 1 }}. {{ q.text }}
                <span v-if="q.required" class="required">*</span>
              </p>
              <p class="preview-q-type">{{ getQuestionTypeLabel(q.type) }}</p>
              <template v-if="q.type !== 3">
                <div v-for="(opt, i) in q.options" :key="i" class="preview-option">
                  {{ String.fromCharCode(65 + i) }}. {{ opt }}
                </div>
              </template>
              <template v-else>
                <input type="text" class="form-input" disabled placeholder="文本输入框" />
              </template>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import SiteHeader from '../components/SiteHeader.vue';
import { questionnaireApi } from '../api/index.js';
import { clubApi } from '../api/index.js';

export default {
  name: 'CreateQuestionnaire',
  components: { SiteHeader },
  data() {
    return {
      form: {
        title: '',
        description: '',
        startTime: '',
        endTime: '',
        allowAnonymous: false,
      },
      questions: [],
      submitting: false,
      showPreview: false,
      clubs: [],
      selectedClubId: null,
    };
  },
  computed: {
    isValid() {
      if (!this.form.title.trim()) return false;
      if (this.questions.length === 0) return false;
      for (const q of this.questions) {
        if (!q.text.trim()) return false;
        if (q.type !== 3 && q.options.some((opt) => !opt.trim())) return false;
      }
      return true;
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
    addQuestion() {
      this.questions.push({
        id: Date.now(),
        text: '',
        type: 1,
        required: true,
        options: ['', ''],
      });
    },
    deleteQuestion(index) {
      this.questions.splice(index, 1);
    },
    moveQuestion(index, direction) {
      const newIndex = index + direction;
      if (newIndex < 0 || newIndex >= this.questions.length) return;
      const temp = this.questions[index];
      this.$set(this.questions, index, this.questions[newIndex]);
      this.$set(this.questions, newIndex, temp);
    },
    addOption(question) {
      if (question.options.length >= 10) return;
      question.options.push('');
    },
    removeOption(question, index) {
      if (question.options.length <= 2) return;
      question.options.splice(index, 1);
    },
    getQuestionTypeLabel(type) {
      const labels = { 1: '单选题', 2: '多选题', 3: '填空题' };
      return labels[type] || '';
    },
    async submitQuestionnaire() {
      if (!this.isValid) {
        alert('请完善问卷信息');
        return;
      }

      this.submitting = true;
      try {
        const data = {
          title: this.form.title.trim(),
          description: this.form.description.trim() || null,
          startTime: this.form.startTime || null,
          endTime: this.form.endTime || null,
          allowAnonymous: this.form.allowAnonymous ? 1 : 0,
          clubId: this.selectedClubId,
          questions: this.questions.map((q, index) => ({
            questionText: q.text.trim(),
            questionType: q.type,
            required: q.required ? 1 : 0,
            options: q.type !== 3 ? q.options.filter((o) => o.trim()).map((o) => o.trim()) : null,
            sortOrder: index,
          })),
        };

        await questionnaireApi.create(data);
        alert('问卷创建成功！');
        this.$router.push('/questionnaires');
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
  background: linear-gradient(180deg, #fff5f7 0%, #f7f8fa 100%);
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
  color: #ff6b9d;
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
  color: #ff6b9d;
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

.btn-add-question {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background: linear-gradient(135deg, #ff6b9d 0%, #ff8e53 100%);
  color: white;
  border: none;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.25s ease;
}

.btn-add-question:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(255, 107, 157, 0.3);
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
.form-textarea,
.form-select {
  width: 100%;
  padding: 12px 16px;
  border: 2px solid #e8efff;
  border-radius: 10px;
  font-size: 14px;
  transition: all 0.2s;
  background: white;
}

.form-input:focus,
.form-textarea:focus,
.form-select:focus {
  outline: none;
  border-color: #ff6b9d;
  box-shadow: 0 0 0 3px rgba(255, 107, 157, 0.1);
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
  background: linear-gradient(135deg, #ff6b9d 0%, #ff8e53 100%);
  border-color: #ff6b9d;
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

.empty-questions {
  text-align: center;
  padding: 48px 24px;
  color: #999;
}

.empty-questions svg {
  margin-bottom: 16px;
  opacity: 0.5;
}

.empty-questions p {
  margin-bottom: 20px;
}

.btn-add-first {
  padding: 10px 24px;
  background: linear-gradient(135deg, #ff6b9d 0%, #ff8e53 100%);
  color: white;
  border: none;
  border-radius: 20px;
  font-size: 14px;
  cursor: pointer;
}

.questions-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.question-item {
  background: #f8f9ff;
  border-radius: 14px;
  overflow: hidden;
}

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: #f0f4ff;
}

.question-number {
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, #ff6b9d 0%, #ff8e53 100%);
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 14px;
}

.question-actions {
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
  color: #5b7fc9;
}

.btn-icon.delete:hover:not(:disabled) {
  background: #fef0f0;
  color: #f56c6c;
}

.btn-icon:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.question-body {
  padding: 20px;
}

.options-group {
  margin-top: 16px;
}

.options-group label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 12px;
}

.options-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 12px;
}

.option-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.option-marker {
  width: 28px;
  height: 28px;
  background: #e8f0fe;
  color: #5b7fc9;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
  flex-shrink: 0;
}

.btn-add-option {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: transparent;
  border: 2px dashed #d0d7e2;
  border-radius: 8px;
  color: #666;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-add-option:hover:not(:disabled) {
  border-color: #ff6b9d;
  color: #ff6b9d;
}

.btn-add-option:disabled {
  opacity: 0.5;
  cursor: not-allowed;
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
  border: 2px solid #ff6b9d;
  color: #ff6b9d;
  border-radius: 24px;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.25s ease;
}

.btn-preview:hover {
  background: #fff5f7;
}

.btn-submit {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 36px;
  background: linear-gradient(135deg, #ff6b9d 0%, #ff8e53 100%);
  color: white;
  border: none;
  border-radius: 24px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.25s ease;
  box-shadow: 0 4px 16px rgba(255, 107, 157, 0.3);
}

.btn-submit:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 24px rgba(255, 107, 157, 0.4);
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
  margin-bottom: 24px;
  padding-bottom: 24px;
  border-bottom: 1px solid #f0f0f0;
}

.preview-questions {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.preview-question {
  padding: 16px;
  background: #f8f9ff;
  border-radius: 12px;
}

.preview-q-text {
  font-size: 15px;
  font-weight: 500;
  color: #333;
  margin-bottom: 6px;
}

.preview-q-type {
  font-size: 12px;
  color: #999;
  margin-bottom: 12px;
}

.preview-option {
  padding: 10px 14px;
  background: white;
  border: 1px solid #e8efff;
  border-radius: 8px;
  margin-bottom: 8px;
  font-size: 14px;
  color: #666;
}

@media (max-width: 768px) {
  .form-row {
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
