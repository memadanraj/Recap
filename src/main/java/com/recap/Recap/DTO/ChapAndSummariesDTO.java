package com.recap.Recap.DTO;

import java.util.List;
import java.util.Optional;

public record ChapAndSummariesDTO(
        List<TranscribeResultDTO>  transcribeResult,
        String summaries,
        String topic
) {
}
