<template>
  <div class="page">
    <SiteHeader />
    <main class="main">
      <div class="page-header">
        <h1>📝 我的发布</h1>
        <div class="header-actions">
          <button class="back-btn" @click="$router.push('/knowledge')">← 返回知识库</button>
          <button class="create-btn" @click="$router.push('/knowledge/create')">
            ✍️ 发布新文章
          </button>
        </div>
      </div>

      <div v-if="!loggedIn" class="login-hint">
        <span class="hint-icon">🔒</span>
        <p>请先 <router-link to="/login">登录</router-link> 查看您的发布</p>
      </div>

      <div v-else-if="loading" class="loading">
        <div class="spinner"></div>
        <p>加载中...</p>
      </div>

      <div v-else-if="articles.length === 0" class="empty-state">
        <span class="empty-icon">✍️</span>
        <h2>还没有发布任何文章</h2>
        <p>开始分享您的知识和经验吧！</p>
        <button class="create-first-btn" @click="$router.push('/knowledge/create')">
          撰写第一篇文章
        </button>
      </div>

      <div v-else class="articles-list">
        <div v-for="article in articles" :key="article.id" class="article-item">
          <div class="article-cover" @click="$router.push(`/knowledge/${article.id}`)">
            <img v-if="article.coverImage" :src="article.coverImage" :alt="article.title" />
            <div v-else class="cover-placeholder">📚</div>
          </div>
          <div class="article-info">
            <div class="article-header">
              <span class="status-tag" :class="'status-' + article.status">
                {{ getStatusText(article.status) }}
              </span>
              <span v-if="article.category" class="category-tag">{{ article.category }}</span>
            </div>
            <h3 class="article-title" @click="$router.push(`/knowledge/${article.id}`)">
              {{ article.title }}
            </h3>
            <div class="article-meta">
              <span>👁️ {{ article.viewCount || 0 }}</span>
              <span>❤️ {{ article.likeCount || 0 }}</span>
              <span>📅 {{ formatDate(article.createdAt) }}</span>
            </div>
            <div class="article-actions">
              <button
                class="action-btn edit-btn"
                @click="$router.push(`/knowledge/edit/${article.id}`)"
              >
                ✏️ 编辑
              </button>
              <button class="action-btn delete-btn" @click="deleteArticle(article.id)">
                🗑️ 删除
              </button>
            </div>
          </div>
        </div>
      </div>

      <div v-if="totalPages > 1" class="pagination">
        <button class="page-btn" :disabled="currentPage === 1" @click="goToPage(currentPage - 1)">
          上一页
        </button>
        <div class="page-info">第 {{ currentPage }} / {{ totalPages }} 页</div>
        <button
          class="page-btn"
          :disabled="currentPage === totalPages"
          @click="goToPage(currentPage + 1)"
        >
          下一页
        </button>
      </div>
    </main>
  </div>
</template>

<script>
import SiteHeader from '../components/SiteHeader.vue';
import { knowledgeApi } from '../api/modules/knowledge.js';

export default {
  name: 'KnowledgeList',
  components: {
    SiteHeader,
  },
  data() {
    return {
      articles: [],
      currentPage: 1,
      pageSize: 10,
      total: 0,
      loading: false,
    };
  },
  computed: {
    loggedIn() {
      return localStorage.getItem('isLoggedIn') === 'true';
    },
    totalPages() {
      return Math.ceil(this.total / this.pageSize);
    },
  },
  async mounted() {
    if (this.loggedIn) {
      await this.loadArticles();
    }
  },
  methods: {
    async loadArticles() {
      this.loading = true;
      try {
        const result = await knowledgeApi.myList({
          page: this.currentPage,
          pageSize: this.pageSize,
        });
        this.articles = result.articles || [];
        this.total = result.total || 0;
      } catch (error) {
        console.error('加载文章失败:', error);
        this.articles = [];
      } finally {
        this.loading = false;
      }
    },
    getStatusText(status) {
      const statusMap = {
        0: '草稿',
        1: '已发布',
        2: '已删除',
      };
      return statusMap[status] || '未知';
    },
    formatDate(date) {
      if (!date) return '';
      const d = new Date(date);
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
    },
    async deleteArticle(id) {
      if (!confirm('确定要删除这篇文章吗？')) return;
      try {
        await knowledgeApi.delete(id);
        alert('文章已删除');
        await this.loadArticles();
      } catch (error) {
        alert(error.message || '删除失败');
      }
    },
    goToPage(page) {
      if (page < 1 || page > this.totalPages) return;
      this.currentPage = page;
      this.loadArticles();
      window.scrollTo({ top: 0, behavior: 'smooth' });
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
  max-width: 1000px;
  margin: 0 auto;
  padding: 24px 16px 48px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
}

.page-header h1 {
  font-size: 28px;
  font-weight: 700;
  color: var(--color-text-primary);
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 12px;
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

.create-btn {
  padding: 10px 20px;
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-light) 100%);
  color: var(--header-text-on-gradient);
  border: none;
  border-radius: var(--radius-sm);
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
  box-shadow: 0 4px 12px rgba(255, 107, 157, 0.3);
}

.create-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(255, 107, 157, 0.4);
}

