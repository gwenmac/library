USE library;

INSERT INTO language (name) VALUES ("English"), ("日本語");

INSERT INTO status (name) VALUES ("Unread"), ("In Progress"), ("Read"), ("Stopped");

INSERT INTO series (title, english_sort_title, ongoing, available_count, read_all_owned, own_all, finished)
    VALUES ("Ajin", "ajin", false, 17, true, true, true);

INSERT INTO book (title, english_sort_title, series_id, vol_num, language_id, furigana, ln_level, status_id)
    VALUES("Ajin", "ajin", 1, 1, 1, NULL, NULL, 1);

INSERT INTO tag (name) VALUES ("manga");

INSERT INTO book_tag (book_id, tag_id) VALUES (1, 1);