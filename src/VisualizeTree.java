/* Simple graph drawing class
Bert Huang
COMS 3137 Data Structures and Algorithms, Spring 2009

This class is really elementary, but lets you draw 
reasonably nice graphs/trees/diagrams. Feel free to 
improve upon it!
 */

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class VisualizeTree extends JFrame {
    /*int width;
    int height;

   DecisionTreeNode root;

    public VisualizeTree() { //Constructor
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        width = 30;
        height = 30;
    }

    public VisualizeTree(String title, DecisionTreeNode root) { //Construct with label
        this.setTitle(title);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.root = root;
        width = 500;
        height = 600;
    }

    class DecisionTreeNode extends DecisionNode {
        int x, y;
        public DecisionTreeNode(int myX, int myY) {
            x = myX;
            y = myY;
        }
    }

    public void addNode(DecisionNode node) {
        //this.root.ad
        this.repaint();
    }

    public void addEdge(int i, int j) {
        //add an edge between nodes i and j
        edges.add(new edge(i, j));
        this.repaint();
    }

    public void paint(Graphics g) { // draw the nodes and edges
        FontMetrics f = g.getFontMetrics();
        int nodeHeight = Math.max(height, f.getHeight());

        g.setColor(Color.black);
        for (edge e : edges) {
            g.drawLine(nodes.get(e.i).x, nodes.get(e.i).y,
                    nodes.get(e.j).x, nodes.get(e.j).y);
        }

        for (Node n : nodes) {
            int nodeWidth = Math.max(width, f.stringWidth(n.name) + width / 2);
            g.setColor(Color.white);
            g.fillOval(n.x - nodeWidth / 2, n.y - nodeHeight / 2,
                    nodeWidth, nodeHeight);
            g.setColor(Color.black);
            g.drawOval(n.x - nodeWidth / 2, n.y - nodeHeight / 2,
                    nodeWidth, nodeHeight);

            g.drawString(n.name, n.x - f.stringWidth(n.name) / 2,
                    n.y + f.getHeight() / 2);
        }
    }
}

class testGraphDraw {
    //Here is some example syntax for the GraphDraw class
    public static void main(String[] args) {
        GraphDraw frame = new GraphDraw("Test Window");

        frame.setSize(400, 300);

        frame.setVisible(true);

        frame.addNode("a", 50, 50);
        frame.addNode("b", 100, 100);
        frame.addNode("longNode", 200, 200);
        frame.addEdge(0, 1);
        frame.addEdge(0, 2);
    }*/
}