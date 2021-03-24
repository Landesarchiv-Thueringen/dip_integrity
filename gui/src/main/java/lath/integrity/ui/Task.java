package lath.integrity.ui;

public class Task {

  public String description;
  public double progress;
  public boolean indefinite;

  public Task(final String description) {
    this.description = description;
    this.progress = 0.0;
    this.indefinite = true;
  }

  public Task(final String description, final boolean indefinite) {
    this.description = description;
    this.progress = 0.0;
    this.indefinite = indefinite;
  }
}
