<template>
  <Teleport to="body">
    <TransitionGroup name="toast" tag="div" class="toast-container">
      <div
        v-for="toastItem in toasts"
        :key="toastItem.id"
        class="toast"
        :class="toastItem.type"
        role="alert"
        @mouseenter="pauseToast(toastItem.id)"
        @mouseleave="resumeToast(toastItem.id)"
      >
        <span class="toast-icon">{{ getIcon(toastItem.type) }}</span>
        <span class="toast-message">{{ toastItem.message }}</span>
        <button class="toast-close" @click="removeToast(toastItem.id)">×</button>
      </div>
    </TransitionGroup>
  </Teleport>
</template>

<script>
import { reactive, toRefs } from 'vue';

let toastId = 0;

const toasts = reactive([]);

function show(message, type = 'info', duration = 3000) {
  const id = ++toastId;
  const toastItem = {
    id,
    message,
    type,
    duration,
    remaining: duration,
    timer: null,
    startTime: null,
    paused: false,
  };

  toasts.push(toastItem);

  if (duration > 0) {
    startTimer(toastItem);
  }

  return id;
}

function startTimer(toastItem) {
  toastItem.startTime = Date.now();
  toastItem.timer = setTimeout(() => {
    removeToast(toastItem.id);
  }, toastItem.remaining);
}

function pauseToast(id) {
  const toastItem = toasts.find((t) => t.id === id);
  if (toastItem && toastItem.timer) {
    clearTimeout(toastItem.timer);
    toastItem.remaining -= Date.now() - toastItem.startTime;
    toastItem.paused = true;
  }
}

function resumeToast(id) {
  const toastItem = toasts.find((t) => t.id === id);
  if (toastItem && toastItem.paused && toastItem.remaining > 0) {
    toastItem.paused = false;
    startTimer(toastItem);
  }
}

function removeToast(id) {
  const index = toasts.findIndex((t) => t.id === id);
  if (index > -1) {
    const toastItem = toasts[index];
    if (toastItem.timer) {
      clearTimeout(toastItem.timer);
    }
    toasts.splice(index, 1);
  }
}

function close() {
  while (toasts.length > 0) {
    removeToast(toasts[0].id);
  }
}

function toast(message, type = 'info', duration = 3000) {
  return show(message, type, duration);
}

toast.success = (message, duration = 3000) => show(message, 'success', duration);
toast.error = (message, duration = 4000) => show(message, 'error', duration);
toast.warning = (message, duration = 3500) => show(message, 'warning', duration);
toast.info = (message, duration = 3000) => show(message, 'info', duration);
toast.remove = removeToast;
toast.clear = close;

export default {
  name: 'Toast',
  setup() {
    const getIcon = (type) => {
      const iconMap = {
        success: '✓',
        error: '✕',
        warning: '⚠',
        info: 'ℹ',
      };
      return iconMap[type] || iconMap.info;
    };

    return {
      toasts,
      getIcon,
      removeToast,
      pauseToast,
      resumeToast,
    };
  },
};

export { toast };
</script>

<style scoped>
.toast-container {
  position: fixed;
  top: 80px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 9999;
  display: flex;
  flex-direction: column;
  gap: 10px;
  pointer-events: none;
}

.toast {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 20px;
  border-radius: 10px;
  font-size: 14px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  max-width: 420px;
  pointer-events: auto;
  background: white;
  border-left: 4px solid;
}

.toast.info {
  border-left-color: #1890ff;
  color: #333;
}

.toast.success {
  border-left-color: #52c41a;
  color: #333;
}

.toast.error {
  border-left-color: #ff4d4f;
  color: #333;
}

.toast.warning {
  border-left-color: #faad14;
  color: #333;
}

.toast-icon {
  font-size: 18px;
  font-weight: bold;
  flex-shrink: 0;
}

.toast.info .toast-icon {
  color: #1890ff;
}
.toast.success .toast-icon {
  color: #52c41a;
}
.toast.error .toast-icon {
  color: #ff4d4f;
}
.toast.warning .toast-icon {
  color: #faad14;
}

.toast-message {
  flex: 1;
  line-height: 1.5;
}

.toast-close {
  background: none;
  border: none;
  font-size: 20px;
  color: #999;
  cursor: pointer;
  padding: 0;
  margin-left: 8px;
  opacity: 0.6;
  transition: opacity 0.2s;
  line-height: 1;
}

.toast-close:hover {
  opacity: 1;
}

.toast-enter-active {
  animation: toast-in 0.3s ease-out;
}

.toast-leave-active {
  animation: toast-out 0.3s ease-in;
}

.toast-move {
  transition: transform 0.3s ease;
}

@keyframes toast-in {
  0% {
    opacity: 0;
    transform: translateY(-20px) scale(0.95);
  }
  100% {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

@keyframes toast-out {
  0% {
    opacity: 1;
    transform: translateX(0) scale(1);
  }
  100% {
    opacity: 0;
    transform: translateX(100%) scale(0.95);
  }
}

@media (max-width: 576px) {
  .toast-container {
    width: calc(100vw - 32px);
    top: 60px;
  }

  .toast {
    max-width: 100%;
  }
}
</style>
