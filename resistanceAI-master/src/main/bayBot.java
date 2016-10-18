/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package src.main;
import java.util.Random;



public class bayBot implements Agent {
    boolean ifSpy;
    private char name;
    private char spyList[];
    private char playerList[];
    private char team[];
    int missionNumber;
    int missionID;
    int numPlayers;
    int numMissions;
    Random rand;

    boolean voteValue;
    int numPlayersOnMission;

    bayStats baysian;
    
    bayBot()
    {
        
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
        this.missionNumber = mission;
        missionID = mission-1;
        numPlayers = players.length();
        numMissions = 5; //I'm guessing
        ifSpy = (spies.contains(name));
        spyList = spies.toCharArray();
        
        rand = new Random();
        
        if(missionID == 0)
        {
            baysian = new bayStats(numPlayers, numMissions, playerList);
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
        s += name;
        
        for(char i : baysian.getTeam(number - 1))
        {
            s += i;
        }
        
        System.out.println("Our nomination: " + s.toString());
        return s;
    }
    
    @Override
    public void get_ProposedMission(String leader, String mission) {
        team = mission.toCharArray();
        
        //Log the nominations:
        baysian.logNominations(leader, team);
    }

    @Override
    public boolean do_Vote() {
        int group[] = new int[team.length];
        
        for (int i = 0; i < team.length; i++) {
            group[i] = getPlayerIndex(team[i]);
        }
            
        return baysian.containsSpy(group);
    }

    
    
    @Override
    public void get_Votes(String yays) {
        baysian.logVotes(yays);
    }

    @Override
    public void get_Mission(String mission) {
        baysian.logMission(mission);
    }

    @Override
    public boolean do_Betray() {
        return true;
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