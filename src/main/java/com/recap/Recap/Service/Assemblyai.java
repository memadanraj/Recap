package com.recap.Recap.Service;

import com.recap.Recap.DTO.ChapAndSummariesDTO;

import java.io.IOException;

public interface Assemblyai {

    ChapAndSummariesDTO transcribeAudio(String filepath) throws IOException;
}
