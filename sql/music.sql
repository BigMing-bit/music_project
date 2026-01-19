CREATE TABLE `app_user` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名（唯一）',
  `password` VARCHAR(255) NOT NULL COMMENT '密码（加密存储）',
  `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
  `avatar_url` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `gender` TINYINT UNSIGNED DEFAULT 1 COMMENT '性别',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0禁用 1正常',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_app_user_username` (`username`),
  KEY `idx_app_user_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='前台用户表';


CREATE TABLE `music_singer` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '歌手ID',
  `name` VARCHAR(100) NOT NULL COMMENT '歌手名',
  `gender` TINYINT UNSIGNED DEFAULT 1 COMMENT '性别',
  `area` TINYINT UNSIGNED DEFAULT 1 COMMENT '地区',
  `style` TINYINT UNSIGNED DEFAULT 1 COMMENT '风格',
  `avatar_url` VARCHAR(255) DEFAULT NULL COMMENT '歌手头像URL',
  `intro` TEXT DEFAULT NULL COMMENT '简介',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0禁用 1启用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_music_singer_name` (`name`),
  KEY `idx_music_singer_status` (`status`),
  KEY `idx_music_singer_area` (`area`),
  KEY `idx_music_singer_style` (`style`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='歌手表';


CREATE TABLE `music_album` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '专辑ID',
  `album_name` VARCHAR(120) NOT NULL COMMENT '专辑名',
  `singer_id` BIGINT UNSIGNED NOT NULL COMMENT '歌手ID',
  `cover_url` VARCHAR(255) DEFAULT NULL COMMENT '专辑封面URL',
  `publish_date` DATE DEFAULT NULL COMMENT '发行日期',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0下架 1上架',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_music_album_singer_id` (`singer_id`),
  KEY `idx_music_album_name` (`album_name`),
  KEY `idx_music_album_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='专辑表';


CREATE TABLE `music_song` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '歌曲ID',
  `song_name` VARCHAR(120) NOT NULL COMMENT '歌曲名',
  `singer_id` BIGINT UNSIGNED NOT NULL COMMENT '歌手ID',
  `album_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '专辑ID',
  `duration_seconds` INT NOT NULL DEFAULT 0 COMMENT '时长（秒）',
  `cover_url` VARCHAR(255) DEFAULT NULL COMMENT '封面URL',
  `audio_url` VARCHAR(255) NOT NULL COMMENT '音频文件URL',
  `lyric` MEDIUMTEXT DEFAULT NULL COMMENT '歌词',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0下架 1上架',
  `play_count` BIGINT NOT NULL DEFAULT 0 COMMENT '播放量',
  `like_count` BIGINT NOT NULL DEFAULT 0 COMMENT '点赞量',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_music_song_name` (`song_name`),
  KEY `idx_music_song_singer_id` (`singer_id`),
  KEY `idx_music_song_album_id` (`album_id`),
  KEY `idx_music_song_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='歌曲表';


CREATE TABLE `music_playlist` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '歌单ID',
  `name` VARCHAR(120) NOT NULL COMMENT '歌单名',
  `creator_type` TINYINT NOT NULL DEFAULT 0 COMMENT '创建者类型：0官方 1用户',
  `creator_user_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '创建者用户ID（creator_type=1时有效）',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '描述',
  `cover_url` VARCHAR(255) DEFAULT NULL COMMENT '封面URL',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0下架 1上架',
  `collect_count` BIGINT NOT NULL DEFAULT 0 COMMENT '收藏数',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_music_playlist_name` (`name`),
  KEY `idx_music_playlist_creator` (`creator_type`, `creator_user_id`),
  KEY `idx_music_playlist_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='歌单表（官方/用户）';



CREATE TABLE `music_playlist_song` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `playlist_id` BIGINT UNSIGNED NOT NULL COMMENT '歌单ID',
  `song_id` BIGINT UNSIGNED NOT NULL COMMENT '歌曲ID',
  `sort` INT NOT NULL DEFAULT 0 COMMENT '歌单内排序（越小越靠前）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_music_playlist_song` (`playlist_id`, `song_id`),
  KEY `idx_music_playlist_song_playlist_id` (`playlist_id`),
  KEY `idx_music_playlist_song_song_id` (`song_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='歌单-歌曲关联表';


CREATE TABLE `music_comment` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '评论ID',
  `song_id` BIGINT UNSIGNED NOT NULL COMMENT '歌曲ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `content` VARCHAR(500) NOT NULL COMMENT '评论内容',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0隐藏 1正常',
  `like_count` INT NOT NULL DEFAULT 0 COMMENT '点赞数',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_music_comment_song_id` (`song_id`),
  KEY `idx_music_comment_user_id` (`user_id`),
  KEY `idx_music_comment_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='歌曲评论表';


CREATE TABLE `music_play_record` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `song_id` BIGINT UNSIGNED NOT NULL COMMENT '歌曲ID',
  `play_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '播放时间',
  PRIMARY KEY (`id`),
  KEY `idx_music_play_record_user_time` (`user_id`, `play_time`),
  KEY `idx_music_play_record_song_id` (`song_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='播放记录表';


CREATE TABLE `music_favorite_song` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `song_id` BIGINT UNSIGNED NOT NULL COMMENT '歌曲ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_music_fav_song` (`user_id`, `song_id`),
  KEY `idx_music_fav_song_song_id` (`song_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户收藏歌曲表';


CREATE TABLE `music_favorite_playlist` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `playlist_id` BIGINT UNSIGNED NOT NULL COMMENT '歌单ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_music_fav_playlist` (`user_id`, `playlist_id`),
  KEY `idx_music_fav_playlist_playlist_id` (`playlist_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户收藏歌单表';



CREATE TABLE `music_banner` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '轮播ID',
  `title` VARCHAR(100) NOT NULL COMMENT '标题',
  `image_url` VARCHAR(255) NOT NULL COMMENT '图片URL',
  `jump_type` TINYINT NOT NULL DEFAULT 0 COMMENT '跳转类型：0无 1歌曲 2歌单 3外链',
  `jump_target` VARCHAR(255) DEFAULT NULL COMMENT '跳转目标（songId/playlistId/url）',
  `sort` INT NOT NULL DEFAULT 0 COMMENT '排序（越小越靠前）',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0禁用 1启用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_music_banner_status_sort` (`status`, `sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='首页轮播图表';



CREATE TABLE `music_search_hot` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `keyword` VARCHAR(100) NOT NULL COMMENT '热搜关键词',
  `hot_score` INT NOT NULL DEFAULT 0 COMMENT '热度分值',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0禁用 1启用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_music_search_hot_keyword` (`keyword`),
  KEY `idx_music_search_hot_score` (`hot_score`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='热搜词表';



CREATE TABLE `sys_admin` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '管理员ID',
  `username` VARCHAR(50) NOT NULL COMMENT '管理员账号（唯一）',
  `password` VARCHAR(255) NOT NULL COMMENT '密码（加密存储）',
  `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0禁用 1启用',
  `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_admin_username` (`username`),
  KEY `idx_sys_admin_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='后台管理员表';



CREATE TABLE `sys_role` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_name` VARCHAR(50) NOT NULL COMMENT '角色名（展示用）',
  `role_code` VARCHAR(50) NOT NULL COMMENT '角色编码（唯一，如 SUPER_ADMIN/OP_ADMIN）',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0禁用 1启用',
  `remark` VARCHAR(200) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_role_code` (`role_code`),
  KEY `idx_sys_role_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';



CREATE TABLE `sys_permission` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '权限ID',
  `permission_code` VARCHAR(100) NOT NULL COMMENT '权限标识（模块:资源:动作）',
  `permission_name` VARCHAR(100) NOT NULL COMMENT '权限名称（展示用）',
  `module` VARCHAR(50) DEFAULT NULL COMMENT '模块（如 music/sys）',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0禁用 1启用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_permission_code` (`permission_code`),
  KEY `idx_sys_permission_module` (`module`),
  KEY `idx_sys_permission_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限点表';


CREATE TABLE `sys_role_permission` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_id` BIGINT UNSIGNED NOT NULL COMMENT '角色ID',
  `permission_id` BIGINT UNSIGNED NOT NULL COMMENT '权限ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_role_permission` (`role_id`, `permission_id`),
  KEY `idx_sys_role_permission_role_id` (`role_id`),
  KEY `idx_sys_role_permission_permission_id` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色-权限关联表';



CREATE TABLE `sys_admin_role` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `admin_id` BIGINT UNSIGNED NOT NULL COMMENT '管理员ID',
  `role_id` BIGINT UNSIGNED NOT NULL COMMENT '角色ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_admin_role` (`admin_id`, `role_id`),
  KEY `idx_sys_admin_role_admin_id` (`admin_id`),
  KEY `idx_sys_admin_role_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员-角色关联表';


CREATE TABLE `sys_menu` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
  `parent_id` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '父级ID（0为根）',
  `menu_name` VARCHAR(50) NOT NULL COMMENT '菜单名称',
  `menu_type` TINYINT NOT NULL COMMENT '类型：1目录 2菜单 3按钮',
  `path` VARCHAR(120) DEFAULT NULL COMMENT '前端路由path（目录/菜单用）',
  `component` VARCHAR(200) DEFAULT NULL COMMENT '前端组件路径（菜单用）',
  `icon` VARCHAR(50) DEFAULT NULL COMMENT '图标',
  `permission_code` VARCHAR(100) DEFAULT NULL COMMENT '权限标识（按钮/菜单可能绑定）',
  `sort` INT NOT NULL DEFAULT 0 COMMENT '排序（越小越靠前）',
  `visible` TINYINT NOT NULL DEFAULT 1 COMMENT '是否可见：0隐藏 1显示',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0禁用 1启用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_sys_menu_parent_sort` (`parent_id`, `sort`),
  KEY `idx_sys_menu_type` (`menu_type`),
  KEY `idx_sys_menu_status` (`status`),
  KEY `idx_sys_menu_permission_code` (`permission_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='后台菜单表（目录/菜单/按钮）';


CREATE TABLE `sys_operation_log` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `admin_id` BIGINT UNSIGNED NOT NULL COMMENT '管理员ID',
  `admin_username` VARCHAR(50) NOT NULL COMMENT '管理员账号（冗余便于查询）',
  `module` VARCHAR(50) DEFAULT NULL COMMENT '模块（如 music/sys）',
  `action` VARCHAR(50) NOT NULL COMMENT '动作（add/edit/delete/publish/login等）',
  `method` VARCHAR(10) NOT NULL COMMENT 'HTTP方法',
  `path` VARCHAR(200) NOT NULL COMMENT '请求路径',
  `params` TEXT DEFAULT NULL COMMENT '请求参数（建议脱敏）',
  `ip` VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
  `success` TINYINT NOT NULL DEFAULT 1 COMMENT '是否成功：0失败 1成功',
  `error_msg` VARCHAR(500) DEFAULT NULL COMMENT '失败原因',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`id`),
  KEY `idx_sys_oplog_admin_time` (`admin_id`, `create_time`),
  KEY `idx_sys_oplog_path` (`path`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='后台操作日志表';
