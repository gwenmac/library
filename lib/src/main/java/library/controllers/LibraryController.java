package library.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LibraryController {
    @GetMapping("/")
    public String hello() {
        return String.format("Hello!");
    }
}
