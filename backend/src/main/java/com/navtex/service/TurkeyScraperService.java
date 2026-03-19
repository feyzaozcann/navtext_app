package com.navtex.service;

import com.navtex.model.NavtexMessage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Logger;

@Service
public class TurkeyScraperService {

    private static final Logger log = Logger.getLogger(TurkeyScraperService.class.getName());
    private static final String BASE_URL = "https://www.kiyiemniyeti.gov.tr/turk_radyo_yayinlari";

    private static final String[][] CITIES = {
        {"istanbul", "Istanbul Radio"},
        {"antalya",  "Antalya Radio"},
        {"samsun",   "Samsun Radio"},
        {"izmir",    "Izmir Radio"},
    };

    private String resolveTypeLabel(String type) {
        return switch (type) {
            case "A" -> "Navigational Warning";
            case "B" -> "Meteorological Warning";
            case "D" -> "SAR Information";
            case "E" -> "Weather Forecast";
            case "J" -> "SATNAV";
            default  -> "Navigational Warning";
        };
    }

    private String detectType(String msg) {
        if (msg == null) return "A";
        String u = msg.toUpperCase();
        if (u.contains("METEOROLOJ") || u.contains("RÜZGAR")) return "B";
        if (u.contains("ARAMA") || u.contains("KURTARMA") || u.contains("IMDAT")) return "D";
        if (u.contains("TAHMIN") || u.contains("FORECAST")) return "E";
        if (u.contains("GPS") || u.contains("SATNAV")) return "J";
        return "A";
    }

    private List<NavtexMessage> scrapeCity(String cityKey, String stationName) {
        List<NavtexMessage> messages = new ArrayList<>();
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        try {
            Document doc = Jsoup.connect(BASE_URL)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .header("Accept", "text/html,application/xhtml+xml")
                    .header("Accept-Language", "tr-TR,tr;q=0.9")
                    .header("Referer", BASE_URL)
                    .data("City", cityKey)
                    .data("State", "2")
                    .data("Date", today)
                    .timeout(20000)
                    .post();
            messages.addAll(parseTable(doc, stationName));
            log.info(cityKey + " -> " + messages.size() + " messages");
        } catch (Exception e) {
            log.warning("POST error [" + cityKey + "]: " + e.getMessage());
        }
        return messages;
    }

    private List<NavtexMessage> parseTable(Document doc, String stationName) {
        List<NavtexMessage> messages = new ArrayList<>();
        Elements rows = doc.select("table tbody tr");
        for (Element row : rows) {
            Elements cols = row.select("td");
            if (cols.size() < 5) continue;
            String startDate = cols.get(1).text().trim();
            String code      = cols.get(3).text().trim().replace("\n", "/");
            String msgText   = cols.get(4).select(".table_message").text().trim();
            if (msgText.isEmpty()) msgText = cols.get(4).text().trim();
            if (msgText.isEmpty()) continue;
            String type = detectType(msgText);
            String raw  = "ZCZC " + code + "\n" + startDate + " UTC\n"
                        + stationName.toUpperCase() + "\n" + msgText + "\nNNNN";
            messages.add(new NavtexMessage(
                UUID.randomUUID().toString(),
                stationName, "Turkey", "🇹🇷",
                type, resolveTypeLabel(type),
                raw, "NAVTEX " + code + " — " + stationName,
                startDate, BASE_URL
            ));
        }
        return messages;
    }

    public List<NavtexMessage> fetchMessages() {
        List<NavtexMessage> all = new ArrayList<>();
        for (String[] city : CITIES) all.addAll(scrapeCity(city[0], city[1]));
        log.info("Total Turkey messages: " + all.size());
        return all;
    }
}
