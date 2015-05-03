# contactsc
contacts just a simple application for importing contacts from xml files and retrieving them.

## Features
Two basic features, 
* Search existing contacts: search for given name, case sensitive.
* Import from XML: parse and import contacts from given files.

## Requirements
*   JDK7+
*   SBT 
*   MongoDB 2.6+

## Dependencies
SBT will handle dependencies

## Retrieve & Build
```
git clone https://github.com/hzengin/contacts
```
will clone codebase to `contacts` directory go inside it with `cd contacts`

Run
```
./build.sh
```
this will compile the code, deploy a single jar and create a executable file named `contacts` in current directory.

## Configuration
Configuration parameters for MongoDB connection are in `src/main/scala/Config.scala`

## Usage
* to import contacts: 
```./contacts --import a.xml b.xml```

* to find a contact: 
```./contacts --find-by-name Name```

## Testing
Project using ScalaTest for unit tests, run them by
`sbt test`

## Notes
* I used "reading whole file into memory" method instead of using streams to read XML files; using streams would give a big advantage against importing huge files and would be more suitable for a log management application but for a contacts management application I thought that it would be a huge overkill.
* I didn't implemented any validation on files to be imported because challege document didn't mentioned of it.
