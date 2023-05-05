package library.controllers;

import library.entities.Language;
import library.repositories.LanguageRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/language")
public class LanguageController extends LibraryController<Language> {

    LanguageController(LanguageRepository repository) {
        this.repository = repository;
    }
}