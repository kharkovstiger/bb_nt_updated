package com.example.bb_nt.controller;

import com.example.bb_nt.service.BBAPIService;
import jakarta.websocket.server.PathParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;

@RestController
@RequestMapping(value = BBAPIController.REST_URL, produces = MediaType.APPLICATION_XML_VALUE)
@CrossOrigin
public class BBAPIController {

    static final String REST_URL = "/api/bbapi";
    public static final String BASE_URL = "http://bbapi.buzzerbeater.com";
    private final RestTemplate restTemplate=new RestTemplate();
    private final BBAPIService bbapiService;

    @Autowired
    public BBAPIController(BBAPIService bbapiService) {
        this.bbapiService = bbapiService;
    }

    @GetMapping(value = "/login")
    public ResponseEntity<String> login(@PathParam("login") String login, @PathParam("code") String code, @PathParam("quickinfo") Integer quickinfo){
        if (quickinfo==null)
            quickinfo=0;
        return bbapiService.login(login, code, quickinfo);
    }

    @GetMapping(value = "/player")
    public ResponseEntity<String> player(@PathParam("id") String id, @PathParam("login") String login, @PathParam("code") String code){
        return new ResponseEntity<>(bbapiService.getPlayer(id, login, code),HttpStatus.OK);
    }

    @GetMapping(value = "/country")
    public ResponseEntity<String> country(@PathParam("login") String login, @PathParam("code") String code){
        HttpEntity<String> entity = new HttpEntity<>(login(login,code,0).getHeaders());

        ResponseEntity<String> responseJson =
                restTemplate.exchange(BASE_URL + "/countries.aspx", HttpMethod.GET, entity, String.class);

        return new ResponseEntity<>(responseJson.getBody(),HttpStatus.OK);
    }

    @GetMapping(value = "/league")
    public ResponseEntity<String> league(@PathParam("level") Integer level, @PathParam("login") String login, @PathParam("code") String code,
                                         @PathParam("country") Integer country){
        HttpEntity<String> entity = new HttpEntity<>(login(login,code,0).getHeaders());

        ResponseEntity<String> responseJson =
                restTemplate.exchange(BASE_URL + "/leagues.aspx?countryid="+country+"&level="+level, HttpMethod.GET, entity, String.class);

        return new ResponseEntity<>(responseJson.getBody(),HttpStatus.OK);
    }

    @GetMapping(value = "/standings")
    public ResponseEntity<String> standings(@PathParam("season") Integer season, @PathParam("leagueid") Integer leagueid, @PathParam("login") String login, @PathParam("code") String code){
        HttpEntity<String> entity = new HttpEntity<>(login(login,code,0).getHeaders());

        ResponseEntity<String> responseJson=null;
        if (season!=null){
            responseJson =
                    restTemplate.exchange(BASE_URL + "/standings.aspx?leagueid=" + leagueid+"&season="+season,
                            HttpMethod.GET, entity, String.class);
        }
        else {
            responseJson =
                    restTemplate.exchange(BASE_URL + "/standings.aspx?leagueid=" + leagueid,
                            HttpMethod.GET, entity, String.class);
        }
        return new ResponseEntity<>(responseJson.getBody(),HttpStatus.OK);
    }

    @GetMapping(value = "/team")
    public ResponseEntity<String> team(@PathParam("id") String id, @PathParam("login") String login, @PathParam("code") String code){
        HttpEntity<String> entity = new HttpEntity<>(login(login,code,0).getHeaders());

        ResponseEntity<String> responseJson =
                restTemplate.exchange(BASE_URL + "/teaminfo.aspx?teamid=" + id, HttpMethod.GET, entity, String.class);

        return new ResponseEntity<>(responseJson.getBody(),HttpStatus.OK);
    }

    @GetMapping(value = "/season")
    public ResponseEntity<String> season(@PathParam("login") String login, @PathParam("code") String code){
        return new ResponseEntity<>(bbapiService.getSeasons(login, code),HttpStatus.OK);
    }

    @GetMapping(value = "/boxscore")
    public ResponseEntity<String> boxscore(@PathParam("id") String id, @PathParam("login") String login, @PathParam("code") String code){
        return new ResponseEntity<>(bbapiService.getBoxScore(id, login, code),HttpStatus.OK);
    }
}
