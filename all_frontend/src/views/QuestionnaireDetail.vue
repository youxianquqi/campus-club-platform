<template>
  <div class="page">
    <SiteHeader />
    <main class="main" v-if="questionnaire">
      <div class="hero-section">
        <div class="hero-content">
          <div class="hero-badges">
            <span class="hero-status" :class="'status-' + questionnaire.status">
              {{ getStatusLabel(questionnaire.status) }}
            </span>
            <span v-if="questionnaire.allowAnonymous" class="hero-badge">
              <svg viewBox="0 0 24 24" width="14" height="14" fill="currentColor">
                <path
                  d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"
                />
              </svg>
              允许匿名
            </span>
          </div>
          <h1>{{ questionnaire.title }}</h1>
          <p class="hero-desc" v-if="questionnaire.description">{{ questionnaire.description }}</p>
          <div class="hero-meta">
            <div class="club-info" v-if="questionnaire.clubName">
              <img :src="questionnaire.clubLogo || defaultLogo" class="club-avatar" alt="" />
              <span>{{ questionnaire.clubName }}</span>
            </div>
            <div class="time-info">
              <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor">
                <path
                  d="M11.99 2C6.47 2 2 6.48 2 12s4.47 10 9.99 10C17.52 22 22 17.52 22 12S17.52 2 11.99 2zM12 20c-4.42 0-8-3.58-8-8s3.58-8 8-8 8 3.58 8 8-3.58 8-8 8zm.5-13H11v6l5.25 3.15.75-1.23-4.5-2.67z"
                />
              </svg>
              <span v-if="questionnaire.endTime">
                {{ formatDateTime(questionnaire.startTime) }} -
                {{ formatDateTime(questionnaire.endTime) }}
              </span>
              <span v-else>长期有效</span>
            </div>
          </div>
        </div>
        <div class="hero-decoration">
          <div class="deco-circle circle-1"></div>
          <div class="deco-circle circle-2"></div>
        </div>
      </div>

      <div class="content-wrapper">
        <div v-if="submitted" class="submitted-card">
          <div class="submitted-icon">
            <svg viewBox="0 0 24 24" width="48" height="48" fill="currentColor">
              <path
                d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"
              />
            </svg>
          </div>
          <h2>已成功提交</h2>
          <p>感谢您的参与！您的回答已提交</p>
          <router-link to="/questionnaires" class="btn-back">返回问卷列表</router-link>
        </div>

        <div v-else-if="questionnaire.status === 1" class="questionnaire-form">
          <div class="form-intro">
            <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor">
              <path
                d="M11 7h2v2h-2zm0 4h2v6h-2zm1-9C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8z"
              />
            </svg>
            <span>请根据实际情况填写以下问题，答案将帮助社团更好地发展</span>
          </div>

          <div class="questions-list">
            <div
              v-for="(question, index) in questions"
              :key="question.id"
              class="question-card"
              :class="{ 'has-error': errors[question.id] }"
            >
              <div class="question-header">
                <span class="question-number">{{ index + 1 }}</span>
                <span class="question-text">{{ question.questionText }}</span>
                <span v-if="question.required" class="required-mark">*</span>
              </div>

              <div class="question-options">
                <template v-if="question.questionType === 1">
                  <label
                    v-for="option in question.options"
                    :key="option"
                    class="radio-option"
                    :class="{ selected: answers[question.id] === option }"
                  >
                    <input
                      type="radio"
                      :name="'question-' + question.id"
                      :value="option"
                      v-model="answers[question.id]"
                      @change="clearError(question.id)"
                    />
                    <span class="radio-circle"></span>
                    <span class="option-text">{{ option }}</span>
                  </label>
                </template>

                <template v-else-if="question.questionType === 2">
                  <label
                    v-for="option in question.options"
                    :key="option"
                    class="checkbox-option"
                    :class="{ selected: isOptionSelected(question.id, option) }"
                  >
                    <input
                      type="checkbox"
                      :value="option"
                      @change="toggleMultiOption(question.id, option, $event)"
                    />
                    <span class="checkbox-box"></span>
                    <span class="option-text">{{ option }}</span>
                  </label>
                </template>

                <template v-else-if="question.questionType === 3">
                  <textarea
                    v-model="answers[question.id]"
                    class="text-input"
                    :placeholder="'请输入您的回答...'"
                    rows="4"
                    @input="clearError(question.id)"
                  ></textarea>
                </template>
              </div>

              <div v-if="errors[question.id]" class="error-message">
                {{ errors[question.id] }}
              </div>
            </div>
          </div>

          <div class="form-actions">
            <button type="button" class="submit-btn" :disabled="submitting" @click="handleSubmit">
              <svg
                v-if="!submitting"
                viewBox="0 0 24 24"
                width="20"
                height="20"
                fill="currentColor"
              >
                <path d="M2.01 21L23 12 2.01 3 2 10l15 2-15 2z" />
              </svg>
              <LoadingSpinner v-else size="20px" color="#fff" />
              {{ submitting ? '提交中...' : '提交问卷' }}
            </button>
          </div>
        </div>

        <div v-else-if="questionnaire.status === 0" class="pending-card">
          <svg viewBox="0 0 24 24" width="48" height="48" fill="currentColor">
            <path
              d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-2 15l-5-5 1.41-1.41L10 14.17l7.59-7.59L19 8l-9 9z"
            />
          </svg>
          <h2>问卷尚未开始</h2>
          <p>该问卷还在准备中，请耐心等待</p>
          <router-link to="/questionnaires" class="btn-back">返回问卷列表</router-link>
        </div>

        <div v-else class="ended-card">
          <svg viewBox="0 0 24 24" width="48" height="48" fill="currentColor">
            <path
              d="M11.99 2C6.47 2 2 6.48 2 12s4.47 10 9.99 10C17.52 22 22 17.52 22 12S17.52 2 11.99 2zM12 20c-4.42 0-8-3.58-8-8s3.58-8 8-8 8 3.58 8 8-3.58 8-8 8zm.5-13H11v6l5.25 3.15.75-1.23-4.5-2.67z"
            />
          </svg>
          <h2>问卷已结束</h2>
          <p>感谢您的关注，该问卷已结束收集</p>
          <router-link to="/questionnaires" class="btn-back">返回问卷列表</router-link>
        </div>
      </div>
    </main>
    <div v-else-if="loading" class="loading-state">
      <LoadingSpinner size="48px" />
      <p>加载中...</p>
    </div>
    <div v-else-if="error" class="error-state">
      <svg viewBox="0 0 24 24" width="48" height="48" fill="currentColor">
        <path
          d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z"
        />
      </svg>
      <h2>加载失败</h2>
      <p>{{ error }}</p>
      <router-link to="/questionnaires" class="btn-back">返回问卷列表</router-link>
    </div>
  </div>
