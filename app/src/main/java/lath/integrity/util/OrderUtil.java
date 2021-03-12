package lath.integrity.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import lath.integrity.error.InvalidInputException;

/**
 * Helper class to maintain the order of the data whose integrity was computed
 * in the case that there is no implicit ordering avaliable. This is basically a
 * list of data identifiers which may be filenames, GUID's, URN's or whatever is
 * suitable to describe the data. If this doesn't directly reference the file
 * locations, a mapping from identifiers to locations must be provided by the
 * application. Whatever identifier is used, it must not contain newline
 * characters as these are used to separate identifiers in text mode
 * serialization.
 */
public class OrderUtil implements TextSerializable {

  private static final long serialVersionUID = -5033306062362219759L;
  public final static String ORDERFILENAME = "integritycomponent-order.txt";
  private final static Charset CHARSET = Charset.forName("UTF-8");
  private final static int BUFSIZE = 1024;
  private final static String LINESEPARATOR = "\n";
  private List<String> identifiers = new ArrayList<String>();
  private boolean isValid = true;
  private ChecksumUtil checksumUtil;

  public OrderUtil(ChecksumUtil checksumUtil) {
    this.checksumUtil = checksumUtil;
  }

  /**
   * Add dataIdentifier to the end of the ordered structure.
   *
   * @param dataIdentifier
   * @throws IllegalArgumentException
   */
  public void add(String dataIdentifier) {
    if (dataIdentifier.contains("\n")) {
    throw new IllegalArgumentException(
      "Identifier contains newline character:" + dataIdentifier);
    }
    identifiers.add(dataIdentifier);
    isValid = false;
  }

  /**
   * Returns the ordered list of data identifiers.
   *
   * @return
   */
  public List<String> getIdentifiers() {
    return Collections.unmodifiableList(identifiers);
  }

  /**
   * Called after reading this object from a stream this flag is true iff the
   * data matches a checksum, otherwise false.
   *
   * @return
   */
  public boolean isValid() {
    return isValid;
  }

  /*
   * Protected to provide regression test
   */
  protected void writeInt(int i, OutputStream os) throws IOException {
    os.write(i >> 24);
    os.write(i >> 16);
    os.write(i >> 8);
    os.write(i);
  }

  /*
   * Protected to provide regression test
   */
  protected int readInt(InputStream is) throws IOException {
    byte[] buf = new byte[4];
    int bytesRead = is.read(buf);
    if (bytesRead != 4) {
      throw new IOException("Couldn't read 4 bytes for an int");
    }

    int result = (0xFF & (int) buf[0]) << 24
       | (0xFF & (int) buf[1]) << 16
       | (0xFF & (int) buf[2]) << 8
       | 0xFF & (int) buf[3];
    return result;
  }

  private String computeChecksum(List<String> data) {
    for (String s : data) {
      checksumUtil.update(s.getBytes(CHARSET));
    }
    return checksumUtil.get();
  }

  @Override
  public void writeTo(Writer w) throws IOException {
    String checksum = computeChecksum(identifiers);
    w.write(checksum);
    for (String s : identifiers) {
      w.write(LINESEPARATOR);
      w.write(s);
    }
  }

  @Override
  public void readFrom(Reader r) throws IOException, InvalidInputException {
    reset();
    char[] buf = new char[BUFSIZE];
    StringBuilder sb = new StringBuilder();
    int charsRead;
    while ((charsRead = r.read(buf)) != -1) {
      sb.append(Arrays.copyOf(buf, charsRead));
    }
    String[] lines = sb.toString().split(LINESEPARATOR);
    for (int i = 1; i < lines.length; ++i) {
      identifiers.add(lines[i]);
    }
    isValid = computeChecksum(identifiers).equals(lines[0]);
    if (!isValid) {
      throw new InvalidInputException("Invalid checksum for ordering information!");
    }
  }

  private void reset() {
    identifiers.clear();
    isValid = false;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (String s : identifiers) {
      sb.append(s);
      sb.append("\n");
    }
    return sb.toString();
  }

}
