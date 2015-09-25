package java.fr.valquev.mypov;

/**
 * Created by ValQuev on 17/09/15.
 */
public class MyPOVResponse<T> {

    private int status;
    private String message;
    private T object;

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public T getObject() {
        return object;
    }
}
