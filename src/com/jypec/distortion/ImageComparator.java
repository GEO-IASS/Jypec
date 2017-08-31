package com.jypec.distortion;

import com.jypec.img.HyperspectralBand;
import com.jypec.img.HyperspectralImage;

/**
 * @author Daniel
 * Utilities for image comparison
 */
public class ImageComparator {

	
	/**
	 * Compares both images, returning the PSNR (Peak Signal Noise Ratio) value.
	 * @param h1
	 * @param h2
	 * @return the PSNR between both images
	 */
	public double rawPSNR (HyperspectralImage h1, HyperspectralImage h2) {
		double mse = this.MSE(h1, h2);
		double maxVal = h1.getDataType().getMagnitudeAbsoluteRange();
		
		return this.PSNR(mse, maxVal);
	}
	
	/**
	 * Compares both bands, returning the PSNR (Peak Signal Noise Ratio) value.
	 * @param h1
	 * @param h2
	 * @return the PSNR between both bands
	 */
	public double rawPSNR (HyperspectralBand h1, HyperspectralBand h2) {
		double mse = this.MSE(h1, h2);
		double maxVal = h1.getDataType().getMagnitudeAbsoluteRange();
		
		return this.PSNR(mse, maxVal);
	}
	
	/**
	 * @param h1
	 * @param h2
	 * @return the normalized PSNR, calculated using the dynamic range of the image instead of
	 * the fixed maximum value range that pixels can have
	 */
	public double normalizedPSNR(HyperspectralImage h1, HyperspectralImage h2) {
		double mse = this.MSE(h1, h2);
		int[] minMax = this.minMaxVal(h1);
		double maxVal = minMax[1] - minMax[0];
		
		return this.PSNR(mse, maxVal);
	}

	/**
	 * @param h1
	 * @param h2
	 * @return the normalized PSNR, calculated using the dynamic range of the image instead of
	 * the fixed maximum value range that pixels can have
	 */
	public double normalizedPSNR(HyperspectralBand h1, HyperspectralBand h2) {
		double mse = this.MSE(h1, h2);
		int[] minMax = this.minMaxVal(h1);
		double maxVal = minMax[1] - minMax[0];
		
		return this.PSNR(mse, maxVal);
	}
	
	private double PSNR (double mse, double max) {
		if (mse == 0d) {
			return Double.POSITIVE_INFINITY;
		} else {
			return 20 * Math.log10(max) - 10 * Math.log10(mse);
		}
	}
	
	
	/**
	 * @param h1
	 * @param h2
	 * @return the Mean Squared Error (mean of all "differences between pixels squared")
	 */
	public double MSE (HyperspectralImage h1, HyperspectralImage h2) {
		if (!h1.sizeAndTypeEquals(h2)) {
			throw new IllegalArgumentException("Image sizes do not match!");
		}
		//add up all squared differences
		double acc = 0;
		for (int i = 0; i < h1.getNumberOfBands(); i++) {
			for (int j = 0; j < h1.getNumberOfLines(); j++) {
				for (int k = 0; k < h1.getNumberOfSamples(); k++) {
					int val = h1.getValueAt(i, j, k) - h2.getValueAt(i, j, k);
					acc += val * val;
				}
			}
		}
		//do the mean and return it
		acc /= h1.getNumberOfBands() * h1.getNumberOfLines() * h1.getNumberOfSamples();
		return acc;
	}
	
	/**
	 * @param h1
	 * @param h2
	 * @return the Mean Squared Error (mean of all "differences between pixels squared")
	 */
	public double MSE (HyperspectralBand h1, HyperspectralBand h2) {
		if (!h1.sizeAndTypeEquals(h2)) {
			throw new IllegalArgumentException("Image sizes do not match!");
		}
		//add up all squared differences
		double acc = 0;
		for (int j = 0; j < h1.getNumberOfLines(); j++) {
			for (int k = 0; k < h1.getNumberOfSamples(); k++) {
				int val = h1.getValueAt(j, k) - h2.getValueAt(j, k);
				acc += val * val;
			}
		}
		//do the mean and return it
		acc /= h1.getNumberOfLines() * h1.getNumberOfSamples();
		return acc;
	}
	
	
	/**
	 * @param h1
	 * @return a pair of integers, the firs one being the minimum value within the image, 
	 * the second one being the maximum. 
	 */
	public int[] minMaxVal(HyperspectralImage h1) {
		int[] minMax = new int[2];
		minMax[0] = Integer.MAX_VALUE;
		minMax[1] = Integer.MIN_VALUE;
		for (int i = 0; i < h1.getNumberOfBands(); i++) {
			for (int j = 0; j < h1.getNumberOfLines(); j++) {
				for (int k = 0; k < h1.getNumberOfSamples(); k++) {
					int sample = h1.getValueAt(i, j, k);
					if (minMax[0] > sample) {
						minMax[0] = sample;
					}
					if (minMax[1] < sample) {
						minMax[1] = sample;
					}
				}
			}
		}
		return minMax;
	}

	
	/**
	 * @param h1
	 * @return a pair of integers, the firs one being the minimum value within the band, 
	 * the second one being the maximum. 
	 */
	public int[] minMaxVal(HyperspectralBand h1) {
		int[] minMax = new int[2];
		minMax[0] = Integer.MAX_VALUE;
		minMax[1] = Integer.MIN_VALUE;
		for (int j = 0; j < h1.getNumberOfLines(); j++) {
			for (int k = 0; k < h1.getNumberOfSamples(); k++) {
				int sample = h1.getValueAt(j, k);
				if (minMax[0] > sample) {
					minMax[0] = sample;
				}
				if (minMax[1] < sample) {
					minMax[1] = sample;
				}
			}
		}
		return minMax;
	}
	
}
