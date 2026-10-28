-- =============================================================================
-- 社团之家 · 完整数据库脚本
-- 版本：1.0.0
-- 日期：2024-04-07
-- 说明：包含所有表结构和初始数据
-- 字符集：utf8mb4；引擎：InnoDB
-- =============================================================================

CREATE DATABASE IF NOT EXISTS club_home
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE club_home;

-- =============================================================================
-- 第一部分：清理旧表（按依赖顺序）
-- =============================================================================
DROP TABLE IF EXISTS `vote_record`;
DROP TABLE IF EXISTS `vote_option`;
DROP TABLE IF EXISTS `vote`;
DROP TABLE IF EXISTS `questionnaire_response`;
DROP TABLE IF EXISTS `questionnaire_question`;
DROP TABLE IF EXISTS `questionnaire`;
DROP TABLE IF EXISTS `user_activity_footprint`;
DROP TABLE IF EXISTS `invite_code`;
DROP TABLE IF EXISTS `club_summary_log`;
DROP TABLE IF EXISTS `club_ai_summary`;
DROP TABLE IF EXISTS `activity_group_member`;
DROP TABLE IF EXISTS `activity_group`;
DROP TABLE IF EXISTS `activity_work`;
DROP TABLE IF EXISTS `activity_signup`;
DROP TABLE IF EXISTS `tutorial_progress`;
DROP TABLE IF EXISTS `tutorial_step`;
DROP TABLE IF EXISTS `tutorial`;
DROP TABLE IF EXISTS `recruitment_application`;
DROP TABLE IF EXISTS `recruitment_post`;
DROP TABLE IF EXISTS `club_question`;
DROP TABLE IF EXISTS `peer_evaluation`;
DROP TABLE IF EXISTS `chat_message`;
DROP TABLE IF EXISTS `announcement_read`;
DROP TABLE IF EXISTS `announcement`;
DROP TABLE IF EXISTS `user_behavior`;
DROP TABLE IF EXISTS `ai_recommendation`;
DROP TABLE IF EXISTS `invitation`;
DROP TABLE IF EXISTS `application`;
DROP TABLE IF EXISTS `club_tag`;
DROP TABLE IF EXISTS `club_activity`;
DROP TABLE IF EXISTS `club_member`;
DROP TABLE IF EXISTS `club`;
DROP TABLE IF EXISTS `user_tag`;
DROP TABLE IF EXISTS `user`;
DROP TABLE IF EXISTS `tag`;
DROP TABLE IF EXISTS `knowledge_article`;
DROP TABLE IF EXISTS `recommendation_rule`;
DROP TABLE IF EXISTS `club_application`;

-- =============================================================================
-- 第二部分：表结构定义
-- =============================================================================

