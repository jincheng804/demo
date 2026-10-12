-- Existing database upgrade: no DROP TABLE and no replacement of business data.
-- Back up bee_farm first. Run once before deploying the reviewed backend; safe to rerun.
USE bee_farm;
CREATE TABLE IF NOT EXISTS sys_business_lock (id INT PRIMARY KEY) ENGINE=InnoDB;
INSERT IGNORE INTO sys_business_lock(id) VALUES (1);
SET @has_auth_version = (SELECT COUNT(*) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='sys_user' AND column_name='auth_version');
SET @upgrade_sql = IF(@has_auth_version=0, 'ALTER TABLE sys_user ADD COLUMN auth_version INT NOT NULL DEFAULT 0', 'SELECT 1');
PREPARE upgrade_stmt FROM @upgrade_sql;
EXECUTE upgrade_stmt;
DEALLOCATE PREPARE upgrade_stmt;
ALTER TABLE biz_purchase MODIFY COLUMN total_qty DECIMAL(12,2) DEFAULT 0 COMMENT '总数量';
