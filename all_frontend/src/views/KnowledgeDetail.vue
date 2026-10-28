<template>
  <div class="page">
    <SiteHeader />
    <main class="main">
      <div v-if="loading" class="loading">
        <div class="spinner"></div>
        <p>加载中...</p>
      </div>

      <div v-else-if="!article" class="not-found">
        <span class="not-found-icon">🔍</span>
        <h2>文章不存在</h2>
        <p>抱歉，您访问的文章不存在或已被删除</p>
        <button class="back-btn" @click="$router.push('/knowledge')">返回知识库</button>
      </div>

      <article v-else class="article-container">
        <header class="article-header">
          <button class="back-btn" @click="$router.push('/knowledge')">← 返回</button>

          <div v-if="isAuthor" class="author-actions">
            <button class="edit-btn" @click="$router.push(`/knowledge/edit/${article.id}`)">
              ✏️ 编辑
            </button>
            <button class="delete-btn" @click="deleteArticle">🗑️ 删除</button>
          </div>
        </header>

        <div class="article-content">
          <div v-if="article.coverImage" class="cover-image">
            <img :src="article.coverImage" :alt="article.title" />
          </div>

          <div class="article-info">
            <div v-if="article.category" class="category-tag">
              {{ article.category }}
            </div>
            <h1 class="article-title">{{ article.title }}</h1>

            <div class="article-meta">
              <span class="meta-item">
                <span class="meta-icon">👁️</span>
                {{ article.viewCount || 0 }} 浏览
              </span>
              <span class="meta-item">
                <span class="meta-icon">❤️</span>
                {{ article.likeCount || 0 }} 点赞
              </span>
              <span class="meta-item">
                <span class="meta-icon">📅</span>
                {{ formatDate(article.createdAt) }}
              </span>
            </div>
          </div>

          <div class="article-body" v-html="article.content"></div>

          <div v-if="articleImages.length > 0" class="article-images">
            <h3>📷 图片集</h3>
            <div class="images-grid">
              <img
                v-for="(img, index) in articleImages"
                :key="index"
                :src="img"
                alt="文章图片"
                class="article-image"
                @click="openImage(img)"
              />
            </div>
          </div>
        </div>
      </article>

      <div v-if="showImageModal" class="image-modal" @click="closeImageModal">
        <img :src="currentImage" alt="大图" class="modal-image" />
        <button class="modal-close" @click="closeImageModal">×</button>
      </div>
    </main>
  </div>
</template>

<script>
import SiteHeader from '../components/SiteHeader.vue';
import { knowledgeApi } from '../api/modules/knowledge.js';

export default {
  name: 'KnowledgeDetail',
  components: {
    SiteHeader,
  },
  data() {
    return {
      article: null,
      loading: true,
      showImageModal: false,
      currentImage: '',
    };
  },
  computed: {
    isAuthor() {
      if (!this.article) return false;
      const userId = localStorage.getItem('userId');
      return userId && this.article.authorId && Number(userId) === this.article.authorId;
    },
    articleImages() {
      if (!this.article || !this.article.images) return [];
      try {
        if (typeof this.article.images === 'string') {
          return JSON.parse(this.article.images);
        }
        return this.article.images;
      } catch {
        return [];
      }
    },
  },
  async mounted() {
    await this.loadArticle();
  },
  methods: {
    async loadArticle() {
      this.loading = true;
      try {
        const id = this.$route.params.id;
        const result = await knowledgeApi.getById(id);
        this.article = result;
      } catch (error) {
        console.error('加载文章失败:', error);
        this.article = null;
      } finally {
        this.loading = false;
      }
    },
    formatDate(date) {
      if (!date) return '';
      const d = new Date(date);
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
    },
    async deleteArticle() {
      if (!confirm('确定要删除这篇文章吗？')) return;

      try {
        await knowledgeApi.delete(this.article.id);
        alert('文章已删除');
        this.$router.push('/knowledge');
      } catch (error) {
        alert(error.message || '删除失败');
      }
    },
    openImage(img) {
      this.currentImage = img;
      this.showImageModal = true;
    },
    closeImageModal() {
      this.showImageModal = false;
      this.currentImage = '';
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

.loading {
  text-align: center;
  padding: 60px 20px;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid var(--color-border-light);
  border-top-color: var(--recruit-primary);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin: 0 auto 16px;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.loading p {
  font-size: 14px;
  color: var(--color-text-muted);
  margin: 0;
}

.not-found {
  text-align: center;
  padding: 60px 20px;
  background: var(--color-bg-card);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-card);
}

.not-found-icon {
  font-size: 64px;
  display: block;
  margin-bottom: 16px;
}

.not-found h2 {
  font-size: 24px;
  color: var(--color-text-primary);
  margin: 0 0 8px 0;
}

.not-found p {
  font-size: 14px;
  color: var(--color-text-muted);
  margin: 0 0 24px 0;
}

.back-btn {
  padding: 10px 20px;
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-light) 100%);
  color: var(--header-text-on-gradient);
  border: none;
  border-radius: var(--radius-sm);
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}

.back-btn:hover {
  opacity: 0.9;
  transform: translateY(-1px);
}

.article-container {
  background: var(--color-bg-card);
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: var(--shadow-md);
}

.article-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid var(--color-border-light);
}

