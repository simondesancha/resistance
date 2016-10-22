/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package src.main;

/**
 *
 * @author root
 */
public class spySplitter implements Agent {
    private boolean ifInitialised;
    Agent bot;
    double values[];
    
    spySplitter()
    {
        ifInitialised = false;
    }
    
    spySplitter(double values[])
    {
        this.values = values;
        ifInitialised = false;
    }
    
    @Override
    public void get_status(String name, String players, String spies, int mission, int failures) {
        if(ifInitialised == false)
        {
            if((spies.contains(name)))
            {
               // System.out.println("bot is ---- Spy");
                bot = new expertSpyBot();
            }
            else
            {
              //  System.out.println("bot is ---- resistance");
                if(values != null)
                    bot = new bayBot(values);
                else
                    bot = new bayBot();
            }
            this.ifInitialised = true;
        }
        
        bot.get_status(name, players, spies, mission, failures);
    }
    
    public int getPlayerIndex(char player)
    {
        if(bot instanceof bayBot)
            return ((bayBot)bot).getPlayerIndex(player);
        return 0;
    }

    @Override
    public String do_Nominate(int number) {
        return bot.do_Nominate(number);
    }

    @Override
    public void get_ProposedMission(String leader, String mission) {
        bot.get_ProposedMission(leader, mission);
    }

    @Override
    public boolean do_Vote() {
        return bot.do_Vote();
    }

    @Override
    public void get_Votes(String yays) {
        bot.get_Votes(yays);
    }

    @Override
    public void get_Mission(String mission) {
        bot.get_Mission(mission);
    }

    @Override
    public boolean do_Betray() {
        return bot.do_Betray();
    }

    @Override
    public void get_Traitors(int traitors) {
        bot.get_Traitors(traitors);
    }

    @Override
    public String do_Accuse() {
        return bot.do_Accuse();
    }

    @Override
    public void get_Accusation(String accuser, String accused) {
        bot.get_Accusation(accuser, accused);
    }
    
}
