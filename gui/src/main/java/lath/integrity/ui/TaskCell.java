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
