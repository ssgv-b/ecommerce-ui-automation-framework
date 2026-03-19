package framework.listeners;

import framework.base.BaseTest;
import org.openqa.selenium.WebDriver;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;

import java.util.List;
import java.util.stream.Collectors;

import static framework.listeners.CaptureScreenshot.captureScreenshotAndAttach;

public class TestListeners implements ITestListener {
    private final ThreadLocal<TestExecutionContext> testStartData =  new ThreadLocal<>();

    @Override
    public void onTestStart(ITestResult result) {
        TestExecutionContext testContext = getTestContext(result);
        testStartData.set(testContext);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        testStartData.remove();
    }

    @Override
    public void onTestFailure(ITestResult result) {
        try {
            TestExecutionContext context = getCurrentContext(result);
            String failedMethodName = context.getMethodName();
            String errorMessage = getThrowableMessage(result);
            captureScreenshotAndAttach(getDriverInstance(result));

            setAttributeIfNotNull(result, "failedMethodName", failedMethodName);
            setAttributeIfNotNull(result, "failureMessage", errorMessage);
        } finally {
            testStartData.remove();
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        try {
            TestExecutionContext context = getCurrentContext(result);
            String skippedMethodName = context.getMethodName();
            String errorMessage = getThrowableMessage(result);
            String skipCauses = formatSkipCauses(result.getSkipCausedBy());

            setAttributeIfNotNull(result, "skippedMethodName", skippedMethodName);
            setAttributeIfNotNull(result, "skipMessage", errorMessage);
            setAttributeIfNotNull(result, "skipCausedBy", skipCauses);
        } finally {
            testStartData.remove();
        }
    }

    @Override
    public void onFinish(ITestContext context) {
        testStartData.remove();
    }

    private TestExecutionContext getTestContext(ITestResult result) {
        String methodName = result.getMethod().getMethodName();
        String[] methodGroups = result.getMethod().getGroups();
        Object[] methodParams = result.getParameters();
        return new TestExecutionContext(methodName, methodGroups,
                methodParams);
    }

    private String getThrowableMessage(ITestResult result) {
        return result.getThrowable() == null
                ? null
                : result.getThrowable().getMessage();
    }

    private TestExecutionContext getCurrentContext(ITestResult result) {
        TestExecutionContext context = testStartData.get();
        if (context != null) {
            return context;
        }
        return getTestContext(result);
    }

    private WebDriver getDriverInstance(ITestResult result) {
        Object instance = result.getInstance();
        if (instance instanceof BaseTest) {
            return ((BaseTest) instance).getDriverListener();
        }
        return null;
    }

    private String formatSkipCauses(List<ITestNGMethod> skipCauses) {
        if (skipCauses == null || skipCauses.isEmpty()) {
            return null;
        }
        return skipCauses.stream()
                .map(method -> method.getRealClass().getSimpleName() + "." + method.getMethodName())
                .collect(Collectors.joining(", "));
    }

    private void setAttributeIfNotNull(ITestResult result, String key, Object value) {
        if (value != null) {
            result.setAttribute(key, value);
        }
    }
}
