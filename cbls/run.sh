#!/bin/bash

java -jar target/scala-2.12/CKmeans-CLBS-assembly-0.1.jar --data-file ../data/medium.data --k 15 --weight-file ../data/medium.weights --min-weight -5 --max-weight 5 --min-size 2 --output-file ../data/medium.out --verbosity 1

