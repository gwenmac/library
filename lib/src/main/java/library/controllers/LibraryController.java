package library.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public abstract class LibraryController<T> {

    protected JpaRepository repository;
    ObjectMapper mapper = new ObjectMapper();

    @GetMapping(value = "/get", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String get(@RequestParam(required = false) List<Long> ids) {
        try {
            if (ids == null) return mapper.writeValueAsString(Lists.newArrayList(repository.findAll()));
            else {
                Iterable entityList = repository.findAllById(ids);
                return mapper.writeValueAsString(entityList);
            }
        } catch (JsonProcessingException e) {
            return "Error processing json";
        }
    }

    @DeleteMapping("/delete")
    public void delete(@RequestParam List<Long> ids) {
        if (ids != null) {
            repository.deleteAllById(ids);
        }
    }

    @PutMapping(value = "/upsert", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void save(@RequestBody T t) {
        repository.save(t);
    }
}