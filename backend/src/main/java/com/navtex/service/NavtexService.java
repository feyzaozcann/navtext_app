package com.navtex.service;

import com.navtex.model.NavtexMessage;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.logging.Logger;

@Service
public class NavtexService {

    private static final Logger log = Logger.getLogger(NavtexService.class.getName());

    private final GreeceScraperService greeceScraper;
    private final TurkeyScraperService turkeyScraper;
    private final UKScraperService ukScraper;
    private final SwedenScraperService swedenScraper;

    public NavtexService(GreeceScraperService greeceScraper,
                         TurkeyScraperService turkeyScraper,
                         UKScraperService ukScraper,
                         SwedenScraperService swedenScraper) {
        this.greeceScraper = greeceScraper;
        this.turkeyScraper = turkeyScraper;
        this.ukScraper     = ukScraper;
        this.swedenScraper = swedenScraper;
    }

    @Cacheable("navtex-all")
    public List<NavtexMessage> getAllMessages() {
        List<NavtexMessage> all = new ArrayList<>();
        fetchSafe("Greece",         greeceScraper::fetchMessages, all);
        fetchSafe("Turkey",         turkeyScraper::fetchMessages, all);
        fetchSafe("United Kingdom", ukScraper::fetchMessages,     all);
        fetchSafe("Sweden",         swedenScraper::fetchMessages,  all);
        
        all.sort((a, b) -> {
        return parseDate(b.getPublishedAt()).compareTo(parseDate(a.getPublishedAt()));
        });

        log.info("Total messages fetched: " + all.size());
        return all;
    }

    private void fetchSafe(String country,
                           java.util.function.Supplier<List<NavtexMessage>> fn,
                           List<NavtexMessage> target) {
        try {
            List<NavtexMessage> msgs = fn.get();
            target.addAll(msgs);
            log.info(country + ": " + msgs.size() + " messages");
        } catch (Exception e) {
            log.warning(country + " scraper failed: " + e.getMessage());
        }
    }

    @Cacheable("navtex-country")
    public List<NavtexMessage> getMessagesByCountry(String country) {
        return getAllMessages().stream()
                .filter(m -> m.getCountry().equalsIgnoreCase(country))
                .toList();
    }

    @Scheduled(fixedDelay = 1800000)
    @CacheEvict(value = {"navtex-all", "navtex-country"}, allEntries = true)
    public void refreshCache() {
        log.info("Cache cleared — will refresh on next request");
    }

    private java.time.LocalDateTime parseDate(String dateStr) {
    if (dateStr == null || dateStr.isBlank()) return java.time.LocalDateTime.MIN;
    String[] formats = {
        "d.MM.yyyy HH:mm:ss",
        "dd.MM.yyyy HH:mm:ss",
        "d.MM.yyyy",
        "dd.MM.yyyy",
        "dd MMM yyyy HH:mm",
        "d MMM yyyy",
        "yyyyMMdd"
    };
    for (String fmt : formats) {
        try {
            return java.time.LocalDateTime.parse(
                dateStr.trim(),
                java.time.format.DateTimeFormatter.ofPattern(fmt)
            );
        } catch (Exception ignored) {}
        try {
            return java.time.LocalDate.parse(
                dateStr.trim().split(" ")[0],
                java.time.format.DateTimeFormatter.ofPattern(fmt)
            ).atStartOfDay();
        } catch (Exception ignored) {}
    }
    return java.time.LocalDateTime.MIN;
}
}
