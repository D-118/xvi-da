Page({
  data: {
    year: 0,
    month: 0,
    weeks: []
  },

  onLoad() {
    const now = new Date()
    this.setData({ year: now.getFullYear(), month: now.getMonth() + 1 })
    this.buildCalendar()
  },

  // 构建月历格子（P4 接入打卡数据渲染角标）
  buildCalendar() {
    const { year, month } = this.data
    const firstDay = new Date(year, month - 1, 1).getDay()
    const days = new Date(year, month, 0).getDate()
    const cells = []
    for (let i = 0; i < firstDay; i++) cells.push(null)
    for (let d = 1; d <= days; d++) cells.push(d)
    const weeks = []
    for (let i = 0; i < cells.length; i += 7) weeks.push(cells.slice(i, i + 7))
    this.setData({ weeks })
  }
})
