-- ============================================================
-- XVI・搭 演示数据脚本（可选执行）
-- 前置：先运行 schema.sql 建库建表；
--       再运行 python3 db/gen_demo_images.py 生成演示图片到 server/upload/demo/
-- 说明：demo 用户 openid 为 dev-mock-user（未配置微信 AppSecret 时
--       服务端 Mock 登录固定使用的账号）。若你使用真实 AppSecret 登录，
--       请把下面 openid 改成你自己的 openid。
-- ============================================================
USE xvda;

INSERT INTO sys_user (openid, nickname, city) VALUES
('dev-mock-user', '小搭', '哈尔滨')
ON DUPLICATE KEY UPDATE nickname = VALUES(nickname);

SET @uid = (SELECT id FROM sys_user WHERE openid = 'dev-mock-user' LIMIT 1);

INSERT INTO cloth (user_id, name, category, color, seasons, scenes, brand, price, image_url) VALUES
(@uid, '燕麦色针织衫', 1, '米',   '春,秋',    '通勤,约会', 'UNIQLO',   199.00, '/upload/demo/beige.png'),
(@uid, '白色基础T恤',  1, '白',   '春,夏,秋', '休闲',      'UNIQLO',    79.00, '/upload/demo/white.png'),
(@uid, '黑色高领毛衣', 1, '黑',   '秋,冬',    '通勤',      '优衣库',    249.00, '/upload/demo/black.png'),
(@uid, '灰色连帽卫衣', 1, '灰',   '春,秋',    '休闲,运动', 'H&M',       159.00, '/upload/demo/gray.png'),
(@uid, '蓝色直筒牛仔裤', 2, '蓝', '春,秋,冬', '休闲,通勤', 'LEVI''S',   399.00, '/upload/demo/blue.png'),
(@uid, '卡其工装裤',   2, '卡其', '春,秋',    '休闲',      'ZARA',      259.00, '/upload/demo/khaki.png'),
(@uid, '黑色西装裤',   2, '黑',   '秋,冬',    '通勤',      'G2000',     229.00, '/upload/demo/black.png'),
(@uid, '米白阔腿裤',   2, '米',   '春,夏,秋', '通勤,约会', 'MO&Co.',    329.00, '/upload/demo/cream.png'),
(@uid, '白色小白鞋',   3, '白',   '春,夏,秋', '休闲,通勤', 'Nike',      469.00, '/upload/demo/white.png'),
(@uid, '棕色马丁靴',   3, '棕',   '秋,冬',    '休闲',      'Dr.Martens',1299.00, '/upload/demo/brown.png'),
(@uid, '黑色短靴',     3, '黑',   '秋,冬',    '通勤,约会', '百丽',      599.00, '/upload/demo/black.png'),
(@uid, '米色帆布包',   4, '米',   '春,夏,秋', '休闲',      '无印良品',   98.00, '/upload/demo/beige.png');
