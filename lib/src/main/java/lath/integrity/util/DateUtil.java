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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * Helper class that provides Date<->String conversion.
 *
 */
public class DateUtil {

  /*
   * We need the full information to provide an equality check with parsed
   * dates.
   */
  public static final String DATEPATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";

  public static Date string2Date(String formattedDate) throws ParseException {
    SimpleDateFormat sdf = new SimpleDateFormat(DATEPATTERN);
    return sdf.parse(formattedDate);
  }

  public static String date2String(Date date) {
    SimpleDateFormat sdf = new SimpleDateFormat(DATEPATTERN);
    return sdf.format(date);
  }

}