</template>

<script>
import SiteHeader from '../components/SiteHeader.vue';
import LoadingSpinner from '../components/LoadingSpinner.vue';
import { questionnaireApi } from '../api/index.js';

export default {
  name: 'QuestionnaireDetail',
  components: { SiteHeader, LoadingSpinner },
  data() {
    return {
      questionnaire: null,
      questions: [],
      answers: {},
      errors: {},
      submitting: false,
      submitted: false,
      loading: true,
      error: '',
      defaultLogo:
        'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCAxMDAgMTAwIj48Y2lyY2xlIGN4PSI1MCIgY3k9IjUwIiByPSI0NSIgZmlsbD0iI2U4ZWZmZiIvPjx0ZXh0IHg9IjUwIiB5PSI1NSIgdGV4dC1hbmNob3I9Im1pZGRsZSIgZm9udC1mYW1pbHk9IkF2ZW5pciIgZm9udC1zaXplPSIyMCIgZmlsbD0iI2M3N2EyYyI+WzwvdGV4dD48L3N2Zz4=',
    };
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
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
      });
    },
    isOptionSelected(questionId, option) {
      const answer = this.answers[questionId];
      return Array.isArray(answer) && answer.includes(option);
    },
    toggleMultiOption(questionId, option, event) {
      if (!this.answers[questionId]) {
        this.$set(this.answers, questionId, []);
      }
      const answer = this.answers[questionId];
      const index = answer.indexOf(option);
      if (event.target.checked) {
        if (index === -1) {
          answer.push(option);
        }
      } else {
        if (index !== -1) {
          answer.splice(index, 1);
        }
      }
      this.clearError(questionId);
    },
    clearError(questionId) {
      this.$delete(this.errors, questionId);
    },
    validateForm() {
      let isValid = true;
      for (const question of this.questions) {
        if (question.required) {
          const answer = this.answers[question.id];
          if (question.questionType === 1 || question.questionType === 3) {
            if (!answer || (typeof answer === 'string' && !answer.trim())) {
              this.$set(this.errors, question.id, '请填写此题');
              isValid = false;
            }
          } else if (question.questionType === 2) {
            if (!answer || !Array.isArray(answer) || answer.length === 0) {
              this.$set(this.errors, question.id, '请至少选择一个选项');
              isValid = false;
            }
          }
        }
      }
      return isValid;
    },
    async handleSubmit() {
      if (!this.validateForm()) {
        return;
      }

      this.submitting = true;
      try {
        const formattedAnswers = {};
        for (const [key, value] of Object.entries(this.answers)) {
          if (Array.isArray(value)) {
            formattedAnswers[key] = JSON.stringify(value);
          } else {
            formattedAnswers[key] = value;
          }
        }

        await questionnaireApi.submit(this.questionnaire.id, {
          answers: formattedAnswers,
        });

        this.submitted = true;
        localStorage.setItem(
          'questionnaireResponded',
          JSON.stringify([
            ...JSON.parse(localStorage.getItem('questionnaireResponded') || '[]'),
            this.questionnaire.id,
          ]),
        );
      } catch (e) {
        alert(e.message || '提交失败，请重试');
      } finally {
        this.submitting = false;
      }
    },
    async loadQuestionnaire() {
      const id = this.$route.params.id;
      if (!id) {
        this.error = '缺少问卷ID';
        this.loading = false;
        return;
      }

      try {
        const res = await questionnaireApi.getById(id);
        this.questionnaire = res.data || res;

        if (this.questionnaire.questions) {
          this.questions = Array.isArray(this.questionnaire.questions)
            ? this.questionnaire.questions
            : [];
        }
      } catch (e) {
        this.error = e.message || '加载失败';
      } finally {
        this.loading = false;
      }
    },
  },
  mounted() {
    this.loadQuestionnaire();
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
  padding: 0 16px 48px;
}

