package com.example.bb_nt.model.utils;

import com.example.bb_nt.model.Game;
import com.example.bb_nt.model.Player;
import lombok.Data;

@Data
public class Record {
    private Double numbers;
    private Player player;
    private Game game;

    public Record(Double numbers, Player player, Game game) {
        this.numbers = numbers;
        this.player = player;
        this.game = game;
    }
}
