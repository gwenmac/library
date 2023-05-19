package library.controllers;

import library.entities.Series;
import library.repositories.BookRepository;
import library.repositories.SeriesRepository;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static library.util.JsonUtil.getBoolFromJson;
import static library.util.JsonUtil.getIntFromJson;

@RestController
@RequestMapping("/series")
public class SeriesController extends LibraryController<Series> {

    SeriesController(SeriesRepository repository) {
        this.repository = repository;
    }

    @DeleteMapping("/delete")
    public void delete(@RequestParam List<Long> ids) {
        if (ids != null) {
            //todo: Delete all books with this series id
        }
        super.delete(ids);
    }

    @PutMapping(value = "/insert", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void insertSeries(@RequestBody Map<String, String> json) {
        seriesRepository.insertSeries(
                json.getOrDefault("title", "not given"),
                json.getOrDefault("englishSortTitle", "not given"),
                getBoolFromJson("ongoing", json),
                getIntFromJson("availableCount", json),
                getBoolFromJson("readAllOwned", json),
                getBoolFromJson("ownAll", json),
                getBoolFromJson("finished", json)
        );
    }
}