.hero-section {
  position: relative;
  background: linear-gradient(135deg, #ff6b9d 0%, #ff8e53 100%);
  border-radius: 20px;
  padding: 40px 32px;
  margin-top: 20px;
  margin-bottom: 32px;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(255, 107, 157, 0.3);
}

.hero-content {
  position: relative;
  z-index: 2;
  color: white;
}

.hero-badges {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.hero-status {
  display: inline-flex;
  align-items: center;
  padding: 6px 14px;
  border-radius: 14px;
  font-size: 13px;
  font-weight: 600;
  background: rgba(255, 255, 255, 0.25);
  backdrop-filter: blur(4px);
}

.hero-status.status-1 {
  background: rgba(103, 194, 58, 0.9);
}

.hero-status.status-2 {
  background: rgba(144, 147, 153, 0.9);
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  border-radius: 14px;
  font-size: 13px;
  background: rgba(255, 255, 255, 0.25);
  backdrop-filter: blur(4px);
}

.hero-content h1 {
  font-size: 28px;
  margin-bottom: 12px;
  color: white;
}

.hero-desc {
  font-size: 15px;
  opacity: 0.95;
  margin-bottom: 20px;
  line-height: 1.6;
}

.hero-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
}

.club-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.club-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid rgba(255, 255, 255, 0.5);
}

.time-info {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  opacity: 0.9;
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
  width: 150px;
  height: 150px;
  bottom: -60px;
  right: 100px;
}

.content-wrapper {
  max-width: 800px;
  margin: 0 auto;
}

.loading-state,
.error-state,
.submitted-card,
.pending-card,
.ended-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 64px 32px;
  background: white;
  border-radius: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  text-align: center;
  color: #666;
}

.loading-state svg,
.error-state svg {
  color: #ff6b9d;
  margin-bottom: 16px;
}

