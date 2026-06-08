package library.controllers;

import library.entities.*;
import library.repositories.*;
import library.security.CurrentUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import jakarta.persistence.criteria.*;
import java.time.LocalDateTime;
import java.util.*;

@RestController
public class BookController {

    private final BookRepository bookRepository;
    private final SeriesRepository seriesRepository;
    private final AuthorRepository authorRepository;
    private final GenreRepository genreRepository;
    private final LanguageRepository languageRepository;
    private final EditionRepository editionRepository;
    private final StatusRepository statusRepository;
    private final ReviewsRepository reviewsRepository;
    private final TagsRepository tagsRepository;
    private final BookStatusRepository bookStatusRepository;

    public BookController(
            BookRepository bookRepository,
            SeriesRepository seriesRepository,
            AuthorRepository authorRepository,
            GenreRepository genreRepository,
            LanguageRepository languageRepository,
            EditionRepository editionRepository,
            StatusRepository statusRepository,
            ReviewsRepository reviewsRepository,
            TagsRepository tagsRepository,
            BookStatusRepository bookStatusRepository) {
        this.bookRepository = bookRepository;
        this.seriesRepository = seriesRepository;
        this.authorRepository = authorRepository;
        this.genreRepository = genreRepository;
        this.languageRepository = languageRepository;
        this.editionRepository = editionRepository;
        this.statusRepository = statusRepository;
        this.reviewsRepository = reviewsRepository;
        this.tagsRepository = tagsRepository;
        this.bookStatusRepository = bookStatusRepository;
    }

    @GetMapping("/statuses/all")
    public List<Status> getAllStatuses() {
        return statusRepository.findAll();
    }

    @GetMapping("/books/status-counts")
    public List<Map<String, Object>> getStatusCounts() {
        Long userId = CurrentUser.id();
        Long householdId = CurrentUser.householdId();
        long totalBooks = bookRepository.countByHouseholdId(householdId);

        List<BookStatus> userStatuses = bookStatusRepository.findAllByUserId(userId);
        Map<String, Long> counts = new HashMap<>();
        long assignedCount = 0;
        for (BookStatus bs : userStatuses) {
            if (bs.getBook().getHousehold().getId().equals(householdId)) {
                String name = bs.getStatus().getName();
                counts.merge(name, 1L, Long::sum);
                assignedCount++;
            }
        }

        long noStatusCount = totalBooks - assignedCount;

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, Long> entry : counts.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("status", entry.getKey());
            item.put("count", entry.getValue());
            result.add(item);
        }
        if (noStatusCount > 0) {
            Map<String, Object> item = new HashMap<>();
            item.put("status", "No Status");
            item.put("count", noStatusCount);
            result.add(item);
        }

