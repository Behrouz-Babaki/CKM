#!/bin/bash

# Example:

# Run constrained k-means on the data stored in the file ../data/players.data
# The weights of data points are stored in the file ../data/players.weights
# number of clusters: 4
# The total weight of points in each cluster should be at least -4
# The total weight of points in each cluster should be at most 4
# Run the algorithm 2 times and ouput the best clustering
# Store the results in the file players1.out

# Relax the balance constraint by an alpha threshold
# The inner loop is not executed more than 100 times
# The algorithm converges if the sum of distances between previous and current centers is less than 1e-3 (scaled to the dimensions of the dataset)
./constrained_kmeans.py ../data/players.data 4 ../data/players.weights -4 4 -n 2 --balanced --alpha 0.1 --max_iters 100 --tolerance 1e-3 -o players3.out




