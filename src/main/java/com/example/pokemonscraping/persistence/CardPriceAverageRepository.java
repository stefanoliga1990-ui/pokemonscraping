package com.example.pokemonscraping.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CardPriceAverageRepository extends JpaRepository<CardPriceAverageEntity, Long> {

    List<CardPriceAverageEntity> findAllByOrderByCalculatedAtAsc();

    List<CardPriceAverageEntity> findAllByOrderByCalculatedAtDesc();
}