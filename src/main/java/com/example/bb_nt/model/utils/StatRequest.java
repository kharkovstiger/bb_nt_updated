package com.example.bb_nt.model.utils;

import com.example.bb_nt.model.Game;
import lombok.Data;

import java.util.List;

@Data
public class StatRequest {
    
    private List<Game> games;
    private String country;
}
