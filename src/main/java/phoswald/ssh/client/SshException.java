package phoswald.ssh.client;

public class SshException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public SshException(String message) {
        super(message);
    }

    public SshException(String message, Throwable cause) {
        super(message, cause);
    }

    public SshException(Throwable cause) {
        super(cause);
    }
}
