-- Інформаційна система обліку дефектів будівельних об'єктів (варіант 16)
-- Скрипт створює базу з нуля: видаляє попередню навчальну базу й створює нову.

DROP DATABASE IF EXISTS construction_defects;
CREATE DATABASE construction_defects CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE construction_defects;

CREATE TABLE building_object (
    id      BIGINT AUTO_INCREMENT PRIMARY KEY,
    code    VARCHAR(40)  NOT NULL,
    name    VARCHAR(160) NOT NULL,
    address VARCHAR(200) NOT NULL
) ENGINE=InnoDB;

CREATE TABLE inspection (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    object_id      BIGINT       NOT NULL,
    defect_type    VARCHAR(80)  NOT NULL,
    measured_value DOUBLE       NOT NULL,
    allowed_value  DOUBLE       NOT NULL,
    scale_score    DOUBLE       NOT NULL,  -- F2: площа/масштаб (0..100)
    safety_score   DOUBLE       NOT NULL,  -- F3: вплив на безпеку (0..100)
    CONSTRAINT fk_inspection_object FOREIGN KEY (object_id)
        REFERENCES building_object(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE defect (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    inspection_id BIGINT      NOT NULL,
    state         VARCHAR(30) NOT NULL,   -- RECORDED, VERIFIED, REPAIR_ASSIGNED, CLOSED
    f1            DOUBLE      NOT NULL,
    f2            DOUBLE      NOT NULL,
    f3            DOUBLE      NOT NULL,
    risk_index    DOUBLE      NOT NULL,
    CONSTRAINT fk_defect_inspection FOREIGN KEY (inspection_id)
        REFERENCES inspection(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE repair_task (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    defect_id    BIGINT       NOT NULL,
    description  VARCHAR(500) NOT NULL,
    planned_date VARCHAR(10),
    CONSTRAINT fk_task_defect FOREIGN KEY (defect_id)
        REFERENCES defect(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Початкові демонстраційні дані
INSERT INTO building_object(code, name, address) VALUES
    ('OBJ-01', 'Житловий будинок', 'м. Полтава, вул. Соборності, 12'),
    ('OBJ-02', 'Адміністративна будівля', 'м. Полтава, вул. Європейська, 5');

INSERT INTO inspection(object_id, defect_type, measured_value, allowed_value, scale_score, safety_score) VALUES
    (1, 'Осідання фундаменту', 38.0, 25.0, 60, 70),
    (2, 'Прогин перекриття',   55.0, 50.0, 30, 40);
