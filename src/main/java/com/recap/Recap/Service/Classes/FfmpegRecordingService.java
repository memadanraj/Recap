package com.recap.Recap.Service.Classes;

import com.recap.Recap.DTO.ChapAndSummariesDTO;
import com.recap.Recap.DTO.TranscribeResultDTO;
import com.recap.Recap.Entity.AssemblyAiEntity;
import com.recap.Recap.Impl.AssemblyAiIMPL;
import com.recap.Recap.Repos.AssemblyAiRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class FfmpegRecordingService {

    private static final String OUTPUT_DIR = "recordings";
    private Long currentMeetingId;
    private static Process ffmpegProcess;

    @Autowired
    private AssemblyAiIMPL assemblyAiIMPL;
    @Autowired
    private AssemblyAiMeetingService assemblyAiMeetingService;
   @Autowired
   private MeetingInfoService meetingInfoService;


    public  void startFFmpegRecording(String hwnd ) throws IOException {

        Files.createDirectories(Paths.get(OUTPUT_DIR));


        System.out.println(" start rec fun called....");
        String os = System.getProperty("os.name").toLowerCase();
        String[] ffmpegCmd;

        if (os.contains("win")) {
            // Windows: Record Chrome window + system audio
            ffmpegCmd = new String[]{

                    "ffmpeg",
                    "-f", "gdigrab",
                    "-framerate", "30",
                    "-y",
                    "-i", "hwnd=" + hwnd, // Use HWND here
                    "-f", "dshow",
                    "-i", "audio=Stereo Mix (Realtek Audio)",
                    "-c:v", "libx264",
                    "-crf", "23",
                    "-preset", "fast",
                    "-pix_fmt", "yuv420p",
                    "-c:a", "aac",
                    OUTPUT_DIR +"/chrome_recording.mp4"};
        } else {
            throw new UnsupportedOperationException("Unsupported OS");
        }

        // Log the FFmpeg command to check it
        System.out.println("FFmpeg command: " + String.join(" ", ffmpegCmd));


        // Start FFmpeg process
        ffmpegProcess = new ProcessBuilder(ffmpegCmd).redirectErrorStream(true).redirectOutput(new File(OUTPUT_DIR + "/ffmpeg-log.txt")) //
                .start();

        // Check if the process is running
        if (ffmpegProcess.isAlive()) {
            System.out.println("FFmpeg is recording...");
        } else {
            System.out.println("FFmpeg did not start successfully.");
        }
    }

    public  void stopFFmpegRecording() {

        System.out.println("Stopping FFmpeg recording...");

        if (ffmpegProcess != null) {
            try {
                // 1. Check if FFmpeg is still running before sending 'q'
                if (ffmpegProcess.isAlive()) {
                    try {
                        OutputStream outputStream = ffmpegProcess.getOutputStream();
                        outputStream.write("q\n".getBytes());
                        outputStream.flush();
                        outputStream.close();  // Close output stream properly
                        System.out.println("Sent quit command to FFmpeg");
                    } catch (IOException e) {
                        System.err.println("Warning: Couldn't send quit command, closing directly");
                    }

                    // 2. Wait for FFmpeg to exit gracefully
                    if (!ffmpegProcess.waitFor(10, TimeUnit.SECONDS)) {
                        System.err.println("FFmpeg didn't exit gracefully, forcing termination...");
                        ffmpegProcess.destroyForcibly();
                        ffmpegProcess.waitFor(); // Ensure cleanup
                    }
                }

                // 3. Ensure we extract audio even if FFmpeg exited early
                if (new File(OUTPUT_DIR + "/chrome_recording.mp4").exists()) {
                    AudioExtract();
                    Thread.sleep(2000);
                    ChapAndSummariesDTO results = assemblyAiIMPL.transcribeAudio(OUTPUT_DIR);
                    if (currentMeetingId != null) {
                        assemblyAiMeetingService.saveTranscribeInfo(results.transcribeResult(), currentMeetingId);
                        meetingInfoService.updateMeetingSummariesAndCHapter(results.summaries(),currentMeetingId,results.topic());
                    } else {
                        System.err.println("No meeting ID found for transcription!");
                    }


                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Interrupted during FFmpeg shutdown");
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                ffmpegProcess = null;// Ensure process reference is cleared
            }
        } else {
            System.out.println("FFmpeg process is already null. Nothing to stop.");
        }
    }



    //AUDIO EXTRACT FUNCTION HERE

    private  void AudioExtract() throws IOException {
        try {
            String[] ffmpegCmd = {
                    "ffmpeg",
                    "-i", OUTPUT_DIR + "/chrome_recording.mp4",
                    "-vn",         // No video
                    "-acodec", "libmp3lame",
                    "-q:a", "2",   // Audio quality (0-9, 0=best)
                    "-y",         // Overwrite output
                    OUTPUT_DIR + "/chrome_recording.mp3"
            };

            Process audioProcess = new ProcessBuilder(ffmpegCmd)
                    .redirectErrorStream(true)
                    .redirectOutput(new File(OUTPUT_DIR + "/audio-extract-log.txt"))
                    .start();

            if (audioProcess.waitFor(10, TimeUnit.SECONDS)) {
                System.out.println("Audio extraction " + (audioProcess.exitValue() == 0 ? "succeeded" : "failed"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setMeetingId(Long meetingId) {
        this.currentMeetingId = meetingId;
    }
}
