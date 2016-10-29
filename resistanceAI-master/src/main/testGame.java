/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package src.main;
//import System.out;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/**
 *
 * @author Owner
 */
public class testGame {
    
    private static final double values[] = {0.95, 0.91, 0.48, 0.69, 0.64, 0.93};//{0.33, 0.25, 0.39, 0.41, 0.66, 0.42, 0.00};
    
    public static void main(String[] args) {
        //GA();
        //GA2();
        tournaments();
        //spy();
    }
    
    static void GA2()
    {
        genetics2 ga = new genetics2();
        ga.go();
    }
    
    static boolean spy()
    {
        Agent agents[] = new Agent[5];
        
        //agents[0] = new smartSpy();
        agents[0] = new expertSpyBot();
        agents[1] = new expertSpyBot();
        agents[2] = new bayBot(values);
        agents[3] = new bayBot(values);
        agents[4] = new bayBot(values);
        
        Random rand = new Random();
        Game game = new Game();
        
        List<Integer> done = new ArrayList<>();
        for (int i = 0; i < agents.length; i++) {
           int id = rand.nextInt(5);
           
           while(done.contains(id))
                id = rand.nextInt(5);
           
           done.add(id);
           if(id < 2)
           {
               game.addSpy(agents[id]);
           }
           else
           {
               game.addResistance(agents[id]);
           }
        }
        game.setupWithPresets();
                
        boolean spyWin = game.play();
        
        //System.out.println(spyWin ? "Spy win " : "resistance win");
        return spyWin;
    }
    
    static void tournaments()
    {
        int spyWins = 0;
        for (int i = 0; i < 1; i++) {
            spyWins += play() ? 1 : 0;
        }
        
        System.out.println("Spy wins: " + (double)spyWins);
    }
    
    static void GA()
    {
        simonGA GA = new simonGA();
        GA.runGA();
    }
    
    Game randomlyAllocate(Agent agents[])
    {
        Random rand = new Random();
        Game game = new Game();
        
        List<Integer> done = new ArrayList<>();
        for (int i = 0; i < agents.length; i++) {
           int id = rand.nextInt(5);
           
           while(done.contains(id))
                id = rand.nextInt(5);
           
           done.add(id);
           if(id < 2)
           {
               game.addSpy(agents[i]);
               System.out.println("Spy: " + i + " is " + ((char)(65 + i)));
           }
           else
           {
               game.addResistance(agents[i]);
           }
        }
        game.setupWithPresets();
        
        return game;
    }
    
    static boolean play()
    {
        //Randomly allocate bots:
        Agent agents[] = new Agent[5];
        
        
        agents[0] = new expertSpyBot();
        agents[1] = new smartSpy();
        
        //bayBot bots[] = new bayBot[3];
        for (int i = 0; i < 3; i++) {
            agents[2+i] = new bayBot(values);
        }
        
        Game game = new Game();
        
        Random rand = new Random();
        List<Integer> done = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
           int id = rand.nextInt(5);
           
           while(done.contains(id))
                id = rand.nextInt(5);
           
           done.add(id);
           if(id < 2)
           {
               game.addSpy(agents[id]);
           }
           else
           {
               game.addResistance(agents[id]);
           }
        }
        
        /*game.addSpy(spy1);
        game.addSpy(spy2);
        
        for (int i = 0; i < 3; i++) {
            game.addResistance(bots[i]);
        }*/
        
        game.setupWithPresets();
        
        return game.play();
    }
    
    static boolean play2()
    {
        //Randomly allocate bots:
        Agent agents[] = new Agent[5];
        
        
        agents[0] = new expertSpyBot();
        agents[1] = new expertSpyBot();
        
        //bayBot bots[] = new bayBot[3];
        for (int i = 0; i < 3; i++) {
            agents[2+i] = new bayBot(values);
        }
        
        Game game = new Game();
        
        Random rand = new Random();
        List<Integer> done = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
           int id = rand.nextInt(5);
           
           while(done.contains(id))
                id = rand.nextInt(5);
           
           done.add(id);
           if(id < 2)
           {
               game.addSpy(agents[id]);
           }
           else
           {
               game.addResistance(agents[id]);
           }
        }
        
        game.setupWithPresets();
        
        return game.play();
    }
}
