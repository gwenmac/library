USE library;

-- -------------------------------------------------------
-- Genres
-- -------------------------------------------------------
INSERT INTO genres (name) VALUES
    ('Fantasy'),
    ('Science Fiction'),
    ('Mystery'),
    ('Horror'),
    ('Romance'),
    ('Historical Fiction'),
    ('Manga');

-- -------------------------------------------------------
-- Languages
-- -------------------------------------------------------
INSERT INTO languages (name, code) VALUES
    ('English',  'en'),
    ('Japanese', 'ja');

-- -------------------------------------------------------
-- Editions
-- editions: 1=Hardcover, 2=Paperback, 3=Mass Market Paperback, 4=Kindle
-- -------------------------------------------------------
INSERT INTO editions (name) VALUES
    ('Hardcover'),
    ('Paperback'),
    ('Mass Market Paperback'),
    ('Kindle');

-- -------------------------------------------------------
-- Authors
-- -------------------------------------------------------
INSERT INTO authors (name) VALUES
    ('J.R.R. Tolkien'),
    ('Frank Herbert'),
    ('Agatha Christie'),
    ('Tsugumi Ohba'),
    ('Hiromu Arakawa');

-- -------------------------------------------------------
-- Series
-- -------------------------------------------------------
INSERT INTO series (name, status) VALUES
    ('The Lord of the Rings', 'IN_PROGRESS'),
    ('Death Note',            'NOT_STARTED'),
    ('Fullmetal Alchemist',   'NOT_STARTED');

-- -------------------------------------------------------
-- Books
-- statuses: 1=Not Started, 2=In Progress, 3=Completed,
--           4=Did Not Finish, 5=Paused, 6=Not Applicable,
--           7=Not Interested
-- -------------------------------------------------------
INSERT INTO books (title, description, page_count, year, sort_title, series_id, series_order, edition_id, created_at, updated_at) VALUES
    -- Lord of the Rings trilogy (series_id = 1)
    ('The Fellowship of the Ring',
     'The first part of Tolkien\'s epic fantasy where the Fellowship sets out to destroy the One Ring.',
     423, 1954, 'Fellowship of the Ring', 1, 1, 2, NOW(), NOW()),

    ('The Two Towers',
     'The Fellowship is broken; Frodo and Sam continue toward Mordor while war erupts in Rohan.',
     352, 1954, 'Two Towers', 1, 2, 2, NOW(), NOW()),

    ('The Return of the King',
     'The final volume of the trilogy: the War of the Ring reaches its climax.',
     416, 1955, 'Return of the King', 1, 3, 2, NOW(), NOW()),

    -- Standalone
    ('Dune',
     'A desert planet, a noble family betrayed, and a boy destined to change the universe.',
     412, 1965, 'Dune', NULL, NULL, 1, NOW(), NOW()),

    ('And Then There Were None',
     'Ten strangers are lured to an isolated island and begin to die one by one.',
     264, 1939, 'And Then There Were None', NULL, NULL, 3, NOW(), NOW()),

    -- Death Note manga (series_id = 2)
    ('Death Note, Vol. 1',
     'High school student Light Yagami finds a supernatural notebook that can kill anyone.',
     200, 2003, 'Death Note 001', 2, 1, 2, NOW(), NOW()),

    ('Death Note, Vol. 2',
     'The game of cat-and-mouse between Light and the mysterious detective L intensifies.',
     200, 2003, 'Death Note 002', 2, 2, 2, NOW(), NOW()),

    -- Fullmetal Alchemist manga (series_id = 3)
    ('Fullmetal Alchemist, Vol. 1',
     'Two brothers use alchemy to try to resurrect their mother, with devastating consequences.',
     192, 2001, 'Fullmetal Alchemist 001', 3, 1, 2, NOW(), NOW());

