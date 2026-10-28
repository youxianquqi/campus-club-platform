<template>
  <div class="page">
    <SiteHeader />
    <main class="main">
      <div class="header-section">
        <h1>📚 知识库</h1>
        <p class="subtitle">分享知识，传递价值</p>
      </div>

      <div class="action-bar">
        <button v-if="loggedIn" class="create-btn" @click="$router.push('/knowledge/create')">
          ✍️ 发布文章
        </button>
      </div>

      <div class="filters-section">
        <div class="search-box">
          <input
            v-model="keyword"
            type="text"
            placeholder="搜索文章..."
            class="search-input"
            @input="debouncedSearch"
          />
          <button class="search-btn" @click="search">🔍</button>
        </div>

        <div class="category-filter">
          <button
            class="category-btn"
            :class="{ active: !selectedCategory }"
            @click="filterByCategory(null)"
          >
            全部
          </button>
          <button
            v-for="cat in categories"
            :key="cat"
            class="category-btn"
            :class="{ active: selectedCategory === cat }"
            @click="filterByCategory(cat)"
          >
            {{ cat }}
          </button>
        </div>

        <div class="sort-select">
          <select v-model="sort" @change="loadArticles" class="sort-dropdown">
            <option value="time">🕐 按时间排序</option>
            <option value="viewCount">👁️ 按热度排序</option>
          </select>
        </div>
      </div>

      <div v-if="loading" class="loading">
        <div class="spinner"></div>
        <p>加载中...</p>
      </div>

      <div v-else-if="articles.length === 0" class="empty-state">
        <span class="empty-icon">📭</span>
        <p>暂无文章</p>
        <button v-if="loggedIn" class="create-first-btn" @click="$router.push('/knowledge/create')">
          撰写第一篇文章
        </button>
      </div>

      <div v-else class="articles-grid">
        <KnowledgeCard v-for="article in articles" :key="article.id" :article="article" />
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
import KnowledgeCard from '../components/knowledge/KnowledgeCard.vue';
import { knowledgeApi } from '../api/modules/knowledge.js';

