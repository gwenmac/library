package library.controllers;

import library.entities.Book;
import library.repositories.BookRepository;
import org.springframework.http.MediaType;
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
    public void insertBook(@RequestBody Map<String, String> json) {
        System.out.println(json);
        ((BookRepository) repository).insertBook(
                json.getOrDefault("title", "not given"),
                getIntFromJson("series", json),
                getIntFromJson("volNum", json),
                getIntFromJson("language", json),
                getBoolFromJson("forigana", json),
                getIntFromJson("lnLevel", json),
                json.getOrDefault("englishSortName", "not given"),
                getIntFromJson("status", json),
                null,
                null
        );
    }

    private Integer getIntFromJson(String key, Map<String, String> json) {
        if (json.containsKey(key)) {
            return json.get(key) == null ? null : Integer.valueOf(json.get(key));
        } else {
            return null;
        }
    }

    private Boolean getBoolFromJson(String key, Map<String, String> json) {
        if (json.containsKey(key)) {
            return json.get(key) == null ? null : Boolean.valueOf(json.get(key));
        } else {
            return null;
        }
    }
}
