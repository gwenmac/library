package library.controllers;

import library.entities.Book;
import library.entities.Genre;
import library.entities.Language;
import library.entities.Tag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SuggesterControllerTest {
    SuggesterController suggesterController;

    @BeforeEach
    void setUp() {
        suggesterController = new SuggesterController(null, null, null);
    }

    @Test
    void bookLengthMatches() {
        Book book = new Book();
        book.setPageCount(3);
        assertTrue(suggesterController.bookLengthMatches(book, 0, 6));
        assertTrue(suggesterController.bookLengthMatches(book, 3, 6));
        assertTrue(suggesterController.bookLengthMatches(book, 0, 3));
        assertFalse(suggesterController.bookLengthMatches(book, 10, 30));
    }

    @Test
    void bookLanguageMatches_bookMatchesOne() {
        Book book = new Book();
        Language bookLanguage = new Language();
        bookLanguage.setId(1L);
        book.setLanguages(Set.of(bookLanguage));

        List<Language> languages = new ArrayList<>();
        Language language = new Language();
        language.setId(1L);
        languages.add(language);

        assertTrue(suggesterController.bookLanguageMatches(book, languages));
    }

    @Test
    void bookLanguageMatches_bookDoesNotMatchOne() {
        Book book = new Book();
        Language bookLanguage = new Language();
        bookLanguage.setId(1L);
        book.setLanguages(Set.of(bookLanguage));

        List<Language> languages = new ArrayList<>();
        Language language = new Language();
        language.setId(2L);
        languages.add(language);

        assertFalse(suggesterController.bookLanguageMatches(book, languages));
    }

    @Test
    void bookLanguageMatches_bookMatchesOneOfTwo() {
        Book book = new Book();
        Language bookLanguage = new Language();
        bookLanguage.setId(1L);
        book.setLanguages(Set.of(bookLanguage));

        List<Language> languages = new ArrayList<>();
        Language language1 = new Language();
        language1.setId(1L);
        languages.add(language1);
        Language language2 = new Language();
        language2.setId(2L);
        languages.add(language2);

        assertTrue(suggesterController.bookLanguageMatches(book, languages));
    }

    @Test
    void bookLanguageMatches_bookMatchesWhenZeroOfTwo() {
        Book book = new Book();
        Language bookLanguage = new Language();
        bookLanguage.setId(1L);
        book.setLanguages(Set.of(bookLanguage));

        List<Language> languages = new ArrayList<>();
        Language language1 = new Language();
        language1.setId(2L);
        languages.add(language1);
        Language language2 = new Language();
        language2.setId(3L);
        languages.add(language2);

        assertFalse(suggesterController.bookLanguageMatches(book, languages));
    }

    @Test
    void bookLanguageMatches_bookMatchesNoLanguageSent() {
        Book book = new Book();
        Language bookLanguage = new Language();
        bookLanguage.setId(1L);
        book.setLanguages(Set.of(bookLanguage));

        List<Language> languages = new ArrayList<>();

        assertTrue(suggesterController.bookLanguageMatches(book, languages));
    }

    @Test
    void bookTagMatches_bookMatchesOne() {
        Book book = new Book();
        Tag bookTag = new Tag();
        bookTag.setId(1L);
        book.setTags(Set.of(bookTag));

        List<Tag> tags = new ArrayList<>();
        Tag tag = new Tag();
        tag.setId(1L);
        tags.add(tag);

        assertTrue(suggesterController.bookTagMatches(book, tags));
    }

    @Test
    void bookTagMatches_bookDoesNotMatchOne() {
        Book book = new Book();
        Tag bookTag = new Tag();
        bookTag.setId(1L);
        book.setTags(Set.of(bookTag));

        List<Tag> tags = new ArrayList<>();
        Tag tag = new Tag();
        tag.setId(2L);
        tags.add(tag);

        assertFalse(suggesterController.bookTagMatches(book, tags));
    }

    @Test
    void bookTagMatches_bookMatchesOneOfTwo() {
        Book book = new Book();
        Tag bookTag = new Tag();
        bookTag.setId(1L);
        book.setTags(Set.of(bookTag));

        List<Tag> tags = new ArrayList<>();
        Tag tag1 = new Tag();
        tag1.setId(1L);
        tags.add(tag1);
        Tag tag2 = new Tag();
        tag2.setId(2L);
        tags.add(tag2);

        assertTrue(suggesterController.bookTagMatches(book, tags));
    }

    @Test
    void bookTagMatches_bookMatchesWhenZeroOfTwo() {
        Book book = new Book();
        Tag bookTag = new Tag();
        bookTag.setId(1L);
        book.setTags(Set.of(bookTag));

        List<Tag> tags = new ArrayList<>();
        Tag tag1 = new Tag();
        tag1.setId(2L);
        tags.add(tag1);
        Tag tag2 = new Tag();
        tag2.setId(3L);
        tags.add(tag2);

        assertFalse(suggesterController.bookTagMatches(book, tags));
    }

    @Test
    void bookTagMatches_bookMatchesNoTagsSent() {
        Book book = new Book();
        Tag bookTag = new Tag();
        bookTag.setId(1L);
        book.setTags(Set.of(bookTag));

        List<Tag> tags = new ArrayList<>();

        assertTrue(suggesterController.bookTagMatches(book, tags));
    }

    @Test
    void bookGenreMatches_bookMatchesOne() {
        Book book = new Book();
        Genre bookGenre = new Genre();
        bookGenre.setId(1L);
        book.setGenres(Set.of(bookGenre));

        List<Genre> genres = new ArrayList<>();
        Genre genre = new Genre();
        genre.setId(1L);
        genres.add(genre);

        assertTrue(suggesterController.bookGenreMatches(book, genres));
    }

    @Test
    void bookGenreMatches_bookDoesNotMatchOne() {
        Book book = new Book();
        Genre bookGenre = new Genre();
        bookGenre.setId(1L);
        book.setGenres(Set.of(bookGenre));

        List<Genre> genres = new ArrayList<>();
        Genre genre = new Genre();
        genre.setId(2L);
        genres.add(genre);

        assertFalse(suggesterController.bookGenreMatches(book, genres));
    }

    @Test
    void bookGenreMatches_bookMatchesOneOfTwo() {
        Book book = new Book();
        Genre bookGenre = new Genre();
        bookGenre.setId(1L);
        book.setGenres(Set.of(bookGenre));

        List<Genre> genres = new ArrayList<>();
        Genre genre1 = new Genre();
        genre1.setId(1L);
        genres.add(genre1);
        Genre genre2 = new Genre();
        genre2.setId(2L);
        genres.add(genre2);

        assertTrue(suggesterController.bookGenreMatches(book, genres));
    }

    @Test
    void bookGenreMatches_bookMatchesWhenZeroOfTwo() {
        Book book = new Book();
        Genre bookGenre = new Genre();
        bookGenre.setId(1L);
        book.setGenres(Set.of(bookGenre));

        List<Genre> genres = new ArrayList<>();
        Genre genre1 = new Genre();
        genre1.setId(2L);
        genres.add(genre1);
        Genre genre2 = new Genre();
        genre2.setId(3L);
        genres.add(genre2);

        assertFalse(suggesterController.bookGenreMatches(book, genres));
    }

    @Test
    void bookGenreMatches_bookMatchesNoTagsSent() {
        Book book = new Book();
        Genre bookGenre = new Genre();
        bookGenre.setId(1L);
        book.setGenres(Set.of(bookGenre));

        List<Genre> genres = new ArrayList<>();

        assertTrue(suggesterController.bookGenreMatches(book, genres));
    }
}