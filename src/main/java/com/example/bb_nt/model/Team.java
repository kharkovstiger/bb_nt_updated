package com.example.bb_nt.model;

import com.example.bb_nt.model.utils.DefensiveTactic;
import com.example.bb_nt.model.utils.OffensiveTactic;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class Team {
    private String name;
    private List<Player> players;
    private Map<String, Double> stats;
    private OffensiveTactic offensiveTactic;
    private DefensiveTactic defensiveTactic;

    public Team() {

    }

    public void addPlayer(Player player){
        players.add(player);
    }
}
