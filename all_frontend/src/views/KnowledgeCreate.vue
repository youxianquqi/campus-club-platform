<template>
  <div class="page">
    <SiteHeader />
    <main class="main">
      <div class="page-header">
        <h1>{{ isEdit ? '✏️ 编辑文章' : '✍️ 发布文章' }}</h1>
        <button class="back-btn" @click="$router.push('/knowledge')">← 返回</button>
      </div>

      <div class="form-container">
        <div class="form-group">
          <label>文章标题 *</label>
          <input
            v-model="form.title"
            type="text"
            class="input"
            placeholder="请输入文章标题"
            maxlength="200"
          />
          <span class="char-count">{{ form.title.length }} / 200</span>
        </div>

        <div class="form-group">
          <label>分类标签</label>
          <select v-model="form.category" class="select">
            <option value="">选择分类（可选）</option>
            <option v-for="cat in categories" :key="cat" :value="cat">{{ cat }}</option>
            <option value="custom">自定义分类</option>
          </select>
          <input
            v-if="form.category === 'custom'"
            v-model="customCategory"
            type="text"
            class="input"
            placeholder="请输入自定义分类"
            style="margin-top: 8px"
          />
        </div>

        <div class="form-group">
          <label>封面图片（可选）</label>
          <ImageUploader v-model="form.coverImage" />
        </div>

        <div class="form-group">
          <label>文章内容 *</label>
          <RichTextEditor v-model="form.content" placeholder="在这里编写您的文章内容..." />
        </div>

        <div class="form-group">
          <label>文章图片集（可选）</label>
          <ImageUploader v-model="form.images" />
          <p class="form-hint">可以上传多张图片，在文章中展示</p>
        </div>

        <div class="form-actions">
          <button type="button" class="cancel-btn" @click="$router.push('/knowledge')">取消</button>
          <button type="button" class="submit-btn" :disabled="submitting" @click="handleSubmit">
            {{ submitting ? '提交中...' : isEdit ? '保存修改' : '发布文章' }}
          </button>
        </div>
      </div>
    </main>
  </div>
</template>

<script>
import SiteHeader from '../components/SiteHeader.vue';
import RichTextEditor from '../components/knowledge/RichTextEditor.vue';
import ImageUploader from '../components/knowledge/ImageUploader.vue';
import { knowledgeApi } from '../api/modules/knowledge.js';

export default {
  name: 'KnowledgeCreate',
  components: {
    SiteHeader,
    RichTextEditor,
    ImageUploader,
  },
  data() {
    return {
      isEdit: false,
      articleId: null,
      form: {
        title: '',
        content: '',
        coverImage: '',
        images: [],
        category: '',
      },
      customCategory: '',
      categories: ['社团攻略', '技术教程', '体育运动', '音乐艺术', '公益活动'],
      submitting: false,
      loading: false,
    };
  },
  async mounted() {
    const id = this.$route.params.id;
    if (id) {
      this.isEdit = true;
      this.articleId = id;
      await this.loadArticle();
    }
  },
  methods: {
    async loadArticle() {
      this.loading = true;
      try {
        const result = await knowledgeApi.getById(this.articleId);
        this.form.title = result.title || '';
        this.form.content = result.content || '';
        this.form.coverImage = result.coverImage || '';
        this.form.category = result.category || '';
        if (result.images) {
          try {
            this.form.images =
              typeof result.images === 'string' ? JSON.parse(result.images) : result.images;
          } catch {
            this.form.images = [];
          }
        }
        if (!this.categories.includes(this.form.category) && this.form.category) {
          this.customCategory = this.form.category;
          this.form.category = 'custom';
        }
      } catch (error) {
        alert('加载文章失败');
        this.$router.push('/knowledge');
      } finally {
        this.loading = false;
      }
    },
    async handleSubmit() {
      if (!this.form.title.trim()) {
        alert('请输入文章标题');
        return;
      }
      if (!this.form.content.trim() || this.form.content === '<br>') {
        alert('请输入文章内容');
        return;
      }

      this.submitting = true;
      try {
        const data = {
          title: this.form.title.trim(),
          content: this.form.content,
          coverImage: this.form.coverImage || null,
          images: this.form.images.length > 0 ? JSON.stringify(this.form.images) : null,
          category:
            this.form.category === 'custom' ? this.customCategory.trim() : this.form.category,
        };

        if (this.isEdit) {
          await knowledgeApi.update(this.articleId, data);
          alert('文章已更新');
        } else {
          await knowledgeApi.create(data);
          alert('文章发布成功');
        }
        this.$router.push('/knowledge');
      } catch (error) {
        alert(error.message || '操作失败，请重试');
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
  background: var(--page-hero-tint);
}

.main {
  max-width: 900px;
  margin: 0 auto;
  padding: 24px 16px 48px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-header h1 {
  font-size: 28px;
  font-weight: 700;
  color: var(--color-text-primary);
  margin: 0;
}

.back-btn {
  padding: 10px 20px;
  background: var(--color-bg-card);
  border: 2px solid var(--recruit-primary);
  color: var(--recruit-primary);
  border-radius: var(--radius-sm);
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}

.back-btn:hover {
  background: var(--recruit-primary);
  color: var(--header-text-on-gradient);
}

.form-container {
  background: var(--color-bg-card);
  border-radius: var(--radius-lg);
  padding: 32px;
  box-shadow: var(--shadow-md);
}

.form-group {
  margin-bottom: 24px;
}

.form-group label {
  display: block;
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin-bottom: 8px;
}

.input,
.select {
  width: 100%;
  padding: 12px 16px;
  border: 2px solid var(--color-border);
  border-radius: var(--radius-sm);
  font-size: 14px;
  outline: none;
  transition: border-color 0.2s;
  box-sizing: border-box;
  background: var(--color-bg-card);
  color: var(--color-text-primary);
}

.input:focus,
.select:focus {
  border-color: var(--recruit-primary);
  box-shadow: 0 0 0 3px rgba(91, 127, 201, 0.15);
}

.char-count {
  display: block;
  text-align: right;
  font-size: 12px;
  color: var(--color-text-muted);
  margin-top: 4px;
}

.form-hint {
  font-size: 12px;
  color: var(--color-text-muted);
  margin: 8px 0 0 0;
}

.form-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  margin-top: 32px;
  padding-top: 24px;
  border-top: 2px solid var(--color-border-light);
}

.cancel-btn {
  padding: 12px 24px;
  background: var(--color-border-light);
  border: none;
  color: var(--color-text-secondary);
  border-radius: var(--radius-sm);
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.cancel-btn:hover {
  background: var(--color-border);
  color: var(--color-text-primary);
}

.submit-btn {
  padding: 12px 32px;
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-light) 100%);
  color: var(--header-text-on-gradient);
  border: none;
  border-radius: var(--radius-sm);
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 4px 12px rgba(255, 107, 157, 0.28);
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(255, 107, 157, 0.38);
}

.submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    gap: 12px;
    text-align: center;
  }

  .page-header h1 {
    font-size: 24px;
  }

  .form-container {
    padding: 20px;
  }

  .form-actions {
    flex-direction: column;
  }

  .cancel-btn,
  .submit-btn {
    width: 100%;
  }
}
</style>
