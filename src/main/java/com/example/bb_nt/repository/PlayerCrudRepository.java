package com.example.bb_nt.repository;

import com.example.bb_nt.model.Player;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PlayerCrudRepository extends MongoRepository<Player, String> {

    @Override
    Player save(Player player);

    @Override
    List<Player> findAll();

    @Override
    Optional<Player> findById(String s);
    
    @Query(value = "{'country':?0}")
    List<Player> getAllFromCountry(String country);

    @Query(value = "{'country':?0, 'stats.games':{$gt:4}}")
    List<Player> getAllFromCountryMinGames(String country);

    @Query(value = "{'country':{$regex:?0}, 'stats.games':{$gt:4}}")
    List<Player> getAllMinGames(String regex);
}
