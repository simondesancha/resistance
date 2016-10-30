/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package src.s21503324;
import java.util.Arrays;


/*
Implementation of Bayesian updating, as used in bayBot
*/
public class bayStats21503324 {
    //Round info:
    char playerList[];
    int ourIndex;
    int numPlayers;
    int missionID;
    int numPlayersOnMission;
    
    //Statistics:
    int leaderLog[]; // [mission number] == leader index
    boolean missionPlayers[][]; //[mission number][player id] == if on/off mission
    int missionSuccess[]; // [mission number] == num betrays (success if 0)
    
    
    Group groups[]; //[groupIndex()]
    Group individuals[]; //[groupIndex()]
    int maxIndex;
    class Group {
        double[] probAtLeastNumSpies; //[0 spy, 1 spy, 2 spy, ..] probability of AT LEAST i spies
        double[] probExactlyNumSpies; //[0 spy, 1 spy, 2 spy, ..] probability of exactly i spies
    }
    
    //Suspicion tables, calculated by 
    private double[] SUSPICION_VALUES;
    
    //Suspicion table indexes:    
    private static final int TOTAL_SPIES_BASE = 5;
    private static final int[] TOTAL_SPIES_LIST = {2, 2, 3, 3, 3, 4}; //[numPlayer - totalSpiesBase] = number of spies in game
    private static final int NUM_MISSIONS = 5;
    
    private static final boolean DEBUG = false; //if testing mode
    
    
    
    
    
    //-------------INITIALISING FUNCTIONS:
    
    bayStats21503324(int numPlayers, char playerList[], char name, double weightings[])
    {
        this.playerList = playerList;
        this.numPlayers = numPlayers;
        this.missionID = 0;
        this.ourIndex = getPlayerIndex(name);
        this.SUSPICION_VALUES = weightings;
        
        initArrays();
    }
    
    private void initArrays() 
    {
        //Stats:
        missionPlayers = new boolean[NUM_MISSIONS][numPlayers];
        missionSuccess = new int[NUM_MISSIONS];
        leaderLog = new int[NUM_MISSIONS];
        
        for (int i = 0; i < numPlayers; i++) 
                maxIndex += Math.pow(2, i);       
        
        
        individuals = new Group[numPlayers];
        for(int i = 0; i < numPlayers; i++)
        {
            individuals[i] = new Group();
            individuals[i].probExactlyNumSpies = new double[2];
            
            individuals[i].probExactlyNumSpies[1] = probOfNumberSpies(1, 1, numPlayers-1);
            individuals[i].probExactlyNumSpies[0] = 1 - individuals[i].probExactlyNumSpies[1];
        }
        
        groups = new Group[maxIndex];
        for (int i = 0; i < maxIndex; i++) {
            groups[i] = new Group();
            groups[i].probAtLeastNumSpies = new double[TOTAL_SPIES()+1];
            groups[i].probExactlyNumSpies = new double[TOTAL_SPIES()+1];
            
            double counter = 0;
            for (int j = TOTAL_SPIES(); j >= 0; j--) {
                groups[i].probExactlyNumSpies[j] = probOfNumberSpies(j, groupSize(i), numPlayers-1);//(double) (NchooseK(TOTAL_SPIES(), j)*NchooseK((numPlayers-1)-TOTAL_SPIES(), groupSize(i)-j)) / (double)NchooseK(numPlayers - 1, groupSize(i));
                
                //sum upwards
                groups[i].probAtLeastNumSpies[j] = groups[i].probExactlyNumSpies[j];
                if(j < TOTAL_SPIES())
                    groups[i].probAtLeastNumSpies[j] += groups[i].probAtLeastNumSpies[j+1];
                
                counter += groups[i].probExactlyNumSpies[j];
            }
        }
    }
    
    public void updateMission(int missionID)
    {
        this.missionID = missionID;
    }
    
    //-----------END INITIALISING FUNCTIONS
    
    //-----------GROUP INDEXING FUNCTIONS:
    //Returns true if the 'index' is in 'group'
    boolean ifInGroup(int index, int group[])
    {
        for(int i : group)
            if(index == i)
                return true;
        return false;
    }
    
