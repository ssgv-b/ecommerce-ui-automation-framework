package framework.listeners;

public class TestExecutionContext {
    private final String methodName;
    private final String[] groupName;
    private final Object[] params;

    public TestExecutionContext(String methodName, String[] groupName, Object[] params) {
        this.methodName = methodName;
        this.groupName = groupName;
        this.params = params;
    }

    public String getMethodName() {
        return methodName;
    }

    public String[] getGroupName() {
        return groupName;
    }

    public Object[] getParams() {
        return params;
    }
}
