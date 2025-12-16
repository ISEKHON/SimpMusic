#!/bin/bash
# Quick start script for myjavaapp

echo "========================================"
echo "  My Java YouTube Music App - Launcher"
echo "========================================"
echo ""

cd ..

echo "Building and running the app..."
echo ""

./gradlew :myjavaapp:run

if [ $? -ne 0 ]; then
    echo ""
    echo "============================================"
    echo "  Error: Build or run failed!"
    echo "============================================"
    echo ""
    echo "Try:"
    echo "  1. Run './gradlew clean' first"
    echo "  2. Make sure you have Java 17+ installed"
    echo "  3. Check that Gradle sync completed"
    echo ""
    exit 1
fi

echo ""
echo "Done!"

