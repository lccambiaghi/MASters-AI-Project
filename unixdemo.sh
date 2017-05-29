#!/bin/bash
# compile
rm -rf build/
mkdir build
javac -d ./build ./communicationclient/*.java
javac -d ./build ./communication/*.java
javac -d ./build ./goal/*.java
javac -d ./build ./graph/*.java
javac -d ./build ./heuristic/*.java
javac -d ./build ./level/*.java
javac -d ./build ./plan/*.java
# MA logs
java -jar server.jar -o logs/MABeliebers.log -g -p 150
java -jar server.jar -o logs/MAHiveMind.log -g -p 150
java -jar server.jar -o logs/MAKalle.log -g -p 300
java -jar server.jar -o logs/MALiquorice.log -g -p 150
java -jar server.jar -o logs/MANeverMind.log -g -p 150
java -jar server.jar -o logs/MAEvilCorp.log -g -p 150
java -jar server.jar -o logs/SAHiveMind.log -g -p 150