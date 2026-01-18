-- System Info
CREATE TABLE IF NOT EXISTS system_info
(
    system_id            INT    NOT NULL AUTO_INCREMENT,
    db_version           DOUBLE NOT NULL,
    current_plot_version DOUBLE NOT NULL,
    last_update          TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    description          TEXT   NULL,
    PRIMARY KEY (system_id)
);

-- Build Team
CREATE TABLE IF NOT EXISTS build_team
(
    build_team_id   INT          NOT NULL AUTO_INCREMENT,
    name            VARCHAR(255) NOT NULL,
    api_key         VARCHAR(255) NULL UNIQUE,
    api_create_date DATETIME     NULL,
    PRIMARY KEY (build_team_id)
);

-- Server
CREATE TABLE IF NOT EXISTS server
(
    build_team_id INT          NOT NULL,
    server_name   VARCHAR(255) NOT NULL UNIQUE,
    PRIMARY KEY (build_team_id, server_name),
    FOREIGN KEY (build_team_id) REFERENCES build_team (build_team_id)
        ON DELETE RESTRICT ON UPDATE CASCADE
);

-- Countries
CREATE TABLE IF NOT EXISTS country
(
    country_code      VARCHAR(2)                           NOT NULL,
    continent         ENUM ('EU','AS','AF','OC','SA','NA') NOT NULL,
    material          VARCHAR(255)                         NOT NULL,
    custom_model_data VARCHAR(255)                         NULL,
    PRIMARY KEY (country_code)
);

