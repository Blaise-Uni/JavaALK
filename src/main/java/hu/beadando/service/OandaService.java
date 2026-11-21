package hu.beadando.service;

import com.oanda.v20.Context;
import com.oanda.v20.ContextBuilder;
import com.oanda.v20.RequestException;
import com.oanda.v20.account.AccountID;
import com.oanda.v20.account.AccountSummary;
import com.oanda.v20.instrument.Candlestick;
import com.oanda.v20.instrument.CandlestickGranularity;
import com.oanda.v20.instrument.InstrumentCandlesRequest;
import com.oanda.v20.order.MarketOrderRequest;
import com.oanda.v20.order.OrderCreateRequest;
import com.oanda.v20.order.OrderCreateResponse;
import com.oanda.v20.position.Position;
import com.oanda.v20.pricing.ClientPrice;
import com.oanda.v20.primitives.InstrumentName;
import com.oanda.v20.trade.Trade;
import com.oanda.v20.trade.TradeCloseRequest;
import com.oanda.v20.trade.TradeCloseResponse;
import com.oanda.v20.trade.TradeSpecifier;
import com.oanda.v20.transaction.OrderFillTransaction;
import hu.beadando.model.Views.CandleView;
import hu.beadando.model.Views.PositionView;
import hu.beadando.model.Views.PriceView;
import hu.beadando.model.Views.TradeView;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Forex kliens az Oanda v20 Java könyvtárral. */
@Service
public class OandaService {

    public static final List<String> INSTRUMENTS = List.of(
            "EUR_USD", "GBP_USD", "USD_JPY", "USD_CHF", "AUD_USD",
            "USD_CAD", "NZD_USD", "EUR_GBP", "EUR_JPY", "EUR_HUF", "USD_HUF");

    public static final List<String> GRANULARITIES = List.of(
            "M1", "M5", "M15", "M30", "H1", "H4", "D", "W");

    private final String url;
    private final String token;
    private final String accountId;
    private Context ctx;

    public OandaService(@Value("${oanda.url}") String url,
                        @Value("${oanda.token}") String token,
                        @Value("${oanda.account-id}") String accountId) {
        this.url = url;
        this.token = token;
        this.accountId = accountId;
    }

    private synchronized Context ctx() {
        if (token == null || token.isBlank() || token.startsWith("IDE_")
                || accountId == null || accountId.startsWith("IDE_")) {
            throw new IllegalStateException(
                    "Az Oanda token / számlaazonosító nincs beállítva. "
                            + "Írd be az application.properties fájlban (oanda.token, oanda.account-id).");
        }
        if (ctx == null) {
            ctx = new ContextBuilder(url).setToken(token).setApplication("beadando").build();
        }
        return ctx;
    }

    private AccountID acc() {
        return new AccountID(accountId);
    }

    // ------------------------------------------------------------ Forex-account
    public Map<String, String> account() throws Exception {
        AccountSummary a = ctx().account.summary(acc()).getAccount();
        Map<String, String> m = new LinkedHashMap<>();
        m.put("Számlaazonosító", String.valueOf(a.getId()));
        m.put("Alias", String.valueOf(a.getAlias()));
        m.put("Pénznem", String.valueOf(a.getCurrency()));
        m.put("Egyenleg", String.valueOf(a.getBalance()));
        m.put("Nettó eszközérték (NAV)", String.valueOf(a.getNAV()));
        m.put("Realizált eredmény (P/L)", String.valueOf(a.getPl()));
        m.put("Nem realizált eredmény", String.valueOf(a.getUnrealizedPL()));
        m.put("Felhasznált margin", String.valueOf(a.getMarginUsed()));
        m.put("Szabad margin", String.valueOf(a.getMarginAvailable()));
        m.put("Nyitott ügyletek száma", String.valueOf(a.getOpenTradeCount()));
        m.put("Nyitott pozíciók száma", String.valueOf(a.getOpenPositionCount()));
        return m;
    }

