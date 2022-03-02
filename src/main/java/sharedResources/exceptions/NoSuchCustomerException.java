package sharedResources.exceptions;

public class NoSuchCustomerException extends Exception {
    public NoSuchCustomerException (String message) {
        super(message);
    }
}