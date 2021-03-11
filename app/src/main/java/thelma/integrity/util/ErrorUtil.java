package thelma.integrity.util;

public class ErrorUtil {

  public enum ErrorType {
    FILE_NOT_FOUND,
    FILE_NOT_READABLE,
    FILE_FORMAT_INVALID,
  }

  public static String getFileErrorMessage(final String fileName, final ErrorType errorType) {
    String errorMessage = "Die Datei \"" + fileName + "\" ";
    switch (errorType) {
      case FILE_NOT_FOUND:
        errorMessage += "existiert nicht.";
        break;
      case FILE_NOT_READABLE:
        errorMessage += "ist nicht lesbar.";
        break;
      case FILE_FORMAT_INVALID:
        errorMessage += "entspricht nicht dem erwarteten Format.";
        break;
      default:
        errorMessage += "hat einen unbekannten Fehler.";
        break;
    }
    return errorMessage;
  }
}
