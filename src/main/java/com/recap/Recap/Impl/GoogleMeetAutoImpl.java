package com.recap.Recap.Impl;

import com.recap.Recap.Entity.MeetingInfoEntity;
import com.recap.Recap.Repos.MeetingInfoRepo;
import com.recap.Recap.Service.Classes.FfmpegRecordingService;
import com.recap.Recap.Service.Classes.MeetingInfoService;
import com.recap.Recap.Service.GoogleMeetAuto;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalTime;

@Service
public class GoogleMeetAutoImpl implements GoogleMeetAuto {


    @Autowired
    private  MeetingInstanceImpl meetingInstance;

    @Autowired
    private FfmpegRecordingService ffmpegRecordingService;

    @Autowired
    private MeetingInfoService meetingInfoService;


    @Override
    public String joinMeet(String url) {
        try {

            WebDriver driver = meetingInstance.joinMeeting(url);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

            driver.findElement(By.
                    xpath("//*[@id=\"yDmH0d\"]/div[3]/span/div[2]/div/div/div[2]/div/button/span[6]")).click(); // for sigunup popup
            driver.findElement(By.
                    cssSelector("#yDmH0d > c-wiz > div > div > div.TKU8Od > div.crqnQb > div > div.gAGjv > div.vgJExf > div > div > div.ZUpb4c > div.oORaUb.NONs6c > div > div.EhAUAc > div.utiQxe > div > div > div > div.U26fgb.JRY2Pb.mUbCce.kpROve.yBiuPb.y1zVCf.Fjnqyb.EZxqsf.M9Bg4d")).click(); //for mic off
            driver.findElement(By.
                    cssSelector("#yDmH0d > c-wiz > div > div > div.TKU8Od > div.crqnQb > div > div.gAGjv > div.vgJExf > div > div > div.ZUpb4c > div.oORaUb.NONs6c > div > div.EhAUAc > div.Pr6Uwe > div > div > div > div > div.U26fgb.JRY2Pb.mUbCce.kpROve.yBiuPb.y1zVCf.Fjnqyb.EZxqsf.M9Bg4d")).click(); //for camera off
            WebElement element = driver.findElement(By.xpath("//*[@id='c12'] | //*[@id='c14'] | //*[@id='c8']") );

            element.click();
            element.sendKeys("Test");
            wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#yDmH0d > c-wiz > div > div > div.TKU8Od > div.crqnQb > div > div.gAGjv > div.vgJExf > div > div > div.d7iDfe.NONs6c > div.shTJQe > div.jtn8y > div.XCoPyb > div > div > span > div.VfPpkd-dgl2Hf-ppHlrf-sM5MNb > button > span.UywwFc-vQzf8d"))).click(); // ask to join button

            Thread.sleep(20000);
            String title = driver.getTitle();
            String Hwnd = meetingInstance.getChromeHWND(title);
            MeetingInfoEntity savedMeetingInfo = meetingInfoService.saveMeetingInfo(driver);
            Long meetingId= savedMeetingInfo.getMeetingId();
            ffmpegRecordingService.setMeetingId(meetingId);
            ffmpegRecordingService.startFFmpegRecording(Hwnd);


            // Start monitoring in a daemon thread
            Thread monitoringThread = new Thread(() -> monitorParticipants(driver));
            monitoringThread.setDaemon(true);
            monitoringThread.start();

            // Keep main thread alive while monitoring
            while (monitoringThread.isAlive()) {
                Thread.sleep(1000);
            }
        }catch (Exception e){
            System.out.println(e);
            throw new RuntimeException(e.getMessage());

        }
        return url;
    }
    public  int getParticipantCount(WebDriver driver) {
        try {
            // Adjust the selector based on Google Meet's current UI
            WebElement countElement = driver.findElement(By.cssSelector("div.gFyGKf.BN1Lfc div.uGOf1d"));
            return Integer.parseInt(countElement.getText());
        } catch (Exception e) {
            return 1; // Fallback if element not found
        }
    }

    public  void monitorParticipants(WebDriver driver) {
        final long TIMEOUT = 10000; // 5 seconds
        long aloneStart = 0;
        boolean shouldRun = true;

        while (shouldRun) {
            try {
                int count = getParticipantCount(driver);
                System.out.println("Current participants: " + count);

                if (count <= 1) { // Adjust if bot is counted as participant
                    if (aloneStart == 0) {
                        aloneStart = System.currentTimeMillis();
                        System.out.println("Alone detection started");
                    } else if (System.currentTimeMillis() - aloneStart >= TIMEOUT) {
                        System.out.println("Alone timeout reached");
                        shouldRun = false;
                    }
                } else {
                    aloneStart = 0;
                    System.out.println("Participants detected, resetting timer");
                }

                Thread.sleep(1000); // Check every second
            } catch (InterruptedException e) {
                System.out.println("Monitoring interrupted");
                Thread.currentThread().interrupt();
                shouldRun = false;
            } catch (Exception e) {
                System.err.println("Monitoring error: " + e.getMessage());
                shouldRun = false;
            }
        }

        cleanupResources(driver);
    }

    private  synchronized void cleanupResources(WebDriver driver) {
        try {

            System.out.println("Cleaning up resources...");

            // Stop recording first
            ffmpegRecordingService.stopFFmpegRecording();
            Thread.sleep(2000); // Allow time for file finalization

            // Then close browser
            if (driver != null) {
                driver.quit();
                System.out.println("Browser closed");
            }
        } catch (Exception e) {
            System.err.println("Cleanup error: " + e.getMessage());
        }
    }





}
