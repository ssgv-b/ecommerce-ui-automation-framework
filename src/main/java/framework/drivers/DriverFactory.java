package framework.drivers;

import constants.ConfigKeys;
import framework.utils.ConfigReader;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class DriverFactory {
    private static final ThreadLocal<WebDriver> driverRegistry = new ThreadLocal<>();
    private static final Logger log = LoggerFactory.getLogger(DriverFactory.class);

    public static DriverContext initializeDriver() {
        boolean headless = readBoolean(ConfigKeys.HEADLESS);
        String browserType = ConfigReader.getProperty(ConfigKeys.BROWSER);

        browserType = browserType.toLowerCase(Locale.ROOT).trim();
        long pageTimeout = readLong(ConfigKeys.PAGE_LOAD_TIMEOUT);
        Duration waitDuration = Duration.ofSeconds(readLong(ConfigKeys.WAIT_TIMEOUT));

        WebDriver driver = createDriver(browserType, headless);
        driverRegistry.set(driver);
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(pageTimeout));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
        return new DriverContext(driver, waitDuration);
    }

    private static ChromeDriver createChromeDriver(boolean headless) {
        Path downloadPath = resolveDownloadPath();

        ChromeOptions options = new ChromeOptions();

        if (headless) {
            options.addArguments("--headless=new");
            options.addArguments("--window-size=1920,1080");
        }

        // CI / Stability flags
        options.addArguments("--disable-save-password-bubble");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        Map<String, Object> prefs = new HashMap<>();
        // set download prefs
        prefs.put("download.default_directory", downloadPath.toString());
        prefs.put("download.prompt_for_download", false);

        // disable autofill
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
            throw new RuntimeException("Failed to create ChromeDriver for: ", e);
        }

        if(!headless) {
            driver.manage().window().maximize();
        }
        return driver;

    }

    private static FirefoxDriver createFirefoxDriver(boolean headless) {
        Path downloadPath = resolveDownloadPath();

        FirefoxOptions options = new FirefoxOptions();
        // set profile download prefs
        options.addPreference("browser.download.dir", downloadPath.toString());
        options.addPreference("browser.download.folderList", 2);
        options.addPreference("browser.download.useDownloadDir", true);
        options.addPreference("pdfjs.disabled", true);

        // MIME types
        options.addPreference(
                "browser.helperApps.neverAsk.saveToDisk",
                "application/pdf,application/octet-stream,text/csv,image/png"
        );

        // disable password manager
        options.addPreference("signon.rememberSignons", false);
        options.addPreference("signon.autofillForms", false);
        FirefoxDriver driver;
        if (headless) {
            options.addArguments("-headless");
            options.addArguments("--width=1920", "--height=1080");
        }

        try {
            driver = new FirefoxDriver(options);
        }
        catch (WebDriverException e) {
            throw new RuntimeException("Failed to create FirefoxDriver for: ", e);
        }

        if (!headless) {
            driver.manage().window().maximize();
        }
        return driver;
    }

    private static WebDriver createDriver(String browserType, boolean headless) {
        return switch (browserType) {
            case "chrome" -> createChromeDriver(headless);
            case "firefox" -> createFirefoxDriver(headless);
            default -> throw new RuntimeException("Unsupported browser " + browserType);
        };
    }

    public static void quitDriver() {
        WebDriver driver = driverRegistry.get();
        if(driver!=null) {
            try {
                driver.quit();
            }
            catch (WebDriverException e) {
                log.warn("Driver quit did not complete cleanly: {}", e.getMessage());
            }
            finally {
                driverRegistry.remove();
            }
        }
    }

    private static long readLong(String key) {
        try {
            return Long.parseLong(ConfigReader.getProperty(key));
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid numeric value for " + key, e);
        }
    }

    private static boolean readBoolean(String key) {
        return Boolean.parseBoolean(ConfigReader.getProperty(key));
    }

    private static Path resolveDownloadPath() {
        Path path = Paths.get(ConfigReader.getProperty(ConfigKeys.DOWNLOAD_DIR)).toAbsolutePath();
        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create download directory: " + path, e);
        }
        return path;
    }
}