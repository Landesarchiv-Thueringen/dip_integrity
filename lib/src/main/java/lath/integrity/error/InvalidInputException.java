package lath.integrity.error;

public class InvalidInputException extends Exception {

  public enum ErrorType {
    SCHEMA_INVALID,
    CHECKSUM_INVALID
  }

  private static final long serialVersionUID = -5655068480224494695L;
  private final ErrorType errorType;

  public InvalidInputException(final String message, final ErrorType errorType) {
    super(message);
    this.errorType = errorType;
  }

  public ErrorType getErrorType() {
    return errorType;
  }

}
