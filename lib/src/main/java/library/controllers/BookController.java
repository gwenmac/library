package library.controllers;

import library.entities.Author;
import library.entities.Book;
import library.entities.Series;
import library.repositories.*;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
public class BookController {

    private final BookRepository bookRepository;
    private final SeriesRepository seriesRepository;
    private final AuthorRepository authorRepository;

    public BookController(BookRepository bookRepository, SeriesRepository seriesRepository, AuthorRepository authorRepository) {
        this.bookRepository = bookRepository;
        this.seriesRepository = seriesRepository;
        this.authorRepository = authorRepository;
    }

    // ── Author endpoints ────────────────────────────────────

    @GetMapping("/authors/all")
    public List<Author> getAllAuthors() {
        return authorRepository.findAll();
    }

    @PostMapping("/authors")
    @ResponseStatus(HttpStatus.CREATED)
    public Author createAuthor(@RequestBody Map<String, String> body) {
        Author author = new Author();
        author.setName(body.get("name"));
        return authorRepository.save(author);
    }

    // ── Book endpoints ──────────────────────────────────────

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

        book.setUpdatedAt(LocalDateTime.now());
        return bookRepository.save(book);
    }

    @DeleteMapping("/books/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        if (!bookRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found");
        }
        bookRepository.deleteById(id);
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
}