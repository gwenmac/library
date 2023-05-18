package library.controllers;

import library.entities.Book;
import library.repositories.BookRepository;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static library.util.JsonUtil.getBoolFromJson;
import static library.util.JsonUtil.getIntFromJson;

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
        ((BookRepository) repository).insertBook(
                json.getOrDefault("title", "not given"),
                json.getOrDefault("englishSortTitle", "not given"),
                getIntFromJson("series", json),
                getIntFromJson("volNum", json),
                getIntFromJson("language", json),
                getBoolFromJson("forigana", json),
                getIntFromJson("lnLevel", json),
                getIntFromJson("status", json)
        );
    }

    @PutMapping(value = "/bulkadd", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void bulkInsertBook(@RequestBody Map<String, String> json) {
        System.out.println(json);
    }
}
