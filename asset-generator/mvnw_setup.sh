#!/bin/bash
MAVEN_VERSION=3.9.6
MAVEN_URL="https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/${MAVEN_VERSION}/apache-maven-${MAVEN_VERSION}-bin.tar.gz"
mkdir -p /tmp/mvn_install
cd /tmp/mvn_install
curl -sf "$MAVEN_URL" -o maven.tar.gz
if [ $? -eq 0 ]; then
    tar xzf maven.tar.gz
    export PATH="/tmp/mvn_install/apache-maven-${MAVEN_VERSION}/bin:$PATH"
    mvn -version
fi
