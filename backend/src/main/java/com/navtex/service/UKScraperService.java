package com.navtex.service;

import com.navtex.model.NavtexMessage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.logging.Logger;

@Service
public class UKScraperService {

    private static final Logger log = Logger.getLogger(UKScraperService.class.getName());
    private static final String URL = "https://msi.admiralty.co.uk/RadioNavigationalWarnings";

    private String detectType(String text) {
        if (text == null) return "A";
        String u = text.toUpperCase();
        if (u.contains("WEATHER") || u.contains("WIND") || u.contains("GALE") || u.contains("STORM")) return "B";
        if (u.contains("SAR") || u.contains("DISTRESS") || u.contains("MAYDAY")) return "D";
        if (u.contains("FORECAST")) return "E";
        if (u.contains("GPS") || u.contains("SATNAV") || u.contains("SATELLITE")) return "J";
        return "A";
    }

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

    public List<NavtexMessage> fetchMessages() {
        List<NavtexMessage> messages = new ArrayList<>();
        try {
            Document doc = Jsoup.connect(URL)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .header("Accept", "text/html,application/xhtml+xml")
                    .timeout(20000)
                    .get();

            // The page has a table where odd rows = header (ref, dtg, desc)
            // and even rows = expanded detail with the full raw message in a <pre> or <code> block
            // We parse all rows and pair them up
            Elements rows = doc.select("table tr");

            for (int i = 0; i < rows.size(); i++) {
                Element row = rows.get(i);
                Elements cells = row.select("td");

                // Header row has 4+ cells: Reference | DTG | Description | View | Select
                if (cells.size() >= 3) {
                    String ref  = cells.get(0).text().trim();
                    String dtg  = cells.get(1).text().trim();
                    String desc = cells.get(2).text().trim();

                    if (ref.isEmpty() || dtg.isEmpty()) continue;

                    // Determine station
                    String station;
                    if (ref.toUpperCase().contains("NAVAREA")) {
                        station = "NAVAREA I";
                    } else if (desc.toUpperCase().contains("NAVAREA") || dtg.toUpperCase().contains("NAVAREA")) {
                        station = "NAVAREA I";
                    } else {
                        station = "UK Coastal";
                    }

                    // Look ahead for the detail row (next tr with pre/code)
                    String rawBody = desc;
                    if (i + 1 < rows.size()) {
                        Element nextRow = rows.get(i + 1);
                        String preText = nextRow.select("pre, code").text().trim();
                        if (!preText.isEmpty()) {
                            rawBody = preText;
                            i++; // skip detail row
                        }
                    }

                    String fullRaw = station + "\n" + ref + "\n" + dtg + "\n" + rawBody;
                    String type    = detectType(rawBody);

                    messages.add(new NavtexMessage(
                        UUID.randomUUID().toString(),
                        station, "United Kingdom", "🇬🇧",
                        type, resolveTypeLabel(type),
                        fullRaw,
                        ref + " — " + desc,
                        dtg, URL
                    ));
                }
            }

            log.info("UK messages fetched: " + messages.size());
        } catch (Exception e) {
            log.severe("UK scraper error: " + e.getMessage());
        }
        return messages;
    }
}