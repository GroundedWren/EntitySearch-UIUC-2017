#!/bin/bash
echo "Checking dependencies..."
pkg="graphviz"
if dpkg --get-selections | grep -q "^$pkg[[:space:]]*install$" >/dev/null; then
     echo "$pkg is already installed"
else
    echo "Please install: $pkg"
    echo "(sudo apt-get install $pkg)"
    echo
fi
#
# build fox
echo "Building FOX..."
if [ -f "$file" ] & dpkg --get-selections | grep -q "^$pkg[[:space:]]*install$" >/dev/null; then
    nohup mvn clean compile package -Dmaven.test.skip=false javadoc:javadoc > build.log &
else
    echo "Hold on to your butts"
    nohup mvn clean compile package -Dmaven.test.skip=false javadoc:javadoc > build.log &
    echo "Did it go?"
fi
#
