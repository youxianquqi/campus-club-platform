import http from '../http.js';
import {
  unwrapApiData,
  normalizeClubsPageResponse,
  normalizeClubEnvelope,
  normalizeArrayToDataRecords,
} from '../helpers.js';
import { rawApi } from '../client.js';

export const clubApi = {
  page: (page = 1, size = 12, typeId) =>
    http
      .get('/api/clubs', { params: { page, size, typeId } })
      .then((r) => normalizeClubsPageResponse(r, page)),
  getById: (id) => http.get(`/api/clubs/${id}`).then(normalizeClubEnvelope),
  getByLeaderId: (leaderId) =>
    http.get('/api/clubs/my', { params: { leaderId } }).then(normalizeClubEnvelope),
  create: (data) => http.post('/api/clubs', data),
  update: (data) => http.put(`/api/clubs/${data.id}`, data),
  delete: (id) => http.delete(`/api/clubs/${id}`),
  search: (keyword) =>
    http.get('/api/clubs/search', { params: { keyword } }).then(normalizeClubEnvelope),
  updateLogo: (id, logoUrl) => http.put(`/api/clubs/${id}/logo`, { logo: logoUrl }),
  updateQQGroup: (id, qqGroup) => http.put(`/api/clubs/${id}/qq-group`, { qqGroup }),
  getRecommendations: (id) => http.get(`/api/clubs/${id}/recommendations`).then(unwrapApiData),
  getSimilarClubs: (id) => http.get(`/api/clubs/${id}/similar`).then(unwrapApiData),
  updateRecommendationStatus: (recId, status) =>
    http.put(`/api/clubs/recommendations/${recId}/status`, { status }),
  getByCurrentUser: () => http.get('/api/clubs/my').then(normalizeClubEnvelope),
  memberMatch: (clubId1, clubId2) =>
    http.get('/api/clubs/member-match', { params: { clubId1, clubId2 } }).then(unwrapApiData),
};

export const clubApplyApi = {
  create: (data) => http.post('/api/club-applications', data),
  apply: (data) => http.post('/api/applications', data),
  getByUserId: () =>
    http.get('/api/applications/my').then((r) => normalizeArrayToDataRecords(r, 0)),
  getById: (id) => http.get(`/api/club-applications/${id}`),
  getByClubId: (clubId) =>
    http.get(`/api/applications/club/${clubId}`).then((r) => {
      const list = Array.isArray(r) ? r : unwrapApiData(r);
      return { data: Array.isArray(list) ? list : [] };
    }),
  getPending: () => http.get('/api/club-applications/pending'),
  approve: (id) => http.put(`/api/club-applications/${id}/approve`),
  reject: (id, reason) => http.put(`/api/club-applications/${id}/reject`, { reason: reason || '' }),
  review: (id, status) =>
    http.put(`/api/club-applications/${id}/${status === 1 ? 'approve' : 'reject'}`),
};

export const clubMemberApi = {
  listByClub: (clubId) => http.get(`/api/club-members/club/${clubId}`),
  remove: (clubId, userId) => http.delete(`/api/club-members/${clubId}/${userId}`),
  join: (clubId, userId) => http.post('/api/club-members/join', { clubId, userId }),
  check: (clubId) =>
    http.get('/api/applications/check', { params: { clubId } }).then(unwrapApiData),
};

export const clubInviteApi = {
  myList: () =>
    http
      .get('/api/invitations/my')
      .then((r) => (Array.isArray(r) ? r : unwrapApiData(r) ?? [])),
  listByClub: (clubId) => http.get(`/api/invitations/club/${clubId}`),
  create: (data) => http.post('/api/invitations', data),
  disable: () => Promise.reject(new Error('后端未提供禁用/删除邀请接口')),
  verify: (code) => http.get(`/api/invitations/verify/${code}`),
  use: (code) => http.post(`/api/invitations/use/${code}`),
  accept: (id) => http.put(`/api/invitations/${id}/accept`),
  reject: (id) => http.put(`/api/invitations/${id}/reject`),
};

export const clubHiddenTagApi = {
  listByClub: (clubId) => http.get(`/api/club-hidden-tags/club/${clubId}`).then(unwrapApiData),
  add: (data) => http.post('/api/club-hidden-tags/add', data).then(unwrapApiData),
  update: (id, data) => http.put(`/api/club-hidden-tags/${id}`, data).then(unwrapApiData),
  removeByName: (clubId, tagName) =>
    rawApi.deleteWithBody('/api/club-hidden-tags/remove', { clubId, tagName }).then(unwrapApiData),
  getVisible: (clubId) =>
    http.get(`/api/club-hidden-tags/visible/club/${clubId}`).then(unwrapApiData),
};
