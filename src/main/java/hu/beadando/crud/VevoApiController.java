package hu.beadando.crud;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/** REST API a React kliensnek (Axios): /api/vevok */
@RestController
@RequestMapping("/api/vevok")
public class VevoApiController {

    private final VevoRepository repo;

    public VevoApiController(VevoRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<Vevo> list() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public Vevo get(@PathVariable Long id) {
        return repo.findById(id).orElseThrow(() -> notFound(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Vevo create(@Valid @RequestBody Vevo v) {
        v.setId(null);
        return repo.save(v);
    }

    @PutMapping("/{id}")
    public Vevo update(@PathVariable Long id, @Valid @RequestBody Vevo v) {
        Vevo existing = repo.findById(id).orElseThrow(() -> notFound(id));
        existing.setNev(v.getNev());
        existing.setEmail(v.getEmail());
        existing.setVaros(v.getVaros());
        existing.setTelefon(v.getTelefon());
        return repo.save(existing);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        if (!repo.existsById(id)) {
            throw notFound(id);
        }
        repo.deleteById(id);
    }

    private static ResponseStatusException notFound(Long id) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Nincs ilyen rekord: " + id);
    }
}
