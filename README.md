# fornax
Fornax contains the zipping logic currently used by orion (data lake uploader).  It will be an executable jar that can zip the files provided.

The zip service is a cammand line application.  

Arguments:

zip.fileNames          : Contains the full path to a file that belongs in the zip file.  Specify once for each file to be added to the zip.
zip.zipFilePath        : Contains the full path and filename for the zip file you want creted.
zip.additionalFileData : Optional.  Specify when you need additional data added to the zip file that isn't already in a file.  Value should be specified as <filename>|<file data>

Example to run it:
java -jar fornax.jar --zip.fileNames=/Users/rlreamy/temp/barcodes.tsv --zip.zipFilePath=/Users/rlreamy/temp/test.zip --zip.additionalFileData="metadata.json|data to include in the additional file"

## Building the docker image
The gradle build file contains information on how to build a docker image.

./gradlew build docker
docker push kingstonduo/fornax

## Pulling down the fornax container
docker pull kingstonduo/fornax

## Using the container to zip files
docker run -d -v /data:/data kingstonduo/fornax --zip.fileNames="/data/dataLake/package_0d51faeb-b6bc-4203-91c7-32c28101aa91/Screen Shot 2019-07-10 at 10.35.45 AM.png" --zip.zipFilePath=/data/dataLake/package_0d51faeb-b6bc-4203-91c7-32c28101aa91/test2.zip --zip.additionalFileData="filename.txt|Data for file"

### Breaking the command down
'docker run' is used to start a container
'-d' will detach from the running container so you get your command line back
'-v /data:/data' mounts /data on your host machine to /data in the container (the first /data refers to the host machine, and the second one refers to the container)
'kingstonduo/fornax' is the name of the image
'--zip.fileNames', '--zip.zipFilePath', and '--zip.additionalFileData' are arguments passed to the ZipService (see above)
