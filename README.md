## DIP2Go - Integritätsprüfung

Das Ziel dieses Projekts ist es eine Möglichkeit zur Verfügung zustellen, mit denen Nutzungspakete noch Jahre nach dem Export auf ihre Integrität überprüft werden können. Zu diesem Zweck hat das Landesarchiv Thüringen in Kooperation mit der [Professur für Mediensicherheit der Bauhaus-Universiät Weimar](https://www.uni-weimar.de/de/medien/professuren/medieninformatik/mediensicherheit/home/) mehrere Java-Komponenten entwickelt.

Dieses Projekt stellt Ihnen eine Java-Biliothek zur Integration in eigene Projekte, eine Kommandozeilen-Anwendung zum Erstellen und Testen der Integritätsinformation von Nutzungspaketen und eine grafische Benutzeroberfläche zum Testen der Integrität zur Verfügung.

### Nachnutzung

Alle bereitgestellten Komponenten können kostenfrei nachgenutzt werden, die genauen Bedingungen können Sie in der [Lizenzvereinbarung](./LICENSE) nachlesen. Die aktuellen JAR-Dateien für alle Komponenten finden Sie immer im [jar Ordner](./jar). Diese können Sie auch direkt verwenden.

#### Voraussetzungen

Für die Verwendung der Komponenten benötigen Sie ausschließlich die Java Runtime Environment. Wir empfehlen Ihnen die aktuelle Version zu nutzen, wenn dies nicht möglich ist, sollte keine ältere Version als Java JRE 11 gewählt werden. Die Komponenten sollten auf allen gängigen Betriebssystemen funktionieren, getestet wurde die Funktionalität bereits auf Ubuntu 20.10 und Windows 10.

#### Kompilieren

Sollten doch einmal Quellcode Anpassungen nötig sein, ist auch das ohne größere Probleme möglich. In diesem Fall benötigen Sie ein Java JDK. Das JDK sollte nicht älter als Version 11 sein. Dieses Projekt verwendet das Build-Tool Gradle. Die Dokumentation für das Tool können Sie unter [https://docs.gradle.org/current/userguide/userguide.html](https://docs.gradle.org/current/userguide/userguide.html) abrufen. Gradle müssen Sie nicht extra installieren. Sie brauchen allerdings eine funktionierende Internetverbindung, damit Gradle die benötigten Bibliotheken laden kann.

 Wie Sie Gradle hinter einem Proxy verwenden, können Sie unter [https://docs.gradle.org/current/userguide/build_environment.html](https://docs.gradle.org/current/userguide/build_environment.html) nachlesen.

 Wenn Sie die Voraussetzungen erfüllt haben müssen Sie zum Erstellen der JAR-Dateien nur noch folgenden Befehl im Wurzelverzeichnis des Projekts ausführen:

 ```
./gradlew build
 ```

Sie können das Projekt auch in Ihre bevorzugte IDE importieren, die meisten müssten mittlerweile die Gradle-Projekt-Dateien erkennen und automatisch integrieren.

### Bibliothek

Die Java Bibliothek kapselt alle Funktionen die zum Erstellen und Prüfen der Integritätsdateien benötigt werden. Bei den Integritätsdateien handelt es sich zum einen um eine Datei, welche die Datei Reihenfolge für das Nutzungspaket dokumentiert, diese ist essenziell für die Prüfung. Die zweite Datei speichert alle weiteren Informationen, die für die Prüfung benötigt werden. Sie können die Bibliothek wie jede andere Java Bibliothek in ein beliebiges Build-Tool einbinden und die bereitgestellten Klassen verwenden. So könnte bspw. die Komponente, die Ihre Nutzungspakete erzeugt so erweitert werden, dass die Integritätsdateien vor dem Export direkt in das Nutzungspaket integriert werden. Wie Sie die Bibliothek in Kommandozeilen-Anwendungen oder grafische Benutzeroberfläche verwenden, können Sie den anderen Komponenten entnehmen.

### Kommandozeilen-Anwendung

Die Kommandozeilen-Anwendung ist nur eine Beispielanwendung um die Grundfunktionen der Bibliothek zu testen. In der Praxis wird man die Integritätsinformationen nicht manuell für eine Vielzahl von Nutzungspaketen erstellen. Deutlich sinnvoller wäre es die Funktionalität der Bibliothek in die eigenen Dienste zu integrieren.

#### Integritätsinformationen erstellen

```
java -jar ./bin/dip_integrity_cli-1.0-all.jar -c ../DIP
```

#### Integritätsinformationen mit vollem Hashbaum speichern

```
java -jar ./bin/dip_integrity_cli-1.0-all.jar -c ../DIP -f
```

#### Integritätsinformationen prüfen

```
java -jar ./bin/dip_integrity_cli-1.0-all.jar -t ../DIP
```

### Grafische Benutzeroberfläche

Die grafische Benutzeroberfläche dient ausschließlich dem Testen der Integritätsinformation. Die Anwendung soll den eigentlichen Nutzern der Nutzungspakete zur Verfügung gestellt werden, damit diese jederzeit die Integrität überprüfen können.

#### Logo

Sie können das Logo im Kopf der Anwendung durch ihr eigenes austauschen ohne den Quellcode dafür zu verändern. Sie müssen dafür nur die Datei unter [gui\src\main\resources\logo.png](gui\src\main\resources\logo.png) durch ihr Logo austauschen und das Projekt neu Kompilieren. Die Anwendung skaliert Ihr Logo so das eine Auflösung von 600px x 100px bestmöglich ausgefüllt wird. Dabei wird das Seitenverhältnis des Bildes nicht verändert. Die besten Ergebnisse werden Sie aber erzielen, wenn Sie das Logo selbst so anpassen, dass es die vorgegebene Größe einhält.

#### Icon

Sie können das Icon im Kopf der Anwendung durch ihr eigenes austauschen ohne den Quellcode dafür zu verändern. Sie müssen dafür nur die Datei unter [gui\src\main\resources\icon.png](gui\src\main\resources\icon.png) durch ihr eigenes Icon austauschen und das Projekt neu Kompilieren. Das Icon sollte eine Auflösung von 64px x 64px haben.

<!------------------------------------------------------------------------------------------------->
<div class="page-break" />
<!------------------------------------------------------------------------------------------------->

#### Nutzungsszenarien

__Erfolgreiche Integritätsprüfung__

![gui-succes](img/screenshot/gui_success.png "erfolgreichen Integritätsprüfung")

Bei einer erfolgreichen Integritätsprüfung wird eine entsprechende Erfolgsmeldung ausgegeben.

<!------------------------------------------------------------------------------------------------->
<div class="page-break" />
<!------------------------------------------------------------------------------------------------->

__Primärdateien wurden verändert__

![gui-hash-tree-fail](img/screenshot/gui_hash_tree_fail.png "fehlgeschlagene Integritätsprüfung")

Bei einer negativen Integritätsprüfung wird eine entsprechende Fehlermeldung ausgegeben.

Wenn der volle Hashbaum gespeichert wurde, können die Dateien, die sich seit der Generierung der Integritätsinformationen verändert haben, aufgelistet werden.

<!------------------------------------------------------------------------------------------------->
<div class="page-break" />
<!------------------------------------------------------------------------------------------------->

__Warnung vor zusätzlichen Primärdateien im Nutzungspaket__

![gui-warning](img/screenshot/gui_warning_additional_files.png "Warnung vor zusätzlichen Dateien")

Wenn Dateien im Verzeichnis des Nutzungspakets enthalten sind, die nicht bei der Erstellung der Integritätsinformation vorhanden waren, wird eine entsprechende Warnung ausgegeben, welche die zusätzlichen Dateien auflistet. Die Dateiliste wird abgekürzt, wenn diese auf Grund von fehlendem Platz nicht dargestellt werden kann.

<!------------------------------------------------------------------------------------------------->
<div class="page-break" />
<!------------------------------------------------------------------------------------------------->

__Fehler bei fehlenden Primärdateien__

![gui-error-missing-file](img/screenshot/gui_missing_file.png "Fehler wegen fehlender Primärdatei")

Wenn im Nutzungspaket Primärdateien fehlen, kann die Integrität nicht weiter geprüft werden und es wird eine entsprechende Fehlermeldung ausgegeben.

<!------------------------------------------------------------------------------------------------->
<div class="page-break" />
<!------------------------------------------------------------------------------------------------->

__Fehler bei fehlenden Integritätsdateien__

![gui-error-missing-integrity-file](img/screenshot/gui_missing_integrity_file.png "Fehler wegen fehlender Integritätsdateien")

Wenn im Nutzungspaket Integritätsdateien fehlen, kann die Integrität nicht weiter geprüft werden und es wird eine entsprechende Fehlermeldung ausgegeben.

<!------------------------------------------------------------------------------------------------->
<div class="page-break" />
<!------------------------------------------------------------------------------------------------->

__Fehler bei fehlerhafter Prüfsumme von Integritätsdateien__

![gui-error-checksum-integrity-file](img/screenshot/gui_integrity_file_checksum_fail.png "Fehler wegen falscher Prüfsumme von Integritätsdateien")

Wenn die Prüfsumme von einer Integritätsdateien falsch ist, kann die Integrität nicht weiter geprüft werden und es wird eine entsprechende Fehlermeldung ausgegeben. Durch die Prüfsumme sind die Integritätsdateien vor unbeabsichtigter Veränderung geschützt.

<!------------------------------------------------------------------------------------------------->
<div class="page-break" />
<!------------------------------------------------------------------------------------------------->

__Fehler bei fehlerhaften Schema von Integritätsdateien__

![gui-error-schema-integrity-file](img/screenshot/gui_integrity_file_schema_fail.png "Fehler wegen falschem Schema von Integritätsdateien")

Wenn das erwartete Schema von einer Integritätsdateien nicht erfüllt wird, kann die Integrität nicht weiter geprüft werden und es wird eine entsprechende Fehlermeldung ausgegeben.
