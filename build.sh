#!/bin/sh
sbt clean assembly
echo '
#!bin/sh
java -jar target/scala-2.11/contacts-assembly-1.0.jar "$@"
' > contacts
chmod +x contacts
