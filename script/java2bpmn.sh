#!/bin/sh

print_usage () {
  echo -e "\nUsage:\n\t\t$0 <process-fqcn> <output-folder> <classpath>\n\n"
  echo -e "\t\tprocess-fqcn:\t\tFully qualified class name of the process implementation"
  echo -e "\t\toutput-folder:\t\tBPMN file output folder"
  echo -e "\t\tclasspath:\t\tWhere to find compiled class files"
}

if [ "$#" -lt 3 ]
then
  print_usage
  exit 1
fi

#!/bin/bash

CLASSPATH_WIN="${project.artifactId}-${project.version}-fatjar.jar;$3"

echo -e "\nprocess-fqcn:\t\t$1"
echo -e "output-folder:\t\t$2"
echo -e "classpath (Win):\t$CLASSPATH_WIN\n"

"$JAVA_HOME\bin\java.exe" -cp $CLASSPATH_WIN io.process4j.core.bpmn.Runner java2bpmn $1 $2