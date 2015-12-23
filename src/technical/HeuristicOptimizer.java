package technical;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.genetics.Chromosome;
import org.apache.commons.math3.genetics.ChromosomePair;
import org.apache.commons.math3.genetics.CrossoverPolicy;
import org.apache.commons.math3.genetics.ElitisticListPopulation;
import org.apache.commons.math3.genetics.FixedGenerationCount;
import org.apache.commons.math3.genetics.GeneticAlgorithm;
import org.apache.commons.math3.genetics.MutationPolicy;
import org.apache.commons.math3.genetics.Population;
import org.apache.commons.math3.genetics.StoppingCondition;
import org.apache.commons.math3.genetics.TournamentSelection;
import domain.Fingering;

/**
 * This class optimizes the hueristic function weights
 * based on the provided chords
 * @author Nicholas
 *
 */
public class HeuristicOptimizer 
{
	private List<Chromosome> pop;
	// parameters for the GA
	private static final int NUM_GENERATIONS = 1000;
	private static final double ELITISM_RATE = 0.4;
	private static final double CROSSOVER_RATE = 0.4;
	private static final double MUTATION_RATE = 0.2;
	private static final int TOURNAMENT_ARITY = 8;
	
	/**
	 * A chromosome representing weights for the 
	 * heuristic function
	 * @author Nicholas
	 *
	 */
	private class WeightChrome extends Chromosome
	{
		public Fingering[] min;
		public Fingering[] max;
		public double[] w;
		
		@Override
		public double fitness() 
		{
			double minSum = 0.0, maxSum = 0.0;
			for(Fingering f : min)
				minSum += f.GetScore();
			minSum /= min.length;
			for(Fingering f : max)
				maxSum += f.GetScore();
			maxSum /= max.length;
			return maxSum - minSum;
		}
		
		/**
		 * Assigned a random set of weights
		 * for a weighted average
		 * @param n The number of values
		 * @return A random vector of n values that
		 * sums to 1.0
		 */
		double[] RandAvgWeight(int n)
		{
			Random r = new Random(System.currentTimeMillis());
			double[] ws = new double[n];
			double s = 0.0;
			for(int i = 0; i < n; ++i)
			{
				ws[i] = r.nextDouble();
				s += ws[i];
			}
			for(int i = 0; i < n; ++i)
				ws[i] /= s;
			return ws;
		}
		
		public void Update()
		{
			for(Fingering f : min)
			{
				f.SetW(w);
				f.UpdateScore();
			}
			for(Fingering f : max)
			{
				f.SetW(w);
				f.UpdateScore();
			}
		}
		
		public WeightChrome(List<Fingering> minimize, List<Fingering> maximize)
		{
			w = RandAvgWeight(5);
			min = new Fingering[minimize.size()];
			for(int i = 0; i < min.length; ++i)
			{
				min[i] = minimize.get(i).Clone();
				min[i].SetW(w);
				min[i].UpdateScore();
			}
			max = new Fingering[maximize.size()];
			for(int i = 0; i < max.length; ++i)
			{
				max[i] = maximize.get(i).Clone();
				max[i].SetW(w);
				max[i].UpdateScore();
			}
		}
		
		public WeightChrome(WeightChrome wc)
		{
			w = wc.w.clone();
			min = new Fingering[wc.min.length];
			for(int i = 0; i < min.length; ++i)
			{
				min[i] = wc.min[i].Clone();
				min[i].SetW(w);
				min[i].UpdateScore();
			}
			max = new Fingering[wc.max.length];
			for(int i = 0; i < max.length; ++i)
			{
				max[i] = wc.max[i].Clone();
				max[i].SetW(w);
				max[i].UpdateScore();
			}
		}
		
	}
	
