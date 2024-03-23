package com.example.bb_nt.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;

@Data
@Document
public class Game implements Serializable{
    @Id
    private String id;
    private Team homeTeam;
    private Team awayTeam;
    private ArrayList<Double> score;
    private int season;
    private String type;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "UTC")
    private LocalDate date;
}
