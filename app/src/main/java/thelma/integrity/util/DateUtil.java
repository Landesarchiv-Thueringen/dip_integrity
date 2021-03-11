package thelma.integrity.util;

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
