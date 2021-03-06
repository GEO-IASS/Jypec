package com.jypec.dimreduction.alg;

import org.ejml.data.FMatrixRMaj;
import org.ejml.dense.row.CommonOps_FDRM;
import org.ejml.dense.row.factory.DecompositionFactory_FDRM;
import org.ejml.interfaces.decomposition.SingularValueDecomposition_F32;

import com.jypec.dimreduction.ProjectingDimensionalityReduction;
import com.jypec.util.arrays.EJMLExtensions;
import com.jypec.util.debug.Logger;

/**
 * Implements the Minimum noise fraction algorithm for dimensionality reduction
 * <br>
 * Taken from "Real-Time Noise Removal for Line-Scanning Hyperspectral
 * Devices Using a Minimum Noise Fraction-Based Approach"
 * by Asgeir Bjorgan and Lise Lyngsnes Randeberg
 * <br>
 * (<a href="https://www.ncbi.nlm.nih.gov/pmc/articles/PMC4367363/pdf/sensors-15-03362.pdf">Visited 2017-09-25</a>)
 * <br><br>
 * @author Daniel
 */
public class MinimumNoiseFraction extends ProjectingDimensionalityReduction {
	
	
	/**
	 * Build a MNF algorithm
	 */
	public MinimumNoiseFraction() {
		super(DimensionalityReductionAlgorithm.DRA_MNF);
	}
	
	/**
	 * Extract the noise from the data. the formula used is: <br>
	 * noise(i,j) = (data(i,j) - data(i,j+1))/2 <br>
	 * except for the last value where: <br>
	 * noise(i,j) = (data(i,j) - data(i,j-1))/2 <br>
	 * @param data
	 * @return
	 */
	private static FMatrixRMaj extractNoise(FMatrixRMaj data) {
		//assume pushbroom sensor and only extract horizontal noise
		FMatrixRMaj res = new FMatrixRMaj(data.getNumRows(), data.getNumCols());
		for (int i = 0; i < data.getNumRows(); i++) {
			for (int j = 0; j < data.getNumCols(); j++) {
				float val = data.get(i, j);
				if (j < data.getNumCols() - 1) {
					val -= data.get(i, j+1);
				} else {
					val -= data.get(i, j-1);
				}
				res.set(i, j, val / 2.0f);
			}
		}
		return res;
	}

	//https://www.researchgate.net/profile/Angelo_Palombo/publication/224354550_Experimental_Approach_to_the_Selection_of_the_Components_in_the_Minimum_Noise_Fraction/links/02bfe51064486871c4000000.pdf
	@Override
	public boolean doTrain(FMatrixRMaj data) {
		if (this.reductionInTrainingRequested()) {
			data = EJMLExtensions.getSubSet(data, percentTraining);
		}
		
		//initialize values
		dimOrig = data.getNumRows();
		//find out data and noise. The data is NOT zero-meaned,
		//while the noise is assumed to be
		Logger.getLogger().log("Estimating noise...");
		FMatrixRMaj noise = extractNoise(data);
		//CommonOps_FDRM.subtract(data, noise, data);
		
		/**Create data covariance matrix */
		Logger.getLogger().log("Getting data covariance...");
		adjustment = new FMatrixRMaj(dimOrig, 1);
		FMatrixRMaj sigma = new FMatrixRMaj(dimOrig, dimOrig);
		EJMLExtensions.generateCovarianceMatrix(data, sigma, null, adjustment);
		/*********************************/
        
        /**Create noise covariance matrix */
		Logger.getLogger().log("Getting noise covariance...");
        FMatrixRMaj sigmaNoise = new FMatrixRMaj(dimOrig, dimOrig);
        CommonOps_FDRM.multTransB(noise, noise, sigmaNoise);
        /**********************************/
        
        //decompose sigma noise as noise = U*W*U^t
        Logger.getLogger().log("Applying SVD to noise...");
        SingularValueDecomposition_F32<FMatrixRMaj> svd = DecompositionFactory_FDRM.svd(dimOrig, dimOrig, true, false, false);
        boolean decompRes = svd.decompose(sigmaNoise);
        if (!decompRes) {
        	Logger.getLogger().log("Decomposition of noise failed!");
        	return false;
        }
        
        FMatrixRMaj B = svd.getU(null, false);
        FMatrixRMaj lambda = svd.getW(null);
        
        FMatrixRMaj A = new FMatrixRMaj(dimOrig, dimOrig);
        EJMLExtensions.inverseSquareRoot(lambda);
        CommonOps_FDRM.mult(B, lambda, A);
        
        
        /** Check if A^t*Sn*A = Identity (seems that way)
        FMatrixRMaj tmp = new FMatrixRMaj(sampleSize, sampleSize);
        FMatrixRMaj tmp2 = new FMatrixRMaj(sampleSize, sampleSize);
        CommonOps_FDRM.multTransA(A, sigmaNoise, tmp);
        CommonOps_FDRM.mult(tmp, A, tmp2);
        */
        Logger.getLogger().log("Removing noise...");
        FMatrixRMaj sigmaTemp = new FMatrixRMaj(dimOrig, dimOrig);
        FMatrixRMaj sigmaTransformed = new FMatrixRMaj(dimOrig, dimOrig);
        CommonOps_FDRM.multTransA(A, sigma, sigmaTemp);
        CommonOps_FDRM.mult(sigmaTemp, A, sigmaTransformed);
        
        //decompose sigma temp as noise = U*W*U^t
        Logger.getLogger().log("Applying SVD to noiseless data...");
        svd = DecompositionFactory_FDRM.svd(dimOrig, dimOrig, true, true, false);
        decompRes = svd.decompose(sigmaTransformed);
        if (!decompRes) {
        	Logger.getLogger().log("Decomposition of noiseless data failed!");
        	return false;
        }
        FMatrixRMaj D = svd.getU(null, false);
        
        this.projectionMatrix = new FMatrixRMaj(dimOrig, dimOrig);
        CommonOps_FDRM.mult(A, D, this.projectionMatrix);
        CommonOps_FDRM.transpose(this.projectionMatrix);
        
        this.unprojectionMatrix = new FMatrixRMaj(this.projectionMatrix);
        CommonOps_FDRM.invert(this.unprojectionMatrix);
        //CommonOps_FDRM.transpose(this.unprojectionMatrix);
        
        //CommonOps_FDRM.transpose(this.projectionMatrix);      
        this.projectionMatrix.reshape(dimProj, dimOrig, true);
        CommonOps_FDRM.transpose(this.unprojectionMatrix);
        this.unprojectionMatrix.reshape(dimProj, dimOrig, true);
        CommonOps_FDRM.transpose(this.unprojectionMatrix);
        this.unprojectionMatrix = new FMatrixRMaj(this.unprojectionMatrix); //ensure internal buffer size is the right shape
        Logger.getLogger().log("Finished");
        
        return true;
	}



}