-- -------------------------------------------------------
-- Book authors  (book_authors = @JoinTable join table)
-- authors: 1=Tolkien, 2=Herbert, 3=Christie,
--          4=Tsugumi Ohba, 5=Hiromu Arakawa
-- books:   1=Fellowship, 2=Two Towers, 3=Return,
--          4=Dune, 5=Christie, 6=DN1, 7=DN2, 8=FMA1
-- -------------------------------------------------------
INSERT INTO book_authors (book_id, author_id) VALUES
    (1, 1), -- Fellowship of the Ring → J.R.R. Tolkien
    (2, 1), -- The Two Towers        → J.R.R. Tolkien
    (3, 1), -- The Return of the King→ J.R.R. Tolkien
    (4, 2), -- Dune                  → Frank Herbert
    (5, 3), -- And Then There Were None → Agatha Christie
    (6, 4), -- Death Note Vol. 1     → Tsugumi Ohba
    (7, 4), -- Death Note Vol. 2     → Tsugumi Ohba
    (8, 5); -- FMA Vol. 1            → Hiromu Arakawa

-- -------------------------------------------------------
-- Book genres  (book_genres = @JoinTable join table)
-- genres: 1=Fantasy, 2=Sci-Fi, 3=Mystery, 4=Horror,
--         5=Romance, 6=Historical Fiction, 7=Manga
-- books:  1=Fellowship, 2=Two Towers, 3=Return,
--         4=Dune, 5=Christie, 6=DN1, 7=DN2, 8=FMA1
-- -------------------------------------------------------
INSERT INTO book_genres (book_id, genre_id) VALUES
    (1, 1), -- Fellowship of the Ring → Fantasy
    (2, 1), -- The Two Towers        → Fantasy
    (3, 1), -- The Return of the King→ Fantasy
    (4, 2), -- Dune                  → Science Fiction
    (5, 3), -- And Then There Were None → Mystery
    (6, 7), -- Death Note Vol. 1     → Manga
    (6, 1), -- Death Note Vol. 1     → Fantasy
    (7, 7), -- Death Note Vol. 2     → Manga
    (7, 1), -- Death Note Vol. 2     → Fantasy
    (8, 7), -- FMA Vol. 1            → Manga
    (8, 1); -- FMA Vol. 1            → Fantasy

-- -------------------------------------------------------
-- Book languages  (book_languages = @JoinTable join table)
-- languages: 1=English, 2=Japanese
-- -------------------------------------------------------
INSERT INTO book_languages (book_id, language_id) VALUES
    (1, 1), -- Fellowship → English
    (2, 1), -- Two Towers → English
    (3, 1), -- Return     → English
    (4, 1), -- Dune       → English
    (5, 1), -- Christie   → English
    (6, 1), -- DN Vol. 1  → English
    (6, 2), -- DN Vol. 1  → Japanese
    (7, 1), -- DN Vol. 2  → English
    (7, 2), -- DN Vol. 2  → Japanese
    (8, 1), -- FMA Vol. 1 → English
    (8, 2); -- FMA Vol. 1 → Japanese

-- -------------------------------------------------------
-- Book status  (reading progress per book)
-- -------------------------------------------------------
INSERT INTO book_status (book_id, status_id, started_at, finished_at, updated_at) VALUES
    (1, 3, '2025-01-05', '2025-01-20', NOW()),  -- Fellowship: Completed
    (2, 2, '2025-01-21', NULL,          NOW()),  -- Two Towers: In Progress
    (4, 3, '2025-02-10', '2025-02-28', NOW()),  -- Dune: Completed
    (5, 3, '2025-03-01', '2025-03-04', NOW()),  -- Christie: Completed
    (6, 1, NULL,         NULL,          NOW()); -- DN Vol. 1: Not Started

-- -------------------------------------------------------
-- Reviews  (only for completed books)
-- -------------------------------------------------------
INSERT INTO reviews (book_id, rating, notes, created_at) VALUES
    (1, 5, 'A masterpiece of world-building. The journey from the Shire to Rivendell is magical.', '2025-01-21 09:00:00'),
    (4, 4, 'Dense but rewarding. The political and ecological themes hold up remarkably well.', '2025-03-01 10:30:00'),
    (5, 5, 'Brilliant plotting. I did not see the ending coming at all.',                        '2025-03-05 20:00:00');

-- Gauges

INSERT INTO gauges (id, name, description, created_at) VALUES
    (1, 'Books Read VS Bought', 'Positive = ahead on reading, Negative = buying faster than reading', NOW());
