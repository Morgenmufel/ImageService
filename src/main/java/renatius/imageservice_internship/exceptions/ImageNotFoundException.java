package renatius.imageservice_internship.exceptions;

public class ImageNotFoundException extends RuntimeException {
    public ImageNotFoundException(String message) {
        super(message);
    }
}
