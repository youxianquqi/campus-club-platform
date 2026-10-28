<template>
  <router-view v-slot="{ Component, route }">
    <Transition :name="route.meta.transition || 'page'" mode="out-in">
      <component :is="Component" :key="route.path" />
    </Transition>
  </router-view>
  <Toast />
  <div class="global-theme-fab" title="浅色 / 深色主题">
    <ThemeToggle compact fab />
  </div>
</template>

<script>
import Toast from './components/Toast.vue';
import ThemeToggle from './components/ThemeToggle.vue';

export default {
  name: 'App',
  components: { Toast, ThemeToggle },
};
</script>

<style>
/* reset / body 见 style.css，此处仅保留路由过渡 */

.page-enter-active,
.page-leave-active {
  transition:
    opacity var(--transition-page, 0.25s ease),
    transform var(--transition-page, 0.25s ease);
}

.page-enter-from {
  opacity: 0;
  transform: translateX(12px);
}

.page-leave-to {
  opacity: 0;
  transform: translateX(-12px);
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity var(--transition-page, 0.25s ease);
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.slide-up-enter-active,
.slide-up-leave-active {
  transition:
    opacity var(--transition-page, 0.25s ease),
    transform var(--transition-page, 0.25s ease);
}

.slide-up-enter-from {
  opacity: 0;
  transform: translateY(30px);
}

.slide-up-leave-to {
  opacity: 0;
  transform: translateY(-30px);
}

.scale-enter-active,
.scale-leave-active {
  transition:
    opacity var(--transition-page, 0.25s ease),
    transform var(--transition-page, 0.25s ease);
}

.scale-enter-from {
  opacity: 0;
  transform: scale(0.95);
}

.scale-leave-to {
  opacity: 0;
  transform: scale(1.05);
}

/* 全站主题切换：左下固定，避免与各页右下角主操作 FAB 重叠 */
.global-theme-fab {
  position: fixed;
  left: 20px;
  bottom: 24px;
  z-index: 9990;
  pointer-events: auto;
}

@media (max-width: 768px) {
  .global-theme-fab {
    left: 16px;
    bottom: 20px;
  }
}
</style>
