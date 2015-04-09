package com.ece1778.project.myAnkle.Helpers;

import java.util.ArrayList;

import junit.framework.Assert;

public class Samples {
	private static final int X = 0, Y = 1, Z = 2, XNEG = 3, YNEG = 4, ZNEG = 5;
	private final ArrayList<Float> m_calibrationValues;
	private ArrayList<Sample> m_samples;
	private float xSum, ySum, zSum;
	
	private ArrayList<Sample> m_samples_temp;
	private float xSum_temp, ySum_temp, zSum_temp;

	public Samples(ArrayList<Float> calibrationValues) {
		m_samples = new ArrayList<Sample>();
		m_samples_temp = new ArrayList<Sample>();
		
		assert(calibrationValues!=null && calibrationValues.size() == 6);
		m_calibrationValues = calibrationValues;
		
		xSum = 0; ySum = 0; zSum = 0;	
		xSum_temp = 0; ySum_temp = 0; zSum_temp = 0;	
	}

	// Never used
	public boolean readFromFile(String filename) {
		Assert.assertTrue(getNumSamples() == 0);
		Assert.fail("Need to implement");
		return false;
	}
	
	/**
	 * Adds a new Sample object to m_samples ArrayList
	 * @param t time
	 * @param x 
	 * @param y
	 * @param z
	 */
	public void add(float t, float x, float y, float z) {
		Sample new_Sample = new Sample(t, x, y, z);
		
		m_samples.add(new_Sample);
		xSum += new_Sample.x_cal();
		ySum += new_Sample.y_cal();
		zSum += new_Sample.z_cal();
		
		m_samples_temp.add(new_Sample);
		xSum_temp += new_Sample.x_cal();
		ySum_temp += new_Sample.y_cal();
		zSum_temp += new_Sample.z_cal();
	}
	
	public void resetTempVars () {
		m_samples_temp = new ArrayList<Sample>();
		xSum_temp = 0; ySum_temp = 0; zSum_temp = 0;	
	}

	public void clear() {
		m_samples.clear();
	}

	public int getNumSamples() {
		return m_samples.size();
	}
	
	public Sample getSample(int index) {
		return m_samples.get(index);
	}

	/**
	 * Computes a temp mean_r value.
	 * Iterates through the entire m_samples ArrayList. 
	 * @return mean_r
	 */
	public float get_mean_r_temp() {
		int num_samples = m_samples_temp.size();
	
		xSum_temp /= num_samples;
		ySum_temp /= num_samples;
		zSum_temp /= num_samples;
		
		float gFactor = (float) (9.81 / Math.sqrt(Math.pow(xSum_temp, 2) + Math.pow(ySum_temp, 2)+ Math.pow(zSum_temp, 2)));
		
		xSum_temp *= gFactor;
		ySum_temp *= gFactor;
		zSum_temp *= gFactor;
		
		float mean_r = 0;
		Sample sample;
		
		for (int i = 0; i < num_samples; ++i) {
			sample = m_samples_temp.get(i);
			mean_r += Math.sqrt((float) (Math.pow(sample.x_cal() - xSum_temp, 2) + 
									  	 Math.pow(sample.y_cal() - ySum_temp, 2) +
									  	 Math.pow(sample.z_cal() - zSum_temp, 2)));
		}
		mean_r /= num_samples;
		
		resetTempVars();
		
		return mean_r;
	}
	
	/**
	 * Computes a mean_r value.
	 * Iterates through the entire m_samples ArrayList. 
	 * @return mean_r
	 */
	public float get_mean_r() {
		int num_samples = getNumSamples();
		xSum /= num_samples;
		ySum /= num_samples;
		zSum /= num_samples;
		
		float gFactor = (float) (9.81 / Math.sqrt(Math.pow(xSum, 2) + Math.pow(ySum, 2)+ Math.pow(zSum, 2)));
		
		xSum *= gFactor;
		ySum *= gFactor;
		zSum *= gFactor;
		
		float mean_r = 0;
		Sample sample;
		
		for (int i = 0; i < num_samples; ++i) {
			sample = getSample(i);
			mean_r += Math.sqrt((float) (Math.pow(sample.x_cal() - xSum, 2) + 
									  	 Math.pow(sample.y_cal() - ySum, 2) +
									  	 Math.pow(sample.z_cal() - zSum, 2)));
		}
		mean_r /= num_samples;
		return mean_r;
	}

	public float get_mean_r(int coverage) {
		int num_samples = getNumSamples();
		float xSumC, ySumC, zSumC;
		
//		xSumC = xSum_temp/coverage;
//		ySumC = ySum_temp/coverage;
//		zSumC = zSum_temp/coverage;
//		xSum_temp = 0;
//		ySum_temp = 0;
//		zSum_temp = 0;
		
		xSumC = xSum/num_samples;
		ySumC = ySum/num_samples;
		zSumC = zSum/num_samples;
		
		float gFactor = (float) (9.81 / Math.sqrt(Math.pow(xSumC, 2) + Math.pow(ySumC, 2)+ Math.pow(zSumC, 2)));
		
		xSumC *= gFactor;
		ySumC *= gFactor;
		zSumC *= gFactor;
		
		float mean_r = 0;
		Sample sample;
		
		//(num_samples - coverage)
		for (int i = num_samples - 1; i >= 0; i--) {
			sample = getSample(i);
			mean_r += Math.sqrt((float) (Math.pow(sample.x_cal() - xSumC, 2) + 
									  	 Math.pow(sample.y_cal() - ySumC, 2) +
									  	 Math.pow(sample.z_cal() - zSumC, 2)));
		}
		mean_r /= num_samples;
		return mean_r;
	}
	
	public class Sample {
		private float m_t;
		private float m_x_calibrated;
		private float m_y_calibrated;
		private float m_z_calibrated;
		public Sample(float t, float x, float y, float z) {
			m_t = t;
			m_x_calibrated = (float) (x * 9.81 / ((x >= 0) ? m_calibrationValues.get(X) : m_calibrationValues.get(XNEG)));
			m_y_calibrated = (float) (y * 9.81 / ((y >= 0) ? m_calibrationValues.get(Y) : m_calibrationValues.get(YNEG)));
			m_z_calibrated = (float) (z * 9.81 / ((z >= 0) ? m_calibrationValues.get(Z) : m_calibrationValues.get(ZNEG)));
		}
		public float t() { return m_t; }
		public float x_cal() { return m_x_calibrated; }
		public float y_cal() { return m_y_calibrated; }
		public float z_cal() { return m_z_calibrated; }
		
	}
}
