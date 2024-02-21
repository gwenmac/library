USE library;

INSERT INTO language (name) VALUES ("English"), ("日本語");

INSERT INTO status (name) VALUES ("Unread"), ("In Progress"), ("Read"), ("Stopped");

INSERT INTO genre (name) VALUES ("Drama"), ("Slice of Life"), ("BL"), ("Action");

INSERT INTO series (title, english_sort_title, is_ongoing, num_available, num_owned, num_read)
    VALUES ("Acid Town", "acid town", true, 3, 3, 3);

INSERT INTO book (title, english_sort_title, series_id, vol_num, language_id, furigana, ln_level, status_id) VALUES
    ("5 Centimeters Per Second", "5 centimeters per second", NULL, 1, 1, NULL, NULL, 2),
    ("Acid Town 1", "acid town 1", 1, 1, 1, NULL, NULL, 2),
    ("Acid Town 2", "acid town 2", 1, 2, 1, NULL, NULL, 2),
    ("Acid Town 3", "acid town 3", 1, 3, 1, NULL, NULL, 2);

INSERT INTO book_genre (book, genre) VALUES
     (1, 1),
     (1, 2),
     (2, 3),
     (2, 4),
     (3, 3),
     (3, 4),
     (4, 3),
     (4, 4);