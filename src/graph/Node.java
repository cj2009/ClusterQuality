package graph;

/*
 * A node represents a single node in the linked-list based adjacency list
 * of a graph.

 * Converted from the C code provided by Skiena, from the textbook 'The Algorithm 
 * Design Manual' 2nd edition (pg. 153).

 * @author chris_joseph
 */
public class Node {
    private int value;
    private Node next;
    
    public Node(int v) {
        value = v;
    }
    
    public void setNext(Node n) {
        next = n;
    }
    
    public Node getNext() {
        return next;
    }
    
    public int getValue() {
        return value;
    }
}
