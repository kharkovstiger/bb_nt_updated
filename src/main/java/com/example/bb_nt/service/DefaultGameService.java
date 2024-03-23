package com.example.bb_nt.service;

import com.example.bb_nt.model.Game;
import com.example.bb_nt.model.Player;
import com.example.bb_nt.model.Team;
import com.example.bb_nt.model.utils.*;
import com.example.bb_nt.repository.GameRepository;
import org.apache.xerces.dom.DeferredElementImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DefaultGameService implements GameService {

    private static final String LOGIN = "lnrstgr";
    private static final String CODE = "katana";
    private final GameRepository gameRepository;
    private final BBAPIService bbapiService;
    private final BBService bbService;
    private final PlayerService playerService;
    private final DateTimeFormatter FORMATTER=DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    public DefaultGameService(GameRepository gameRepository, BBAPIService bbapiService, BBService bbService, PlayerService playerService) {
        this.gameRepository = gameRepository;
        this.bbapiService = bbapiService;
        this.bbService = bbService;
        this.playerService = playerService;
    }

    @Override
    public Game parseBoxScore(String response) {
        Game game=new Game();
        Document doc = getDocument(response);
        NodeList timeNodeList = doc.getElementsByTagName("startTime");
        if (timeNodeList == null || timeNodeList.getLength() == 0) {
            return null;
        }
        String time = timeNodeList.item(0).getTextContent().trim();
        LocalDateTime dateTime = LocalDateTime.parse(time.substring(0, time.length() - 1));
        LocalDate date = dateTime.toLocalDate();
        game.setDate(date);
        game.setSeason(getSeason(date));

        NodeList awayTeamNL = doc.getElementsByTagName("awayTeam");
        NodeList homeTeamNL = doc.getElementsByTagName("homeTeam");

        if (awayTeamNL.getLength() == 0 || homeTeamNL.getLength() == 0) {
            return null;
        }

        Team awayTeam=new Team();
        setTeamStats(awayTeam, (DeferredElementImpl) awayTeamNL.item(0));
        setPlayersStats(awayTeam, (DeferredElementImpl) awayTeamNL.item(0));
        game.setAwayTeam(awayTeam);

        Team homeTeam=new Team();
        setTeamStats(homeTeam, (DeferredElementImpl) homeTeamNL.item(0));
        setPlayersStats(homeTeam, (DeferredElementImpl) homeTeamNL.item(0));
        game.setHomeTeam(homeTeam);

        ArrayList<Double> score=new ArrayList<>();
        score.add(awayTeam.getStats().get("points"));
        score.add(homeTeam.getStats().get("points"));

        String type = getDocument(response).getElementsByTagName("match").item(0).getAttributes().item(2).getNodeValue();
        if (type.equals("nt.friendly")) {
            type = "Scrimmage";
        } else if (type.contains("tournament")) {
            type = "Consolation Tournament";
        } else if (type.contains("robin") || type.contains("inal")) {
            if (game.getSeason()%2==(game.getAwayTeam().getName().matches(".*\\d+.*")?0:1))
                type="Euro Champs";
            else
                type="World Champs";
        }
        game.setType(type);
        game.setScore(score);
        
        setTactics(homeTeam, awayTeam, response);
        return game;
    }

    private void setTactics(Team homeTeam, Team awayTeam, String response) {
        Document doc = getDocument(response);
        NodeList offensiveTactics = doc.getElementsByTagName("offStrategy");
        NodeList defensiveTactics = doc.getElementsByTagName("defStrategy");
        awayTeam.setOffensiveTactic(OffensiveTactic.fromString(offensiveTactics.item(0).getTextContent().trim()));
        homeTeam.setOffensiveTactic(OffensiveTactic.fromString(offensiveTactics.item(1).getTextContent().trim()));
        awayTeam.setDefensiveTactic(DefensiveTactic.fromString(defensiveTactics.item(0).getTextContent().trim()));
        homeTeam.setDefensiveTactic(DefensiveTactic.fromString(defensiveTactics.item(1).getTextContent().trim()));
    }

    @Override
    public int getSeason(LocalDate localDate) {
        String xml=bbapiService.getSeasons(LOGIN, CODE);
        Document doc=getDocument(xml);
        NodeList nodeList=doc.getElementsByTagName("season");
        for (int i = 0; i <nodeList.getLength()-1 ; i++) {
            String s=((DeferredElementImpl) nodeList.item(i)).getElementsByTagName("finish").item(0)
                    .getTextContent().split("T")[0];
            LocalDate date=LocalDate.parse(s, FORMATTER);
            if (!localDate.isAfter(date)){
                return Integer.parseInt(((DeferredElementImpl) nodeList.item(i)).getAttribute("id"));
            }
        }
        return Integer.parseInt(nodeList.item(nodeList.getLength()-1).getAttributes().getNamedItem("id").getTextContent());
    }

    private void setPlayersStats(Team team, DeferredElementImpl doc) {
        NodeList playersNL = doc.getElementsByTagName("player");
        if (playersNL.getLength() == 0) {
            return;
        }
        team.setPlayers(new ArrayList<>());
        for (int i = 0; i < playersNL.getLength(); i++) {
            Node playerNode = playersNL.item(i);
            String id = playerNode.getAttributes().item(0).getNodeValue();
            boolean u21=team.getName().split(" ")[team.getName().split(" ").length-1].equals("U21");
            Player player=new Player(id+(u21?"u21":""), team.getName());
            Map<String, Double> stats=new HashMap<>();
            Stats.initialize(stats, Player.class.getName());
            DeferredElementImpl playerDoc = (DeferredElementImpl) playerNode;
            stats.replace("games",1.);
            int minutes = 0;
            int minutesMax = 0;
            Position position = null;
            int posMinutes = Integer.parseInt(playerDoc.getElementsByTagName("PG").item(0).getTextContent().trim());
            minutes += posMinutes;
            if (posMinutes > minutesMax) {
                position = Position.PG;
                minutesMax = posMinutes;
            }
            posMinutes = Integer.parseInt(playerDoc.getElementsByTagName("SG").item(0).getTextContent().trim());
            minutes += posMinutes;
            if (posMinutes > minutesMax) {
                position = Position.SG;
                minutesMax = posMinutes;
            }
            posMinutes = Integer.parseInt(playerDoc.getElementsByTagName("SF").item(0).getTextContent().trim());
            minutes += posMinutes;
            if (posMinutes > minutesMax) {
                position = Position.SF;
                minutesMax = posMinutes;
            }
            posMinutes = Integer.parseInt(playerDoc.getElementsByTagName("PF").item(0).getTextContent().trim());
            minutes += posMinutes;
            if (posMinutes > minutesMax) {
                position = Position.PF;
                minutesMax = posMinutes;
            }
            posMinutes = Integer.parseInt(playerDoc.getElementsByTagName("C").item(0).getTextContent().trim());
            minutes += posMinutes;
            if (posMinutes > minutesMax) {
                position = Position.C;
            }

            stats.replace("minutes",Double.parseDouble(String.valueOf(minutes)));
            stats.replace("fieldGoals",Double.parseDouble(playerDoc.getElementsByTagName("fgm").item(0).getTextContent().trim()));
            stats.replace("fieldGoalsAttempts",Double.parseDouble(playerDoc.getElementsByTagName("fga").item(0).getTextContent().trim()));
            stats.replace("threePoints",Double.parseDouble(playerDoc.getElementsByTagName("tpm").item(0).getTextContent().trim()));
            stats.replace("threePointsAttempts",Double.parseDouble(playerDoc.getElementsByTagName("tpa").item(0).getTextContent().trim()));
            stats.replace("freeThrows",Double.parseDouble(playerDoc.getElementsByTagName("ftm").item(0).getTextContent().trim()));
            stats.replace("freeThrowsAttempts",Double.parseDouble(playerDoc.getElementsByTagName("fta").item(0).getTextContent().trim()));
//            stats.replace("plusMinus",q==1?Double.parseDouble(nList.item(6).getTextContent().trim()):0);
            stats.replace("offensiveRebounds",Double.parseDouble(playerDoc.getElementsByTagName("oreb").item(0).getTextContent().trim()));
            stats.replace("rebounds",Double.parseDouble(playerDoc.getElementsByTagName("reb").item(0).getTextContent().trim()));
            stats.replace("assists",Double.parseDouble(playerDoc.getElementsByTagName("ast").item(0).getTextContent().trim()));
            stats.replace("turnovers",Double.parseDouble(playerDoc.getElementsByTagName("to").item(0).getTextContent().trim()));
            stats.replace("steals",Double.parseDouble(playerDoc.getElementsByTagName("stl").item(0).getTextContent().trim()));
            stats.replace("blocks",Double.parseDouble(playerDoc.getElementsByTagName("blk").item(0).getTextContent().trim()));
            stats.replace("fouls",Double.parseDouble(playerDoc.getElementsByTagName("pf").item(0).getTextContent().trim()));
            stats.replace("points",Double.parseDouble(playerDoc.getElementsByTagName("pts").item(0).getTextContent().trim()));
            stats.replace("doubleDouble",isDD(stats)==2?1.:0.);
            player.setStats(stats);
            player.setPosition(position);
//            String playerString=bbapiService.getPlayer(id, LOGIN, CODE);
//            doc=getDocument(playerString);
            String firstName=playerDoc.getElementsByTagName("firstName").item(0).getTextContent().trim();
            String lastName=playerDoc.getElementsByTagName("lastName").item(0).getTextContent().trim();
            player.setFirstName(firstName);
            player.setLastName(lastName);
            team.addPlayer(player);
        }
    }

    private int isDD(Map<String, Double> stats) {
        final int[] c = {0};
        stats.forEach((s, aDouble) -> {
            if (s.equals("points") || s.equals("rebounds") || s.equals("assists") || s.equals("steals") || s.equals("blocks"))
                c[0] +=(aDouble >= 10 ? 1 : 0);
        });
        return c[0];
    }

    private Document getDocument(String s){
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;
        Document doc = null;
        try {
            dBuilder = dbFactory.newDocumentBuilder();
            doc = dBuilder.parse(new InputSource(new StringReader(s)));
            doc.getDocumentElement().normalize();
        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
        }
        return doc;
    }

    private void setTeamStats(Team team, DeferredElementImpl doc){
        NodeList nameNL = doc.getElementsByTagName("teamName");
        if (nameNL.getLength() == 0) {
            return;
        }
        team.setName(nameNL.item(0).getTextContent().trim());

        NodeList teamTotalsNL = doc.getElementsByTagName("teamTotals");
        if (teamTotalsNL.getLength() == 0) {
            return;
        }

        DeferredElementImpl teamStatsDoc = (DeferredElementImpl) teamTotalsNL.item(0);
        Map<String, Double> stats=new HashMap<>();
        Stats.initialize(stats,Team.class.getName());
        stats.replace("fieldGoals",Double.parseDouble(teamStatsDoc.getElementsByTagName("fgm").item(0).getTextContent().trim()));
        stats.replace("fieldGoalsAttempts",Double.parseDouble(teamStatsDoc.getElementsByTagName("fga").item(0).getTextContent().trim()));
        stats.replace("threePoints",Double.parseDouble(teamStatsDoc.getElementsByTagName("tpm").item(0).getTextContent().trim()));
        stats.replace("threePointsAttempts",Double.parseDouble(teamStatsDoc.getElementsByTagName("tpa").item(0).getTextContent().trim()));
        stats.replace("freeThrows",Double.parseDouble(teamStatsDoc.getElementsByTagName("ftm").item(0).getTextContent().trim()));
        stats.replace("freeThrowsAttempts",Double.parseDouble(teamStatsDoc.getElementsByTagName("fta").item(0).getTextContent().trim()));
        stats.replace("offensiveRebounds",Double.parseDouble(teamStatsDoc.getElementsByTagName("oreb").item(0).getTextContent().trim()));
        stats.replace("rebounds",Double.parseDouble(teamStatsDoc.getElementsByTagName("reb").item(0).getTextContent().trim()));
        stats.replace("assists",Double.parseDouble(teamStatsDoc.getElementsByTagName("ast").item(0).getTextContent().trim()));
        stats.replace("turnovers",Double.parseDouble(teamStatsDoc.getElementsByTagName("to").item(0).getTextContent().trim()));
        stats.replace("steals",Double.parseDouble(teamStatsDoc.getElementsByTagName("stl").item(0).getTextContent().trim()));
        stats.replace("blocks",Double.parseDouble(teamStatsDoc.getElementsByTagName("blk").item(0).getTextContent().trim()));
        stats.replace("fouls",Double.parseDouble(teamStatsDoc.getElementsByTagName("pf").item(0).getTextContent().trim()));
        stats.replace("points",Double.parseDouble(teamStatsDoc.getElementsByTagName("pts").item(0).getTextContent().trim()));
        team.setStats(stats);
    }

    @Override
    public Game save(Game game) {
        boolean exist=gameRepository.exists(game.getId());
        if (!exist){
            game.getAwayTeam().getPlayers().forEach(playerService::addStats);
            game.getHomeTeam().getPlayers().forEach(playerService::addStats);
        }
        return gameRepository.save(game);
    }

    @Override
    public List<Game> getAllGamesForCountry(String country, boolean official) {
        return gameRepository.getAllGamesForCountry(country, official);
    }

    @Override
    public List<Game> getAllGamesForCountryForSeason(String country, boolean official, Integer season) {
        return gameRepository.getAllGamesForCountryForSeason(country,official,season);
    }

    @Override
    public List<Game> getAllGamesForCountryAgainstCountry(String s, String s1, boolean official) {
        return gameRepository.getAllGamesForCountryAgainstCountry(s, s1, official);
    }

    @Override
    public List<Game> getGamesForList(List<String> ids) {
        return gameRepository.getGamesForList(ids);
    }

    @Override
    public String getMaxId(int season) {
        Game game=gameRepository.getMaxId(season);
        return game==null?"0":game.getId();
    }

    @Override
    public void addGame(Integer id) {
        String response=bbapiService.getBoxScore(String.valueOf(id), LOGIN, CODE);
        final boolean[] flag = {false};
        Countries.countries.forEach((s, integer) -> {
            if (response.contains(s))
                flag[0] =true;
        });
        if (!flag[0])
            return;
        Game game=parseBoxScore(response);
        game.setId(String.valueOf(id));
//        if (Countries.countries.contains(game.getAwayTeam().getName()) || Countries.countries.contains(game.getHomeTeam().getName()))
            save(game);
    }

    @Override
    public Map<String, Double> getSeasonStatisticsForCountry(String country, Integer season) {
        List<Game> games=gameRepository.getAllGamesForCountryForSeason(country, true, season);
        return getAveragedStatistics(games, country);
    }

    @Override
    public Map<String, Double> getAveragedStatistics(List<Game> games, String country) {
        Map<String, Double> stats=new HashMap<>();
        Stats.initialize(stats, Team.class.getName());
        stats.put("pointsAgainst",0.);
        stats.put("winRate",0.);
        games.forEach(game -> {
            if (game.getAwayTeam().getName().equals(country)) {
                addStat(game.getAwayTeam(), stats);
                stats.replace("pointsAgainst",stats.get("pointsAgainst")+ game.getScore().get(1));
                stats.replace("winRate",stats.get("winRate")+ (game.getScore().get(0)>game.getScore().get(1)?1.:0.));
            } else {
                addStat(game.getHomeTeam(), stats);
                stats.replace("pointsAgainst",stats.get("pointsAgainst")+ game.getScore().get(0));
                stats.replace("winRate",stats.get("winRate")+ (game.getScore().get(1)>game.getScore().get(0)?1.:0.));
            }
        });
        stats.forEach((s, aDouble) -> stats.replace(s,aDouble/games.size()));
        stats.put("wins", stats.get("winRate")*games.size());
        stats.put("games", (double) games.size());
        return stats;
    }

    @Override
    public List<Game> getAllGamesForSeason(int season) {
        return gameRepository.getAllGamesForSeason(season);
    }

    @Override
    public Results getResultsFromGameList(List<Game> games, String country) {
        Results results=new Results();
        games.forEach(game -> {
            if (game.getAwayTeam().getName().equals(country)) {
                int pos=game.getScore().get(0)>game.getScore().get(1)?0:1;
                addResult(results, pos, game.getType());
            } else {
                int pos=game.getScore().get(1)>game.getScore().get(0)?0:1;
                addResult(results, pos, game.getType());
            }
        });
        return results;
    }

    @Override
    public Game getLastInsertedGame() {
        return gameRepository.getLastInsertedGame();
    }

    @Override
    public Map<OffensiveTactic, Map<String, Double>> getStatsForOffensiveTacticsForGameList(List<Game> games, String country) {
        Map<OffensiveTactic, Map<String, Double>> result=new HashMap<>();
        for (int i = 0; i <OffensiveTactic.values().length ; i++) {
            int finalI = i;
            Map<String, Double> stat=getAveragedStatistics(games.stream().filter(g ->
                    country.equals(CollectionUtils.isEmpty(g.getAwayTeam().getPlayers())?"":g.getAwayTeam().getPlayers().get(0).getCountry())? 
                            g.getAwayTeam().getOffensiveTactic().equals(OffensiveTactic.values()[finalI]): 
                            g.getHomeTeam().getOffensiveTactic().equals(OffensiveTactic.values()[finalI])
            ).collect(Collectors.toList()), country);
            result.put(OffensiveTactic.values()[finalI], stat);
        }
        return result;
    }

    @Override
    public Map<DefensiveTactic, Map<String, Double>> getStatsForDefensiveTacticsForGameList(List<Game> games, String country) {
        Map<DefensiveTactic, Map<String, Double>> result=new HashMap<>();
        for (int i = 0; i <DefensiveTactic.values().length ; i++) {
            int finalI = i;
            Map<String, Double> stat=getAveragedStatistics(games.stream().filter(g ->
                    country.equals(CollectionUtils.isEmpty(g.getAwayTeam().getPlayers())?"":g.getAwayTeam().getPlayers().get(0).getCountry())?
                            g.getAwayTeam().getDefensiveTactic().equals(DefensiveTactic.values()[finalI]):
                            g.getHomeTeam().getDefensiveTactic().equals(DefensiveTactic.values()[finalI])
            ).collect(Collectors.toList()), country);
            result.put(DefensiveTactic.values()[finalI], stat);
        }
        return result;
    }

    private void addResult(Results results, int pos, String type){
        results.add(results.getAll(), pos);
        switch (type){
            case "Euro Champs":
                results.add(results.getContinental(), pos);
                break;
            case "Scrimmage":
                results.add(results.getScrimmage(), pos);
                break;
            case "World Champs":
                results.add(results.getWorld(), pos);
                break;
            case "Consolation Tournament":
                results.add(results.getCt(), pos);
                break;
        }
    }
    
    private void addStat(Team team, Map<String, Double> stats){
        team.getStats().forEach((s, aDouble) -> stats.replace(s, stats.get(s)+aDouble));
    }
}
