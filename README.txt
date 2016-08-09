
WTDiff if a tool for comparing directories and files.  Directories can be
either file system directories, ZIP files, or XML snapshots of directories.

To compare two files

    java -jar WTDiff.jar org.wtdiff.util.ui.DiffFrame file1 file2

Or use one of the bundled scripts

  Windows: 
    wtdiff.bat file1 file2
  Linux:
    wtdiff.sh file1 file2
    
To compare two directories

    java -jar WTDiff.jar -gui dir1 dir2
    
or

    java -jar WTDiff.jar -gui

then select and load the two directories of interest

The -gui option overrides the default text mode of oepration.  Text mode is not
well developed.

Alternatively use one of the bundled scripts

  Windows: 
    wtdircmp.bat file1 file2
  Linux:
    wtdircmp.sh file1 file2
    

WTDiff also includes a tool for creating snapshot of a directory structure and file checksums.

    java -cp "${JAR_DIR}"/WTDiff.jar org.wtdiff.util.xml.Snapshotter output_file directory 

The resulting snapshot can be used by WTDirCmp like a directory with the exception that
text file diffs are not possible.

 
