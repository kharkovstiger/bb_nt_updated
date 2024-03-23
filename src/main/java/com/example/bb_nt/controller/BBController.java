package com.example.bb_nt.controller;

import com.example.bb_nt.service.BBService;
import jakarta.websocket.server.PathParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;


@RestController
@RequestMapping(value = BBController.REST_URL, produces = MediaType.TEXT_HTML_VALUE)
@CrossOrigin
public class BBController {

    static final String REST_URL = "/api/bb";
    public static final String BASE_URL = "http://buzzerbeater.com";
    private RestTemplate restTemplate=new RestTemplate();
    private final BBService bbService;

    @Autowired
    public BBController(BBService bbService) {
        this.bbService = bbService;
    }

    @GetMapping(value = "/country/{country}")
    public ResponseEntity<String> country(@PathVariable("country") Integer country){

        ResponseEntity<String> responseJson=
                restTemplate.getForEntity(BASE_URL + "/country/"+country+"/overview.aspx", String.class);

        return new ResponseEntity<>(responseJson.getBody(), HttpStatus.OK);
    }

    @GetMapping(value = "/cup")
    public ResponseEntity<String> cup(@PathParam("season") String season, @PathParam("country") Integer country){

        ResponseEntity<String> responseJson=
                restTemplate.getForEntity(BASE_URL + "/country/"+country+"/cup.aspx?season="+season, String.class);

        return new ResponseEntity<>(responseJson.getBody(), HttpStatus.OK);
    }

    @GetMapping(value = "/ntschedule")
    public ResponseEntity<String> ntshcedule(@PathParam("season") String season, @PathParam("junior") Boolean jun){

        ResponseEntity<String> responseJson;
        if (jun==null)
            jun=false;
        String nt=jun?"jnt":"nt";
        if (season!=null){
            responseJson =
                    restTemplate.getForEntity(BASE_URL + "/country/33/"+nt+"/schedule.aspx?season="+season, String.class);
        }
        else {
            responseJson =
                    restTemplate.getForEntity(BASE_URL + "/country/33/"+nt+"/schedule.aspx", String.class);
        }
        return new ResponseEntity<>(responseJson.getBody(), HttpStatus.OK);
    }

    @GetMapping(value = "/game")
    public ResponseEntity<String> game(@PathParam("id") Integer id){
        return new ResponseEntity<>(bbService.getBoxScore(id), HttpStatus.OK);
    }
}
