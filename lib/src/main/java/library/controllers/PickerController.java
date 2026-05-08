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
public class PickerController {
    private final BookRepository bookRepository;
    private final BookStatusRepository bookStatusRepository;
    private final SeriesRepository seriesRepository;

    public PickerController(
            BookRepository bookRepository,
            BookStatusRepository bookStatusRepository,
            SeriesRepository seriesRepository
    ) {
        this.bookRepository = bookRepository;
        this.bookStatusRepository = bookStatusRepository;
        this.seriesRepository = seriesRepository;
    }

    @PostMapping("/picker/")
    public List<Book> listBooks(@RequestBody PickerRequestBody body) {
        Integer minLength = body.getMinLength();
        Integer maxLength = body.getMaxLength();
        List<Language> languages = body.getLanguages();
        List<Tag> tags = body.getTags();
        List<Genre> genres = body.getGenres();
        List<Status> statuses = body.getStatuses();

        List<Book> allBooks = bookRepository.findAllByHouseholdIdOrderBySortTitleAsc(CurrentUser.id());
        return allBooks.stream().filter( b -> {
            boolean goodMinLength = minLength == null || minLength <= b.getPageCount();
            boolean goodMaxLength = maxLength == null || b.getPageCount() < maxLength;
            boolean goodLanguage = languages.isEmpty() || b.getLanguages().stream().anyMatch(languages::contains);
            boolean goodTag =  tags.isEmpty() || b.getTags().stream().anyMatch(tags::contains);
            boolean goodGenre = genres.isEmpty() || b.getGenres().stream().anyMatch(genres::contains);

            Optional<BookStatus> bs = bookStatusRepository.findByBookIdAndUserId(b.getId(), CurrentUser.id());
            boolean goodStatus = bs.isEmpty() || statuses.contains(bs.get());

            //series status
            boolean isStandalone = body.isWantStandalone() && seriesRepository.findByBooks(Set.of(b)).isEmpty();
            boolean isNewSeries = body.isWantNewSeries() && isNewSeries(seriesRepository.findByBooks(Set.of(b)), CurrentUser.id());
            boolean isStartedSeries = body.isWantStartedSeries() && !isNewSeries(seriesRepository.findByBooks(Set.of(b)), CurrentUser.id());;

            return goodMinLength && goodMaxLength && goodLanguage && goodTag && goodGenre && goodStatus
                    && (isStandalone || isNewSeries || isStartedSeries);
        }).toList();
    }

    private boolean isNewSeries(List<Series> series, long userId) {
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

    private boolean isUnreadBook(Book book, long userId) {
        Optional<BookStatus> bs = bookStatusRepository.findByBookIdAndUserId(book.getId(), userId);
        return !bs.isPresent()
                || bs.get().getStatus().getName().equals("To Be Read");
    }
}
