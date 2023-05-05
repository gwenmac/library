package library.controllers;

import library.entities.BookTag;
import library.repositories.BookTagRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bookTag")
public class BookTagController extends LibraryController<BookTag> {

    BookTagController(BookTagRepository repository) {
        this.repository = repository;
    }
}