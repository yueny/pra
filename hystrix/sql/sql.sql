-- from kapo-example.sql
DROP TABLE IF EXISTS TEST.MODIFY_DEMO;
CREATE TABLE TEST.MODIFY_DEMO
(
   ID        BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
   NAME      VARCHAR(50) NOT NULL COMMENT '名称',
   `DESC` VARCHAR(50) COMMENT '描述',
   `TYPE` ENUM('Y','N') DEFAULT 'Y' NOT NULL COMMENT '状态',
   EVENT_DATA      VARCHAR(1024) COMMENT '事件数据',
   VERSION              INT COMMENT '版本',
   MODIFY_USER          CHAR(18) COMMENT '修改人',
   CREATE_TIME          TIMESTAMP COMMENT '创建时间',
   UPDATE_TIME          DATETIME COMMENT '更新时间',
   PRIMARY KEY (ID)
)
ENGINE = INNODB
COLLATE = UTF8_GENERAL_CI;
ALTER TABLE TEST.MODIFY_DEMO COMMENT 'MODIFY_DEMO表';