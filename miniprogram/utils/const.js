/**
 * 全局常量与展示文案
 */

// 分类：key 与数据库 category 字段一致
const CATEGORIES = [
  { value: 1, label: '上装', icon: '👔' },
  { value: 2, label: '下装', icon: '👖' },
  { value: 3, label: '鞋', icon: '👟' },
  { value: 4, label: '配饰', icon: '🧢' }
]

// 槽位（搭配器用，与分类同值）
const SLOTS = CATEGORIES

const SEASONS = ['春', '夏', '秋', '冬']

const SCENES = ['通勤', '休闲', '运动', '约会']

// 标准色（推荐打分与同色系匹配用）
const COLORS = ['黑', '白', '灰', '米', '红', '粉', '橙', '黄', '绿', '蓝', '紫', '棕']

const BODY_TYPES = ['苹果型', '梨形', '沙漏型', '直筒型', '运动型']

const STYLE_PREFS = ['简约', '甜美', '街头', '通勤', '运动', '复古', '文艺']

function categoryLabel(value) {
  const c = CATEGORIES.find(i => i.value === Number(value))
  return c ? c.label : '其他'
}

module.exports = { CATEGORIES, SLOTS, SEASONS, SCENES, COLORS, BODY_TYPES, STYLE_PREFS, categoryLabel }
