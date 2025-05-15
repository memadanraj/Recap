package com.recap.Recap.Service.Classes;

import com.recap.Recap.DTO.TranscribeResultDTO;
import com.recap.Recap.Entity.AssemblyAiEntity;
import com.recap.Recap.Entity.MeetingInfoEntity;
import com.recap.Recap.Repos.AssemblyAiRepo;
import com.recap.Recap.Repos.MeetingInfoRepo;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class AssemblyAiMeetingService {

    @Autowired
    private AssemblyAiRepo assemblyAiRepo;

    @Autowired
    private MeetingInfoRepo meetingInfoRepo;

    @Transactional
    public void saveTranscribeInfo(List<TranscribeResultDTO> response, Long currentMeetingId)  {

        MeetingInfoEntity meetingInfoEntity = meetingInfoRepo.findById(currentMeetingId)
                .orElseThrow(()-> new RuntimeException("Meeting Id Not Found"));

        List<AssemblyAiEntity> resultDTO = response.stream().map(saveResponse -> {
            AssemblyAiEntity add = new AssemblyAiEntity();
            add.setSpeaker(saveResponse.Speaker());
            add.setSpeakerText(saveResponse.SpeakerText());
            add.setStartTime(saveResponse.startTime());
            add.setEndTime(saveResponse.endTime());
            add.setMeetingInfoEntity(meetingInfoEntity);
            return add;

        }).toList();
        assemblyAiRepo.saveAll(resultDTO);

    }
}