    //Returns the index of specified group
    private int groupIndex(int group[])
    {
        int index = 0;
        for(int i : group)
            if(i != ourIndex)
                index += Math.pow(2, i);
        
        if(index == 0)
            return index;
        
        return index-1;
    }
    
    //Returns the index of the group made up by players NOT in the specified group
    private int antiIndex(int group[])
    { 
        int index = groupIndex(group)+1;
        
        return (maxIndex ^ index) - 1;
    }
    
    //Returns the group with the specified index
    private int[] indexToGroup(int index)
    {
        int length = groupSize(index);
        index++;
        int group[] = new int[length];
        int counter = 0;
        
        for (int i = 0; i < numPlayers; i++) {
            if(i != ourIndex)
                if((index & (0x1 << i)) > 0)
                {
                    if(counter >= length)        
                        System.out.println("eror");
                    group[counter++] = i;
                }
        }
        
        return group;
    }
    
    //Returns group size of group
    private int groupSize(int groupIndex)
    {
        //Need to erase ourIndex        
        return Integer.bitCount((groupIndex+1 | (0x1 << ourIndex))^(0x1 << ourIndex));
    }

    //Get the index of a player
    private int getPlayerIndex(char player)
    {
        for(int i = 0; i < numPlayers; i++)
            if(playerList[i] == player)
                return i;
        return 0;
    }
    
    
    //----------END GROUP INDEXING FUNCTIONS
    
    
    
    //-------------BAYESIAN FUNCTIONS:
    //Calculates and applies bayesian update on the individuals
    void doIndividualCalc(int group[], int numTraitors, int missionSize) {
        for (int i = 0; i < numPlayers; i++) {
            if (i != ourIndex && ifInGroup(i, group)) {
                Group g = individuals[i];

                double result = 0;
                for (int j = 1; j <= TOTAL_SPIES(); j++) {
                    double probJspiesGivenSpy = this.probSpyGivenAlreadySpies(1, j, missionSize, numPlayers - 1) * this.probBetrayalsGivenSpies(missionSize, j, numTraitors);

                    //Check the edge case:
                    if (this.probOfNumberSpies(j, missionSize, numPlayers - 1) == 0) 
                        continue;
                    

                    result += groups[groupIndex(group)].probExactlyNumSpies[j]
                            * baysianUpdate(probJspiesGivenSpy,
                                    g.probExactlyNumSpies[1],
                                    this.probOfNumberSpies(j, missionSize, numPlayers - 1));
                }

                g.probExactlyNumSpies[1] = result;

                g.probExactlyNumSpies[0] = 1 - g.probExactlyNumSpies[1];
            } else if (i != ourIndex) {
                Group g = individuals[i];

                double result = 0;
                for (int j = 0; j <= TOTAL_SPIES() - 1; j++) {
                    //If j spies inside group (given one already in anti) 0<j<TOTAL_SPIES()-1
                    //spies in anti-group = 1 + (TOTAL_SPIES()-1) - J
                    
                    double probJspiesGivenSpy = this.probOfNumberSpies(j, missionSize, (numPlayers - 1) - 1, TOTAL_SPIES() - 1) * this.probBetrayalsGivenSpies(missionSize, j, numTraitors);

                    //Check the edge case:
                    if (this.probOfNumberSpies(j, missionSize, numPlayers - 1) == 0) 
                        continue;                    

                    result += groups[groupIndex(group)].probExactlyNumSpies[j]
                            * baysianUpdate(probJspiesGivenSpy,
                                    g.probExactlyNumSpies[1],
                                    this.probOfNumberSpies(j, missionSize, numPlayers - 1));
                }
                g.probExactlyNumSpies[1] = result;

                g.probExactlyNumSpies[0] = 1 - g.probExactlyNumSpies[1];
            }
        }
    }
    