        return result;
    }

    @GetMapping("/all")
    public List<Book> getAll() {
        return bookRepository.findAllByHouseholdIdOrderBySortTitleAsc(CurrentUser.householdId());
    }

    @GetMapping("/books/page")
    public Map<String, Object> getPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "title") String sort,
            @RequestParam(defaultValue = "asc") String dir,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String genre) {

        Long householdId = CurrentUser.householdId();
        Long userId = CurrentUser.id();

        Specification<Book> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("household").get("id"), householdId));

            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.toLowerCase() + "%";
                Join<Book, Author> authorJoin = root.join("authors", JoinType.LEFT);
                Join<Book, Series> seriesJoin = root.join("series", JoinType.LEFT);
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), pattern),
                        cb.like(cb.lower(authorJoin.get("firstName")), pattern),
                        cb.like(cb.lower(authorJoin.get("lastName")), pattern),
                        cb.like(cb.lower(seriesJoin.get("name")), pattern)
                ));
                query.distinct(true);
            }

            if (genre != null && !genre.isBlank()) {
                if (genre.equals("__none__")) {
                    predicates.add(cb.isEmpty(root.get("genres")));
                } else {
                    Join<Book, Genre> genreJoin = root.join("genres", JoinType.INNER);
                    predicates.add(cb.equal(genreJoin.get("name"), genre));
                    query.distinct(true);
                }
            }

            if (status != null && !status.isBlank()) {
                Subquery<Long> statusSub = query.subquery(Long.class);
                Root<BookStatus> bsRoot = statusSub.from(BookStatus.class);
                statusSub.select(bsRoot.get("book").get("id"));
                statusSub.where(
                        cb.equal(bsRoot.get("user").get("id"), userId)
                );
                if (status.equals("__none__")) {
                    predicates.add(cb.not(root.get("id").in(statusSub)));
                } else {
                    statusSub.where(
                            cb.equal(bsRoot.get("user").get("id"), userId),
                            cb.equal(bsRoot.get("status").get("name"), status)
                    );
                    predicates.add(root.get("id").in(statusSub));
                }
            }

            // Apply ordering for join-based sorts (only on data queries, not count queries)
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                if (sort.equals("author")) {
                    Subquery<String> authorSub = query.subquery(String.class);
                    Root<Book> subRoot = authorSub.correlate(root);
                    Join<Book, Author> subAuthorJoin = subRoot.join("authors", JoinType.LEFT);
                    if ("asc".equals(dir)) {
                        authorSub.select(cb.least(subAuthorJoin.<String>get("lastName")));
                        query.orderBy(
                                cb.asc(authorSub),
                                cb.asc(root.get("sortTitle"))
                        );
                    } else {
                        authorSub.select(cb.greatest(subAuthorJoin.<String>get("lastName")));
                        query.orderBy(
                                cb.desc(authorSub),
                                cb.desc(root.get("sortTitle"))
                        );
                    }
                } else if (sort.equals("series")) {
                    Subquery<String> seriesSub = query.subquery(String.class);
                    Root<Book> seriesSubRoot = seriesSub.correlate(root);
                    Join<Book, Series> subSeriesJoin = seriesSubRoot.join("series", JoinType.LEFT);
                    seriesSub.select(subSeriesJoin.get("name"));
                    if ("asc".equals(dir)) {
                        query.orderBy(
                                cb.asc(seriesSub),
                                cb.asc(root.get("seriesOrder")),
                                cb.asc(root.get("sortTitle"))
                        );
                    } else {
                        query.orderBy(
                                cb.desc(seriesSub),
                                cb.desc(root.get("seriesOrder")),
                                cb.desc(root.get("sortTitle"))
                        );
                    }
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Sort sortOrder;
        switch (sort) {
            case "author":
            case "series":
                sortOrder = Sort.unsorted();
                break;
            default:
                sortOrder = Sort.by("asc".equals(dir) ? Sort.Direction.ASC : Sort.Direction.DESC, "sortTitle");
        }

        Pageable pageable = PageRequest.of(page, size, sortOrder);
        Page<Book> bookPage = bookRepository.findAll(spec, pageable);

        // Batch-load statuses for all books on this page
        List<Long> bookIds = bookPage.getContent().stream().map(Book::getId).toList();
        List<BookStatus> userStatuses = bookStatusRepository.findAllByUserId(userId);
        Map<Long, BookStatus> statusMap = new HashMap<>();
        for (BookStatus bs : userStatuses) {
            if (bookIds.contains(bs.getBook().getId())) {
                statusMap.put(bs.getBook().getId(), bs);
            }
        }

        List<Map<String, Object>> content = bookPage.getContent().stream().map(book -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", book.getId());
            row.put("title", book.getTitle());
            row.put("sortTitle", book.getSortTitle());
            row.put("authors", book.getAuthors());
            row.put("series", book.getSeries());
            row.put("seriesOrder", book.getSeriesOrder());
            row.put("genres", book.getGenres());
            row.put("edition", book.getEdition());
            row.put("languages", book.getLanguages());
            BookStatus bs = statusMap.get(book.getId());
            row.put("statusId", bs != null ? bs.getStatus().getId() : null);
            row.put("statusName", bs != null ? bs.getStatus().getName() : null);
            return row;
        }).toList();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("content", content);
        result.put("totalElements", bookPage.getTotalElements());
        result.put("totalPages", bookPage.getTotalPages());
        result.put("number", bookPage.getNumber());
        return result;
    }

    @Transactional
    @PostMapping("/books")
    @ResponseStatus(HttpStatus.CREATED)
    public Book create(@RequestBody Map<String, Object> body) {
        Book book = new Book();
        book.setHousehold(CurrentUser.get().getHousehold());
        book.setTitle((String) body.get("title"));
        book.setDescription((String) body.get("description"));
        if (body.get("pageCount") != null) {
            book.setPageCount(((Number) body.get("pageCount")).intValue());
        }
        if (body.get("year") != null) {
            book.setYear(Integer.parseInt(body.get("year").toString()));
        }
        if (body.get("seriesId") != null) {
            Long seriesId = ((Number) body.get("seriesId")).longValue();
            Series series = seriesRepository.findById(seriesId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Series not found"));
            book.setSeries(series);
        }
        if (body.get("seriesOrder") != null) {
            book.setSeriesOrder(((Number) body.get("seriesOrder")).intValue());
        }
        if (body.get("authorIds") != null) {
            book.setAuthors(resolveAuthorIds(body.get("authorIds")));
        }
        if (body.get("genreIds") != null) {
            book.setGenres(resolveGenreIds(body.get("genreIds")));
        }
        if (body.get("tagIds") != null) {
            book.setTags(resolveTagIds(body.get("tagIds")));
        }
        if (body.get("languageIds") != null) {
            book.setLanguages(resolveLanguageIds(body.get("languageIds")));
        }
        if (body.containsKey("editionId")) {
            if (body.get("editionId") != null) {
                Long editionId = ((Number) body.get("editionId")).longValue();
                Edition edition = editionRepository.findById(editionId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Edition not found"));
                book.setEdition(edition);
            } else {
                book.setEdition(null);
            }
        }
        book.setCreatedAt(LocalDateTime.now());
        book.setUpdatedAt(LocalDateTime.now());
        return bookRepository.save(book);
    }

    @GetMapping("/books/{id}")
    public Book getById(@PathVariable Long id) {
        return bookRepository.findByIdAndHouseholdId(id, CurrentUser.householdId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found"));
    }

    @Transactional
    @PutMapping("/books/{id}")
    public Book update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Book book = bookRepository.findByIdAndHouseholdId(id, CurrentUser.householdId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found"));

        if (body.containsKey("title")) {
            book.setTitle((String) body.get("title"));
        }
        if (body.containsKey("authorIds")) {
            book.getAuthors().clear();
            book.getAuthors().addAll(resolveAuthorIds(body.get("authorIds")));
        }
        if (body.containsKey("genreIds")) {
            book.getGenres().clear();
            book.getGenres().addAll(resolveGenreIds(body.get("genreIds")));
        }
        if (body.containsKey("tagIds")) {
            book.getTags().clear();
            book.getTags().addAll(resolveTagIds(body.get("tagIds")));
        }
        if (body.containsKey("languageIds")) {
            book.getLanguages().clear();
            book.getLanguages().addAll(resolveLanguageIds(body.get("languageIds")));
        }
        if (body.containsKey("editionId")) {
            if (body.get("editionId") != null) {
                Long editionId = ((Number) body.get("editionId")).longValue();
                Edition edition = editionRepository.findById(editionId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Edition not found"));
                book.setEdition(edition);
            } else {
                book.setEdition(null);
            }
        }
        if (body.containsKey("description")) {
            book.setDescription((String) body.get("description"));
        }
        if (body.containsKey("pageCount")) {
            book.setPageCount(body.get("pageCount") != null && body.get("pageCount") != "" ? ((Number) body.get("pageCount")).intValue() : null);
        }
        if (body.containsKey("year")) {
            book.setYear(body.get("year") != null ? ((Number) body.get("year")).intValue() : null);
        }
        if (body.containsKey("seriesId")) {
            if (body.get("seriesId") != null) {
                Long seriesId = ((Number) body.get("seriesId")).longValue();
                Series series = seriesRepository.findById(seriesId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Series not found"));
                book.setSeries(series);
            } else {
                book.setSeries(null);
            }
        }
        if (body.containsKey("seriesOrder")) {
            book.setSeriesOrder(body.get("seriesOrder") != null ? ((Number) body.get("seriesOrder")).intValue() : null);
        }

        book.setUpdatedAt(LocalDateTime.now());
        return bookRepository.save(book);
    }

    @GetMapping("/books/{id}/review")
    public Review getMyReview(@PathVariable Long id) {
        bookRepository.findByIdAndHouseholdId(id, CurrentUser.householdId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found"));
        return reviewsRepository.findByBookIdAndUserId(id, CurrentUser.id()).orElse(null);
    }

    @Transactional
    @PutMapping("/books/{id}/review")
    public Review updateReview(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Book book = bookRepository.findByIdAndHouseholdId(id, CurrentUser.householdId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found"));

        Number ratingNum = (Number) body.get("rating");
        String notes = (String) body.get("notes");

        if (ratingNum == null && (notes == null || notes.isBlank())) {
            reviewsRepository.findByBookIdAndUserId(id, CurrentUser.id())
                    .ifPresent(reviewsRepository::delete);
            return null;
        }

        Review review = reviewsRepository.findByBookIdAndUserId(id, CurrentUser.id())
                .orElseGet(() -> {
                    Review r = new Review();
                    r.setBook(book);
                    r.setUser(CurrentUser.get());
                    return r;
                });
        review.setRating(ratingNum != null ? ratingNum.shortValue() : null);
        review.setNotes(notes);
        return reviewsRepository.save(review);
    }

    @GetMapping("/books/{id}/status")
    public Status getMyStatus(@PathVariable Long id) {
        bookRepository.findByIdAndHouseholdId(id, CurrentUser.householdId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found"));
        BookStatus bookStatus = bookStatusRepository.findByBookIdAndUserId(id, CurrentUser.id()).orElse(null);
        if (bookStatus != null) {
            return bookStatus.getStatus();
        } else {
            return null;
        }
    }

    @Transactional
    @PutMapping("/books/{id}/status")
    public BookStatus updateStatus(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Book book = bookRepository.findByIdAndHouseholdId(id, CurrentUser.householdId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found"));

        Number statusId = (Number) body.get("statusId");

        if (statusId == null) {
            bookStatusRepository.findByBookIdAndUserId(id, CurrentUser.id())
                    .ifPresent(bookStatusRepository::delete);
            return null;
        }

        Status status = statusRepository.findById(statusId.longValue())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status not found"));

        BookStatus bookStatus = bookStatusRepository.findByBookIdAndUserId(id, CurrentUser.get().getId())
                .orElseGet(() -> {
                        BookStatus bs = new BookStatus();
                        bs.setBook(book);
                        bs.setUser(CurrentUser.get());
                        return bs;
                    }
                );
        bookStatus.setStatus(status);
        bookStatus.setUpdatedAt(LocalDateTime.now());

        return bookStatusRepository.save(bookStatus);
    }

    @Transactional
    @DeleteMapping("/books/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        Book book = bookRepository.findByIdAndHouseholdId(id, CurrentUser.householdId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found"));
        List<BookStatus> bookStatuses = bookStatusRepository.findAllByBookId(book.getId());
        bookStatusRepository.deleteAll(bookStatuses);
        List<Review> reviews = reviewsRepository.findAllByBookId(book.getId());
        reviewsRepository.deleteAll(reviews);
        book.getAuthors().clear();
        book.getGenres().clear();
        book.getLanguages().clear();
        bookRepository.delete(book);
    }

    // ── Helpers ─────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private Set<Author> resolveAuthorIds(Object raw) {
        if (raw == null) return new HashSet<>();
        List<Long> ids = ((List<Number>) raw).stream()
                .map(Number::longValue)
                .toList();
        Set<Author> authors = new HashSet<>(authorRepository.findAllById(ids));
        if (authors.size() != ids.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "One or more author IDs not found");
        }
        return authors;
    }

    @SuppressWarnings("unchecked")
    private Set<Genre> resolveGenreIds(Object raw) {
        if (raw == null) return new HashSet<>();
        List<Long> ids = ((List<Number>) raw).stream()
                .map(Number::longValue)
                .toList();
        Set<Genre> genres = new HashSet<>(genreRepository.findAllById(ids));
        if (genres.size() != ids.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "One or more genre IDs not found");
        }
        return genres;
    }

    @SuppressWarnings("unchecked")
    private Set<Tag> resolveTagIds(Object raw) {
        if (raw == null) return new HashSet<>();
        Long householdId = CurrentUser.householdId();
        List<Long> ids = ((List<Number>) raw).stream()
                .map(Number::longValue)
                .toList();
        Set<Tag> tags = new HashSet<>();
        for (Long id : ids) {
            tags.add(tagsRepository.findByIdAndHouseholdId(id, householdId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "One or more tag IDs not found")));
        }
        return tags;
    }

    @SuppressWarnings("unchecked")
    private Set<Language> resolveLanguageIds(Object raw) {
        if (raw == null) return new HashSet<>();
        List<Long> ids = ((List<Number>) raw).stream()
                .map(Number::longValue)
                .toList();
        Set<Language> languages = new HashSet<>(languageRepository.findAllById(ids));
        if (languages.size() != ids.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "One or more language IDs not found");
        }
        return languages;
    }
}