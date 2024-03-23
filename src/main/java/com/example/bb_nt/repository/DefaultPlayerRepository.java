package com.example.bb_nt.repository;

import com.example.bb_nt.model.Player;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class DefaultPlayerRepository implements PlayerRepository {
    
    private final PlayerCrudRepository playerCrudRepository;

    @Autowired
    public DefaultPlayerRepository(PlayerCrudRepository playerCrudRepository) {
        this.playerCrudRepository = playerCrudRepository;
    }

    @Override
    public Player save(Player player) {
        return playerCrudRepository.save(player);
    }

    @Override
    public Player findOne(String id) {
        Optional<Player> player = playerCrudRepository.findById(id);
        return player.orElse(null);
    }

    @Override
    public List<Player> getAllFromCountry(String country) {
        return playerCrudRepository.getAllFromCountry(country);
    }

    @Override
    public List<Player> getAllFromCountryMinGames(String country) {
        return playerCrudRepository.getAllFromCountryMinGames(country);
    }

    @Override
    public List<Player> getAllMinGames(boolean u21) {
//        String regex=u21?".*U21":"(?!.*U21.*).*";
        String regex=u21?".*\\d+.*":"[a-zA-Z ]+";
        List<Player> players=playerCrudRepository.getAllMinGames(regex);
        for (int i = 0; i <players.size() ; ){
            if (!players.get(i).getCountry().matches(regex)){
                players.remove(players.get(i));
            }
            else 
                i++;
        }
        return players;
    }
}
