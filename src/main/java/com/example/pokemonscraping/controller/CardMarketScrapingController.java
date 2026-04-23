package com.example.pokemonscraping.controller;

import com.example.pokemonscraping.model.CardPriceAverageResponse;
import com.example.pokemonscraping.persistence.CardPriceAverageEntity;
import com.example.pokemonscraping.service.CardMarketScrapingService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/scraping/cardmarket")
public class CardMarketScrapingController {

    private final CardMarketScrapingService scrapingService;

    public CardMarketScrapingController(CardMarketScrapingService scrapingService) {
        this.scrapingService = scrapingService;
    }

    @GetMapping("/calculate")
    public CardPriceAverageResponse calculate(@RequestParam(required = false) String url) {
        try {
            return scrapingService.calculateAverages(url);
        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Errore durante il recupero o parsing della pagina",
                    e
            );
        }
    }

    @GetMapping("/history")
    public List<CardPriceAverageEntity> getHistory() {
        return scrapingService.getHistory();
    }
}