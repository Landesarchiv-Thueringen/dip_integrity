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
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
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

public class App extends Application {

  // user interface
  private final ObservableList<String> taskList = FXCollections.observableArrayList();
  private final ListView taskListView = new ListView(taskList);
  private final Label errorLabel = new Label();
  private final Image logo = new Image("logo.jpg", true);
  private final ImageView logoImageView = new ImageView(logo);
  private final Region logoContentSpacer = new Region();
  private final VBox layout = new VBox(logoImageView, logoContentSpacer, taskListView, errorLabel);
  private final Scene scene = new Scene(layout, 475, 475);

  private final HashForest<SHA512HashValue> hf = new HashForest<SHA512HashValue>();
  private OrderUtil fileOrder;

  public static void main(String[] args) {
    launch(args);
  }

  @Override
  public void start(final Stage primaryStage) {
    primaryStage.setTitle("ThELMA: DIP-Integritätsprüfung");
    scene.getStylesheets().add("main.css");
    primaryStage.setScene(scene);
    initLayout();
    initTaskListView();
    initErrorLabel();
    final File dipDir = selectDipDir(primaryStage);
    if (dipDir == null) {
      Platform.exit();
    } else {
      primaryStage.show();
      if(readIntegrityFile(dipDir) && readFileOrder(dipDir) && readDipFiles(dipDir)) {

      }
    }
  }

  private void initLayout() {
    layout.setStyle("-fx-background-color: white;");
    logoContentSpacer.setPrefHeight(20);
  }

  private void initTaskListView() {
    // make list items not selectable
    taskListView.setMouseTransparent(true);
    taskListView.setFocusTraversable(false);
    // shrink list view to item size
    taskListView.prefHeightProperty().bind(Bindings.size(taskList).multiply(26));
  }

  private void initErrorLabel() {
    errorLabel.setVisible(false);
    errorLabel.setTextFill(Color.web("#B22222"));
    errorLabel.setWrapText(true);
  }

  private void showError(final String errorMessage) {
    errorLabel.setText(errorMessage);
    errorLabel.setVisible(true);
  }

  private File selectDipDir(final Stage primaryStage) {
    final DirectoryChooser fileChooser = new DirectoryChooser();
    fileChooser.setTitle("DIP Ordner auswählen");
    fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
    return fileChooser.showDialog(primaryStage);
  }

  private boolean readIntegrityFile(final File dipDir) {
    boolean success = true;
    taskList.add("1. Lese Datei-Integritätsinformationen");
    File integrityFile = new File(dipDir, HashForest.INTEGRITYFILENAME);
    if (integrityFile.isFile() && integrityFile.canRead() && integrityFile.length() != 0) {
      try {
        hf.readFrom(new FileReader(integrityFile));
      } catch (FileNotFoundException e) {
        showError("Die Datei \"" + HashForest.INTEGRITYFILENAME + "\" existiert nicht.");
        success = false;
      } catch (IOException e) {
        showError("Die Datei \"" + HashForest.INTEGRITYFILENAME + "\" ist nicht lesbar.");
        success = false;
      } catch (InvalidInputException e) {
        showError("Die Datei \"" + HashForest.INTEGRITYFILENAME + "\" entspricht nicht dem erwarteten Format.");
        success = false;
      }
    } else {
      success = false;
      showError("Die Datei \"" + HashForest.INTEGRITYFILENAME + "\" ist nicht lesbar.");
    }
    return success;
  }

  private boolean readFileOrder(final File dipDir) {
    boolean success = true;
    taskList.add("2. Lese Datei-Ordnungsinformationen");
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
        showError("Die Datei \"" + OrderUtil.ORDERFILENAME + "\" existiert nicht.");
        success = false;
      } catch (IOException e) {
        showError("Die Datei \"" + OrderUtil.ORDERFILENAME + "\" ist nicht lesbar.");
        success = false;
      } catch (InvalidInputException e) {
        showError("Die Datei \"" + OrderUtil.ORDERFILENAME + "\" entspricht nicht dem erwarteten Format.");
        success = false;
      }
    } else {
      success = false;
      showError("Die Datei \"" + OrderUtil.ORDERFILENAME + "\" ist nicht lesbar.");
    }
    return true;
  }

  private boolean readDipFiles(final File dipDir) {
    boolean success = true;
    taskList.add("3. Lese DIP-Dateien");
    for (String fileName : fileOrder.getIdentifiers()) {
      try {
        final SHA512HashValue fileHash = FileUtil.getHash(new File(dipDir, fileName).getAbsolutePath());
      } catch (NoSuchAlgorithmException e) {
        // clearly a developer error, reraise instead of propagating to ui
        throw new RuntimeException(e);
      } catch (FileNotFoundException e) {
        showError(ErrorUtil.getFileErrorMessage(fileName, ErrorUtil.ErrorType.FILE_NOT_FOUND));
        success = false;
      } catch (IOException e) {
        showError(ErrorUtil.getFileErrorMessage(fileName, ErrorUtil.ErrorType.FILE_NOT_READABLE));
        success = false;
      }
    }
    return success;
  }
}
