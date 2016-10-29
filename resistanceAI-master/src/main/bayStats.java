/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package src.main;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;



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
    
    int numPossibleSpies[]; //[player id]
    
    Group groups[]; //[groupIndex()]
    Group individuals[]; //[groupIndex()]
    int maxIndex;
    class Group {
        double[] probAtLeastNumSpies; //[0 spy, 1 spy, 2 spy, ..] probability of AT LEAST i spies
        double[] probExactlyNumSpies; //[0 spy, 1 spy, 2 spy, ..] probability of exactly i spies
    }
    
    
    //Suspicion tables:
    double suspicion[]; // [player id]  = suspicion value
    double suspicionTableValues[];
    
    //Suspicion table indexes:
    private static final double defaultValues[] = {0.4, 0.8, 0.8, 0.8, 0.8, 0.8, 0.5};
    
    private static final int SPY_BETRAYS_FIRST_MISSION = 0;
    private static final int SPY_LEADS_FAILED_MISSION = 5; 
    private static final int VOTE_THRESHOLD = 6;
    
    private static final int TEST_1 = 0;
    private static final int TEST_2 = 5;
    
    
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
    
    //should ignore our index
    private int groupIndex(int group[])
    {
        int index = 0;
        for(int i : group)
            if(i != ourIndex)
                index += Math.pow(2, i);
        
        if(index == 0)
            return index;
        
        if(index-1 < 0 || index-1 >= maxIndex)
            System.out.println("error2");
        
        return index-1;
    }
    
    private int groupIndex(int index)
    {
        
        return (int) Math.pow(2, index)-1;
    }
    
    private int antiIndex(int group[])
    { 
        int index = groupIndex(group)+1;
        
        return (maxIndex ^ index) - 1;
    }
    
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
        numPossibleSpies = new int[numMissions];
        
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
            groups[i].probAtLeastNumSpies = new double[TOTAL_SPIES+1];
            groups[i].probExactlyNumSpies = new double[TOTAL_SPIES+1];
            
            double counter = 0;
            for (int j = TOTAL_SPIES; j >= 0; j--) {
                groups[i].probExactlyNumSpies[j] = probOfNumberSpies(j, groupSize(i), numPlayers-1);//(double) (NchooseK(TOTAL_SPIES, j)*NchooseK((numPlayers-1)-TOTAL_SPIES, groupSize(i)-j)) / (double)NchooseK(numPlayers - 1, groupSize(i));
                
                //sum upwards
                groups[i].probAtLeastNumSpies[j] = groups[i].probExactlyNumSpies[j];
                if(j < TOTAL_SPIES)
                    groups[i].probAtLeastNumSpies[j] += groups[i].probAtLeastNumSpies[j+1];
                
                counter += groups[i].probExactlyNumSpies[j];
            }
            if(counter < 0.99)
                System.out.println("Probability fail");
        }
                
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
    
    boolean ifInGroup(int index, int group[])
    {
        for(int i : group)
            if(index == i)
                return true;
        return false;
    }
    
    void doIndividualCalc(int group[], int numTraitors, int missionSize)
    {
        for (int i = 0; i < numPlayers; i++) 
        {
          /*  if(ifInGroup(i, group) && missionSize == 1)
            {
                //Already filled in, don't want to erase with an estimation
                continue;
            }
            else*/ 
            if(i != ourIndex && ifInGroup(i, group))
            {
                Group g = individuals[i];
                
                double probFailGivenSpy = 0;
                double result = 0;
                for (int j = 1; j <= TOTAL_SPIES; j++) {
                 //   probFailGivenSpy += this.probSpyGivenAlreadySpies(1, j, missionSize, numPlayers-1) * this.probBetrayalsGivenSpies(missionSize, j, numTraitors);
                    //result += groups[groupIndex(group)].probExactlyNumSpies[j] * (j) / (double)(missionSize);
                    
                  //  result += groups[groupIndex(group)].probExactlyNumSpies[j] * this.probBetrayalsGivenSpies(missionSize, TOTAL_SPIES-j, numTraitors)
                 //                               / (1-groups[groupIndex(group)].probExactlyNumSpies[0]);
                 
                      double probJspiesGivenSpy = this.probSpyGivenAlreadySpies(1, j, missionSize, numPlayers-1);
                      
                      if(this.probOfNumberSpies(j, missionSize, numPlayers-1) == 0)
                        continue;
                      
                      result +=    groups[groupIndex(group)].probExactlyNumSpies[j] *
                                                        baysianUpdate(probJspiesGivenSpy,
                                                                g.probExactlyNumSpies[1],
                                                                this.probOfNumberSpies(j, missionSize, numPlayers-1));
                }

              //  g.probExactlyNumSpies[1] = baysianUpdate(result,
                //                                    g.probExactlyNumSpies[1],
                  //                                  this.getProbMissionOutcome(missionSize, numTraitors, numPlayers-1));
                g.probExactlyNumSpies[1] = result;
                //if(g.probExactlyNumSpies[1] != 1)
                  //      g.probExactlyNumSpies[1] = Math.sqrt(result*g.probExactlyNumSpies[1]);

                g.probExactlyNumSpies[0] = 1 - g.probExactlyNumSpies[1];
            }
            else if(i != ourIndex)
            {
                Group g = individuals[i];
                
                double result = 0;
                double probFailGivenSpy = 0;
                for (int j = 0; j <= TOTAL_SPIES-1; j++) {
                    //probFailGivenSpy += this.probSpyGivenAlreadySpies(1, j, numPlayers-1 - missionSize, numPlayers-1) * this.probBetrayalsGivenSpies(missionSize, TOTAL_SPIES-j, numTraitors);
                    //          Probability of j spies     *    prob of i  being spy
                    //result += groups[antiIndex(group)].probExactlyNumSpies[j] * (j) / (double)(numPlayers-1 - missionSize);
                    
                    //Prob 1 spy in 
                    //result += groups[groupIndex(group)].probExactlyNumSpies[j] * this.probBetrayalsGivenSpies(missionSize, TOTAL_SPIES-j, numTraitors)
                      //                          / (1-groups[groupIndex(group)].probExactlyNumSpies[TOTAL_SPIES]);
                      
                      
                      //If j spies inside group (given one already in anti) 0<j<TOTAL_SPIES-1
                      //spies in anti-group = 1 + (TOTAL_SPIES-1) - J
                      
                    double probJspiesGivenSpy = this.probOfNumberSpies(j, missionSize, (numPlayers-1)-1, TOTAL_SPIES-1);
                                  //this.probSpyGivenAlreadySpies(1, j, missionSize, numPlayers-1);

                    if(this.probOfNumberSpies(j, missionSize, numPlayers-1) == 0)
                        continue;

                    result += groups[groupIndex(group)].probExactlyNumSpies[j] *
                                                      baysianUpdate(probJspiesGivenSpy,
                                                              g.probExactlyNumSpies[1],
                                                              this.probOfNumberSpies(j, missionSize, numPlayers-1));
                }
                
                //g.probExactlyNumSpies[1] = baysianUpdate(result,
                //                                   g.probExactlyNumSpies[1],
                 //                                   this.getProbMissionOutcome(missionSize, numTraitors, numPlayers-1));
                 g.probExactlyNumSpies[1] = result;
          //      if(g.probExactlyNumSpies[1] != 1)
            //            g.probExactlyNumSpies[1] = Math.sqrt(result*g.probExactlyNumSpies[1]);
                //g.probExactlyNumSpies[1] = result;

                g.probExactlyNumSpies[0] = 1 - g.probExactlyNumSpies[1];
            }
        }
    }    
    
    void calculateTeam(int group[], int team[])
    {
        int length = team.length;
        Group g = groups[groupIndex(team)];
        
        int groupIndex = groupIndex(group);
        int antiIndex = antiIndex(group);
        double probNotAllSpies[] = {1 - groups[antiIndex].probAtLeastNumSpies[ groupSize(antiIndex) > TOTAL_SPIES ? TOTAL_SPIES : groupSize(antiIndex) ], //Probability anti group is not all spies
                1 - groups[groupIndex].probAtLeastNumSpies[ groupSize(antiIndex) > TOTAL_SPIES ? TOTAL_SPIES : groupSize(antiIndex) ] // probability group is not all spies
        };
        
        int numPossibleSpyCombinations = (int) Math.pow(2, length);
        
    /*    double probOfOne = 0;
        for (int i = 0; i < team.length; i++) {
            probOfOne += individuals[team[i]].probExactlyNumSpies[1];
        }
        
        g.probAtLeastNumSpies[1] = probOfOne;
      */  
        //erase previous:
        Arrays.fill(g.probExactlyNumSpies, 0);
        for(int i = 0; i < numPossibleSpyCombinations; i++)
        {
            int numSpies = Integer.bitCount(i);
            if(numSpies > TOTAL_SPIES)
            {
                //g.probExactlyNumSpies[numSpies] = 0;
            }
            else
            {
                double result = 1;
                
                for (int j = 0; j < length; j++) {
                    if((i & (0x1 << j)) > 0)
                    {
                        //ie if team[j] is a spy
                        //result *= groups[ifInGroup(team[j], group) ? groupIndex : antiIndex]
                          //              .probAtLeastNumSpies[1];
                        result *= individuals[team[j]].probExactlyNumSpies[1];
                    }
                    else
                    {
                        //ie it team[j] is resistance
                        //result *= probNotAllSpies[ifInGroup(team[j], group) ? 1 : 0];
                        result *= individuals[team[j]].probExactlyNumSpies[0];
                    }
                }
                
                g.probExactlyNumSpies[numSpies] += result;
            }
        }
        
        //Sum up:
        for(int i = TOTAL_SPIES; i >= 0; i--)
        {
            g.probAtLeastNumSpies[i] = g.probExactlyNumSpies[i];
            if(i < g.probAtLeastNumSpies.length-1)
                g.probAtLeastNumSpies[i] += g.probAtLeastNumSpies[i+1];
        }
    }
    
    
    void doAllGroups(int group[])
    {
        int groupIndex = groupIndex(group);
        int antiIndex = antiIndex(group);  
        
        for(int i = 0; i < maxIndex; i++)
        {
            if(i != groupIndex && i != antiIndex
                    && groupSize(i) != 1)
            {
                //print("Index:" + i);
                check(groups[i].probExactlyNumSpies);
                this.calculateTeam(group, this.indexToGroup(i));
                //print("done:" + i);
                check(groups[i].probExactlyNumSpies);
            }
        }
    }
    
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
                  //      baysianUpdate(this.probBetrayalsGivenSpies(missionSize, i, numTraitors),
                    //                            g.probExactlyNumSpies[i],
                      //                          this.getProbMissionOutcome(missionSize, numTraitors, numPlayers-1));
                
            }
                
        }
        
        //Do baysian for the other spies:
        for (int i = TOTAL_SPIES; i > numTraitors; i--) {
            if(this.getProbMissionOutcome(missionSize, numTraitors, numPlayers-1) == 0)
                g.probExactlyNumSpies[i] = 0;
            else
                g.probExactlyNumSpies[i] = this.probBetrayalsGivenSpies(missionSize, i, numTraitors);
          //          baysianUpdate(this.probBetrayalsGivenSpies(missionSize, i, numTraitors),
            //                                g.probExactlyNumSpies[i],
              //                              this.getProbMissionOutcome(missionSize, numTraitors, numPlayers-1));
            
            //Note need to sum up
            g.probAtLeastNumSpies[i] = g.probExactlyNumSpies[i];
            if(i < TOTAL_SPIES)
                g.probAtLeastNumSpies[i] += g.probAtLeastNumSpies[i+1];
        }
        
        check(g.probExactlyNumSpies);
        
        doAntiGroupCalc(group, numTraitors, missionSize);
    }
    
    void doAntiGroupCalc(int group[], int numTraitors, int missionSize)
    {
        Group g = groups[antiIndex(group)];
        
        for(int i = TOTAL_SPIES; i >= 0; i--)
        {
            //P(1 spy in anti-group) = P(TOTAL_SPIES - 1 spy in group)
            g.probExactlyNumSpies[i] = groups[groupIndex(group)].probExactlyNumSpies[TOTAL_SPIES-i];
            
            //sum up:
            g.probAtLeastNumSpies[i] = g.probExactlyNumSpies[i];
            if(i < TOTAL_SPIES)
                g.probAtLeastNumSpies[i] += g.probAtLeastNumSpies[i+1];    
                
        }
    }
    
    void doLeaderCalc(int leader, boolean fail)
    {
        int leaderTeam[] = {leader};
        
        if(fail)
        {
            //DOing subset:
            for(int i = 0; i < maxIndex; i++) {
                if (groupSize(i) != 1) {
                    //Make sure group is a subset of:
                    if (this.ifInGroup(leader, indexToGroup(i)))
                        groups[i].probAtLeastNumSpies[1] += this.suspicionTableValues[5];
                }
            }
        }
        
    }
    
    public void updateSuspicion()
    {
        //first check if we were on mission:
        int ifOnMission = 0;
        
        if(missionPlayers[missionID-1][ourIndex] == true)
            ifOnMission = 1;
        
                
        int n = numPlayersOnMission-ifOnMission;
        int group[] = new int[n];

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
            //checkAll();
            doGroupCalc(group, missionSuccess[missionID - 1], n);
            //checkAll();
            //checkIndividuals();
            doIndividualCalc(group, missionSuccess[missionID - 1], n);
            this.doGroupSubsets(group, missionSuccess[missionID - 1], n);
            
            if(missionID != 1)
                doLeaderCalc(this.leaderLog[missionID-1], missionSuccess[missionID - 1]>0);
            //checkAll();
            //checkIndividuals();
            //doAllGroups(group);
            //checkIndividuals();
            //checkAll();
        }
        else
        {
            if(missionID != 1)
                doLeaderCalc(this.leaderLog[missionID-1], missionSuccess[missionID - 1]>0);
            doGroupCalc(group, missionSuccess[missionID - 1], n);
            doIndividualCalc(group, missionSuccess[missionID - 1], n);
            this.doGroupSubsets(group, missionSuccess[missionID - 1], n);
        }
        
        //Erase our suspicion:
        suspicion[ourIndex] = 0;
        
        print("Suspicion Values: ");
        for (int i = 0; i < numPlayers; i++) {
            if(i != ourIndex)
                print("" + individuals[i].probExactlyNumSpies[1]);
        }
        
        checkIndividuals();
    }
    
    void checkIndividuals()
    {
        double x = 0;
        for(int i = 0; i < numPlayers; i++)
            if(i != ourIndex)
                x += individuals[i].probExactlyNumSpies[1];
        
        if(x < 1.99 || x > 2.01)
            print("individual error..." + (2 - x));
    }
    
    void checkAll()
    {
        print("startin long check...");
        for(Group g : groups)
            check(g.probExactlyNumSpies);
        print(".....done...");
    }
    
    void check(double values[])
    {
        double x = 0;
        for(double d : values)
            x += d;
        
        if(x < 0.99);
            //print("Error in values... " + (1-x));
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
        
        return this.suspicionTableValues[TEST_1 + missionID-1];
    }
    
    
    
    //Estimating mission failing overall:
    private double getProbMissionOutcome(int missionSize, int numTraitors, int totalPool)
    {
        //if(missionSize == numTraitors) //ie perfect game
          //  return 0.001;
        
        double result = 0;
        
        for (int i = 1; i <= missionSize; i++) {
            result += probOfNumberSpies(i, missionSize, totalPool)*probBetrayalsGivenSpies(missionSize, i, numTraitors);
        }        
        return result;
    }
    
    private double probBetrayalsGivenSpies(int missionSize, int numSpies, int numTraitors)
    {
        if(numSpies < numTraitors || missionSize < numTraitors)
            return 0;
        
        //Calculate binomial distribution:
        double prob = probSpyBetrays(missionSize, numTraitors);
        return this.NchooseK(numSpies, numTraitors) * Math.pow(prob, numTraitors) * Math.pow(1 - prob, numSpies - numTraitors);
    }
    
    private double probSpyGivenAlreadySpies(int givenSpies, int numSpies, int missionSize, int totalPool)
    {   
        return ((double) (NchooseK(TOTAL_SPIES-givenSpies, numSpies-givenSpies)*NchooseK(totalPool-TOTAL_SPIES, missionSize-numSpies))) 
                / (double)NchooseK(totalPool-givenSpies, missionSize-givenSpies);
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
    
    int NchooseK(int n, int k)
    {
        return factorial(n) / (factorial(k) * factorial(n - k));
    }
    
    double probOfNumberSpies(int n, int missionSize, int totalPool)
    {
        return ((double) (NchooseK(TOTAL_SPIES, n)*NchooseK(totalPool-TOTAL_SPIES, missionSize-n))) / (double)NchooseK(totalPool, missionSize);
    }
    
    double probOfNumberSpies(int n, int missionSize, int totalPool, int totalSpies)
    {
        return ((double) (NchooseK(totalSpies, n)*NchooseK(totalPool-totalSpies, missionSize-n))) / (double)NchooseK(totalPool, missionSize);
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
        
        print("Mission: " + mission + " led by " + leader);
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
        if(ourIndex == 0) 
            ;//System.out.println(s);
    }
    
    
    //Deciding:
    
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
    
    public boolean containsSpy(int[] group)
    {
        if(!this.ifInGroup(ourIndex, group))
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
