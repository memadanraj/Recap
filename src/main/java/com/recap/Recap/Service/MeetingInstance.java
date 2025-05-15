package com.recap.Recap.Service;

import org.openqa.selenium.WebDriver;

import java.io.IOException;

public interface MeetingInstance {
    WebDriver joinMeeting(String url) throws IOException;
    void StopMeeting();

}
