/**
 * 统一网络请求封装
 * - 自动携带 token
 * - 统一处理 401（重新登录）与业务错误 toast
 */
const app = getApp()

function request(url, method = 'GET', data = {}) {
  return new Promise((resolve, reject) => {
    wx.request({
      url: app.globalData.baseUrl + url,
      method,
      data,
      header: {
        'Authorization': 'Bearer ' + (app.globalData.token || '')
      },
      success: (res) => {
        if (res.statusCode === 401) {
          // token 失效：清空并重新静默登录
          wx.removeStorageSync('token')
          app.globalData.token = ''
          app.silentLogin()
          reject(new Error('未登录'))
          return
        }
        const body = res.data
        if (body && body.code === 0) {
          resolve(body.data)
        } else {
          const msg = (body && body.msg) || '请求失败'
          wx.showToast({ title: msg, icon: 'none' })
          reject(new Error(msg))
        }
      },
      fail: (err) => {
        wx.showToast({ title: '网络异常，请检查服务端是否启动', icon: 'none' })
        reject(err)
      }
    })
  })
}

// 文件上传（图片）
function uploadFile(filePath) {
  return new Promise((resolve, reject) => {
    wx.uploadFile({
      url: app.globalData.baseUrl + '/api/file/upload',
      filePath,
      name: 'file',
      header: {
        'Authorization': 'Bearer ' + (app.globalData.token || '')
      },
      success: (res) => {
        try {
          const body = JSON.parse(res.data)
          if (body.code === 0) resolve(body.data.url)
          else reject(new Error(body.msg || '上传失败'))
        } catch (e) {
          reject(e)
        }
      },
      fail: reject
    })
  })
}

module.exports = { request, uploadFile }
