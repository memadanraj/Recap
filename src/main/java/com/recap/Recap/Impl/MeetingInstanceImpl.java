package com.recap.Recap.Impl;

import com.recap.Recap.Service.Classes.FfmpegRecordingService;
import com.recap.Recap.Service.MeetingInstance;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class MeetingInstanceImpl implements MeetingInstance {

    @Autowired
    private FfmpegRecordingService ffmpegRecordingService;

    @Autowired
    private AssemblyAiIMPL assemblyAiIMPL;

    @Override
    public WebDriver joinMeeting(String url) {
        WebDriver driver = null;
        try {


            WebDriverManager.chromedriver().setup();

            ChromeOptions options = new ChromeOptions();
            // Simplify Chrome arguments
            options.addArguments("--disable-blink-features=AutomationControlled");

            options.addArguments("-use-fake-device-for-media-stream"); //fake media and mic stream
            options.addArguments("--auto-select-desktop-capture-source=Screen 1"); // Auto-selects the screen for recording
            options.addArguments("--use-fake-ui-for-media-stream"); // Skips permission dialogs
            options.addArguments("--enable-usermedia-screen-capturing"); // Enables screen recording
            options.addArguments("--allow-http-screen-capture"); // Allows recording in HTTP sites
            options.addArguments("--allow-file-access"); // Grants file access permissions
            options.addArguments("--start-maximized", "--disable-gpu",                     // Critical for screen capture
                    "--disable-software-rasterizer"    // Disable GPU fallback
            ); // Maximize window of a browser

            driver = new ChromeDriver(options);
            driver.manage().timeouts().pageLoadTimeout(30, TimeUnit.SECONDS);
            driver.get(url);

            // Wait for critical page elements
            new WebDriverWait(driver, Duration.ofSeconds(20))
                    .until(ExpectedConditions.jsReturnsValue("return document.readyState === 'complete'"));

            return driver;
        } catch (Exception e) {
            if (driver != null) driver.quit();
            throw new RuntimeException("Chrome initialization failed: " + e.getMessage(), e);
        }
        // Remove finally block - FFmpeg control belongs elsewhere
    }

    @Override
    public void StopMeeting() {



    }




//JUST FOR HWMD
    public  String getChromeHWND(String windowTitle) {
        // Use AtomicReference for thread-safe HWND storage
        AtomicReference<WinDef.HWND> foundHwnd = new AtomicReference<>();
        final String targetTitlePartial = windowTitle; // Adjust to your tab title

        // Enumerate all windows to find the correct Chrome instance
        User32.INSTANCE.EnumWindows((hWnd, data) -> {
            char[] windowText = new char[512];
            User32.INSTANCE.GetWindowText(hWnd, windowText, 512);
            String title = new String(windowText).trim();

            // Check both class and title
            char[] className = new char[512];
            User32.INSTANCE.GetClassName(hWnd, className, 512);
            String clsName = new String(className).trim();

            if (clsName.startsWith("Chrome_WidgetWin_") && title.contains(targetTitlePartial) && User32.INSTANCE.IsWindowVisible(hWnd)) {

                System.out.println("Found Chrome window:");
                System.out.println("  Class: " + clsName);
                System.out.println("  Title: " + title);
                System.out.println("  Handle: 0x" + Long.toHexString(Pointer.nativeValue(hWnd.getPointer())));

                foundHwnd.set(new WinDef.HWND(hWnd.getPointer()));
                return false; // Stop enumeration
            }
            return true; // Continue enumeration
        }, null);

        // Wait for window to appear (with timeout)
        Instant start = Instant.now();
        while (foundHwnd.get() == null && Duration.between(start, Instant.now()).getSeconds() < 10) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        if (foundHwnd.get() == null) {
            throw new RuntimeException("Chrome window not found after 10 seconds");
        }

        // Additional verification
        if (!User32.INSTANCE.IsWindowVisible(foundHwnd.get())) {
            throw new RuntimeException("Chrome window is not visible");
        }

        return "0x" + Long.toHexString(Pointer.nativeValue(foundHwnd.get().getPointer()));
    }
}
