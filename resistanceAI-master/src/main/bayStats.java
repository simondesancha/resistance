/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package src.main;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;



/**
 *
 * Fails ~20 - %30 when versing random agent
 */
public class bayStats {
    //Round info:
    char playerList[];
    int ourIndex;
    int numPlayers;
    int numMissions;
    int missionID;
    int numPlayersOnMission;
    
    //Statistics:
    int nominationLog[][];
    int leaderLog[]; // [mission number] == leader index
    boolean missionVotes[][]; // [mission number][player vote] == pass/fail
    boolean missionPlayers[][]; //[mission number][player id] == if on/off mission
    int missionSuccess[]; // [mission number] == num betrays (success if 0)
    int numFailedMissions[]; // [player id]
    
    //Suspicion tables:
    double suspicion[]; // [player id]  = suspicion value
    double suspicionTableValues[];
    
    //Suspicion table indexes:
    private static final double defaultValues[] = {0.4, 0.8, 0.8, 0.8, 0.8, 0.8, 0.5};
    
    private static final int SPY_BETRAYS_FIRST_MISSION = 0;
    private static final int SPY_LEADS_FAILED_MISSION = 5; 
    private static final int VOTE_THRESHOLD = 6;
    
    private static final int TOTAL_SPIES = 2;
    
    bayStats(int numPlayers, int numMissions, char playerList[], char name)
    {
        this.playerList = playerList;
        this.numPlayers = numPlayers;
        this.numMissions = numMissions;
        this.missionID = 0;
        this.ourIndex = this.getPlayerIndex(name);
        this.suspicionTableValues = defaultValues;
        
        initArrays();
    }
    
    public void setValues(double values[])
    {
        this.suspicionTableValues = values;
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
        leaderLog = new int[numMissions];
                
        //Suspicion:
        suspicion = new double[numPlayers];
        Arrays.fill(suspicion, (float)1/(numPlayers-1));
        suspicion[ourIndex] = 0;
        
    }
    
    int getPlayerIndex(char player)
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
        
        if(missionPlayers[missionID-1][ourIndex] == true)
            ifOnMission = 1;
        
        //First look at the previous mission and the players on them:
        for (int i = 0; i < numPlayers; i++)
        {
            if(missionPlayers[missionID-1][i] == true)
            {
                if(missionSuccess[missionID-1] > 0)
                {
                    this.numFailedMissions[i]++;
                    print(i + " was on failed mission");
                    
                    //testing:
                    
                    if(numPlayersOnMission-ifOnMission == missionSuccess[missionID-1])
                        print("Caught perfect game");

                    double sus = baysianUpdate(missionFailedGivenSpy(numPlayersOnMission, missionSuccess[missionID-1]),
                            suspicion[i], 
                            getProbMissionOutcome(numPlayersOnMission-ifOnMission, missionSuccess[missionID-1], numPlayers-1));

                    //print("old sus %f, new sus: %f\n", suspicion[i], sus);
                    suspicion[i] = sus;
                }
                else
                {
                    //Mission passed
                    print(i + " was on successful mission");
                    
                    double sus = baysianUpdate((1-missionFailedGivenSpy(numPlayersOnMission, missionSuccess[missionID-1])),
                            suspicion[i],
                            (1-getProbMissionOutcome(numPlayersOnMission-ifOnMission, missionSuccess[missionID-1], numPlayers-1)));
                    suspicion[i] = sus;
                }
            }
        }
        
        //Look at the leader of the mission, as long as it's not the first
        if(missionSuccess[missionID-1] > 0)
        {
            int leader = leaderLog[missionID - 1];
            double sus = baysianUpdate(missionFailedGivenLeader(), suspicion[leader],
                    getProbPlayerLeadsFailed(numPlayersOnMission-ifOnMission, missionSuccess[missionID-1], numPlayers-1));
            print("Leader " + leader + " failed; " + suspicion[leader] + " to " + sus);
            suspicion[leader] = sus;
        }
        else
        {
            int leader = leaderLog[missionID - 1];
            double sus = baysianUpdate(1 - missionFailedGivenLeader(), suspicion[leader],
                    1 - getProbPlayerLeadsFailed(numPlayersOnMission-ifOnMission, missionSuccess[missionID-1], numPlayers-1));
            print("Leader " + leader + " passed; " + suspicion[leader] + " to " + sus);
            suspicion[leader] = sus;
        }
        
        
        
