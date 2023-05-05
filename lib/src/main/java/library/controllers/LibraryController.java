package library.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import library.entities.Book;
import library.entities.Tag;
import library.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
public class LibraryController {
    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private BookTagRepository bookTagRepository;

    @Autowired
    private LanguageRepository languageRepository;

    @Autowired
    private SeriesRepository seriesRepository;

    @Autowired
    private StatusRepository statusRepository;

    ObjectMapper mapper = new ObjectMapper();

    @GetMapping("/")
    public String hello() {
        return "Hello!";
    }

    @GetMapping(value = "/books", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String getBooks(@RequestParam(required = false) Long id) { //todo make id multi parameter?
        try {
            if (id == null) return mapper.writeValueAsString(bookRepository.findAll());
            else {
                Optional<Book> book = bookRepository.findById(id);
                List<Book> bookList = new ArrayList<>();
                if (book.isPresent()) bookList.add(book.get());
                return mapper.writeValueAsString(bookList);
            }
        } catch (JsonProcessingException e) {
            return "Error processing json";
        }
    }

    @PostMapping("/books")
    public String addBook() {
        return "todo";
    }

    @GetMapping(value = "/tags", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String getTags(@RequestParam(required = false) Long id) { //todo make id multi parameter?
        try {
            if (id == null) return mapper.writeValueAsString(tagRepository.findAll());
            else {
                Optional<Tag> book = tagRepository.findById(id);
                List<Tag> tagList = new ArrayList<>();
                if (book.isPresent()) tagList.add(book.get());
                return mapper.writeValueAsString(tagList);
            }
        } catch (JsonProcessingException e) {
            return "Error processing json";
        }
    }

    @PostMapping("/tags")
    public String addTag(@ModelAttribute String name) {
        Tag tag = new Tag();
        tag.setName(name);
        tagRepository.save(tag);
        return "done";
    }

    @GetMapping("/deleteTag")
    public void deleteTag(@RequestParam Long id) {
        tagRepository.deleteById(id);
    }
}