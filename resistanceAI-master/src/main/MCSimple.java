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
public class MCSimple {
    int numPlayers;
    int numSpies;
    Agent resistance[];
    Agent spies[];
    
    Agent agents[];
    
    String names[]; //[player id] = name
    String spyList;
    String playerList;
    
    private static final int totalSpiesBase = 5;
    private static final int totalSpiesList[] = {2, 2, 3, 3, 3, 4}; //[numPlayer - totalSpiesBase] = number of spies in game
    private static final int MISSION_NUMBER[] = {2,3,2,3,3};
    private static final int MISSION_LENGTH = 5;
    private static final char NAME_BASE = 'B'; //'A' is reserved for us
    private static final String OUR_NAME = "A";
    
    MCSimple(int numPlayers)
    {
        this.numPlayers = numPlayers - 1;//As we don't include ourself
        numSpies = totalSpiesList[numPlayers-totalSpiesBase] - 1; 
        names = new String[this.numPlayers];
        spyList = new String();
        playerList = new String();
        
        initPlayers();     
    }
    
    void initPlayers()
    {
        spies = new Agent[numSpies];
        resistance = new Agent[numPlayers-numSpies];   
        agents = new Agent[numPlayers];
        
        playerList += OUR_NAME;
        spyList += OUR_NAME;
        for (int i = 0; i < numSpies; i++) {
            agents[i] = new expertSpyBot();
            names[i] = Character.toString((char)(NAME_BASE + i));
            playerList += (char)(NAME_BASE + i);
            spyList += (char)(NAME_BASE + i);
        }
        
        for (int i = numSpies; i < numPlayers; i++) {
            agents[i] = new expertResistanceBot();
            names[i] = Character.toString((char)(NAME_BASE + i));
            playerList += (char)(NAME_BASE + i);
        }
    }
    
    int playerIndex(char player)
    {
        String s = Character.toString(player);
        for (int i = 0; i < names.length; i++) {
            if(names[i].equals(s))
                return i;
        }
        return 0;
    }
    
    void setupPlayers(Node node)
    {
        for(int i = 0; i < numPlayers; i++)
        {
            agents[i].get_status(names[i], playerList, spyList, node.mission, node.failures);
        }
    }
    
    String runVote(Node node)
    {
        String yays = new String();
        for(int i = 0; i < numPlayers; i++)
        {
            agents[i].get_ProposedMission(node.leader, node.proposedMission);
            
            if(agents[i].do_Vote())
                yays += names[i];
        }
        
        for (int i = 0; i < numPlayers; i++) 
            agents[i].get_Votes(yays);        
        
        return yays;
    }
    
    //Return true if passed
    boolean runMission(Node node)
    {
        for (int i = 0; i < numPlayers; i++) 
        {
            agents[i].get_Mission(node.proposedMission);
        }
        
        //Get our team
        int team[] = new int[node.proposedMission.length()];
        for (int i = 0; i < node.proposedMission.length(); i++) 
            team[i] = playerIndex(node.proposedMission.charAt(i));
        
        
        int traitors = 0;
        //Inform team:
        for(int i : team)
        {
            //If spy:
            if(spyList.contains(names[i]) && agents[i].do_Betray())
                    traitors++;
        }
        
        for (int i = 0; i < numPlayers; i++) 
        {
            agents[i].get_Traitors(traitors);
        }
        
        node.votingAttempt = 0;
        
        return !(traitors !=0 && (traitors !=1 || node.mission !=4 || numPlayers<7));
    }
    
    //Run our simulation on this node until the next action from our spy is needed (ie the children nodes)
    void runGameOnNode(Node node)
    {
        switch (node.type) {
            case Nomination:
                //If our spy just nominated, get the others to vote:
                node.type = Node.nodeType.Voting;
                node.vote = true;
                runGameOnNode(node);
                break;
            case Voting:
                //ie we just voted, need to get the outcome from the other players.
                setupPlayers(node);
                String yays = runVote(node);
                if(node.vote) //if we're voting yes
                    yays += OUR_NAME;
                if(yays.length() > (numPlayers+1)/2)
                {
                    //First check if we're on team:
                    if(node.proposedMission.contains(OUR_NAME))
                    {
                        //Pass it up to get a betrayal from our spy:
                        node.type = Node.nodeType.doBetray;
                        return;
                    }
                    
                    //the mission goes, evaluate the outcome.
                    boolean result = runMission(node);
                    evaluateMissionOutcome(node, result);
                    
                }
                else
                {
                    //the mission doesn't go, goes to next vote.
                    nextVotingRound(node);
                }   break;
            case doBetray:
                //Now we run the mission:
                boolean result = runMission(node);
                evaluateMissionOutcome(node, result);
                break;
        }
    }
    
    void evaluateMissionOutcome(Node node, boolean result)
    {
        if (!result) 
        {
            node.failures++;
            if (node.failures == 3) 
            {
                //System.out.println("Reached terminal, spies win");
                node.type = Node.nodeType.TerminalGood;
                return;
            }
        }

        //Next voting round:
        node.mission++;
        if (node.mission >= MISSION_LENGTH) 
        {
            //resistance wins!
            //System.out.println("Reached terminal, resistance win");
            node.type = Node.nodeType.TerminalBad;
            return;
        }

        nextVotingRound(node);
    }
    
    void nextVotingRound(Node node)
    {
        //Next leader in line:
        node.leaderID = (node.leaderID + 1) % (numPlayers + 1);
        node.votingAttempt++;
        
        if(node.leaderID != 0)
        {
            //Get nominations;
            String team = agents[node.leaderID-1].do_Nominate(MISSION_NUMBER[node.mission-1]);
            node.proposedMission = team;
            node.leader = names[node.leaderID-1];
            node.type = Node.nodeType.Voting;
        }
        else //It is our spies turn to nominate,
        {
            node.type = Node.nodeType.Nomination;
            node.leader = OUR_NAME;
        }
        
        if(node.votingAttempt == 5)
        {
            //We automatically pass it, 
            boolean result = runMission(node);
            evaluateMissionOutcome(node, result);
        }
    }
    
    //To be run on a root node, the start of game
    void startGame(Node node)
    {
        setupPlayers(node);
        
        //Pick random player to vote.
        Random rand = new Random();
        node.leaderID = rand.nextInt(numSpies+1);
        
        //Set up voting node:
        nextVotingRound(node); 
    }
    
}