    // ------------------------------------------------------------- Forex-AktÁr
    public PriceView price(String instrument) throws Exception {
        List<ClientPrice> prices = ctx().pricing.get(acc(), List.of(instrument)).getPrices();
        if (prices.isEmpty()) {
            throw new IllegalStateException("Nincs ár a(z) " + instrument + " instrumentumra.");
        }
        ClientPrice p = prices.get(0);
        String bid = p.getBids().isEmpty() ? "-" : String.valueOf(p.getBids().get(0).getPrice());
        String ask = p.getAsks().isEmpty() ? "-" : String.valueOf(p.getAsks().get(0).getPrice());
        return new PriceView(String.valueOf(p.getInstrument()), String.valueOf(p.getTime()),
                bid, ask, String.valueOf(p.getStatus()));
    }

    // ------------------------------------------------------------ Forex-HistÁr
    public List<CandleView> history(String instrument, String granularity) throws Exception {
        InstrumentCandlesRequest req = new InstrumentCandlesRequest(new InstrumentName(instrument))
                .setGranularity(CandlestickGranularity.valueOf(granularity))
                .setCount(10L);
        List<CandleView> out = new ArrayList<>();
        for (Candlestick c : ctx().instrument.candles(req).getCandles()) {
            if (c.getMid() == null) continue;
            out.add(new CandleView(String.valueOf(c.getTime()),
                    String.valueOf(c.getMid().getO()), String.valueOf(c.getMid().getH()),
                    String.valueOf(c.getMid().getL()), String.valueOf(c.getMid().getC()),
                    c.getVolume() == null ? 0 : c.getVolume()));
        }
        return out;
    }

    // --------------------------------------------------------------- Forex-Nyit
    public String openPosition(String instrument, double units) throws Exception {
        MarketOrderRequest order = new MarketOrderRequest()
                .setInstrument(new InstrumentName(instrument))
                .setUnits(units);
        OrderCreateResponse resp = ctx().order.create(new OrderCreateRequest(acc()).setOrder(order));
        OrderFillTransaction fill = resp.getOrderFillTransaction();
        if (fill != null) {
            String tradeId = fill.getTradeOpened() != null ? String.valueOf(fill.getTradeOpened().getTradeID()) : "-";
            return "Sikeres végrehajtás (" + (units > 0 ? "LONG" : "SHORT") + "). Ügylet azonosító (tradeId): "
                    + tradeId + ", ár: " + fill.getPrice() + ", egység: " + fill.getUnits();
        }
        return "A megbízás nem teljesült (a rendszer törölte). Tranzakció: "
                + (resp.getOrderCancelTransaction() != null ? resp.getOrderCancelTransaction() : "ismeretlen");
    }

    // ----------------------------------------------------------------- Forex-Poz
    public List<PositionView> positions() throws Exception {
        List<PositionView> out = new ArrayList<>();
        for (Position p : ctx().position.listOpen(acc()).getPositions()) {
            out.add(new PositionView(String.valueOf(p.getInstrument()),
                    String.valueOf(p.getLong().getUnits()), String.valueOf(p.getShort().getUnits()),
                    String.valueOf(p.getUnrealizedPL())));
        }
        return out;
    }

    public List<TradeView> openTrades() throws Exception {
        List<TradeView> out = new ArrayList<>();
        for (Trade t : ctx().trade.listOpen(acc()).getTrades()) {
            out.add(new TradeView(String.valueOf(t.getId()), String.valueOf(t.getInstrument()),
                    String.valueOf(t.getCurrentUnits()), String.valueOf(t.getPrice()),
                    String.valueOf(t.getOpenTime()), String.valueOf(t.getUnrealizedPL())));
        }
        return out;
    }

    // ----------------------------------------------------------------- Forex-Zár
    public String closeTrade(String tradeId) throws Exception {
        TradeCloseResponse resp = ctx().trade.close(new TradeCloseRequest(acc(), new TradeSpecifier(tradeId)));
        OrderFillTransaction fill = resp.getOrderFillTransaction();
        if (fill != null) {
            return "Az ügylet (" + tradeId + ") lezárva. Záróár: " + fill.getPrice() + ", realizált P/L: " + fill.getPl();
        }
        return "A zárási megbízás nem teljesült.";
    }

    /** Barátságos hibaüzenet a sablonoknak. */
    public static String message(Exception e) {
        if (e instanceof RequestException re) {
            return "Oanda hiba (" + re.getStatus() + "): " + re.getErrorMessage();
        }
        return e.getMessage() != null ? e.getMessage() : e.toString();
    }
}
