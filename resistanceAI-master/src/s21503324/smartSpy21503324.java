/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package src.s21503324;

import java.util.Random;

/**
A Spy bot implementing a Monte-Carlo Search Tree 
 */
public class smartSpy21503324 implements Agent {
    //Misc game properties:
    private char name;
    private char resistanceList[];
    int missionNumber;
    int votingTime;
    int numFailures;
    String leader;
    String missionPlayers;
    
    //Game constants:
    private static final int NUM_MISSIONS = 5;
    private static final int REQUIRED_FAILURES = 3; 
    
    //M-C tree:
    MonteCarloTree21503324 MCT;
    
    //Array of all missions that have occured:
    MonteCarloTree21503324.Round rounds[]; 
    
    
    @Override
    public void get_status(String name, String players, String spies, int mission, int failures) {
        this.name = name.charAt(0);
        missionNumber = mission;
        numFailures = failures;
        votingTime = 0;
        
        //Initialising:
        if(missionNumber == 1)
        {
            resistanceList = new char[players.length() - spies.length()];

            //Create a list of resistance players:
            int i = 0;
            for (char p : players.toCharArray()) {
                //Check if player is spy
                if (spies.indexOf(p) < 0) {
                    continue;
                }

                resistanceList[i++] = p;
            }

            
            //Initialising the MCT:
            MCT = new MonteCarloTree21503324(spies, players, name);
            rounds = new MonteCarloTree21503324.Round[NUM_MISSIONS];
        }
    }

    //Adds ourself and only resistance players:
    @Override
    public String do_Nominate(int number) {
        String s = new String();
        Random rand = new Random();
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
        boolean result = MCT.ifShouldVote(rounds, missionNumber, numFailures, votingTime, this.missionPlayers, this.leader);
        votingTime++;
        return result;
    }

    @Override
    public void get_Votes(String yays) {
        
    }

    @Override
    public void get_Mission(String mission) {
    }

    @Override
    public boolean do_Betray() {
        votingTime = 0;
        
        //Certain expert rules that over-ride the MCT:
        
        //ie need to sabotage every mission in order to win
        if(REQUIRED_FAILURES - numFailures > NUM_MISSIONS - missionNumber)
            return true;
        
        if(REQUIRED_FAILURES - numFailures == 1)
            return true;
        
        //---End expert rules
        
        boolean result = MCT.ifShouldBetray(rounds, missionNumber, numFailures, missionPlayers, leader);
        return result;
    }

    @Override
    public void get_Traitors(int traitors) {
        if(traitors > 0)
            this.numFailures++;
        
        //Log our round:
        rounds[this.missionNumber-1] = new MonteCarloTree21503324.Round();
        rounds[this.missionNumber-1].missionNumber = missionNumber;
        rounds[this.missionNumber-1].failures = this.numFailures;
        rounds[this.missionNumber-1].leader = leader;
        rounds[this.missionNumber-1].team = this.missionPlayers;
        rounds[this.missionNumber-1].numTraitors = traitors;
    }

    @Override
    public String do_Accuse() {
        return new String();
    }

    @Override
    public void get_Accusation(String accuser, String accused) {
    }    
}

