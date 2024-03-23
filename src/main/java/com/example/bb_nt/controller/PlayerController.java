package com.example.bb_nt.controller;

import com.example.bb_nt.model.Player;
import com.example.bb_nt.model.utils.OffensiveTactic;
import com.example.bb_nt.model.utils.PlayerResponse;
import com.example.bb_nt.model.utils.Position;
import com.example.bb_nt.model.utils.StatRequest;
import com.example.bb_nt.service.PlayerService;
import jakarta.websocket.server.PathParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = PlayerController.REST_URL, produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin
public class PlayerController {
    static final String REST_URL = "api/player";
    private final PlayerService playerService;

    @Autowired
    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @PostMapping(value = "/getAllFromCountry")
    public List<Player> getAllFromCountry(@RequestBody String country){
        return playerService.getAllFromCountry(country); 
    }

    @PostMapping(value = "/getAllFromCountryForGame")
    public List<Player> getAllFromCountryForGame(@RequestBody String country){
        return playerService.getAllFromCountryForGame(country);
    }

    @GetMapping(value = "/getAllForGame")
    public List<Player> getAllForGame(@PathParam("u21") boolean u21){
        return playerService.getAllForGame(u21);
    }

    @PostMapping(value = "/getAllFromCountryForMinutes")
    public List<Player> getAllFromCountryForMinutes(@RequestBody String country){
        return playerService.getAllFromCountryForMinutes(country);
    }

    @GetMapping(value = "/getAllForMinutes")
    public List<Player> getAllForMinutes(@PathParam("u21") boolean u21){
        return playerService.getAllForMinutes(u21);
    }
    
    @PostMapping(value = "/getPlayersStatForGameList")
    public PlayerResponse getPlayersStatForGameList(@RequestBody StatRequest request){
        return playerService.getPlayersStatForGameList(request.getGames(), request.getCountry());
    }

    @PostMapping(value = "/getPlayersStatForGameListPerGame")
    public List<Player> getPlayersStatForGameListPerGame(@RequestBody StatRequest request){
        PlayerResponse playerResponse=playerService.getPlayersStatForGameList(request.getGames(), request.getCountry());
        playerResponse.setPlayers(playerService.getAverages(playerResponse.getPlayers(), "game"));
        return playerResponse.getPlayers();
    }

    @PostMapping(value = "/getPlayersStatForGameListPerMinutes")
    public List<Player> getPlayersStatForGameListPerMinutes(@RequestBody StatRequest request){
        PlayerResponse playerResponse=playerService.getPlayersStatForGameList(request.getGames(), request.getCountry());
        playerResponse.setPlayers(playerService.getAverages(playerResponse.getPlayers(), "minutes"));
        return playerResponse.getPlayers();
    }

    @PostMapping(value = "/offTactics/{playerId}")
    public Map<OffensiveTactic, Map<String, Double>> getStatsForOffensiveTacticsForGameList(@RequestBody StatRequest request, @PathVariable String playerId){
        return playerService.getPlayerStatsForOffensiveTactics(request.getGames(), request.getCountry(), playerId);
    }

    @PostMapping(value = "/position/{playerId}")
    public Map<Position, Map<String, Double>> getStatsForPositionForGameList(@RequestBody StatRequest request, @PathVariable String playerId){
        return playerService.getPlayerStatsForPosition(request.getGames(), request.getCountry(), playerId);
    }

    @PostMapping(value = "/position/offTactics/{playerId}")
    public Map<Position, Map<OffensiveTactic, Map<String, Double>>> getStatsForPositionForOffensiveTacticsForGameList(@RequestBody StatRequest request,
                                                                                                                      @PathVariable String playerId){
        return playerService.getStatsForPositionForOffensiveTactics(request.getGames(), request.getCountry(), playerId);
    }

    @PostMapping(value = "/offTactics/position/{playerId}")
    public Map<OffensiveTactic, Map<Position, Map<String, Double>>> getStatsForOffensiveTacticsForPositionForGameList(@RequestBody StatRequest request,
                                                                                                                      @PathVariable String playerId){
        return playerService.getStatsForOffensiveTacticsForPosition(request.getGames(), request.getCountry(), playerId);
    }
}
