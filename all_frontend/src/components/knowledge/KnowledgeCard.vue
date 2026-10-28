<template>
  <div class="knowledge-card" @click="goToDetail">
    <div v-if="article.coverImage" class="card-cover">
      <img :src="article.coverImage" :alt="article.title" />
    </div>
    <div v-else class="card-cover-placeholder">
      <span class="placeholder-icon">📚</span>
    </div>
    <div class="card-content">
      <div class="card-category" v-if="article.category">
        <span class="category-tag">{{ article.category }}</span>
      </div>
      <h3 class="card-title">{{ article.title }}</h3>
      <p class="card-excerpt" v-html="excerpt"></p>
      <div class="card-footer">
        <div class="card-meta">
          <span class="meta-item">
            <span class="meta-icon">👁️</span>
            <span>{{ article.viewCount || 0 }}</span>
          </span>
          <span class="meta-item">
            <span class="meta-icon">❤️</span>
            <span>{{ article.likeCount || 0 }}</span>
          </span>
        </div>
        <div class="card-date">
          {{ formatDate(article.createdAt) }}
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'KnowledgeCard',
  props: {
    article: {
      type: Object,
      required: true,
    },
  },
  computed: {
    excerpt() {
      if (!this.article.content) return '';
      const plainText = this.article.content.replace(/<[^>]*>/g, '');
      return plainText.length > 80 ? plainText.substring(0, 80) + '...' : plainText;
    },
  },
  methods: {
    formatDate(date) {
      if (!date) return '';
      const d = new Date(date);
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
    },
    goToDetail() {
      this.$router.push(`/knowledge/${this.article.id}`);
    },
  },
};
</script>

<style scoped>
.knowledge-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  border: 2px solid transparent;
}

.knowledge-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.12);
  border-color: #ff6b9d;
}

.card-cover {
  width: 100%;
  height: 160px;
  overflow: hidden;
}

.card-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.knowledge-card:hover .card-cover img {
  transform: scale(1.05);
}

.card-cover-placeholder {
  width: 100%;
  height: 160px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.placeholder-icon {
  font-size: 48px;
}

.card-content {
  padding: 16px;
}

.card-category {
  margin-bottom: 8px;
}

.category-tag {
  display: inline-block;
  padding: 4px 10px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-size: 12px;
  border-radius: 12px;
  font-weight: 500;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin: 0 0 8px 0;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.card-excerpt {
  font-size: 13px;
  color: #666;
  margin: 0 0 12px 0;
  line-height: 1.6;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.card-meta {
  display: flex;
  gap: 12px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #999;
}

.meta-icon {
  font-size: 14px;
}

.card-date {
  font-size: 12px;
  color: #999;
}
</style>
