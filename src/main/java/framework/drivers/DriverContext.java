package framework.drivers;

import org.openqa.selenium.WebDriver;

import java.time.Duration;

public class DriverContext {
    private final WebDriver driver;
    private final Duration waitDuration;
    private final Duration optionalWaitDuration;

    DriverContext(WebDriver driver, Duration waitDuration, Duration optionalWaitDuration) {
        this.driver = driver;
        this.waitDuration = waitDuration;
        this.optionalWaitDuration = optionalWaitDuration;
    }

    public WebDriver getDriver() {
        return driver;
    }

    public Duration getWaitDuration() {
        return waitDuration;
    }

    public Duration getOptionalWaitDuration() {
        return optionalWaitDuration;
    }
}
