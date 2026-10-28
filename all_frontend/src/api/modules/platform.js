import http from '../http.js';
import { unwrapApiData, envelopeData } from '../helpers.js';

export const announcementApi = {
  list: () => http.get('/api/announcements'),
  getById: (id) => http.get(`/api/announcements/${id}`),
  getUnreadCount: () => http.get('/api/announcements/unread-count'),
  getWithReadStatus: () => http.get('/api/announcements/with-read-status'),
  markRead: (id) => http.post(`/api/announcements/${id}/read`),
  create: (data) => http.post('/api/announcements', data),
  update: (data) => http.put(`/api/announcements/${data.id}`, data),
  delete: (id) => http.delete(`/api/announcements/${id}`),
};

export const chatApi = {
  clubContext: () => http.get('/api/chat/context').then(unwrapApiData),
  clubThreads: () => http.get('/api/chat/threads').then(unwrapApiData),
  getMessages: (params) => http.get('/api/chat/messages', { params }).then(unwrapApiData),
  sendMessage: (data) => http.post('/api/chat/messages', data).then(unwrapApiData),
  markAsRead: (senderId, receiverId) =>
    http.put('/api/chat/messages/read', { senderId, receiverId }).then(unwrapApiData),
  getUnreadCount: () => http.get('/api/chat/unread-count').then(unwrapApiData),
};

export const messageApi = {
  getConversations: (userId) => http.get('/api/messages/conversations', { params: { userId } }),
  getMessages: (userId, otherId, page = 1, size = 50) =>
    http.get('/api/messages', { params: { userId, otherId, page, size } }),
  send: (data) => http.post('/api/messages', data),
  markRead: (userId, targetId) =>
    http.put('/api/messages/read', { senderId: targetId, receiverId: userId }),
  getUnreadCount: (userId) => http.get('/api/messages/unread-count', { params: { userId } }),
};

/** 用户兴趣标签（后端 user 表合并原 student，路径为 /api/user/tags） */
export const userTagApi = {
  listByUser: () =>
    http.get('/api/user/tags').then((r) => {
      const ids = Array.isArray(r?.data) ? r.data : unwrapApiData(r);
      return { data: Array.isArray(ids) ? ids : [] };
    }),
  saveTagIds: (tagIds) => http.post('/api/user/tags', { tagIds }),
  updateTagIds: (tagIds) => http.put('/api/user/tags', { tagIds }),
  deleteTags: () => http.delete('/api/user/tags'),
  add: (userId, tagName, description) =>
    http.post('/api/user/tags', { userId, tagName, description }),
  remove: (userId, tagName) => http.delete('/api/user/tags', { data: { userId, tagName } }),
  batchAdd: (userId, tagNames) => http.post('/api/user/tags/batch', { userId, tagNames }),
  hasSelectedTags: () => http.get('/api/user/tags/check'),
  save: (userId, tagNames) => http.post('/api/user/tags', { userId, tagNames }),
};

/** 当前用户资料与推荐加成（对应后端 /api/user） */
export const userProfileApi = {
  getInfo: () => http.get('/api/user/info').then(envelopeData),
  updateInfo: (data) => http.put('/api/user/info', data),
  updateAvatar: (avatarUrl) => http.put('/api/user/avatar', { avatar: avatarUrl }),
  updateRecommendBoost: (clubIds) => {
    const cid = Array.isArray(clubIds) ? clubIds[0] : clubIds;
    return http.put('/api/user/recommend-boost', { clubId: cid });
  },
};

/** @deprecated 使用 userProfileApi */
export const studentApi = userProfileApi;

export const evaluationApi = {
  getNeighbors: () => http.get('/api/evaluations/neighbors'),
  submit: (data) => http.post('/api/evaluations', data),
  getMyEvaluations: () => http.get('/api/evaluations/my-evaluations'),
  getReceived: () => http.get('/api/evaluations/received'),
};