.login-hint {
  text-align: center;
  padding: 60px 20px;
  background: var(--color-bg-card);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-card);
}

.hint-icon {
  font-size: 48px;
  display: block;
  margin-bottom: 16px;
}

.login-hint p {
  font-size: 16px;
  color: var(--color-text-secondary);
  margin: 0;
}

.login-hint a {
  color: var(--recruit-primary);
  text-decoration: none;
  font-weight: 500;
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

.empty-state {
  text-align: center;
  padding: 60px 20px;
  background: var(--color-bg-card);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-card);
}

.empty-icon {
  font-size: 64px;
  display: block;
  margin-bottom: 16px;
}

.empty-state h2 {
  font-size: 24px;
  color: var(--color-text-primary);
  margin: 0 0 8px 0;
}

.empty-state p {
  font-size: 14px;
  color: var(--color-text-muted);
  margin: 0 0 24px 0;
}

.create-first-btn {
  padding: 12px 24px;
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-light) 100%);
  color: var(--header-text-on-gradient);
  border: none;
  border-radius: 24px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
}

.create-first-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(255, 107, 157, 0.35);
}

.articles-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.article-item {
  display: flex;
  gap: 20px;
  background: var(--color-bg-card);
  border-radius: var(--radius-md);
  padding: 20px;
  box-shadow: var(--shadow-sm);
  transition: all 0.3s;
}

.article-item:hover {
  box-shadow: var(--shadow-md);
}

.article-cover {
  flex-shrink: 0;
  width: 160px;
  height: 120px;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
}

.article-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.article-item:hover .article-cover img {
  transform: scale(1.05);
}

.cover-placeholder {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, var(--recruit-primary) 0%, var(--color-secondary) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36px;
}

.article-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.article-header {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
}

.status-tag {
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.status-0 {
  background: rgba(250, 173, 20, 0.15);
  color: var(--color-warning);
}

.status-1 {
  background: rgba(82, 196, 26, 0.12);
  color: var(--color-success);
}

.status-2 {
  background: rgba(207, 19, 34, 0.1);
  color: var(--color-error);
}

.category-tag {
  padding: 4px 10px;
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-light) 100%);
  color: var(--header-text-on-gradient);
  font-size: 12px;
  border-radius: 12px;
}

.article-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin: 0 0 8px 0;
  cursor: pointer;
  transition: color 0.2s;
}

.article-title:hover {
  color: var(--recruit-primary);
}

.article-meta {
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: var(--color-text-muted);
  margin-bottom: 12px;
}

.article-actions {
  display: flex;
  gap: 8px;
  margin-top: auto;
}

.action-btn {
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

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  padding: 20px;
}

.page-btn {
  padding: 10px 20px;
  background: var(--color-bg-card);
  border: 2px solid var(--recruit-primary);
  color: var(--recruit-primary);
  border-radius: var(--radius-sm);
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}

.page-btn:hover:not(:disabled) {
  background: var(--recruit-primary);
  color: var(--header-text-on-gradient);
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-info {
  font-size: 14px;
  color: var(--color-text-secondary);
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    gap: 16px;
  }

  .header-actions {
    width: 100%;
    flex-direction: column;
  }

  .article-item {
    flex-direction: column;
  }

  .article-cover {
    width: 100%;
    height: 200px;
  }
}
</style>
