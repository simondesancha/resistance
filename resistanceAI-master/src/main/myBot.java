/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package src.main;
import java.util.Random;



/**
 *
 * Fails ~20 - %30 when versing random agent
 */
public class myBot implements Agent {
    
    boolean ifSpy;
    private char name;
    private char spyList[];
    private char playerList[];
    int missionNumber;
    int numPlayers;
    Random rand;
    
    boolean voteValue;
    
    @Override
    public void get_status(String name, String players, String spies, int mission, int failures) {
        this.name = name.charAt(0);
        this.playerList = players.toCharArray();
        this.missionNumber = mission;
        numPlayers = players.length();
        ifSpy = (spies.contains(name));
        spyList = spies.toCharArray();
        
        rand = new Random();
        
        System.out.printf("Spy: %d\n", ifSpy ? 1 : 0);
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
        
    }

    @Override
    public void get_Mission(String mission) {
    }

    @Override
    public boolean do_Betray() {
        return true;
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
