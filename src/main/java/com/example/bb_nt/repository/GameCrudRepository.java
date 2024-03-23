package com.example.bb_nt.repository;

import com.example.bb_nt.model.Game;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface GameCrudRepository extends MongoRepository<Game, String> {

    @Override
    Game save(Game game);

    @Override
    List<Game> findAll();

    @Override
    List<Game> findAllById(Iterable<String> strings);

    @Override
    Optional<Game> findById(String s);

    @Override
    boolean existsById(String s);

    @Query(value = "{$or:[{'homeTeam.name':?0}, {'awayTeam.name':?0}]}")
    List<Game> getAllGamesForCountry(String country);

    @Query(value = "{$or:[{'homeTeam.name':?0}, {'awayTeam.name':?0}], 'type':{$ne:'Scrimmage'}}")
    List<Game> getAllOfficialGamesForCountry(String country);

    @Query(value = "{$or:[{'homeTeam.name':?0}, {'awayTeam.name':?0}], 'season':?1, 'type':{$ne:'Scrimmage'}}")
    List<Game> getAllOfficialGamesForCountryForSeason(String country, Integer season);

    @Query(value = "{$or:[{'homeTeam.name':?0}, {'awayTeam.name':?0}], 'season':?1}")
    List<Game> getAllGamesForCountryForSeason(String country, Integer season);

    @Query(value = "{$or:[{'homeTeam.name':?0, 'awayTeam.name':?1}, {'homeTeam.name':?1, 'awayTeam.name':?0}], 'type':{$ne:'Scrimmage'}}")
    List<Game> getAllOfficialGamesForCountryAgainstCountry(String s, String s1);

    @Query(value = "{$or:[{'homeTeam.name':?0, 'awayTeam.name':?1}, {'homeTeam.name':?1, 'awayTeam.name':?0}]}")
    List<Game> getAllGamesForCountryAgainstCountry(String s, String s1);

    @Query(value = "{$sort:{'id':-1}, $limit:1}")
    Game findMaxId();

    @Query(value = "{'season':?0}")
    List<Game> getAllGamesForSeason(int season);

    @Query(value = "{ sort: { _id: 1 }, limit: 1 }")
    Game findLastInsertedGame();
}
