<template>
  <div class="tag-selection-page">
    <div class="tag-selection-container">
      <div class="header">
        <h1>选择你的兴趣标签</h1>
        <p>告诉我们你的兴趣，我们将为你推荐最匹配的社团</p>
      </div>

      <div class="progress-bar">
        <Transition name="progress">
          <div class="progress" :style="{ width: progressWidth }"></div>
        </Transition>
      </div>

      <div class="content">
        <section class="section">
          <h2>热门标签</h2>
          <p class="hint">点击选择你感兴趣的标签（至少选择3个）</p>
          <TransitionGroup name="tag" tag="div" class="tags-grid">
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
          </TransitionGroup>
        </section>

        <section class="section">
          <h2>自定义标签</h2>
          <p class="hint">添加你独特的兴趣标签</p>
          <div class="custom-input">
            <input
              v-model="customTagInput"
              type="text"
              placeholder="输入自定义标签，如：无人机、机器人..."
              class="input"
              @keyup.enter="addCustomTag"
            />
            <button type="button" class="add-btn" @click="addCustomTag">添加</button>
          </div>
          <TransitionGroup name="custom-tag" tag="div" class="custom-tags">
            <span v-for="tag in customTags" :key="tag" class="custom-tag">
              {{ tag }}
              <button type="button" class="remove-btn" @click="removeCustomTag(tag)">×</button>
            </span>
          </TransitionGroup>
        </section>

        <section class="section">
          <h2>补充信息（选填）</h2>
          <div class="info-form">
            <div class="form-group">
              <label>邀请码</label>
              <input
                v-model="inviteCode"
                type="text"
                class="input"
                placeholder="如有邀请码请输入，可快速加入社团"
              />
            </div>
            <div class="form-group">
              <label>专业</label>
              <input
                v-model="major"
                type="text"
                class="input"
                placeholder="请输入你的专业，如：计算机科学与技术"
              />
            </div>
          </div>
        </section>
      </div>

      <div class="footer">
        <button type="button" class="skip-btn" @click="skipSelection">跳过</button>
        <button
          type="button"
          class="submit-btn"
          :disabled="!canSubmit || submitting"
          @click="submitSelection"
        >
          {{ submitting ? '保存中...' : '完成选择' }}
        </button>
      </div>

      <p class="selected-count">
        已选择 <strong>{{ totalSelectedCount }}</strong> 个标签
        <span v-if="totalSelectedCount < 3" class="warning">（至少需要3个）</span>
      </p>
    </div>
  </div>
</template>

<script>
import { clubTypeApi, userTagApi, getCurrentUserId } from '../utils/api.js';

export default {
  name: 'TagSelection',
  data() {
    return {
      hotTags: [],
      selectedTagIds: [],
      customTags: [],
      customTagInput: '',
      inviteCode: '',
      major: '',
      submitting: false,
    };
  },
  computed: {
    totalSelectedCount() {
      return this.selectedTagIds.length + this.customTags.length;
    },
    canSubmit() {
      // 后端仅持久化字典 tag.id；至少选 3 个系统标签
      return this.selectedTagIds.length >= 3;
    },
    progressWidth() {
      const percent = Math.min((this.totalSelectedCount / 5) * 100, 100);
      return `${percent}%`;
    },
  },
  async mounted() {
    await this.loadHotTags();
    await this.loadExistingTags();
  },
  methods: {
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
    async loadExistingTags() {
      if (!getCurrentUserId()) return;
      try {
        const res = await userTagApi.listByUser();
        const raw = res.data;
        // 后端返回 { code, data: number[] }，为 tag.id 列表
        const ids = Array.isArray(raw) ? raw : [];
        ids.forEach((id) => {
          const n = Number(id);
          if (!Number.isNaN(n) && !this.selectedTagIds.some((x) => Number(x) === n)) {
            this.selectedTagIds.push(n);
          }
        });
      } catch (e) {
        console.error('加载已有标签失败:', e);
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
    skipSelection() {
      localStorage.setItem('tagSelectionSkipped', 'true');
      this.$router.push('/');
    },
    async submitSelection() {
      if (!this.canSubmit || this.submitting) return;

      const userId = getCurrentUserId();
      if (!userId) {
        alert('请先登录');
        this.$router.push('/login');
        return;
      }

      this.submitting = true;
      try {
        const tagIds = this.selectedTagIds.map(Number).filter((n) => !Number.isNaN(n));
        await userTagApi.saveTagIds(tagIds);

        localStorage.setItem('tagSelectionCompleted', 'true');
        localStorage.removeItem('tagSelectionSkipped');

        if (this.inviteCode) {
          localStorage.setItem('pendingInviteCode', this.inviteCode);
        }

        if (this.major) {
          localStorage.setItem('userMajor', this.major);
        }

        alert('标签选择完成！');
        this.$router.push('/');
      } catch (e) {
        alert(e.message || '保存失败，请重试');
      } finally {
        this.submitting = false;
      }
    },
  },
};
</script>

<style scoped>
.tag-selection-page {
  min-height: 100vh;
  background: var(--header-gradient);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  animation: pageIn 0.5s ease;
}

@keyframes pageIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.tag-selection-container {
  width: 100%;
  max-width: 800px;
  background: var(--color-bg-card);
  border-radius: 20px;
  padding: 40px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
  animation: containerIn 0.6s ease;
}

@keyframes containerIn {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.header {
  text-align: center;
  margin-bottom: 30px;
}

.header h1 {
  font-size: 28px;
  color: var(--color-text-primary);
  margin-bottom: 10px;
}

.header p {
  font-size: 16px;
  color: var(--color-text-secondary);
}

.progress-bar {
  height: 6px;
  background: var(--color-border);
  border-radius: 3px;
  margin-bottom: 30px;
  overflow: hidden;
}

.progress {
  height: 100%;
  background: linear-gradient(90deg, var(--color-primary), var(--color-secondary));
  border-radius: 3px;
  transition: width 0.3s ease;
}

.progress-enter-active,
.progress-leave-active {
  transition: width 0.3s ease;
}

.content {
  margin-bottom: 30px;
}

.section {
  margin-bottom: 30px;
  animation: sectionIn 0.5s ease;
}

@keyframes sectionIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.section h2 {
  font-size: 18px;
  color: var(--color-text-primary);
  margin-bottom: 8px;
}

.hint {
  font-size: 14px;
  color: var(--color-text-muted);
  margin-bottom: 16px;
}

.tags-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.tag-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  background: var(--color-border-light);
  border: 2px solid transparent;
  border-radius: 25px;
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 14px;
}

.tag-btn:hover {
  background: var(--color-border);
  transform: translateY(-2px);
}

.tag-btn.selected {
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-light) 100%);
  color: var(--header-text-on-gradient);
  border-color: var(--color-primary);
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(255, 107, 157, 0.3);
}