    //Calculates and applies an appoximate suspicision values onto a specified 'team',
    //based on the group that just went on a mission.
    void calculateTeam(int group[], int team[])
    {
        int length = team.length;
        Group g = groups[groupIndex(team)];
        
        int groupIndex = groupIndex(group);
        int antiIndex = antiIndex(group);
        double probNotAllSpies[] = {1 - groups[antiIndex].probAtLeastNumSpies[ groupSize(antiIndex) > TOTAL_SPIES() ? TOTAL_SPIES() : groupSize(antiIndex) ], //Probability anti group is not all spies
                1 - groups[groupIndex].probAtLeastNumSpies[ groupSize(antiIndex) > TOTAL_SPIES() ? TOTAL_SPIES() : groupSize(antiIndex) ] // probability group is not all spies
        };
        
        int numPossibleSpyCombinations = (int) Math.pow(2, length);
        
        double probOfOne = 0;
        for (int i = 0; i < team.length; i++) {
            probOfOne += individuals[team[i]].probExactlyNumSpies[1];
        }
        
        g.probAtLeastNumSpies[1] = probOfOne;
        //g.probExactlyNumSpies[0] = 1 - probOfOne;
    }
    
    //Loop through every possible group and do calculateTeam(..)
    void doAllGroups(int group[])
    {
        int groupIndex = groupIndex(group);
        int antiIndex = antiIndex(group);  
        
        for(int i = 0; i < maxIndex; i++)
        {
            if(i != groupIndex && i != antiIndex
                    && groupSize(i) != 1)
            {
                this.calculateTeam(group, this.indexToGroup(i));
            }
        }
    }
    
    //If we know for certain a certain group has a spy, apply this knowledge
    //to all larger groups which contain this smaller subgroup    
    void doGroupSubsets(int group[], int numTraitors, int missionSize)
    {
        //Do group first:
        Group g = groups[groupIndex(group)];
        int groupIndex = groupIndex(group);
        int antiIndex = antiIndex(group);  
        
        for(int i = 0; i < maxIndex; i++)
        {
            if(i != groupIndex && i != antiIndex
                    && groupSize(i) != 1)
            {
                //Make sure group is a subset of:
                if(((i+1) & (groupIndex+1)) == (groupIndex+1))
                {
                    if(groups[i].probAtLeastNumSpies[1] < g.probAtLeastNumSpies[1])
                        groups[i].probAtLeastNumSpies[1] = g.probAtLeastNumSpies[1];
                }
                
                //Now anti-group:
                if(((i+1) & (antiIndex+1)) == (antiIndex+1))
                {
                    if(groups[i].probAtLeastNumSpies[1] < groups[antiIndex].probAtLeastNumSpies[1])
                        groups[i].probAtLeastNumSpies[1] = groups[antiIndex].probAtLeastNumSpies[1];
                }
            }
        }
        
    }
    
    //Apply the probabilities to a group that just went on a mission
    void doGroupCalc(int group[], int numTraitors, int missionSize)
    {
        Group g = groups[groupIndex(group)];
        
        //Add what we know for certain:
        for(int i = 0; i <= numTraitors; i++)
        {
            g.probAtLeastNumSpies[i] = 1;
            
            if(i < numTraitors)
                g.probExactlyNumSpies[i] = 0;   //ie can't have 0 spies
            else
            {
                if(this.getProbMissionOutcome(missionSize, numTraitors, numPlayers-1) == 0)
                    g.probExactlyNumSpies[i] = 0;
                else
                    g.probExactlyNumSpies[i] = this.probBetrayalsGivenSpies(missionSize, i, numTraitors);
                
            }
                
        }
        
        //Do baysian for the other spies:
        for (int i = TOTAL_SPIES(); i > numTraitors; i--) {
            if(this.getProbMissionOutcome(missionSize, numTraitors, numPlayers-1) == 0)
                g.probExactlyNumSpies[i] = 0;
            else
                g.probExactlyNumSpies[i] = this.probBetrayalsGivenSpies(missionSize, i, numTraitors);
            
            //Note need to sum up
            g.probAtLeastNumSpies[i] = g.probExactlyNumSpies[i];
            if(i < TOTAL_SPIES())
                g.probAtLeastNumSpies[i] += g.probAtLeastNumSpies[i+1];
        }
        
        doAntiGroupCalc(group, numTraitors, missionSize);
    }
    
