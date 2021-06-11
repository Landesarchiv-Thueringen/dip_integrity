/**
 * DIP Integrity Library for generation and validation of integrity information of DIP
 * Copyright (C) 2021 Tony Grochow (tony.grochow@la.thueringen.de)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.

 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA
 */

package lath.integrity.util;

public class ErrorUtil {

  public enum ErrorType {
    FILE_NOT_FOUND,
    FILE_NOT_READABLE,
    FILE_FORMAT_INVALID,
    FILE_CHECKSUM_INVALID
  }

  public static String getFileErrorMessage(final String fileName, final ErrorType errorType) {
    String errorMessage = "Die Datei \"" + fileName + "\" ";
    switch (errorType) {
      case FILE_NOT_FOUND:
        errorMessage += "ist im Verzeichnis nicht vorhanden.";
        break;
      case FILE_NOT_READABLE:
        errorMessage += "ist nicht lesbar.";
        break;
      case FILE_FORMAT_INVALID:
        errorMessage += "entspricht nicht dem erwarteten Schema.";
        break;
      case FILE_CHECKSUM_INVALID:
        errorMessage = "Die Pr\u00fcfsumme der Datei \"" + fileName + "\" ist nicht korrekt.";
        break;
      default:
        errorMessage += "hat einen unbekannten Fehler.";
        break;
    }
    return errorMessage;
  }
}
