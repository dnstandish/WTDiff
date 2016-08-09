#!/bin/sh

JAR_DIR=`dirname "$0"`

java -jar "${JAR_DIR}/WTDiff.jar" -gui "$@"

exit $?

