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

CREATE TABLE genre (
    id INT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) UNIQUE NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE series (
    id INT NOT NULL AUTO_INCREMENT,
    title VARCHAR(255) UNIQUE NOT NULL,
    english_sort_title VARCHAR(255) NOT NULL,
    is_ongoing BOOLEAN NOT NULL,
    num_available TINYINT NOT NULL,
    num_owned TINYINT NOT NULL,
    num_read TINYINT NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE book (
    id INT NOT NULL AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    english_sort_title VARCHAR(255) NOT NULL,
    author VARCHAR(255) NOT NULL,
    series INT,
    vol_num TINYINT DEFAULT 1,
    language_id INT NOT NULL,
    has_furigana BOOLEAN,
    reading_level TINYINT,
    status INT NOT NULL,
    start_ts TIMESTAMP DEFAULT NULL,
    complete_ts TIMESTAMP DEFAULT NULL,
    dlu TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    doe TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (series) REFERENCES series(id),
    FOREIGN KEY (language_id) REFERENCES language(id),
    FOREIGN KEY (status) REFERENCES status(id)
);

CREATE TABLE book_genre (
    id INT NOT NULL AUTO_INCREMENT,
    book INT NOT NULL,
    genre INT NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (book) REFERENCES book(id),
    FOREIGN KEY (genre) REFERENCES genre(id)
);