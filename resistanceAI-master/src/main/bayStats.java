/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package src.main;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;



/**
 *
 * Fails ~20 - %30 when versing random agent
 */
public class bayStats {
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
    private static final int SPY_BETRAYS_FIRST_MISSION = 0;
    
    private static final int TOTAL_SPIES = 2;
    
    bayStats(int numPlayers, int numMissions, char playerList[])
    {
        this.playerList = playerList;
        this.numPlayers = numPlayers;
        this.numMissions = numMissions;
        this.missionID = 0;
        
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
        Arrays.fill(suspicion, (float)1/(numPlayers-1));
        
        //for testing:
        suspicionTableValues = new double[11];
        suspicionTableValues[SPY_BETRAYS_FIRST_MISSION] = 0.4; //ie double the suspicion
        Arrays.fill(suspicionTableValues, SPY_BETRAYS_FIRST_MISSION+1, SPY_BETRAYS_FIRST_MISSION+5, 0.8);
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
        //first check if we were on mission:
        int ifOnMission = 0;
        
        if(missionPlayers[missionID-1][0] == true)
            ifOnMission = 1;
        
        //First look at the previous mission and the players on them:
        for (int i = 1; i < numPlayers; i++)
        {
            if(missionPlayers[missionID-1][i] == true)
            {
                if(missionSuccess[missionID-1] > 0)
                {
                    this.numFailedMissions[i]++;
                    System.out.printf("%d was on failed mission\n", i);
                    
                    //testing:
                    
                    if(numPlayersOnMission-ifOnMission == missionSuccess[missionID-1])
                        System.out.println("Caught perfect game");

                    double sus = baysianUpdate(missionFailedGivenSpy(numPlayersOnMission, missionSuccess[missionID-1]),
                            suspicion[i], 
                            getProbMissionOutcome(numPlayersOnMission-ifOnMission, missionSuccess[missionID-1], numPlayers-1));

                    //System.out.printf("old sus %f, new sus: %f\n", suspicion[i], sus);
                    suspicion[i] = sus;
                }
                else
                {
                    //Mission passed
                    System.out.printf("%d was on successful mission\n", i);
                    
                    double sus = baysianUpdate((1-missionFailedGivenSpy(numPlayersOnMission, missionSuccess[missionID-1])),
                            suspicion[i],
                            (1-getProbMissionOutcome(numPlayersOnMission-ifOnMission, missionSuccess[missionID-1], numPlayers-1)));
                    suspicion[i] = sus;
                }
            }
        }
        
        System.out.println("Suspicion Values: ");
        for (int i = 0; i < numPlayers; i++) {
            System.out.printf("%f\n", suspicion[i]);
        }
    }
    
    
    
    
    //Probability:
    
    private double baysianUpdate(double HgivenE, double H, double E)
    {
        return HgivenE*H/E;
    }
    
    //Estimate of mission failing given spy:
    private double missionFailedGivenSpy(int missionSize, int numTraitors)
    {
        return probSpyBetrays(missionSize, numTraitors);
    }
    
    double probSpyBetrays(int missionSize, int numTraitors)
    {
        if(numTraitors > missionSize)
            return 0;
        
        return this.suspicionTableValues[SPY_BETRAYS_FIRST_MISSION + missionID-1];
    }
    
    
    
    //Estimating mission failing overall:
    private double getProbMissionOutcome(int missionSize, int numTraitors, int totalPool)
    {
        if(missionSize == numTraitors) //ie perfect game
            return 0.001;
        
        double result = 0;
        
        for (int i = 1; i <= missionSize; i++) {
            result += probOfNumberSpies(i, missionSize, totalPool)*probBetrayalsGivenSpies(missionSize, i, numTraitors);
        }        
        return result;
    }
    
    private double probBetrayalsGivenSpies(int missionSize, int numSpies, int numTraitors)
    {
        if(numSpies < numTraitors)
            return 0;
        
        //Calculate binomial distribution:
        double prob = probSpyBetrays(missionSize, numTraitors);
        return this.NchooseK(numSpies, numTraitors) * Math.pow(prob, numTraitors) * Math.pow(1 - prob, numSpies);
    }
    
    
    
    
    
    
    
    
    
    
    
    
    //-----------------------------------------------------------------------------------------------------------------------------------------------------------
    
    
    //Math/misc:
    
    int factorial(int n)
    {
        int result = 1;
        
        for (int i = n; i > 1; i--) {
            result *= i;
        }
        
        return result;
    }
    
    double NchooseK(int n, int k)
    {
        return factorial(n) / (factorial(k) * factorial(n - k));
    }
    
    double probOfNumberSpies(int n, int missionSize, int totalPool)
    {
        return NchooseK(TOTAL_SPIES, n) / NchooseK(totalPool, missionSize);
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
        
        if(traitors ==  0) 
           System.out.println("Mission passed");
        else
            System.out.printf("Mission failed with %d traitors\n", traitors); 
    }
    
    
    //Deciding:
    
    public char[] getTeam(int length)
    {
        List<Integer> trust = new ArrayList<>();
        
        for (int i = 0; i < length; i++) {
            double minSus = 1;
            int minIndex = 0;
            for (int j = 1; j < numPlayers; j++) {
                if(suspicion[j] < minSus && !trust.contains(j))
                {
                    minIndex = j;
                    minSus = suspicion[j];
                }
            }
            trust.add(minIndex);
        }
        
        char team[] = new char[length];
        for (int i = 0; i < trust.size(); i++) {
            team[i] = this.playerList[trust.get(i)];
        }
        
        return team;
    }
    
    public boolean containsSpy(int[] group)
    {
        if(missionID == 0)
            return true;
        
        List<Integer> spies = new ArrayList<>();
        
        for (int i = 0; i < TOTAL_SPIES; i++) {
            double maxSus = 0;
            int maxIndex = 0;
            for (int j = 1; j < numPlayers; j++) {
                if(suspicion[j] > maxSus && !spies.contains(j))
                {
                    maxIndex = j;
                    maxSus = suspicion[j];
                }
            }
            spies.add(maxIndex);
        }
        
        System.out.println("Proposed team: " + Arrays.toString(group));
        System.out.println("Likely spies: " + spies.toString());
        
        for (int i = 0; i < group.length; i++) {
            if(spies.contains(group[i]))
                return false;   
        }
        
        return true;
    }
}
