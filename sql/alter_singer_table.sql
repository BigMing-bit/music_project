-- 为music_singer表添加area和style字段
ALTER TABLE `music_singer` 
ADD COLUMN `area` TINYINT UNSIGNED DEFAULT 1 COMMENT '地区' AFTER `gender`,
ADD COLUMN `style` TINYINT UNSIGNED DEFAULT 1 COMMENT '风格' AFTER `area`,
ADD INDEX `idx_music_singer_area` (`area`),
ADD INDEX `idx_music_singer_style` (`style`);
