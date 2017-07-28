package com.jypec.img;

import com.jypec.ebc.SubBand;
import com.jypec.ebc.data.CodingBlock;
import com.jypec.util.data.IntegerMatrix;

/**
 * Class that stores one hyperspectral band and can be used for extracting blocks for coding and such
 * @author Daniel
 *
 */
public class HyperspectralBand implements IntegerMatrix {

	private HyperspectralImage hyimg;
	private int band;
	private int depth;
	private int lines;
	private int samples;
	
	
	/**
	 * Builds a hyperspectral band from the given hyperspectral image
	 * @param hi
	 * @param band
	 * @param depth
	 * @param lines 
	 * @param samples
	 */
	public HyperspectralBand (HyperspectralImage hi, int band, int depth, int lines, int samples) {
		this.hyimg = hi;
		this.band = band;
		this.depth = depth;
		this.lines = lines;
		this.samples = samples;
	}
	
	@Override
	public int getDataAt(int line, int sample) {
		return this.hyimg.getDataAt(this.band, line, sample);
	}
	
	/**
	 * @param line
	 * @param sample
	 * @return the VALUE of the DATA at the given position. This could be different from {@link #getDataAt(int, int)}
	 */
	private double getValueAt(int line, int sample) {
		return this.hyimg.getValueAt(this.band, line, sample);
	}
	
	@Override
	public void setDataAt(int data, int line, int sample) {
		this.hyimg.setDataAt(data, this.band, line, sample);
	}
	
	/**
	 * sets the given value at the given position
	 * @see HyperspectralImage
	 * @param value value to set
	 * @param line
	 * @param sample
	 */
	private void setValueAt(double value, int line, int sample) {
		this.hyimg.setValueAt(value, this.band, line, sample);
	}
	
	/**
	 * @return the number of lines this band has
	 */
	public int getNumberOfLines() {
		return this.lines;
	}
	@Override
	public int getRows() {
		return this.lines;
	}
	
	/**
	 * @return the number of samples this band has
	 */
	public int getNumberOfSamples() {
		return this.samples;
	}
	@Override
	public int getColumns() {
		return this.samples;
	}
	

	
	/**
	 * Extract a block that references the internal data (that is, can MODIFY it, be careful!)
	 * @param rowOffset
	 * @param colOffset
	 * @param height
	 * @param width
	 * @param band
	 * @return a Coding block Wrapper for the data in the requested position
	 */
	public CodingBlock extractBlock(int rowOffset, int colOffset, int height, int width, SubBand band) {
		if (rowOffset < 0 || colOffset < 0 || height < 0 || width < 0) {
			throw new IllegalArgumentException("Arguments cannot be negative");
		}
		if (rowOffset + height > this.lines || colOffset + width > this.samples) {
			throw new IllegalArgumentException("The block you are trying to create would have samples out of bounds");
		}
			
		return new CodingBlock(this, height, width, rowOffset, colOffset, this.depth, band);
	}
	

	/**
	 * @param lineOffset skip this number of lines before filling the returned value
	 * @param sampleOffset same as lineOffset but for samples
	 * @param lines number of lines to be returned
	 * @param samples number of samples to be returned
	 * @return the internal information in double precision format for wave analysis
	 */
	public double[][] toWave(int lineOffset, int sampleOffset, int lines, int samples) {
		double[][] wave = new double[lines][samples];
		
		for (int i = 0; i < lines; i++) {
			for (int j = 0; j  < samples; j++) {
				wave[i][j] = this.getValueAt(i + lineOffset, j + sampleOffset);
			}
		}
		
		return wave;
	}

	@Override
	public int[][] extractInnerMatrix() {
		return this.hyimg.getDataReferenceToBand(this.band);
	}

	/**
	 * Fill this image with the values from the double matrix. They are converted
	 * to the data type of the encompassing image and then set into the inner array
	 * @see HyperspectralImage
	 * @param waveForm from where to take the data
	 * @param lineOffset starting line for filling the data in
	 * @param sampleOffset starting sample for filling the data in
	 * @param lines number of lines in waveForm
	 * @param samples number of samples in waveForm
	 */
	public void fromWave(double[][] waveForm, int lineOffset, int sampleOffset, int lines, int samples) {
		for (int i = 0; i < lines; i++) {
			for (int j = 0; j  < samples; j++) {
				this.setValueAt(waveForm[i][j], i, j);
			}
		}
	}




}
