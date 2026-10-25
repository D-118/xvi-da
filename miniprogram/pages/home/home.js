const { request } = require('../../utils/request')
const app = getApp()

function pad(n) { return n < 10 ? '0' + n : '' + n }

Page({
  data: {
    dateText: '',
    today: '',
    hasCheckin: false,
    items: [],
    note: '',
    totalCloth: 0,
    monthCheckins: 0,
    greeting: ''
  },

  onShow() {
    const now = new Date()
    const today = `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())}`
    const hour = now.getHours()
    const greeting = hour < 6 ? '夜深了' : hour < 11 ? '早上好' : hour < 14 ? '午安' : hour < 18 ? '下午好' : '晚上好'
    this.setData({
      today,
      dateText: `${now.getMonth() + 1}月${now.getDate()}日`,
      greeting
    })
    this.load()
  },

  load() {
    // 今日打卡
    request('/api/calendar/date?date=' + this.data.today).then(c => {
      if (c) {
        let snap = []
        try { snap = JSON.parse(c.snapshot || '[]') } catch (e) { }
        this.setData({
          hasCheckin: true,
          note: c.note || '',
          items: snap.map(s => ({ ...s, imageFull: app.globalData.baseUrl + s.imageUrl }))
        })
      } else {
        this.setData({ hasCheckin: false, items: [], note: '' })
      }
    }).catch(() => { })

    // 衣橱总数
    request('/api/cloth/page?page=1&size=1&archived=0').then(res => {
      this.setData({ totalCloth: Number(res.total) || 0 })
    }).catch(() => { })

    // 本月打卡数
    request('/api/calendar/month').then(list => {
      this.setData({ monthCheckins: (list || []).length })
    }).catch(() => { })
  },

  goCalendar() {
    wx.switchTab({ url: '/pages/calendar/calendar' })
  },

  goWardrobe() {
    wx.switchTab({ url: '/pages/wardrobe/wardrobe' })
  },

  goMatcher() {
    wx.switchTab({ url: '/pages/matcher/matcher' })
  }
})
