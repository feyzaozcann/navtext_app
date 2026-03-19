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
public class SwedenScraperService {

    private static final Logger log = Logger.getLogger(SwedenScraperService.class.getName());
    private static final String URL = "https://navvarn.sjofartsverket.se/en/Navigationsvarningar/Navtex";

    private String detectType(String text) {
        if (text == null) return "A";
        String u = text.toUpperCase();
        if (u.contains("WEATHER") || u.contains("WIND") || u.contains("GALE") || u.contains("ICE")) return "B";
        if (u.contains("SAR") || u.contains("DISTRESS")) return "D";
        if (u.contains("FORECAST")) return "E";
        if (u.contains("GPS") || u.contains("SATNAV") || u.contains("SATELLITE") || u.contains("INTERFERENCE")) return "J";
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

            // Each area has an h5 heading followed by warning blocks
            // Warnings are in p or div elements containing DTG + bold title + message text
            String currentArea = "Swedish Waters";
            Elements sections = doc.select("h5, p, div.warning, .warning-text");

            // Simpler approach: get all h5 (area headings) and following siblings
            Elements h5s = doc.select("h5");
            for (Element h5 : h5s) {
                currentArea = h5.text().trim();
                // Get following siblings until next h5
                Element sibling = h5.nextElementSibling();
                while (sibling != null && !sibling.tagName().equals("h5")) {
                    String text = sibling.text().trim();
                    // A warning block contains a DTG pattern like "NNNNNN UTC MMM"
                    if (text.matches(".*\\d{6} UTC [A-Z]{3}.*") || text.contains("NAV WARN")) {
                        String type = detectType(text);
                        String station = currentArea.length() > 30
                            ? currentArea.substring(0, 30) : currentArea;

                        messages.add(new NavtexMessage(
                            UUID.randomUUID().toString(),
                            "Swedish Maritime Administration",
                            "Sweden", "🇸🇪",
                            type, resolveTypeLabel(type),
                            text,
                            "NAVWARN — " + currentArea,
                            "", URL
                        ));
                    }
                    sibling = sibling.nextElementSibling();
                }
            }

            // Fallback: grab all text blocks that look like warnings
            if (messages.isEmpty()) {
                Elements paras = doc.select("p, pre");
                for (Element p : paras) {
                    String text = p.text().trim();
                    if ((text.contains("NAV WARN") || text.contains("UTC")) && text.length() > 30) {
                        String type = detectType(text);
                        messages.add(new NavtexMessage(
                            UUID.randomUUID().toString(),
                            "Swedish Maritime Administration",
                            "Sweden", "🇸🇪",
                            type, resolveTypeLabel(type),
                            text,
                            "NAVWARN — Swedish Waters",
                            "", URL
                        ));
                    }
                }
            }

            log.info("Sweden messages fetched: " + messages.size());
        } catch (Exception e) {
            log.severe("Sweden scraper error: " + e.getMessage());
        }
        return messages;
    }
}
