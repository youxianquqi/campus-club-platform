/** 默认浅色；暗色为可选，写入 localStorage，通过 html.theme-dark 生效 */
const THEME_KEY = 'ui-theme';

export function getTheme() {
  return localStorage.getItem(THEME_KEY) === 'dark' ? 'dark' : 'light';
}

export function applyTheme(theme) {
  const root = document.documentElement;
  if (theme === 'dark') {
    root.classList.add('theme-dark');
    localStorage.setItem(THEME_KEY, 'dark');
  } else {
    root.classList.remove('theme-dark');
    localStorage.setItem(THEME_KEY, 'light');
  }
}

export function toggleTheme() {
  applyTheme(getTheme() === 'dark' ? 'light' : 'dark');
}

/** 首屏前调用，避免闪烁 */
export function initTheme() {
  applyTheme(getTheme());
}
