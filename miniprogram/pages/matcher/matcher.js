const { request } = require('../../utils/request')
const app = getApp()

const SLOT_DEFS = [
  { slot: 1, name: '上装', category: 1 },
  { slot: 2, name: '下装', category: 2 },
  { slot: 3, name: '鞋', category: 3 },
  { slot: 4, name: '配饰', category: 4 }
]

Page({
  data: {
    slots: [],           // [{slot,name,items,picked}]
    outfits: [],         // 已保存方案
    scene: '通勤',
    scenes: ['通勤', '休闲', '运动', '约会'],
    name: '',
    saving: false,
    showOutfits: false
  },

  onShow() {
    this.loadCloset()
    this.loadOutfits()
    this.adoptFromRecommend()
  },

  // 从推荐页点"用这套"跳转过来：自动回填选中的组合
  adoptFromRecommend() {
    const ids = app.globalData.adoptCombo
    if (!ids || ids.length === 0) return
    app.globalData.adoptCombo = null
    // 等衣橱异步加载完成后再回填
    const tryApply = (times) => {
      const slots = this.data.slots
      if (!slots || slots.length === 0) {
        if (times > 0) setTimeout(() => tryApply(times - 1), 300)
        return
      }
      const all = slots.flatMap(s => s.items)
      const result = slots.map(s => ({
        ...s,
        picked: all.find(i => i.id === ids[s.slot - 1]) ||
                all.find(i => ids.includes(i.id) && i.category === s.category) || null
      }))
      this.setData({ slots: result })
      wx.showToast({ title: '已按推荐回填', icon: 'none' })
    }
    tryApply(10)
  },

  // 拉取在柜衣物，按分类填充四个滑道
  loadCloset() {
    request('/api/cloth/page?page=1&size=100&archived=0').then(res => {
      const all = (res.records || []).map(c => ({
        ...c,
        imageFull: app.globalData.baseUrl + c.imageUrl
      }))
      const slots = SLOT_DEFS.map(def => ({
        ...def,
        items: all.filter(c => c.category === def.category),
        picked: null
      }))
      this.setData({ slots })
    })
  },

  loadOutfits() {
    request('/api/outfit/list').then(outfits => {
      outfits = (outfits || []).map(o => ({
        ...o,
        thumbs: (o.clothes || []).map(c => app.globalData.baseUrl + c.imageUrl)
      }))
      this.setData({ outfits })
    })
  },

  // 滑道内选中一件
  pick(e) {
    const { slot, id } = e.currentTarget.dataset
    const slots = this.data.slots.map(s => {
      if (s.slot !== Number(slot)) return s
      const found = s.items.find(i => i.id === Number(id))
      return { ...s, picked: found && s.picked && s.picked.id === found.id ? null : found }
    })
    this.setData({ slots })
  },

  setScene(e) {
    this.setData({ scene: e.currentTarget.dataset.value })
  },

  onName(e) {
    this.setData({ name: e.detail.value })
  },

  // 随机来一套（每个必选槽位随机挑一件）
  randomPick() {
    const slots = this.data.slots.map(s => {
      if (s.items.length === 0) return { ...s, picked: null }
      const idx = Math.floor(Math.random() * s.items.length)
      return { ...s, picked: s.items[idx] }
    })
    this.setData({ slots })
  },

  clearPick() {
    const slots = this.data.slots.map(s => ({ ...s, picked: null }))
    this.setData({ slots, name: '' })
  },

  // 保存方案
  save() {
    if (this.data.saving) return
    const pickedItems = []
    for (const s of this.data.slots) {
      if (s.picked) pickedItems.push({ clothId: s.picked.id, slot: s.slot })
    }
    if (pickedItems.filter(i => i.slot <= 3).length < 3) {
      return wx.showToast({ title: '至少选齐：上装+下装+鞋', icon: 'none' })
    }
    this.setData({ saving: true })
    request('/api/outfit', 'POST', {
      name: this.data.name.trim(),
      scene: this.data.scene,
      items: pickedItems
    }).then(() => {
      wx.showToast({ title: '已保存', icon: 'success' })
      this.clearPick()
      this.loadOutfits()
    }).finally(() => this.setData({ saving: false }))
  },

  // 用某套历史方案回填滑道
  reuse(e) {
    const id = Number(e.currentTarget.dataset.id)
    const outfit = this.data.outfits.find(o => o.id === id)
    if (!outfit) return
    const slots = this.data.slots.map(s => {
      const match = (outfit.clothes || []).find(c => c.category === s.category)
      return { ...s, picked: match || null }
    })
    this.setData({ slots, name: outfit.name, scene: outfit.scene || '通勤' })
    wx.showToast({ title: '已回填，可微调后另存', icon: 'none' })
  },

  deleteOutfit(e) {
    const id = Number(e.currentTarget.dataset.id)
    wx.showModal({
      title: '删除方案',
      content: '确定删除这套搭配吗？',
      confirmColor: '#d43f3f',
      success: (res) => {
        if (res.confirm) {
          request('/api/outfit/' + id, 'DELETE').then(() => this.loadOutfits())
        }
      }
    })
  },

  toggleOutfits() {
    this.setData({ showOutfits: !this.data.showOutfits })
  }
})
