package framework.drivers;

import org.openqa.selenium.WebDriver;

import java.time.Duration;

public class DriverContext {
    private final WebDriver driver;
    private final Duration waitDuration;

    DriverContext(WebDriver driver, Duration waitDuration) {
        this.driver = driver;
        this.waitDuration = waitDuration;
    }

    public WebDriver getDriver() {
        return driver;
    }
    public Duration getWaitDuration() {
        return waitDuration;
    }

}
