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
import javafx.scene.control.ListView;
import javafx.util.Callback;

public class TaskCellFactory implements Callback<ListView<Task>, ListCell<Task>> {

  @Override
  public ListCell<Task> call(ListView<Task> listView) {
    return new TaskCell();
  }
}
