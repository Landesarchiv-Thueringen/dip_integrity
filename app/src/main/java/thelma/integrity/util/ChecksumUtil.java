package thelma.integrity.util;

import java.security.MessageDigest;

import thelma.integrity.hashforest.HashValue;

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
