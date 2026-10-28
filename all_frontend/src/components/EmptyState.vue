<template>
  <div class="empty-state">
    <div class="empty-icon">{{ icon }}</div>
    <h3 class="empty-title">{{ title }}</h3>
    <p class="empty-description">{{ description }}</p>
    <div class="empty-actions" v-if="$slots.action">
      <slot name="action"></slot>
    </div>
    <button v-if="showRetry" class="retry-btn" @click="$emit('retry')">
      {{ retryText }}
    </button>
  </div>
</template>

<script>
export default {
  name: 'EmptyState',
  props: {
    type: {
      type: String,
      default: 'empty',
      validator: (value) => ['empty', 'search', 'error', 'network'].includes(value),
    },
    title: {
      type: String,
      default: '暂无数据',
    },
    description: {
      type: String,
      default: '',
    },
    icon: {
      type: String,
      default: '',
    },
    showRetry: {
      type: Boolean,
      default: false,
    },
    retryText: {
      type: String,
      default: '重新加载',
    },
  },
  emits: ['retry'],
  setup(props) {
    const iconMap = {
      empty: '📭',
      search: '🔍',
      error: '⚠️',
      network: '🌐',
    };

    const titleMap = {
      empty: '暂无数据',
      search: '未找到结果',
      error: '加载失败',
      network: '网络异常',
    };

    const descriptionMap = {
      empty: '这里还没有内容，稍后再来看看吧',
      search: '没有找到符合条件的结果，请尝试调整搜索条件',
      error: '抱歉，数据加载失败了，请稍后重试',
      network: '网络连接不稳定，请检查网络后重试',
    };

    return {
      icon: props.icon || iconMap[props.type] || iconMap.empty,
      title: props.title || titleMap[props.type] || titleMap.empty,
      description: props.description || descriptionMap[props.type] || descriptionMap.empty,
    };
  },
};
</script>

<style scoped>
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  text-align: center;
  animation: fadeIn 0.5s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 20px;
  opacity: 0.8;
}

.empty-title {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}

.empty-description {
  font-size: 14px;
  color: #999;
  line-height: 1.6;
  max-width: 400px;
  margin-bottom: 20px;
}

.empty-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  justify-content: center;
}

.retry-btn {
  padding: 10px 24px;
  background: linear-gradient(135deg, #ff6b9d 0%, #ff8e53 100%);
  color: white;
  border: none;
  border-radius: 20px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.retry-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(255, 107, 157, 0.3);
}

@media (max-width: 576px) {
  .empty-state {
    padding: 40px 16px;
  }

  .empty-icon {
    font-size: 48px;
  }

  .empty-title {
    font-size: 18px;
  }

  .empty-description {
    font-size: 13px;
  }
}
</style>
