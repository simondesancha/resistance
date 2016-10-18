/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package src.main;

import java.util.Arrays;

/**
 *
 * @author root
 */
public class statsClass {
    //Round info:
    char playerList[];
    int numPlayers;
    int numMissions;
    int missionID;
    int numPlayersOnMission;
    
    //Statistics:
    int nominationLog[][];
    boolean missionVotes[][]; // [mission number][player vote] == pass/fail
    boolean missionPlayers[][]; //[mission number][player id] == if on/off mission
    int missionSuccess[]; // [mission number] == num betrays (success if 0)
    int numFailedMissions[]; // [player id]
    
    //Suspicion tables:
    double suspicion[]; // [player id]  = suspicion value
    double suspicionTableValues[];
    
    //Suspicion table indexes:
    private static final int PLAYER_ON_FAILED_MISSION = 0;
    private static final int PLAYER_ON_FAILED_MISSION_END = 6;
    private static final int PLAYER_VOTES_FOR_FAILED_MISSION = 1;
    
    statsClass(int numPlayers, int numMissions, char playerList[], double values[])
    {
        this.playerList = playerList;
        this.numPlayers = numPlayers;
        this.numMissions = numMissions;
        this.missionID = 0;
        this.suspicionTableValues = values;
        
        initArrays();
    }
    
    public double[] getSuspicion()
    {
        return suspicion;
    }
    
    public void updateMission(int missionID)
    {
        this.missionID = missionID;
    }
    
    private void initArrays()
    {
        //Stats:
        nominationLog = new int[numPlayers][numPlayers]; //should be already filled with zeros
        missionVotes = new boolean[numMissions][numPlayers];
        missionPlayers = new boolean[numMissions][numPlayers];
        missionSuccess = new int[numMissions];
        numFailedMissions = new int[numPlayers];
        
        //Suspicion:
        suspicion = new double[numPlayers];
        Arrays.fill(suspicion, (float)1/numPlayers);
        
        //for testing:
        //suspicionTableValues = new double[11];
        suspicionTableValues[PLAYER_ON_FAILED_MISSION] = (float) 30; //ie double the suspicion
        suspicionTableValues[PLAYER_VOTES_FOR_FAILED_MISSION] = (float) 1.5; //ie double the suspicion
    }
    
    public int getPlayerIndex(char player)
    {
        for(int i = 0; i < numPlayers; i++)
            if(playerList[i] == player)
                return i;
        return 0;
    }
    
    public void updateSuspicion()
    {
        //First look at the previous mission and the players on them:
        if(missionSuccess[missionID-1] > 0)
        {
            for (int i = 0; i < numPlayers; i++)
            {
                if(missionPlayers[missionID-1][i] == true)
                {
                    this.numFailedMissions[i]++;
                    //System.out.printf("%d was on failed mission\n", i);
                    //This player was on a mission, do suspicion calc
                    
                    double sus = doFailedMissionSuspicionCal(this.numFailedMissions[i], suspicion[i], (int)missionSuccess[missionID-1]);
                    
                    //System.out.printf("old sus %f, new sus: %f\n", suspicion[i], sus);
                    suspicion[i] = sus;
                }
            }
        }
        
        /*//Now look at who voted for a failed mission
        if(missionSuccess[missionID-1] > 0)
        {
            for (int i = 0; i < numPlayers; i++)
            {
                if(missionVotes[missionID-1][i] == true)
                {
                    System.out.printf("%d voted for a failed mission\n", i);
                    //This player was on a mission, do suspicion calc
                    suspicion[i] *= suspicionTableValues[PLAYER_VOTES_FOR_FAILED_MISSION];
                }
            }
        }*/
        
        //System.out.println("Suspicion Values: ");
        for (int i = 0; i < numPlayers; i++) {
            //System.out.printf("%f\n", suspicion[i]);
        }
    }
    
    private double doFailedMissionSuspicionCal(int totalFailedMissions, double previousSuspicion, int numTraitors)
    {
        double x[] = {(double) totalFailedMissions, previousSuspicion, (double) numTraitors};
        return this.doPolynomial(x, this.suspicionTableValues, this.PLAYER_ON_FAILED_MISSION);
    }
    
    private double doPolynomial(double x[], double weights[], int startIndex)
    {
        double result = 0;
        
        //powers of 3, 2:
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                result += weights[startIndex++]*Math.pow(x[i], 2)*x[j];
            }
        }
        
        //last component:
        result += weights[startIndex++]*x[0]*x[1]*x[2];
                
        return result;
    }
    
    public void logNominations(String leader, char team[])
    {
        int leaderIndex = getPlayerIndex(leader.charAt(0));
        
        for(char player : team)
            nominationLog[leaderIndex][getPlayerIndex(player)]++;
    }
    
    public void logVotes(String yays)
    {
        char votesYay[] = yays.toCharArray();
        
        Arrays.fill(missionVotes[missionID], false);
        for(char player : votesYay)
            missionVotes[missionID][getPlayerIndex(player)] = true;
    }
    
    public void logMission(String mission)
    {
        char team[] = mission.toCharArray();
        
        Arrays.fill(missionPlayers[missionID], false);
        for(char player : team)
            missionPlayers[missionID][getPlayerIndex(player)] = true;
        
        numPlayersOnMission = team.length;
    }
    
    public void logTraitors(int traitors)
    {
        missionSuccess[missionID] = traitors;
        
        if(traitors ==  0) ;
           //System.out.println("Mission passed");
        else ;
            //System.out.printf("Mission failed with %d traitors\n", traitors); 
    }
}
