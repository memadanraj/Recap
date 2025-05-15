package com.recap.Recap.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


@Entity
@Table(name = "transcribeInfo")
@Getter
@Setter
public class AssemblyAiEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long meetingTranscibeId;

    @Column(name = "speaker")
    private String Speaker;

    @Column(name="SpeakerText", columnDefinition = "TEXT")
    private String SpeakerText;

    @Column(name= "startTime")
    private String startTime;

    @Column(name= "endTime")
    private String endTime;

    @ManyToOne
    @JoinColumn(name = "meetingId", referencedColumnName = "meetingId")
    private MeetingInfoEntity meetingInfoEntity;



}
