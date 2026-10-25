Page({
  data: { hasCheckin: false },

  onShow() {
    // P4 接入：/api/calendar/today 今日橱窗数据
  },

  goMatcher() {
    wx.switchTab({ url: '/pages/matcher/matcher' })
  }
})
