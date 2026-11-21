package hu.beadando.model;

/** Egyszerű megjelenítési modellek a sablonokhoz. */
public final class Views {
    private Views() {}

    public record Rate(String date, int unit, double value) {}
    public record PriceView(String instrument, String time, String bid, String ask, String status) {}
    public record CandleView(String time, String open, String high, String low, String close, long volume) {}
    public record PositionView(String instrument, String longUnits, String shortUnits, String unrealizedPl) {}
    public record TradeView(String id, String instrument, String units, String price, String openTime, String unrealizedPl) {}
}
