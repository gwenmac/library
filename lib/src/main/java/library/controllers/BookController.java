package library.controllers;

import library.entities.*;
import library.repositories.*;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;

@RestController
public class BookController {

    private final BookRepository bookRepository;
    private final SeriesRepository seriesRepository;
    private final AuthorRepository authorRepository;
    private final GenreRepository genreRepository;
    private final StatusRepository statusRepository;

    public BookController(
            BookRepository bookRepository,
            SeriesRepository seriesRepository,
            AuthorRepository authorRepository,
            GenreRepository genreRepository,
            StatusRepository statusRepository
    ) {
        this.bookRepository = bookRepository;
        this.seriesRepository = seriesRepository;
        this.authorRepository = authorRepository;
        this.genreRepository = genreRepository;
        this.statusRepository = statusRepository;
    }

    @GetMapping("/statuses/all")
    public List<Status> getAllStatuses() {
        return statusRepository.findAll();
    }

    @GetMapping("/all")
    public List<Book> getAll() {
        return bookRepository.findAll();
    }

    @Transactional
    @PostMapping("/books")
    @ResponseStatus(HttpStatus.CREATED)
    public Book create(@RequestBody Map<String, Object> body) {
        Book book = new Book();
        book.setTitle((String) body.get("title"));
        book.setDescription((String) body.get("description"));
        if (body.get("pageCount") != null) {
            book.setPageCount(((Number) body.get("pageCount")).intValue());
        }
        if (body.get("seriesId") != null) {
            Long seriesId = ((Number) body.get("seriesId")).longValue();
            Series series = seriesRepository.findById(seriesId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Series not found"));
            book.setSeries(series);
        }
        if (body.get("seriesOrder") != null) {
            book.setSeriesOrder(((Number) body.get("seriesOrder")).intValue());
        }
        if (body.get("authorIds") != null) {
            book.setAuthors(resolveAuthorIds(body.get("authorIds")));
        }
        if (body.get("genreIds") != null) {
            book.setGenres(resolveGenreIds(body.get("genreIds")));
        }
        book.setCreatedAt(LocalDateTime.now());
        book.setUpdatedAt(LocalDateTime.now());
        return bookRepository.save(book);
    }

    @GetMapping("/books/{id}")
    public Book getById(@PathVariable Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found"));
    }

    @Transactional
    @PutMapping("/books/{id}")
    public Book update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found"));

        if (body.containsKey("title")) {
            book.setTitle((String) body.get("title"));
        }
        if (body.containsKey("authorIds")) {
            book.getAuthors().clear();
            book.getAuthors().addAll(resolveAuthorIds(body.get("authorIds")));
        }
        if (body.containsKey("genreIds")) {
            book.getGenres().clear();
            book.getGenres().addAll(resolveGenreIds(body.get("genreIds")));
        }
        if (body.containsKey("description")) {
            book.setDescription((String) body.get("description"));
        }
        if (body.containsKey("pageCount")) {
            book.setPageCount(body.get("pageCount") != null ? ((Number) body.get("pageCount")).intValue() : null);
        }
        if (body.containsKey("seriesId")) {
            if (body.get("seriesId") != null) {
                Long seriesId = ((Number) body.get("seriesId")).longValue();
                Series series = seriesRepository.findById(seriesId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Series not found"));
                book.setSeries(series);
            } else {
                book.setSeries(null);
            }
        }
        if (body.containsKey("seriesOrder")) {
            book.setSeriesOrder(body.get("seriesOrder") != null ? ((Number) body.get("seriesOrder")).intValue() : null);
        }
        if (body.containsKey("statusId")) {
            if (body.get("statusId") != null) {
                Long statusId = ((Number) body.get("statusId")).longValue();
                Status status = statusRepository.findById(statusId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status not found"));
                BookStatus bookStatus = book.getBookStatus();
                if (bookStatus == null) {
                    bookStatus = new BookStatus();
                    bookStatus.setBook(book);
                    book.setBookStatus(bookStatus);
                }
                bookStatus.setStatus(status);
                bookStatus.setUpdatedAt(LocalDateTime.now());
            } else {
                book.setBookStatus(null);
            }
        }

        book.setUpdatedAt(LocalDateTime.now());
        return bookRepository.save(book);
    }

    @Transactional
    @DeleteMapping("/books/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found"));
        book.getAuthors().clear();
        book.getGenres().clear();
        bookRepository.delete(book);
    }

    // ── Helpers ─────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private Set<Author> resolveAuthorIds(Object raw) {
        if (raw == null) return new HashSet<>();
        List<Long> ids = ((List<Number>) raw).stream()
                .map(Number::longValue)
                .toList();
        Set<Author> authors = new HashSet<>(authorRepository.findAllById(ids));
        if (authors.size() != ids.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "One or more author IDs not found");
        }
        return authors;
    }

    @SuppressWarnings("unchecked")
    private Set<Genre> resolveGenreIds(Object raw) {
        if (raw == null) return new HashSet<>();
        List<Long> ids = ((List<Number>) raw).stream()
                .map(Number::longValue)
                .toList();
        Set<Genre> genres = new HashSet<>(genreRepository.findAllById(ids));
        if (genres.size() != ids.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "One or more genre IDs not found");
        }
        return genres;
    }
}