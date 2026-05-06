package library.controllers;

import library.entities.Series;
import library.repositories.SeriesRepository;
import library.security.CurrentUser;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class SeriesController {

    private final SeriesRepository seriesRepository;

    public SeriesController(SeriesRepository seriesRepository) {
        this.seriesRepository = seriesRepository;
    }

    @GetMapping("/series/all")
    public List<Series> getAll() {
        return seriesRepository.findAllByUserId(CurrentUser.id());
    }

    @PostMapping("/series")
    @ResponseStatus(HttpStatus.CREATED)
    public Series create(@RequestBody Map<String, String> body) {
        Series series = new Series();
        series.setName(body.get("name"));
        series.setUser(CurrentUser.get());
        return seriesRepository.save(series);
    }
}
