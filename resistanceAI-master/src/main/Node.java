/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package src.main;

/**
 *
 * @author root
 */
public class Node {
    public enum nodeType {
        Voting,
        doBetray,
        TerminalGood,
        TerminalBad,
        Nomination,
        DepthLimited,
    }
    
    int mission;
    int failures;
    nodeType type;

    //If we are a voting/betrayal node, we need to specify the the outcome
    boolean vote;
    String proposedMission;
    String leader;
    int votingAttempt;
    int leaderID;
    
    Node children[];
    double utility;
    
    int counter;
    int depth;
    
    Node(Node copy)
    {
        mission = copy.mission;
        failures = copy.failures;
        type = copy.type;
        vote = copy.vote;
        proposedMission = copy.proposedMission;
        leader = copy.leader;
        children = new Node[2];
        utility = 0;
        votingAttempt = 0;
        counter = 0;
        
        depth = copy.depth + 1;
    }
    
    Node()
    {
        children = new Node[2];
        utility = 0;
        votingAttempt = 0;
        counter = 0;
        depth = 0;
    }
    
}
