# MedReportViewer-dicom-tools #

[![License](https://img.shields.io/badge/License-EPL%202.0-blue.svg)](https://opensource.org/licenses/EPL-2.0) [![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0) ![Maven Build](https://github.com/nroduit/MedReportViewer-dicom-tools/workflows/Build/badge.svg)  
[![Sonar](https://sonarcloud.io/api/project_badges/measure?project=MedReportViewer-dicom-tools&metric=ncloc)](https://sonarcloud.io/component_measures?id=MedReportViewer-dicom-tools) [![Sonar](https://sonarcloud.io/api/project_badges/measure?project=MedReportViewer-dicom-tools&metric=reliability_rating)](https://sonarcloud.io/component_measures?id=MedReportViewer-dicom-tools) [![Sonar](https://sonarcloud.io/api/project_badges/measure?project=MedReportViewer-dicom-tools&metric=sqale_rating)](https://sonarcloud.io/component_measures?id=MedReportViewer-dicom-tools) [![Sonar](https://sonarcloud.io/api/project_badges/measure?project=MedReportViewer-dicom-tools&metric=security_rating)](https://sonarcloud.io/component_measures?id=MedReportViewer-dicom-tools) [![Sonar](https://sonarcloud.io/api/project_badges/measure?project=MedReportViewer-dicom-tools&metric=alert_status)](https://sonarcloud.io/dashboard?id=MedReportViewer-dicom-tools)

This project provides a DICOM API for [C-Echo](src/main/java/org/MedReportViewer/dicom/op/Echo.java)
, [C-Move](src/main/java/org/MedReportViewer/dicom/op/CMove.java)
, [C-Get](src/main/java/org/MedReportViewer/dicom/op/CGet.java)
, [C-Find](src/main/java/org/MedReportViewer/dicom/op/CFind.java)
and [C-Store](src/main/java/org/MedReportViewer/dicom/op/CStore.java) based on dcm4che3. The implementation
allows to follow the progression of an DICOM operation like C-Move and gives its status. It contains
also some other classes for worklist SCU, strore SCP, dicomization, DICOM forward with attributes
modification on the fly and a DICOM gateway (experimental).

This project replaces [MedReportViewer-dicom-operations](https://github.com/nroduit/MedReportViewer-dicom-operations)
and now this library is used by recent versions
of [MedReportViewer-pacs-connector](https://github.com/nroduit/MedReportViewer-pacs-connector) and in the
MedReportViewer-dicom-codec module of [MedReportViewer](https://github.com/nroduit/MedReportViewer).

**Getting
started**: [see the test classes](https://github.com/nroduit/MedReportViewer-dicom-tools/tree/master/src/test/java/org/MedReportViewer/dicom)

Code formatter: [google-java-format](https://github.com/google/google-java-format)

## [Release History](CHANGELOG.md)

## Build MedReportViewer-dicom-tools ##

Prerequisites: JDK 8 and Maven 3

The master branch requires Java 8+ and the 1.0.x branch requires Java 7+.

Execute the maven command `mvn clean install` in the root directory of the project.

Note: the dependencies are not includes in the jar file, see in [pom.xml](pom.xml) which libraries
are required (at least dcm4che-core and dcm4che-net).
