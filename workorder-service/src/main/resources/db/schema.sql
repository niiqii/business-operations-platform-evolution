CREATE DATABASE IF NOT EXISTS `bop_workorder`
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;

USE `bop_workorder`;

CREATE TABLE IF NOT EXISTS `t_form_template` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `form_code` VARCHAR(32) NOT NULL COMMENT 'WORKHOUR / ASSESSMENT',
  `schema_version` INT NOT NULL DEFAULT 1,
  `schema_json` JSON NOT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_form_code_version` (`form_code`, `schema_version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `t_work_order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `wo_no` VARCHAR(64) NOT NULL,
  `order_no` VARCHAR(64) NOT NULL,
  `activity_no` VARCHAR(64) NOT NULL,
  `vendor_id` VARCHAR(64) NOT NULL,
  `city_code` VARCHAR(32) NOT NULL,
  `settle_period` VARCHAR(16) NOT NULL COMMENT 'yyyy-MM',
  `biz_type` VARCHAR(32) NOT NULL COMMENT 'WORKHOUR / ASSESSMENT',
  `schema_version` INT NOT NULL DEFAULT 1,
  `status` VARCHAR(32) NOT NULL COMMENT 'DRAFT / VENDOR_FILLING / PM_APPROVING / REJECTED / CLOSED',
  `current_handler` VARCHAR(64) DEFAULT NULL,
  `calc_amount` DECIMAL(18, 2) NOT NULL DEFAULT 0.00,
  `lander_piid` VARCHAR(64) DEFAULT NULL,
  `lander_tiid` VARCHAR(64) DEFAULT NULL,
  `ext_json` JSON DEFAULT NULL,
  `sla_deadline` DATETIME DEFAULT NULL,
  `version` INT NOT NULL DEFAULT 0,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_wo_no` (`wo_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `t_work_order_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `wo_no` VARCHAR(64) NOT NULL,
  `from_status` VARCHAR(32) DEFAULT NULL,
  `to_status` VARCHAR(32) NOT NULL,
  `operator` VARCHAR(64) NOT NULL,
  `action` VARCHAR(32) NOT NULL,
  `remark` VARCHAR(512) DEFAULT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_wo_no` (`wo_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO `t_form_template` (`form_code`, `schema_version`, `schema_json`)
VALUES (
  'WORKHOUR',
  1,
  '{
    "$schema": "http://json-schema.org/draft-07/schema#",
    "type": "object",
    "required": ["priceTiers"],
    "properties": {
      "priceTiers": {
        "type": "array",
        "minItems": 1,
        "maxItems": 4,
        "items": {
          "type": "object",
          "required": ["tierCode", "tierName", "unitPrice", "personDays"],
          "properties": {
            "tierCode": { "type": "string", "minLength": 1 },
            "tierName": { "type": "string", "minLength": 1 },
            "unitPrice": { "type": "number", "minimum": 0 },
            "personDays": { "type": "number", "minimum": 0 },
            "subtotal": { "type": "number", "minimum": 0 }
          },
          "additionalProperties": false
        }
      },
      "attachments": {
        "type": "array",
        "items": {
          "type": "object",
          "required": ["fileName", "fileUrl"],
          "properties": {
            "fileName": { "type": "string", "minLength": 1 },
            "fileUrl": { "type": "string", "minLength": 1 }
          },
          "additionalProperties": false
        }
      },
      "summary": { "type": "string" }
    },
    "additionalProperties": false
  }'
)
ON DUPLICATE KEY UPDATE `schema_json` = VALUES(`schema_json`);

INSERT INTO `t_form_template` (`form_code`, `schema_version`, `schema_json`)
VALUES (
  'ASSESSMENT',
  1,
  '{
    "$schema": "http://json-schema.org/draft-07/schema#",
    "type": "object",
    "required": ["clauseVersion", "deductionItems"],
    "properties": {
      "clauseVersion": { "type": "string", "minLength": 1 },
      "deductionItems": {
        "type": "array",
        "minItems": 1,
        "items": {
          "type": "object",
          "required": ["clauseCode", "clauseName", "minusScore", "minusAmount"],
          "properties": {
            "clauseCode": { "type": "string", "minLength": 1 },
            "clauseName": { "type": "string", "minLength": 1 },
            "minusScore": { "type": "number", "minimum": 0 },
            "minusAmount": { "type": "number", "minimum": 0 },
            "remark": { "type": "string" }
          },
          "additionalProperties": false
        }
      },
      "attachments": {
        "type": "array",
        "items": {
          "type": "object",
          "required": ["fileName", "fileUrl"],
          "properties": {
            "fileName": { "type": "string", "minLength": 1 },
            "fileUrl": { "type": "string", "minLength": 1 }
          },
          "additionalProperties": false
        }
      }
    },
    "additionalProperties": false
  }'
)
ON DUPLICATE KEY UPDATE `schema_json` = VALUES(`schema_json`);
