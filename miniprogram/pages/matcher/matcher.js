Page({
  data: {
    slots: [
      { slot: 1, name: '上装', items: [], picked: null },
      { slot: 2, name: '下装', items: [], picked: null },
      { slot: 3, name: '鞋', items: [], picked: null },
      { slot: 4, name: '配饰', items: [], picked: null }
    ]
  },

  onShow() {
    // P3 接入：按分类拉取衣物填充滑道
  }
})
