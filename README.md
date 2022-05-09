# range-analysis

## Requirements

JDK version 8-14. This project will not work Java versions greater than 16.

## Running the Analysis

`./gradlew build` will run the entire test suite. This will likely fail if one does not have enough memory allocated as heap space. This can be modified usinng the `-xmx` and `-xms` flags in `gradlew`. Tests can be run individually by running their respective class in `/src/test/java/range_analysis`. 
