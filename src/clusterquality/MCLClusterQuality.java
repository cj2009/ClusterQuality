package clusterquality;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

/**
 * This class provides some useful statistics about the clusters produced by
 * the Hadoop MCL program. It requires the outputs of both MR5 and MR6.
 * @author chris_joseph
 */
public class MCLClusterQuality {

    public static void main(String args[]) throws Exception {
        main2(args);
    }

    public static void main2(String args[]) throws Exception {
        String MR5_OUTPUT = "mr5.txt";
        String MR6_OUTPUT = "mr6.txt";
        String outputName = "MCLstats.txt";
        if (args.length > 0 && (args[0].contains("BC") || args[1].contains("BC")))
            outputName = "BCstats.txt";
        
        HashMap<Integer, Integer> edgeCounts = readClusterEdges(MR6_OUTPUT);
        HashMap<Integer, Integer> clusterSizes = readClusterSizes(MR5_OUTPUT);
        
        ArrayList<Integer> clusters = sortKeysByValue(clusterSizes);
        
        int clustersSizeGreaterThan1 = 0;
        int clustersSizeGreaterThan2 = 0;
        int clustersSizeGreaterThan3 = 0;
        double avgTotalsSizeGreaterThan1 = 0.0;
        double avgTotalsSizeGreaterThan2 = 0.0;
        double avgTotalsSizeGreaterThan3 = 0.0;
        
        
        // Use a BufferedWriter to write the output to a file:
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputName), "utf-8"))) {
            String header = String.format("%-20s%-10s%-20s%-15s\n", "Cluster ID", "Size", "Number of Edges", "Completion %");
            writer.write(header);
            writer.write("--------------------------------------------------------------\n");
            
            for (Integer cluster : clusters) {
                
                int edges = 0;
                try {
                    edges = edgeCounts.get(cluster);
                } catch(Exception e) {
                    ;
                    // There are no edges in this cluster
                }
                
                int size = clusterSizes.get(cluster);
                int total = size*(size-1) / 2; // total number of possible edges in this graph
                
                String s;
                if (size == 1) {
                    // this is a trivial cluster
                    s = String.format("%-20d%-10d%-20d100.00%%\n", cluster, size, edges);
                }
                    
                else {
                    // this is a non-trivial cluster, because it has 2 or more vertices
                    double avg = (edges * 100.0 / total);
                    s = String.format("%-20d%-10d%-20d%-1.2f%%\n", cluster, size, edges, avg);
                    
                    // Increment the counters for cluster of size > 1:
                    clustersSizeGreaterThan1++;
                    avgTotalsSizeGreaterThan1 += avg;
                    
                    // Increment the counters for cluster of size > 2:
                    if (size > 2) {
                        clustersSizeGreaterThan2++;
                        avgTotalsSizeGreaterThan2 += avg;
                    }
                    // Increment the counters for cluster of size > 3:
                    if (size > 3) {
                        clustersSizeGreaterThan3++;
                        avgTotalsSizeGreaterThan3 += avg;
                    }
                    
                }
                    
                writer.write(s);
                
            }
            
            // We can now compute the average cluster density for this entire graph:
            double sizeGreaterThan1Avg = avgTotalsSizeGreaterThan1 / clustersSizeGreaterThan1;
            double sizeGreaterThan2Avg = avgTotalsSizeGreaterThan2 / clustersSizeGreaterThan2;
            double sizeGreaterThan3Avg = avgTotalsSizeGreaterThan3 / clustersSizeGreaterThan3;
            System.out.printf("Average cluster density for clusters of size greater than 1: %.2f%%\n", sizeGreaterThan1Avg);
            System.out.printf("Average cluster density for clusters of size greater than 2: %.2f%%\n", sizeGreaterThan2Avg);
            System.out.printf("Average cluster density for clusters of size greater than 3: %.2f%%\n", sizeGreaterThan3Avg);
        }
    }


    /**
    * This function reads the output file from MR6 of the Hadoop MCL Program
    * and counts the number of edges that are part of each cluster. 
    * Each cluster is keyed by its ID in the hashtable.
    */
    public static HashMap<Integer, Integer> readClusterEdges(String fileName) throws Exception {

        HashMap<Integer, Integer> hashtable = new HashMap<Integer, Integer>();
        try(BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line = br.readLine();

            while (line != null) {

                String str[] = line.split("\t");
                int id = Integer.parseInt(str[2]); // the cluster's ID
                
                if (hashtable.containsKey(id)) {
                    int i = hashtable.get(id);
                    hashtable.put(id, ++i);
                }
                else
                    hashtable.put(id, 1);


                // continue reading the line
                line = br.readLine();
            }
        }


        return hashtable;

    }


    /**
    * This function reads the output file from MR5 of the Hadoop MCL program 
    * and counts the number of vertices in each cluster. The output is a hashtable 
    * in which the clusters are keyed by their IDs.
    */
    public static HashMap<Integer, Integer> readClusterSizes(String fileName) throws Exception {

        HashMap<Integer, Integer> hashtable = new HashMap<Integer, Integer>();
        try(BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line = br.readLine();

            while (line != null) {

                String str[] = line.split("\t");
                String id = (str[0].split(":"))[1]; // the cluster's ID
                
                String nodes[];
                if (str[1].indexOf(", ") == -1) {
                    nodes = new String[1];
                    nodes[0] = str[1];
                }
                else
                    nodes = str[1].split(", "); 
                // split the list of nodes, to get the size of this cluster

                if (hashtable.containsKey(Integer.parseInt(id))) {
                    System.out.println("Multiple clusters found with the same ID! (" + id + ")");
                    // This is just a warning; fixing this issue requires changing
                    // the underlying code behind the MCL program.
                }
                hashtable.put(Integer.parseInt(id), nodes.length);

                // continue reading the line
                line = br.readLine();
            }
        }

        return hashtable;

    }
    
    /**
     * Sorts the keys in the given HashMap based on their values. Returns an 
     * ArrayList containing the sorted keys.
     * @param hashtable
     * @return
     */
    public static ArrayList<Integer> sortKeysByValue(HashMap<Integer, Integer> hashtable) {
        Set<Integer> keySet = hashtable.keySet();
        ArrayList<Integer> arr = new ArrayList<>(keySet.size());
        for (Integer i : keySet)
            arr.add(i);
        
        Collections.sort(arr, (Integer i1, Integer i2) -> {
            int v1 = hashtable.get(i1);
            int v2 = hashtable.get(i2);
            
            if (v1 < v2)
                return -1;
            if (v2 > v1)
                return 1;
            return 0;
        });
        return arr;
        
    }



}