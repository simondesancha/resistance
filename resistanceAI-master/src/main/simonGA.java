/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package src.main;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 * @author root
 */
public class simonGA {
    
    //GA properties:
    private static final int INITIAL_POPULATION = 500;
    private static final double MUTATION_RATE = 0.02;
    private static final int NUM_GENERATIONS = 250;
    private static final int WINNERS_PER_GENERATION = 20;
    private static final int TOURNAMENT_SIZE = 3;
    
    
    //Individual:
    private class Individual {
        private static final int NUMBER_GENES = 7;
        
        double genes[];
        bayBot bot;
        Random rand;
        
        Individual()
        {
            genes = new double[NUMBER_GENES];
            
            rand = new Random();
            
            for (int i = 0; i < NUMBER_GENES; i++) {
                genes[i] = rand.nextDouble();
            }
            
            bot = new bayBot(genes);
        }
        
        Individual(double values[])
        {
            genes = values;
            
            bot = new bayBot(genes);
        }
        
        bayBot getBot()
        {
            return bot;
        }
        
    }
    
    Individual population[];
    int tournaments[][]; //[tournament ID][num Players] = individual ID
    List<Individual> survivors;
    double maxScore;
    Individual maxGene;
    
    void runGA()
    {
        System.out.println("Initialising...");
        createInitialPop();
        
        for (int i = 0; i < NUM_GENERATIONS; i++) {
            System.out.println("Running generation #" + i);
            
            maxScore = 0;
            getSurvivors();
            System.out.printf("Max score: %.1f, Genes: %.2f, %.2f, %.2f, %.2f, %.2f, %.2f, %.2f\n", maxScore, maxGene.genes[0], maxGene.genes[1], maxGene.genes[2], maxGene.genes[3], maxGene.genes[4], maxGene.genes[5], maxGene.genes[6]);
            
            
            createNextGeneration();
            injectDiversity();
        }
    }
    
    void createInitialPop()
    {
        //Create initial random population:
        population = new Individual[INITIAL_POPULATION];
        
        for (int i = 0; i < INITIAL_POPULATION; i++) {
            //Randomly generate genes
            population[i] = new Individual(); 
        }
    }
    
    void createNextGeneration()
    {
        population = new Individual[INITIAL_POPULATION];
        
        //Fill first 20 with last gen's survivors:
        for (int i = 0; i < WINNERS_PER_GENERATION; i++)
            population[i] = survivors.get(i);
        
        Random rand = new Random();
        for(int i = WINNERS_PER_GENERATION; i < INITIAL_POPULATION; i++)
        {
            //Randomly pick 2 parents:
            Individual father = survivors.get(rand.nextInt(WINNERS_PER_GENERATION));
            Individual mother = survivors.get(rand.nextInt(WINNERS_PER_GENERATION));
            double child[] = new double[Individual.NUMBER_GENES];
            
            
            //Single point crossover:
            int crossPoint = rand.nextInt(Individual.NUMBER_GENES);
            for (int j = 0; j < Individual.NUMBER_GENES; j++) {
                if(j < crossPoint)
                    child[j] = father.genes[j];
                else
                    child[j] = mother.genes[j];
            }
            
            population[i] = new Individual(child);
        }
    }
    
    void injectDiversity()
    {
        Random rand = new Random();
        for (int i = 0; i < INITIAL_POPULATION; i++) {
            for (int j = 0; j < Individual.NUMBER_GENES; j++) {
                if(rand.nextDouble() < MUTATION_RATE)
                {
                    //Mutate this value:
                    population[i].genes[j] = rand.nextDouble();
                }
            }
        }
    }
    
    void getSurvivors()
    {
        Random rand = new Random();
        
        if(survivors != null)
            survivors.clear();
        survivors = new ArrayList<>();
        
        for (int i = 0; i < WINNERS_PER_GENERATION; i++) {
            int ID[] = new int[TOURNAMENT_SIZE];
            
            //Randomly pick 3 players:
            for (int j = 0; j < TOURNAMENT_SIZE; j++) 
                ID[j] = rand.nextInt(INITIAL_POPULATION);
            
            //Get the winner from the 3:
            int winner = runTournament(ID);
            survivors.add(population[winner]);
        }
    }
    
    
    
    //Return the ID of a survivor
    int runTournament(int indexes[])
    {
        expertSpyBot spy1 = new expertSpyBot();
        expertSpyBot spy2 = new expertSpyBot();
        
        bayBot bots[] = new bayBot[TOURNAMENT_SIZE];
        for (int i = 0; i < TOURNAMENT_SIZE; i++) {
            bots[i] = population[indexes[i]].getBot();
        }
        
        Game game = new Game();
        
        game.addSpy(spy1);
        game.addSpy(spy2);
        
        for (int i = 0; i < TOURNAMENT_SIZE; i++) {
            game.addResistance(bots[i]);
        }
        
        game.setupWithPresets();
        
        game.play();
        
        
        //Now check the bots suspicions:
        //Assuming spies are always A, B
        int spies[] = {1, 1, -1, -1, -1};
        
        double scores[] = new double[TOURNAMENT_SIZE];
        for (int i = 0; i < TOURNAMENT_SIZE; i++) {
            double values[] = bots[i].getSuspicion();
            for (int j = 0; j < 5; j++) {
                scores[i] += values[j] * spies[j];
            }
            //System.out.println(values[0]);
        }
        
        //Now pick highest score:
        double maxValue = 0;
        int maxIndex = 0;
        for (int i = 0; i < TOURNAMENT_SIZE; i++) {
            if(scores[i] > maxValue)
            {
                maxIndex = i;
                maxValue = scores[i];
            }
        }
        
        if(maxValue > maxScore)
        {
            maxScore = maxValue;
            maxGene = population[indexes[maxIndex]];
        }
        
        return indexes[maxIndex];
    }
    
    
    
    /*
    
    void getTournaments()
    {
        //Fill IDarray with all individual ID's
        List<Integer> IDarray = new ArrayList<>();
        for (int i = 0; i < INITIAL_POPULATION; i++) {
            IDarray.add(i);
        }
        
        //Make 20 tournaments:
        Random rand = new Random();
        for (int i = 0; i < population.length/TOURNAMENT_SIZE; i++) {
            
            //Fill each tournament with a randomly selected set of ID's
            for (int j = 0; j < TOURNAMENT_SIZE; j++) {
                int ID = rand.nextInt(IDarray.size());
                
                tournaments[i][j] = IDarray.remove(ID);
            }
        }
    }*/
}
