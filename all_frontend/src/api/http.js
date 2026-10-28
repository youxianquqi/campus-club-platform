import axios from 'axios';
import { reactive } from 'vue';
import { toast } from '../components/Toast.vue';

const BASE_URL =
  import.meta.env.VITE_API_BASE_URL ?? (import.meta.env.DEV ? '' : 'http://localhost:8080');

const http = axios.create({
  baseURL: BASE_URL,
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json',
  },
});

export const loadingState = reactive({
  global: false,
  requests: {},
});

let requestId = 0;

http.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    if (config.showLoading !== false) {
      const id = ++requestId;
      config.requestId = id;
      loadingState.requests[id] = true;
      updateGlobalLoading();
    }

    return config;
  },
  (error) => Promise.reject(error),
);

function updateGlobalLoading() {
  const count = Object.keys(loadingState.requests).filter((k) => loadingState.requests[k]).length;
  loadingState.global = count > 0;
}

http.interceptors.response.use(
  (response) => {
    const config = response.config;
    if (config.requestId) {
      delete loadingState.requests[config.requestId];
      updateGlobalLoading();
    }

    const res = response.data;
    if (res.code !== 200 && res.code !== undefined) {
      return Promise.reject(new Error(res.message || '请求失败'));
    }
    return res;
  },
  (error) => {
    if (error.config?.requestId) {
      delete loadingState.requests[error.config.requestId];
      updateGlobalLoading();
    }

    if (error.response) {
      const { status, data } = error.response;
      const msg =
        (data && typeof data === 'object' && data.message) ||
        (typeof data === 'string' ? data : null) ||
        error.message ||
        `请求失败 (${status})`;

      if (status === 401) {
        localStorage.removeItem('token');
        localStorage.removeItem('userId');
        localStorage.removeItem('accountUserId');
        localStorage.removeItem('isLoggedIn');
        window.location.href = '/login';
        return Promise.reject(new Error(msg));
      }

      if (status >= 500) {
        toast.error(msg || '服务器内部错误');
      }

      return Promise.reject(new Error(msg));
    }

    if (error.code === 'ECONNABORTED') {
      return Promise.reject(new Error('请求超时，请检查网络连接'));
    }

    if (!navigator.onLine) {
      return Promise.reject(new Error('网络连接已断开'));
    }

    return Promise.reject(error);
  },
);

export function showLoading() {
  loadingState.global = true;
}

export function hideLoading() {
  loadingState.global = false;
}

export function isLoading() {
  return loadingState.global;
}

export default http;
export { BASE_URL };
