#!/bin/sh

print_usage () {
  echo -e "\nUsage:\t\t$0 [options] <output-folder> <input-file>\n\n"
  echo -e "\t\toutput-folder:\t\tJava output folder"
  echo -e "\t\tinput-file:\t\tBPMN input file"
  echo -e "\n\t\toptions:"
  echo -e "\t\t-s (--stubs)\t\tGenerate implementation stubs [false]"
  echo -e "\t\t-f (--force)\t\tReplace existing files [false]"
  echo -e "\t\t-c (--create)\t\tCreate missing output folder [false]"
  echo -e "\t\t-v (--verbose)\t\tVerbose [false]"
}

if [ "$#" -lt 2 ]
then
  print_usage
  exit 1
fi

STUBS=false
FORCE=false
CREATE=false
DEBUG=false

let NR_OF_OPTS=$#-2

for (( i=1; i <= NR_OF_OPTS; i++ )); do
    #echo "arg position: ${i}"
    #echo "arg value: ${!i}"
    
    if [ "${!i}" = "-s" -o "${!i}" = "--stubs" ]; then
      STUBS=true
    elif [ "${!i}" = "-f" -o "${!i}" = "--force" ]; then
      FORCE=true
    elif [ "${!i}" = "-c" -o "${!i}" = "--create" ]; then
      CREATE=true
    elif [ "${!i}" = "-v" -o "${!i}" = "--verbose" ]; then
      DEBUG=true
    fi
   
done

OUTPUT_FOLDER="${@:(-2):1}"
INPUT_FILE="${@:(-1):1}"
CLASSPATH_WIN="${project.artifactId}-${project.version}-fatjar.jar;"

echo -e "\nBPMN input file:\t\t$INPUT_FILE"
echo -e "Java output folder:\t\t$OUTPUT_FOLDER"
echo -e "Implementation stubs:\t\t$STUBS"
echo -e "Replace files:\t\t\t$FORCE"
echo -e "Create output folder:\t\t$CREATE"
echo -e "Verbose:\t\t\t$DEBUG"
echo -e "Classpath:\t\t\t$CLASSPATH_WIN\n"

"$JAVA_HOME\bin\java.exe" -cp $CLASSPATH_WIN io.process4j.core.bpmn.Runner bpmn2java $INPUT_FILE $OUTPUT_FOLDER $STUBS $FORCE $CREATE $DEBUG