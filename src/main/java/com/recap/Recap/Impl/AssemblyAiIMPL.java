package com.recap.Recap.Impl;

import com.assemblyai.api.AssemblyAI;
import com.assemblyai.api.resources.transcripts.types.*;
import com.recap.Recap.DTO.ChapAndSummariesDTO;
import com.recap.Recap.DTO.TranscribeResultDTO;
import com.recap.Recap.Repos.AssemblyAiRepo;
import com.recap.Recap.Service.Assemblyai;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AssemblyAiIMPL implements Assemblyai {

    @Autowired
    private AssemblyAiRepo assemblyAiRepo;

    private final AssemblyAI assemblyAIClient;

    public AssemblyAiIMPL(AssemblyAI assemblyAI) {
       this.assemblyAIClient=assemblyAI;
    }

    @Override
    public ChapAndSummariesDTO transcribeAudio(String Outputfile) throws IOException {

        var params = TranscriptOptionalParams.builder()
                .speakerLabels(true)
                .speechModel(SpeechModel.NANO)
                .iabCategories(true)
                .summarization(true)
                .build();


        // You can use a local file:

        Transcript transcript = assemblyAIClient.transcripts().transcribe(
                new File(Outputfile + "/chrome_recording.mp3"), params);
        Optional<String> summariesInJson = transcript.getSummary();
        Optional<TopicDetectionModelResult> topicDisInList = transcript.getIabCategoriesResult();
        String topic = topicDisInList.map(result -> result.getSummary().toString())
                .orElseThrow(() -> new RuntimeException("No Topic Found"));
        System.out.println(topicDisInList);
        System.out.println(topic);
        String summaries = summariesInJson.orElseThrow(() ->new RuntimeException("No Summary Found "));

       return new ChapAndSummariesDTO(processTranscibe(transcript),summaries,topic);
    }

private List<TranscribeResultDTO> processTranscibe(Transcript transcript){
        return transcript.getUtterances()
                .orElseThrow(() ->new RuntimeException("No Utterances Found "))
                .stream().
                map(this::mapResultTranscribe).
                collect(Collectors.toList());
    }

    private TranscribeResultDTO mapResultTranscribe(TranscriptUtterance utterance){
        return new TranscribeResultDTO(
                utterance.getSpeaker(),
                utterance.getText(),
                timeStampFormat(utterance.getStart()),
                timeStampFormat(utterance.getEnd())

        );
    }





    private String timeStampFormat(float timestampMs) {
        long totalMs = (long) timestampMs;
        long hours = totalMs / 3600000;
        long minutes = (totalMs % 3600000) / 60000;
        long seconds = (totalMs % 60000) / 1000;
        long milliseconds = totalMs % 1000;

        return String.format("%02d:%02d:%02d,%03d",
                hours, minutes, seconds, milliseconds);


    }
}
