const { request } = require('../../utils/request')
const { CATEGORIES, SEASONS, categoryLabel } = require('../../utils/const')
const app = getApp()

Page({
  data: {
    categories: CATEGORIES,
    category: 0,        // 0=全部
    keyword: '',
    archived: 0,        // 0=在柜 1=已归档
    list: [],
    page: 1,
    size: 12,
    total: 0,
    finished: false,
    loading: false
  },

  onShow() {
    // 每次回到衣橱页都刷新（录入/编辑/删除后保持同步）
    this.setData({ list: [], page: 1, finished: false })
    this.loadPage()
  },

  switchCategory(e) {
    const category = Number(e.currentTarget.dataset.value)
    this.setData({ category, list: [], page: 1, finished: false })
    this.loadPage()
  },

  switchArchived() {
    const archived = this.data.archived === 0 ? 1 : 0
    this.setData({ archived, list: [], page: 1, finished: false })
    this.loadPage()
  },

  onKeywordInput(e) {
    this.setData({ keyword: e.detail.value })
  },

  onSearch() {
    this.setData({ list: [], page: 1, finished: false })
    this.loadPage()
  },

  loadPage() {
    if (this.data.loading || this.data.finished) return
    const { page, size, keyword, category, archived } = this.data
    this.setData({ loading: true })
    const query = `page=${page}&size=${size}&archived=${archived}` +
      (category ? `&category=${category}` : '') +
      (keyword ? `&keyword=${encodeURIComponent(keyword)}` : '')
    request('/api/cloth/page?' + query).then(res => {
      const records = (res.records || []).map(c => ({
        ...c,
        imageFull: app.globalData.baseUrl + c.imageUrl,
        categoryLabel: categoryLabel(c.category)
      }))
      const total = Number(res.total) || 0
      const list = this.data.list.concat(records)
      this.setData({
        list,
        total,
        page: page + 1,
        finished: list.length >= total,
        loading: false
      })
    }).catch(() => {
      this.setData({ loading: false })
    })
  },

  onReachBottom() {
    this.loadPage()
  },

  goDetail(e) {
    wx.navigateTo({ url: '/pages/cloth-detail/cloth-detail?id=' + e.currentTarget.dataset.id })
  },

  goAdd() {
    wx.navigateTo({ url: '/pages/cloth-edit/cloth-edit' })
  }
})
