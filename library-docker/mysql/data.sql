USE library;

INSERT INTO language (name) VALUES ("English"), ("日本語");

INSERT INTO status (name) VALUES ("Unread"), ("In Progress"), ("Read");

INSERT INTO series (name, ongoing, available_count, read_all_owned, own_all, finished) VALUES ("series name", true, 19, false, false, false);

INSERT INTO book (series_id, title, vol_num, language_id, furigana, ln_level, english_sort_name, status_id, start_ts, complete_ts)
    VALUES
        (1, "Test", 1, 1, NULL, NULL, "test", 1, NULL, NULL)
;