export const recruitmentPostApi = {
  list: (type, status) => {
    const params = {};
    if (type != null) params.type = type;
    if (status != null) params.status = status;
    return http.get('/api/recruitment-posts', { params }).then(unwrapApiData);
  },
  getById: (id) => http.get(`/api/recruitment-posts/${id}`).then(envelopeData),
  create: (data) => http.post('/api/recruitment-posts', data).then(unwrapApiData),
  update: (id, data) => http.put(`/api/recruitment-posts/${id}`, data).then(unwrapApiData),
  updateStatus: (id, status) =>
    http.put(`/api/recruitment-posts/${id}/status`, { status }).then(unwrapApiData),
  delete: (id) => http.delete(`/api/recruitment-posts/${id}`).then(unwrapApiData),
  apply: (postId, message) =>
    http.post(`/api/recruitment-posts/${postId}/apply`, { message }).then(unwrapApiData),
  getApplications: (postId) =>
    http.get(`/api/recruitment-posts/${postId}/applications`).then(unwrapApiData),
  reviewApplication: (postId, appId, action) =>
    http
      .put(`/api/recruitment-posts/${postId}/applications/${appId}`, { action })
      .then(unwrapApiData),
  getApplicationStatus: (postId) =>
    http.get(`/api/recruitment-posts/${postId}/application-status`).then(unwrapApiData),
};

export const tutorialApi = {
  list: (category) => {
    const params = {};
    if (category != null) params.category = category;
    return http.get('/api/tutorials', { params }).then(unwrapApiData);
  },
  getById: (id) => http.get(`/api/tutorials/${id}`).then(envelopeData),
  getProgress: () => http.get('/api/tutorials/progress').then(unwrapApiData),
  markAsRead: (id) => http.post(`/api/tutorials/${id}/progress`).then(envelopeData),
  toggleFavorite: (id) => http.put(`/api/tutorials/${id}/favorite`).then(envelopeData),
};

export const activityGroupApi = {
  listByClub: (clubId) => http.get(`/api/activities/groups/club/${clubId}`).then(unwrapApiData),
  getById: (id) => http.get(`/api/activities/groups/${id}`).then(envelopeData),
  create: (data) => http.post('/api/activities/groups', data).then(unwrapApiData),
  update: (id, data) => http.put(`/api/activities/groups/${id}`, data).then(unwrapApiData),
  delete: (id) => http.delete(`/api/activities/groups/${id}`).then(unwrapApiData),
  join: (id, activityId) =>
    http.post(`/api/activities/groups/${id}/join`, { activityId }).then(envelopeData),
  leave: (id) => http.delete(`/api/activities/groups/${id}/join`).then(unwrapApiData),
  myGroups: () => http.get('/api/activities/groups/my').then(unwrapApiData),
};

export const clubAiSummaryApi = {
  get: (clubId) => http.get(`/api/ai/club-summary/${clubId}`).then(envelopeData),
  generate: (clubId, summaryType = 'monthly') =>
    http
      .post(`/api/ai/club-summary/${clubId}`, null, { params: { summaryType } })
      .then(envelopeData),
  refresh: (clubId, summaryType = 'monthly') =>
    http
      .put(`/api/ai/club-summary/${clubId}/refresh`, null, { params: { summaryType } })
      .then(envelopeData),
};

export const applicationApi = {
  create: (data) => http.post('/api/applications', data),
  getMy: () => http.get('/api/applications/my'),
  getByClubId: (clubId) =>
    http.get(`/api/applications/club/${clubId}`).then((r) => {
      const list = Array.isArray(r) ? r : unwrapApiData(r);
      return { data: Array.isArray(list) ? list : [] };
    }),
  getPending: (clubId) =>
    http.get(`/api/applications/club/${clubId}/pending`).then((r) => {
      const list = Array.isArray(r) ? r : unwrapApiData(r);
      return { data: Array.isArray(list) ? list : [] };
    }),
  approve: (id) => http.put(`/api/applications/${id}/approve`),
  reject: (id, reason) =>
    http.put(`/api/applications/${id}/reject`, null, {
      params: { rejectReason: reason != null ? String(reason) : '' },
    }),
  updateStatus: (id, status) => http.put(`/api/applications/${id}/status`, { status }),
  checkMembership: (clubId) =>
    http.get('/api/applications/check', { params: { clubId } }).then(unwrapApiData),
};
