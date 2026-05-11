package library.controllers;

import library.entities.*;
import library.repositories.BookRepository;
import library.repositories.BookStatusRepository;
import library.repositories.SeriesRepository;
import library.security.CurrentUser;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@RestController
public class SuggesterController {
    private final BookRepository bookRepository;
    private final BookStatusRepository bookStatusRepository;
    private final SeriesRepository seriesRepository;

    public SuggesterController(
            BookRepository bookRepository,
            BookStatusRepository bookStatusRepository,
            SeriesRepository seriesRepository
    ) {
        this.bookRepository = bookRepository;
        this.bookStatusRepository = bookStatusRepository;
        this.seriesRepository = seriesRepository;
    }

    @PostMapping("/suggester")
    public List<Book> listBooks(@RequestBody SuggesterRequestBody body) {
        List<Book> allBooks = bookRepository.findAllByHouseholdIdOrderBySortTitleAsc(CurrentUser.id());
        return allBooks.stream().filter( b -> bookMatches(b, body)).toList();
    }

    protected boolean bookMatches(Book book, SuggesterRequestBody body) {
        return bookLengthMatches(book, body.getMinLength(), body.getMaxLength(), body.isIncludeNoPageCount())
                && bookLanguageMatches(book, body.getLanguages())
                && bookTagMatches(book, body.getTags())
                && bookGenreMatches(book, body.getGenres())
                && bookStatusMatches(book, body.getStatuses());
//                && bookSeriesChecksMatches(book, body.isWantStandalone(), body.isWantNewSeries(), body.isWantStartedSeries());
    }

    protected boolean bookLengthMatches(Book book, Integer minLength, Integer maxLength, boolean includeNoPageCount) {
        if(book.getPageCount() == null && includeNoPageCount) {
            return true;
        }
        if (book.getPageCount() == null && !includeNoPageCount) {
            return false;
        }
        boolean matchesMinLength = minLength == null || minLength <= book.getPageCount();
        boolean matchesMaxLength = maxLength == null || book.getPageCount() <= maxLength;
        return matchesMinLength && matchesMaxLength;
    }

    protected boolean bookLanguageMatches(Book book, List<Language> languages) {
        return languages.isEmpty() || book.getLanguages().stream().map(Language::getId)
                .anyMatch(l -> languages.stream().map(Language::getId).toList().contains(l));
    }

    protected boolean bookTagMatches(Book book, List<Tag> tags) {
        return tags.isEmpty() || book.getTags().stream().map(Tag::getId)
                .anyMatch(t -> tags.stream().map(Tag::getId).toList().contains(t));
    }

    protected boolean bookGenreMatches(Book book, List<Genre> genres) {
        return genres.isEmpty() || book.getGenres().stream().map(Genre::getId)
                .anyMatch(g -> genres.stream().map(Genre::getId).toList().contains(g));
    }

    protected boolean bookStatusMatches(Book book, List<Status> statuses) {
        Optional<BookStatus> bs = bookStatusRepository.findByBookIdAndUserId(book.getId(), CurrentUser.id());
        return bs.isEmpty() || (statuses != null && statuses.stream()
                .map(Status::getId).toList().contains(bs.get().getStatus().getId()));
    }

    protected boolean bookSeriesChecksMatches(Book book, boolean wantStandalone, boolean wantNew, boolean wantStarted) {
        boolean standaloneMatch = wantStandalone && seriesRepository.findByBooks(Set.of(book)).isEmpty();
        boolean newMatch = wantNew && isNewSeries(seriesRepository.findByBooks(Set.of(book)), CurrentUser.id());
        boolean startedMatch = wantStarted && !isNewSeries(seriesRepository.findByBooks(Set.of(book)), CurrentUser.id());
        return standaloneMatch && newMatch && startedMatch;
    }

    protected boolean isNewSeries(List<Series> series, long userId) {
        for (Series s : series) {
            List<Book> books = s.getBooks().stream().toList();
            if (books.size() > 1) {
                for (Book b : books) {
                    if (!isUnreadBook(b, userId)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    protected boolean isUnreadBook(Book book, long userId) {
        Optional<BookStatus> bs = bookStatusRepository.findByBookIdAndUserId(book.getId(), userId);
        return bs.isEmpty() || bs.get().getStatus().getName().equals("To Be Read");
    }
}