        //Erase our suspicion:
        suspicion[ourIndex] = 0;
        
        print("Suspicion Values: ");
        for (int i = 0; i < numPlayers; i++) {
            print("" + suspicion[i]);
        }
    }
    
    
    
    
    //Probability:
    private double baysianUpdate(double HgivenE, double H, double E)
    {
        return HgivenE*H/E;
    }
    
    
    //Estimate of A leading failed mission:
    private double missionFailedGivenLeader()
    {
        return 0.05*(2*missionID - 1);
        //return bayStats.suspicionTableValues[SPY_LEADS_FAILED_MISSION];
    }
    
    private double getProbPlayerLeadsFailed(int missionSize, int numTraitors, int totalPool)
    {
        double result = 1;
        
        //result = Pr(A leads mission) * Pr(mission fails)
        result *= (1/((double)numPlayers));
        result *= getProbMissionOutcome(missionSize, numTraitors, totalPool);
        
        return result;
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
        return this.NchooseK(numSpies, numTraitors) * Math.pow(prob, numTraitors) * Math.pow(1 - prob, numSpies - numTraitors);
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
        print("Yays: " + yays);
        
        char votesYay[] = yays.toCharArray();
        
        Arrays.fill(missionVotes[missionID], false);
        for(char player : votesYay)
            missionVotes[missionID][getPlayerIndex(player)] = true;
    }
    
    public void logMission(String mission, char leader)
    {
        char team[] = mission.toCharArray();
        
        Arrays.fill(missionPlayers[missionID], false);
        for(char player : team)
            missionPlayers[missionID][getPlayerIndex(player)] = true;
        
        numPlayersOnMission = team.length;
        this.leaderLog[missionID] = getPlayerIndex(leader);
    }
    
    public void logTraitors(int traitors)
    {
        missionSuccess[missionID] = traitors;
        
        if(traitors ==  0) 
           print("Mission passed");
        else
            print("Mission failed with " + traitors + "traitors"); 
    }
    
    private void print(String s)
    {
        if(ourIndex == 0) ;
            //System.out.println(s);
    }
    
    
    //Deciding:
    
    public char[] getTeam(int length)
    {
        List<Integer> trust = new ArrayList<>();
        
        for (int i = 0; i < length; i++) {
            double minSus = -1;
            int minIndex = 0;
            for (int j = 0; j < numPlayers; j++) {
                if((suspicion[j] < minSus || minSus == -1) && !trust.contains(j))
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
            for (int j = 0; j < numPlayers; j++) {
                if(suspicion[j] > maxSus && !spies.contains(j))
                {
                    maxIndex = j;
                    maxSus = suspicion[j];
                }
            }
            spies.add(maxIndex);
        }
        
        print("Likely spies: " + spies.toString());
        
        //Calculate average suspiscion:
        double avSus = 0;
        for (int i = 0; i < numPlayers; i++) {
            avSus += suspicion[i];
        }
        avSus /= numPlayers-1;
        
        //Calculate how badly we don't want this mission:
        double teamSus = 0;
        for (int i = 0; i < group.length; i++) {
            if(spies.contains(group[i]))
                teamSus += suspicion[i];
        }
        
        double score = (teamSus/group.length) - avSus;
        print("Av Score: " + avSus + ", this score: " + teamSus/group.length);
        print("Score of: " + score);
        if(score < this.suspicionTableValues[VOTE_THRESHOLD])
        {
            return true;
        }
        
        for (int i = 0; i < group.length; i++) {
            if(spies.contains(group[i]))
                return false;   
        }
        
        return true;
    }
}
