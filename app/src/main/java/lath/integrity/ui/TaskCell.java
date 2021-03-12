package lath.integrity.ui;

import javafx.scene.control.ListCell;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.BorderPane;
import javafx.scene.control.Label;

public class TaskCell extends ListCell<Task> {

  private final Label descriptionLabel = new Label();
  private final ProgressIndicator progressIndicator = new ProgressIndicator();
  private final BorderPane layout = new BorderPane();

  @Override
  public void updateItem(final Task task, final boolean empty) {
    super.updateItem(task, empty);
    if (empty) {
      setText(null);
      setGraphic(null);
    } else {
      setText(null);
      descriptionLabel.setText(task.description);
      progressIndicator.setProgress(task.progress);
      layout.setLeft(descriptionLabel);
      layout.setRight(progressIndicator);
      setGraphic(layout);
    }
  }
}
