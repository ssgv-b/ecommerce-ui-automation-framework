package framework.exceptions;

public class PageStateException extends FrameworkException {

    public PageStateException(String message, Throwable err) {
        super(message, err);
    }

    public PageStateException(String message) {
        super(message);
    }
}
