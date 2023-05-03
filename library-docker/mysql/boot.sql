CREATE USER 'orangutan'@'%' IDENTIFIED BY 'orangutan';
GRANT ALL PRIVILEGES ON *.* TO 'orangutan'@'%' WITH GRANT OPTION;
FLUSH PRIVILEGES;

SET NAMES 'utf8mb4';

CREATE DATABASE library;

USE library;

CREATE TABLE languages (
     id INT NOT NULL AUTO_INCREMENT,
     name VARCHAR(255) UNIQUE NOT NULL,
     PRIMARY KEY (id)
);

CREATE TABLE status (
    id INT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) UNIQUE NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE series (
    id INT NOT NULL AUTO_INCREMENT,
    ongoing BOOLEAN NOT NULL,
    available_count TINYINT NOT NULL,
    read_all_owned BOOLEAN NOT NULL,
    own_all BOOLEAN NOT NULL,
    finished BOOLEAN NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE books (
    id INT NOT NULL AUTO_INCREMENT,
    series_id INT,
    title VARCHAR(255) NOT NULL,
    vol_num TINYINT,
    language_id INT NOT NULL,
    furigana BOOLEAN,
    ln_level BOOLEAN,
    english_sort_name VARCHAR(255) NOT NULL,
    status_id INT NOT NULL,
    start_ts TIMESTAMP,
    complete_ts TIMESTAMP,
    dlu TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    doe TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (series_id) REFERENCES series(id),
    FOREIGN KEY (language_id) REFERENCES languages(id),
    FOREIGN KEY (status_id) REFERENCES status(id)
);