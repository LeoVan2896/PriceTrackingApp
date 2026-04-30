package com.example.app.service;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@Slf4j
public class PriceScraperService {

    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                    "AppleWebKit/537.36 (KHTML, like Gecko) " +
                    "Chrome/120.0.0.0 Safari/537.36";

    public Optional<BigDecimal> scrapePrice(String url) {
        if (url == null || url.isBlank()) {
            return Optional.empty();
        }

        try {
            log.info("Scraping URL: {}", url);

            Document doc = Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
                    .header("Accept-Language", "en-US,en;q=0.9")
                    .header("Connection", "keep-alive")
                    .header("Upgrade-Insecure-Requests", "1")
                    .header("Sec-Fetch-Dest", "document")
                    .header("Sec-Fetch-Mode", "navigate")
                    .header("Sec-Fetch-Site", "none")
                    .header("Sec-Fetch-User", "?1")
                    .referrer("https://www.google.com")
                    .timeout(15_000)
                    .followRedirects(true)
                    .get();

            if (url.contains("bestbuy.com")) {
                return scrapeBestBuy(doc);
            } else if (url.contains("gamestop.com")) {
                return scrapeGameStop(doc);
            } else if (url.contains("walmart.com")) {
                return scrapeWalmart(doc);
            }
             else if (url.contains("newegg.com")) {
            return scrapeNewegg(doc);
            }else {
                log.warn("No scraper configured for URL: {}", url);
                return Optional.empty();
            }

        } catch (Exception e) {
            log.error("Failed to scrape URL {}: {}", url, e.getMessage());
            return Optional.empty();
        }
    }
    private Optional<BigDecimal> scrapeNewegg(Document doc) {
        log.info("Page title: {}", doc.title());
        Element priceEl = doc.selectFirst("div.price-current strong");
        return parsePrice(priceEl);
    }
    private Optional<BigDecimal> scrapeBestBuy(Document doc) {
        log.debug("Page title: {}", doc.title());
        log.info("Page length: {} chars", doc.html().length());

        Element priceEl = doc.selectFirst("div.priceView-customer-price span[aria-hidden=true]");
        return parsePrice(priceEl);
    }

    private Optional<BigDecimal> scrapeGameStop(Document doc) {
        // GameStop price selector
        Element priceEl = doc.selectFirst("span.selling-price");
        return parsePrice(priceEl);
    }

    private Optional<BigDecimal> scrapeWalmart(Document doc) {
        // Walmart price selector
        Element priceEl = doc.selectFirst("span[itemprop=price]");
        if (priceEl != null) {
            String content = priceEl.attr("content");
            if (!content.isBlank()) {
                try {
                    return Optional.of(new BigDecimal(content));
                } catch (NumberFormatException e) {
                    log.warn("Could not parse Walmart price: {}", content);
                }
            }
        }
        return Optional.empty();
    }

    private Optional<BigDecimal> parsePrice(Element element) {
        if (element == null) {
            log.warn("Price element not found — site may have changed its HTML structure");
            return Optional.empty();
        }

        String raw = element.text()
                .replace("$", "")
                .replace(",", "")
                .trim();

        try {
            return Optional.of(new BigDecimal(raw));
        } catch (NumberFormatException e) {
            log.warn("Could not parse price text: '{}'", raw);
            return Optional.empty();
        }
    }
}