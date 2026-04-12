package framework.listeners;

import framework.annotations.NoRetry;
import framework.utils.ConfigReader;
import org.testng.IAnnotationTransformer;
import org.testng.annotations.ITestAnnotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import static constants.Groups.CRITICAL_PATH;
import static constants.Groups.DOWNLOAD;

public class RetryTransformer implements IAnnotationTransformer {

    // Raw types required by IAnnotationTransformer API
    @SuppressWarnings("rawtypes")
    @Override
    public void transform(ITestAnnotation annotation, Class testClass,
                          Constructor testConstructor, Method testMethod) {
        if (testMethod == null) return;
        if (hasNoRetryAnnotation(testMethod)) return;
        if(annotation.getRetryAnalyzerClass() !=null) {
            return;
        }
        if(shouldRetry(annotation, testMethod)) {
            annotation.setRetryAnalyzer(RetryAnalyzer.class);
        }
    }

    private static boolean shouldRetry (ITestAnnotation annotation, Method testMethod) {
        return isCiEnvironment() && isInRetryGroup(annotation);
    }

    private static boolean isCiEnvironment() {
        String testEnv = ConfigReader.getProperty("test.env", "local");
        String ciVar = ConfigReader.getProperty("CI", "false");
        String ghVar = ConfigReader.getProperty("GITHUB_ACTIONS", "false");
        return "ci".equalsIgnoreCase(testEnv)
                || "true".equalsIgnoreCase(ciVar)
                || "true".equalsIgnoreCase(ghVar);
    }

    private static boolean isInRetryGroup(ITestAnnotation annotation) {
        String[] groups = annotation.getGroups();
        if(groups == null || groups.length == 0) {
            return false;
        }
        for(String group : groups) {
            if(CRITICAL_PATH.equals(group) || DOWNLOAD.equals(group)) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasNoRetryAnnotation(Method testMethod) {
        return testMethod.isAnnotationPresent(NoRetry.class);
    }
}


