package framework.listeners;

public class TestExecutionContext {
    private final String methodName;

    public TestExecutionContext(String methodName) {
        this.methodName = methodName;
    }

    public String getMethodName() {
        return methodName;
    }
}
