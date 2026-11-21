package hu.beadando.controller;

import hu.beadando.model.Views.Rate;
import hu.beadando.service.MnbService;
import hu.beadando.service.OandaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Controller
public class MainController {

    private final MnbService mnb;
    private final OandaService oanda;

    public MainController(MnbService mnb, OandaService oanda) {
        this.mnb = mnb;
        this.oanda = oanda;
    }

    // ------------------------------------------------------------------ Főoldal
    @GetMapping("/")
    public String index() {
        return "index";
    }

    // --------------------------------------------------------------------- SOAP
    @GetMapping("/soap")
    public String soap(@RequestParam(required = false) String currency,
                       @RequestParam(required = false) String from,
                       @RequestParam(required = false) String to,
                       Model model) {
        String f = (from == null || from.isBlank()) ? LocalDate.now().minusDays(30).toString() : from;
        String t = (to == null || to.isBlank()) ? LocalDate.now().toString() : to;
        model.addAttribute("from", f);
        model.addAttribute("to", t);
        model.addAttribute("currency", currency);
        try {
            model.addAttribute("currencies", mnb.currencies());
            if (currency != null && !currency.isBlank()) {
                List<Rate> rates = mnb.rates(currency, f, t);
                model.addAttribute("rates", rates);
                model.addAttribute("labels", rates.stream().map(Rate::date).toList());
                model.addAttribute("values", rates.stream().map(Rate::value).toList());
            }
        } catch (Exception e) {
            model.addAttribute("error", "SOAP hiba: " + e.getMessage());
        }
        return "soap";
    }

    // ------------------------------------------------------------ Forex-account
    @GetMapping("/forex/account")
    public String account(Model model) {
        try {
            model.addAttribute("account", oanda.account());
        } catch (Exception e) {
            model.addAttribute("error", OandaService.message(e));
        }
        return "forex-account";
    }

    // -------------------------------------------------------------- Forex-AktÁr
    @GetMapping("/forex/price")
    public String price(@RequestParam(required = false) String instrument, Model model) {
        model.addAttribute("instruments", OandaService.INSTRUMENTS);
        model.addAttribute("selected", instrument);
        if (instrument != null && !instrument.isBlank()) {
            try {
                model.addAttribute("price", oanda.price(instrument));
            } catch (Exception e) {
                model.addAttribute("error", OandaService.message(e));
            }
        }
        return "forex-price";
    }

    // ------------------------------------------------------------- Forex-HistÁr
    @GetMapping("/forex/hist")
    public String hist(@RequestParam(required = false) String instrument,
                       @RequestParam(required = false) String granularity, Model model) {
        model.addAttribute("instruments", OandaService.INSTRUMENTS);
        model.addAttribute("granularities", OandaService.GRANULARITIES);
        model.addAttribute("selected", instrument);
        model.addAttribute("selectedGranularity", granularity == null ? "D" : granularity);
        if (instrument != null && !instrument.isBlank()) {
            try {
                model.addAttribute("candles", oanda.history(instrument, granularity == null ? "D" : granularity));
            } catch (Exception e) {
                model.addAttribute("error", OandaService.message(e));
            }
        }
        return "forex-hist";
    }

    // ---------------------------------------------------------------- Forex-Nyit
    @GetMapping("/forex/open")
    public String openForm(Model model) {
        model.addAttribute("instruments", OandaService.INSTRUMENTS);
        return "forex-open";
    }

    @PostMapping("/forex/open")
    public String openSubmit(@RequestParam String instrument, @RequestParam double units, Model model) {
        model.addAttribute("instruments", OandaService.INSTRUMENTS);
        model.addAttribute("selected", instrument);
        model.addAttribute("units", units);
        if (units == 0) {
            model.addAttribute("error", "A mennyiség nem lehet 0 (pozitív: Long, negatív: Short).");
            return "forex-open";
        }
        try {
            model.addAttribute("result", oanda.openPosition(instrument, units));
        } catch (Exception e) {
            model.addAttribute("error", OandaService.message(e));
        }
        return "forex-open";
    }

    // ------------------------------------------------------------------ Forex-Poz
    @GetMapping("/forex/positions")
    public String positions(Model model) {
        try {
            model.addAttribute("positions", oanda.positions());
        } catch (Exception e) {
            model.addAttribute("error", OandaService.message(e));
        }
        return "forex-positions";
    }

    // ------------------------------------------------------------------ Forex-Zár
    @GetMapping("/forex/close")
    public String closeForm(Model model) {
        loadTrades(model);
        return "forex-close";
    }

    @PostMapping("/forex/close")
    public String closeSubmit(@RequestParam String tradeId, Model model) {
        try {
            model.addAttribute("result", oanda.closeTrade(tradeId.trim()));
        } catch (Exception e) {
            model.addAttribute("error", OandaService.message(e));
        }
        loadTrades(model);
        return "forex-close";
    }

    private void loadTrades(Model model) {
        try {
            model.addAttribute("trades", oanda.openTrades());
        } catch (Exception e) {
            if (!model.containsAttribute("error")) {
                model.addAttribute("error", OandaService.message(e));
            }
        }
    }
}
