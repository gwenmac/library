package library.controllers;

import library.entities.Tag;
import library.repositories.TagRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tag")
public class TagController extends LibraryController<Tag> {

    TagController(TagRepository repository) {
        this.repository = repository;
    }
}