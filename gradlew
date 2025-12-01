#!/usr/bin/env sh
# Gradle wrapper script for Unix

# Set environment variables
DIRNAME=$(dirname "$0")
APP_HOME=$(cd "$DIRNAME" && pwd -P)

# Use system Java or define JAVA_HOME
if [ -n "$JAVA_HOME" ]; then
    JAVACMD="$JAVA_HOME/bin/java"
else
    JAVACMD="java"
fi

# Run gradle wrapper
exec "$JAVACMD" -jar "$APP_HOME/gradle/wrapper/gradle-wrapper.jar" "$@"
