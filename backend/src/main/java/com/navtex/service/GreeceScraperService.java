package com.navtex.service;

import com.navtex.model.NavtexMessage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

@Service
public class GreeceScraperService {

    private static final Logger log = Logger.getLogger(GreeceScraperService.class.getName());
    private static final String BASE_URL = "https://hnhs.gr/en/category/navtex-messages/";

    private String resolveTypeLabel(String type) {
        return switch (type) {
            case "A" -> "Navigational Warning";
            case "B" -> "Meteorological Warning";
            case "C" -> "Ice Report";
            case "D" -> "SAR Information";
            case "E" -> "Weather Forecast";
            case "F" -> "Pilot Service";
            case "J" -> "SATNAV";
            case "T" -> "Test Message";
            default  -> "Navigational Warning";
        };
    }

    private String extractType(String raw) {
        if (raw == null || raw.length() < 8) return "A";
        String[] lines = raw.trim().split("\n");
        for (String line : lines) {
            if (line.startsWith("ZCZC") && line.length() >= 8) {
                String code = line.substring(5).trim();
                if (code.length() >= 2) return String.valueOf(code.charAt(1)).toUpperCase();
            }
        }
        return "A";
    }

    private String resolveStation(String raw) {
        if (raw == null) return "Hellenic Radio";
        String u = raw.toUpperCase();
        if (u.contains("IRAKLEIO") || u.contains("IRAKLION")) return "Irakleio Radio";
        if (u.contains("KERKYRA") || u.contains("CORFU"))     return "Kerkyra Radio";
        if (u.contains("LIMNOS")  || u.contains("LEMNOS"))    return "Limnos Radio";
        return "Hellenic Radio";
    }

    public List<NavtexMessage> fetchMessages() {
        List<NavtexMessage> messages = new ArrayList<>();
        try {
            Document doc = Jsoup.connect(BASE_URL)
                    .userAgent("Mozilla/5.0 (compatible; NavtexBot/1.0)")
                    .timeout(15000)
                    .get();

            Elements articles = doc.select("article");
            for (Element article : articles) {
                try {
                    String title   = article.select("h2.entry-title, h1.entry-title").text();
                    String postUrl = article.select("a[href]").attr("href");
                    String dateText = article.select("time.entry-date, time").attr("datetime");
                    if (dateText.isEmpty()) dateText = article.select("time").text();
                    String content = article.select(".entry-content, .entry-summary, p").text();

                    String raw = content;
                    if (postUrl != null && !postUrl.isEmpty()) {
                        try {
                            Document postDoc = Jsoup.connect(postUrl)
                                    .userAgent("Mozilla/5.0 (compatible; NavtexBot/1.0)")
                                    .timeout(10000).get();
                            String fullContent = postDoc.select(".entry-content").text();
                            if (!fullContent.isEmpty()) raw = fullContent;
                        } catch (Exception ignored) {}
                    }

                    String type    = extractType(raw);
                    String station = resolveStation(raw);

                    messages.add(new NavtexMessage(
                        UUID.randomUUID().toString(),
                        station, "Greece", "🇬🇷",
                        type, resolveTypeLabel(type),
                        raw,
                        title.isEmpty() ? "NAVTEX Message" : title,
                        dateText, postUrl
                    ));
                } catch (Exception e) {
                    log.warning("Error parsing Greek article: " + e.getMessage());
                }
            }
            log.info("Greece messages fetched: " + messages.size());
        } catch (IOException e) {
            log.severe("Failed to fetch from hnhs.gr: " + e.getMessage());
        }
        return messages;
    }
}
