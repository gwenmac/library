package library.controllers;

import library.entities.Gauge;
import library.entities.GaugeEntry;
import library.repositories.GaugeEntryRepository;
import library.repositories.GaugeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/gauges")
public class GaugeController {

    private final GaugeRepository gaugeRepository;
    private final GaugeEntryRepository gaugeEntryRepository;

    public GaugeController(GaugeRepository gaugeRepository,
                           GaugeEntryRepository gaugeEntryRepository) {
        this.gaugeRepository = gaugeRepository;
        this.gaugeEntryRepository = gaugeEntryRepository;
    }

    // ── Gauge CRUD ──────────────────────────────────────────

    @GetMapping
    public List<Gauge> getAll() {
        return gaugeRepository.findAllWithEntries();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Gauge create(@RequestBody Map<String, Object> body) {
        Gauge gauge = new Gauge();
        gauge.setName((String) body.get("name"));
        gauge.setDescription((String) body.get("description"));
        gauge.setCreatedAt(LocalDateTime.now());
        return gaugeRepository.save(gauge);
    }

    @GetMapping("/{id}")
    public Gauge getById(@PathVariable Long id) {
        return gaugeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Gauge not found"));
    }

    @PutMapping("/{id}")
    public Gauge update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Gauge gauge = gaugeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Gauge not found"));
        if (body.containsKey("name")) {
            gauge.setName((String) body.get("name"));
        }
        if (body.containsKey("description")) {
            gauge.setDescription((String) body.get("description"));
        }
        return gaugeRepository.save(gauge);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        Gauge gauge = gaugeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Gauge not found"));
        gaugeRepository.delete(gauge);
    }

    // ── Entries ─────────────────────────────────────────────

    @GetMapping("/{id}/entries")
    public List<GaugeEntry> getEntries(@PathVariable Long id) {
        if (!gaugeRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Gauge not found");
        }
        return gaugeEntryRepository.findByGaugeIdOrderByCreatedAtAsc(id);
    }

    @Transactional
    @PostMapping("/{id}/entries")
    @ResponseStatus(HttpStatus.CREATED)
    public GaugeEntry addEntry(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Gauge gauge = gaugeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Gauge not found"));

        GaugeEntry entry = new GaugeEntry();
        entry.setGauge(gauge);
        entry.setDelta(((Number) body.get("delta")).intValue());
        entry.setNote((String) body.get("note"));
        entry.setCreatedAt(LocalDateTime.now());
        return gaugeEntryRepository.save(entry);
    }

    @DeleteMapping("/{id}/entries/{entryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEntry(@PathVariable Long id, @PathVariable Long entryId) {
        GaugeEntry entry = gaugeEntryRepository.findById(entryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Entry not found"));
        if (!entry.getGauge().getId().equals(id)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Entry does not belong to this gauge");
        }
        gaugeEntryRepository.delete(entry);
    }
}
