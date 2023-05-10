package library.controllers;

import library.entities.Book;
import library.repositories.BookRepository;
import org.springframework.http.MediaType;
import org.springframework.util.NumberUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/book")
public class BookController extends LibraryController<Book> {

    BookController(BookRepository repository) {
        this.repository = repository;
    }

    @DeleteMapping("/delete")
    public void delete(@RequestParam List<Long> ids) {
        if (ids != null) {
            //todo: Delete all bookTags where book_id = id
        }
        super.delete(ids);
    }

    @PutMapping(value = "/insert", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void insertBook(@RequestBody Map<String, String> jsonBody) {
        // todo verify the fields are correctly filled out before saving
        System.out.println(jsonBody);
        ((BookRepository) repository).insertBook(
                jsonBody.getOrDefault("title", "title not given"),
                jsonBody.containsKey("series") ? Integer.valueOf(jsonBody.get("series")) : null,
                jsonBody.containsKey("volNum") ? Integer.valueOf(jsonBody.get("series")) : null,
                jsonBody.containsKey("language") ? Integer.valueOf(jsonBody.get("language")) : null,
                jsonBody.containsKey("furigana") ? Boolean.valueOf(jsonBody.get("furigana")) : null,
                jsonBody.containsKey("lnLevel") ? Integer.valueOf(jsonBody.get("lnLevel")) : null,
                jsonBody.getOrDefault("englishSortName", null),
                jsonBody.containsKey("status") ? Integer.valueOf(jsonBody.get("status")) : null,
                null,
                null
        );
    }
}
