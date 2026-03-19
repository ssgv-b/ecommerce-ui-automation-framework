package framework.utils;


import org.testng.Assert;
import java.io.File;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;

public class FileDownloadUtils {

    public static File waitForDownloadedFile(
            Path downloadDir,
            String expectedNamePart,
            Duration timeout
    ) {
        return waitForDownloadedFile(downloadDir, expectedNamePart, timeout, null);
    }

    public static File waitForDownloadedFile(
            Path downloadDir,
            String expectedNamePart,
            Duration timeout,
            Instant notBefore
    ) {
        Instant end = Instant.now().plus(timeout);
        long notBeforeEpoch = notBefore == null ? Long.MIN_VALUE : notBefore.toEpochMilli();

        while (Instant.now().isBefore(end)) {
            File[] files = downloadDir.toFile().listFiles();

            if (files != null) {
                for (File file : files) {
                    if (file.getName().contains(expectedNamePart)
                            && !file.getName().endsWith(".crdownload")
                            && !file.getName().endsWith(".part")
                            && file.lastModified() >= notBeforeEpoch
                            && file.length() > 0) {
                        return file;
                    }
                }
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while waiting for file download", e);
            }
        }

        throw new RuntimeException(
                "Timed out waiting for file containing '" + expectedNamePart +
                        "' in directory: " + downloadDir
        );
    }
        public static void assertValidDownloadedFile(File file, String expectedNamePart) {
            Assert.assertNotNull(file, "Downloaded file is null");
            Assert.assertTrue(
                    file.getName().contains(expectedNamePart),
                    "Expected file name to contain '" + expectedNamePart +
                            "' but was '" + file.getName() + "'"
            );
            Assert.assertTrue(
                    file.length() > 0,
                    "Downloaded file is empty: " + file.getName()
            );
        }
    }

