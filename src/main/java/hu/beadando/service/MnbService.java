package hu.beadando.service;

import hu.beadando.model.Views.Rate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * SOAP kliens a Magyar Nemzeti Bank árfolyam webszolgáltatásához.
 * A SOAP 1.1 üzeneteket közvetlenül építjük fel és küldjük HTTP POST-tal.
 */
@Service
public class MnbService {

    private static final String NS = "http://www.mnb.hu/webservices/";
    private static final String ACTION_BASE = "http://www.mnb.hu/webservices/MNBArfolyamServiceSoap/";

    private final String url;
    private final HttpClient http = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    public MnbService(@Value("${mnb.url}") String url) {
        this.url = url;
    }

    /** A szolgáltatás által ismert devizanemek (GetCurrencies). */
    public List<String> currencies() throws Exception {
        String inner = call("GetCurrencies", "", "GetCurrenciesResult");
        NodeList nl = parse(inner).getElementsByTagName("Curr");
        List<String> out = new ArrayList<>();
        for (int i = 0; i < nl.getLength(); i++) {
            out.add(nl.item(i).getTextContent().trim());
        }
        out.sort(Comparator.naturalOrder());
        return out;
    }

    /** Napi árfolyamok egy devizára a megadott időszakban (GetExchangeRates). */
    public List<Rate> rates(String currency, String from, String to) throws Exception {
        String body = "<web:startDate>" + esc(from) + "</web:startDate>"
                + "<web:endDate>" + esc(to) + "</web:endDate>"
                + "<web:currencyNames>" + esc(currency) + "</web:currencyNames>";
        String inner = call("GetExchangeRates", body, "GetExchangeRatesResult");
        List<Rate> out = new ArrayList<>();
        NodeList days = parse(inner).getElementsByTagName("Day");
        for (int i = 0; i < days.getLength(); i++) {
            Element day = (Element) days.item(i);
            NodeList rs = day.getElementsByTagName("Rate");
            for (int j = 0; j < rs.getLength(); j++) {
                Element r = (Element) rs.item(j);
                String txt = r.getTextContent().trim().replace(',', '.');
                if (txt.isEmpty()) continue;
                int unit = r.hasAttribute("unit") ? Integer.parseInt(r.getAttribute("unit")) : 1;
                out.add(new Rate(day.getAttribute("date"), unit, Double.parseDouble(txt)));
            }
        }
        out.sort(Comparator.comparing(Rate::date));
        return out;
    }

    // ---------------------------------------------------------------- SOAP

    private String call(String operation, String bodyContent, String resultTag) throws Exception {
        String envelope = """
                <?xml version="1.0" encoding="utf-8"?>
                <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:web="%s">
                  <soapenv:Body>
                    <web:%s>%s</web:%s>
                  </soapenv:Body>
                </soapenv:Envelope>""".formatted(NS, operation, bodyContent, operation);

        HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "text/xml; charset=utf-8")
                .header("SOAPAction", "\"" + ACTION_BASE + operation + "\"")
                .POST(HttpRequest.BodyPublishers.ofString(envelope, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (resp.statusCode() != 200) {
            throw new IllegalStateException("MNB SOAP hiba (HTTP " + resp.statusCode() + ")");
        }
        NodeList nl = parse(resp.body()).getElementsByTagNameNS("*", resultTag);
        if (nl.getLength() == 0) {
            throw new IllegalStateException("Az MNB válasza nem tartalmaz '" + resultTag + "' elemet.");
        }
        return nl.item(0).getTextContent();
    }

    private static Document parse(String xml) throws Exception {
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(true);
        f.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        f.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        return f.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
    }

    private static String esc(String s) {
        return s == null ? "" : s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
