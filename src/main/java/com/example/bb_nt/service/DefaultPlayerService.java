package com.example.bb_nt.service;

import com.example.bb_nt.model.Game;
import com.example.bb_nt.model.Player;
import com.example.bb_nt.model.Team;
import com.example.bb_nt.model.utils.OffensiveTactic;
import com.example.bb_nt.model.utils.PlayerResponse;
import com.example.bb_nt.model.utils.Position;
import com.example.bb_nt.model.utils.Record;
import com.example.bb_nt.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DefaultPlayerService implements PlayerService{
    
    private final PlayerRepository playerRepository;

    @Autowired
    public DefaultPlayerService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    @Override
    public Player addStats(Player player) {
        Player existPlayer = playerRepository.findOne(player.getId());
        if (existPlayer == null)
            return playerRepository.save(player);

        existPlayer.addStat(player.getStats());
        return playerRepository.save(existPlayer);
    }

    @Override
    public List<Player> getAllFromCountry(String country) {
        return playerRepository.getAllFromCountry(country);
    }

    @Override
    public List<Player> getAllFromCountryForGame(String country) {
        List<Player> players=playerRepository.getAllFromCountryMinGames(country);
        return getAverages(players, "game");
    }

    @Override
    public List<Player> getAverages(List<Player> players, String type) {
        players.forEach(player -> {
            Double divider=type.equals("game")?player.getStats().get("games"):player.getStats().get("minutes")/48;
            player.getStats().forEach((s, aDouble) -> {
                if (!s.equals("games"))
                    player.getStats().replace(s,aDouble/divider);
            });
        });
        return players;
    }

    @Override
    public Map<OffensiveTactic, Map<String, Double>> getPlayerStatsForOffensiveTactics(List<Game> games, String country, String playerId) {
        Map<OffensiveTactic, Map<String, Double>> result=new HashMap<>();
        for (OffensiveTactic offensiveTactic : OffensiveTactic.values()) {
            List<Game> gamesForTactic=games.stream().filter(g -> getTeam(g, country).getOffensiveTactic().equals(offensiveTactic))
                    .collect(Collectors.toList());
            PlayerResponse response=getPlayersStatForGameList(gamesForTactic, country);
            List<Player> players=getAverages(response.getPlayers(), "game");
            players.stream().filter(p -> p.getId().equals(playerId)).findFirst()
                    .ifPresent(player -> result.put(offensiveTactic, player.getStats()));
        }
        return result;
    }

    @Override
    public Map<Position, Map<String, Double>> getPlayerStatsForPosition(List<Game> games, String country, String playerId) {
        Map<Position, Map<String, Double>> result=new HashMap<>();
        for (Position position : Position.values()) {
            List<Game> gamesForPlayer=games.stream().filter(g -> getTeam(g, country).getPlayers().stream()
                    .anyMatch(p -> p.getId().equals(playerId) && position.equals(p.getPosition())))
                    .collect(Collectors.toList());
            PlayerResponse response=getPlayersStatForGameList(gamesForPlayer, country);
            List<Player> players=getAverages(response.getPlayers(), "game");
            players.stream().filter(p -> p.getId().equals(playerId)).findFirst()
                    .ifPresent(player -> result.put(position, player.getStats()));
        }
        return result;
    }

    @Override
    public Map<OffensiveTactic, Map<Position, Map<String, Double>>> getStatsForOffensiveTacticsForPosition(List<Game> games, String country, String playerId) {
        Map<OffensiveTactic, Map<Position, Map<String, Double>>> result=new HashMap<>();
        for (OffensiveTactic offensiveTactic : OffensiveTactic.values()) {
            List<Game> gamesForTactic=games.stream().filter(g -> getTeam(g, country).getOffensiveTactic().equals(offensiveTactic))
                    .collect(Collectors.toList());
            Map<Position, Map<String, Double>> partialResult=getPlayerStatsForPosition(gamesForTactic, country, playerId);
            result.put(offensiveTactic, partialResult);
        }
        return result;
    }

    @Override
    public Map<Position, Map<OffensiveTactic, Map<String, Double>>> getStatsForPositionForOffensiveTactics(List<Game> games, String country, String playerId) {
        Map<Position, Map<OffensiveTactic, Map<String, Double>>> result=new HashMap<>();
        for (Position position : Position.values()) {
            List<Game> gamesForPlayer=games.stream().filter(g -> getTeam(g, country).getPlayers().stream()
                    .anyMatch(p -> p.getId().equals(playerId) && position.equals(p.getPosition())))
                    .collect(Collectors.toList());
            Map<OffensiveTactic, Map<String, Double>> partialResult=getPlayerStatsForOffensiveTactics(gamesForPlayer, country, playerId);
            result.put(position, partialResult);
        }
        return result;
    }

    private Team getTeam(Game g, String country) {
        return g.getAwayTeam().getName().equals(country)?g.getAwayTeam():g.getHomeTeam();
    }

    @Override
    public List<Player> getAllFromCountryForMinutes(String country) {
        List<Player> players=playerRepository.getAllFromCountryMinGames(country);
        return getAverages(players, "minutes");
    }

    @Override
    public List<Player> getAllForGame(boolean u21) {
        List<Player> players=playerRepository.getAllMinGames(u21);
        return getAverages(players, "game");
    }

    @Override
    public List<Player> getAllForMinutes(boolean u21) {
        List<Player> players=playerRepository.getAllMinGames(u21);
        return getAverages(players, "minutes");
    }

    @Override
    public PlayerResponse getPlayersStatForGameList(List<Game> games, String country) {
        List<Player> players=new ArrayList<>();
        Map<String, List<Record>> doubles=new HashMap<>();
        doubles.put("doubleDouble", new ArrayList<>());
        doubles.put("tripleDouble", new ArrayList<>());
        doubles.put("quadroDouble", new ArrayList<>());
        doubles.put("pentaDouble", new ArrayList<>());
        doubles.put("twenty", new ArrayList<>());
        doubles.put("fifth", new ArrayList<>());
        Map<String, List<Record>> records=new HashMap<>();
//        Stats.initialize(records, Player.class.getName());
        games.forEach(game -> {
            if (game.getAwayTeam().getName().equals(country)) {
                addStat(game, game.getAwayTeam(), players, doubles, records);
            } else {
                addStat(game, game.getHomeTeam(), players, doubles, records);
            }
        });
        return new PlayerResponse(players, doubles, records);
    }

    private void addStat(Game game, Team team, List<Player> players, Map<String, List<Record>> doubles, Map<String, List<Record>> records) {
        team.getPlayers().forEach(player -> {
            Player existPlayer=players.stream().filter(player1 -> player1.equals(player)).findFirst().orElse(null);
            if (existPlayer != null) {
                existPlayer.getStats().forEach((s, aDouble) -> existPlayer.getStats().
                        replace(s,existPlayer.getStats().get(s)+player.getStats().get(s)));
            }
            else 
                players.add(player);
            player.getStats().forEach((s, aDouble) -> {
                if (!s.equals("doubleDouble") && !s.equals("games") && !s.equals("fouls") && !s.equals("turnovers") && !s.equals("minutes")) {
                    if ((records.get(s) == null || aDouble > records.get(s).get(0).getNumbers()))
                        records.put(s, new ArrayList<>(Collections.singletonList(new Record(aDouble, player, game))));
                    else if (aDouble.equals(records.get(s).get(0).getNumbers())){
                        List<Record> list=records.get(s);
                        list.add(new Record(aDouble, player, game));
                        records.put(s, list);
                    }
                }
            });
            int[] c=isDoubles(player.getStats());
            List<Record> doubleList;
            switch (c[0]){
                case 2:
                    doubleList=doubles.get("doubleDouble");
                    doubleList.add(new Record(0., player, game));
                    doubles.replace("doubleDouble", doubleList);
                    break;
                case 3:
                    doubleList=doubles.get("tripleDouble");
                    doubleList.add(new Record(0., player, game));
                    doubles.replace("tripleDouble", doubleList);
                    break;
                case 4:
                    doubleList=doubles.get("quadroDouble");
                    doubleList.add(new Record(0., player, game));
                    doubles.replace("quadroDouble", doubleList);
                    break;
                case 5:
                    doubleList=doubles.get("pentaDouble");
                    doubleList.add(new Record(0., player, game));
                    doubles.replace("pentaDouble", doubleList);
                    break;
            }
            if (c[1]>=2){
                doubleList=doubles.get("twenty");
                doubleList.add(new Record(0., player, game));
                doubles.replace("twenty", doubleList);
            }
            if (c[2]>=4){
                doubleList=doubles.get("fifth");
                doubleList.add(new Record(0., player, game));
                doubles.replace("fifth", doubleList);
            }
        });
    }

    private int[] isDoubles(Map<String, Double> stats) {
        final int[] c = {0, 0, 0};
        stats.forEach((s, aDouble) -> {
            if (s.equals("points") || s.equals("rebounds") || s.equals("assists") || s.equals("steals") || s.equals("blocks")) {
                c[0] += (aDouble >= 10 ? 1 : 0);
                c[1] += (aDouble >= 20 ? 1 : 0);
                c[2] += (aDouble >= 5 ? 1 : 0);
            }
        });
        return c;
    }
}