    //Apply calculations the the group that did NOT go on the mission
    void doAntiGroupCalc(int group[], int numTraitors, int missionSize)
    {
        Group g = groups[antiIndex(group)];
        
        for(int i = TOTAL_SPIES(); i >= 0; i--)
        {
            //P(1 spy in anti-group) = P(TOTAL_SPIES() - 1 spy in group)
            g.probExactlyNumSpies[i] = groups[groupIndex(group)].probExactlyNumSpies[TOTAL_SPIES()-i];
            
            //sum up:
            g.probAtLeastNumSpies[i] = g.probExactlyNumSpies[i];
            if(i < TOTAL_SPIES())
                g.probAtLeastNumSpies[i] += g.probAtLeastNumSpies[i+1];    
                
        }
    }
    
    //Apply probability to the leader of a mission
    void doLeaderCalc(int leader)
    {
            //DOing subset:
        for (int i = 0; i < maxIndex; i++) {
            if (groupSize(i) != 1) {
                //Make sure group is a subset of:
                if (this.ifInGroup(leader, indexToGroup(i))) {
                    groups[i].probAtLeastNumSpies[1] += this.SUSPICION_VALUES[5];
                }
            }
        }
    }
    
    //Update our suspicion values after a mission
    public void updateSuspicion()
    {
        //first check if we were on mission:
        int ifOnMission = 0;
        
        if(missionPlayers[missionID-1][ourIndex] == true)
            ifOnMission = 1;
        
        
        int n = numPlayersOnMission-ifOnMission;
        int group[] = new int[n];

        //Get the players who were on the mission (ignoring ourself)
        int counter = 0;
        for (int i = 0; i < numPlayers; i++) 
        {
            if (missionPlayers[missionID - 1][i] == true) 
            {
                print("Was on mission: " + i);
                if(i != ourIndex)
                    group[counter++] = i;
            }
        }

        
        if(missionSuccess[missionID - 1] > 0) 
        {
            //If the mission failed:
            doGroupCalc(group, missionSuccess[missionID - 1], n);
            
            if(missionID != 1)
                doLeaderCalc(this.leaderLog[missionID-1]);
            
            doIndividualCalc(group, missionSuccess[missionID - 1], n);
            this.doGroupSubsets(group, missionSuccess[missionID - 1], n);            
        }
        else
        {
            //If the mission passed
            doGroupCalc(group, missionSuccess[missionID - 1], n);
            
            doIndividualCalc(group, missionSuccess[missionID - 1], n);
            this.doAllGroups(group);
            this.doGroupSubsets(group, missionSuccess[missionID - 1], n);
        }
    }    
    //--------------END BAYESIAN FUNCTIONS
    
    
    
    
    
    //--------------PROBABILITY FUNCTIONS
    private double baysianUpdate(double HgivenE, double H, double E)
    {
        return HgivenE*H/E;
    }
    
    double probSpyBetrays(int missionSize, int numTraitors)
    {
        if(numTraitors > missionSize)
            return 0;
        
        return this.SUSPICION_VALUES[missionID-1];
    }
    
    //Estimating the probability that mission fails with the given number of players and traitors (see report)
    private double getProbMissionOutcome(int missionSize, int numTraitors, int totalPool)
    {        
        double result = 0;
        
        for (int i = 1; i <= missionSize; i++) {
            result += probOfNumberSpies(i, missionSize, totalPool)*probBetrayalsGivenSpies(missionSize, i, numTraitors);
        }        
        return result;
    }
    
    //Probability that a spy betrays, given the number of players and spies on a mission
    //Assumes a binomial distribution
    private double probBetrayalsGivenSpies(int missionSize, int numSpies, int numTraitors)
    {
        if(numSpies < numTraitors || missionSize < numTraitors)
            return 0;
        
        //Calculate binomial distribution:
        double prob = probSpyBetrays(missionSize, numTraitors);
        return this.NchooseK(numSpies, numTraitors) * Math.pow(prob, numTraitors) * Math.pow(1 - prob, numSpies - numTraitors);
    }
    
    //Probilitity of a subset of players containing the specified number of spies, given some spies are already outside the subset
    private double probSpyGivenAlreadySpies(int givenSpies, int numSpies, int missionSize, int totalPool)
    {   
        return ((double) (NchooseK(TOTAL_SPIES()-givenSpies, numSpies-givenSpies)*NchooseK(totalPool-TOTAL_SPIES(), missionSize-numSpies))) 
                / (double)NchooseK(totalPool-givenSpies, missionSize-givenSpies);
    }
    
