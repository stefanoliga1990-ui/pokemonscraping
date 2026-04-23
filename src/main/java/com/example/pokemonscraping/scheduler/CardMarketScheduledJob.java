package com.example.pokemonscraping.scheduler;

import com.example.pokemonscraping.service.CardMarketScrapingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CardMarketScheduledJob {

    private static final Logger log = LoggerFactory.getLogger(CardMarketScheduledJob.class);

    private final CardMarketScrapingService scrapingService;

    public CardMarketScheduledJob(CardMarketScrapingService scrapingService) {
        this.scrapingService = scrapingService;
    }

    @Scheduled(cron = "${app.scraping.cron:0 0 12 * * *}", zone = "${app.scraping.zone:Europe/Rome}")
    public void calculateDailyAverage() {
        try {
            log.info("Avvio calcolo schedulato di EX e NM...");
            scrapingService.calculateAverages(null);
            log.info("Calcolo schedulato completato con successo.");
        } catch (Exception e) {
            log.error("Errore durante il calcolo schedulato", e);
        }
    }
}