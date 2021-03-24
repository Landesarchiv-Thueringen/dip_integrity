package lath.integrity.ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import lath.integrity.error.InvalidInputException;
import lath.integrity.hashforest.HashForest;
import lath.integrity.hashforest.SHA512HashValue;
import lath.integrity.util.ChecksumUtil;
import lath.integrity.util.ErrorUtil;
import lath.integrity.util.FileUtil;
import lath.integrity.util.OrderUtil;

public class DipIntegrityValidator extends Application {

  // user interface
  private final Button chooseDipButton = new Button("Verzeichnis wählen");
  private final ObservableList<Task> taskList = FXCollections.observableArrayList();
  private final ListView<Task> taskListView = new ListView<Task>(taskList);
  private static final int TASK_LIST_ITEM_HEIGHT = 50;
  private final Label warningMessageLabel = new Label();
  private final Label successMessageLabel = new Label();
  private final Label errorMessageLabel = new Label();
  private final Image icon = new Image("/icon.png");
  private final Image logo = new Image("/logo.jpg");
  private final ImageView logoImageView = new ImageView(logo);
  private final HBox logoLayout = new HBox(logoImageView);
  private final Region logoContentSpacer = new Region();
  private final Region warningMessageSpacer = new Region();
  private final Region controlSpacer = new Region();
  private final Region bottomSpacer = new Region();
  private final HBox controlLayout = new HBox(chooseDipButton);
  private final VBox rootLayout = new VBox(
    logoLayout,
    logoContentSpacer,
    taskListView,
    warningMessageLabel,
    warningMessageSpacer,
    errorMessageLabel,
    successMessageLabel,
    controlSpacer,
    controlLayout,
    bottomSpacer
  );
  private final Scene scene = new Scene(rootLayout, 650, 650);

  // integrity check
  private HashForest<SHA512HashValue> expectedHashForrest;
  private HashForest<SHA512HashValue> actualdHashForrest;
  private OrderUtil fileOrder;

  public static void main(String[] args) {
    launch(args);
  }

  @Override
  public void start(final Stage primaryStage) {
    primaryStage.setTitle("DIP2Go - Integritätsprüfung");
    primaryStage.getIcons().add(icon);
    primaryStage.setResizable(false);
    scene.getStylesheets().add("main.css");
    primaryStage.setScene(scene);
    initLayout();
    initControls(primaryStage);
    initTaskListView();
    initSuccessMessage();
    initErrorMessage();
    initWarningMessage();
    primaryStage.show();
  }

  private void initLayout() {
    rootLayout.setVgrow(controlSpacer, Priority.ALWAYS);
    rootLayout.setStyle("-fx-background-color: white;");
    logoLayout.setAlignment(Pos.CENTER);
    controlLayout.setAlignment(Pos.CENTER);
    logoContentSpacer.setPrefHeight(30);
    warningMessageSpacer.setPrefHeight(20);
    controlSpacer.setPrefHeight(20);
    bottomSpacer.setPrefHeight(30);
  }

  private void initControls(final Stage primaryStage) {
    chooseDipButton.setOnAction(actionEvent ->  {
      taskList.clear();
      hideWarningMessage();
      hideErrorMessage();
      hideSuccessMessage();
      addSelectDipTask();
      final File dipDir = selectDipDir(primaryStage);
      if (dipDir != null) {
        taskList.get(0).progress = 1.0;
        if(readIntegrityFile(dipDir) && readFileOrder(dipDir) && readDipFiles(dipDir)) {
          validateDip();
        }
      }
    });
  }

  private void initTaskListView() {
    taskListView.setCellFactory(new TaskCellFactory());
    // make list items not selectable
    taskListView.setMouseTransparent(true);
    taskListView.setFocusTraversable(false);
    // shrink list view to item size
    taskListView.prefHeightProperty().bind(Bindings.size(taskList).multiply(TASK_LIST_ITEM_HEIGHT));
    addSelectDipTask();
  }

  private void addSelectDipTask() {
    taskList.add(new Task("1. Bitte wählen Sie das zu prüfende Nutzungspaket aus."));
  }

  private void initSuccessMessage() {
    hideSuccessMessage();
    successMessageLabel.setTextFill(Color.web("#2E8B57"));
    successMessageLabel.setStyle("-fx-font-weight: bold;");
    successMessageLabel.setWrapText(true);
  }

