/**
 * DIP Integrity Library for generation and validation of integrity information of DIP
 * Copyright (C) 2015 Christof Bräutigam (christof.braeutigam@cbraeutigam.de)
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import lath.integrity.hashforest.SHA512HashValue;

/**
 * Helper class for file hashing.
 */
public class FileUtil {

  /**
   * Helper method to compute the SHA512 hash value for a given file.
   * @throws NoSuchAlgorithmException
   * @throws MissingDataFileException
   */
  public static SHA512HashValue getHash(String fileName)
      throws NoSuchAlgorithmException, FileNotFoundException, IOException {
    final MessageDigest sha512 = MessageDigest.getInstance("SHA-512");
    final int bufferSize = 128;
    final byte[] buffer = new byte[bufferSize];
    final File f = new File(fileName);
    final FileInputStream fis = new FileInputStream(f);
    int bytesRead = fis.read(buffer);
    // file is empty, update with empty message
    if (bytesRead == -1) {
      sha512.update(new byte[0]);
    } else {
      sha512.update(Arrays.copyOf(buffer, bytesRead));
    }
    while ((bytesRead = fis.read(buffer)) != -1) {
      sha512.update(Arrays.copyOf(buffer, bytesRead));
    }
    fis.close();
    return new SHA512HashValue(sha512.digest());
  }

}
