/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package src.main;
import java.util.Arrays;
import java.util.Random;



public class bayBot implements Agent {
    private char name;
    private char playerList[];
    private char team[];
    private char leader;
    int missionID;
    int numPlayers;
    int numMissions;
    Random rand;

    //Statistics
    bayStats baysian;
    double setValues[];
    
    bayBot()
    {
        
    }
    
    bayBot(double values[])
    {
        setValues = values;
    }
    
    public double[] getSuspicion()
    {
        return baysian.getSuspicion();
    }
    
    public int getPlayerIndex(char player)
    {
        for(int i = 0; i < numPlayers; i++)
            if(playerList[i] == player)
                return i;
        return 0;
    }
    
    public char indexToChar(int index)
    {
        return playerList[index];
    }
    
    @Override
    public void get_status(String name, String players, String spies, int mission, int failures) {
        this.name = name.charAt(0);
        this.playerList = players.toCharArray();
        missionID = mission-1;
        numPlayers = players.length();
        numMissions = 5; //I'm guessing
        
        rand = new Random();
        
        if(missionID == 0)
        {
            baysian = new bayStats(numPlayers, numMissions, playerList, name.charAt(0));
            
            //Check if we are setting values (from GA)
            if(setValues != null)
                baysian.setValues(setValues);
        }
        else
        {
            baysian.updateMission(missionID);
            baysian.updateSuspicion();
        }
    }

    @Override
    public String do_Nominate(int number) {
        String s = new String();
        
        for(char i : baysian.getTeam(number))
            s += i;
        
      //  System.out.println("Our " + getPlayerIndex(name) + " nomination: " + s.toString());
        return s;
    }
    
    @Override
    public void get_ProposedMission(String leader, String mission) {
        team = mission.toCharArray();
        this.leader = leader.charAt(0);
        
        //Log the nominations:
        baysian.logNominations(leader, team);
    }

    @Override
    public boolean do_Vote() {
        int group[] = new int[team.length];
        
        for (int i = 0; i < team.length; i++) {
            group[i] = getPlayerIndex(team[i]);
        }
        
        //System.out.println("Proposed team: " + Arrays.toString(group) + " from: " + leader);
            
        return baysian.containsSpy(group);
    }

    
    
    @Override
    public void get_Votes(String yays) {
        baysian.logVotes(yays);
    }

    @Override
    public void get_Mission(String mission) {
        baysian.logMission(mission, leader);
    }

    @Override
    public boolean do_Betray() {
        return false;
    }

    @Override
    public void get_Traitors(int traitors) {
        baysian.logTraitors(traitors);       
    }

    @Override
    public String do_Accuse() {
        return new String();
    }

    @Override
    public void get_Accusation(String accuser, String accused) {
    }
}