#!/usr/bin/env bash
# Компиляция и запуск Space Invaders
set -e
cd "$(dirname "$0")"
rm -rf out
javac -d out src/main/java/invaders/*.java
java -cp out invaders.Game
