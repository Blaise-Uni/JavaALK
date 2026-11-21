package hu.beadando.crud;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;

/**
 * A CRUD alkalmazás táblája. Ha másik táblát választasz a kiadott adatbázisból,
 * itt cseréld a mezőket (és a frontend/src/App.jsx FIELDS tömbjét).
 */
@Entity
public class Vevo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "A név megadása kötelező.")
    private String nev;
    private String email;
    private String varos;
    private String telefon;

    public Vevo() {}

    public Vevo(String nev, String email, String varos, String telefon) {
        this.nev = nev;
        this.email = email;
        this.varos = varos;
        this.telefon = telefon;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNev() { return nev; }
    public void setNev(String nev) { this.nev = nev; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getVaros() { return varos; }
    public void setVaros(String varos) { this.varos = varos; }
    public String getTelefon() { return telefon; }
    public void setTelefon(String telefon) { this.telefon = telefon; }
}
