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

import java.security.MessageDigest;

import lath.integrity.hashforest.HashValue;

public class ChecksumUtil {

  private final MessageDigest md;
  public String checksum;

  /*
   * TODO: maybe replace the checksum by a CRC32 to prevent
   * "NoSuchAlgorithmException" when instantiating an algorithm.
   *
   */
  public ChecksumUtil(MessageDigest md) {
    this.md = md;
    md.reset();
    checksum = null;
  }

  public void update(byte[] bytes) {
    md.update(bytes);
    checksum = null;
  }

  public String get() {
    if (checksum == null) {
      checksum = HashValue.bytes2hex(md.digest());
    }
    return checksum;
  }

}
