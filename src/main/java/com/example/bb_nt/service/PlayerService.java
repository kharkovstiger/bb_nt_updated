package com.example.bb_nt.service;

import com.example.bb_nt.model.Game;
import com.example.bb_nt.model.Player;
import com.example.bb_nt.model.utils.OffensiveTactic;
import com.example.bb_nt.model.utils.PlayerResponse;
import com.example.bb_nt.model.utils.Position;

import java.util.List;
import java.util.Map;

public interface PlayerService {
    
    Player addStats(Player player);

    List<Player> getAllFromCountry(String country);

    List<Player> getAllFromCountryForGame(String country);

    List<Player> getAllFromCountryForMinutes(String country);

    List<Player> getAllForGame(boolean u21);

    List<Player> getAllForMinutes(boolean u21);

    PlayerResponse getPlayersStatForGameList(List<Game> games, String country);

    List<Player> getAverages(List<Player> players, String game);

    Map<OffensiveTactic,Map<String,Double>> getPlayerStatsForOffensiveTactics(List<Game> games, String country, String playerId);

    Map<Position, Map<String, Double>> getPlayerStatsForPosition(List<Game> games, String country, String playerId);

    Map<OffensiveTactic, Map<Position, Map<String, Double>>> getStatsForOffensiveTacticsForPosition(List<Game> games, String country, String playerId);

    Map<Position, Map<OffensiveTactic, Map<String, Double>>> getStatsForPositionForOffensiveTactics(List<Game> games, String country, String playerId);
}
