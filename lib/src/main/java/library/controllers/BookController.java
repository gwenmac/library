package library.controllers;

import library.entities.Book;
import library.repositories.BookRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
}
