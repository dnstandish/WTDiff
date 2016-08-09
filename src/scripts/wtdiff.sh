#!/bin/sh

JAR_DIR=`dirname "$0"`

java -cp "${JAR_DIR}"/WTDiff.jar org.wtdiff.util.ui.DiffFrame "$@"

exit $?

