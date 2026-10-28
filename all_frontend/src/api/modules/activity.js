import http from '../http.js';
import { unwrapApiData, envelopeData, normalizeActivitiesPage } from '../helpers.js';

export const activityApi = {
  page: (page = 0, size = 10, status) =>
    http
      .get('/api/activities', { params: { page, size, status } })
      .then((r) => normalizeActivitiesPage(r, page)),
  getById: (id) => http.get(`/api/activities/${id}`).then(envelopeData),
  getByClubId: (clubId) => http.get(`/api/activities/club/${clubId}`).then(unwrapApiData),
  getPending: () => http.get('/api/activities/pending'),
  create: (data) => http.post('/api/activities', data),
  update: (data) => http.put(`/api/activities/${data.id}`, data),
  approve: (id) => http.put(`/api/activities/${id}/approve`),
  reject: (id, reason) => http.put(`/api/activities/${id}/reject`, { reason: reason || '' }),
  delete: (id) => http.delete(`/api/activities/${id}`),
  signup: () => Promise.reject(new Error('活动报名功能尚未在后端开放，请通过社团入社申请参与。')),
  signupPair: (activityId, data) => http.post(`/api/activities/${activityId}/signup-pair`, data),
  cancelSignup: (activityId, userId) =>
    http.delete(`/api/activities/${activityId}/signup/${userId}`),
  getSignups: (activityId) => http.get(`/api/activities/${activityId}/signups`).then(unwrapApiData),
  getMyActivities: () => http.get('/api/activities/my').then(unwrapApiData),
  getWorks: (activityId) => http.get(`/api/activities/${activityId}/works`).then(unwrapApiData),
  createWork: (activityId, data) => http.post(`/api/activities/${activityId}/works`, data),
  updateWork: (activityId, data) => http.put(`/api/activities/${activityId}/works`, data),
  getMyWork: (activityId) => http.get(`/api/activities/${activityId}/works/my`).then(envelopeData),
};
