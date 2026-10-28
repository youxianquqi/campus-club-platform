<template>
  <button
    type="button"
    class="theme-toggle"
    :class="{ compact, fab, 'on-light-surface': surface || fab }"
    :aria-pressed="isDark"
    :aria-label="isDark ? '切换为浅色模式' : '切换为深色模式'"
    :title="isDark ? '当前：深色 · 点击切换浅色' : '当前：浅色 · 点击切换深色'"
    @click="onClick"
  >
    <span class="icon" aria-hidden="true">{{ isDark ? '☀' : '☽' }}</span>
    <span v-if="!compact" class="label">{{ isDark ? '浅色' : '深色' }}</span>
  </button>
</template>

<script>
import { getTheme, toggleTheme } from '../utils/theme.js';

export default {
  name: 'ThemeToggle',
  props: {
    compact: {
      type: Boolean,
      default: false,
    },
    /** 用于顶栏渐变以外的浅色背景页（登录/注册等） */
    surface: {
      type: Boolean,
      default: false,
    },
    /** 全局浮动按钮：卡片底 + 阴影，任意页面背景均清晰 */
    fab: {
      type: Boolean,
      default: false,
    },
  },
  data() {
    return {
      isDark: false,
    };
  },
  mounted() {
    this.sync();
    window.addEventListener('theme-changed', this.sync);
  },
  beforeUnmount() {
    window.removeEventListener('theme-changed', this.sync);
  },
  methods: {
    sync() {
      this.isDark = getTheme() === 'dark';
    },
    onClick() {
      toggleTheme();
      this.sync();
      window.dispatchEvent(new CustomEvent('theme-changed'));
    },
  },
};
</script>

<style scoped>
.theme-toggle {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 10px;
  border-radius: var(--radius-full, 9999px);
  border: 1px solid rgba(255, 255, 255, 0.35);
  background: rgba(255, 255, 255, 0.15);
  color: var(--header-text-on-gradient, #fff);
  font-size: 13px;
  cursor: pointer;
  transition:
    background 0.2s,
    border-color 0.2s;
}

.theme-toggle:hover {
  background: rgba(255, 255, 255, 0.28);
  border-color: rgba(255, 255, 255, 0.55);
}

.theme-toggle.compact {
  padding: 6px 8px;
  min-width: 36px;
  justify-content: center;
}

.theme-toggle .icon {
  font-size: 16px;
  line-height: 1;
}

.theme-toggle .label {
  font-weight: 500;
}

/* 无顶栏的页面：浅色按钮 */
.theme-toggle.on-light-surface {
  border: 1px solid var(--color-border, #e0e0e0);
  background: var(--color-bg-card, #fff);
  color: var(--color-text-primary, #333);
}

.theme-toggle.on-light-surface:hover {
  border-color: var(--color-primary, #ff6b9d);
  color: var(--color-primary, #ff6b9d);
  background: rgba(255, 107, 157, 0.08);
}

html.theme-dark .theme-toggle.on-light-surface {
  border-color: var(--color-border);
  background: var(--color-bg-card);
  color: var(--color-text-primary);
}

html.theme-dark .theme-toggle.on-light-surface:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
}

.theme-toggle.fab {
  padding: 10px 14px;
  min-height: 44px;
  box-shadow: var(--shadow-md, 0 4px 12px rgba(0, 0, 0, 0.12));
}

.theme-toggle.fab.compact {
  min-width: 44px;
  padding: 10px 12px;
  justify-content: center;
}
</style>