export default {
  name: 'KnowledgeBase',
  components: {
    SiteHeader,
    KnowledgeCard,
  },
  data() {
    return {
      articles: [],
      categories: [],
      selectedCategory: null,
      keyword: '',
      sort: 'time',
      currentPage: 1,
      pageSize: 12,
      total: 0,
      loading: false,
      searchTimeout: null,
    };
  },
  computed: {
    totalPages() {
      return Math.ceil(this.total / this.pageSize);
    },
    loggedIn() {
      return localStorage.getItem('isLoggedIn') === 'true';
    },
  },
  async mounted() {
    await this.loadCategories();
    await this.loadArticles();
  },
  methods: {
    async loadCategories() {
      try {
        const result = await knowledgeApi.getCategories();
        this.categories = result.categories || [];
      } catch (error) {
        console.error('加载分类失败:', error);
        this.categories = ['社团攻略', '技术教程', '体育运动', '音乐艺术', '公益活动'];
      }
    },
    async loadArticles() {
      this.loading = true;
      try {
        const result = await knowledgeApi.list({
          page: this.currentPage,
          pageSize: this.pageSize,
          category: this.selectedCategory,
          keyword: this.keyword,
          sort: this.sort,
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
    filterByCategory(category) {
      this.selectedCategory = category;
      this.currentPage = 1;
      this.loadArticles();
    },
    search() {
      this.currentPage = 1;
      this.loadArticles();
    },
    debouncedSearch() {
      if (this.searchTimeout) {
        clearTimeout(this.searchTimeout);
      }
      this.searchTimeout = setTimeout(() => {
        this.search();
      }, 500);
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
  max-width: var(--page-max-width);
  margin: 0 auto;
  padding: var(--spacing-xl) var(--spacing-lg) 48px;
}

.header-section {
  text-align: center;
  margin-bottom: var(--spacing-xxl);
}

.header-section h1 {
  font-size: 32px;
  font-weight: 700;
  color: var(--color-text-primary);
  margin: 0 0 8px 0;
}

.subtitle {
  font-size: var(--font-size-lg);
  color: var(--color-text-secondary);
  margin: 0;
}

.action-bar {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 24px;
}

.create-btn {
  padding: 12px 24px;
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-light) 100%);
  color: var(--header-text-on-gradient);
  border: none;
  border-radius: 24px;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition:
    transform var(--transition-normal),
    box-shadow var(--transition-normal);
  box-shadow: 0 4px 12px rgba(255, 107, 157, 0.28);
}

.create-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(255, 107, 157, 0.38);
}

.filters-section {
  background: var(--color-bg-card);
  border-radius: var(--radius-md);
  padding: 20px;
  margin-bottom: var(--spacing-xl);
  box-shadow: var(--shadow-sm);
}

.search-box {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}

.search-input {
  flex: 1;
  padding: 10px 16px;
  border: 2px solid var(--color-border);
  border-radius: var(--radius-sm);
  font-size: var(--font-size-base);
  outline: none;
  transition: border-color var(--transition-fast);
  background: var(--color-bg-card);
  color: var(--color-text-primary);
}

.search-input:focus {
  border-color: var(--recruit-primary);
}

.search-btn {
  padding: 10px 16px;
  background: var(--recruit-primary);
  color: var(--header-text-on-gradient);
  border: none;
  border-radius: var(--radius-sm);
  cursor: pointer;
  font-size: var(--font-size-lg);
  transition: background var(--transition-fast);
}

.search-btn:hover {
  background: var(--recruit-primary-hover);
}

.category-filter {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 16px;
}

.category-btn {
  padding: 8px 16px;
  background: var(--color-border-light);
  border: 2px solid transparent;
  border-radius: 20px;
  cursor: pointer;
  font-size: var(--font-size-sm);
  transition:
    background var(--transition-fast),
    color var(--transition-fast);
  color: var(--color-text-secondary);
}

.category-btn:hover {
  background: var(--color-border);
}

.category-btn.active {
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-light) 100%);
  color: var(--header-text-on-gradient);
  border-color: var(--color-primary);
}

.sort-select {
  display: flex;
  justify-content: flex-end;
}

.sort-dropdown {
  padding: 8px 16px;
  border: 2px solid var(--color-border);
  border-radius: var(--radius-sm);
  font-size: var(--font-size-sm);
  cursor: pointer;
  outline: none;
  background: var(--color-bg-card);
  color: var(--color-text-primary);
  transition: border-color var(--transition-fast);
}

.sort-dropdown:focus {
  border-color: var(--recruit-primary);
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
  font-size: var(--font-size-base);
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

.empty-state p {
  font-size: var(--font-size-lg);
  color: var(--color-text-muted);
  margin: 0 0 20px 0;
}

.create-first-btn {
  padding: 12px 24px;
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-light) 100%);
  color: var(--header-text-on-gradient);
  border: none;
  border-radius: 24px;
  font-size: var(--font-size-base);
  cursor: pointer;
  transition:
    transform var(--transition-normal),
    box-shadow var(--transition-normal);
}

.create-first-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(255, 107, 157, 0.35);
}

.articles-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
  margin-bottom: 32px;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: var(--spacing-lg);
  padding: 20px;
}

.page-btn {
  padding: 10px 20px;
  background: var(--color-bg-card);
  border: 2px solid var(--recruit-primary);
  color: var(--recruit-primary);
  border-radius: var(--radius-sm);
  cursor: pointer;
  font-size: var(--font-size-base);
  transition:
    background var(--transition-fast),
    color var(--transition-fast);
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
  font-size: var(--font-size-base);
  color: var(--color-text-secondary);
}

@media (max-width: 768px) {
  .articles-grid {
    grid-template-columns: 1fr;
  }

  .filters-section {
    padding: 16px;
  }

  .category-filter {
    overflow-x: auto;
    flex-wrap: nowrap;
    padding-bottom: 8px;
  }

  .category-btn {
    white-space: nowrap;
  }
}
</style>
