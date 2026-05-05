package library.controllers;

import library.entities.Language;
import library.repositories.LanguageRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class LanguageController {
    private final LanguageRepository languageRepository;

    public LanguageController(LanguageRepository languageRepository) {
        this.languageRepository = languageRepository;
    }

    @GetMapping("/languages/all")
    public List<Language> getAllLanguages() {
        return languageRepository.findAll();
    }

    @PostMapping("/languages")
    @ResponseStatus(HttpStatus.CREATED)
    public Language createLanguage(@RequestBody Map<String, String> body) {
        Language language = new Language();
        language.setName(body.get("name"));
        return languageRepository.save(language);
    }
}