-- City Projects
CREATE TABLE IF NOT EXISTS city_project
(
    city_project_id VARCHAR(255) NOT NULL,
    build_team_id   INT          NOT NULL,
    country_code    VARCHAR(2)   NOT NULL,
    server_name     VARCHAR(255) NOT NULL,
    is_visible      BOOLEAN      NOT NULL DEFAULT 1,
    PRIMARY KEY (city_project_id),
    FOREIGN KEY (build_team_id) REFERENCES build_team (build_team_id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    FOREIGN KEY (country_code) REFERENCES country (country_code)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    FOREIGN KEY (server_name) REFERENCES server (server_name)
        ON DELETE RESTRICT ON UPDATE CASCADE
);

-- Builders
CREATE TABLE IF NOT EXISTS builder
(
    uuid        VARCHAR(36)  NOT NULL,
    name        VARCHAR(255) NOT NULL UNIQUE,
    score       INT          NOT NULL DEFAULT 0,
    first_slot  INT          NULL,
    second_slot INT          NULL,
    third_slot  INT          NULL,
    plot_type   INT          NOT NULL,
    PRIMARY KEY (uuid)
);

-- Difficulty
CREATE TABLE IF NOT EXISTS plot_difficulty
(
    difficulty_id     VARCHAR(255) NOT NULL,
    multiplier        DECIMAL(4, 2)         DEFAULT 1.00,
    score_requirement INT          NOT NULL DEFAULT 0,
    PRIMARY KEY (difficulty_id),
    CHECK ( multiplier > 0 ),
    CHECK ( score_requirement >= 0 )
);

-- Plot
CREATE TABLE IF NOT EXISTS plot
(
    plot_id            INT                                                      NOT NULL AUTO_INCREMENT,
    city_project_id    VARCHAR(255)                                             NOT NULL,
    difficulty_id      VARCHAR(255)                                             NOT NULL,
    owner_uuid         VARCHAR(36)                                              NULL,
    status             ENUM ('unclaimed','unfinished','unreviewed','completed') NOT NULL DEFAULT 'unclaimed',
    outline_bounds     TEXT                                                     NOT NULL,
    initial_schematic  MEDIUMBLOB                                               NOT NULL,
    complete_schematic MEDIUMBLOB                                               NULL,
    last_activity_date DATETIME                                                 NULL,
    is_pasted          BOOLEAN                                                  NOT NULL DEFAULT 0,
    mc_version         VARCHAR(8)                                               NULL,
    plot_version       DOUBLE                                                   NOT NULL,
    plot_type          INT                                                      NULL,
    created_by         VARCHAR(36)                                              NOT NULL,
    create_date        DATETIME                                                 NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (plot_id),
    FOREIGN KEY (city_project_id) REFERENCES city_project (city_project_id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    FOREIGN KEY (difficulty_id) REFERENCES plot_difficulty (difficulty_id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    FOREIGN KEY (owner_uuid) REFERENCES builder (uuid)
        ON DELETE RESTRICT ON UPDATE CASCADE
);

-- Tutorial
CREATE TABLE IF NOT EXISTS tutorial
(
    tutorial_id              INT         NOT NULL,
    uuid                     VARCHAR(36) NOT NULL,
    stage_id                 INT         NOT NULL DEFAULT 0,
    is_complete              BOOLEAN     NOT NULL DEFAULT 0,
    first_stage_start_date   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_stage_complete_date DATETIME    NULL,
    PRIMARY KEY (tutorial_id, uuid)
);

-- Review
CREATE TABLE IF NOT EXISTS plot_review
(
    review_id   INT          NOT NULL AUTO_INCREMENT,
    plot_id     INT          NOT NULL,
    rating      VARCHAR(7)   NOT NULL,
    score       INT          NOT NULL DEFAULT 0,
    feedback    VARCHAR(512) NULL,
    reviewed_by VARCHAR(36)  NOT NULL,
    review_date DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (review_id),
    FOREIGN KEY (plot_id) REFERENCES plot (plot_id)
        ON DELETE CASCADE ON UPDATE CASCADE
);

-- Toggle Criteria
CREATE TABLE IF NOT EXISTS review_toggle_criteria
(
    criteria_name VARCHAR(255) NOT NULL,
    is_optional   BOOLEAN      NOT NULL,
    PRIMARY KEY (criteria_name)
);

-- Build team uses toggle criteria
CREATE TABLE IF NOT EXISTS build_team_uses_toggle_criteria
(
    build_team_id INT          NOT NULL,
    criteria_name VARCHAR(255) NOT NULL,
    PRIMARY KEY (build_team_id, criteria_name),
    FOREIGN KEY (build_team_id) REFERENCES build_team (build_team_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (criteria_name) REFERENCES review_toggle_criteria (criteria_name)
        ON DELETE CASCADE ON UPDATE CASCADE
);

-- Review contains toggle criteria
CREATE TABLE IF NOT EXISTS review_contains_toggle_criteria
(
    review_id     INT          NOT NULL,
    criteria_name VARCHAR(255) NOT NULL,
    is_checked    BOOLEAN      NOT NULL,
    PRIMARY KEY (review_id, criteria_name),
    FOREIGN KEY (review_id) REFERENCES plot_review (review_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (criteria_name) REFERENCES review_toggle_criteria (criteria_name)
        ON DELETE CASCADE ON UPDATE CASCADE
);

-- Build team has reviewer
CREATE TABLE IF NOT EXISTS build_team_has_reviewer
(
    build_team_id INT         NOT NULL,
    uuid          VARCHAR(36) NOT NULL,
    PRIMARY KEY (build_team_id, uuid),
    FOREIGN KEY (build_team_id) REFERENCES build_team (build_team_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (uuid) REFERENCES builder (uuid)
        ON DELETE CASCADE ON UPDATE CASCADE
);

-- Builder is plot member
CREATE TABLE IF NOT EXISTS builder_is_plot_member
(
    plot_id INT         NOT NULL,
    uuid    VARCHAR(36) NOT NULL,
    PRIMARY KEY (plot_id, uuid),
    FOREIGN KEY (plot_id) REFERENCES plot (plot_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (uuid) REFERENCES builder (uuid)
        ON DELETE CASCADE ON UPDATE CASCADE
);

-- Builder has review notification
CREATE TABLE IF NOT EXISTS builder_has_review_notification
(
    review_id INT         NOT NULL,
    uuid      VARCHAR(36) NOT NULL,
    PRIMARY KEY (review_id, uuid),
    FOREIGN KEY (review_id) REFERENCES plot_review (review_id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (uuid) REFERENCES builder (uuid)
        ON DELETE CASCADE ON UPDATE CASCADE
);

-- Insert initial system info
INSERT INTO system_info (system_id, db_version, current_plot_version, description)
SELECT 1, 2.0, 4.0, 'Initial database schema for Plot-System v5.0'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM system_info);

-- Insert initial review toggle criterias
INSERT INTO review_toggle_criteria (criteria_name, is_optional)
VALUES ('built_on_outlines', 0),
       ('correct_amount_windows_doors', 0),
       ('correct_facade_colour', 0),
       ('correct_height', 0),
       ('correct_roof_colour', 1),
       ('correct_roof_shape', 0),
       ('correct_window_type', 1),
       ('windows_blacked_out', 0)
ON DUPLICATE KEY UPDATE criteria_name = VALUES(criteria_name);

INSERT INTO plot_difficulty (difficulty_id, multiplier)
VALUES ('EASY', 1.0),
       ('MEDIUM', 1.5),
       ('HARD', 2)
ON DUPLICATE KEY UPDATE difficulty_id = VALUES(difficulty_id);