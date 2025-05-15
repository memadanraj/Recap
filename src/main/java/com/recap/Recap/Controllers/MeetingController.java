package com.recap.Recap.Controllers;

import com.recap.Recap.Entity.AssemblyAiEntity;
import com.recap.Recap.Impl.GoogleMeetAutoImpl;
import com.recap.Recap.Impl.MeetingInstanceImpl;
import com.recap.Recap.Repos.AssemblyAiRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class MeetingController {

    @Autowired
    private MeetingInstanceImpl meetInstance;
    @Autowired
    private GoogleMeetAutoImpl googleMeetAuto;


    /*

    for zoom
 https://us04web.zoom.us = 23
for ms team
https://teams.microsoft= 23
google meet
https://meet.google.com = 23
     */
@Autowired
private AssemblyAiRepo  assemblyAiRepo;

    @PostMapping
    public ResponseEntity<?> joinMeeting(@RequestParam String url){

        try{
            if (url.startsWith("https://us04web.zoom.us"))
            {
                System.out.println("zoom things ");
                meetInstance.joinMeeting(url);
            }
            else if (url.startsWith("https://teams.microsoft"))
            {
                System.out.println("MS Team Body");
                meetInstance.joinMeeting(url);
            }
            else if(url.startsWith("https://meet.google.com"))
            {
                System.out.println("Google meet");
//                meetInstance.joinMeeting(url);
                googleMeetAuto.joinMeet(url);
            }
            else {
                throw new RuntimeException("Invalid Meeting Link ");
            }



            return new ResponseEntity<>(HttpStatus.OK);
        }catch (Exception e) {
            System.out.println(e);
            return  new ResponseEntity<>(e ,HttpStatus.BAD_REQUEST  );
        }


    }

    @GetMapping("/getMeetingTranscribe")
    public ResponseEntity<?> getMeetingTranscribe(){

        try{

            return new ResponseEntity<>( assemblyAiRepo.findAll(), HttpStatus.OK);
        }catch(Exception e){
            return  new ResponseEntity<>(e.getMessage(),HttpStatus.BAD_REQUEST);
        }
    }



}
