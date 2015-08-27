package graph;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Queue;

/*
 * This class represents an entire graph. It must be initialized with an edge 
 * file.

 * Converted from the C code provided by Skiena, from the textbook 'The Algorithm 
 * Design Manual' 2nd edition (pg. 153).
 *
 * @author chris_joseph
 */
public class Graph {
    private HashMap<Integer, Node> edges; // the linked-list of edges in this graph
    private HashMap<Integer, Integer> degrees; // the degree of each vertex
    private int numEdges = 0;
    private int numVertices = 0;
    private boolean directed;
    
    // These are needed for the BFS:
    private boolean processed[];
    private boolean discovered[];
    
    public Graph(String fileName, boolean directed) throws IOException {
        edges = new HashMap<>();
        degrees = new HashMap<>();
        this.directed = directed;
        
        readGraph(fileName, directed);
        numVertices++;
        
        processed = new boolean[numVertices];
        discovered = new boolean[numVertices];
    }
    
    /**
     * This method opens the edge file and reads in line by line, adding each 
     * edge into the graph data structure.
     * @param fileName
     * @throws IOException
     */
    private void readGraph(String fileName, boolean directed) throws IOException {

        try(BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line = br.readLine();

            while (line != null) {
                
                String v[] = line.split("\t");
                int v1 = Integer.parseInt(v[0]);
                int v2 = Integer.parseInt(v[1]);
                int temp = Math.max(v1, v2);
                numVertices = Math.max(numVertices, temp);
                
                insertEdge(Integer.parseInt(v[0]), Integer.parseInt(v[1]), directed);
                
                // continue reading the line
                line = br.readLine();
            }
        }
        
    }
    
    private void insertEdge(int x, int y, boolean directed) {
        Node p = new Node(y);
        
        // Insert the new edge into the linked list structure:
        if (edges.containsKey(x)) {
            edges.get(x).setNext(p);
            int deg = degrees.get(x);
            degrees.put(x, ++deg);
        }
        else {
            edges.put(x, p);
            degrees.put(x, 1);
        }
        
        
        // Update some global variables:
        if (!directed)
            insertEdge(y, x, true);
        else
            numEdges++;
    }
    
    public int getNumEdges() {
        return numEdges;
    }
    
    public int getNumVertices() {
        return numVertices;
    }
    
    /**
     * Performs BFS on this graph. The result of this traversal will depend on 
     * the state of the boolean variables in the arrays 'discovered' and 
     * 'processed.'
     * @return 
     */
    public ArrayList<Integer> BFS(int start) {
        // This will be a single connected component of the graph, which is 
        // essentially all the vertices that will be discovered in a single 
        // call of this BFS method:
        ArrayList<Integer> component = new ArrayList<>();
        
        Queue<Integer> q = new PriorityQueue<>();
        int v, y;
        Node p;
        
        q.add(start);
        discovered[start] = true;
        
        while (!q.isEmpty()) {
            v = q.poll();
            processed[v] = true;
            if (edges.containsKey(v)) {
                p = edges.get(v);
                while (p != null) {
                    y = p.getValue();
                    if (!discovered[y]) {
                        q.add(y);
                        component.add(y);
                        discovered[y] = true;
                    }
                    p = p.getNext();
                }
            }
        }
        
        return component;
    }
    
    /**
     * This function finds all the components of this graph by repeatedly 
     * calling the BFS function.
     * If the nontrivial == true, then components of size 1 will be ignored.
     * @param nontrivial
     * @return 
     */
    public ArrayList<ArrayList<Integer>> components(boolean nontrivial) {
        ArrayList<ArrayList<Integer>> components = new ArrayList<>();
        
        // First step: initialize the search
        for (int i = 0; i < discovered.length; i++) {
            discovered[i] = false;
            processed[i] = false;
        }

        for (int i = 0; i < numVertices; i++) {
            if (!discovered[i]) {
                ArrayList<Integer> singleComponent = BFS(i);
                if (!nontrivial || (nontrivial && singleComponent.size() > 1))
                    components.add(singleComponent);
            }
        }
        
        return components;
    }
}
