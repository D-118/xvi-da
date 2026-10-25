-- ============================================================
-- XVI・搭 —— 个人电子衣橱（电子橱窗）数据库脚本
-- MySQL 8.x，字符集 utf8mb4
-- ============================================================

CREATE DATABASE IF NOT EXISTS xvda DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE xvda;

-- 1. 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    openid        VARCHAR(64)  NOT NULL COMMENT '微信openid',
    nickname      VARCHAR(50)  NULL COMMENT '昵称',
    avatar_url    VARCHAR(255) NULL COMMENT '头像地址',
    city          VARCHAR(50)  NOT NULL DEFAULT '哈尔滨' COMMENT '城市（天气查询用）',
    height_cm     DECIMAL(5,1) NULL COMMENT '身高cm',
    weight_kg     DECIMAL(5,1) NULL COMMENT '体重kg',
    body_type     VARCHAR(20)  NULL COMMENT '体型：苹果/梨形/沙漏/直筒/运动',
    style_prefs   VARCHAR(255) NULL COMMENT '风格偏好，逗号分隔：简约,街头',
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_openid (openid)
) ENGINE=InnoDB COMMENT='用户表';

-- 2. 衣物表
CREATE TABLE IF NOT EXISTS cloth (
    id             BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id        BIGINT        NOT NULL COMMENT '所属用户',
    name           VARCHAR(50)   NOT NULL COMMENT '衣物名称',
    category       TINYINT       NOT NULL COMMENT '分类：1上装 2下装 3鞋 4配饰',
    color          VARCHAR(20)   NULL COMMENT '主色（标准色名，推荐打分用）',
    seasons        VARCHAR(30)   NULL COMMENT '适用季节，逗号分隔：春,夏,秋,冬',
    scenes         VARCHAR(100)  NULL COMMENT '适用场合，逗号分隔：通勤,休闲,运动,约会',
    brand          VARCHAR(50)   NULL COMMENT '品牌',
    price          DECIMAL(10,2) NULL COMMENT '价格',
    image_url      VARCHAR(255)  NOT NULL COMMENT '图片地址（相对路径）',
    wear_count     INT           NOT NULL DEFAULT 0 COMMENT '累计穿着次数',
    last_wear_date DATE          NULL COMMENT '最近穿着日期（闲置提醒用）',
    archived       TINYINT       NOT NULL DEFAULT 0 COMMENT '是否归档：0否 1是',
    deleted        TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除：0否 1是',
    created_at     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_user_cat (user_id, category),
    KEY idx_user_season (user_id, seasons)
) ENGINE=InnoDB COMMENT='衣物表';

-- 3. 搭配方案表
CREATE TABLE IF NOT EXISTS outfit (
    id         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id    BIGINT       NOT NULL COMMENT '所属用户',
    name       VARCHAR(50)  NULL COMMENT '方案名称',
    scene      VARCHAR(20)  NULL COMMENT '场合：通勤/休闲/运动/约会',
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_user (user_id)
) ENGINE=InnoDB COMMENT='搭配方案表';

-- 4. 方案-衣物 多对多关联表
CREATE TABLE IF NOT EXISTS outfit_item (
    id        BIGINT  NOT NULL AUTO_INCREMENT COMMENT '主键',
    outfit_id BIGINT  NOT NULL COMMENT '方案id',
    cloth_id  BIGINT  NOT NULL COMMENT '衣物id',
    slot      TINYINT NOT NULL COMMENT '槽位：1上装 2下装 3鞋 4配饰',
    PRIMARY KEY (id),
    UNIQUE KEY uk_outfit_cloth (outfit_id, cloth_id),
    KEY idx_cloth (cloth_id)
) ENGINE=InnoDB COMMENT='搭配方案-衣物关联表';

-- 5. 穿搭日历打卡表
CREATE TABLE IF NOT EXISTS calendar_checkin (
    id           BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id      BIGINT        NOT NULL COMMENT '用户id',
    checkin_date DATE          NOT NULL COMMENT '打卡日期',
    outfit_id    BIGINT        NULL COMMENT '来源方案id（可空=临时组合）',
    item_ids     VARCHAR(500)  NULL COMMENT '当日衣物id串，冗余防衣物删除后失效',
    snapshot     VARCHAR(1000) NULL COMMENT 'JSON快照：[{clothId,name,imageUrl,slot}]',
    note         VARCHAR(200)  NULL COMMENT '备注',
    created_at   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_date (user_id, checkin_date)
) ENGINE=InnoDB COMMENT='穿搭日历打卡表';
