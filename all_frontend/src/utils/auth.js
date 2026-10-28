import { reactive, computed } from 'vue';

const AUTH_KEYS = {
  TOKEN: 'token',
  USER_ID: 'userId',
  USERNAME: 'username',
  NICKNAME: 'nickname',
  AVATAR: 'avatar',
  USER_ROLE: 'userRole',
  IS_LOGGED_IN: 'isLoggedIn',
};

const state = reactive({
  token: localStorage.getItem(AUTH_KEYS.TOKEN) || null,
  userId: localStorage.getItem(AUTH_KEYS.USER_ID)
    ? parseInt(localStorage.getItem(AUTH_KEYS.USER_ID))
    : null,
  username: localStorage.getItem(AUTH_KEYS.USERNAME) || '',
  nickname: localStorage.getItem(AUTH_KEYS.NICKNAME) || '',
  avatar: localStorage.getItem(AUTH_KEYS.AVATAR) || '',
  userRole: localStorage.getItem(AUTH_KEYS.USER_ROLE)
    ? parseInt(localStorage.getItem(AUTH_KEYS.USER_ROLE))
    : 1,
  isLoggedIn: localStorage.getItem(AUTH_KEYS.IS_LOGGED_IN) === 'true',
});

export function useAuth() {
  const isLoggedIn = computed(() => state.isLoggedIn);
  const isAdmin = computed(() => state.userRole === 3);
  const currentUserId = computed(() => state.userId);
  const currentNickname = computed(() => state.nickname);
  const currentAvatar = computed(() => state.avatar);

  function setAuth(data) {
    if (data.token) {
      state.token = data.token;
      localStorage.setItem(AUTH_KEYS.TOKEN, data.token);
    }
    if (data.userId != null) {
      state.userId = parseInt(data.userId);
      localStorage.setItem(AUTH_KEYS.USER_ID, String(data.userId));
    }
    if (data.username != null) {
      state.username = data.username;
      localStorage.setItem(AUTH_KEYS.USERNAME, data.username);
    }
    if (data.nickname != null) {
      state.nickname = data.nickname;
      localStorage.setItem(AUTH_KEYS.NICKNAME, data.nickname);
    }
    if (data.avatar != null) {
      state.avatar = data.avatar;
      localStorage.setItem(AUTH_KEYS.AVATAR, data.avatar);
    } else {
      localStorage.removeItem(AUTH_KEYS.AVATAR);
    }
    if (data.role != null) {
      state.userRole = parseInt(data.role);
      localStorage.setItem(AUTH_KEYS.USER_ROLE, String(data.role));
    }
    state.isLoggedIn = true;
    localStorage.setItem(AUTH_KEYS.IS_LOGGED_IN, 'true');
  }

  function clearAuth() {
    state.token = null;
    state.userId = null;
    state.username = '';
    state.nickname = '';
    state.avatar = '';
    state.userRole = 1;
    state.isLoggedIn = false;

    localStorage.removeItem(AUTH_KEYS.TOKEN);
    localStorage.removeItem(AUTH_KEYS.USER_ID);
    localStorage.removeItem(AUTH_KEYS.USERNAME);
    localStorage.removeItem(AUTH_KEYS.NICKNAME);
    localStorage.removeItem(AUTH_KEYS.AVATAR);
    localStorage.removeItem(AUTH_KEYS.USER_ROLE);
    localStorage.removeItem(AUTH_KEYS.IS_LOGGED_IN);
  }

  function updateProfile(data) {
    if (data.nickname != null) {
      state.nickname = data.nickname;
      localStorage.setItem(AUTH_KEYS.NICKNAME, data.nickname);
    }
    if (data.avatar != null) {
      state.avatar = data.avatar;
      localStorage.setItem(AUTH_KEYS.AVATAR, data.avatar);
    }
    if (data.userRole != null) {
      state.userRole = parseInt(data.userRole);
      localStorage.setItem(AUTH_KEYS.USER_ROLE, String(data.userRole));
    }
  }

  return {
    state,
    isLoggedIn,
    isAdmin,
    currentUserId,
    currentNickname,
    currentAvatar,
    setAuth,
    clearAuth,
    updateProfile,
  };
}

export function getCurrentUserId() {
  return state.userId;
}

export function isUserLoggedIn() {
  return state.isLoggedIn;
}

export function isUserAdmin() {
  return state.userRole === 3;
}

export function getToken() {
  return state.token;
}

export { AUTH_KEYS };
