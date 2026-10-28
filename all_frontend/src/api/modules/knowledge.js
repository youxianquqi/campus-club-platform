import http from '../http.js';
import { unwrapApiData, envelopeData } from '../helpers.js';

export const knowledgeApi = {
  list: (params) => {
    const queryParams = {};
    if (params?.page) queryParams.page = params.page;
    if (params?.pageSize) queryParams.pageSize = params.pageSize;
    if (params?.category) queryParams.category = params.category;
    if (params?.keyword) queryParams.keyword = params.keyword;
    if (params?.sort) queryParams.sort = params.sort;
    return http.get('/api/knowledge', { params: queryParams }).then(unwrapApiData);
  },

  getById: (id) => http.get(`/api/knowledge/${id}`).then(unwrapApiData),

  create: (data) => http.post('/api/knowledge', data).then(envelopeData),

  update: (id, data) => http.put(`/api/knowledge/${id}`, data).then(envelopeData),

  delete: (id) => http.delete(`/api/knowledge/${id}`).then(envelopeData),

  myList: (params) => {
    const queryParams = {};
    if (params?.page) queryParams.page = params.page;
    if (params?.pageSize) queryParams.pageSize = params.pageSize;
    if (params?.sort) queryParams.sort = params.sort;
    return http.get('/api/knowledge/my', { params: queryParams }).then(unwrapApiData);
  },

  getCategories: () => http.get('/api/knowledge/categories').then(unwrapApiData),

  getRecent: (limit = 5) => {
    return http.get('/api/knowledge/recent', { params: { limit } }).then(unwrapApiData);
  },

  getPopular: (limit = 5) => {
    return http.get('/api/knowledge/popular', { params: { limit } }).then(unwrapApiData);
  },
};
