package thelma.integrity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import thelma.integrity.error.InvalidInputException;
import thelma.integrity.hashforest.HashForest;
import thelma.integrity.hashforest.SHA512HashValue;
import thelma.integrity.util.ChecksumUtil;
import thelma.integrity.util.ErrorUtil;
import thelma.integrity.util.FileUtil;
import thelma.integrity.util.OrderUtil;

public class DipIntegrityValidator extends Application {

  // user interface
  private final Button chooseDipButton = new Button("DIP-Verzeichnis wählen");
  private final ProgressBar validationProgressBar = new ProgressBar(0);
  private final ObservableList<String> taskList = FXCollections.observableArrayList();
  private final ListView taskListView = new ListView(taskList);
  private static final int TASK_LIST_ITEM_HEIGHT = 26;
  private final Label successMessageLabel = new Label();
  private final Label errorMessageLabel = new Label();
  private final Image icon = new Image("/icon.png");
  private final Image logo = new Image("/logo.jpg");
  private final ImageView logoImageView = new ImageView(logo);
  private final Region logoContentSpacer = new Region();
  private final Region controlSpacer = new Region();
  private final Region bottomSpacer = new Region();
  private final HBox controlLayout = new HBox(chooseDipButton);
  private final VBox rootLayout = new VBox(
    logoImageView,
    logoContentSpacer,
    taskListView,
    successMessageLabel,
    errorMessageLabel,
    controlSpacer,
    controlLayout,
    bottomSpacer,
    validationProgressBar
  );
  private final Scene scene = new Scene(rootLayout, 475, 425);

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
    primaryStage.show();
  }

  private void initLayout() {
    rootLayout.setVgrow(controlSpacer, Priority.ALWAYS);
    rootLayout.setStyle("-fx-background-color: white;");
    controlLayout.setAlignment(Pos.CENTER);
    logoContentSpacer.setPrefHeight(30);
    controlSpacer.setPrefHeight(20);
    bottomSpacer.setPrefHeight(20);
    validationProgressBar.setPrefWidth(475);
    validationProgressBar.setVisible(false);
  }

  private void initControls(final Stage primaryStage) {
    chooseDipButton.setOnAction(actionEvent ->  {
      taskList.clear();
      successMessageLabel.setVisible(false);
      errorMessageLabel.setVisible(false);
      addSelectDipTask();
      validationProgressBar.setVisible(false);
      validationProgressBar.setProgress(0);
      final File dipDir = selectDipDir(primaryStage);
      if (dipDir != null) {
        validationProgressBar.setVisible(true);
        if(readIntegrityFile(dipDir) && readFileOrder(dipDir) && readDipFiles(dipDir)) {
          validateDip();
        }
      }
    });
  }

  private void initTaskListView() {
    // make list items not selectable
    taskListView.setMouseTransparent(true);
    taskListView.setFocusTraversable(false);
    // shrink list view to item size
    taskListView.prefHeightProperty().bind(Bindings.size(taskList).multiply(TASK_LIST_ITEM_HEIGHT));
    addSelectDipTask();
  }

  private void addSelectDipTask() {
    taskList.add("1. Bitte wählen Sie das zu prüfende DIP-Verzeichnis aus.");
  }

  private void initSuccessMessage() {
    successMessageLabel.setVisible(false);
    successMessageLabel.setTextFill(Color.web("#2E8B57"));
    successMessageLabel.setWrapText(true);
  }

  private void showSuccessMessage(final String successMessage) {
    successMessageLabel.setText(successMessage);
    successMessageLabel.setVisible(true);
  }

  private void initErrorMessage() {
    errorMessageLabel.setVisible(false);
    errorMessageLabel.setTextFill(Color.web("#B22222"));
    errorMessageLabel.setWrapText(true);
  }

  private void showErrorMessage(final String errorMessage) {
    errorMessageLabel.setText(errorMessage);
    errorMessageLabel.setVisible(true);
  }

  private File selectDipDir(final Stage primaryStage) {
    final DirectoryChooser fileChooser = new DirectoryChooser();
    fileChooser.setTitle("DIP Ordner auswählen");
    fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
    return fileChooser.showDialog(primaryStage);
  }

  private boolean readIntegrityFile(final File dipDir) {
    taskList.add("2. Lese Datei-Integritätsinformationen");
    boolean success = true;
    expectedHashForrest = new HashForest<SHA512HashValue>();
    File integrityFile = new File(dipDir, HashForest.INTEGRITYFILENAME);
    if (integrityFile.isFile() && integrityFile.canRead() && integrityFile.length() != 0) {
      try {
        expectedHashForrest.readFrom(new FileReader(integrityFile));
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
    return success;
  }

  private boolean readFileOrder(final File dipDir) {
    taskList.add("3. Lese Datei-Ordnungsinformationen");
    boolean success = true;
    final File orderingFile = new File(dipDir, OrderUtil.ORDERFILENAME);
    if (orderingFile.isFile() && orderingFile.canRead() && orderingFile.length() != 0) {
      try {
        final ChecksumUtil checksumProvider = new ChecksumUtil(MessageDigest.getInstance("SHA-512"));
        fileOrder = new OrderUtil(checksumProvider);
        fileOrder.readFrom(new FileReader(orderingFile));
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
    return success;
  }

  private boolean readDipFiles(final File dipDir) {
    final int taskId = taskList.size();
    final int fileNumber = fileOrder.getIdentifiers().size();
    int currentFile = 1;
    taskList.add(getFileReadingMessage(currentFile, fileNumber));
    actualdHashForrest = new HashForest<SHA512HashValue>();
    boolean success = true;
    for (String fileName : fileOrder.getIdentifiers()) {
      try {
        final SHA512HashValue fileHash = FileUtil.getHash(new File(dipDir, fileName).getAbsolutePath());
        actualdHashForrest.update(fileHash);
        if (currentFile < fileNumber) ++currentFile;
        taskList.set(taskId, getFileReadingMessage(currentFile, fileNumber));
        validationProgressBar.setProgress(currentFile/fileNumber);
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
    return "4. Lese DIP-Datei " + currentFile + "/" + fileNumber;
  }

  private void validateDip() {
    taskList.add("5. Validiere DIP");
    if (expectedHashForrest.validate(actualdHashForrest)) {
      showSuccessMessage("Die DIP-Prüfung ist erfolgreich beendet wurden. Alle DIP-Dateien sind valide.");
    } else {
      showErrorMessage("Die DIP-Prüfung ist fehlgeschlagen. Die DIP-Dateien wurden beschädigt oder manipuliert.");
    }
  }
}
