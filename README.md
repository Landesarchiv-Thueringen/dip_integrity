## DIP2Go - Integritätsprüfung

Dieses Projekt stellt Ihnen eine Java-Biliothek zur Integration in eigene Projekte, eine Kommandozeilen-Anwendung zum Erstellen und Testen der Integritätsinformation von Nutzungspaketen und eine grafische Benutzeroberfläche zum Testen der Integrität zur Verfügung.

### Bibliothek

bla

### Kommandozeilen-Anwendung

bla bla

### Grafische Benutzeroberfläche

bla bla bla

#### Erfolgreiche Integritätsprüfung

![gui-succes](doc/screenshot/gui_success.png "erfolgreichen Integritätsprüfung")

Bei einer erfolgreichen Integritätsprüfung wird eine entsprechende Erfolgsmeldung ausgegeben.

#### Primärdateien wurden verändert

![gui-hash-tree-fail](doc/screenshot/gui_hash_tree_fail.png "fehlgeschlagene Integritätsprüfung")

Bei einer negativen Integritätsprüfung wird eine entsprechende Fehlermeldung ausgegeben.

#### Warnung vor Zusätzlichen Primärdateien im Nutzungspaket

![gui-warning](doc/screenshot/gui_warning_additional_files.png "Warnung vor zusätzlichen Dateien")

Wenn Dateien im Verzeichnis des Nutzungspakets enthalten sind, die nicht bei der Erstellung der Integritätsinformation vorhanden waren, wird eine entsprechende Warnung ausgegeben, welche die zusätzlichen Dateien auflistet. Die Dateiliste wird abgekürzt, wenn diese auf Grund von fehlendem Platz nicht dargestellt werden kann.

#### Fehler bei fehlenden Dateien

![gui-error-missing-file](doc/screenshot/gui_missing_file.png "Fehler wegen fehlender Primärdatei")

Wenn im Nutzungspaket Primärdateien fehlen, kann die Integrität nicht weiter geprüft werden und es wird eine entsprechende Fehlermeldung ausgegeben.
