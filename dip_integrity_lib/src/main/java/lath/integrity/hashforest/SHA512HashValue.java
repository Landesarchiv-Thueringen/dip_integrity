package lath.integrity.hashforest;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class SHA512HashValue extends HashValue {

  public SHA512HashValue(byte[] bytes) throws NoSuchAlgorithmException {
    super(bytes);
    if (bytes.length != 64) {
      throw new IllegalArgumentException("Not a valid SHA512 value, length is not exactly 64 bytes!");
    }
    md = MessageDigest.getInstance("SHA-512");
  }

  public SHA512HashValue(String hexDigest) throws NoSuchAlgorithmException {
    this(HashValue.hex2bytes(hexDigest));
    md = MessageDigest.getInstance("SHA-512");
  }

  // concatenation c'tor to avoid NoSuchAlgorithmException
  private SHA512HashValue(byte[] bytes, MessageDigest md) {
    super(bytes);
    this.md = md;
  }

  @Override
  public HashValue concatenate(HashValue other) {
    if (! (other instanceof SHA512HashValue)) {
      throw new IllegalArgumentException("Concatenation of different hash types is not supported!");
    }

    md.reset();
    md.update(bytes);
    md.update(other.bytes);

    return new SHA512HashValue(md.digest(), md);
  }

  @Override
  public boolean equals(Object obj) {
    if (! (obj instanceof SHA512HashValue)) {
      return false;
    }
    SHA512HashValue other = (SHA512HashValue) obj;
    return Arrays.equals(bytes, other.bytes);
  }

  @Override
  public int hashCode() {
    return bytes.hashCode();
  }

  @Override
  public String toString() {
    return HashValue.bytes2hex(bytes);
  }

}
