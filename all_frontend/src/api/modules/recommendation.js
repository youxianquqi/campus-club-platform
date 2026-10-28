import http from '../http.js';
import { unwrapApiData, envelopeData } from '../helpers.js';

export const recommendationApi = {
  getClubs: (params = {}) => http.get('/api/recommendations', { params }).then(envelopeData),

  getActivities: (limit = 8) =>
    http.get('/api/recommendations/activities', { params: { limit } }).then(envelopeData),

  getVotes: (limit = 8) =>
    http.get('/api/recommendations/votes', { params: { limit } }).then(envelopeData),

  getRecruitments: (limit = 8) =>
    http.get('/api/recommendations/recruitments', { params: { limit } }).then(envelopeData),

  getAll: (limit = 10) =>
    http.get('/api/recommendations/all', { params: { limit } }).then(envelopeData),
};
