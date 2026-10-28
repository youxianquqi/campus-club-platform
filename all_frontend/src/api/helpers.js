/**
 * 响应解包与列表归一化（与后端 Result / 历史 Map 结构兼容）
 */
export function unwrapApiData(root) {
  if (
    root != null &&
    typeof root === 'object' &&
    !Array.isArray(root) &&
    Object.prototype.hasOwnProperty.call(root, 'data')
  ) {
    return root.data;
  }
  return root;
}

export function normalizeTagsResponse(root) {
  if (Array.isArray(root)) return { code: 200, data: root };
  if (root && typeof root === 'object' && Array.isArray(root.data)) return root;
  return { code: 200, data: [] };
}

export function normalizeClubsPageResponse(root, page) {
  if (root?.data?.records !== undefined) return root;
  if (root && typeof root === 'object' && Array.isArray(root.clubs)) {
    return {
      code: 200,
      data: {
        records: root.clubs,
        total: root.total ?? root.clubs.length,
        pages: root.pages ?? 1,
        currentPage: root.currentPage ?? page,
      },
    };
  }
  return root;
}

export function normalizeClubEnvelope(root) {
  if (root && typeof root === 'object' && root.data !== undefined) return root;
  if (root && typeof root === 'object' && root.id != null) {
    return { code: 200, data: root };
  }
  if (root && typeof root === 'object' && Object.prototype.hasOwnProperty.call(root, 'found')) {
    return { code: 200, data: root };
  }
  return root;
}

export function envelopeData(root) {
  if (root != null && typeof root === 'object' && root.data !== undefined) return root;
  if (root != null && typeof root === 'object') return { code: 200, data: root };
  return { code: 200, data: root };
}

/** 推荐接口：统一包装后 data 为 { activities|votes|recruitments, total }，取列表数组 */
export function unwrapRecommendationItems(res, key) {
  const d = res?.data;
  if (Array.isArray(d)) return d;
  if (d && typeof d === 'object' && Array.isArray(d[key])) return d[key];
  return [];
}

export function normalizeArrayToDataRecords(root, page = 0) {
  if (Array.isArray(root)) {
    return {
      code: 200,
      data: { records: root, total: root.length, pages: 1, currentPage: page },
    };
  }
  if (root?.data?.records !== undefined) return root;
  if (Array.isArray(root?.records)) {
    return { code: 200, data: root };
  }
  return root;
}

export function normalizeActivitiesPage(root, page) {
  if (root?.data?.records !== undefined) return root;
  const nestedActs = root?.data?.activities;
  if (root && typeof root === 'object' && Array.isArray(nestedActs)) {
    return {
      code: 200,
      data: {
        records: nestedActs,
        total: root.data.total ?? nestedActs.length,
        pages: root.data.pages ?? 1,
        currentPage: root.data.currentPage ?? page,
      },
    };
  }
  if (root && typeof root === 'object' && Array.isArray(root.activities)) {
    return {
      code: 200,
      data: {
        records: root.activities,
        total: root.total ?? root.activities.length,
        pages: root.pages ?? 1,
        currentPage: root.currentPage ?? page,
      },
    };
  }
  return root;
}

export const IMG_PLACEHOLDER = '/img-placeholder.svg';

export function onImgError(e) {
  const el = e && e.target;
  if (!el || el.tagName !== 'IMG') return;
  if (el.dataset.imgPh === '1') return;
  el.dataset.imgPh = '1';
  el.removeAttribute('srcset');
  el.src = IMG_PLACEHOLDER;
}

export function getCurrentUserId() {
  return localStorage.getItem('userId') ? parseInt(localStorage.getItem('userId')) : null;
}

/** 登录账号 user.id，与招募帖等绑定 user 表的字段一致；未登录或旧会话可能为 null */
export function getCurrentAccountUserId() {
  const v = localStorage.getItem('accountUserId');
  if (!v) return null;
  const n = parseInt(v, 10);
  return Number.isNaN(n) ? null : n;
}

export function resolveBackendUrl(path) {
  if (!path) return '';
  if (path.startsWith('http')) return path;
  const base = localStorage.getItem('backendUrl') || 'http://localhost:8080';
  return path.startsWith('/') ? `${base}${path}` : `${base}/${path}`;
}

export function isLoggedIn() {
  return !!localStorage.getItem('token');
}

export function setAuth(token, userId, nickname, role) {
  localStorage.setItem('token', token);
  localStorage.setItem('userId', String(userId));
  localStorage.setItem('nickname', nickname || '');
  localStorage.setItem('userRole', String(role != null ? role : 1));
}

export function clearAuth() {
  localStorage.removeItem('token');
  localStorage.removeItem('userId');
  localStorage.removeItem('nickname');
  localStorage.removeItem('userRole');
  localStorage.removeItem('isLoggedIn');
}
