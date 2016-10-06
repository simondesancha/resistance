/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;
//import System.out;

/**
 *
 * @author Owner
 */
public class testGame {
    public static void main(String[] args) {
        int fails = 0;
        for(int i = 0; i < 100; i++)
        {
            if(doGame())
                fails++;
        }
        
        System.out.printf("numFails: %d\n", fails);
    }
    
    
    static boolean doGame()
    {
        Game g = new Game();
        myBot b = new myBot();
        g.stopwatchOn();g.addPlayer(b);g.stopwatchOff(1000,'A');
        g.stopwatchOn();g.addPlayer(new RandomAgent());g.stopwatchOff(1000,'B');
        g.stopwatchOn();g.addPlayer(new RandomAgent());g.stopwatchOff(1000,'C');
        g.stopwatchOn();g.addPlayer(new RandomAgent());g.stopwatchOff(1000,'D');
        g.stopwatchOn();g.addPlayer(new RandomAgent());g.stopwatchOff(1000,'E');
        g.setup();
        
        boolean ifSpy = b.ifSpy;
        boolean spyWin = g.play();
        
        if(ifSpy != spyWin)
            System.out.println("Failed!------\n");
        else
            System.out.println("Win!\n");
        
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
        
        if(ifSpy != spyWin)
            System.out.println("Failed!------\n");
        else
            System.out.println("Win!\n");
        
        
        
        return ifSpy != spyWin;
    }
}
