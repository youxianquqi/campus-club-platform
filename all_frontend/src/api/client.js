/**
 * 底层 HTTP 工具（供 upload、DELETE body 等使用）
 */
import http from './http.js';

export const rawApi = {
  get: (url, params) => http.get(url, { params }),
  post: (url, data) => http.post(url, data),
  put: (url, data) => http.put(url, data),
  delete: (url) => http.delete(url),
  deleteWithBody: (url, data) => http.delete(url, { data }),
  upload: (url, file, onProgress) => {
    const formData = new FormData();
    formData.append('file', file);
    return http.post(url, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      onUploadProgress: (e) => {
        if (onProgress && e.total) {
          onProgress(Math.round((e.loaded * 100) / e.total));
        }
      },
    });
  },
  uploadMultiple: (url, files, onProgress) => {
    const formData = new FormData();
    files.forEach((file) => formData.append('files', file));
    return http.post(url, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      onUploadProgress: (e) => {
        if (onProgress && e.total) {
          onProgress(Math.round((e.loaded * 100) / e.total));
        }
      },
    });
  },
};

export const api = rawApi;
