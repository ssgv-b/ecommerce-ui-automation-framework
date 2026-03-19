package framework.drivers;

import framework.utils.ConfigReader;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class DriverFactory {
    public DriverContext initializeDriver() {
        boolean headless = readBoolean("headless");
        String browserType = ConfigReader.getProperty("browser");

        browserType = browserType.toLowerCase(Locale.ROOT).trim();
        long pageTimeout = readLong("pageLoadTimeout");
        Duration waitDuration = Duration.ofSeconds(readLong("waitTimeout"));

        WebDriver driver = createDriver(browserType, headless);
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(pageTimeout));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
        return new DriverContext(driver, waitDuration);
    }

    private ChromeDriver createChromeDriver(boolean headless, String browserType) {
        String downloadPath = ConfigReader.getProperty("downloadDir");
        Path path = Paths.get(downloadPath).toAbsolutePath();
        try {
            Files.createDirectories(path);
        }
            catch(IOException e) {
            throw new RuntimeException("Failed to create download directory " + path, e);
        }

        ChromeOptions options = new ChromeOptions();

        if (headless) {
            options.addArguments("--headless=new");
            options.addArguments("window-size=1920,1080");
        }

        // CI / Stability flags
        options.addArguments("--disable-save-password-bubble");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        Map<String, Object> prefs = new HashMap<>();
        prefs.put("download.default_directory", path.toString());
        prefs.put("download.prompt_for_download", false);
        prefs.put("autofill.profile_enabled", false);
        prefs.put("autofill.credit_card_enabled", false);
        prefs.put("safebrowsing.enabled", true);
        prefs.put("credentials_enable_service", false);
        prefs.put("profile.password_manager_enabled", false);
        options.setExperimentalOption("prefs",prefs);

        ChromeDriver driver;
        try {
            driver = new ChromeDriver(options);

        }
        catch (WebDriverException e) {
            throw new RuntimeException("Failed to create ChromeDriver for: " + browserType, e);
        }
        if(!headless) {
            driver.manage().window().maximize();
        }
        return driver;

    }
    private FirefoxDriver createFirefoxDriver(boolean headless, String browserType) {
        FirefoxOptions options = new FirefoxOptions();
        FirefoxDriver driver;
        if (headless) {
            options.addArguments("-headless");
        }
        try {
            driver = new FirefoxDriver(options);
        }
        catch (WebDriverException e) {
            throw new RuntimeException("Failed to create FirefoxDriver for: " + browserType, e);
        }
        if (headless) {
            driver.manage().window().setSize(new Dimension(1920,1080));
        }
        else {
            driver.manage().window().maximize();
        }
        return driver;
    }
    private long readLong(String key) {
            try {
                return Long.parseLong(ConfigReader.getProperty(key));
            } catch (NumberFormatException e) {
                throw new RuntimeException("Invalid numeric value for " + key, e);
            }
    }

    private boolean readBoolean(String key) {
        return Boolean.parseBoolean(ConfigReader.getProperty(key));
    }

    private WebDriver createDriver(String browserType, boolean headless) {
        return switch (browserType) {
            case "chrome" -> createChromeDriver(headless, browserType);
            case "firefox" -> createFirefoxDriver(headless, browserType);
            default -> throw new RuntimeException("Unsupported browser " + browserType);
        };
    }
}