  private void showSuccessMessage(final String successMessage) {
    successMessageLabel.setText(successMessage);
    successMessageLabel.setVisible(true);
    successMessageLabel.setManaged(true);
  }

  private void hideSuccessMessage() {
    successMessageLabel.setVisible(false);
    successMessageLabel.setManaged(false);
  }

  private void initErrorMessage() {
    hideErrorMessage();
    errorMessageLabel.setTextFill(Color.web("#B22222"));
    errorMessageLabel.setStyle("-fx-font-weight: bold;");
    errorMessageLabel.setWrapText(true);
  }

  private void showErrorMessage(final String errorMessage) {
    errorMessageLabel.setText(errorMessage);
    errorMessageLabel.setVisible(true);
    errorMessageLabel.setManaged(true);
  }

  private void hideErrorMessage() {
    errorMessageLabel.setVisible(false);
    errorMessageLabel.setManaged(false);
  }

  private void initWarningMessage() {
    hideWarningMessage();
    warningMessageLabel.setTextFill(Color.web("#FF4500"));
    warningMessageLabel.setStyle("-fx-font-weight: bold;");
    warningMessageLabel.setWrapText(true);
  }

  private void showWarningMessage(final String warningMessage) {
    warningMessageLabel.setText(warningMessage);
    warningMessageLabel.setVisible(true);
    warningMessageLabel.setManaged(true);
    warningMessageSpacer.setManaged(true);
    warningMessageSpacer.setVisible(true);
  }

  private void hideWarningMessage() {
    warningMessageLabel.setVisible(false);
    warningMessageLabel.setManaged(false);
    warningMessageSpacer.setManaged(false);
    warningMessageSpacer.setVisible(false);
  }

  private File selectDipDir(final Stage primaryStage) {
    final DirectoryChooser fileChooser = new DirectoryChooser();
    fileChooser.setTitle("DIP2Go - Integritätsprüfung - Verzeichnis auswählen");
    fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
    return fileChooser.showDialog(primaryStage);
  }

  private boolean readIntegrityFile(final File dipDir) {
    final int taskId = taskList.size();
    taskList.add(new Task("2. Datei-Integritätsinformationen werden eingelesen."));
    boolean success = true;
    expectedHashForrest = new HashForest<SHA512HashValue>();
    File integrityFile = new File(dipDir, HashForest.INTEGRITYFILENAME);
    if (integrityFile.isFile() && integrityFile.canRead() && integrityFile.length() != 0) {
      try {
        expectedHashForrest.readFrom(new FileReader(integrityFile, HashForest.CHARSET));
      } catch (FileNotFoundException e) {
        showErrorMessage(ErrorUtil.getFileErrorMessage(
          HashForest.INTEGRITYFILENAME,
          ErrorUtil.ErrorType.FILE_NOT_FOUND
        ));
        success = false;
      } catch (IOException e) {
        showErrorMessage(ErrorUtil.getFileErrorMessage(
          HashForest.INTEGRITYFILENAME,
          ErrorUtil.ErrorType.FILE_NOT_READABLE
        ));
        success = false;
      } catch (InvalidInputException e) {
        showErrorMessage(ErrorUtil.getFileErrorMessage(
          HashForest.INTEGRITYFILENAME,
          ErrorUtil.ErrorType.FILE_FORMAT_INVALID
        ));
        success = false;
      }
    } else {
      success = false;
      showErrorMessage(ErrorUtil.getFileErrorMessage(
        HashForest.INTEGRITYFILENAME,
        ErrorUtil.ErrorType.FILE_NOT_READABLE
      ));
    }
    taskList.get(taskId).progress = 1.0;
    return success;
  }

