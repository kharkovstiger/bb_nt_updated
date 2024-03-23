package com.example.bb_nt.repository;


import com.example.bb_nt.model.Player;

import java.util.List;

public interface PlayerRepository {
    
    Player save(Player player);
    
    Player findOne(String id);
    
    List<Player> getAllFromCountry(String country);

    List<Player> getAllFromCountryMinGames(String country);

    List<Player> getAllMinGames(boolean u21);
}
