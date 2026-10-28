/**
 * 前端 API 统一出口：按业务拆分为 modules/*，此处聚合导出。
 */
export {
  default as http,
  BASE_URL,
  loadingState,
  showLoading,
  hideLoading,
  isLoading,
} from './http.js';
export * from './helpers.js';
export { rawApi, api } from './client.js';

export * from './modules/auth-home.js';
export * from './modules/club.js';
export * from './modules/activity.js';
export * from './modules/platform.js';
export * from './modules/content.js';
export * from './modules/admin-behavior.js';
export * from './modules/newFeatures.js';
export * from './modules/recommendation.js';
