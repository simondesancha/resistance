package src.s21503324;

/*
A combination of smartSpy and expertResistanceBot, the 2 worse performing AI's
 */
public class secondBot215033241 implements Agent {

    private boolean ifInitialised;
    Agent bot;

    secondBot215033241() {
        ifInitialised = false;
    }

    @Override
    public void get_status(String name, String players, String spies, int mission, int failures) {
        //Check if we need to initialised our bots:
        if (ifInitialised == false) {
            //Check if we need a spy bot or resistance bot:
            if ((spies.contains(name))) {
                bot = new smartSpy21503324();
            } else {
                bot = new expertResistanceBot21503324();
            }
            this.ifInitialised = true;
        }

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
