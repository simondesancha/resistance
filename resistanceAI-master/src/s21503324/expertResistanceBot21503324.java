
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package src.s21503324;
import java.util.Random;



/**
 *
 * 
 */
public class expertResistanceBot21503324 implements Agent {
    private char name;
    private char playerList[];
    int numPlayers;
    
    Random rand;
    
    String missionPlayers;
    
    @Override
    public void get_status(String name, String players, String spies, int mission, int failures) {
        this.name = name.charAt(0);
        this.playerList = players.toCharArray();
        numPlayers = players.length();
        rand = new Random();
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
