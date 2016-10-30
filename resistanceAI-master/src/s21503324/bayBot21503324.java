/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package src.s21503324;


/*
A resistance bot using Bayesian updating to hold suspicion values for each player/player group
*/
public class bayBot21503324 implements Agent {
    private char team[];
    private char leader;
    int votingRound;

    //Bayesian updating class:
    bayStats21503324 baysian;
    
    //Optimal probability weighting determined by Genetic algorithm:
    private static final double DEFAULT_VALUES[] = {0.94, 0.93, 0.99, 0.91, 0.62, 1.00};
    double setValues[];
    
    bayBot21503324()
    {
        setValues = DEFAULT_VALUES;
    }
    
    //If we need to specify different wieghtings (ie for doing GA)
    bayBot21503324(double values[])
    {
        setValues = values;
    }
    
    @Override
    public void get_status(String name, String players, String spies, int mission, int failures) 
    {
        votingRound = 0;
        
        if(mission == 1)
        {
            //Initialise our bayStats class on the first round.
            baysian = new bayStats21503324(players.length(), players.toCharArray(), name.charAt(0), setValues);
        }
        else
        {
            baysian.updateMission(mission-1);

            //Had some problems when using bayBot in the Monte-Carlo search tree,
            //Fixed the issue but leaving the try-catch just to be safe
            try
            {
                baysian.updateSuspicion();
            }
            catch(java.lang.ArrayIndexOutOfBoundsException e)
            {
                return;
            }
        }
    }

    @Override
    public String do_Nominate(int number) {
        String s = new String();
        
        for(char i : baysian.getTeam(number))
            s += i;
        
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
        //Make sure to pass mission on 5th round
        votingRound++;
        if(votingRound == 5)
            return true;
        
        return !baysian.containsSpy(team);
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
        votingRound = 0;
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