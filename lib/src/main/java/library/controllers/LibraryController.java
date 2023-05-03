package library.controllers;

import library.repositories.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LibraryController {
    @Autowired
    private BookRepository bookRepository;

    @GetMapping("/")
    public String hello() {
        return String.format("Hello!");
    }

    @GetMapping("/getBooks")
    public String getBooks() {
        return bookRepository.findAll().get(0).getTitle();
    }
}