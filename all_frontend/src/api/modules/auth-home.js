import http from '../http.js';
import { envelopeData, normalizeTagsResponse } from '../helpers.js';

export const userApi = {
  login: (username, password) => http.post('/api/auth/login', { username, password }),
  register: (data) => http.post('/api/auth/register', data),
  getById: (id) => http.get(`/api/users/${id}`).then(envelopeData),
  /** 按用户主键 id 拉取公开资料（与 /api/users/{id} 一致） */
  getUserById: (id) => http.get(`/api/user/${id}`).then(envelopeData),
  /** @deprecated 使用 getUserById */
  getStudentById: (id) => http.get(`/api/user/${id}`).then(envelopeData),
  update: (data) => http.put('/api/user/info', data),
  getCurrentUser: () => http.get('/api/users/me').then(envelopeData),
};

export const homeApi = {
  overview: (hotLimit = 8, activitySize = 9) =>
    http.get('/api/home/overview', { params: { hotLimit, activitySize } }),
  banners: () => http.get('/api/home/banners').then(envelopeData),
};

export const clubTypeApi = {
  list: () => http.get('/api/tags').then(normalizeTagsResponse),
};
