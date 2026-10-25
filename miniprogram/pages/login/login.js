const app = getApp()

Page({
  data: { logging: false },

  onLoad() {
    if (app.globalData.token) {
      wx.switchTab({ url: '/pages/home/home' })
    }
  },

  // 兜底手动登录（静默登录失败时使用）
  doLogin() {
    this.setData({ logging: true })
    wx.login({
      success: (res) => {
        if (!res.code) {
          this.setData({ logging: false })
          return
        }
        wx.request({
          url: app.globalData.baseUrl + '/api/auth/login',
          method: 'POST',
          data: { code: res.code },
          success: (r) => {
            if (r.data && r.data.code === 0 && r.data.data.token) {
              app.globalData.token = r.data.data.token
              wx.setStorageSync('token', app.globalData.token)
              wx.switchTab({ url: '/pages/home/home' })
            } else {
              wx.showToast({ title: (r.data && r.data.msg) || '登录失败', icon: 'none' })
            }
          },
          fail: () => wx.showToast({ title: '网络异常', icon: 'none' }),
          complete: () => this.setData({ logging: false })
        })
      },
      fail: () => this.setData({ logging: false })
    })
  }
})
