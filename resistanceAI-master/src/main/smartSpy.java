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
public class smartSpy implements Agent {
    
    private char name;
    private String spyList;
    private char playerList[];
    private char resistanceList[];
    int missionNumber;
    int numPlayers;
    
    int numFailures;
    Random rand;
    
    String leader;
    String missionPlayers;
    
    
    //Game constants:
    int numMissions;
    int totalSpies;
    int requiredFailures;
    
    private static final int totalSpiesBase = 5;
    private static final int totalSpiesList[] = {2, 2, 3, 3, 3, 4}; //[numPlayer - totalSpiesBase] = number of spies in game
    
    //M-C tree:
    MonteCarloTree MCT;
    Node currentNode;
    
    @Override
    public void get_status(String name, String players, String spies, int mission, int failures) {
        this.name = name.charAt(0);
        playerList = players.toCharArray();
        missionNumber = mission;
        numPlayers = players.length();
        numFailures = failures;
        rand = new Random();
        
        spyList = spies;
        
        resistanceList = new char[players.length() - spies.length()];
        
        int i = 0;
        for(char p : playerList)
        {
            //Check if player is spy
            if(spyList.indexOf(p) < 0)
                    continue;
            
            resistanceList[i++] = p;            
        }
        
        //Game constants:
        numMissions = 5;
        requiredFailures = 3;
        totalSpies = totalSpiesList[numPlayers-totalSpiesBase];
        
        //Set our current node:
        MCT = new MonteCarloTree();
        currentNode = new Node();
        currentNode.failures = failures;
        currentNode.mission = mission;
    }

    //Adds ourself and only resistance players:
    @Override
    public String do_Nominate(int number) {
        String s = new String();
        s += name;
        for(int i = 0; i < number-1; i++)
        {
            char c = resistanceList[rand.nextInt(resistanceList.length)];
            
            s += c;
        }
        return s;
    }

    @Override
    public void get_ProposedMission(String leader, String mission) {
        missionPlayers = mission;
        this.leader = leader;
    }

    @Override
    public boolean do_Vote() {
        //Set our node and search for best possibility:
        currentNode = new Node();
        currentNode.failures = this.numFailures;
        currentNode.mission = this.missionNumber;
        currentNode.type = Node.nodeType.Voting;
        currentNode.proposedMission = missionPlayers;
        currentNode.leader = "B";
        currentNode.leaderID = 1;//testing
        currentNode.depth = 0;
        
        //Run simulation:
        MCT.doNode(currentNode);
        
        System.out.println("Considering Voting.....");
        
        System.out.println("Counter: " + currentNode.counter);
        
        System.out.println("Score: " + currentNode.children[0].utility + " vs " + currentNode.children[1].utility);
        if(currentNode.children[0].utility > currentNode.children[1].utility)
            return true;
        return false;
    }

    @Override
    public void get_Votes(String yays) {
        
    }

    @Override
    public void get_Mission(String mission) {
    }

    @Override
    public boolean do_Betray() {
/*        char team[] = missionPlayers.toCharArray();
        
        int numSpies = 0;
        for (char c : team) {
            if(spyList.indexOf(c) >= 0)
                    numSpies++;
        }
        
        //Sabotage only if we are only spy on mission
        if(numSpies == 1)
            return true;
        
        //ie need to sabotage every mission in order to win
        if(requiredFailures - numFailures > numMissions - missionNumber)
            return true;
            
        return false;*/

        currentNode = new Node();
        currentNode.failures = this.numFailures;
        currentNode.mission = this.missionNumber;
        currentNode.type = Node.nodeType.doBetray;
        currentNode.proposedMission = missionPlayers;
        currentNode.leader = "B";
        currentNode.leaderID = 1;//testing
        currentNode.depth = 0;
        
        //Run simulation:
        MCT.doNode(currentNode);
        
        System.out.println("Considering Betray.....");
        
        System.out.println("Counter: " + currentNode.counter);
        
        System.out.println("Score: " + currentNode.children[0].utility + " vs " + currentNode.children[1].utility);
        if(currentNode.children[0].utility > currentNode.children[1].utility)
        {
            System.out.println("Betray true-----------------------");
            return true;
        }
        return false;
    }

    @Override
    public void get_Traitors(int traitors) {
    }

    @Override
    public String do_Accuse() {
        return new String();
    }

    @Override
    public void get_Accusation(String accuser, String accused) {
    }
    
}

