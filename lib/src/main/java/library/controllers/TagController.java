package library.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import library.entities.Tag;
import library.repositories.TagRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tag")
public class TagController extends LibraryController<Tag> {

    TagController(TagRepository repository) {
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
            return mapper.writeValueAsString(((TagRepository) repository).findByName(name));
        } catch (JsonProcessingException e) {
            return "Error processing json";
        }
    }
}