package com.example.bb_nt.repository;


import com.example.bb_nt.model.Game;

import java.util.List;

public interface GameRepository {
    Game save(Game game);

    boolean exists(String id);

    List<Game> getAllGamesForCountry(String country, boolean official);

    List<Game> getAllGamesForCountryForSeason(String country, boolean official, Integer season);

    List<Game> getAllGamesForCountryAgainstCountry(String s, String s1, boolean official);

    List<Game> getGamesForList(List<String> ids);

    Game getMaxId(int season);

    List<Game> getAllGamesForSeason(int season);

    Game getLastInsertedGame();
}
