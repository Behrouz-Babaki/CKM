#!/bin/bash

sbt "run CL --method simple --data-file ../../data/players.data --k 3 --weight-file ../../data/players.weights --min-weight -10 --max-weight 1000"

#sbt "run KM ../../data/players.data 3"
