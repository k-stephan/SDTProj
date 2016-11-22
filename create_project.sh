#!/bin/bash

if [ "$#" <= "0" ] || [ "$#" > "2" ]; then
    echo "Error: no domain/project info"
    echo "Usage: create_project.bat <domain> <project>"
    echo "		e.g.: create_project SolutionDevelopment SampleProject"
    read -n1 -r -p "Press any key to continue..." key
    exit 1
fi

echo "creating domain..."
if [ ! -d "$1" ]; then
    mkdir $1
fi

echo "creating project..."
cd $1
if [ ! -d "$2" ]; then
    mkdir $2
fi

cd $2

echo "creating dirs..."
mkdir "features"
mkdir "src/main/java/com/macys/sdt/projects/$1/$2"
cd "src/main/java/com/macys/sdt/projects/$1/$2"
mkdir "actions/website/mcom/pages"
mkdir "actions/website/mcom/panels"
mkdir "actions/website/bcom/pages"
mkdir "actions/website/bcom/panels"
mkdir "actions/MEW/mcom/pages"
mkdir "actions/MEW/mcom/panels"
mkdir "actions/MEW/bcom/pages"
mkdir "actions/MEW/bcom/panels"
mkdir "actions/responsive/mcom/pages"
mkdir "actions/responsive/mcom/panels"
mkdir "actions/responsive/bcom/pages"
mkdir "actions/responsive/bcom/panels"
mkdir "resources/elements/website/mcom/pages"
mkdir "resources/elements/website/mcom/panels"
mkdir "resources/elements/website/bcom/pages"
mkdir "resources/elements/website/bcom/panels"
mkdir "resources/elements/MEW/mcom/pages"
mkdir "resources/elements/MEW/mcom/panels"
mkdir "resources/elements/MEW/bcom/pages"
mkdir "resources/elements/MEW/bcom/panels"
mkdir "resources/elements/responsive/mcom/pages"
mkdir "resources/elements/responsive/mcom/panels"
mkdir "resources/elements/responsive/bcom/pages"
mkdir "resources/elements/responsive/bcom/panels"
mkdir "steps/website/mcom"
mkdir "steps/website/bcom"
mkdir "steps/MEW/mcom"
mkdir "steps/MEW/bcom"
mkdir "steps/responsive/mcom"
mkdir "steps/responsive/bcom"
mkdir "utils"
cp ../../../../../../../../../../../pom_template.xml pom.xml
cd ../../../../../../../../../../..

echo "Folders created."
echo "Git will not recognize the folders until they have files in them. Please place files in any directories you want to keep in git."
read -n1 -r -p "Press any key to continue..." key