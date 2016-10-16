/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package src.main;
import java.util.Arrays;
import java.util.Random;



/**
 *
 * Fails ~20 - %30 when versing random agent
 */
public class statBot implements Agent {
    boolean ifSpy;
    private char name;
    private char spyList[];
    private char playerList[];
    int missionNumber;
    int missionID;
    int numPlayers;
    int numMissions;
    Random rand;
    
    boolean voteValue;
    int numPlayersOnMission;
    
    //Statistics:
    int nominationLog[][];
    boolean missionVotes[][]; // [mission number][player vote] == pass/fail
    boolean missionPlayers[][]; //[mission number][player id] == if on/off mission
    float missionSuccess[]; // [mission number] == num betrays (success if 0)
    
    //Suspicion tables:
    float suspicion[]; // [player id]  = suspicion value
    float suspicionTableValues[];
    
    //Suspicion table indexes:
    private static final int PLAYER_ON_FAILED_MISSION = 0;
    private static final int PLAYER_VOTES_FOR_FAILED_MISSION = 1;
    
    statBot()
    {
        
    }
    
    public int getPlayerIndex(char player)
    {
        for(int i = 0; i < numPlayers; i++)
            if(playerList[i] == player)
                return i;
        return 0;
    }
    
    private void initArrays()
    {
        //Stats:
        nominationLog = new int[numPlayers][numPlayers]; //should be already filled with zeros
        missionVotes = new boolean[numMissions][numPlayers];
        missionPlayers = new boolean[numMissions][numPlayers];
        missionSuccess = new float[numMissions];
        
        //Suspicion:
        suspicion = new float[numPlayers];
        Arrays.fill(suspicion, (float)1/numPlayers);
        
        //for testing:
        suspicionTableValues = new float[5];
        suspicionTableValues[PLAYER_ON_FAILED_MISSION] = (float) 30; //ie double the suspicion
        suspicionTableValues[PLAYER_VOTES_FOR_FAILED_MISSION] = (float) 1.5; //ie double the suspicion
    }
    
    
    private void updateSuspicion()
    {
        //First look at the previous mission and the players on them:
        if(missionSuccess[missionID-1] > 0)
        {
            for (int i = 1; i < numPlayers; i++)
            {
                if(missionPlayers[missionID-1][i] == true)
                {
                    System.out.printf("%d was on failed mission\n", i);
                    //This player was on a mission, do suspicion calc
                    suspicion[i] *= suspicionTableValues[PLAYER_ON_FAILED_MISSION]*missionSuccess[missionID-1]*missionSuccess[missionID-1];
                }
            }
        }
        
        //Now look at who voted for a failed mission
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
        }
        
        System.out.println("Suspicion Values: ");
        for (int i = 1; i < numPlayers; i++) {
            System.out.printf("%f\n", suspicion[i]);
        }
    }
     
    //See if the previous mission passed
  /*  private void checkOutcome(int failures)
    {
        for(int i = 0; i < missionNumber - 2; i++)
            if(missionSuccess[i] > 0)
                failures--;
        
        if(failures > 0)
        {
            System.out.printf("Previous mission failed\n");
        }
        else
        {
            System.out.printf("Previous mission passed\n");
            missionSuccess[missionID-1] = true;
        }
    }*/
    
    @Override
    public void get_status(String name, String players, String spies, int mission, int failures) {
        this.name = name.charAt(0);
        this.playerList = players.toCharArray();
        this.missionNumber = mission;
        missionID = mission-1;
        numPlayers = players.length();
        numMissions = 5; //I'm guessing
        ifSpy = (spies.contains(name));
        spyList = spies.toCharArray();
        
        rand = new Random();
        
        if(missionID == 0)
        {
            System.out.printf("Spy: %d\n", ifSpy ? 1 : 0);
            
            initArrays();
        }
        else
        {
         //   checkOutcome(failures);   
            updateSuspicion();
        }
    }

    @Override
    public String do_Nominate(int number) {
        String s = new String();
        s += name;
        for(int i = 0; i < number-1; i++)
        {
            char c = playerList[rand.nextInt(numPlayers)];
            
            while(c == name)
                c = playerList[rand.nextInt(numPlayers)];
            s += c;
        }
        return s;
    }
    
    @Override
    public void get_ProposedMission(String leader, String mission) {
        char team[] = mission.toCharArray();
        
        //Log the nominations:
        logNominations(leader, team);
        
        if(missionNumber == 5)
        {
            voteValue = !ifSpy;
            return;
        }
        else if(ifSpy)
        {
            int numSpies = 0;
            for(char c : team)
                for(char x : spyList)
                    if(x == c) numSpies++;
            
            voteValue = numSpies > 1;
            return;
        }
        
        boolean haveMe = false;
        for(char c : team)
            if(c == name)
                haveMe = true;
        
        if(!haveMe && team.length == 3)
        {
            voteValue = false;
            return;
        }
        voteValue = true;        
    }

    @Override
    public boolean do_Vote() {
        return voteValue;
    }

    
    
    @Override
    public void get_Votes(String yays) {
        logVotes(yays);
    }

    @Override
    public void get_Mission(String mission) {
        this.logMission(mission);
    }

    @Override
    public boolean do_Betray() {
        return true;
    }

    @Override
    public void get_Traitors(int traitors) {
        missionSuccess[missionID] = (float)traitors/numPlayersOnMission;
        
        if(traitors ==  0)
            System.out.println("Mission passed");
        else
            System.out.printf("Mission failed with %d traitors\n", traitors);        
    }

    @Override
    public String do_Accuse() {
        return new String();
    }

    @Override
    public void get_Accusation(String accuser, String accused) {
    }
    
    
    private void logNominations(String leader, char team[])
    {
        int leaderIndex = getPlayerIndex(leader.charAt(0));
        
        for(char player : team)
            nominationLog[leaderIndex][getPlayerIndex(player)]++;
    }
    
    private void logVotes(String yays)
    {
        char votesYay[] = yays.toCharArray();
        
        Arrays.fill(missionVotes[missionID], false);
        for(char player : votesYay)
            missionVotes[missionID][getPlayerIndex(player)] = true;
    }
    
    private void logMission(String mission)
    {
        char team[] = mission.toCharArray();
        
        Arrays.fill(missionPlayers[missionID], false);
        for(char player : team)
            missionPlayers[missionID][getPlayerIndex(player)] = true;
        
        numPlayersOnMission = team.length;
    }
    
}
