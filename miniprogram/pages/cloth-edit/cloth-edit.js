const { request, uploadFile } = require('../../utils/request')
const { CATEGORIES, SEASONS, SCENES, COLORS, categoryLabel } = require('../../utils/const')
const app = getApp()

Page({
  data: {
    categories: CATEGORIES,
    seasons: SEASONS,
    scenes: SCENES,
    colors: COLORS,
    isEdit: false,
    id: null,
    name: '',
    category: 1,
    color: '',
    seasonsSel: [],
    scenesSel: [],
    brand: '',
    price: '',
    imageUrl: '',
    submitting: false
  },

  onLoad(options) {
    if (options.id) {
      this.setData({ isEdit: true, id: Number(options.id) })
      wx.setNavigationBarTitle({ title: '编辑衣物' })
      this.loadDetail(options.id)
    } else {
      wx.setNavigationBarTitle({ title: '添加衣物' })
    }
  },

  loadDetail(id) {
    request('/api/cloth/' + id).then(c => {
      this.setData({
        name: c.name,
        category: c.category,
        color: c.color || '',
        seasonsSel: c.seasons ? c.seasons.split(',') : [],
        scenesSel: c.scenes ? c.scenes.split(',') : [],
        brand: c.brand || '',
        price: c.price == null ? '' : String(c.price),
        imageUrl: c.imageUrl,
        imageFull: app.globalData.baseUrl + c.imageUrl
      })
    })
  },

  // ----- 表单交互 -----
  onName(e) { this.setData({ name: e.detail.value }) },
  onBrand(e) { this.setData({ brand: e.detail.value }) },
  onPrice(e) { this.setData({ price: e.detail.value }) },
  pickCategory(e) { this.setData({ category: Number(e.currentTarget.dataset.value) }) },
  pickColor(e) {
    const color = e.currentTarget.dataset.value
    this.setData({ color: this.data.color === color ? '' : color })
  },
  toggleSeason(e) {
    const s = e.currentTarget.dataset.value
    const sel = this.data.seasonsSel.slice()
    const i = sel.indexOf(s)
    i >= 0 ? sel.splice(i, 1) : sel.push(s)
    this.setData({ seasonsSel: sel })
  },
  toggleScene(e) {
    const s = e.currentTarget.dataset.value
    const sel = this.data.scenesSel.slice()
    const i = sel.indexOf(s)
    i >= 0 ? sel.splice(i, 1) : sel.push(s)
    this.setData({ scenesSel: sel })
  },

  // ----- 图片：拍照或相册 -----
  chooseImage() {
    wx.chooseMedia({
      count: 1,
      mediaType: ['image'],
      sourceType: ['album', 'camera'],
      success: (res) => {
        const filePath = res.tempFiles[0].tempFilePath
        wx.showLoading({ title: '上传中' })
        uploadFile(filePath).then(url => {
          wx.hideLoading()
          this.setData({ imageUrl: url, imageFull: app.globalData.baseUrl + url })
        }).catch(() => wx.hideLoading())
      }
    })
  },

  // ----- 提交 -----
  submit() {
    const d = this.data
    if (d.submitting) return
    if (!d.name.trim()) return wx.showToast({ title: '请填写名称', icon: 'none' })
    if (!d.imageUrl) return wx.showToast({ title: '请上传图片', icon: 'none' })
    if (d.seasonsSel.length === 0) return wx.showToast({ title: '请选择适用季节', icon: 'none' })

    const payload = {
      name: d.name.trim(),
      category: d.category,
      color: d.color,
      seasons: d.seasonsSel.join(','),
      scenes: d.scenesSel.join(','),
      brand: d.brand.trim(),
      price: d.price === '' ? null : Number(d.price),
      imageUrl: d.imageUrl
    }
    this.setData({ submitting: true })
    if (d.isEdit) {
      payload.id = d.id
      request('/api/cloth', 'PUT', payload).then(() => this.done()).catch(() => this.setData({ submitting: false }))
    } else {
      request('/api/cloth', 'POST', payload).then(() => this.done()).catch(() => this.setData({ submitting: false }))
    }
  },

  done() {
    wx.showToast({ title: this.data.isEdit ? '已保存' : '已入橱', icon: 'success' })
    setTimeout(() => wx.navigateBack(), 600)
  }
})
