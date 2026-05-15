package library.controllers;

import library.entities.*;
import library.repositories.BookStatusRepository;
import library.repositories.SeriesRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SuggesterControllerTest {
    SuggesterController suggesterController;
    BookStatusRepository bookStatusRepository = Mockito.mock(BookStatusRepository.class);
    SeriesRepository seriesRepository = Mockito.mock(SeriesRepository.class);

    @BeforeEach
    void setUp() {
        suggesterController = new SuggesterController(null, bookStatusRepository, seriesRepository);
    }

    @Test
    void bookLengthMatches() {
        Book book = new Book();
        book.setPageCount(3);
        assertTrue(suggesterController.bookLengthMatches(book, 0, 6, false));
        assertTrue(suggesterController.bookLengthMatches(book, 3, 6, false));
        assertTrue(suggesterController.bookLengthMatches(book, 0, 3, false));
        assertFalse(suggesterController.bookLengthMatches(book, 10, 30, false));
        book.setPageCount(null);
        assertFalse(suggesterController.bookLengthMatches(book, 0, 6, false));
        assertTrue(suggesterController.bookLengthMatches(book, 3, 6, true));
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

    @Test
    void bookStatusMatches_bookMatchesOne() {
        Book book = new Book();
        book.setId(1L);
        User user = new User();
        user.setId(1L);
        Status status = new Status();
        status.setId(1L);

        BookStatus bookStatus = new BookStatus();
        bookStatus.setId(1L);
        bookStatus.setBook(book);
        bookStatus.setStatus(status);
        bookStatus.setUser(user);

        List<Status> statuses = new ArrayList<>();
        statuses.add(status);

        Mockito.when(bookStatusRepository.findByBookIdAndUserId(Mockito.anyLong(), Mockito.any())).thenReturn(Optional.of(bookStatus));

        assertTrue(suggesterController.bookStatusMatches(book, user, statuses, false));
    }

    @Test
    void bookStatusMatches_bookMatchesWhenMissing() {
        Book book = new Book();
        book.setId(1L);

        User user = new User();
        user.setId(1L);

        List<Status> statuses = new ArrayList<>();

        Mockito.when(bookStatusRepository.findByBookIdAndUserId(Mockito.anyLong(), Mockito.any())).thenReturn(Optional.empty());

        assertTrue(suggesterController.bookStatusMatches(book, user, statuses, true));
    }

    @Test
    void bookStatusMatches_bookMatchesWhenStatusProvidedButIncludeMissing() {
        Book book = new Book();
        book.setId(1L);
        User user = new User();
        user.setId(1L);

        Status status = new Status();
        status.setId(1L);
        List<Status> statuses = new ArrayList<>();
        statuses.add(status);

        Mockito.when(bookStatusRepository.findByBookIdAndUserId(Mockito.anyLong(), Mockito.any())).thenReturn(Optional.empty());

        assertTrue(suggesterController.bookStatusMatches(book, user, statuses, true));
    }

    @Test
    void bookStatusMatches_bookWhenStatusMismatch() {
        Book book = new Book();
        book.setId(1L);
        User user = new User();
        user.setId(1L);
        Status status = new Status();
        status.setId(1L);

        BookStatus bookStatus = new BookStatus();
        bookStatus.setId(1L);
        bookStatus.setBook(book);
        bookStatus.setStatus(status);
        bookStatus.setUser(user);

        Status status2 = new Status();
        status2.setId(2L);
        List<Status> statuses = new ArrayList<>();
        statuses.add(status2);

        Mockito.when(bookStatusRepository.findByBookIdAndUserId(Mockito.anyLong(), Mockito.any())).thenReturn(Optional.of(bookStatus));

        assertFalse(suggesterController.bookStatusMatches(book, user, statuses, false));
    }

    @Test
    void isUnreadBook_noBookStatus() {
        Book book = new Book();
        book.setId(1L);

        Mockito.when(bookStatusRepository.findByBookIdAndUserId(1L, 10L)).thenReturn(Optional.empty());

        assertTrue(suggesterController.isUnreadBook(book, 10L));
    }

    @Test
    void isUnreadBook_statusIsToBeRead() {
        Book book = new Book();
        book.setId(1L);

        Status status = new Status();
        status.setId(1L);
        status.setName("To Be Read");

        BookStatus bookStatus = new BookStatus();
        bookStatus.setStatus(status);

        Mockito.when(bookStatusRepository.findByBookIdAndUserId(1L, 10L)).thenReturn(Optional.of(bookStatus));

        assertTrue(suggesterController.isUnreadBook(book, 10L));
    }

    @Test
    void isUnreadBook_statusIsNotToBeRead() {
        Book book = new Book();
        book.setId(1L);

        Status status = new Status();
        status.setId(2L);
        status.setName("Read");

        BookStatus bookStatus = new BookStatus();
        bookStatus.setStatus(status);

        Mockito.when(bookStatusRepository.findByBookIdAndUserId(1L, 10L)).thenReturn(Optional.of(bookStatus));

        assertFalse(suggesterController.isUnreadBook(book, 10L));
    }

    @Test
    void isUnreadBook_statusIsDifferentNonReadStatus() {
        Book book = new Book();
        book.setId(1L);

        Status status = new Status();
        status.setId(3L);
        status.setName("Currently Reading");

        BookStatus bookStatus = new BookStatus();
        bookStatus.setStatus(status);

        Mockito.when(bookStatusRepository.findByBookIdAndUserId(1L, 10L)).thenReturn(Optional.of(bookStatus));

        assertFalse(suggesterController.isUnreadBook(book, 10L));
    }
}