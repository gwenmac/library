CREATE USER 'orangutan'@'%' IDENTIFIED BY 'orangutan';
GRANT ALL PRIVILEGES ON *.* TO 'orangutan'@'%' WITH GRANT OPTION;
FLUSH PRIVILEGES;

SET NAMES 'utf8mb4';

CREATE DATABASE library;

USE library;

CREATE TABLE language (
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
    title VARCHAR(255) UNIQUE NOT NULL,
    english_sort_title VARCHAR(255) NOT NULL,
    ongoing BOOLEAN NOT NULL,
    available_count TINYINT NOT NULL,
    read_all_owned BOOLEAN NOT NULL,
    own_all BOOLEAN NOT NULL,
    finished BOOLEAN NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE book (
    id INT NOT NULL AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    english_sort_title VARCHAR(255) NOT NULL,
    series_id INT,
    vol_num TINYINT,
    language_id INT NOT NULL,
    furigana BOOLEAN,
    ln_level BOOLEAN,
    status_id INT NOT NULL,
    start_ts TIMESTAMP DEFAULT NULL,
    complete_ts TIMESTAMP DEFAULT NULL,
    dlu TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    doe TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (series_id) REFERENCES series(id),
    FOREIGN KEY (language_id) REFERENCES language(id),
    FOREIGN KEY (status_id) REFERENCES status(id)
);

CREATE TABLE tag (
    id INT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) UNIQUE NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE book_tag (
    id INT NOT NULL AUTO_INCREMENT,
    book_id INT NOT NULL,
    tag_id INT NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (book_id) REFERENCES book(id),
    FOREIGN KEY (tag_id) REFERENCES tag(id)
);