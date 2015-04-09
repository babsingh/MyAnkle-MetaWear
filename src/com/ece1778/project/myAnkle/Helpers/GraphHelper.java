package com.ece1778.project.myAnkle.Helpers;

public class GraphHelper {
	
	/* used to determine (heuristically) the optimal upper bound of the graph.
	 * the method is used to account for the chart values (point labels), which
	 * may be otherwise cut-off. to allow the upper bound to be set automatically,
	 * remove all instances of the method call */
	public static double estimateUpperBound(double[] values) {
		
		/* the margin factor governs what the minimum difference between the current 
		 * upper bound of the graph and the largest y-coordinate should be before the
		 * bound is raised. a larger margin factor implies larger padding */
		final double MARGIN_FACTOR = 0.4;
		
		int ceilingInteger, intervalCount;
		double highestValue = 0, roundedValue, step;
		
		/* find the highest of the parameterized values and its ceiling (integer) */
		for(double value : values) {
			
			// if the value is higher than the current highest value
			if(value > highestValue) {
				highestValue = value;
			}
		}
		
		ceilingInteger = (int) Math.ceil(highestValue);
		
		// if all values are less than 1 (special case), (5 intervals)
		if(ceilingInteger == 1) {
			
			// set the starting rounded value to 0.5
			roundedValue = 0.5;
			
			// set the increment and approximate interval count
			step = 0.5;
			intervalCount = 5;
			
		// the step is probably a multiple of 2 (4 intervals)
		} else if(ceilingInteger <= 8) {
			
			// round ceilingInteger up to the nearest multiple of 2
			roundedValue = ceilingInteger + (ceilingInteger % 2);
			
			// set the increment and approximate interval count
			step = 2;
			intervalCount = 4;
						
		// the step is probably a multiple of 5 (5 intervals)
		} else {
			
			// if ceilingInteger is not a multiple of 5, round it up to the nearest one
			if(ceilingInteger%5 != 0) {
				roundedValue = ceilingInteger + (5 - (ceilingInteger % 5));
				
			// else, set roundedInteger to ceilingInteger
			} else {
				roundedValue = ceilingInteger;
			}
			
			// set the increment and approximate interval count
			step = 5;
			intervalCount = 5;
		}
		
		// while the difference is not sufficiently large, increment the rounded value
		while((roundedValue - highestValue) <= MARGIN_FACTOR * (roundedValue/intervalCount)) {
			roundedValue += step;
		}
		
		// set the upper bound
		return roundedValue;
	}
}
