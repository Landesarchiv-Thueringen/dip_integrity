package thelma.integrity.util;

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;

import thelma.integrity.error.InvalidInputException;

/**
 * Interface for classes that must support writing to and reading from character
 * streams.
 */
public interface TextSerializable extends Serializable {

  /**
   * Writes this object to w.
   * @param w Writer
   */
  public void writeTo(Writer w) throws IOException;

  /**
   * Reads this object from r.
   * @param r Reader
   */
  public void readFrom(Reader r) throws IOException, InvalidInputException;

}