.tag-icon {
  font-size: 18px;
}

.tag-name {
  font-weight: 500;
}

.tag-enter-active,
.tag-leave-active {
  transition: all 0.3s ease;
}

.tag-enter-from {
  opacity: 0;
  transform: scale(0.8);
}

.tag-leave-to {
  opacity: 0;
  transform: scale(0.8);
}

.custom-input {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.input {
  flex: 1;
  padding: 12px 16px;
  border: 2px solid var(--color-border);
  border-radius: 10px;
  font-size: 14px;
  outline: none;
  transition:
    border-color 0.2s,
    box-shadow 0.2s;
  background: var(--color-bg-card);
  color: var(--color-text-primary);
}

.input:focus {
  border-color: var(--color-primary);
  box-shadow: var(--focus-ring-primary);
}

.add-btn {
  padding: 12px 24px;
  background: var(--recruit-primary);
  color: var(--header-text-on-gradient);
  border: none;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.add-btn:hover {
  background: var(--recruit-primary-hover);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(91, 127, 201, 0.35);
}

.custom-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.custom-tag {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 14px;
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-light) 100%);
  color: var(--header-text-on-gradient);
  border-radius: 20px;
  font-size: 13px;
  transition: all 0.2s;
}

.custom-tag:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(255, 107, 157, 0.3);
}

.custom-tag-enter-active,
.custom-tag-leave-active {
  transition: all 0.3s ease;
}

.custom-tag-enter-from {
  opacity: 0;
  transform: scale(0.8);
}

.custom-tag-leave-to {
  opacity: 0;
  transform: scale(0.8);
}

.remove-btn {
  background: none;
  border: none;
  color: var(--header-text-on-gradient);
  font-size: 18px;
  cursor: pointer;
  opacity: 0.8;
  line-height: 1;
  transition: opacity 0.2s;
}

.remove-btn:hover {
  opacity: 1;
}

.info-form {
  display: grid;
  gap: 16px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-group label {
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text-primary);
}

.form-group input {
  padding: 12px 16px;
  border: 2px solid var(--color-border);
  border-radius: 10px;
  font-size: 14px;
  font-family: inherit;
  outline: none;
  transition:
    border-color 0.2s,
    box-shadow 0.2s;
  background: var(--color-bg-card);
  color: var(--color-text-primary);
}

.form-group input:focus {
  border-color: var(--color-primary);
  box-shadow: var(--focus-ring-primary);
}

.footer {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-bottom: 20px;
}

.skip-btn {
  padding: 14px 32px;
  background: var(--color-border-light);
  color: var(--color-text-secondary);
  border: none;
  border-radius: 10px;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.2s;
}

.skip-btn:hover {
  background: var(--color-border);
  transform: translateY(-1px);
}

.submit-btn {
  padding: 14px 48px;
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-light) 100%);
  color: var(--header-text-on-gradient);
  border: none;
  border-radius: 10px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(255, 107, 157, 0.35);
}

.submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.selected-count {
  text-align: center;
  font-size: 14px;
  color: var(--color-text-secondary);
}

.selected-count strong {
  color: var(--recruit-primary);
  font-size: 18px;
  transition: all 0.3s;
}

.warning {
  color: var(--color-error);
  font-size: 13px;
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%,
  100% {
    opacity: 1;
  }
  50% {
    opacity: 0.6;
  }
}

@media (max-width: 600px) {
  .tag-selection-container {
    padding: 24px;
  }

  .header h1 {
    font-size: 22px;
  }

  .tag-btn {
    padding: 10px 16px;
    font-size: 13px;
  }

  .custom-input {
    flex-direction: column;
  }

  .footer {
    flex-direction: column;
  }

  .skip-btn,
  .submit-btn {
    width: 100%;
  }
}
</style>