-- 1. 用户表
CREATE TABLE `user` (
    `id`              INT         NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username`        VARCHAR(50) NOT NULL COMMENT '登录用户名',
    `password`        VARCHAR(255) NOT NULL COMMENT '密码',
    `role`            INT         NOT NULL DEFAULT 0 COMMENT '角色：0学生 1社团管理员 2平台管理员',
    `student_id`      VARCHAR(50) DEFAULT NULL COMMENT '学号',
    `real_name`       VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
    `nickname`        VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    `id_card`         VARCHAR(20) DEFAULT NULL COMMENT '身份证号',
    `avatar`          VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
    `phone`           VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `email`           VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `college`         VARCHAR(100) DEFAULT NULL COMMENT '学院',
    `major`           VARCHAR(100) DEFAULT NULL COMMENT '专业',
    `grade`           INT DEFAULT NULL COMMENT '年级',
    `specialty`       VARCHAR(200) DEFAULT NULL COMMENT '特长',
    `hobby`           VARCHAR(200) DEFAULT NULL COMMENT '爱好',
    `expectation`     VARCHAR(500) DEFAULT NULL COMMENT '期望',
    `recommend_boost_club_id` INT DEFAULT NULL COMMENT '推荐加成社团ID',
    `invite_code`     VARCHAR(20) DEFAULT NULL COMMENT '用户生成的邀请码',
    `invited_by`      INT DEFAULT NULL COMMENT '邀请人用户ID',
    `invite_code_used` TINYINT DEFAULT 0 COMMENT '是否使用过邀请码：0否 1是',
    `created_at`      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_student_id` (`student_id`),
    UNIQUE KEY `uk_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 2. 兴趣标签字典
CREATE TABLE `tag` (
    `id`         INT         NOT NULL AUTO_INCREMENT COMMENT '标签ID',
    `name`       VARCHAR(50) NOT NULL COMMENT '标签名称',
    `created_at` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='兴趣标签字典';

-- 3. 用户兴趣标签
CREATE TABLE `user_tag` (
    `id`          INT NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id`     INT NOT NULL COMMENT '用户ID',
    `tag_id`      INT NOT NULL COMMENT '标签ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_tag` (`user_id`, `tag_id`),
    KEY `idx_user` (`user_id`),
    KEY `idx_tag` (`tag_id`),
    CONSTRAINT `fk_user_tag_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户选择的兴趣标签';

-- 4. 社团表
CREATE TABLE `club` (
    `id`          INT         NOT NULL AUTO_INCREMENT COMMENT '社团ID',
    `name`        VARCHAR(100) NOT NULL COMMENT '社团名称',
    `description` TEXT         DEFAULT NULL COMMENT '社团描述',
    `qq_group`    VARCHAR(50)  DEFAULT NULL COMMENT 'QQ群',
    `logo_url`    VARCHAR(500) DEFAULT NULL COMMENT 'Logo URL',
    `admin_id`    INT          DEFAULT NULL COMMENT '管理员用户ID',
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='社团表';

-- 5. 社团成员表
CREATE TABLE `club_member` (
    `id`         BIGINT   NOT NULL AUTO_INCREMENT COMMENT '成员ID',
    `club_id`    BIGINT   NOT NULL COMMENT '社团ID',
    `user_id`    BIGINT   NOT NULL COMMENT '用户ID',
    `role`       TINYINT  NOT NULL DEFAULT 1 COMMENT '角色：1成员 2管理员 3负责人',
    `join_time`  DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
    `status`     TINYINT  NOT NULL DEFAULT 1 COMMENT '状态：1在社 2待审 3已退出',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_club_user` (`club_id`, `user_id`),
    KEY `idx_user` (`user_id`),
    KEY `idx_club` (`club_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='社团成员表';

-- 6. 入社申请表
CREATE TABLE `application` (
    `id`            INT         NOT NULL AUTO_INCREMENT COMMENT '申请ID',
    `user_id`       INT         NOT NULL COMMENT '用户ID',
    `club_id`       INT         NOT NULL COMMENT '社团ID',
    `self_intro`    TEXT        DEFAULT NULL COMMENT '自我介绍',
    `phone`         VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `status`        TINYINT     NOT NULL DEFAULT 0 COMMENT '状态：0待审 1通过 2拒绝',
    `reject_reason` VARCHAR(500) DEFAULT NULL COMMENT '拒绝原因',
    `submitted_at`  DATETIME    DEFAULT NULL COMMENT '提交时间',
    `reviewed_at`   DATETIME    DEFAULT NULL COMMENT '审核时间',
    PRIMARY KEY (`id`),
    KEY `idx_club_status` (`club_id`, `status`),
    KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='入社申请表';

-- 7. 社团活动表
CREATE TABLE `club_activity` (
    `id`                  INT         NOT NULL AUTO_INCREMENT COMMENT '活动ID',
    `club_id`             INT         NOT NULL COMMENT '社团ID',
    `title`               VARCHAR(200) NOT NULL COMMENT '活动标题',
    `content`             TEXT        DEFAULT NULL COMMENT '活动内容',
    `image_url`           VARCHAR(500) DEFAULT NULL COMMENT '图片URL',
    `activity_time`       DATETIME    DEFAULT NULL COMMENT '活动时间',
    `location`            VARCHAR(200) DEFAULT NULL COMMENT '活动地点',
    `current_participants` INT NOT NULL DEFAULT 0 COMMENT '当前报名人数',
    `max_participants`    INT         DEFAULT NULL COMMENT '最大报名人数',
    `status`              TINYINT     NOT NULL DEFAULT 0 COMMENT '状态：0待审 1通过 2拒绝',
    `reject_reason`       VARCHAR(500) DEFAULT NULL COMMENT '拒绝原因',
    `tags`                VARCHAR(500) DEFAULT NULL COMMENT '标签，多个用逗号分隔',
    `created_at`          DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`          DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `reviewed_at`         DATETIME    DEFAULT NULL COMMENT '审核时间',
    PRIMARY KEY (`id`),
    KEY `idx_club` (`club_id`),
    KEY `idx_status` (`status`),
    KEY `idx_participants` (`current_participants`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='社团活动表';

-- 8. 社团标签关联
CREATE TABLE `club_tag` (
    `id`          INT         NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `club_id`     INT         NOT NULL COMMENT '社团ID',
    `tag_id`      INT         NOT NULL COMMENT '标签ID',
    `is_hidden`   TINYINT(1)  NOT NULL DEFAULT 0 COMMENT '是否隐藏：0显示 1隐藏',
    `weight`      DECIMAL(5,2) DEFAULT 1.00 COMMENT '权重',
    `description` VARCHAR(200) DEFAULT NULL COMMENT '描述',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_club_tag` (`club_id`, `tag_id`),
    KEY `idx_club` (`club_id`),
    KEY `idx_tag` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='社团标签关联';

-- 9. AI推荐记录
CREATE TABLE `ai_recommendation` (
    `id`                   INT         NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id`             INT         NOT NULL COMMENT '用户ID',
    `club_id`              INT         NOT NULL COMMENT '社团ID',
    `recommendation_text` TEXT        DEFAULT NULL COMMENT '推荐文本',
    `similarity`          DOUBLE      DEFAULT NULL COMMENT '相似度',
    `status`              VARCHAR(20) DEFAULT NULL COMMENT '状态',
    `created_at`          DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user` (`user_id`),
    KEY `idx_club` (`club_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI推荐记录';

-- 10. 推荐规则
CREATE TABLE `recommendation_rule` (
    `id` INT NOT NULL AUTO_INCREMENT COMMENT '规则ID',
    `max_tag_score` DECIMAL(10, 2) DEFAULT 60.00 COMMENT '标签分上限',
    `qq_boost_score` DECIMAL(10, 2) DEFAULT 20.00 COMMENT 'QQ群加成',
    `hidden_match_unit` DECIMAL(10, 2) DEFAULT 5.00 COMMENT '隐藏标签单位分',
    `max_hidden_score` DECIMAL(10, 2) DEFAULT 20.00 COMMENT '隐藏标签分上限',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='社团推荐规则';

-- 11. 邀请表
CREATE TABLE `invitation` (
    `id`                   INT         NOT NULL AUTO_INCREMENT COMMENT '邀请ID',
    `club_id`              INT         NOT NULL COMMENT '社团ID',
    `user_id`              INT         DEFAULT NULL COMMENT '用户ID',
    `ai_recommendation_id` INT         DEFAULT NULL COMMENT 'AI推荐ID',
    `message`              TEXT        DEFAULT NULL COMMENT '邀请消息',
    `code`                 VARCHAR(50) DEFAULT NULL COMMENT '邀请码',
    `status`               VARCHAR(20) DEFAULT NULL COMMENT '状态',
    `used_at`              DATETIME    DEFAULT NULL COMMENT '使用时间',
    `created_at`           DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_club` (`club_id`),
    KEY `idx_user` (`user_id`),
    KEY `idx_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='邀请表';

-- 12. 系统公告
CREATE TABLE `announcement` (
    `id`         INT         NOT NULL AUTO_INCREMENT COMMENT '公告ID',
    `title`      VARCHAR(200) NOT NULL COMMENT '公告标题',
    `content`    TEXT        DEFAULT NULL COMMENT '公告内容',
    `created_at` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统公告';

-- 13. 公告已读表
CREATE TABLE `announcement_read` (
    `id`              INT      NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `announcement_id`  INT      NOT NULL COMMENT '公告ID',
    `user_id`          INT      NOT NULL COMMENT '用户ID',
    `read_at`          DATETIME DEFAULT NULL COMMENT '阅读时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_announcement_user` (`announcement_id`, `user_id`),
    KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公告已读表';

-- 14. 聊天消息
CREATE TABLE `chat_message` (
    `id`          INT      NOT NULL AUTO_INCREMENT COMMENT '消息ID',
    `sender_id`   INT      NOT NULL COMMENT '发送者ID',
    `content`     TEXT     NOT NULL COMMENT '消息内容',
    `receiver_id` BIGINT   DEFAULT 0 COMMENT '接收者ID：0表示公共聊天室',
    `sent_at`     DATETIME DEFAULT NULL COMMENT '发送时间',
    `created_at`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_sender` (`sender_id`),
    KEY `idx_receiver` (`receiver_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天消息';

-- 15. 同伴互评
CREATE TABLE `peer_evaluation` (
    `id`             INT         NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `evaluator_id`   INT         NOT NULL COMMENT '评价者ID',
    `evaluated_id`   INT         NOT NULL COMMENT '被评价者ID',
    `activity_type`  VARCHAR(50) DEFAULT NULL COMMENT '活动类型',
    `custom_activity` VARCHAR(100) DEFAULT NULL COMMENT '自定义活动',
    `rating`         INT         DEFAULT NULL COMMENT '评分',
    `comment`        TEXT        DEFAULT NULL COMMENT '评价内容',
    `created_at`     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_evaluator` (`evaluator_id`),
    KEY `idx_evaluated` (`evaluated_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='同伴互评';

-- 16. 用户行为
CREATE TABLE `user_behavior` (
    `id`               INT         NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id`          INT         NOT NULL COMMENT '用户ID',
    `page_name`        VARCHAR(100) DEFAULT NULL COMMENT '页面名称',
    `page_params`      VARCHAR(500) DEFAULT NULL COMMENT '页面参数',
    `enter_time`       DATETIME     DEFAULT NULL COMMENT '进入时间',
    `exit_time`        DATETIME     DEFAULT NULL COMMENT '离开时间',
    `duration_seconds` INT          DEFAULT NULL COMMENT '停留秒数',
    `created_at`       DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user` (`user_id`),
    KEY `idx_page` (`page_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户行为';

-- 17. 社团问答
CREATE TABLE `club_question` (
    `id`          INT      NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `club_id`     INT      NOT NULL COMMENT '社团ID',
    `user_id`     INT      NOT NULL COMMENT '用户ID',
    `content`     TEXT     NOT NULL COMMENT '问题内容',
    `answer`      TEXT     DEFAULT NULL COMMENT '回答内容',
    `answered_at` DATETIME DEFAULT NULL COMMENT '回答时间',
    `created_at`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_club` (`club_id`),
    KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='社团问答';

-- 18. 招募广场
CREATE TABLE `recruitment_post` (
    `id`          INT          NOT NULL AUTO_INCREMENT COMMENT '招募帖ID',
    `user_id`     INT          NOT NULL COMMENT '发布者用户ID',
    `title`       VARCHAR(200) DEFAULT NULL COMMENT '标题',
    `content`     TEXT         NOT NULL COMMENT '招募正文',
    `type`        TINYINT      NOT NULL DEFAULT 0 COMMENT '类型：0其他 1技术岗 2运营岗 3设计岗',
    `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1进行中 2已截止',
    `contact`     VARCHAR(200) DEFAULT NULL COMMENT '联系方式',
    `club_id`     INT          DEFAULT NULL COMMENT '关联社团ID',
    `tags`        VARCHAR(500) DEFAULT NULL COMMENT '标签，多个用逗号分隔',
    `deadline`    DATETIME     DEFAULT NULL COMMENT '截止时间',
    `view_count`  INT          DEFAULT 0 COMMENT '浏览次数',
    `updated_at`  DATETIME     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_created` (`created_at`),
    KEY `idx_type` (`type`),
    KEY `idx_status` (`status`),
    KEY `idx_club_id` (`club_id`),
    KEY `idx_deadline` (`deadline`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户招募帖';

-- 19. 招募申请表
CREATE TABLE `recruitment_application` (
    `id`              INT         NOT NULL AUTO_INCREMENT COMMENT '申请ID',
    `post_id`         INT         NOT NULL COMMENT '招募帖ID',
    `user_id`         INT         NOT NULL COMMENT '申请用户ID',
    `message`         TEXT        DEFAULT NULL COMMENT '申请留言',
    `status`          TINYINT     NOT NULL DEFAULT 0 COMMENT '状态：0待审核 1已通过 2已拒绝',
    `created_at`      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_post_user` (`post_id`, `user_id`),
    KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='招募申请表';

-- 20. 教程表
CREATE TABLE `tutorial` (
    `id`              INT          NOT NULL AUTO_INCREMENT COMMENT '教程ID',
    `title`           VARCHAR(200) NOT NULL COMMENT '教程标题',
    `content`         TEXT         DEFAULT NULL COMMENT '教程内容（支持Markdown）',
    `category`        TINYINT      NOT NULL DEFAULT 1 COMMENT '分类：1新手指南 2功能教程 3高级技巧',
    `sort_order`       INT          NOT NULL DEFAULT 0 COMMENT '排序顺序',
    `status`          TINYINT      DEFAULT 1 COMMENT '状态：0草稿 1已发布',
    `created_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_category` (`category`),
    KEY `idx_status` (`status`),
    KEY `idx_sort` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='教程表';

-- 21. 教程步骤表
CREATE TABLE `tutorial_step` (
    `id`             INT          NOT NULL AUTO_INCREMENT COMMENT '步骤ID',
    `tutorial_id`    INT          NOT NULL COMMENT '所属教程ID',
    `step_order`     INT          NOT NULL COMMENT '步骤顺序',
    `title`          VARCHAR(200) NOT NULL COMMENT '步骤标题',
    `content`        TEXT         DEFAULT NULL COMMENT '步骤内容',
    `image_url`      VARCHAR(500) DEFAULT NULL COMMENT '配图URL',
    `video_url`      VARCHAR(500) DEFAULT NULL COMMENT '视频URL',
    `target_action`  VARCHAR(100) DEFAULT NULL COMMENT '目标操作标识',
    `target_param`   VARCHAR(200) DEFAULT NULL COMMENT '目标参数',
    `created_at`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_tutorial` (`tutorial_id`),
    KEY `idx_order` (`step_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='教程步骤表';

-- 22. 教程学习进度表
CREATE TABLE `tutorial_progress` (
    `id`            BIGINT  NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id`       BIGINT  NOT NULL COMMENT '用户ID',
    `tutorial_id`   INT     NOT NULL COMMENT '教程ID',
    `status`        TINYINT DEFAULT 0 COMMENT '状态：0未读 1已读 2已收藏',
    `read_at`       DATETIME DEFAULT NULL COMMENT '阅读时间',
    `created_at`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_tutorial` (`user_id`, `tutorial_id`),
    KEY `idx_user` (`user_id`),
    KEY `idx_tutorial` (`tutorial_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='教程学习进度表';

-- 23. 活动组合表
CREATE TABLE `activity_group` (
    `id`            INT          NOT NULL AUTO_INCREMENT COMMENT '组合ID',
    `activity_id`  INT          DEFAULT NULL COMMENT '关联活动ID',
    `club_id`       INT          DEFAULT NULL COMMENT '社团ID',
    `creator_id`   INT          NOT NULL COMMENT '创建者用户ID',
    `name`          VARCHAR(100) NOT NULL COMMENT '组合名称',
    `description`   VARCHAR(500) DEFAULT NULL COMMENT '组合描述',
    `min_size`      INT          DEFAULT 1 COMMENT '最小人数',
    `max_size`      INT          DEFAULT 10 COMMENT '最大人数',
    `status`        TINYINT      DEFAULT 1 COMMENT '状态：1招募中 2已满员 3已结束',
    `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_activity` (`activity_id`),
    KEY `idx_club` (`club_id`),
    KEY `idx_creator` (`creator_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动组合表';

-- 24. 活动组合成员表
CREATE TABLE `activity_group_member` (
    `id`         BIGINT  NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `group_id`   INT     NOT NULL COMMENT '组合ID',
    `user_id`    BIGINT  NOT NULL COMMENT '用户ID',
    `activity_id` INT    DEFAULT NULL COMMENT '关联的活动ID',
    `status`     TINYINT DEFAULT 1 COMMENT '状态：1已报名 2已取消',
    `joined_at`  DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_group_user` (`group_id`, `user_id`),
    KEY `idx_group` (`group_id`),
    KEY `idx_user` (`user_id`),
    KEY `idx_activity` (`activity_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动组合成员表';

-- 25. 活动报名表
CREATE TABLE `activity_signup` (
    `id`          BIGINT  NOT NULL AUTO_INCREMENT COMMENT '报名ID',
    `activity_id` INT     NOT NULL COMMENT '活动ID',
    `user_id`     BIGINT  NOT NULL COMMENT '用户ID',
    `status`      TINYINT DEFAULT 1 COMMENT '状态：1已报名 2已取消',
    `signup_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '报名时间',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_activity_user` (`activity_id`, `user_id`),
    KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动报名表';

-- 26. 活动作品展示表
CREATE TABLE `activity_work` (
    `id`          BIGINT  NOT NULL AUTO_INCREMENT COMMENT '作品ID',
    `activity_id` INT     NOT NULL COMMENT '活动ID',
    `user_id`     BIGINT  NOT NULL COMMENT '用户ID',
    `group_id`    INT     DEFAULT NULL COMMENT '小组ID',
    `title`       VARCHAR(200) NOT NULL COMMENT '作品标题',
    `content`     TEXT    DEFAULT NULL COMMENT '作品内容/描述',
    `file_url`    VARCHAR(500) DEFAULT NULL COMMENT '作品文件URL',
    `images`      TEXT    DEFAULT NULL COMMENT '作品图片（JSON数组）',
    `status`      TINYINT DEFAULT 1 COMMENT '状态：1展示中 2已获奖 3已取消',
    `created_at`  DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
    `updated_at`  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_activity` (`activity_id`),
    KEY `idx_user` (`user_id`),
    KEY `idx_group` (`group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='活动作品展示表';

-- 27. 社团AI总结表
CREATE TABLE `club_ai_summary` (
    `id`            INT          NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `club_id`       INT          NOT NULL COMMENT '社团ID',
    `summary_type`  VARCHAR(20)  NOT NULL COMMENT '总结类型：monthly/quarterly/annual/full/highlight',
    `title`         VARCHAR(200) DEFAULT NULL COMMENT '总结标题',
    `content`       TEXT         DEFAULT NULL COMMENT 'AI生成的总结内容',
    `tags`          VARCHAR(500) DEFAULT NULL COMMENT '提取的特色标签',
    `highlights`    TEXT         DEFAULT NULL COMMENT '社团亮点（JSON格式）',
    `strengths`     TEXT         DEFAULT NULL COMMENT '优势分析',
    `suitable_for`  TEXT         DEFAULT NULL COMMENT '适合人群',
    `status`        TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：0生成中 1已生成 2生成失败',
    `error_message` VARCHAR(500) DEFAULT NULL COMMENT '错误信息',
    `generated_at`  DATETIME     DEFAULT NULL COMMENT '生成时间',
    `expires_at`    DATETIME     DEFAULT NULL COMMENT '过期时间',
    `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_club_type` (`club_id`, `summary_type`),
    KEY `idx_club` (`club_id`),
    KEY `idx_status` (`status`),
    KEY `idx_expires` (`expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='社团AI总结表';

-- 28. 社团总结生成记录表
CREATE TABLE `club_summary_log` (
    `id`            INT          NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `club_id`       INT          NOT NULL COMMENT '社团ID',
    `summary_id`    INT          DEFAULT NULL COMMENT '关联的总结ID',
    `action`        VARCHAR(50)  NOT NULL COMMENT '操作：generate/refresh/error',
    `trigger_type`  VARCHAR(50)  DEFAULT NULL COMMENT '触发类型：auto/manual/scheduled',
    `trigger_user`  INT          DEFAULT NULL COMMENT '触发用户ID',
    `request_data`  TEXT         DEFAULT NULL COMMENT '请求数据',
    `response_data` TEXT         DEFAULT NULL COMMENT '响应数据',
    `status`        VARCHAR(20)  DEFAULT NULL COMMENT '状态：success/failed',
    `error_message` VARCHAR(500) DEFAULT NULL COMMENT '错误信息',
    `duration_ms`   INT          DEFAULT NULL COMMENT '耗时（毫秒）',
    `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_club` (`club_id`),
    KEY `idx_action` (`action`),
    KEY `idx_created` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='社团总结生成记录表';

-- 29. 邀请码表
CREATE TABLE `invite_code` (
    `id`         INT         NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `code`       VARCHAR(20) NOT NULL COMMENT '邀请码',
    `creator_id` INT         NOT NULL COMMENT '创建者用户ID',
    `club_id`    INT         DEFAULT NULL COMMENT '关联社团ID（可选）',
    `max_uses`   INT         DEFAULT 10 COMMENT '最大使用次数',
    `used_count` INT         DEFAULT 0 COMMENT '已使用次数',
    `expires_at` DATETIME    DEFAULT NULL COMMENT '过期时间',
    `status`     TINYINT     DEFAULT 1 COMMENT '状态：1有效 0无效',
    `created_at` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`),
    KEY `idx_creator` (`creator_id`),
    KEY `idx_club` (`club_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='邀请码表';

-- 30. 用户活动足迹表
CREATE TABLE `user_activity_footprint` (
    `id`          BIGINT  NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `user_id`     INT     NOT NULL COMMENT '用户ID',
    `activity_id` INT     NOT NULL COMMENT '活动ID',
    `action_type` TINYINT NOT NULL COMMENT '行为类型：1浏览 2报名 3签到 4参与 5收藏',
    `created_at`  DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_activity` (`user_id`, `activity_id`, `action_type`),
    KEY `idx_user` (`user_id`),
    KEY `idx_activity` (`activity_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户活动足迹表';

-- 31. 问卷表
CREATE TABLE `questionnaire` (
    `id`              INT          NOT NULL AUTO_INCREMENT COMMENT '问卷ID',
    `club_id`         INT          NOT NULL COMMENT '创建社团ID',
    `title`           VARCHAR(200) NOT NULL COMMENT '问卷标题',
    `description`     TEXT         DEFAULT NULL COMMENT '问卷描述',
    `start_time`      DATETIME     DEFAULT NULL COMMENT '开始时间',
    `end_time`        DATETIME     DEFAULT NULL COMMENT '结束时间',
    `status`          TINYINT      DEFAULT 0 COMMENT '状态：0草稿 1进行中 2已结束',
    `allow_anonymous` TINYINT      DEFAULT 0 COMMENT '允许匿名：0否 1是',
    `tags`            VARCHAR(500) DEFAULT NULL COMMENT '标签，多个用逗号分隔',
    `created_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_club` (`club_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='问卷表';

-- 32. 问卷题目表
CREATE TABLE `questionnaire_question` (
    `id`               INT      NOT NULL AUTO_INCREMENT COMMENT '题目ID',
    `questionnaire_id` INT      NOT NULL COMMENT '问卷ID',
    `question_text`    TEXT     NOT NULL COMMENT '题目内容',
    `question_type`    TINYINT  NOT NULL COMMENT '题目类型：1单选 2多选 3填空',
    `options`          TEXT     DEFAULT NULL COMMENT '选项JSON：["选项1","选项2"]',
    `required`         TINYINT  DEFAULT 1 COMMENT '是否必填：0否 1是',
    `sort_order`       INT      DEFAULT 0 COMMENT '排序',
    PRIMARY KEY (`id`),
    KEY `idx_questionnaire` (`questionnaire_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='问卷题目表';

-- 33. 问卷回答表
CREATE TABLE `questionnaire_response` (
    `id`               BIGINT     NOT NULL AUTO_INCREMENT COMMENT '回答ID',
    `questionnaire_id` INT        NOT NULL COMMENT '问卷ID',
    `user_id`          INT        DEFAULT NULL COMMENT '用户ID（匿名时为空）',
    `anonymous_name`   VARCHAR(50) DEFAULT NULL COMMENT '匿名时显示的名称',
    `answers`          TEXT       DEFAULT NULL COMMENT '回答JSON',
    `submitted_at`     DATETIME   DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
    PRIMARY KEY (`id`),
    KEY `idx_questionnaire` (`questionnaire_id`),
    KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='问卷回答表';

-- 34. 投票表
CREATE TABLE `vote` (
    `id`              INT          NOT NULL AUTO_INCREMENT COMMENT '投票ID',
    `club_id`         INT          NOT NULL COMMENT '创建社团ID',
    `title`           VARCHAR(200) NOT NULL COMMENT '投票标题',
    `description`     TEXT         DEFAULT NULL COMMENT '投票描述',
    `vote_type`       TINYINT      NOT NULL DEFAULT 1 COMMENT '投票类型：1单选 2多选',
    `max_options`     INT          DEFAULT 1 COMMENT '最多可选数量',
    `start_time`      DATETIME     DEFAULT NULL COMMENT '开始时间',
    `end_time`        DATETIME     DEFAULT NULL COMMENT '结束时间',
    `status`          TINYINT      DEFAULT 0 COMMENT '状态：0草稿 1进行中 2已结束',
    `allow_anonymous` TINYINT      DEFAULT 0 COMMENT '允许匿名：0否 1是',
    `show_results`    TINYINT      DEFAULT 1 COMMENT '投票中显示结果：0否 1是',
    `tags`            VARCHAR(500) DEFAULT NULL COMMENT '标签，多个用逗号分隔',
    `created_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_club` (`club_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='投票表';

-- 35. 投票选项表
CREATE TABLE `vote_option` (
    `id`          INT          NOT NULL AUTO_INCREMENT COMMENT '选项ID',
    `vote_id`     INT          NOT NULL COMMENT '投票ID',
    `option_text` VARCHAR(500) NOT NULL COMMENT '选项内容',
    `image_url`   VARCHAR(500) DEFAULT NULL COMMENT '选项图片',
    `vote_count`  INT          DEFAULT 0 COMMENT '得票数',
    `sort_order`  INT          DEFAULT 0 COMMENT '排序',
    PRIMARY KEY (`id`),
    KEY `idx_vote` (`vote_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='投票选项表';

-- 36. 投票记录表
CREATE TABLE `vote_record` (
    `id`               BIGINT     NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `vote_id`          INT        NOT NULL COMMENT '投票ID',
    `user_id`          INT        DEFAULT NULL COMMENT '用户ID（匿名时为空）',
    `anonymous_name`   VARCHAR(50) DEFAULT NULL COMMENT '匿名时显示的名称',
    `selected_options` TEXT       DEFAULT NULL COMMENT '选择的选项ID列表JSON，如[1,3]',
    `voted_at`         DATETIME   DEFAULT CURRENT_TIMESTAMP COMMENT '投票时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_vote_user` (`vote_id`, `user_id`),
    KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='投票记录表';

-- 37. 知识库文章表
CREATE TABLE `knowledge_article` (
    `id`            BIGINT         NOT NULL AUTO_INCREMENT COMMENT '文章ID',
    `author_id`     BIGINT         NOT NULL COMMENT '作者ID',
    `title`         VARCHAR(200)   NOT NULL COMMENT '文章标题',
    `content`       TEXT           DEFAULT NULL COMMENT '文章内容（HTML）',
    `cover_image`   VARCHAR(500)   DEFAULT NULL COMMENT '封面图片URL',
    `images`        TEXT           DEFAULT NULL COMMENT '文章图片列表（JSON数组）',
    `category`      VARCHAR(50)    DEFAULT NULL COMMENT '分类标签',
    `view_count`    INT            DEFAULT 0 COMMENT '浏览量',
    `like_count`    INT            DEFAULT 0 COMMENT '点赞数',
    `created_at`    DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`    DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `status`        TINYINT        DEFAULT 1 COMMENT '状态：0草稿 1已发布 2已删除',
    PRIMARY KEY (`id`),
    KEY `idx_author` (`author_id`),
    KEY `idx_category` (`category`),
    KEY `idx_created_at` (`created_at`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='知识库文章表';

-- 38. 建社申请表
CREATE TABLE `club_application` (
    `id` INT NOT NULL AUTO_INCREMENT COMMENT '建社申请ID',
    `applicant_id` INT NOT NULL COMMENT '申请人 user.id',
    `name` VARCHAR(100) NOT NULL COMMENT '拟建社团名',
    `description` TEXT COMMENT '简介',
    `tags` VARCHAR(500) DEFAULT NULL COMMENT '标签',
    `status` INT DEFAULT 0 COMMENT '0待审 1通过 2未通过',
    `reject_reason` VARCHAR(500) DEFAULT NULL,
    `submitted_at` DATETIME DEFAULT NULL,
    `reviewed_at` DATETIME DEFAULT NULL,
    `reviewer_id` INT DEFAULT NULL,
    `created_club_id` INT DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_applicant` (`applicant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='建社申请';

-- =============================================================================
-- 第三部分：初始数据
-- =============================================================================

-- 兴趣标签字典
INSERT INTO `tag` (`id`, `name`) VALUES
    (1, '编程'), (2, '音乐'), (3, '绘画'), (4, '摄影'), (5, '篮球'),
    (6, '足球'), (7, '游泳'), (8, '跑步'), (9, '舞蹈'), (10, '戏剧'),
    (11, '演讲'), (12, '志愿服务'), (13, '科技创新'), (14, '创业'), (15, '文学'),
    (16, '电竞'), (17, '动漫'), (18, '棋艺'), (19, '书法'), (20, '手工')
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`);

-- 推荐规则
INSERT INTO `recommendation_rule` (`id`, `max_tag_score`, `qq_boost_score`, `hidden_match_unit`, `max_hidden_score`) VALUES
    (1, 60.00, 20.00, 5.00, 20.00)
ON DUPLICATE KEY UPDATE `max_tag_score` = VALUES(`max_tag_score`);

-- 系统公告
INSERT INTO `announcement` (`title`, `content`) VALUES
    ('欢迎加入社团之家', '欢迎各位新同学加入社团之家平台，这里有丰富多彩的社团活动等待你的参与！'),
    ('2024年春季社团招新通知', '各社团招新工作已正式启动，请关注各社团主页了解详情。'),
    ('平台使用指南', '新生必看！如何在社团之家平台找到适合自己的社团？')
ON DUPLICATE KEY UPDATE `title` = VALUES(`title`);

-- 教程数据
INSERT INTO `tutorial` (`title`, `content`, `category`, `sort_order`, `status`) VALUES
    ('新手指南：如何加入社团', '# 如何加入社团\n\n欢迎使用社团之家平台！本教程将帮助您快速上手。\n\n## 第一步：完善个人信息\n\n在加入社团之前，请先完善您的个人信息。\n\n## 第二步：浏览社团\n\n您可以通过首页推荐、社团广场、兴趣标签筛选发现感兴趣的社团。\n\n## 第三步：申请加入\n\n找到心仪的社团后，点击"申请加入"按钮，填写申请表单即可。', 1, 1, 1),
    ('新手指南：AI智能推荐', '# AI智能推荐\n\n我们的AI推荐系统会根据您的兴趣和特长，为您推荐最适合的社团。', 1, 2, 1),
    ('功能教程：招募广场', '# 招募广场使用指南\n\n招募广场是社团发布招募信息的平台，支持按类型、状态筛选。', 2, 3, 1),
    ('功能教程：活动报名', '# 活动报名教程\n\n了解如何报名参加社团活动。', 2, 4, 1),
    ('高级技巧：自定义兴趣标签', '# 自定义兴趣标签\n\n如果您找不到匹配的标签，可以联系管理员添加。', 3, 5, 1)
ON DUPLICATE KEY UPDATE `title` = VALUES(`title`);

-- 测试用户（密码均为123456）
INSERT INTO `user` (`id`, `username`, `password`, `role`, `student_id`, `real_name`, `nickname`, `phone`, `email`, `college`, `major`, `grade`, `specialty`, `invite_code_used`, `created_at`) VALUES
    (1, 'admin001', 'admin123', 2, NULL, '系统管理员', '管理员', '13900000001', 'admin001@test.local', NULL, NULL, NULL, NULL, 0, NOW()),
    (2, 'student01', '123456', 0, '2026001', '测试学生甲', '小甲', '13800000001', 'student01@test.local', '计算机学院', '软件工程', 1, '编程', 0, NOW()),
    (3, 'student02', '123456', 0, '2026002', '测试学生乙', '小乙', '13800000002', 'student02@test.local', '计算机学院', '计算机科学', 1, '算法', 0, NOW()),
    (4, 'clubadmin1', '123456', 1, 'T2026001', '编程社负责人', '程社长', '13800000003', 'clubadmin1@test.local', '计算机学院', '软件工程', 3, 'Java', 0, NOW()),
    (5, 'clubadmin2', '123456', 1, 'T2026002', '篮球社负责人', '篮社长', '13800000004', 'clubadmin2@test.local', '体育学院', '运动训练', 3, '篮球', 0, NOW()),
    (6, 'clubadmin_art', '123456', 1, 'T2026004', '绘画社负责人', '画社长', '13800000005', 'clubadmin_art@test.local', '艺术学院', '视觉传达', 3, '绘画', 0, NOW()),
    (7, 'clubadmin3', '123456', 1, 'T2026003', '志愿社负责人', '志社长', '13800000006', 'clubadmin3@test.local', '文学院', '社会学', 3, '志愿服务', 0, NOW()),
    (8, 'student05', '123456', 0, '2026005', '测试学生戊', '小戊', '13800000007', 'student05@test.local', '经济学院', '金融学', 2, '创业', 0, NOW())
ON DUPLICATE KEY UPDATE `password` = VALUES(`password`);

-- 用户兴趣标签
INSERT INTO `user_tag` (`user_id`, `tag_id`) VALUES
    (2, 1), (2, 13),
    (3, 1), (3, 5),
    (4, 1), (4, 13),
    (5, 5), (5, 8),
    (6, 3), (6, 4),
    (7, 11), (7, 15),
    (8, 14), (8, 13)
ON DUPLICATE KEY UPDATE `user_id` = VALUES(`user_id`);

-- 测试社团
INSERT INTO `club` (`id`, `name`, `description`, `qq_group`, `admin_id`, `created_at`) VALUES
    (1, 'Endtiem 编程社', '算法、工程与开源，定期技术分享与黑客松', '100010001', 4, NOW()),
    (2, '阳光篮球社', '院赛、训练营与新生杯', '100020002', 5, NOW()),
    (3, '艺创绘画社', '写生、展览与工作坊', '100030003', 6, NOW()),
    (4, '星火志愿者', '社区服务与支教', '100040004', 7, NOW())
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`);

-- 社团标签关联
INSERT INTO `club_tag` (`club_id`, `tag_id`, `is_hidden`, `weight`) VALUES
    (1, 1, 0, 1.50), (1, 13, 0, 1.40), (1, 14, 1, 0.80),
    (2, 5, 0, 1.50), (2, 8, 0, 1.00),
    (3, 3, 0, 1.40), (3, 4, 0, 1.30),
    (4, 12, 0, 1.50), (4, 11, 0, 1.00)
ON DUPLICATE KEY UPDATE `weight` = VALUES(`weight`);

-- 社团成员
INSERT INTO `club_member` (`club_id`, `user_id`, `role`, `status`) VALUES
    (1, 4, 3, 1), (1, 2, 1, 1), (1, 3, 1, 1),
    (2, 5, 3, 1), (2, 3, 1, 1),
    (3, 6, 3, 1),
    (4, 7, 3, 1), (4, 8, 1, 1)
ON DUPLICATE KEY UPDATE `role` = VALUES(`role`);

-- 社团活动
INSERT INTO `club_activity` (`club_id`, `title`, `content`, `location`, `activity_time`, `current_participants`, `max_participants`, `status`, `tags`, `created_at`) VALUES
    (1, 'Spring Boot 工作坊', '从 0 搭建社团服务接口', '教学楼 A101', '2026-05-10 14:00:00', 0, 40, 1, '1,13', NOW()),
    (1, '校赛模拟赛 · 算法', 'ACM 风格练习赛', '实验楼 301', '2026-05-18 09:00:00', 0, 60, 1, '1,13', NOW()),
    (2, '新生杯篮球赛', '分组循环 + 淘汰赛', '体育馆', '2026-05-12 15:00:00', 0, 100, 1, '5', NOW()),
    (3, '校园写生', '春季校园户外写生', '图书馆草坪', '2026-05-20 10:00:00', 0, 30, 1, '3,4', NOW()),
    (4, '社区清洁日', '与街道联合志愿服务', '市民广场', '2026-05-08 09:00:00', 0, 50, 1, '12', NOW())
ON DUPLICATE KEY UPDATE `title` = VALUES(`title`);

-- 入社申请
INSERT INTO `application` (`user_id`, `club_id`, `self_intro`, `phone`, `status`, `submitted_at`, `reviewed_at`) VALUES
    (6, 1, '希望参与开源项目维护', '13800000005', 0, NOW(), NULL),
    (7, 2, '喜欢打控球后卫', '13800000006', 1, DATE_SUB(NOW(), INTERVAL 5 DAY), NOW())
ON DUPLICATE KEY UPDATE `status` = VALUES(`status`);

-- 招募帖
INSERT INTO `recruitment_post` (`user_id`, `title`, `content`, `type`, `status`, `contact`, `club_id`, `tags`, `view_count`, `created_at`) VALUES
    (4, '招募后端组干事', '参与 Spring Boot 与 MySQL 维护，要求熟悉 REST', 1, 1, 'QQ群见社团页', 1, '1,13', 12, NOW()),
    (5, '招募篮球队经理', '负责赛程与物资，不限年级', 2, 1, '请联系社长', 2, '5', 8, NOW()),
    (6, '插画师招募', '海报与社媒配图', 3, 1, '邮箱见主页', 3, '3,4', 5, NOW())
ON DUPLICATE KEY UPDATE `title` = VALUES(`title`);

-- 招募申请
INSERT INTO `recruitment_application` (`post_id`, `user_id`, `message`, `status`) VALUES
    (1, 2, '熟悉 Java，想参与接口开发', 0),
    (1, 3, '学过数据结构，希望学习后端', 0)
ON DUPLICATE KEY UPDATE `message` = VALUES(`message`);

-- 建社申请
INSERT INTO `club_application` (`applicant_id`, `name`, `description`, `tags`, `status`, `submitted_at`) VALUES
    (8, '桌游与策略社', '桌游、棋类与轻策略聚会', '18,11', 0, NOW())
ON DUPLICATE KEY UPDATE `status` = VALUES(`status`);

-- 问卷
INSERT INTO `questionnaire` (`club_id`, `title`, `description`, `status`, `allow_anonymous`, `tags`, `created_at`) VALUES
    (1, '技术分享主题征集', '下学期分享方向', 1, 0, '1,13', NOW()),
    (2, '训练时间偏好', '周中/周末', 1, 1, '5', NOW())
ON DUPLICATE KEY UPDATE `title` = VALUES(`title`);

INSERT INTO `questionnaire_question` (`questionnaire_id`, `question_text`, `question_type`, `options`, `required`, `sort_order`) VALUES
    (1, '您最希望的主题', 1, '["后端","前端","DevOps","开源协作"]', 1, 1),
    (2, '可参加的时间段', 2, '["工作日晚上","周末上午","周末下午"]', 1, 1)
ON DUPLICATE KEY UPDATE `question_text` = VALUES(`question_text`);

INSERT INTO `questionnaire_response` (`questionnaire_id`, `user_id`, `answers`, `submitted_at`) VALUES
    (1, 2, '{"1":"后端"}', NOW())
ON DUPLICATE KEY UPDATE `answers` = VALUES(`answers`);

-- 投票
INSERT INTO `vote` (`club_id`, `title`, `description`, `vote_type`, `max_options`, `status`, `tags`, `created_at`) VALUES
    (1, '下次工作坊语言', '单选', 1, 1, 1, '1,13', NOW()),
    (2, '队服主色', NULL, 1, 1, 1, '5', NOW())
ON DUPLICATE KEY UPDATE `title` = VALUES(`title`);

INSERT INTO `vote_option` (`vote_id`, `option_text`, `vote_count`, `sort_order`) VALUES
    (1, 'Java / Spring', 0, 1), (1, 'Python', 0, 2), (1, 'Go', 0, 3),
    (2, '深蓝', 0, 1), (2, '白色', 0, 2)
ON DUPLICATE KEY UPDATE `option_text` = VALUES(`option_text`);

INSERT INTO `vote_record` (`vote_id`, `user_id`, `selected_options`, `voted_at`) VALUES
    (1, 2, '[1]', NOW())
ON DUPLICATE KEY UPDATE `selected_options` = VALUES(`selected_options`);

-- 公告已读
INSERT INTO `announcement_read` (`announcement_id`, `user_id`, `read_at`) VALUES
    (1, 2, NOW())
ON DUPLICATE KEY UPDATE `read_at` = VALUES(`read_at`);

-- 聊天消息
INSERT INTO `chat_message` (`sender_id`, `content`, `receiver_id`, `sent_at`, `created_at`) VALUES
    (2, '大家好，这是一条公共聊天测试消息', 0, NOW(), NOW()),
    (2, '社长好，我想了解下次活动时间', 4, NOW(), NOW())
ON DUPLICATE KEY UPDATE `content` = VALUES(`content`);

-- 用户行为
INSERT INTO `user_behavior` (`user_id`, `page_name`, `page_params`, `enter_time`, `duration_seconds`, `created_at`) VALUES
    (2, 'Home', '/', NOW(), 42, NOW()),
    (2, 'ClubDetail', 'id=1', NOW(), 30, NOW())
ON DUPLICATE KEY UPDATE `duration_seconds` = VALUES(`duration_seconds`);

-- 用户活动足迹
INSERT INTO `user_activity_footprint` (`user_id`, `activity_id`, `action_type`, `created_at`) VALUES
    (2, 1, 1, NOW())
ON DUPLICATE KEY UPDATE `action_type` = VALUES(`action_type`);

-- 同伴互评
INSERT INTO `peer_evaluation` (`evaluator_id`, `evaluated_id`, `activity_type`, `rating`, `comment`, `created_at`) VALUES
    (3, 2, 'WORKSHOP', 5, '协作积极', NOW())
ON DUPLICATE KEY UPDATE `rating` = VALUES(`rating`);

-- 邀请
INSERT INTO `invitation` (`club_id`, `user_id`, `message`, `code`, `status`, `created_at`) VALUES
    (1, 2, '欢迎加入编程社', 'INV2026', 'pending', NOW())
ON DUPLICATE KEY UPDATE `status` = VALUES(`status`);

-- AI 推荐记录
INSERT INTO `ai_recommendation` (`user_id`, `club_id`, `recommendation_text`, `similarity`, `status`, `created_at`) VALUES
    (2, 1, '与您的「编程」「科技创新」标签高度匹配', 0.92, 'generated', NOW())
ON DUPLICATE KEY UPDATE `similarity` = VALUES(`similarity`);

-- 邀请码
INSERT INTO `invite_code` (`code`, `creator_id`, `club_id`, `max_uses`, `used_count`, `expires_at`, `status`, `created_at`) VALUES
    ('TEST2026', 1, NULL, 100, 0, DATE_ADD(NOW(), INTERVAL 180 DAY), 1, NOW()),
    ('PROG2026', 4, 1, 20, 0, DATE_ADD(NOW(), INTERVAL 90 DAY), 1, NOW())
ON DUPLICATE KEY UPDATE `used_count` = VALUES(`used_count`);

-- 教程步骤
INSERT INTO `tutorial_step` (`tutorial_id`, `step_order`, `title`, `content`, `target_action`, `created_at`) VALUES
    (1, 1, '进入个人中心', '点击头像进入资料页', 'open_profile', NOW())
ON DUPLICATE KEY UPDATE `content` = VALUES(`content`);

-- 教程进度
INSERT INTO `tutorial_progress` (`user_id`, `tutorial_id`, `status`, `read_at`, `created_at`) VALUES
    (2, 1, 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE `status` = VALUES(`status`);

-- 活动组合
INSERT INTO `activity_group` (`activity_id`, `club_id`, `creator_id`, `name`, `description`, `min_size`, `max_size`, `status`, `created_at`) VALUES
    (1, 1, 4, '工作坊小组 A', '现场组队', 2, 5, 1, NOW())
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`);

INSERT INTO `activity_group_member` (`group_id`, `user_id`, `activity_id`, `status`, `joined_at`, `created_at`) VALUES
    (1, 2, 1, 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE `status` = VALUES(`status`);

-- 社团问答
INSERT INTO `club_question` (`club_id`, `user_id`, `content`, `answer`, `answered_at`, `created_at`) VALUES
    (1, 2, '工作坊需要自带电脑吗？', '机房提供环境，也可自带笔记本。', NOW(), NOW())
ON DUPLICATE KEY UPDATE `answer` = VALUES(`answer`);

-- 知识库文章
INSERT INTO `knowledge_article` (`author_id`, `title`, `content`, `category`, `view_count`, `like_count`, `status`, `created_at`) VALUES
    (1, '测试环境使用说明', '<p>使用 student01 / 123456 登录验证推荐与活动。</p>', '平台', 1, 0, 1, NOW()),
    (4, '编程社招新 FAQ', '<p>技术栈：Java 17、Spring Boot 3、Vue 3。</p>', '社团', 3, 1, 1, NOW())
ON DUPLICATE KEY UPDATE `title` = VALUES(`title`);

-- 社团 AI 总结
INSERT INTO `club_ai_summary` (`club_id`, `summary_type`, `title`, `content`, `status`, `generated_at`, `created_at`) VALUES
    (1, 'highlight', '编程社亮点', '技术活动丰富，成员活跃', 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE `content` = VALUES(`content`);

-- 自增重置（避免后续插入 ID 冲突）
ALTER TABLE `user` AUTO_INCREMENT = 100;
ALTER TABLE `club` AUTO_INCREMENT = 100;
ALTER TABLE `club_activity` AUTO_INCREMENT = 100;
ALTER TABLE `recruitment_post` AUTO_INCREMENT = 100;
ALTER TABLE `tag` AUTO_INCREMENT = 100;

-- =============================================================================
-- 完成
-- =============================================================================
