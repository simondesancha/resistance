package src.s21503324;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
Genetic Algorithm used to find the optimal probability weightings to use in bayBot
 */
public class GeneticAlgorithm21503324 {
    Random rand;
    
    //GA properties:
    private static final int INITIAL_POPULATION = 100;
    private static final double MUTATION_RATE = 0.05;
    private static final int NUM_GENERATIONS = 100;
    private static final int WINNERS_PER_GENERATION = 20;
    private static final int TOURNAMENT_SIZE = 3;
    
    private static final int TOURNAMENT_LENGTH = 100;
    
    private static final double DEFAULT_GENES[] = {0.4, 0.8, 0.8, 0.8, 0.8, 0.6, 0.5};
    
    //Individual:
    private class Individual {
        private static final int NUMBER_GENES = 6;
        double genes[];
        bayBot21503324 bot;
        
        Individual()
        {
            genes = new double[NUMBER_GENES];
            
            rand = new Random();
            
            for (int i = 0; i < NUMBER_GENES; i++) {
                genes[i] = rand.nextDouble();
            }
            
            bot = new bayBot21503324(genes);
        }
        
        Individual(double values[])
        {
            genes = values;
            
            bot = new bayBot21503324(genes);
        }
        
        bayBot21503324 getBot()
        {
            return bot;
        }
        
    }
    
    Individual population[];
    int tournaments[][]; //[tournament ID][num Players] = individual ID
    List<Individual> survivors;
    double maxScore;
    Individual maxGene;
    
    double overAllMax = 0;
    int maxID = 0;
    int overAllGene = 0;
    
    
    public double testEval(double values[])
    {
        rand = new Random();
        double result = 0;
        for (int j = 0; j < TOURNAMENT_LENGTH; j++) {
            
            Agent bots[] = new Agent[3];
            
            for (int i = 0; i < 3; i++) {
                bots[i] = new bayBot21503324(values);
            }
            
            result += getResult(bots) ? 1 : 0;
        }
        
        return result/TOURNAMENT_LENGTH;
    }
    
    void runGA()
    {
        System.out.println("Initialising...");
        rand = new Random();
        createInitialPop();
        
        for (int i = 0; true || i < NUM_GENERATIONS; i++) {
            System.out.println("Running generation #" + i);
            
            maxScore = 0;
            getSurvivors();
            //maxGene = survivors.get(0);
            System.out.printf("Max score: %.2f, Genes: %.2f, %.2f, %.2f, %.2f, %.2f, %.2f,\n", maxScore, maxGene.genes[0], maxGene.genes[1], maxGene.genes[2], maxGene.genes[3], maxGene.genes[4], maxGene.genes[5]);
            
            if(maxScore > overAllMax)
            {
                overAllMax = maxScore;
                overAllGene = maxID;
            }
            System.out.println("");
            System.out.println("");
            System.out.printf("Overall max: %.2f\n", overAllMax);
            System.out.printf("Redo: %.2f\n", getFitness_MultiplePlays(overAllGene));
            System.out.println("");
            System.out.println("");
            
            
            
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
        for (int i = WINNERS_PER_GENERATION; i < INITIAL_POPULATION; i++) {
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
        
        if(survivors != null)
            survivors.clear();
        survivors = new ArrayList<>();
        
        while (survivors.size() < WINNERS_PER_GENERATION) {
            int ID[] = new int[TOURNAMENT_SIZE];
            
            //Randomly pick 3 players:
            for (int j = 0; j < TOURNAMENT_SIZE; j++) 
                ID[j] = rand.nextInt(INITIAL_POPULATION);
            
            //Get the winner from the 3:
            runTournament(ID);
        }
    }
    
    void runTournament(int ID[])
    {
        double maxResult = 0;
        int maxIndex = 0;
        for (int i = 0; i < TOURNAMENT_SIZE; i++) {
            double fitness = getFitness_MultiplePlays(ID[i]);
            
            if(fitness > maxResult)   
            {
                maxResult = fitness;
                maxIndex = i;
            }
        }
        survivors.add(population[ID[maxIndex]]);
        
        
        /*double maxResult = getFitness_MultiplePlays_Team(ID);
        int maxIndex = 0;
        
        if(maxResult > maxScore - 0.10)
            for (int i = 0; i < 3; i++) {
                survivors.add(population[ID[i]]);
        }*/
        
        if(maxResult > maxScore)
        {
            maxScore = maxResult;
            maxGene = population[ID[maxIndex]];
            maxID = ID[maxIndex];
        }
       
        
    }
    
    double getFitness_MultiplePlays(int ID)
    {
        double result = 0;
        for (int j = 0; j < TOURNAMENT_LENGTH; j++) {
            
            Agent bots[] = new Agent[TOURNAMENT_SIZE];
            
            for (int i = 0; i < TOURNAMENT_SIZE; i++) {
                bots[i] = new bayBot21503324(population[ID].genes);
            }
            
            result += getResult(bots) ? 1 : 0;
        }
        
        return result/TOURNAMENT_LENGTH;
    }
    
    double getFitness_MultiplePlays_Team(int ID[])
    {
        double result = 0;
        for (int j = 0; j < TOURNAMENT_LENGTH; j++) {
            
            Agent bots[] = new Agent[TOURNAMENT_SIZE];
            
            for (int i = 0; i < TOURNAMENT_SIZE; i++) {
                bots[i] = new bayBot21503324(population[ID[i]].genes);
            }
            
            result += getResult(bots) ? 1 : 0;
        }
        
        return result/TOURNAMENT_LENGTH;
    }
    
    boolean getResult(Agent resistancePlayers[])
    {
        Agent agents[] = new Agent[TOURNAMENT_SIZE+2];
        
        agents[0] = new expertSpyBot21503324();
        agents[1] = new expertSpyBot21503324();
        
        for (int i = 0; i < TOURNAMENT_SIZE; i++) {
            agents[2+i] = resistancePlayers[i];
        }
        
        superGame21503324 game = randomlyAllocate(agents);
        
        boolean spyWin = game.play();
        
        return !spyWin;
    }
    
    superGame21503324 randomlyAllocate(Agent agents[])
    {
        superGame21503324 game = new superGame21503324();
        
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
       /*game.addSpy(agents[0]);
       game.addSpy(agents[1]);
       game.addResistance(agents[2]);
       game.addResistance(agents[3]);
       game.addResistance(agents[4]);*/
       
       
        game.setupWithPresets();
        
        return game;
    }
    
    //Return the ID of a survivor
    void playEachOther(int indexes[])
    {
        Agent agents[] = new Agent[TOURNAMENT_SIZE+2];
        
        agents[0] = new expertSpyBot21503324();
        agents[1] = new expertSpyBot21503324();
        
        //bayBot bots[] = new bayBot[3];
        for (int i = 0; i < TOURNAMENT_SIZE; i++) {
            agents[2+i] = population[indexes[i]].getBot();
        }
        
        superGame21503324 game = randomlyAllocate(agents);
        
        boolean spyWin = game.play();
        
        if(!spyWin)
        {
            //add players to survivors
            for (int i = 0; i < TOURNAMENT_SIZE; i++) {
                survivors.add(population[indexes[i]]);
            }
        }
        
        
        if(spyWin)
            return;
    }
        
    /*    //Now check the bots suspicions:
        //Assuming spies are always A, B
        char spiesList[] = game.spyString.toCharArray();
        
        int spies[] = new int[5];
        Arrays.fill(spies, -100);
        
        for(char c : spiesList)
        {
            spies[c-65] = 1;
        }
        
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
       
        survivors.add(population[indexes[maxIndex]]);
    }*/
    
    
    
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
