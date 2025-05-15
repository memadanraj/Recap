package com.recap.Recap.DTO;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import org.checkerframework.checker.units.qual.C;

public record TranscribeResultDTO(
        String Speaker,
        @Column(columnDefinition = "TEXT")
        String SpeakerText,
        String startTime,
        String endTime


) {
}
