package library.controllers;

import library.entities.Edition;
import library.repositories.EditionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class EditionController {

    private final EditionRepository editionRepository;

    public EditionController(EditionRepository editionRepository) {
        this.editionRepository = editionRepository;
    }

    @GetMapping("/editions/all")
    public List<Edition> getAllEditions() {
        return editionRepository.findAll();
    }

    @PostMapping("/editions")
    @ResponseStatus(HttpStatus.CREATED)
    public Edition createEdition(@RequestBody Map<String, String> body) {
        Edition edition = new Edition();
        edition.setName(body.get("name"));
        return editionRepository.save(edition);
    }
}
