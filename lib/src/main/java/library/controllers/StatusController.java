package library.controllers;

import library.entities.Status;
import library.repositories.StatusRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/status")
public class StatusController extends LibraryController<Status> {

    StatusController(StatusRepository repository) {
        this.repository = repository;
    }
}