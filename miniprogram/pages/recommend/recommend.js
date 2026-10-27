const { request } = require('../../utils/request')
const app = getApp()

Page({
  data: {
    scenes: ['不限', '通勤', '休闲', '运动', '约会'],
    scene: '不限',
    weather: null,
    summary: '',
    combos: [],
    loading: false,
    loaded: false
  },

  onLoad() { },

  onShow() {
    if (!this.data.loaded) this.load()
  },

  setScene(e) {
    const scene = e.currentTarget.dataset.value
    this.setData({ scene })
    this.load()
  },

  load() {
    if (this.data.loading) return
    this.setData({ loading: true })
    const q = this.data.scene === '不限' ? '' : '&scene=' + this.data.scene
    request('/api/recommend/today?_t=' + Date.now() + q).then(res => {
      const combos = (res.combos || []).map(c => ({
        ...c,
        items: (c.items || []).map(i => ({ ...i, imageFull: app.globalData.baseUrl + i.imageUrl }))
      }))
      this.setData({
        weather: res.weather,
        summary: res.summary,
        combos,
        loading: false,
        loaded: true
      })
    }).catch(() => this.setData({ loading: false }))
  },

  // 采纳组合：跳到搭配台回填
  adopt(e) {
    const idx = Number(e.currentTarget.dataset.index)
    const combo = this.data.combos[idx]
    if (!combo) return
    getApp().globalData.adoptCombo = combo.items.map(i => i.id)
    wx.switchTab({ url: '/pages/matcher/matcher' })
  }
})
