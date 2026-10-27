const app = getApp()
const { request } = require('../../utils/request')
const { categoryLabel } = require('../../utils/const')

Page({
  data: {
    nickname: '',
    stats: null,
    wearTop: [],
    idle: [],
    idleDays: 30,
    loading: true
  },

  onShow() {
    this.loadAll()
  },

  loadAll() {
    this.setData({ loading: true })
    Promise.all([
      request('/api/user/profile'),
      request('/api/stats/overview'),
      request('/api/stats/wear-top?limit=5'),
      request('/api/stats/idle?idleDays=' + this.data.idleDays)
    ]).then(([profile, stats, wearTop, idle]) => {
      // 闲置衣物补充分类文案
      idle.forEach(c => { c.categoryLabel = categoryLabel(c.category) })
      wearTop.forEach(c => { c.categoryLabel = categoryLabel(c.category) })
      this.setData({
        nickname: profile.nickname || '',
        stats,
        wearTop,
        idle,
        loading: false
      })
    }).catch(() => this.setData({ loading: false }))
  },

  editNickname() {
    wx.showModal({
      title: '修改昵称',
      editable: true,
      placeholderText: '输入新昵称',
      success: res => {
        if (!res.confirm || !res.content) return
        request('/api/user/profile', 'PUT', { nickname: res.content }).then(() => {
          this.setData({ nickname: res.content })
          wx.showToast({ title: '已保存' })
        })
      }
    })
  },

  changeIdleDays(e) {
    const days = Number(e.detail.value) || 30
    this.setData({ idleDays: days })
    request('/api/stats/idle?idleDays=' + days).then(idle => {
      idle.forEach(c => { c.categoryLabel = categoryLabel(c.category) })
      this.setData({ idle })
    })
  },

  goCloth(e) {
    wx.navigateTo({ url: '/pages/cloth-detail/cloth-detail?id=' + e.currentTarget.dataset.id })
  },

  goWardrobe() {
    wx.switchTab({ url: '/pages/wardrobe/wardrobe' })
  }
})
