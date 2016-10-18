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
/**
 *
 * @author root
 */
public class geneticAI {

    static mySelector tour;

    private static Double eval(Genotype<DoubleGene> gt) {
        int hash = gt.hashCode();
        
        
        for (int i = 0; i < 5; i++) {
            if(tour.hashToIndex[i] == hash)
                return tour.results[i];
        }

        return 0.0;
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
                        DoubleChromosome.of(0, 1),
                        DoubleChromosome.of(0, 1),
                        DoubleChromosome.of(0, 1),
                        DoubleChromosome.of(0, 1),
                        DoubleChromosome.of(0, 1));

        tour = new mySelector<>(5);

        Engine<DoubleGene, Double> engine = Engine
                .builder(geneticAI::eval, gtf)
                .offspringSelector(tour)
                .build();

        // 4.) Start the execution (evolution) and
        //     collect the result.
        Genotype<DoubleGene> result = engine.stream()
                .limit(100)
                .collect(EvolutionResult.toBestGenotype());

        System.out.println("Hello World:\n" + result);
    }

    public class mySelector<
	G extends Gene<?, G>, C extends Comparable<? super C>>
            implements Selector<G, C> {

        private final int _sampleSize;

        public double results[];
        public int hashToIndex[];

        /**
         * Create a tournament selector with the give sample size. The sample
         * size must be greater than one.
         *
         * @param sampleSize the number of individuals involved in one
         * tournament
         * @throws IllegalArgumentException if the sample size is smaller than
         * two.
         */
        public mySelector(final int sampleSize) {
            if (sampleSize < 2) {
                throw new IllegalArgumentException(
                        "Sample size must be greater than one, but was " + sampleSize
                );
            }
            _sampleSize = sampleSize;
            results = new double[sampleSize];
            hashToIndex = new int[sampleSize];
        }

        private void runTournament(Population<G, C> pop) {
            double values[][] = new double[_sampleSize][10];

            for (int i = 0; i < _sampleSize; i++) {
                Phenotype<G, C> p = pop.get(i);

                Genotype<G> g = p.getGenotype();

                for (int j = 0; j < g.getNumberOfGenes(); j++) {
                    values[i][j] = ((DoubleChromosome) g.getChromosome(j)).getGene().getAllele();
                }
                
                hashToIndex[i] = g.hashCode();
            }

            //Now to run the tournament:
            statBot[] bots = new statBot[5];
            Game g = new Game();

            for (int i = 0; i < 5; i++) {
                bots[i] = new statBot(values[i]);
                g.addPlayer(bots[i]);
            }

            g.setup();
            boolean spyWin = g.play();

            char spies[] = g.spyString.toCharArray();
            int spy[] = new int[5];
            for (char s : spies) {
                //System.out.printf("%d\n", bots[0].getPlayerIndex(s));
                spy[bots[0].getPlayerIndex(s)] = 1;
            }

            for (int i = 0; i < 5; i++) {
                for (int j = 0; j < 5; j++) {
                    results[i] += bots[i].getSuspicion()[j] * spy[j];
                }
            }
        }

        @Override
        public Population<G, C> select(
                final Population<G, C> population,
                final int count,
                final Optimize opt
        ) {
            requireNonNull(population, "Population");
            requireNonNull(opt, "Optimization");
            if (count < 0) {
                throw new IllegalArgumentException(format(
                        "Selection count must be greater or equal then zero, but was %s",
                        count
                ));
            }

            final Random random = RandomRegistry.getRandom();
            Population<G, C> pop = population.isEmpty()
                    ? new Population<>(0)
                    : new Population<G, C>(count)
                    .fill(() -> select(population, opt, _sampleSize, random), count);

            this.runTournament(pop);

            return pop;
        }

        private Phenotype<G, C> select(
                final Population<G, C> population,
                final Optimize opt,
                final int sampleSize,
                final Random random
        ) {
            final int N = population.size();
            return Stream.generate(() -> population.get(random.nextInt(N)))
                    .limit(sampleSize)
                    .collect(maxBy(opt.ascending())).get();
        }

        @Override
        public int hashCode() {
            return Hash.of(getClass()).and(_sampleSize).value();
        }

        @Override
        public boolean equals(final Object obj) {
            return Equality.of(this, obj).test(s -> _sampleSize == s._sampleSize);
        }

        @Override
        public String toString() {
            return format("%s[s=%d]", getClass().getSimpleName(), _sampleSize);
        }

    }
}
