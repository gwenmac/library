package library.controllers;

import library.entities.Series;
import library.repositories.SeriesRepository;
import library.security.CurrentUser;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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
        return seriesRepository.findAllByHouseholdId(CurrentUser.householdId());
    }

    @PostMapping("/series")
    @ResponseStatus(HttpStatus.CREATED)
    public Series create(@RequestBody Map<String, String> body) {
        Series series = new Series();
        series.setName(body.get("name"));
        series.setHousehold(CurrentUser.get().getHousehold());
        return seriesRepository.save(series);
    }

    @DeleteMapping("/series/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        Series series = seriesRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Series not found"));
        if (!series.getHousehold().getId().equals(CurrentUser.householdId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        seriesRepository.delete(series);
    }
}
