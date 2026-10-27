package com.xvda.dto;

import java.util.List;

/**
 * 衣橱统计总览 VO
 */
public class StatsVO {
    private long total;          // 在库衣物总数（未归档）
    private long archived;       // 已归档件数
    private long checkinDays;    // 累计打卡天数
    private long outfitCount;    // 搭配方案数
    private List<CategoryCount> byCategory;
    private List<SeasonCount> bySeason;

    public static class CategoryCount {
        private int category;
        private String label;
        private long count;

        public CategoryCount(int category, String label, long count) {
            this.category = category;
            this.label = label;
            this.count = count;
        }

        public int getCategory() { return category; }
        public String getLabel() { return label; }
        public long getCount() { return count; }
    }

    public static class SeasonCount {
        private String season;
        private long count;

        public SeasonCount(String season, long count) {
            this.season = season;
            this.count = count;
        }

        public String getSeason() { return season; }
        public long getCount() { return count; }
    }

    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }
    public long getArchived() { return archived; }
    public void setArchived(long archived) { this.archived = archived; }
    public long getCheckinDays() { return checkinDays; }
    public void setCheckinDays(long checkinDays) { this.checkinDays = checkinDays; }
    public long getOutfitCount() { return outfitCount; }
    public void setOutfitCount(long outfitCount) { this.outfitCount = outfitCount; }
    public List<CategoryCount> getByCategory() { return byCategory; }
    public void setByCategory(List<CategoryCount> byCategory) { this.byCategory = byCategory; }
    public List<SeasonCount> getBySeason() { return bySeason; }
    public void setBySeason(List<SeasonCount> bySeason) { this.bySeason = bySeason; }
}
