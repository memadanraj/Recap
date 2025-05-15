package com.recap.Recap.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Set;

@Entity
@Table(name = "MeetingInfo")
@Getter
@Setter
public class MeetingInfoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long meetingId;

    //HERE MEETING TYPE LIKE GOOGLE MEET / ZOOM / TEAM HERE
    private String meetingType;


    private String meetingName;

    @Column(name="Summary", columnDefinition = "TEXT")
    private String summary;

    @Column(name="topicDiscussion", columnDefinition = "TEXT")
    private String topicDiscussion;

    @CreationTimestamp
    private String createdAt;

//    private String token ;


    @OneToMany(mappedBy = "meetingInfoEntity" , cascade= CascadeType.ALL, orphanRemoval = true)
    private Set <AssemblyAiEntity> assemblyAiEntities;

    // Helper method to link child to parent
    public void addTranscript(AssemblyAiEntity transcript) {
        assemblyAiEntities.add(transcript);
        transcript.setMeetingInfoEntity(this); // Link child to parent
    }


}
