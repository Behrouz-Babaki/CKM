#!/bin/bash

# Example:

# Run constrained k-means on the data stored in the file ../data/players.data
# The weights of data points are stored in the file ../data/players.weights
# number of clusters: 4
# The total weight of points in each cluster should be at least -4
# The total weight of points in each cluster should be at most 4
# Run the algorithm 2 times and ouput the best clustering
# Store the results in the file players1.out
./constrained_kmeans.py ../data/players.data 4 ../data/players.weights -4 4 -n 2 -o players1.out


# Add the constraint that the clusters should be balanced
./constrained_kmeans.py ../data/players.data 4 ../data/players.weights -4 4 -n 2 --balanced -o players2.out


# Relax the balance constraint by an alpha threshold
./constrained_kmeans.py ../data/players.data 4 ../data/players.weights -4 4 -n 2 --balanced --alpha 0.1 -o players3.out

# Instead of using the balance constraint, require the clusters to have a minimum size
./constrained_kmeans.py ../data/players.data 4 ../data/players.weights -4 4 -n 2 --min_size 3 -o players4.out


