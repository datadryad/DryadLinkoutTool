DryadLinkoutTool
================

A standalone tool for generating NCBI linkout files from a Dryad database.  See http://wiki.datadryad.org/NCBI_Linkout_Technology for more information.

Installation and usage
-----------------------

Build with ant in the buildfiles directory.
In the config directory, copy the template.properties and fill in your credentials.

Run with a command like 
```
build/generateUploadLinkout.sh config/ncbi-ftp-credentials.conf
```
