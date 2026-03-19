package framework.listeners;

import framework.annotations.NoRetry;
import org.testng.IAnnotationTransformer;
import org.testng.annotations.ITestAnnotation;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class RetryTransformer implements IAnnotationTransformer {

    @Override
    public void transform(ITestAnnotation annotation, Class testClass,
                          Constructor testConstructor, Method testMethod) {
        if (testMethod == null) return;
        if(annotation.getRetryAnalyzerClass() !=null) {
            return;
        }
        if(shouldRetry(annotation, testMethod)) {
            annotation.setRetryAnalyzer(RetryAnalyzer.class);
        }
    }

    private static boolean shouldRetry (ITestAnnotation annotation, Method testMethod) {
        return isCiEnvironment() && isInRetryGroup(annotation) && !hasNoRetryAnnotation(testMethod);
    }

    private static boolean isCiEnvironment() {
        String testEnv = System.getProperty("test.env");
        String ciVar = System.getenv("CI");
        String ghVar = System.getenv("GITHUB_ACTIONS");
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
            if("critical_path".equalsIgnoreCase(group) || "download".equalsIgnoreCase(group)) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasNoRetryAnnotation(Method testMethod) {
        Annotation[] methodAnnotations = testMethod.getAnnotations();
        if (methodAnnotations.length == 0) {
            return false;
        }
        for(Annotation annotation : methodAnnotations) {
            if (annotation.annotationType()== NoRetry.class){
                return true;
            }
        }
        return false;
    }
}


