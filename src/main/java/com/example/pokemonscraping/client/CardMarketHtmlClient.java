package com.example.pokemonscraping.client;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CardMarketHtmlClient {

    public Document fetch(String url) throws IOException {
        return Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
                        + "(KHTML, like Gecko) Chrome/147.0.0.0 Safari/537.36")
                .referrer("https://www.google.com")
                .timeout(15000)
                .get();
    }
}