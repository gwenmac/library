package library.controllers;

import library.entities.Book;
import library.entities.Language;
import library.entities.Series;
import library.repositories.BookRepository;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static library.util.JsonUtil.*;

@RestController
@RequestMapping("/book")
public class BookController extends LibraryController<Book> {

    BookController(BookRepository repository) {
        this.repository = repository;
    }

    @DeleteMapping("/delete")
    public void delete(@RequestParam List<Long> ids) {
        if (ids != null) {
            //todo: Delete all bookTags where book_id = id
        }
        super.delete(ids);
    }

    @PutMapping(value = "/insert", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void insertBook(@RequestBody Map<String, String> json) {
        bookRepository.insertBook(
                json.getOrDefault("title", "not given"),
                json.getOrDefault("englishSortTitle", "not given"),
                getIntFromJson("series", json),
                getIntFromJson("volNum", json),
                getIntFromJson("language", json),
                getBoolFromJson("furigana", json),
                getIntFromJson("lnLevel", json),
                getIntFromJson("status", json)
        );
    }

    @PutMapping(value = "/bulkAdd", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void bulkAdd(@RequestBody Map<String, String> json) {
        List<String> unreadVols = Arrays.stream(json.getOrDefault("unreadVols", "").split(",")).filter(x -> !x.isEmpty()).collect(Collectors.toList());
        List<String> inProgressVols = Arrays.stream(json.getOrDefault("inProgressVols", "").split(",")).filter(x -> !x.isEmpty()).collect(Collectors.toList());
        List<String> readVols = Arrays.stream(json.getOrDefault("readVols", "").split(",")).filter(x -> !x.isEmpty()).collect(Collectors.toList());

        String title = json.getOrDefault("title", "not given");
        String englishSortTitle = json.getOrDefault("englishSortTitle", "not given");
        Boolean ongoing = getBoolFromJson("ongoing", json);
        Integer availableCount = getIntFromJson("availableCount", json);
        Boolean readAllOwned = unreadVols.size() == 0;
        Boolean ownAll = availableCount == (readVols.size() + inProgressVols.size() + unreadVols.size());
        Boolean finished = availableCount == readVols.size();

        Series series = new Series();
        series.setTitle(title);
        series.setEnglishSortTitle(englishSortTitle);
        series.setOngoing(ongoing);
        series.setAvailableCount(availableCount);
        series.setReadAllOwned(readAllOwned);
        series.setOwnAll(ownAll);
        series.setFinished(finished);
        seriesRepository.save(series);

        Long languageId = getLongFromJson("language", json);
        Language language = languageRepository.getReferenceById(languageId);
        Boolean furigana = getBoolFromJson("furigana", json);
        Integer lnLevel = getIntFromJson("lnLevel", json);

        unreadVols.forEach(volNum -> {
            Book book = new Book();
            book.setTitle(title);
            book.setEnglishSortTitle(englishSortTitle);
            book.setSeries(series);
            book.setVolNum(Integer.valueOf(volNum));
            book.setLanguage(language);
            book.setFurigana(furigana);
            book.setLnLevel(lnLevel);
            book.setStatus(statusRepository.getReferenceById(1L));
            bookRepository.save(book);
        });
        inProgressVols.forEach(volNum -> {
            Book book = new Book();
            book.setTitle(title);
            book.setEnglishSortTitle(englishSortTitle);
            book.setSeries(series);
            book.setVolNum(Integer.valueOf(volNum));
            book.setLanguage(language);
            book.setFurigana(furigana);
            book.setLnLevel(lnLevel);
            book.setStatus(statusRepository.getReferenceById(2L));
            bookRepository.save(book);
        });
        readVols.forEach(volNum -> {
            Book book = new Book();
            book.setTitle(title);
            book.setEnglishSortTitle(englishSortTitle);
            book.setSeries(series);
            book.setVolNum(Integer.valueOf(volNum));
            book.setLanguage(language);
            book.setFurigana(furigana);
            book.setLnLevel(lnLevel);
            book.setStatus(statusRepository.getReferenceById(3L));
            bookRepository.save(book);
        });
    }
}