package lath.integrity;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class DipIntegrityGenerator {

  public static void main(String[] args) {
    System.out.println("hello");
    final Option createIntegrityInformation = new Option("c", "create", true, "dip dir");
    final Option testIntegrityInformation = new Option("t", "test", true, "dip dir");
    final Options options = new Options();
    options.addOption(createIntegrityInformation);
    options.addOption(testIntegrityInformation);
    final CommandLineParser parser = new DefaultParser();
    final CommandLine cmd;
    try {
      cmd = parser.parse(options, args);
    } catch (final ParseException e) {
      System.out.println(e.getMessage());
      final HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp("DIP2Gp Integritätsinformation Erstellung", options);
      System.exit(1);
    }
  }
}
