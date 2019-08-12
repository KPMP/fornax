# fornax
Fornax contains the zipping logic currently used by orion (data lake uploader).  It will be an executable jar that can zip the files provided.

The zip service is a cammand line application.  

Arguments:

zip.fileNames          : Contains the full path to a file that belongs in the zip file.  Specify once for each file to be added to the zip.
zip.zipFilePath        : Contains the full path and filename for the zip file you want creted.
zip.additionalFileData : Optional.  Specify when you need additional data added to the zip file that isn't already in a file.  Value should be specified as <filename>|<file data>
