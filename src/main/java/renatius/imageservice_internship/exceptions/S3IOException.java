package renatius.imageservice_internship.exceptions;

public class S3IOException extends RuntimeException {
    public S3IOException(String message) {
        super(message);
    }
}
