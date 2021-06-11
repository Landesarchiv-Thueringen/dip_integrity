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

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;

import lath.integrity.error.InvalidInputException;

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
