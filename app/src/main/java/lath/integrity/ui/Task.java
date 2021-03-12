package lath.integrity.ui;

public class Task {

  public String description;
  public double progress;

  public Task(final String description) {
    this.description = description;
    this.progress = 0.0;
  }

  public Task(final String description, final double progress) {
    this.description = description;
    this.progress = progress;
  }
}
