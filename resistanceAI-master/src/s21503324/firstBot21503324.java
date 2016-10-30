/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package src.s21503324;

/*
A combination of bayBot and expertSpyBot, the best performing resistance and spy bot.
 */
public class firstBot21503324 implements Agent {

    private boolean ifInitialised;
    Agent bot;

    firstBot21503324() {
        ifInitialised = false;
    }

    @Override
    public void get_status(String name, String players, String spies, int mission, int failures) {
        //Check if we need to initialised our bots:
        if (ifInitialised == false) {
            //Check if we need a spy bot or resistance bot:
            if ((spies.contains(name))) {
                bot = new expertSpyBot21503324();
            } else {
                bot = new bayBot21503324();
            }
            this.ifInitialised = true;
        }
        
        //Pass onto the correct bot
        bot.get_status(name, players, spies, mission, failures);
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
