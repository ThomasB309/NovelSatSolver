LaTeX-Vorlage für Abschlussarbeiten
=======
(English Version below.)

Dies ist die Vorlage für Abschlussarbeiten am Lehrstuhl Software Design and
Quality (SDQ) am Institut für Datenorganisation und Programmstrukturen (IPD)
des Karlsruher Instituts für Technologie (KIT).

Vielen Dank an Markus Kohm (http://www.komascript.de) für die hilfreiche
Unterstützung beim Erstellen dieser Vorlage.

Version
=======
Version: 1.3.5
Autor: Dr.-Ing. Erik Burger (burger@kit.edu)
mit Beiträgen von Joshua Gleitze

Siehe https://sdqweb.ipd.kit.edu/wiki/Dokumentvorlagen

Verwendung
==========
Das vorliegende Paket dient als Vorlage für eine Abschlussarbeit. Sie können dazu
die bestehende Hauptdatei `thesis.tex` und die einzelnen Kapiteldateien im
Verzeichnis `sections/` anpassen, indem Sie den Beispieltext entfernen und durch
eigene Inhalte entfernen. Bitte ändern Sie keine Layout-Parameter wie
Schriftgröße, Ränder, Zeilenabstände u.ä. an der Datei, damit die Ausarbeitungen
in einem einheitlichen Format erscheinen.

Die Klasse basiert auf `scrbook` aus dem Paket KOMA-Script. Somit können alle 
Optionen dieser Klasse verwendet werden. 

Sprache
-------
Die Sprache des Dokuments ist standardmäßig auf Englisch eingestellt.
Dies kann in der `\documentclass`-Anweisung am Anfang von `thesis.tex` auf Deutsch 
umgestellt werden.

Einseitig/doppelseitig
----------------------
Das Dokument ist standardmäßig auf doppelseitiges Layout eingestellt, kann aber
durch die Angabe von `oneside` in der `\documentclass`-Anweisung am Anfang von
`thesis.tex` auf einseitiges Layout umgestellt werden.

Draft-Modus
-----------
Der Draft-Modus kann verwendet werden, um eine Entwurfsfassung zu generieren. 
Das kann durch die Option `draft` in der `\documentclass`-Anweisung am Anfang von `seminar.tex` geschehen, oder durch eine Einstellung innerhalb der LaTeX-Umgebung
(z.B. Overleaf: "Compile Mode: Fast (draft)").
Die entsprechende Option für das endgültige Dokument lautet `final`.
Im Draft-Modus werden z.B. todo-Notizen sowie Platzhalter für fehlende Abbildungen angezeigt, im `final`-Modus jedoch ausgeblendet.

LaTeX allgemein
---------------
Siehe https://sdqweb.ipd.kit.edu/wiki/LaTeX

Dateistruktur
============
`thesis.tex`
------------
Dies ist die Hauptdatei des LaTeX-Dokuments. Bitte tragen Sie dort Ihre
Daten ein. Benennen Sie dann die Datei am besten um, damit sie später von
Ihrem Betreuer von anderen leicht unterschieden werden kann
(z.B. in `thesis_erik_burger.tex`).

`thesis.bib`
------------
Dies ist eine BibTeX-Datei, in der Sie Ihre Literatur-Referenzen sammeln
können. Wir empfehlen die Verwendung von biblatex und biber statt BibTeX. 
Dies ist in der Ausarbeitungsvorlage bereits so voreingestellt. Für 
SDQ-relevante Publikationen können Sie die zentralen BibTeX-Dateien einbinden,
siehe https://sdqweb.ipd.kit.edu/wiki/BibTeX-Literaturlisten

`sdqthesis.cls`
---------------
Dies ist die Vorlage, die die Stil-Informationen für das Dokument enthält.
Im Sinne eines einheitlichen Ausarbeitungsstils soll diese Datei nicht
verändert werden.

`images/`
--------
In diesem Verzeichnis befinden das das SDQ-Logo als PDF und EPS.

`sections/`
-----------
In diesem Verzeichnis können Sie ihre Inhaltsabschnitte als einzelne
`.tex`-Dateien anlegen. Wir empfehlen Ihnen das Aufteilen der Dateien nach
Abschnitten.

`README.md`
-----------
Dieser Text.

English Version
===============
This is a template for student's final theses at the chair of Software Design
and Quality (SDQ) at the Institute of Program Structures and Data Organization
(IPD) at Karlsruhe Institute of Technology (KIT).

Many thanks to Markus Kohm (http://www.komascript.de) for his support in
creating the template.

Version
=======
Version: 1.3.5
Author: Dr.-Ing. Erik Burger (burger@kit.edu)
with contributions by Joshua Gleitze

See https://sdqweb.ipd.kit.edu/wiki/Dokumentvorlagen

Usage
=====
This package is used as a template for student final theses. To use it, adapt
the main file `thesis.tex` and the files for the chapters in the directory
`sections/` by removing the example text and replacing it with your own content.
Please do not change any layout parameters such as font size, margins, line
spacing etc., so that the theses appear in a uniform way.

The class is based on `scrbook` from the KOMA-Script package. All options of
this class can be used here as well.

Language
--------
The standard language of this document is English. You can change the
language in the `\documentclass` command at the beginning of `thesis.tex`.
German and English are available.

One-sided/two-sided layout
--------------------------
The standard format is two-sided layout. You can change this to one-sided
layout in the `\documentclass` command at the beginning of `thesis.tex`.

Draft mode
----------
The draft mode can be activated with the option `draft`
in the `\documentclass` command at the beginning of `seminar.tex`,
or by choosing the appropriate compile mode in the LaTeX IDE
(e.g., in Overleaf: "Compile Mode: Draft (Fast)")
In `draft` mode, todo-notes and placeholders for missing graphics are displayed,
while they are omitted in `final` mode.

LaTeX
-----
See https://sdqweb.ipd.kit.edu/wiki/LaTeX

File structure
==============
`thesis.tex`
------------
This is the main file of your LaTeX document. Please insert your data there.
It is recommended to rename the file so that your advisor can distinguish
different theses (e.g., in `thesis_erik_burger.tex`).

`thesis.bib`
------------
You can use this BibTeX file to collect your literature.
We recommend using biblatex and biber instead of BibTeX.
The template is already configured in this way.
You can include the SDQ literature database for SDQ-relevant publications,
see https://sdqweb.ipd.kit.edu/wiki/BibTeX-Literaturlisten

`sdqthesis.cls`
---------------
This is the style template for the document. Please do not modify this file,
so that all theses appear in the same style.

`images/`
--------
This directory contains the SDQ logo in PDF and EPS.

`sections/`
-----------
This directory contains your content sections, split in single `.tex` files.
We recommend splitting your sections into single files.

`README.md`
-----------
This text.