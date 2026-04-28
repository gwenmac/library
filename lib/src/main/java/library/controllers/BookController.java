package library.controllers;

import library.entities.Book;
import library.entities.Series;
import library.repositories.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
public class BookController {

    private final BookRepository bookRepository;
    private final SeriesRepository seriesRepository;

    public BookController(BookRepository bookRepository, SeriesRepository seriesRepository) {
        this.bookRepository = bookRepository;
        this.seriesRepository = seriesRepository;
    }

    @GetMapping("/all")
    public List<Book> getAll() {
        return bookRepository.findAll();
    }

    @PostMapping("/books")
    @ResponseStatus(HttpStatus.CREATED)
    public Book create(@RequestBody Map<String, Object> body) {
        Book book = new Book();
        book.setTitle((String) body.get("title"));
        book.setAuthors((String) body.get("authors"));
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
        book.setCreatedAt(LocalDateTime.now());
        book.setUpdatedAt(LocalDateTime.now());
        return bookRepository.save(book);
    }

    @GetMapping("/books/{id}")
    public Book getById(@PathVariable Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found"));
    }

    @PutMapping("/books/{id}")
    public Book update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found"));

        if (body.containsKey("title")) {
            book.setTitle((String) body.get("title"));
        }
        if (body.containsKey("authors")) {
            book.setAuthors((String) body.get("authors"));
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
}