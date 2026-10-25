const { request } = require('../../utils/request')
const app = getApp()

function pad(n) { return n < 10 ? '0' + n : '' + n }

Page({
  data: {
    year: 0,
    month: 0,
    weeks: [],
    monthText: '',
    today: '',
    checkinDates: {},    // '2026-10-25' -> {thumb, count}
    selected: '',        // 当前选中日期
    dayDetail: null,     // 选中日期的打卡详情
    outfits: [],         // 打卡时可选的方案
    showPicker: false,
    pickerDate: '',
    note: '',
    submitting: false
  },

  onLoad() {
    const now = new Date()
    const today = `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())}`
    this.setData({
      year: now.getFullYear(),
      month: now.getMonth() + 1,
      today
    })
  },

  onShow() {
    this.refresh()
  },

  refresh() {
    this.buildCalendar()
    this.loadMonth()
    if (this.data.selected) this.loadDayDetail(this.data.selected)
  },

  buildCalendar() {
    const { year, month } = this.data
    const firstDay = new Date(year, month - 1, 1).getDay()
    const days = new Date(year, month, 0).getDate()
    const cells = []
    for (let i = 0; i < firstDay; i++) cells.push({ day: null })
    for (let d = 1; d <= days; d++) {
      const date = `${year}-${pad(month)}-${pad(d)}`
      const info = this.data.checkinDates[date]
      cells.push({ day: d, date, hasCheckin: !!info, thumb: info ? info.thumb : '' })
    }
    while (cells.length % 7 !== 0) cells.push({ day: null })
    const weeks = []
    for (let i = 0; i < cells.length; i += 7) weeks.push(cells.slice(i, i + 7))
    this.setData({ weeks, monthText: `${year}年${month}月` })
  },

  loadMonth() {
    const { year, month } = this.data
    request('/api/calendar/month?month=' + year + '-' + pad(month)).then(list => {
      const checkinDates = {}
      for (const c of list || []) {
        // 取快照第一张图作为角标缩略
        let thumb = ''
        try {
          const snap = JSON.parse(c.snapshot || '[]')
          if (snap.length > 0) thumb = app.globalData.baseUrl + snap[0].imageUrl
        } catch (e) { }
        checkinDates[c.checkinDate] = { thumb, count: (c.itemIds || '').split(',').filter(Boolean).length }
      }
      this.setData({ checkinDates })
      this.buildCalendar()
    })
  },

  prevMonth() {
    let { year, month } = this.data
    month--; if (month === 0) { month = 12; year-- }
    this.setData({ year, month, selected: '', dayDetail: null })
    this.loadMonth()
  },

  nextMonth() {
    let { year, month } = this.data
    month++; if (month === 13) { month = 1; year++ }
    this.setData({ year, month, selected: '', dayDetail: null })
    this.loadMonth()
  },

  tapDay(e) {
    const date = e.currentTarget.dataset.date
    if (!date) return
    this.setData({ selected: date, dayDetail: null })
    this.loadDayDetail(date)
  },

  loadDayDetail(date) {
    request('/api/calendar/date?date=' + date).then(c => {
      let detail = null
      if (c) {
        let snap = []
        try { snap = JSON.parse(c.snapshot || '[]') } catch (e) { }
        detail = {
          date: c.checkinDate,
          note: c.note,
          items: snap.map(s => ({ ...s, imageFull: app.globalData.baseUrl + s.imageUrl }))
        }
      }
      this.setData({ dayDetail: detail })
    }).catch(() => { })
  },

  // ---- 打卡面板 ----
  openPicker() {
    request('/api/outfit/list').then(outfits => {
      outfits = (outfits || []).map(o => ({
        ...o,
        thumbs: (o.clothes || []).map(x => app.globalData.baseUrl + x.imageUrl)
      }))
      this.setData({ showPicker: true, outfits, pickerDate: this.data.selected || this.data.today, note: '' })
    })
  },

  closePicker() {
    this.setData({ showPicker: false })
  },

  onNote(e) {
    this.setData({ note: e.detail.value })
  },

  pickOutfit(e) {
    if (this.data.submitting) return
    const id = Number(e.currentTarget.dataset.id)
    const outfit = this.data.outfits.find(o => o.id === id)
    if (!outfit) return
    this.setData({ submitting: true })
    request('/api/calendar/checkin', 'POST', {
      date: this.data.pickerDate,
      outfitId: outfit.id,
      itemIds: (outfit.clothes || []).map(c => c.id),
      note: this.data.note
    }).then(() => {
      wx.showToast({ title: '打卡成功', icon: 'success' })
      this.setData({ showPicker: false, selected: this.data.pickerDate, submitting: false })
      this.refresh()
    }).catch(() => this.setData({ submitting: false }))
  },

  cancelCheckin() {
    const date = this.data.dayDetail.date
    wx.showModal({
      title: '取消打卡',
      content: '确定取消 ' + date + ' 的打卡吗？',
      confirmColor: '#d43f3f',
      success: (res) => {
        if (res.confirm) {
          request('/api/calendar/checkin?date=' + date, 'DELETE').then(() => {
            this.setData({ selected: '' })
            this.refresh()
          })
        }
      }
    })
  },

  goMatcher() {
    wx.switchTab({ url: '/pages/matcher/matcher' })
  }
})
