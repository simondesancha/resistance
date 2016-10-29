/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package src.main;

import org.jenetics.*;
import org.jenetics.engine.*;
import org.jenetics.util.*;
import org.jenetics.Phenotype;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.maxBy;

import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.jenetics.internal.util.Equality;
import org.jenetics.internal.util.Hash;

import org.jenetics.util.RandomRegistry;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.lang.String.format;
import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author root
 */
public class genetics2 {
    
    private static final int TOURNAMENT_LENGTH = 100;
    static int counter = 0;
    static double max = 0;
    
    private static Double eval(Genotype<DoubleGene> gt) {
        int length = gt.getNumberOfGenes();
        double values[] = new double[length];
        for (int i = 0; i < length; i++) {
                values[i] = ((DoubleChromosome)gt.getChromosome(i)).getGene().doubleValue();
        }
        
        simonGA ga = new simonGA();
        double result = ga.testEval(values);
    
        if(result > max)
            max = result;
        
        counter++;
        if(counter == 100)
        {
            System.out.println("Result: " + result);
            System.out.println("Max: " + max);
            counter = 0;
            max = 0;
        }
        
        return result;
    }
    
    public void go() {
        // 1.) Define the genotype (factory) suitable
        //     for the problem.
        Factory<Genotype<DoubleGene>> gtf
                = Genotype.of(DoubleChromosome.of(0, 1),
                        DoubleChromosome.of(0, 1),
                        DoubleChromosome.of(0, 1),
                        DoubleChromosome.of(0, 1),
                        DoubleChromosome.of(0, 1),
                        DoubleChromosome.of(0, 1)
                 /*       DoubleChromosome.of(0, 1),
                        DoubleChromosome.of(0, 1),
                        DoubleChromosome.of(0, 1),
                        DoubleChromosome.of(0, 1)*/);

        TournamentSelector tour = new TournamentSelector<>(3);

        Engine<DoubleGene, Double> engine = Engine
                .builder(genetics2::eval, gtf)
                //.offspringSelector(tour)
                .build();

        // 4.) Start the execution (evolution) and
        //     collect the result.
        Genotype<DoubleGene> result = engine.stream()
                .limit(10000)
                .collect(EvolutionResult.toBestGenotype());

        System.out.println("Hello World:\n" + result);
    }
}