package library.controllers;

import library.entities.Series;
import library.repositories.SeriesRepository;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/series")
public class SeriesController extends LibraryController<Series> {

    SeriesController(SeriesRepository repository) {
        this.repository = repository;
    }

    @GetMapping(value = "findSeries", produces = MediaType.APPLICATION_JSON_VALUE)
    public String findSeries() {
        return "todo";
    }
}