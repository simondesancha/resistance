/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package src.main;
//import System.out;

/**
 *
 * @author Owner
 */
public class testGame {
    public static void main(String[] args) {
        int fails = 0;
        for(int i = 0; i < 1; i++)
        {
            if(doGame())
                fails++;
        }
        
        System.out.printf("numFails: %d\n", fails);
    }
    
    
    static boolean doGame()
    {
        Game g = new Game();
        statBot b = new statBot();
        Agent r[] = new myBot[4];
        for (int i = 0; i < 4; i++) {
            r[i] = new myBot();
        }
        g.stopwatchOn();g.addPlayer(b);g.stopwatchOff(1000,'A');
        g.stopwatchOn();g.addPlayer(new RandomAgent());g.stopwatchOff(1000,'B');
        g.stopwatchOn();g.addPlayer(new RandomAgent());g.stopwatchOff(1000,'C');
        g.stopwatchOn();g.addPlayer(new RandomAgent());g.stopwatchOff(1000,'D');
        g.stopwatchOn();g.addPlayer(new RandomAgent());g.stopwatchOff(1000,'E');
        g.setup();
        
        System.out.println("Spys:" + g.spyString);
        char spies[] = g.spyString.toCharArray();
        
        for(char s : spies)
        {
            System.out.printf("%d\n", b.getPlayerIndex(s));
        }
        
//        for (int i = 0; i < 4; i++) {
  //          System.out.printf("%d\n", r[i].spy ? 1 : 0);
    //    }
        
        boolean ifSpy = b.ifSpy;
        boolean spyWin = g.play();
        
        if(spyWin)
            System.out.println("Spy wins!------\n");
        else
            System.out.println("resistance Wins!\n");
        
        
        
        
        return ifSpy != spyWin;
    }
    
    static boolean doRandom()
    {
        Game g = new Game();
        RandomAgent b = new RandomAgent();
        
        
        
        g.stopwatchOn();g.addPlayer(b);g.stopwatchOff(1000,'A');
        g.stopwatchOn();g.addPlayer(new RandomAgent());g.stopwatchOff(1000,'B');
        g.stopwatchOn();g.addPlayer(new RandomAgent());g.stopwatchOff(1000,'C');
        g.stopwatchOn();g.addPlayer(new RandomAgent());g.stopwatchOff(1000,'D');
        g.stopwatchOn();g.addPlayer(new RandomAgent());g.stopwatchOff(1000,'E');
        g.setup();
        
        boolean ifSpy = b.spy;
        boolean spyWin = g.play();
        
        if(spyWin)
            System.out.println("Spy wins!------\n");
        else
            System.out.println("resistance Wins!\n");
        
        System.out.println("Spys:");
        
        for (int i = 0; i < 5; i++) {
            System.out.printf("%d");
        }
        
        
        return ifSpy != spyWin;
    }
}
