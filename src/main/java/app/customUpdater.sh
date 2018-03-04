#!/bin/bash
# update_custom.sh
#
# Utility created to update custom directory based on new custom
# 2017-12-12 ecastellano@avaloninformatica.es. 
# Input: oldCustom newCustom 
# Output: oldCustomdir is overwritten with final custom

envvars() {
	DATE=$(date +%Y%m%d)
	MERGER="/opt/tools/merger.20171122.jar"
	# if set with precedence to this java location 
	JAVA_HOMEDIR="/opt/java7"

}

usage() {
  echo -e "----------------------------------------------------------------------------"
  echo -e "$0 utility to merge two custom properties directories"
  echo -e "Parameters: $0 actual(old)configDirectory newCustomDirectory"
  echo -e "JAVA_HOME or JAVA_HOMEDIR (this script) should be set"
  echo -e "----------------------------------------------------------------------------"
  exit 1
}

toUpper () {
	echo "$1" | tr '[:lower:]' '[:upper:]'
}

patch_custom() {
# Input: $oldCustomDir = current directory $2 = new repository directory
	newDir=$2
	currentDir=$1

# copy all new files
	echo -e "\nCOPYING ONLY NEW FILES $newDir --> $currentDir"
	echo n | cp -riv $newDir/* $currentDir/.
	echo -e "\nPATCHING CUSTOM\n"
	echo "========================================" 

# TODO CLEAN DEPRECATED NON .PROPERTIES FILES	
	
# copy all files but .properties .jar .svn
	echo "Copying non  .properties files ..."
	while read FILE; do
		echo "cp $newDir/$FILE $currentDir/$FILE"
		cp $newDir/$FILE $currentDir/$FILE
	done< <(find $currentDir/ -type f | sed "s@$currentDir/@@g" | grep -v .svn | grep -v ".jar" | grep -v ".properties" | grep -v "urlrewrite.xml")
	
# process properties files
	while read FILE; do
		if [ -f "$newDir/$FILE" ]; then
			/opt/java7/bin/java -jar /opt/tools/merger.20171122.jar $currentDir/$FILE $newDir/$FILE
		else 
			echo "$currentDir/$FILE deprecated. NOT Cleaning it..."
			#rm -f $currentDir/$FILE
		fi
	done< <(find $currentDir/ -type f -iname "*.properties" | sed "s@$currentDir/@@g" | grep -v "jobs.properties" | grep -v "Templates.properties")	
}


# --------------------------------------------
# --------------------------------------------
# ------------- START OF SCRIPT --------------
# --------------------------------------------
# --------------------------------------------


envvars

[ "$1" = "-?" ] || [ "$1" = "--help" ] || [ "$1" = "-h" ] && usage
[[ $# -ne 2 ]] && usage
oldCustomDir=${1%/}
newCustomDir=${2%/}

test ! -d $oldCustomDir && echo "directory $oldCustomDir not found" && exit 1
test ! -d $newCustomDir && echo "directory $newCustomDir not found" && exit 1
##if [ -z ${var+x} ]; then echo "var is unset"; else echo "var is set to '$var'"; fi
if [ -z ${JAVA_HOMEDIR+x} ]; then 
	echo -e "JAVA_HOMEDIR unset checking JAVA_HOME\n"
	test ! -f "$JAVA_HOME/bin/java" && echo "JAVA_HOMEDIR not set and java not found in $JAVA_HOME" && exit 1;
	echo -e "Will use JAVA located in $JAVA_HOME/bin/java \n"
else 
	test ! -f "$JAVA_HOMEDIR/bin/java" && echo "java not found in $JAVA_HOME" && exit 1;
	echo "Will use JAVA located in $JAVA_HOMEDIR/bin/java"
fi
test ! -f $MERGER  && echo "Merger utility not found in $MERGER" && exit 1

echo -e "!!!!!!!!!!!!!!!Attention!!!!!!!!!!!!!!!!"
echo -e "Directory $oldCustomDir will be patched (overwritten) with new directory $newCustomDir. ARE YOU SURE???? (Y/N)"
echo -e "!!!!!!!!!!!!!!!Attention!!!!!!!!!!!!!!!!"
read ok
if [ $(toUpper $ok) != "Y" ]; then exit 0; fi
patch_custom $oldCustomDir $newCustomDir
mv $oldCustomDir $oldCustomDir.patched
echo "Well done!!"
