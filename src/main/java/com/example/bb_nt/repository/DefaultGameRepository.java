package com.example.bb_nt.repository;

import com.example.bb_nt.model.Game;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.util.Comparator;
import java.util.List;

@Repository
public class DefaultGameRepository implements GameRepository {
    
    private final GameCrudRepository gameCrudRepository;

    @Autowired
    public DefaultGameRepository(GameCrudRepository gameCrudRepository) {
        this.gameCrudRepository = gameCrudRepository;
    }

    @Override
    public Game save(Game game) {
        return gameCrudRepository.save(game);
    }

    @Override
    public boolean exists(String id) {
        return gameCrudRepository.existsById(id);
    }

    @Override
    public List<Game> getAllGamesForCountry(String country, boolean official) {
        return official?gameCrudRepository.getAllOfficialGamesForCountry(country):gameCrudRepository.getAllGamesForCountry(country);
    }

    @Override
    public List<Game> getAllGamesForCountryForSeason(String country, boolean official, Integer season) {
        return official?gameCrudRepository.getAllOfficialGamesForCountryForSeason(country, season)
                :gameCrudRepository.getAllGamesForCountryForSeason(country, season);
    }

    @Override
    public List<Game> getAllGamesForCountryAgainstCountry(String s, String s1, boolean official) {
        return official?gameCrudRepository.getAllOfficialGamesForCountryAgainstCountry(s, s1)
                :gameCrudRepository.getAllGamesForCountryAgainstCountry(s, s1);
    }

    @Override
    public List<Game> getGamesForList(List<String> ids) {
        return gameCrudRepository.findAllById(ids);
    }

    @Override
    public Game getMaxId(int season) {
//        List<Game> games=gameCrudRepository.getAllGamesForSeason(season);
//        if (games==null || games.size()==0){
        List<Game> games=gameCrudRepository.getAllGamesForSeason(season-1);
            games.sort(Comparator.comparing(o -> Integer.valueOf(o.getId())));
            return CollectionUtils.isEmpty(games) ? null : games.get(games.size()-1);
//        }
//        games.sort(Comparator.comparing(o -> Integer.valueOf(o.getId())));
//        return games.get(0);
    }

    @Override
    public List<Game> getAllGamesForSeason(int season) {
        return gameCrudRepository.getAllGamesForSeason(season);
    }
    
    @Override
    public Game getLastInsertedGame(){
        return gameCrudRepository.findLastInsertedGame();
    }
}
