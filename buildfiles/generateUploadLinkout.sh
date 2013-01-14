#!/bin/sh

set -e # If any command fails, fail the script
E_BADARGS=65
if [ $# -ne 1 ]
then
  echo "Usage: `basename $0` {cURL config specifying LinkOut FTP credentials}"
  exit $E_BADARGS
fi

JAVA_BIN=/usr/bin/java
CURL_BIN=/usr/bin/curl
XMLLINT_BIN=/usr/bin/xmllint

LINKOUT_FTP_PASSWORD_FILE=$1
LINKOUT_TOOL_DIR=`dirname $0`
OUTPUT_DIR="$LINKOUT_TOOL_DIR/output"
JAVA_OPTS=-Xmx1600M
CLASSPATH=".:../jars/commons-lang-2.2.jar:../jars/postgresql-9.0-802.jdbc4.jar:../jars/log4j-1.2.15.jar:../jars/xom-1.2.7.jar"
LINKOUT_DTD="http://www.ncbi.nlm.nih.gov/projects/linkout/doc/LinkOut.dtd"
LINKOUT_FTP="ftp://ftp-private.ncbi.nlm.nih.gov/holdings/"

# 0. Make the output dir if it does not yet exist
cd "$LINKOUT_TOOL_DIR"
if [ ! -d "$OUTPUT_DIR" ]; then
	mkdir "$OUTPUT_DIR"
fi

# 1. Run DryadLinkoutTool
# arguments are the paths to the pubmed link file name and the sequence link file name
$JAVA_BIN ${JAVA_OPTS} -cp ${CLASSPATH} org/datadryad/interop/NCBILinkoutBuilder output/pubmedlinkout output/sequencelinkout

# 2. Validate the linkout files
for LINKOUT_FILE in $OUTPUT_DIR/*
do
	# http://www.ncbi.nlm.nih.gov/projects/linkout/doc/LinkOut.dtd
	$XMLLINT_BIN -dtdvalid $LINKOUT_DTD --noout "$LINKOUT_FILE"
done

# 3. Upload the linkout files
for LINKOUT_FILE in $OUTPUT_DIR/*
do
	$CURL_BIN "$LINKOUT_FTP" -T "$LINKOUT_FILE" -K "$LINKOUT_FTP_PASSWORD_FILE"
done

# Cleanup, remove generated files

rm -rf "$OUTPUT_DIR"