    //Probility of a specified number of spies in a mission of certain size
    //Uses binomial distribution
    double probOfNumberSpies(int n, int missionSize, int totalPool)
    {
        return ((double) (NchooseK(TOTAL_SPIES(), n)*NchooseK(totalPool-TOTAL_SPIES(), missionSize-n))) / (double)NchooseK(totalPool, missionSize);
    }
    
    double probOfNumberSpies(int n, int missionSize, int totalPool, int totalSpies)
    {
        return ((double) (NchooseK(totalSpies, n)*NchooseK(totalPool-totalSpies, missionSize-n))) / (double)NchooseK(totalPool, missionSize);
    }
    
    //--------------END PROBABILITY FUNCTIONS
    
    
    
    
    
    //-------------MATH/MISC FUNCITONS:
    int factorial(int n)
    {
        int result = 1;
        
        for (int i = n; i > 1; i--) {
            result *= i;
        }
        
        return result;
    }
    
    //Calculates the binomial coefficient
    int NchooseK(int n, int k)
    {
        return factorial(n) / (factorial(k) * factorial(n - k));
    }
    
    
    
    //Log the nominations (currently ignored)
    public void logNominations(String leader, char team[])
    {
        
    }
    
    //Log the votes (currently ignored)
    public void logVotes(String yays)
    {
        
    }
    
    //Log the past mission
    public void logMission(String mission, char leader)
    {
        char team[] = mission.toCharArray();
        
        Arrays.fill(missionPlayers[missionID], false);
        for(char player : team)
            missionPlayers[missionID][getPlayerIndex(player)] = true;
        
        numPlayersOnMission = team.length;
        this.leaderLog[missionID] = getPlayerIndex(leader);
        
        print("Mission: " + mission + " led by " + leader);
    }
    
    //Log the results of the last mission
    public void logTraitors(int traitors)
    {
        missionSuccess[missionID] = traitors;
        
        if(traitors ==  0) 
           print("Mission passed");
        else
            print("Mission failed with " + traitors + "traitors"); 
    }
    
    //Conveinence for printing
    private void print(String s)
    {
        if(DEBUG) 
            System.out.println(s);
    }
    
    //Convenience for number of spies
    private int TOTAL_SPIES()
    {
        return this.TOTAL_SPIES_LIST[numPlayers-TOTAL_SPIES_BASE];
    }
    
    
    //--------------DECISION FUNCTIONS
    //Returns the group least likely to contain a spy. Always contains self.
    public char[] getTeam(int length)
    {
        double maxProb = 0;
        int index = 0;
        for(int i = 0; i < maxIndex; i++)
        {
            if(groupSize(i) == length - 1) //as we will put ourselves on
            {
                if(groups[i].probExactlyNumSpies[0] > maxProb)
                {
                    maxProb = groups[i].probExactlyNumSpies[0];
                    index = i;
                }
            }
        }
        
        int team[] = this.indexToGroup(index);
        char teamWithMe[] = new char[length];
        int counter = 0;
        
        teamWithMe[counter++] = playerList[ourIndex];
        for(int i : team)
            teamWithMe[counter++] = playerList[i];
        
        return teamWithMe;
    }
    
    //Determines if the specified group is likely to contain a spy
    public boolean containsSpy(char[] team)
    {
        int group[] = new int[team.length];
        
        for (int i = 0; i < team.length; i++) {
            group[i] = getPlayerIndex(team[i]);
        }
        
        if(group.length == numPlayers-TOTAL_SPIES() &&
                !this.ifInGroup(ourIndex, group))
            return true;
        
        if(missionID == 0)
            return false;
        
        
        double minProb = 10000;
        for(int i = 0; i < maxIndex; i++)
        {
            if(groupSize(i) == group.length-1) //as we are on it
            {
                if(groups[i].probAtLeastNumSpies[1] < minProb)
                {
                    minProb = groups[i].probAtLeastNumSpies[1];
                }
            }
        }
        
        double result = groups[groupIndex(group)].probAtLeastNumSpies[1];
        
        return(result > (minProb));
    }
}
