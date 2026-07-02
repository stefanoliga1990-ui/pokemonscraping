package com.example.pokemonscraping;

import com.example.pokemonscraping.config.CardTraderApiProperties;
import com.example.pokemonscraping.config.MonitoringScheduleProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.URI;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class PokemonscrapingApplicationTests {

    @Autowired
    private CardTraderApiProperties cardTraderApiProperties;

    @Autowired
    private MonitoringScheduleProperties monitoringScheduleProperties;

    @Test
    void contextLoadsWithExpectedConfiguration() {
        assertThat(cardTraderApiProperties.getBaseUrl())
                .isEqualTo(URI.create("https://api.cardtrader.com/api/v2"));
        assertThat(cardTraderApiProperties.getConnectTimeout()).isEqualTo(Duration.ofSeconds(5));
        assertThat(cardTraderApiProperties.getReadTimeout()).isEqualTo(Duration.ofSeconds(20));
        assertThat(monitoringScheduleProperties.getCron()).isEqualTo("0 0 6 * * MON");
        assertThat(monitoringScheduleProperties.getZone()).isEqualTo("Europe/Rome");
    }
}
