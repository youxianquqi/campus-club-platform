export const TAG_ICONS = {
  文艺类: '🎨',
  科技类: '💻',
  体育类: '⚽',
  公益类: '❤️',
  学术类: '📚',
  实践类: '🔧',
  娱乐类: '🎮',
  音乐类: '🎵',
  舞蹈类: '💃',
  摄影类: '📷',
  编程类: '👨‍💻',
  文学类: '📖',
  游戏类: '🎯',
  户外类: '🏕️',
  美食类: '🍜',
  影视类: '🎬',
  手工类: '✂️',
  电竞类: '🎮',
  志愿服务: '🤝',
  创新创业: '🚀',
  文艺: '🎨',
  科技: '💻',
  体育: '⚽',
  音乐: '🎵',
  摄影: '📷',
  编程: '👨‍💻',
  舞蹈: '💃',
  篮球: '🏀',
  公益: '❤️',
  文学: '📖',
};

export const DEFAULT_TAG_ICON = '🏷️';

export function getTagIcon(tagName) {
  if (!tagName) return DEFAULT_TAG_ICON;
  return TAG_ICONS[tagName] || TAG_ICONS[tagName.replace(/类$/, '')] || DEFAULT_TAG_ICON;
}

export default TAG_ICONS;
