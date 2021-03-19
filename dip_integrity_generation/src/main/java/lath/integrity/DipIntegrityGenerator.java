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

import lath.integrity.hashforest.HashForest;
import lath.integrity.hashforest.SHA512HashValue;
import lath.integrity.util.ChecksumUtil;
import lath.integrity.util.FileUtil;
import lath.integrity.util.OrderUtil;

public class DipIntegrityGenerator {

  private static final String invokeCommand = "java -jar dip_integrity_generation.jar";
  private static final HelpFormatter formatter = new HelpFormatter();
  private static final Option createIntegrityInformation = new Option("c", "create", true, "dip dir");
  private static final Option testIntegrityInformation = new Option("t", "test", true, "dip dir");
  private static final Options options = new Options();
  private static CommandLine cmd;

  private static OrderUtil fileOrder;

  private static void createIntegrityInformation(final Path dipDir) {
    final Path orderFilePath = Paths.get(dipDir.toString(), OrderUtil.ORDERFILENAME);
    final Path integrityFilePath = Paths.get(dipDir.toString(), HashForest.INTEGRITYFILENAME);
    try {
      Files.deleteIfExists(orderFilePath);
      Files.deleteIfExists(integrityFilePath);
    } catch (final IOException e) {
      System.out.println("error deleting old integrity information files");
      System.out.println(e.getMessage());
      System.exit(1);
    }
    final List<Path> fileList = getFileList(dipDir);
    generateOrderFile(orderFilePath, fileList);
    generateIntegrityFile(dipDir, integrityFilePath);
  }

  private static void generateOrderFile(final Path orderFilePath, final List<Path> fileList) {
    try {
      final ChecksumUtil checksumProvider = new ChecksumUtil(MessageDigest.getInstance("SHA-512"));
      fileOrder = new OrderUtil(checksumProvider);
      fileOrder.add(OrderUtil.ORDERFILENAME);
      final Path root = Paths.get(System.getProperty("user.dir"));
      for (final Path filePath : fileList) {
        final Path relativePath = filePath.subpath(root.getNameCount(), filePath.getNameCount());
        fileOrder.add(relativePath.toString());
      }
      Files.createFile(orderFilePath);
      final File orderFile = orderFilePath.toFile();
      final Writer fstream = new OutputStreamWriter(new FileOutputStream(orderFile, false), OrderUtil.CHARSET);
      fileOrder.writeTo(fstream);
      fstream.close();
    } catch (NoSuchAlgorithmException e) {
      // clearly a developer error, reraise instead of propagating
      throw new RuntimeException(e);
    } catch (IOException e) {
      System.out.println("error writing order file");
      System.out.println(e.getMessage());
      System.exit(1);
    }
  }

  private static void generateIntegrityFile(final Path dipDir, final Path integrityFilePath) {
    final List<String> fileOrderList = fileOrder.getIdentifiers();
    final HashForest<SHA512HashValue> hf = new HashForest<SHA512HashValue>();
    try {
      for (final String fileName : fileOrderList) {
        final Path filePath = Paths.get(dipDir.toString(), fileName);
        final SHA512HashValue hashValue = FileUtil.getHash(filePath.toString());
        hf.update(hashValue);
      }
      hf.setOrderInformationLocation(OrderUtil.ORDERFILENAME);
      hf.pruneForest();
      Files.createFile(integrityFilePath);
      final File integrityFile = integrityFilePath.toFile();
      final Writer fstream = new OutputStreamWriter(new FileOutputStream(integrityFile, false), HashForest.CHARSET);
      hf.writeTo(fstream);
      fstream.close();
    } catch (NoSuchAlgorithmException e) {
      // clearly a developer error, reraise instead of propagating
      throw new RuntimeException(e);
    } catch (IOException e) {
      System.out.println("error writing integrity file");
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
