#!/bin/sh
# Maven Wrapper - downloads maven if not present
MAVEN_VERSION=3.9.6
MAVEN_HOME="$HOME/.m2/wrapper/dists/maven-${MAVEN_VERSION}"
MAVEN_CMD="${MAVEN_HOME}/bin/mvn"

if [ ! -f "$MAVEN_CMD" ]; then
    echo "Downloading Maven ${MAVEN_VERSION}..."
    mkdir -p "$MAVEN_HOME"
    # Use multiple mirrors
    for URL in \
        "https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/${MAVEN_VERSION}/apache-maven-${MAVEN_VERSION}-bin.tar.gz" \
        "https://downloads.apache.org/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz"; do
        curl -fsSL "$URL" | tar xz -C "$MAVEN_HOME" --strip-components=1 2>/dev/null && break
    done
fi

if [ ! -f "$MAVEN_CMD" ]; then
    echo "ERROR: Could not download Maven"
    exit 1
fi

exec "$MAVEN_CMD" "$@"
