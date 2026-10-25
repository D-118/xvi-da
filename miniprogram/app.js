// XVI・搭 —— 个人电子衣橱（电子橱窗）
App({
  globalData: {
    baseUrl: 'http://localhost:8080', // 真机预览时改成电脑局域网 IP
    token: ''
  },

  onLaunch() {
    // 静默登录：已有 token 则跳过
    const token = wx.getStorageSync('token')
    if (token) {
      this.globalData.token = token
      return
    }
    this.silentLogin()
  },

  // wx.login 静默登录，失败不阻塞（各页面自行处理未登录态）
  silentLogin() {
    wx.login({
      success: (res) => {
        if (!res.code) return
        wx.request({
          url: this.globalData.baseUrl + '/api/auth/login',
          method: 'POST',
          data: { code: res.code },
          success: (r) => {
            if (r.data && r.data.code === 0 && r.data.data && r.data.data.token) {
              this.globalData.token = r.data.data.token
              wx.setStorageSync('token', this.globalData.token)
            }
          }
        })
      }
    })
  }
})
