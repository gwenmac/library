package library.controllers;

import library.entities.Status;
import library.repositories.StatusRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/status")
public class StatusController extends LibraryController<Status> {

    StatusController(StatusRepository repository) {
        this.repository = repository;
    }

    @DeleteMapping("/delete")
    public void delete(@RequestParam List<Long> ids) {
        //todo: only delete if status is not in use
        super.delete(ids);
    }
}