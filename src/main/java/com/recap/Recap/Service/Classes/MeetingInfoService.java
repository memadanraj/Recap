package com.recap.Recap.Service.Classes;

import com.recap.Recap.DTO.TranscribeResultDTO;
import com.recap.Recap.Entity.AssemblyAiEntity;
import com.recap.Recap.Entity.MeetingInfoEntity;
import com.recap.Recap.Impl.AssemblyAiIMPL;
import com.recap.Recap.Repos.MeetingInfoRepo;
import jakarta.transaction.Transactional;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.Set;

@Service
public class MeetingInfoService {

    @Autowired
    private MeetingInfoRepo meetingInfoRepo;


    @Transactional
    public  MeetingInfoEntity saveMeetingInfo(WebDriver driver)  {
        MeetingInfoEntity add = new MeetingInfoEntity();
        String meetingName= driver.getTitle();
        String time= String.valueOf(LocalTime.now());
        if(meetingName!=null){
            add.setMeetingType(driver.getTitle().substring(0,4));

            add.setMeetingName(meetingName.substring(0,4).concat(time));
        }
        else throw new RuntimeException("Meeting Title cannot found");

        return meetingInfoRepo.save(add);

    }

    @Transactional
    public void  updateMeetingSummariesAndCHapter (String summaries , Long meetingId, String topicDis){
        MeetingInfoEntity add = meetingInfoRepo.findById(meetingId)
                .orElseThrow(()-> new RuntimeException("Meeting Id Not Found"));
        add.setSummary(summaries);
        add.setTopicDiscussion(topicDis);
        meetingInfoRepo.save(add);

    }
}
