package com.example.bb_nt.service;

import com.example.bb_nt.model.Game;
import com.example.bb_nt.model.utils.DefensiveTactic;
import com.example.bb_nt.model.utils.OffensiveTactic;
import com.example.bb_nt.model.utils.Results;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface GameService {
    Game parseBoxScore(String response);

    int getSeason(LocalDate localDate);

    Game save(Game game);

    List<Game> getAllGamesForCountry(String country, boolean official);

    List<Game> getAllGamesForCountryForSeason(String country, boolean official, Integer season);

    List<Game> getAllGamesForCountryAgainstCountry(String s, String s1, boolean official);

    List<Game> getGamesForList(List<String> ids);
    
    String getMaxId(int season);

    void addGame(Integer id);

    Map<String,Double> getSeasonStatisticsForCountry(String country, Integer season);

    Map<String,Double> getAveragedStatistics(List<Game> games, String country);

    List<Game> getAllGamesForSeason(int season);

    Results getResultsFromGameList(List<Game> games, String s);

    Game getLastInsertedGame();

    Map<OffensiveTactic,Map<String,Double>> getStatsForOffensiveTacticsForGameList(List<Game> games, String country);

    Map<DefensiveTactic, Map<String, Double>> getStatsForDefensiveTacticsForGameList(List<Game> games, String country);
}
