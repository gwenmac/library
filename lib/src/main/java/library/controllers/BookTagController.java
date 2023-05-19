package library.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import library.entities.Book;
import library.entities.BookTag;
import library.entities.Tag;
import library.repositories.BookTagRepository;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static library.util.JsonUtil.*;

@RestController
@RequestMapping("/bookTag")
public class BookTagController extends LibraryController<BookTag> {

    BookTagController(BookTagRepository repository) {
        this.repository = repository;
    }

    @PutMapping(value = "/insert", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void insertBook(@RequestBody Map<String, String> json) {
        Book book = bookRepository.getReferenceById(getLongFromJson("bookId", json));
        Tag tag = tagRepository.getReferenceById(getLongFromJson("tagId", json));
        BookTag booktag = new BookTag();
        booktag.setBook(book);
        booktag.setTag(tag);
        repository.save(booktag);
    }

    @GetMapping(value = "/getByBook", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String get(@RequestParam Long bookId) {
        try {
            return mapper.writeValueAsString(((BookTagRepository) repository).findByBookId(bookId));
        } catch (JsonProcessingException e) {
            return "Error processing json";
        }
    }
}