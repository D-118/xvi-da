const { request } = require('../../utils/request')
const { categoryLabel } = require('../../utils/const')
const app = getApp()

Page({
  data: {
    cloth: null,
    imageFull: ''
  },

  onLoad(options) {
    this.id = Number(options.id)
  },

  onShow() {
    this.load()
  },

  load() {
    request('/api/cloth/' + this.id).then(c => {
      c.categoryLabel = categoryLabel(c.category)
      // WXML 不支持方法调用，这里预处理成数组
      c.seasonList = c.seasons ? c.seasons.split(',') : []
      c.sceneList = c.scenes ? c.scenes.split(',') : []
      this.setData({
        cloth: c,
        imageFull: app.globalData.baseUrl + c.imageUrl
      })
    })
  },

  toggleArchive() {
    const c = this.data.cloth
    const archived = c.archived === 1 ? 0 : 1
    request('/api/cloth', 'PUT', { id: c.id, name: c.name, category: c.category, imageUrl: c.imageUrl, archived })
      .then(() => {
        wx.showToast({ title: archived === 1 ? '已归档' : '已取回在柜', icon: 'success' })
        this.load()
      })
  },

  goEdit() {
    wx.navigateTo({ url: '/pages/cloth-edit/cloth-edit?id=' + this.id })
  },

  confirmDelete() {
    wx.showModal({
      title: '删除衣物',
      content: '删除后不可恢复，确定删除吗？',
      confirmColor: '#d43f3f',
      success: (res) => {
        if (!res.confirm) return
        request('/api/cloth/' + this.id, 'DELETE').then(() => {
          wx.showToast({ title: '已删除', icon: 'success' })
          setTimeout(() => wx.navigateBack(), 600)
        })
      }
    })
  }
})