	/**
	 * This class describes a crossover policy for the 
	 * heuristic function weights
	 * @author Nicholas
	 *
	 */
	private class WeightXvr implements CrossoverPolicy
	{	
		@Override
		public ChromosomePair crossover(Chromosome a0, Chromosome a1) throws MathIllegalArgumentException 
		{
			WeightChrome c1 = new WeightChrome((WeightChrome) a0);
			WeightChrome c2 = new WeightChrome((WeightChrome) a1);
			Random r = new Random(System.currentTimeMillis());
			double s1 = 0.0, s2 = 0.0, tmp;
			int n = c1.w.length, i = 0;
			//Pick the cross-over point
			int sp = r.nextInt(n);
			for( ; i < sp; ++i)
			{
				tmp = c1.w[i];
				c1.w[i] = c2.w[i];
				c2.w[i] = tmp;
				s1 += c1.w[i];
				s2 += c2.w[i];
			}
			for( ; i < n; ++i)
			{
				tmp = c2.w[i];
				c2.w[i] = c1.w[i];
				c1.w[i] = tmp;
				s1 += c1.w[i];
				s2 += c2.w[i];
			}
			//Normalize the vectors
			for(i = 0; i < c1.w.length; ++i)
			{
				if(c1.w[i] < 0.0)
					c1.w[i] = 0.0;
				else
					c1.w[i] /= s1;
				if(c2.w[i] < 0.0)
					c2.w[i] = 0.0;
				else
					c2.w[i] /= s2;
			}
			c1.Update();
			c2.Update();
			return new ChromosomePair(c1, c2);
		}
	}
	
	/**
	 * This class represents a mutation policy for
	 * the weight chromosomes. The weight vector is
	 * perturbed slightly
	 * @author Nicholas
	 *
	 */
	private class WeightMut implements MutationPolicy
	{
		@Override
		public Chromosome mutate(Chromosome a0) throws MathIllegalArgumentException {
			WeightChrome mut = new WeightChrome((WeightChrome) a0);
			double s = 0.0;
			for(int i = 0; i < mut.w.length; ++i)
			{	//Slightly perturb the vector
				mut.w[i] += (Math.random() - 0.5) / 5.0;
				if(mut.w[i] < 0.0)
					mut.w[i] = 0.0;
				s += mut.w[i];
			}
			if(s != 0.0d)
			{	//Normalize all values
				for(int i = 0; i < mut.w.length; ++i)
					mut.w[i] /= s;
			}
			mut.Update();
			return mut;
		}
	}

	public HeuristicOptimizer(List<Fingering> min, List<Fingering> max, int n)
	{	//Initialize the population
		pop = new ArrayList<Chromosome>(n);
		for(int i = 0; i < n; ++i)
		{
			pop.add(new WeightChrome(min, max));
		}
	}
		
	public void RunGE() 
	{
		// initialize a new genetic algorithm
		GeneticAlgorithm ga = new GeneticAlgorithm(new WeightXvr(), 
				CROSSOVER_RATE, // all selected chromosomes will be recombined (=crosssover)
				new WeightMut(),
				MUTATION_RATE,
				new TournamentSelection(TOURNAMENT_ARITY));
		// initial population
		Population initial = new ElitisticListPopulation(pop, pop.size(), ELITISM_RATE);
		// stopping condition
		StoppingCondition stopCond = new FixedGenerationCount(NUM_GENERATIONS);
		// best initial chromosome
		WeightChrome f1 = (WeightChrome) initial.getFittestChromosome();
		System.out.print("Initial Chromosome:\t" + f1.getFitness() + "\n");
		double[] vals = f1.w;
		for(int i = 0; i < vals.length; ++i)
			System.out.print(vals[i] + " ");
		// run the algorithm
		Population finalPopulation = ga.evolve(initial, stopCond);
		// best chromosome from the final population
		WeightChrome f2 = (WeightChrome) finalPopulation.getFittestChromosome();
		System.out.print("\nFinal Chromosome:\t" + f2.getFitness() + "\n");
		vals = f2.w;
		for(int i = 0; i < vals.length; ++i)
			System.out.print(vals[i] + " ");
		System.out.print("\n");
		f2.Update();
	}
}
