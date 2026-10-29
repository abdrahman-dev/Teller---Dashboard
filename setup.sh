#!/usr/bin/env bash
#
# setup.sh — Build script for Account Management System (Mac/Linux/WSL/Git Bash)
#
# This script:
#   1. Ensures lib/ exists
#   2. Downloads missing jar dependencies from Maven Central
#   3. Compiles the Java sources into out/
#   4. Prints the command to run the app
#
# Run with: ./setup.sh   or   bash setup.sh

set -e  # exit on first error

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
LIB_DIR="$PROJECT_DIR/lib"
SRC_DIR="$PROJECT_DIR/src"
OUT_DIR="$PROJECT_DIR/out"

SQLITE_JAR="$LIB_DIR/sqlite-jdbc-3.53.4.0.jar"
FLATLAF_JAR="$LIB_DIR/flatlaf-3.7.2.jar"

SQLITE_URL="https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.53.4.0/sqlite-jdbc-3.53.4.0.jar"
FLATLAF_URL="https://repo1.maven.org/maven2/com/formdev/flatlaf/3.7.2/flatlaf-3.7.2.jar"

echo "================================================"
echo "  Account Management System — Setup Script"
echo "================================================"
echo ""

# --- Step 1: Ensure lib/ directory exists ---
if [ ! -d "$LIB_DIR" ]; then
    echo "[1/3] Creating lib/ directory..."
    mkdir -p "$LIB_DIR"
else
    echo "[1/3] lib/ directory already exists."
fi

# --- Step 2: Download missing jars from Maven Central ---
download_if_missing() {
    local jar_path="$1"
    local jar_url="$2"
    local jar_name
    jar_name="$(basename "$jar_path")"

    if [ -f "$jar_path" ]; then
        echo "   $jar_name already present — skipping download."
    else
        echo "   Downloading $jar_name from Maven Central..."
        curl -fsSL -o "$jar_path" "$jar_url"
        echo "   Saved $jar_name to lib/"
    fi
}

echo ""
echo "[2/3] Checking jar dependencies..."
download_if_missing "$SQLITE_JAR" "$SQLITE_URL"
download_if_missing "$FLATLAF_JAR" "$FLATLAF_URL"

# --- Step 3: Compile Java sources ---
echo ""
echo "[3/3] Compiling Java sources..."
if [ ! -d "$OUT_DIR" ]; then
    mkdir -p "$OUT_DIR"
fi

javac -cp "$SQLITE_JAR:$FLATLAF_JAR" -d "$OUT_DIR" "$SRC_DIR"/*.java

echo ""
echo "================================================"
echo "  Setup complete!"
echo "================================================"
echo ""
echo "To run the application, execute:"
echo ""
echo "    java -cp \"$OUT_DIR:$SQLITE_JAR:$FLATLAF_JAR\" Main"
echo ""
echo "Default login:  username = admin   password = admin123"
echo ""
