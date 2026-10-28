export function formatRelativeTime(isoString) {
  if (!isoString) return '';

  const date = new Date(isoString);
  const now = new Date();
  const diff = now - date;

  const seconds = Math.floor(diff / 1000);
  const minutes = Math.floor(seconds / 60);
  const hours = Math.floor(minutes / 60);
  const days = Math.floor(hours / 24);
  const weeks = Math.floor(days / 7);
  const months = Math.floor(days / 30);
  const years = Math.floor(days / 365);

  if (seconds < 60) return '刚刚';
  if (minutes < 60) return `${minutes}分钟前`;
  if (hours < 24) return `${hours}小时前`;
  if (days < 7) return `${days}天前`;
  if (weeks < 4) return `${weeks}周前`;
  if (months < 12) return `${months}个月前`;
  return `${years}年前`;
}

export function formatDeadline(isoString) {
  if (!isoString) return null;

  const deadline = new Date(isoString);
  const now = new Date();
  const diff = deadline - now;

  if (diff < 0) return { text: '已截止', type: 'expired', days: 0 };

  const days = Math.floor(diff / (1000 * 60 * 60 * 24));
  const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));

  if (days === 0) {
    if (hours === 0) return { text: '即将截止', type: 'urgent', days: 0 };
    return { text: `${hours}小时后截止`, type: 'urgent', days: 0 };
  }

  if (days === 1) return { text: '明天截止', type: 'warning', days: 1 };
  if (days <= 3) return { text: `${days}天后截止`, type: 'warning', days };
  if (days <= 7) return { text: `${days}天后截止`, type: 'normal', days };

  return { text: formatDate(isoString), type: 'normal', days };
}

export function formatDate(isoString) {
  if (!isoString) return '';
  const date = new Date(isoString);
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  });
}

export function formatDateTime(isoString) {
  if (!isoString) return '';
  const date = new Date(isoString);
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  });
}

export function getRemainingDays(isoString) {
  if (!isoString) return null;

  const deadline = new Date(isoString);
  const now = new Date();
  const diff = deadline - now;

  if (diff < 0) return 0;
  return Math.ceil(diff / (1000 * 60 * 60 * 24));
}