  private boolean readFileOrder(final File dipDir) {
    final int taskId = taskList.size();
    taskList.add(new Task("3. Datei-Ordnungsinformationen werden eingelesen."));
    boolean success = true;
    final File orderingFile = new File(dipDir, OrderUtil.ORDERFILENAME);
    if (orderingFile.isFile() && orderingFile.canRead() && orderingFile.length() != 0) {
      try {
        final ChecksumUtil checksumProvider = new ChecksumUtil(MessageDigest.getInstance("SHA-512"));
        fileOrder = new OrderUtil(checksumProvider);
        fileOrder.readFrom(new FileReader(orderingFile, OrderUtil.CHARSET));
      } catch (NoSuchAlgorithmException e) {
        // clearly a developer error, reraise instead of propagating to ui
        throw new RuntimeException(e);
      } catch (FileNotFoundException e) {
        showErrorMessage(ErrorUtil.getFileErrorMessage(
          OrderUtil.ORDERFILENAME,
          ErrorUtil.ErrorType.FILE_NOT_FOUND
        ));
        success = false;
      } catch (IOException e) {
        showErrorMessage(ErrorUtil.getFileErrorMessage(
          OrderUtil.ORDERFILENAME,
          ErrorUtil.ErrorType.FILE_NOT_READABLE
        ));
        success = false;
      } catch (InvalidInputException e) {
        showErrorMessage(ErrorUtil.getFileErrorMessage(
          OrderUtil.ORDERFILENAME,
          ErrorUtil.ErrorType.FILE_FORMAT_INVALID
        ));
        success = false;
      }
    } else {
      success = false;
      showErrorMessage(ErrorUtil.getFileErrorMessage(
        OrderUtil.ORDERFILENAME,
        ErrorUtil.ErrorType.FILE_NOT_READABLE
      ));
    }
    taskList.get(taskId).progress = 1.0;
    return success && checkFileOrder(dipDir.toPath());
  }

  private boolean checkFileOrder(final Path dipDir) {
    boolean success = true;
    final List<String> expectedFileList = fileOrder.getIdentifiers();
    try {
      // get all relative file paths in dip directory
      final List<String> actualFileList = Files.walk(dipDir)
        .filter(Files::isRegularFile)
        .map(path -> path.subpath(dipDir.getNameCount(), path.getNameCount()).toString())
        .collect(Collectors.toList());
      actualFileList.remove(HashForest.INTEGRITYFILENAME);
      actualFileList.removeAll(expectedFileList);
      if (actualFileList.size() > 0) {
        showWarningMessage("Im ausgewählten Nutzungspaket befinden sich zusätzliche Dateien.");
      }
    } catch (IOException e) {
      showErrorMessage("Die Dateien in ihrem Nutzungspaket können nicht gelesen werden.");
      success = false;
    }
    return success;
  }

  private boolean readDipFiles(final File dipDir) {
    final int fileNumber = fileOrder.getIdentifiers().size();
    int currentFile = 1;
    final int taskId = taskList.size();
    taskList.add(new Task(getFileReadingMessage(currentFile, fileNumber), false));
    final Task task = taskList.get(taskId);
    actualdHashForrest = new HashForest<SHA512HashValue>();
    boolean success = true;
    for (String fileName : fileOrder.getIdentifiers()) {
      try {
        final SHA512HashValue fileHash = FileUtil.getHash(new File(dipDir, fileName).getAbsolutePath());
        actualdHashForrest.update(fileHash);
        if (currentFile < fileNumber) ++currentFile;
        task.description = getFileReadingMessage(currentFile, fileNumber);
        task.progress = (double) currentFile / fileNumber;
      } catch (NoSuchAlgorithmException e) {
        // clearly a developer error, reraise instead of propagating to ui
        throw new RuntimeException(e);
      } catch (FileNotFoundException e) {
        showErrorMessage(ErrorUtil.getFileErrorMessage(
          fileName,
          ErrorUtil.ErrorType.FILE_NOT_FOUND
        ));
        success = false;
        break;
      } catch (IOException e) {
        showErrorMessage(ErrorUtil.getFileErrorMessage(
          fileName,
          ErrorUtil.ErrorType.FILE_NOT_READABLE
        ));
        success = false;
        break;
      }
    }
    return success;
  }

  private String getFileReadingMessage(final int currentFile, final int fileNumber) {
    return "4. Datei " + currentFile + " von " + fileNumber + " wird eingelesen.";
  }

  private void validateDip() {
    final int taskId = taskList.size();
    taskList.add(new Task("5. Integrität des Nutzungspakets wird überprüft."));
    if (expectedHashForrest.validate(actualdHashForrest)) {
      showSuccessMessage("Die Prüfung wurde erfolgreich beendet. Ihr Nutzungspaket ist unverändert.");
    } else {
      showErrorMessage("Die Prüfung ist fehlgeschlagen. Ihr Nutzungspaket ist beschädigt oder verändert.");
    }
    taskList.get(taskId).progress = 1.0;
  }
}
