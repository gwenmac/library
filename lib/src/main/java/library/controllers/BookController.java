package library.controllers;

import library.entities.Book;
import library.repositories.BookRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/book")
public class BookController extends LibraryController<Book> {

    BookController(BookRepository repository) {
        this.repository = repository;
    }

    @PostMapping("/find")
    public String findBooks() {
        // search based on multiple optional search parameters
        return "todo";
    }
}
