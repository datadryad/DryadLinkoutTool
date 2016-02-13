#!/bin/sh

set -e # If any command fails, fail the script
E_BADARGS=65
if [ $# -ne 1 ]
then
  echo "Usage: `basename $0` {cURL config specifying LabsLink FTP credentials}" 1>&2
  exit $E_BADARGS
fi

JAVA_BIN=/usr/bin/java
CURL_BIN=/usr/bin/curl
XMLLINT_BIN=/usr/bin/xmllint
GZIP_BIN=/bin/gzip

LABSLINK_FTP_PASSWORD_FILE=$1
LABSLINK_TOOL_DIR=`dirname $0`
OUTPUT_DIR="$LABSLINK_TOOL_DIR/output-labslink"
JAVA_OPTS=-Xmx1600M
CLASSPATH=".:../jars/commons-lang-2.2.jar:../jars/postgresql-9.0-802.jdbc4.jar:../jars/log4j-1.2.15.jar:../jars/xom-1.2.7.jar"
LABSLINK_XSD="http://europepmc.org/docs/labslink.xsd"
LABSLINK_FTP="ftp://labslink.ebi.ac.uk/f24ml3c8/"
LABSLINK_ERROR_EMAIL="linkout@datadryad.org"
LABSLINK_ERROR_FILE="/tmp/labslink.error"

# Trap any error output and mail it
trap 'mail -s "Error running `basename $0` on line $LINENO; rc=$?" "$LABSLINK_ERROR_EMAIL" < "$LABSLINK_ERROR_FILE" >/dev/null' ERR

# Cleanup, remove any old generated files

rm -rf "$OUTPUT_DIR" 2> "$LABSLINK_ERROR_FILE"

# 0. Make the output dir if it does not yet exist
cd "$LABSLINK_TOOL_DIR"
if [ ! -d "$OUTPUT_DIR" ]; then
	mkdir "$OUTPUT_DIR" 2> "$LABSLINK_ERROR_FILE"
fi

# 1. Run DryadLinkoutTool
# arguments are the paths to the link file and the profile file 
$JAVA_BIN ${JAVA_OPTS} -cp ${CLASSPATH} org/datadryad/interop/LabsLinkLinksBuilder "${OUTPUT_DIR}/labslink-links" "${OUTPUT_DIR}/labslink-profile" 2> "$LABSLINK_ERROR_FILE"

# 2. Validate the labslink files
for LABSLINK_FILE in $OUTPUT_DIR/*
do
	# http://europepmc.org/docs/labslink.xsd
	$XMLLINT_BIN --schema $LABSLINK_XSD --noout "$LABSLINK_FILE" 2> "$LABSLINK_ERROR_FILE"
done

# 3. Upload the labslink files
for LABSLINK_FILE in $OUTPUT_DIR/*
do
	$GZIP_BIN "$LABSLINK_FILE"
	$CURL_BIN "$LABSLINK_FTP" -T "${LABSLINK_FILE}.gz" -K "$LABSLINK_FTP_PASSWORD_FILE" 2> "$LABSLINK_ERROR_FILE"
done