.submitted-icon {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: linear-gradient(135deg, #67c23a 0%, #95d475 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 20px;
}

.submitted-icon svg {
  color: white;
}

.error-state svg {
  color: #f56c6c;
}

.loading-state h2,
.error-state h2,
.submitted-card h2,
.pending-card h2,
.ended-card h2 {
  font-size: 22px;
  color: #333;
  margin-bottom: 8px;
}

.loading-state p,
.error-state p,
.submitted-card p,
.pending-card p,
.ended-card p {
  font-size: 14px;
  margin-bottom: 24px;
}

.btn-back {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 12px 28px;
  background: linear-gradient(135deg, #ff6b9d 0%, #ff8e53 100%);
  color: white;
  border-radius: 24px;
  text-decoration: none;
  font-weight: 600;
  transition: all 0.25s ease;
  box-shadow: 0 4px 16px rgba(255, 107, 157, 0.3);
}

.btn-back:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 24px rgba(255, 107, 157, 0.4);
  color: white;
}

.form-intro {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
  background: #fff5f7;
  border-radius: 12px;
  margin-bottom: 24px;
  font-size: 14px;
  color: #ff6b9d;
}

.questionnaire-form {
  background: white;
  border-radius: 20px;
  padding: 32px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
}

.questions-list {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.question-card {
  padding: 24px;
  background: #fafafa;
  border-radius: 16px;
  border: 2px solid transparent;
  transition: all 0.2s ease;
}

.question-card:hover {
  border-color: rgba(255, 107, 157, 0.2);
}

.question-card.has-error {
  border-color: #f56c6c;
  background: #fef0f0;
}

.question-header {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 16px;
}

.question-number {
  flex-shrink: 0;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: linear-gradient(135deg, #ff6b9d 0%, #ff8e53 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 600;
}

.question-text {
  flex: 1;
  font-size: 15px;
  font-weight: 500;
  color: #333;
  line-height: 1.5;
}

.required-mark {
  color: #f56c6c;
  font-size: 18px;
}

.question-options {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.radio-option,
.checkbox-option {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 18px;
  background: white;
  border: 2px solid #e8e8e8;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.radio-option:hover,
.checkbox-option:hover {
  border-color: #ff6b9d;
  background: #fff5f7;
}

.radio-option.selected,
.checkbox-option.selected {
  border-color: #ff6b9d;
  background: #fff5f7;
}

.radio-option input,
.checkbox-option input {
  display: none;
}

.radio-circle {
  width: 20px;
  height: 20px;
  border: 2px solid #d0d0d0;
  border-radius: 50%;
  position: relative;
  transition: all 0.2s ease;
}

.radio-option.selected .radio-circle {
  border-color: #ff6b9d;
}

.radio-option.selected .radio-circle::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #ff6b9d;
}

.checkbox-box {
  width: 20px;
  height: 20px;
  border: 2px solid #d0d0d0;
  border-radius: 6px;
  position: relative;
  transition: all 0.2s ease;
}

.checkbox-option.selected .checkbox-box {
  border-color: #ff6b9d;
  background: #ff6b9d;
}

.checkbox-option.selected .checkbox-box::after {
  content: '✓';
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: white;
  font-size: 12px;
  font-weight: bold;
}

.option-text {
  flex: 1;
  font-size: 14px;
  color: #333;
}

.text-input {
  width: 100%;
  padding: 14px 18px;
  border: 2px solid #e8e8e8;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.6;
  resize: vertical;
  transition: all 0.2s ease;
}

.text-input:focus {
  outline: none;
  border-color: #ff6b9d;
  box-shadow: 0 0 0 3px rgba(255, 107, 157, 0.1);
}

.error-message {
  margin-top: 12px;
  padding: 10px 14px;
  background: #fef0f0;
  border-radius: 8px;
  font-size: 13px;
  color: #f56c6c;
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
  background: linear-gradient(135deg, #ff6b9d 0%, #ff8e53 100%);
  color: white;
  border: none;
  border-radius: 28px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.25s ease;
  box-shadow: 0 4px 20px rgba(255, 107, 157, 0.35);
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 28px rgba(255, 107, 157, 0.45);
}

.submit-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
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

  .questionnaire-form {
    padding: 20px;
    border-radius: 16px;
  }

  .question-card {
    padding: 18px;
  }

  .submit-btn {
    width: 100%;
    padding: 14px 32px;
  }
}
</style>
