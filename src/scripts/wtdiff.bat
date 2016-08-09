@echo off

set JAR_DIR=%~dp0

java -cp "%JAR_DIR%\WTDiff.jar" org.wtdiff.util.ui.DiffFrame %*

