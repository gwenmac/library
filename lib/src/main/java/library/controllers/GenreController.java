package library.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import library.entities.Genre;
import library.repositories.GenreRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/genre")
public class GenreController extends LibraryController<Genre> {

    GenreController(GenreRepository repository) {
        this.repository = repository;
    }

    @DeleteMapping("/delete")
    public void delete(@RequestParam List<Long> ids) {
        if (ids != null) {
            //todo: Delete all BookTags where tag_id = id
        }
        super.delete(ids);
    }

    @GetMapping(value = "search")
    public String search(@RequestParam String name) {
        try {
            return mapper.writeValueAsString(((GenreRepository) repository).findByName(name));
        } catch (JsonProcessingException e) {
            return "Error processing json";
        }
    }
}