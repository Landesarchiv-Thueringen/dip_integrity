/**
 * DIP Integrity Library for generation and validation of integrity information of DIP
 * Copyright (C) 2021 Tony Grochow (tony.grochow@la.thueringen.de)
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

package lath.integrity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
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

import lath.integrity.error.InvalidInputException;
import lath.integrity.hashforest.HashForest;
import lath.integrity.hashforest.SHA512HashValue;
import lath.integrity.util.ChecksumUtil;
import lath.integrity.util.ErrorUtil;
import lath.integrity.util.FileUtil;
import lath.integrity.util.OrderUtil;

public class DipIntegrityGenerator {

  private static final String invokeCommand = "java -jar dip_integrity_generation.jar";
  private static final HelpFormatter formatter = new HelpFormatter();
  private static final Options options = new Options();
  private static CommandLine cmd;

  private static final Option createIntegrityInformation = new Option(
    "c",
    "create",
    true,
    "Create integrity files for selected DIP directory."
  );
  private static final Option testIntegrityInformation = new Option(
    "t",
    "test",
    true,
    "Test integrity of selected DIP directory.");
  private static final Option fullHashTree = new Option(
    "f",
    "full", false,
    "Write full hash tree and not only the root nodes in the integrity file."
  );


  private static OrderUtil fileOrder;
  private static HashForest<SHA512HashValue> expectedHashForrest;
  private static HashForest<SHA512HashValue> actualdHashForrest;

  private static void createIntegrityInformation(final Path dipDir, final boolean fullHashTree) {
    final Path orderFilePath = Paths.get(dipDir.toString(), OrderUtil.ORDERFILENAME);
    final Path integrityFilePath = Paths.get(dipDir.toString(), HashForest.INTEGRITYFILENAME);
    try {
      Files.deleteIfExists(orderFilePath);
      Files.deleteIfExists(integrityFilePath);
    } catch (final IOException e) {
      System.out.println("Es gab einen Fehler beim L\u00f6schen veralteter Integrit\u00e4tsinformationen.");
      System.out.println(e.getMessage());
      System.exit(1);
    }
    final List<Path> fileList = getFileList(dipDir);
    generateOrderFile(orderFilePath, fileList);
    generateIntegrityFile(dipDir, integrityFilePath, fullHashTree);
    printIntegrityFileCreationSuccessMessage(
      dipDir,
      fileOrder.getIdentifiers().size(),
      fullHashTree,
      integrityFilePath,
      orderFilePath
    );
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
      System.out.println("Beim schreiben der Ordnungsinformationen kam es zu einem Fehler.");
      System.out.println(e.getMessage());
      System.exit(1);
    }
  }

  private static void generateIntegrityFile(
    final Path dipDir,
    final Path integrityFilePath,
    final boolean fullHashTree
  ) {
    final List<String> fileOrderList = fileOrder.getIdentifiers();
    final HashForest<SHA512HashValue> hf = new HashForest<SHA512HashValue>();
    try {
      for (final String fileName : fileOrderList) {
        final Path filePath = Paths.get(dipDir.toString(), fileName);
        final SHA512HashValue hashValue = FileUtil.getHash(filePath.toString());
        hf.update(hashValue);
      }
      hf.setOrderInformationLocation(OrderUtil.ORDERFILENAME);
      if (!fullHashTree) hf.pruneForest();
      Files.createFile(integrityFilePath);
      final File integrityFile = integrityFilePath.toFile();
      final Writer fstream = new OutputStreamWriter(new FileOutputStream(integrityFile, false), HashForest.CHARSET);
      hf.writeTo(fstream);
      fstream.close();
    } catch (NoSuchAlgorithmException e) {
      // clearly a developer error, reraise instead of propagating
      throw new RuntimeException(e);
    } catch (IOException e) {
      System.out.println("Beim schreiben der Integrit\u00e4tsinformationen kam es zu einem Fehler.");
      System.out.println(e.getMessage());
      System.exit(1);
    }
  }

  private static void printIntegrityFileCreationSuccessMessage(
    final Path dipDir,
    final int fileNumber,
    final boolean fullHashTree,
    final Path integrityFilePath,
    final Path orderFilePath
  ) {
    final StringBuilder statusMessage = new StringBuilder(500);
    statusMessage.append("\nDie Integrit\u00e4tsinformationen f\u00fcr das Nutzungspaket \"");
    statusMessage.append(dipDir);
    statusMessage.append("\" wurden erfolgreich erstellt.\n\n");
    statusMessage.append(fileNumber);
    statusMessage.append(" Dateien wurden f\u00fcr die Erstellung der Integrit\u00e4tsinformationen ber\u00fccksichtigt.\n");
    if (fullHashTree) {
      statusMessage.append("\nIn die Integrit\u00e4tsdatei wurde der komplette Hash-Forest geschrieben.\n");
    } else {
      statusMessage.append("\nIn die Integrit\u00e4tsdatei wurden nur die Wurzeln der Hash-Trees geschrieben.\n");
    }
    statusMessage.append("\nDie Integrit\u00e4tsdatei wurde unter \"");
    statusMessage.append(integrityFilePath);
    statusMessage.append("\" gespeichert.\n");
    statusMessage.append("Die Ordnungsdatei wurde unter \"");
    statusMessage.append(orderFilePath);
    statusMessage.append("\" gespeichert.\n");
    System.out.println(statusMessage);
  }

  private static void testIntegrityInformation(final Path dipDir) {
    if (readIntegrityFile(dipDir) && readFileOrder(dipDir) && readDipFiles(dipDir)) {
      validateDip();
    }
  }

  private static boolean readIntegrityFile(final Path dipDir) {
    System.out.println("\nDatei-Integrit\u00e4tsinformationen werden eingelesen.\n");
    boolean success = true;
    expectedHashForrest = new HashForest<SHA512HashValue>();
    final File integrityFile = Paths.get(dipDir.toString(), HashForest.INTEGRITYFILENAME).toFile();
    if (integrityFile.isFile() && integrityFile.canRead() && integrityFile.length() != 0) {
      try {
        expectedHashForrest.readFrom(new FileReader(integrityFile, HashForest.CHARSET));
      } catch (FileNotFoundException e) {
        System.out.println(ErrorUtil.getFileErrorMessage(
          HashForest.INTEGRITYFILENAME,
          ErrorUtil.ErrorType.FILE_NOT_FOUND
        ));
        success = false;
      } catch (IOException e) {
        System.out.println(ErrorUtil.getFileErrorMessage(
          HashForest.INTEGRITYFILENAME,
          ErrorUtil.ErrorType.FILE_NOT_READABLE
        ));
        success = false;
      } catch (InvalidInputException e) {
        System.out.println(ErrorUtil.getFileErrorMessage(
          HashForest.INTEGRITYFILENAME,
          ErrorUtil.ErrorType.FILE_FORMAT_INVALID
        ));
        success = false;
      }
    } else {
      success = false;
      System.out.println(ErrorUtil.getFileErrorMessage(
        HashForest.INTEGRITYFILENAME,
        ErrorUtil.ErrorType.FILE_NOT_READABLE
      ));
    }
    return success;
  }

  private static boolean readFileOrder(final Path dipDir) {
    System.out.println("Datei-Ordnungsinformationen werden eingelesen.\n");
    boolean success = true;
    final File orderingFile = Paths.get(dipDir.toString(), OrderUtil.ORDERFILENAME).toFile();
    if (orderingFile.isFile() && orderingFile.canRead() && orderingFile.length() != 0) {
      try {
        final ChecksumUtil checksumProvider = new ChecksumUtil(MessageDigest.getInstance("SHA-512"));
        fileOrder = new OrderUtil(checksumProvider);
        fileOrder.readFrom(new FileReader(orderingFile, OrderUtil.CHARSET));
      } catch (NoSuchAlgorithmException e) {
        // clearly a developer error, reraise instead of propagating to ui
        throw new RuntimeException(e);
      } catch (FileNotFoundException e) {
        System.out.println(ErrorUtil.getFileErrorMessage(
          OrderUtil.ORDERFILENAME,
          ErrorUtil.ErrorType.FILE_NOT_FOUND
        ));
        success = false;
      } catch (IOException e) {
        System.out.println(ErrorUtil.getFileErrorMessage(
          OrderUtil.ORDERFILENAME,
          ErrorUtil.ErrorType.FILE_NOT_READABLE
        ));
        success = false;
      } catch (InvalidInputException e) {
        System.out.println(ErrorUtil.getFileErrorMessage(
          OrderUtil.ORDERFILENAME,
          ErrorUtil.ErrorType.FILE_FORMAT_INVALID
        ));
        success = false;
      }
    } else {
      success = false;
      System.out.println(ErrorUtil.getFileErrorMessage(
        OrderUtil.ORDERFILENAME,
        ErrorUtil.ErrorType.FILE_NOT_READABLE
      ));
    }
    return success;
  }

  private static boolean readDipFiles(final Path dipDir) {
    final int fileNumber = fileOrder.getIdentifiers().size();
    int currentFile = 1;
    actualdHashForrest = new HashForest<SHA512HashValue>();
    boolean success = true;
    for (String fileName : fileOrder.getIdentifiers()) {
      try {
        System.out.println("Lese Date " + currentFile + " von " + fileNumber + " ein.");
        final Path filePath = Paths.get(dipDir.toString(), fileName);
        final SHA512HashValue fileHash = FileUtil.getHash(filePath.toString());
        actualdHashForrest.update(fileHash);
        ++currentFile;
      } catch (NoSuchAlgorithmException e) {
        // clearly a developer error, reraise instead of propagating to ui
        throw new RuntimeException(e);
      } catch (FileNotFoundException e) {
        System.out.println(ErrorUtil.getFileErrorMessage(
          fileName,
          ErrorUtil.ErrorType.FILE_NOT_FOUND
        ));
        success = false;
        break;
      } catch (IOException e) {
        System.out.println(ErrorUtil.getFileErrorMessage(
          fileName,
          ErrorUtil.ErrorType.FILE_NOT_READABLE
        ));
        success = false;
        break;
      }
    }
    return success;
  }

  private static void validateDip() {
    System.out.println("\nIntegrit\u00e4t des Nutzungspakets wird \u00fcberpr\u00fcft.");
    if (expectedHashForrest.validate(actualdHashForrest)) {
      System.out.println("\nDie Pr\u00fcfung wurde erfolgreich beendet. Ihr Nutzungspaket ist unver\u00e4ndert.\n");
    } else {
      System.out.println("\nDie Pr\u00fcfung ist fehlgeschlagen. Ihr Nutzungspaket ist besch\u00e4digt oder ver\u00e4ndert.\n");
    }
  }

  private static Path getDipDir(final String commandLineValue) {
    String pathValue = commandLineValue.replaceFirst("^~", System.getProperty("user.home"));
    final Path dipDir = Paths.get(pathValue).toAbsolutePath();
    if (!Files.isDirectory(dipDir)) {
      System.out.println("Der Pfad f\u00fcr das Nutzungspaket \"" + dipDir + "\" ist kein Ordner.");
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
      System.out.println("Die Dateien in ihrem Nutzungspaket k\u00f6nnen nicht ausgelesen werden.");
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
    options.addOption(fullHashTree);
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
      createIntegrityInformation(dipDir, cmd.hasOption("f"));
    } else if (cmd.hasOption("t")) {
      final Path dipDir = getDipDir(cmd.getOptionValue("t"));
      testIntegrityInformation(dipDir);
    } else {
      formatter.printHelp(invokeCommand, options);
    }
  }

}
