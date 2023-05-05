package library.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import library.entities.Tag;
import library.repositories.TagRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tag")
public class TagController extends LibraryController<Tag> {

    TagController(TagRepository repository) {
        this.repository = repository;
    }

    @GetMapping(value = "search")
    public String search(@RequestParam String name) {
        try {
            return mapper.writeValueAsString(((TagRepository) repository).findByName(name));
        } catch (JsonProcessingException e) {
            return "Error processing json";
        }
    }
}