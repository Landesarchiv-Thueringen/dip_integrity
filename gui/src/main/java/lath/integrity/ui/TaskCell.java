package lath.integrity.ui;

import javafx.scene.control.ListCell;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.BorderPane;
import javafx.scene.control.Label;

public class TaskCell extends ListCell<Task> {

  @Override
  public void updateItem(final Task task, final boolean empty) {
    super.updateItem(task, empty);
    if (empty) {
      setText(null);
      setGraphic(null);
    } else {
      setText(null);
      final Label descriptionLabel = new Label(task.description);
      final ProgressIndicator progressIndicator = new ProgressIndicator();
      if (!task.indefinite || task.progress > 0.0) {
        progressIndicator.setPrefHeight(40);
        progressIndicator.setProgress(task.progress);
      } else {
        progressIndicator.setPrefHeight(20);
      }
      final BorderPane layout = new BorderPane();
      layout.setLeft(descriptionLabel);
      layout.setRight(progressIndicator);
      setGraphic(layout);
    }
  }
}
