package clusterquality;

import graph.Graph;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * This class provides some useful statistics about the clusters produced by
 * the BC program. It requires the outputs from NodeXL, which is a list of edges 
 * in which each line contains an ID of a cluster and the 2 vertices of that edge.
 */
public class BCClusterQuality {
    
    public static void main (String args[]) throws Exception {
        int N = 134; // number of vertices in the graph.
        String BC_OUTPUT = "bc.txt"; // the input for this program
        
        HashMap<Integer, ArrayList<Integer>> clusters = readFile(BC_OUTPUT);
        outputClusters(clusters, N);
        
        // Now we can finally run the MCLClusterQuality program using this program's
        // output:
        String args2[] = {"BC", "BC"};
        MCLClusterQuality.main2(args2);
        
    }
    
    /**
     * Reads the file from disk, processes it and creates a list of clusters.
     * Also creates a file called "mr6.txt" with the list of edges and an ID for 
     * each edge.
     * @param fileName 
     */
    public static HashMap<Integer, ArrayList<Integer>> readFile(String fileName) throws Exception {
        HashMap<Integer, ArrayList<Integer>> hashtable = new HashMap<Integer, ArrayList<Integer>>();
        ArrayList<String> edges = new ArrayList<>();
        try(BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line = br.readLine();

            while (line != null) {

                String str[];
                if (line.contains(" "))
                    str = line.split(" ");
                else
                    str = line.split("\t");
                
                int id = extractIntFromString(str[0]); // the cluster's ID
                
                int v1 = Integer.parseInt(str[1]);
                int v2 = Integer.parseInt(str[2]);
                
                ArrayList<Integer> thisCluster;
                if (hashtable.containsKey(id)) {
                    thisCluster = hashtable.get(id);
                }
                else {
                    thisCluster = new ArrayList<Integer>();
                    hashtable.put(id, thisCluster);
                }
                
                // 'thisCluster' is the list of vertices for this particular cluster;
                // If it doesn't already contain v1 and v2, add them:
                if (!thisCluster.contains(v1))
                    thisCluster.add(v1);
                if (!thisCluster.contains(v2))
                    thisCluster.add(v2);
                
                
                // Keep a copy of this edge in the arraylist:
                edges.add(v1 + "\t" + v2 + "\t" + id + "\n");

                // continue reading the line
                line = br.readLine();
            }
        }

        // Create the "mr6.txt" file:
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("mr6.txt"), "utf-8"))) {
            for (String edge : edges)
                writer.write(edge);
        }

        
        // print some diagnostic info:
        System.out.println(edges.size() + " edges were processed.");
        System.out.println(hashtable.keySet().size() + " nontrivial clusters were identified.");
        
        return hashtable;
    }
    
    public static int extractIntFromString(String s) {
        String result = "";
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c >= '0' && c <= '9')
                result += c;
        }
        
        return Integer.parseInt(result);
    }
    
    /**
     * Writes the list of clusters to a file called "mr5.txt". Produces output 
     * identical to MCL's MR5.
     * @param n Number of nodes in the graph.
     */
    public static void outputClusters(HashMap<Integer, ArrayList<Integer>> clusters, int n) throws Exception {
        
        boolean used[] = new boolean[n];
        
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("mr5.txt"), "utf-8"))) {
            
            Set<Integer> keys = clusters.keySet();
            for (int key : keys) {
                ArrayList<Integer> cluster = clusters.get(key);
                
                // Create a string out of this cluster:
                String clusterString = "Cluster ID:" + key + "\t";
                for (int i = 0; i < cluster.size() - 1; i++) {
                    int vertex = cluster.get(i);
                    used[vertex] = true;
                    clusterString += vertex + ", ";
                }
                // And add the very last vertex in this cluster also:
                int vertex = cluster.get(cluster.size() - 1);
                used[vertex] = true;
                clusterString += vertex;
                
                writer.write(clusterString + "\n");
            }
            
            // Now we have to go thru the list of vertices and pick up any that 
            // are disconnected components (i.e. trivial clusters.)
            int lonelyNodes = 0;
            for (int i = 0; i < used.length; i++) {
                if (!used[i]) {
                    writer.write("Cluster ID:" + (-i) + "\t" + i + "\n");
                    lonelyNodes++;
                }
            }
            System.out.println("There are " + lonelyNodes + " trivial clusters.");
        }
    }
}
