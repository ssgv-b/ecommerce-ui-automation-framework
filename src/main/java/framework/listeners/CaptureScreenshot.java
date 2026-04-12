package framework.listeners;

import io.qameta.allure.Attachment;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CaptureScreenshot {

    private static final Logger log = LoggerFactory.getLogger(CaptureScreenshot.class);

    private CaptureScreenshot() {
    }

    @Attachment(value = "Failure screenshot", type = "image/png")
    public static byte[] captureScreenshotAndAttach(WebDriver driver) {
        if (driver == null) {
            log.warn("Driver is null, cannot take screenshot");
            return new byte[0];
        }
        if (!(driver instanceof TakesScreenshot)) {
            log.warn("Driver does not implement TakesScreenshot, cannot take screenshot");
            return new byte[0];
        }
            try {
            return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
        }
        catch (Exception e) {
            log.warn("Unable to take screenshot on failure",e);
        }
        return new byte[0];
    }
}
