package lath.integrity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import lath.integrity.util.ChecksumUtil;
import lath.integrity.util.OrderUtil;

public class DipIntegrityGenerator {

  private static final String invokeCommand = "java -jar dip_integrity_generation.jar";
  private static final HelpFormatter formatter = new HelpFormatter();
  private static final Option createIntegrityInformation = new Option("c", "create", true, "dip dir");
  private static final Option testIntegrityInformation = new Option("t", "test", true, "dip dir");
  private static final Options options = new Options();
  private static CommandLine cmd;

  private static void createIntegrityInformation(final Path dipDir) {
    final List<Path> fileList = getFileList(dipDir);
    try {
      final ChecksumUtil checksumProvider = new ChecksumUtil(MessageDigest.getInstance("SHA-512"));
      final OrderUtil fileOrder = new OrderUtil(checksumProvider);
      fileOrder.add(OrderUtil.ORDERFILENAME);
      final Path root = Paths.get(System.getProperty("user.dir"));
      for (final Path filePath : fileList) {
        final Path relativePath = filePath.subpath(root.getNameCount(), filePath.getNameCount());
        fileOrder.add(relativePath.toString());
      }
      final File orderFile = Paths.get(dipDir.toString(), OrderUtil.ORDERFILENAME).toFile();
      final Writer fstream = new OutputStreamWriter(new FileOutputStream(orderFile, false), OrderUtil.CHARSET);
      fileOrder.writeTo(fstream);
      fstream.close();
    } catch (NoSuchAlgorithmException e) {
      // clearly a developer error, reraise instead of propagating to ui
      throw new RuntimeException(e);
    } catch (IOException e) {
      System.out.println("error writing order file");
      System.out.println(e.getMessage());
      System.exit(1);
    }
  }

  private static void testIntegrityInformation(final Path dipDir) {

  }

  private static Path getDipDir(final String commandLineValue) {
    final Path dipDir = Paths.get(System.getProperty("user.dir"), commandLineValue);
    if (!Files.isDirectory(dipDir)) {
      System.out.println("The path \"" + dipDir + "\" is no valid directory.");
      System.exit(1);
    }
    return dipDir.normalize();
  }

  private static List<Path> getFileList(final Path dipDir) {
    List<Path> dipFileList = new ArrayList<Path>();
    try {
      dipFileList = Files.walk(dipDir)
        .filter(Files::isRegularFile)
        .collect(Collectors.toList());
    } catch (IOException e) {
      System.out.println("DIP directory files can't be read.");
      System.out.println(e.getMessage());
      System.exit(1);
    }
    return dipFileList;
  }

  private static void parseCommandLineArguments(final String[] args) {
    final OptionGroup optionGroup = new OptionGroup();
    optionGroup.addOption(createIntegrityInformation);
    optionGroup.addOption(testIntegrityInformation);
    options.addOptionGroup(optionGroup);
    final CommandLineParser parser = new DefaultParser();
    try {
      cmd = parser.parse(options, args);
    } catch (final ParseException e) {
      System.out.println(e.getMessage());
      formatter.printHelp(invokeCommand, options);
      System.exit(1);
    }
  }

  public static void main(final String[] args) {
    parseCommandLineArguments(args);
    if (cmd.hasOption("c")) {
      final Path dipDir = getDipDir(cmd.getOptionValue("c"));
      createIntegrityInformation(dipDir);
    } else if (cmd.hasOption("t")) {
      final Path dipDir = getDipDir(cmd.getOptionValue("t"));
      testIntegrityInformation(dipDir);
    } else {
      formatter.printHelp(invokeCommand, options);
    }
  }

}
