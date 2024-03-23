package com.example.bb_nt.model.utils;

import com.example.bb_nt.model.Player;
import lombok.Data;

import java.util.Map;

@Data
public class Stats {
    
    public static void initialize(Map<String, Double> stats, String className){
        try {
            Class clas=Class.forName(className);
            if (clas.newInstance() instanceof Player){
                stats.put("games",0.);
                stats.put("minutes",0.);
                stats.put("plusMinus",0.);
                stats.put("doubleDouble",0.);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
        stats.put("fieldGoals",0.);
        stats.put("fieldGoalsAttempts",0.);
        stats.put("threePoints",0.);
        stats.put("threePointsAttempts",0.);
        stats.put("freeThrows",0.);
        stats.put("freeThrowsAttempts",0.);
        stats.put("offensiveRebounds",0.);
        stats.put("rebounds",0.);
        stats.put("assists",0.);
        stats.put("turnovers",0.);
        stats.put("steals",0.);
        stats.put("blocks",0.);
        stats.put("fouls",0.);
        stats.put("points",0.);
    }
}
