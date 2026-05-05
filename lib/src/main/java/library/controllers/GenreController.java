package library.controllers;

import library.entities.Genre;
import library.repositories.GenreRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class GenreController {

    private final GenreRepository genreRepository;

    public GenreController(GenreRepository genreRepository) {
        this.genreRepository = genreRepository;
    }

    @GetMapping("/genres/all")
    public List<Genre> getAllGenres() {
        return genreRepository.findAll();
    }

    @PostMapping("/genres")
    @ResponseStatus(HttpStatus.CREATED)
    public Genre createGenre(@RequestBody Map<String, String> body) {
        Genre genre = new Genre();
        genre.setName(body.get("name"));
        return genreRepository.save(genre);
    }
}
