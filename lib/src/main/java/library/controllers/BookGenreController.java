package library.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import library.entities.Book;
import library.entities.BookGenre;
import library.entities.Genre;
import library.repositories.BookGenreRepository;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static library.util.JsonUtil.*;

@RestController
@RequestMapping("/bookGenre")
public class BookGenreController extends LibraryController<BookGenre> {

    BookGenreController(BookGenreRepository repository) {
        this.repository = repository;
    }

    @PutMapping(value = "/insert", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void insertBook(@RequestBody Map<String, String> json) {
        Book book = bookRepository.getReferenceById(getLongFromJson("bookId", json));
        Genre genre = genreRepository.getReferenceById(getLongFromJson("genreId", json));
        BookGenre bookGenre = new BookGenre();
        bookGenre.setBook(book);
        bookGenre.setGenre(genre);
        repository.save(bookGenre);
    }

    @GetMapping(value = "/getByBook", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String get(@RequestParam Long bookId) {
        try {
            return mapper.writeValueAsString(((BookGenreRepository) repository).findByBookId(bookId));
        } catch (JsonProcessingException e) {
            return "Error processing json";
        }
    }
}