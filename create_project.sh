#!/bin/bash

if [ $# -ne 2 ]; then
    echo "Error: no domain/project info"
    echo "Usage: create_project.bat <domain> <project>"
    echo "		e.g.: create_project SolutionDevelopment SampleProject"
    read -n1 -r -p "Press any key to continue..." key
    exit 1
fi

echo "creating domain..."
if [ ! -d $1 ]; then
    mkdir $1
fi

echo "creating project..."
cd $1
if [ ! -d $2 ]; then
    mkdir $2
fi

cd $2

echo "creating dirs..."
mkdir "features"
mkdir "src/main/java/com/macys/sdt/projects/$1/$2" -p
cd "src/main/java/com/macys/sdt/projects/$1/$2" -p
mkdir "actions/website/mcom/pages" -p
mkdir "actions/website/mcom/panels" -p
mkdir "actions/website/bcom/pages" -p
mkdir "actions/website/bcom/panels" -p
mkdir "actions/MEW/mcom/pages" -p
mkdir "actions/MEW/mcom/panels" -p
mkdir "actions/MEW/bcom/pages" -p
mkdir "actions/MEW/bcom/panels" -p
mkdir "actions/responsive/mcom/pages" -p
mkdir "actions/responsive/mcom/panels" -p
mkdir "actions/responsive/bcom/pages" -p
mkdir "actions/responsive/bcom/panels" -p
mkdir "resources/elements/website/mcom/pages" -p
mkdir "resources/elements/website/mcom/panels" -p
mkdir "resources/elements/website/bcom/pages" -p
mkdir "resources/elements/website/bcom/panels" -p
mkdir "resources/elements/MEW/mcom/pages" -p
mkdir "resources/elements/MEW/mcom/panels" -p
mkdir "resources/elements/MEW/bcom/pages" -p
mkdir "resources/elements/MEW/bcom/panels" -p
mkdir "resources/elements/responsive/mcom/pages" -p
mkdir "resources/elements/responsive/mcom/panels" -p
mkdir "resources/elements/responsive/bcom/pages" -p
mkdir "resources/elements/responsive/bcom/panels" -p
mkdir "steps/website/mcom" -p
mkdir "steps/website/bcom" -p
mkdir "steps/MEW/mcom" -p
mkdir "steps/MEW/bcom" -p
mkdir "steps/responsive/mcom" -p
mkdir "steps/responsive/bcom" -p
mkdir "utils"
cp ../../../../../../../../../../../pom_template.xml pom.xml
cd ../../../../../../../../../../..

echo "Folders created."
echo "Git will not recognize the folders until they have files in them. Please place files in any directories you want to keep in git."
read -n1 -r -p "Press any key to continue..." key