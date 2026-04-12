package framework.listeners;

import constants.ConfigKeys;
import framework.utils.ConfigReader;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

public class RetryAnalyzer implements IRetryAnalyzer {

    private int retryCount = 0;
    private static final int maxRetryCount = Integer.parseInt(ConfigReader.getProperty(ConfigKeys.MAX_RETRY_COUNT));
    @Override
    public boolean retry(ITestResult iTestResult) {
        if (retryCount<maxRetryCount) {
            retryCount++;
            return true;
        }
        return false;
    }
}
