package library.controllers;

import library.entities.Author;
import library.repositories.AuthorRepository;
import library.security.CurrentUser;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class AuthorController {

    private final AuthorRepository authorRepository;

    public AuthorController(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }

    @GetMapping("/authors/all")
    public List<Author> getAllAuthors() {
        return authorRepository.findAllByHouseholdId(CurrentUser.householdId());
    }

    @PostMapping("/authors")
    @ResponseStatus(HttpStatus.CREATED)
    public Author createAuthor(@RequestBody Map<String, String> body) {
        Author author = new Author();
        author.setFirstName(body.get("firstName"));
        author.setLastName(body.get("lastName"));
        author.setHousehold(CurrentUser.get().getHousehold());
        return authorRepository.save(author);
    }
}
