import http from '../http.js';
import { unwrapApiData, envelopeData } from '../helpers.js';

export const inviteCodeApi = {
  create: (data) => http.post('/api/invite-codes', data).then(envelopeData),
  myList: () => http.get('/api/invite-codes/my').then(unwrapApiData),
  verify: (code) => http.get(`/api/invite-codes/verify/${code}`).then(envelopeData),
  delete: (id) => http.delete(`/api/invite-codes/${id}`).then(envelopeData),
};

export const footprintApi = {
  list: (actionType) => {
    const params = {};
    if (actionType != null) params.actionType = actionType;
    return http.get('/api/footprints', { params }).then(unwrapApiData);
  },
  record: (data) => http.post('/api/footprints', data).then(envelopeData),
};

export const questionnaireApi = {
  listByClub: (clubId) => http.get(`/api/questionnaires/club/${clubId}`).then(unwrapApiData),
  getById: (id) => http.get(`/api/questionnaires/${id}`).then(envelopeData),
  create: (data) => http.post('/api/questionnaires', data).then(envelopeData),
  update: (id, data) => http.put(`/api/questionnaires/${id}`, data).then(envelopeData),
  submit: (id, data) => http.post(`/api/questionnaires/${id}/submit`, data).then(envelopeData),
  responses: (id) => http.get(`/api/questionnaires/${id}/responses`).then(unwrapApiData),
  delete: (id) => http.delete(`/api/questionnaires/${id}`).then(envelopeData),
};

export const voteApi = {
  listByClub: (clubId) => http.get(`/api/votes/club/${clubId}`).then(unwrapApiData),
  getById: (id) => http.get(`/api/votes/${id}`).then(envelopeData),
  create: (data) => http.post('/api/votes', data).then(envelopeData),
  update: (id, data) => http.put(`/api/votes/${id}`, data).then(envelopeData),
  submit: (id, data) => http.post(`/api/votes/${id}/submit`, data).then(envelopeData),
  results: (id) => http.get(`/api/votes/${id}/results`).then(envelopeData),
  delete: (id) => http.delete(`/api/votes/${id}`).then(envelopeData),
};
