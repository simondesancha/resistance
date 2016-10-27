/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package src.main;

import java.util.Random;

/**
 *
 * @author root
 */
public class MonteCarloTree {
    MCSimulation sim;
    Node root;
    
    private static final int NUM_PLAYERS = 5;
    
    MonteCarloTree()
    {
        //Initialise root node:
        root = new Node();
        root.failures = 0;
        root.type = Node.nodeType.Voting;
        root.mission = 1;
        
        sim = new MCSimulation(NUM_PLAYERS);
        sim.startGame(root);
    }
    
    void doNode(Node node)
    {
        node.counter++;
        if(node.depth > 40)
        {
            //System.out.println("depth limit reached");
            node.type = Node.nodeType.DepthLimited;
            return;
        }
        
        //System.out.println("Test--");
        switch (node.type) 
        {
            case Nomination:
                //Our turn to nominate:
                node.proposedMission = doNomination(node);
                doNode(node);
                break;
                
            case Voting:
                //our two children are votes yes and no,
                node.children[0] = new Node(node);
                node.children[0].vote = true;
                
                //Iteratively run the nodes, and add result in utility
                sim.runGameOnNode(node.children[0]);
                doNode(node.children[0]);
                node.utility += node.children[0].utility;
                node.counter += node.children[0].counter;
                //node.children[0] = null;
                
                //Now for the NO vote:
                node.children[1] = new Node(node);
                node.children[1].vote = false;
                
                //Iteratively run the nodes, and add result in utility
                sim.runGameOnNode(node.children[1]);
                doNode(node.children[1]);
                node.utility += node.children[1].utility;
                node.counter += node.children[1].counter;
                break;
                
            case doBetray:
                //our two childrean are betray yes and no,
                node.children[0] = new Node(node);
                node.children[0].vote = true;
                
                sim.runGameOnNode(node.children[0]);
                doNode(node.children[0]);
                node.utility += node.children[0].utility;
                
                //node.children[0] = null;
                
                node.children[1] = new Node(node);
                node.children[1].vote = false;
                
                sim.runGameOnNode(node.children[1]);
                doNode(node.children[1]);
                node.utility += node.children[1].utility;
                
                break;
                
            case TerminalGood:
                //System.out.println("Found good endpoint");
                node.utility = 1;
                break;
                
            case TerminalBad:
                //System.out.println("Found Bad endpoint");
                node.utility = -1;
                break;
                
            case DepthLimited:
                break;
        }
        
    }
    
    String doNomination(Node node)
    {
        String s = "ABC";
        return s;
    }
    
}