.author-actions {
  display: flex;
  gap: 8px;
}

.edit-btn,
.delete-btn {
  padding: 8px 16px;
  border-radius: 8px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
  border: none;
}

.edit-btn {
  background: var(--recruit-primary);
  color: var(--header-text-on-gradient);
}

.edit-btn:hover {
  background: var(--recruit-primary-hover);
}

.delete-btn {
  background: rgba(245, 108, 108, 0.12);
  color: var(--recruit-type-design);
  border: 1px solid var(--recruit-type-design);
}

.delete-btn:hover {
  background: var(--recruit-type-design);
  color: var(--header-text-on-gradient);
}

.article-content {
  padding: 32px 40px;
}

.cover-image {
  width: 100%;
  max-height: 400px;
  overflow: hidden;
  border-radius: 12px;
  margin-bottom: 24px;
}

.cover-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.article-info {
  margin-bottom: 32px;
}

.category-tag {
  display: inline-block;
  padding: 6px 14px;
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-light) 100%);
  color: var(--header-text-on-gradient);
  font-size: 13px;
  border-radius: 20px;
  margin-bottom: 12px;
}

.article-title {
  font-size: 32px;
  font-weight: 700;
  color: var(--color-text-primary);
  margin: 0 0 16px 0;
  line-height: 1.3;
}

.article-meta {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: var(--color-text-muted);
}

.meta-icon {
  font-size: 16px;
}

.article-body {
  font-size: 16px;
  line-height: 1.8;
  color: var(--color-text-primary);
  margin-bottom: 32px;
}

.article-body :deep(h2) {
  font-size: 24px;
  font-weight: 600;
  margin: 32px 0 16px 0;
  color: var(--color-text-primary);
}

.article-body :deep(h3) {
  font-size: 20px;
  font-weight: 600;
  margin: 24px 0 12px 0;
  color: var(--color-text-primary);
}

.article-body :deep(p) {
  margin: 12px 0;
}

.article-body :deep(ul),
.article-body :deep(ol) {
  padding-left: 24px;
  margin: 12px 0;
}

.article-body :deep(li) {
  margin: 6px 0;
}

.article-body :deep(pre) {
  background: var(--color-border-light);
  padding: 16px;
  border-radius: var(--radius-sm);
  overflow-x: auto;
  font-family: 'Courier New', monospace;
  font-size: 14px;
}

.article-body :deep(code) {
  background: var(--color-border-light);
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'Courier New', monospace;
  font-size: 14px;
}

.article-body :deep(img) {
  max-width: 100%;
  height: auto;
  border-radius: 8px;
  margin: 16px 0;
}

.article-images {
  margin-top: 32px;
  padding-top: 24px;
  border-top: 2px solid var(--color-border-light);
}

.article-images h3 {
  font-size: 18px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin: 0 0 16px 0;
}

.images-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 12px;
}

.article-image {
  width: 100%;
  aspect-ratio: 1;
  object-fit: cover;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  border: 2px solid var(--color-border-light);
}

.article-image:hover {
  transform: scale(1.05);
  border-color: var(--recruit-primary);
}

.image-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.9);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  cursor: pointer;
}

.modal-image {
  max-width: 90%;
  max-height: 90%;
  object-fit: contain;
  border-radius: 8px;
}

.modal-close {
  position: absolute;
  top: 20px;
  right: 20px;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: none;
  background: rgba(255, 255, 255, 0.2);
  color: white;
  font-size: 24px;
  cursor: pointer;
  transition: all 0.2s;
}

.modal-close:hover {
  background: rgba(255, 255, 255, 0.3);
}

@media (max-width: 768px) {
  .article-content {
    padding: 24px 20px;
  }

  .article-title {
    font-size: 24px;
  }

  .article-meta {
    gap: 12px;
  }

  .article-header {
    flex-direction: column;
    gap: 12px;
  }

  .author-actions {
    width: 100%;
    justify-content: flex-end;
  }
}
</style>
