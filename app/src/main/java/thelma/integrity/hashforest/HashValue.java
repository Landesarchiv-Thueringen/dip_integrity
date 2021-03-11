package thelma.integrity.hashforest;

import java.security.InvalidParameterException;
import java.security.MessageDigest;

public abstract class HashValue {

  MessageDigest md;

  byte[] bytes; // hash value internally stored as byte array

  public HashValue(byte[] bytes) {
    this.bytes = bytes;
  }

  /**
   * Returns the hexadecimal string representation of this hash value.
   *
   * @return hexadecimal string representation of this hash value
   */
  public String getHexString() {
    return HashValue.bytes2hex(bytes);
  }

  /**
   * Returns the byte[] representation of this hash value.
   *
   * @return byte[] representation of this hash value.
   */
  public byte[] getBytes() {
    return bytes.clone();
  }

  /**
   * Converts the given byte[] to a hexadecimal string representation.
   *
   * @param raw
   *            the byte[] to convert
   * @return the string the given byte[] represents
   *
   */
  // cf.
  // http://www.coderblog.de/producing-the-same-sha-512-hash-in-java-and-php/
  public static String bytes2hex(byte[] b) {
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < b.length; i++) {
      sb.append(Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1));
    }
    return sb.toString();
  }

  /**
   * Converts the given string to a byte array. The string must be a
   * hexadecimal representation of a hash value, i.e. it must have a length
   * that is divisible by 2 and must consist only of characters in the range
   * [0-9A-Fa-f].
   *
   * @param s
   *            the hexadecimal string to convert
   * @return the byte[] the hex-string represents
   * @exception InvalidParameterException
   *                if the given string is not a valid hexadecimal hash value
   *                representation
   *
   */
  // cf. http://stackoverflow.com/a/140861
  public static byte[] hex2bytes(String s) {
    int len = s.length();
    if (len % 2 != 0) { // sanity check
    throw new InvalidParameterException(
      "Not a valid hex digest (length is not a multiple of 2): " + s);
    }
    byte[] data = new byte[len / 2];
    int high, low;
    for (int i = 0; i < len; i += 2) {
      high = Character.digit(s.charAt(i), 16);
      low = Character.digit(s.charAt(i + 1), 16);
      if (high == -1 || low == -1) {
        throw new InvalidParameterException(
        "Not a valid hex digest (invalid characters): " + s);
      }
      data[i / 2] = (byte) ((high << 4) + low);
    }
    return data;
  }

  /**
   * Computes and returns a new hash value that is the hashed concatenation of
   * this and the other hash value:<br>
   * h (this || other)<br>
   * Both hash values (this and other) must be of the same type!
   *
   * @param other
   * @return the hash value of the concatenation of this and the other hash
   *         value
   * @throws InvalidParameterException
   *             if the hash values are not of the same type
   */
  public abstract HashValue concatenate(HashValue other);

}
