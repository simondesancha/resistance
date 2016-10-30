/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package src.s21503324;

/**
 Implementation of a Monte-Carlo Search Tree, used by smartSpy
 Due to time constraints the search algorithm was not fully implemented and uses
 various short-cuts (see report)
 */
public class MonteCarloTree21503324 {
    String spies;
    String players;
    String name;
    
    private static final int NUM_GAMES = 1000;
    private static final double values[] = {0.94, 0.93, 0.99, 0.91, 0.62, 1.00};
    
    static class Round {
        int missionNumber;
        int failures;
        String team;
        String leader;
        int numTraitors;
    };
    
    
    MonteCarloTree21503324(String spies, String playerList, String name)
    {
        this.spies = spies;
        this.players = playerList;
        this.name = name;
    }
    
    //Returns if the best option is to vote/pass
    boolean ifShouldVote(Round rounds[], int missionNumber, int failures, int votingTime, String team, String leader)
    {
        return this.calculateOutcome(rounds, missionNumber, failures, true, votingTime, team, leader);
    }
    
    //Returns if the best option is to betray
    boolean ifShouldBetray(Round rounds[], int missionNumber, int failures, String team, String leader)
    {
       return this.calculateOutcome(rounds, missionNumber, failures, false, 0, team, leader);
    }
    
    //Runs the MCT playthroughs
    boolean calculateOutcome(Round rounds[], int missionNumber, int failures, boolean ifVoting, int votingTime, String team, String leader)
    {
        Round currentRound = new Round();
        currentRound.failures = failures;
        currentRound.missionNumber = missionNumber;
        currentRound.team = team;
        currentRound.leader = leader;
        
        double trueExit = 0;
        double trueResult = 0;
        double falseExit = 0;
        double falseResult = 0;
        
        long time = System.currentTimeMillis();
        
        //If we're betraying/voting true:
        for (int i = 0; i < NUM_GAMES; i++) {
            superGame21503324 g = setUpGame(rounds, missionNumber, ifVoting, true, currentRound);
            
            //Play the game and see if spies win:
            trueResult += g.playForMCT(missionNumber, failures, ifVoting, votingTime, team) ? 1 : 0;
            
            //Check how we're going for time:
            if(System.currentTimeMillis() - time > 400)
            {
                trueExit = i + 1;
                i = NUM_GAMES;
            }
        }
        
        
        //If we're betraying/voting false:
        for (int i = 0; i < NUM_GAMES; i++) {
            superGame21503324 g = setUpGame(rounds, missionNumber, ifVoting, false, currentRound);
            
            falseResult += g.playForMCT(missionNumber, failures, ifVoting, votingTime, team) ? 1 : 0;
            
            //Check how we're going for time:
            if(System.currentTimeMillis() - time > 800)
            {
                falseExit = i;
                i = NUM_GAMES;
            }
        }
        
        //Return whether betraying/voting true is best:
        return ((trueExit > 0) ? trueResult / trueExit : trueResult)
                    >= ((falseExit > 0) ? falseResult / falseExit : falseResult);
    }
    
    //Set up a game with the specified parameters
    superGame21503324 setUpGame(Round rounds[], int missionNumber, boolean ifVoting, boolean firstVote, Round currentRound)
    {
        int numPlayers = players.length();
        Agent agents[] = new Agent[numPlayers];
        
        //Set up our agents:
        for (int i = 0; i < numPlayers; i++) 
        {
            char c = (char)(65+i);
            
            if(spies.indexOf(c) >= 0)
            {
                //If this agent should be a spy:
                if(this.name.charAt(0) == c)
                {
                    //If this is OUR agent, assign a special spy bot for him.
                    //This bot should vote 'firstVote' for the first decision,
                    //And then randomly from then on out,
                    //Ensuring an evenly distributed set of random play-throughs
                    //NB: this is not a proper depth-first search, see report for details
                    agents[i] = new expertSpyBot21503324(firstVote);
                }
                else
                    agents[i] = new expertSpyBot21503324();
            }
            else
            {
                //If this bot should be a spy
                agents[i] = new bayBot21503324(values);
                
                //Simulate the bot being moved through the earlier rounds:
                for(int j = 0; j < missionNumber-1; j++)
                {
                    agents[i].get_status(spies, players, spies, rounds[j].missionNumber, rounds[j].failures);
                    agents[i].get_ProposedMission(rounds[j].leader, rounds[j].team);
                    agents[i].get_Mission(rounds[j].team);
                    agents[i].get_Traitors(rounds[j].numTraitors);
                }
            }
            
            //Now set up if we are deciding whether to vote on a mission or wheter to betray:
            if(ifVoting)
            {
                //If we need to decide whether to vote
                agents[i].get_status(spies, players, spies, currentRound.missionNumber, currentRound.failures);
                agents[i].get_ProposedMission(currentRound.leader, currentRound.team);
            }
            else
            {
                //If we need to decide whether to betray
                agents[i].get_status(spies, players, spies, currentRound.missionNumber, currentRound.failures);
                agents[i].get_ProposedMission(currentRound.leader, currentRound.team);
                agents[i].get_Mission(currentRound.team);
            }
        }
        
        //Set up our special game
        superGame21503324 g = new superGame21503324();
        
        for(Agent a : agents)
        {
            if(a instanceof expertSpyBot21503324)
                g.addSpy(a);
            else
                g.addResistance(a);
        }
        
        g.setupForMCT(missionNumber, numPlayers);
        
        return g;
    }
    
}

