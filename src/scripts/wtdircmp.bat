@echo off

set JAR_DIR=%~dp0

java -jar "%JAR_DIR%\WTDiff.jar" -gui %*


