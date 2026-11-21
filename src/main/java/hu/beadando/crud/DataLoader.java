package hu.beadando.crud;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/** Mintaadatok, ha az adatbázis tábla üres (app.seed=true). */
@Configuration
public class DataLoader {

    @Bean
    @ConditionalOnProperty(name = "app.seed", havingValue = "true")
    CommandLineRunner seed(VevoRepository repo) {
        return args -> {
            if (repo.count() == 0) {
                repo.saveAll(List.of(
                        new Vevo("Kovács Ferenc", "kovacs.ferenc@example.com", "Szarvas", "+36 30 111 2222"),
                        new Vevo("Nagy Anna", "nagy.anna@example.com", "Békéscsaba", "+36 20 333 4444"),
                        new Vevo("Tóth Béla", "toth.bela@example.com", "Gyula", "+36 70 555 6666")));
            }
        };
    }
}
