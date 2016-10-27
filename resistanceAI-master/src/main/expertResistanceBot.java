
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
public class expertResistanceBot implements Agent {
    
    private char name;
    private String spyList;
    private char playerList[];
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

    expertResistanceBot() {
        
    }
    
    
    
 //   private static final double betrayProb[] = {0.4, 0.8, 0.8, 0.8, 0.8};
    
    @Override
    public void get_status(String name, String players, String spies, int mission, int failures) {
        this.name = name.charAt(0);
        this.playerList = players.toCharArray();
        this.missionNumber = mission;
        numPlayers = players.length();
        numFailures = failures;
        rand = new Random();
        
        
        //Game constants:
        numMissions = 5;
        requiredFailures = 3;
        totalSpies = totalSpiesList[numPlayers-totalSpiesBase];
        
            
        
        //System.out.printf("Spy: %d\n", ifSpy ? 1 : 0);
    }

    //Adds ourself and random others:
    @Override
    public String do_Nominate(int number) {
        String s = new String();
        s += name;
        for(int i = 0; i < number-1; i++)
        {
            char c = playerList[rand.nextInt(numPlayers)];
            
            while(s.indexOf(c) != -1)
                c = playerList[rand.nextInt(numPlayers)];
            
            s += c;
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
        
       if(missionPlayers.indexOf(name) < 0)
           return false;
       
       return true;        
    }

    @Override
    public void get_Votes(String yays) {
        
    }

    @Override
    public void get_Mission(String mission) {
    }

    @Override
    public boolean do_Betray() {
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
