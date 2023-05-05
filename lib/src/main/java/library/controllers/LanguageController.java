package library.controllers;

import library.entities.Language;
import library.repositories.LanguageRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/language")
public class LanguageController extends LibraryController<Language> {

    LanguageController(LanguageRepository repository) {
        this.repository = repository;
    }

    @DeleteMapping("/delete")
    public void delete(@RequestParam List<Long> ids) {
        //todo: only delete if language is not in use
        super.delete(ids);
    }
}