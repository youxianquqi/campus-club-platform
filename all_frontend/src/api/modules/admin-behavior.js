import http from '../http.js';
import { envelopeData, normalizeArrayToDataRecords } from '../helpers.js';

export const adminApi = {
  getUsers: (page = 1, size = 20) =>
    http
      .get('/api/admin/users', { params: { page, size } })
      .then((r) => normalizeArrayToDataRecords(r, page)),
  updateUserRole: (userId, role) => http.put(`/api/admin/users/${userId}/role`, { role }),
  getStatistics: () => http.get('/api/admin/statistics').then(envelopeData),
  pendingClubs: (page = 1, size = 10) =>
    http
      .get('/api/club-applications/pending', { params: { page, size } })
      .then((r) => normalizeArrayToDataRecords(r, page)),
  pendingActivities: (page = 1, size = 10) =>
    http
      .get('/api/activities/pending', { params: { page, size } })
      .then((r) => normalizeArrayToDataRecords(r, page)),
  approveClub: (id) => http.put(`/api/club-applications/${id}/approve`),
  rejectClub: (id, reason) => http.put(`/api/club-applications/${id}/reject`, { reason }),
  approveActivity: (id) => http.put(`/api/activities/${id}/approve`),
  rejectActivity: (id, reason) => http.put(`/api/activities/${id}/reject`, { reason }),
};

export const behaviorApi = {
  recordEnter: (pageName) => http.post('/api/behavior/enter', { pageName }),
  recordExit: (id) => http.put(`/api/behavior/exit/${id}`),
  getMyRecords: () => http.get('/api/behavior/my'),
  getMyByPage: (pageName) => http.get(`/api/behavior/my/page/${pageName}`),
  getPageStats: (pageName) => http.get(`/api/behavior/stats/page/${pageName}`),
};
