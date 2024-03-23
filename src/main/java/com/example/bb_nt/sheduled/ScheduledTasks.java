package com.example.bb_nt.sheduled;

import com.example.bb_nt.model.Game;
import com.example.bb_nt.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Arrays;

@Component
public class ScheduledTasks {
    
    private final GameService gameService;
    private final int MIN_MAX_ID=373;

    @Autowired
    public ScheduledTasks(GameService gameService) {
        this.gameService = gameService;
    }

    @Scheduled(cron = "0 0 4 ? * 3")
    public boolean addGames(){
        Game lastInsertedGame=gameService.getLastInsertedGame();
        if (LocalDate.now().minusDays(7).isBefore(lastInsertedGame.getDate()))
            return true;
        int season=gameService.getSeason(LocalDate.now());
        final Integer maxId= Integer.valueOf(gameService.getMaxId(season));
        System.err.println("Begin to add new games");
        addingGames(maxId, maxId);
        return true;
    }

    private void addingGames(Integer maxId, Integer id) {
//        while (id-maxId<1500){
            try {
//                gameService.addGame(++id);
                gameService.addGame(id);
                System.err.println("Added game with ID: "+id);
            }
            catch (ArrayIndexOutOfBoundsException e){
                System.err.println(Arrays.toString(e.getStackTrace()));
            }
//        }
    }

    public boolean addGamesFromId(Integer id){
        int season=gameService.getSeason(LocalDate.now());
        final Integer maxId= Integer.valueOf(gameService.getMaxId(season));
        System.err.println("Begin to add new games");
        addingGames(maxId, id);
        return true;
    }
}
