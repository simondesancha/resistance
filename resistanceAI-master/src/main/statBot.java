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
    
    statsClass stats;
    double values[];
    
    statBot(double values[])
    {
        this.values = values;
    }
    
    public double[] getSuspicion()
    {
        return stats.getSuspicion();
    }
    
    public int getPlayerIndex(char player)
    {
        for(int i = 0; i < numPlayers; i++)
            if(playerList[i] == player)
                return i;
        return 0;
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
            //System.out.printf("Spy: %d\n", ifSpy ? 1 : 0);
            
            stats = new statsClass(numPlayers, numMissions, playerList, values);
        }
        else
        {
         //   checkOutcome(failures);   
            stats.updateMission(missionID);
            stats.updateSuspicion();
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
        stats.logNominations(leader, team);
        
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
        stats.logVotes(yays);
    }

    @Override
    public void get_Mission(String mission) {
        stats.logMission(mission);
    }

    @Override
    public boolean do_Betray() {
        return true;
    }

    @Override
    public void get_Traitors(int traitors) {
        stats.logTraitors(traitors);       
    }

    @Override
    public String do_Accuse() {
        return new String();
    }

    @Override
    public void get_Accusation(String accuser, String accused) {
    }
}