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
    
    private static final double values[] = {0.06, 0.15, 0.99, 0.93, 0.97, 0.18, 0.50};
    
    public static void main(String[] args) {
        GA();
        //tournaments();
    }
    
    static void tournaments()
    {
        int spyWins = 0;
        for (int i = 0; i < 100; i++) {
            spyWins += doGame() ? 1 : 0;
        }
        
        System.out.println("Spy wins: " + spyWins);
    }
    
    static void GA()
    {
        simonGA GA = new simonGA();
        GA.runGA();
    }
    
    static boolean doGame()
    {
        Game g = new Game();
        spySplitter b = new spySplitter();
        spySplitter r[] = new spySplitter[4];
        for (int i = 0; i < 4; i++) {
            r[i] = new spySplitter(values);
        }
        g.stopwatchOn();g.addPlayer(b);g.stopwatchOff(1000,'A');
        g.stopwatchOn();g.addPlayer(r[0]);g.stopwatchOff(1000,'B');
        g.stopwatchOn();g.addPlayer(r[1]);g.stopwatchOff(1000,'C');
        g.stopwatchOn();g.addPlayer(r[2]);g.stopwatchOff(1000,'D');
        g.stopwatchOn();g.addPlayer(r[3]);g.stopwatchOff(1000,'E');
        g.setup();
        
      //  System.out.println("Spys:" + g.spyString);
        char spies[] = g.spyString.toCharArray();
        
        for(char s : spies)
        {
          //  System.out.printf("%d\n", b.getPlayerIndex(s));
        }
        
//        for (int i = 0; i < 4; i++) {
  //          System.out.printf("%d\n", r[i].spy ? 1 : 0);
    //    }
        
        boolean spyWin = g.play();
        
    /*    if(spyWin)
            System.out.println("Spy wins!------\n");
        else
            System.out.println("resistance Wins!\n");
      */  
        return spyWin;
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
