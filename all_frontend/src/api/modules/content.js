import http from '../http.js';
import { rawApi } from '../client.js';

export const postApi = {
  page: (page = 1, size = 10, clubId) => http.get('/api/posts', { params: { page, size, clubId } }),
  getById: (id) => http.get(`/api/posts/${id}`),
  create: (data) => http.post('/api/posts', data),
  update: (data) => http.put(`/api/posts/${data.id}`, data),
  delete: (id) => http.delete(`/api/posts/${id}`),
  like: (id) => http.post(`/api/posts/${id}/like`),
};

export const commentApi = {
  listByPost: (postId) => http.get(`/api/comments/post/${postId}`),
  create: (data) => http.post('/api/comments', data),
  delete: (id) => http.delete(`/api/comments/${id}`),
};

export const questionApi = {
  listByClub: (clubId) => http.get(`/api/questions/club/${clubId}`),
  getById: (id) => http.get(`/api/questions/${id}`),
  create: (data) => http.post('/api/questions', data),
  answer: (questionId, data) => http.post(`/api/questions/${questionId}/answer`, data),
  getMy: () => http.get('/api/questions/my'),
};

export const articleApi = {
  page: (page = 1, size = 10) => http.get('/api/articles', { params: { page, size } }),
  getById: (id) => http.get(`/api/articles/${id}`),
  create: (data) => http.post('/api/articles', data),
  update: (data) => http.put(`/api/articles/${data.id}`, data),
  delete: (id) => http.delete(`/api/articles/${id}`),
};

export const uploadApi = {
  uploadImage: (file, onProgress) => rawApi.upload('/api/upload/activity-image', file, onProgress),
  uploadImages: async (files, onProgress) => {
    const arr = Array.from(files || []);
    const urls = [];
    for (let i = 0; i < arr.length; i++) {
      const file = arr[i];
      const res = await uploadApi.uploadImage(file, (p) => {
        if (onProgress) {
          const base = (i / arr.length) * 100;
          onProgress(base + (Number(p) || 0) / arr.length);
        }
      });
      const u = res?.data !== undefined ? res.data : res;
      urls.push(u);
    }
    return { data: urls };
  },
  uploadAvatar: (file, onProgress) => rawApi.upload('/api/upload/avatar', file, onProgress),
  uploadClubLogo: (file, onProgress) => rawApi.upload('/api/upload/club-logo', file, onProgress),
  uploadActivityImage: (file, onProgress) =>
    rawApi.upload('/api/upload/activity-image', file, onProgress),
};
