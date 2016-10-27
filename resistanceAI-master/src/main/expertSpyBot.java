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
 * 
 */
public class expertSpyBot implements Agent {
    
    private String name;
    private String spyList;
    private String playerList;
    private String resistanceList[];
    int missionNumber;
    int numPlayers;
    
    int numFailures;
    Random rand;
    
    boolean voteValue;
    String missionPlayers;
    
    //Game constants:
    int numMissions;
    int totalSpies;
    int requiredFailures;
    
    private static final int totalSpiesBase = 5;
    private static final int totalSpiesList[] = {2, 2, 3, 3, 3, 4}; //[numPlayer - totalSpiesBase] = number of spies in game
    
    @Override
    public void get_status(String name, String players, String spies, int mission, int failures) {
        this.name = name;
        this.playerList = players;
        this.missionNumber = mission;
        numPlayers = players.length();
        numFailures = failures;
        
        spyList = spies;
        //spyList = new ArrayList<Character>(Arrays.asList(spies.toCharArray()));
        
        resistanceList = new String[players.length() - spies.length()];
        //resistanceList = new String[players.length()];
        
        int i = 0;
        for(int j = 0; j < playerList.length(); j++)// String p : playerList)
        {
            String p = playerList.substring(j, j+1);
            
            //Check if player is spy
            if(spyList.contains(p))
                    continue;
            
            resistanceList[i++] = p;            
        }
        
        
        //Game constants:
        numMissions = 5;
        requiredFailures = 3;
        totalSpies = totalSpiesList[numPlayers-totalSpiesBase];
    }

    //Adds ourself and only resistance players:
    @Override
    public String do_Nominate(int number) {
        String s = new String();
        s = s.concat(name);
        for(int i = 0; i < number-1; i++)
        {
            String c = resistanceList[(new Random()).nextInt(resistanceList.length)];
            
            //while(s.contains(c))
              //  c = resistanceList[(new Random()).nextInt(resistanceList.length)];
            
            s = s.concat(c);
        }
        return s;
    }

    @Override
    public void get_ProposedMission(String leader, String mission) {
        missionPlayers = mission;
    }

    @Override
    public boolean do_Vote() {
        char team[] = missionPlayers.toCharArray();
        
        int numSpies = 0;
        for (char c : team) {
            if(spyList.indexOf(c) >= 0)
                    numSpies++;
        }
        
        
      //  if(numFailures == requiredFailures-1 && (numSpies > 0))
        //    return true;
        
        if(missionNumber == 5)
        {
            if(numFailures == requiredFailures-1)
                return numSpies > 0;
        }
            
            
        if(numSpies == team.length)
            return false;
        
        if(numSpies == totalSpies)
            return false;
        
        
        
        return numSpies > 0;
    }

    @Override
    public void get_Votes(String yays) {
        
    }

    @Override
    public void get_Mission(String mission) {
        missionPlayers = mission;
    }

    @Override
    public boolean do_Betray() {
        char team[] = missionPlayers.toCharArray();
        
        int numSpies = 0;
        for (char c : team) {
            if(spyList.indexOf(c) >= 0)
                    numSpies++;
        }
        
        //Sabotage only if we are only spy on mission
        if(numSpies == 1)
            return true;
        
        //ie need to sabotage every mission in order to win
      //  if(requiredFailures - numFailures > numMissions - missionNumber)
        //    return true;
            
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
