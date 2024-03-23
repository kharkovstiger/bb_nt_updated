package com.example.bb_nt.service;

import org.springframework.http.ResponseEntity;

public interface BBAPIService {
    String getBoxScore(String id, String login, String code);

    ResponseEntity<String> login(String login, String code, Integer info);

    String getPlayer(String id, String login, String code);

    String getSeasons(String login, String code);
}
