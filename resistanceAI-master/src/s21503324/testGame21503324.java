/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package src.s21503324;
//import System.out;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/**
A testing class used to gauge performance of the bots
 */
public class testGame21503324 {
    private static final int TotalSpiesBase = 5;
    private static final int TotalSpiesList[] = {2, 2, 3, 3, 3, 4}; 
    
    
    public static void main(String[] args) {
        //GA();
        tournaments();
    }
    
    
    static void tournaments()
    {
        int spyWins = 0;
        for (int i = 0; i < 10000; i++) {
            boolean result = play();
            
            spyWins += result ? 1 : 0;
        }
        
        System.out.println("Spy wins: " + (double)spyWins/10000);
    }
    
    static void GA()
    {
        GeneticAlgorithm21503324 GA = new GeneticAlgorithm21503324();
        GA.runGA();
    }
    
    static boolean play()
    {
        Random rand = new Random();
        
        //int n = rand.nextInt(6)+5;
        int n = 5;
        int s = TotalSpiesList[n - TotalSpiesBase];
        
        //Randomly allocate bots:
        Agent agents[] = new Agent[n];
        
        for (int i = 0; i < s; i++) 
            agents[i] = new expertSpyBot21503324();
            //agents[i] = new smartSpy21503324();
        
        for (int i = 0; i < n-s; i++) {
            agents[s+i] = new bayBot21503324();
        }
        
        superGame21503324 game = new superGame21503324();
        
        //Randomly allocate our bots positions:
        List<Integer> done = new ArrayList<>();
        for (int i = 0; i < n; i++) {
           int id = rand.nextInt(n);
           
           while(done.contains(id))
                id = rand.nextInt(n);
           
           done.add(id);
           if(id < s)
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
