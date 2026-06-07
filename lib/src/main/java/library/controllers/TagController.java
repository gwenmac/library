package library.controllers;

import library.entities.Household;
import library.entities.Tag;
import library.repositories.TagsRepository;
import library.security.CurrentUser;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class TagController {
    private final TagsRepository tagsRepository;

    public TagController(TagsRepository tagsRepository) {
        this.tagsRepository = tagsRepository;
    }

    @GetMapping("/tags/all")
    public List<Tag> getAllTags() {
        return tagsRepository.findAllByHouseholdId(CurrentUser.householdId());
    }

    @PostMapping("/tags")
    @ResponseStatus(HttpStatus.CREATED)
    public Tag createTag(@RequestBody Map<String, String> body) {
        Tag tag = new Tag();
        tag.setName(body.get("name"));
        Household household = CurrentUser.get().getHousehold();
        tag.setHousehold(household);
        return tagsRepository.save(tag);
    }
}
