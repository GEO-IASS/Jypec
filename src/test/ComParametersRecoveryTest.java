package test;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Random;

import org.junit.Test;

import com.jypec.comdec.ComParameters;
import com.jypec.util.bits.BitInputStream;
import com.jypec.util.bits.BitOutputStream;

/**
 * @author Daniel
 * Test compressor parameters class
 */
public class ComParametersRecoveryTest {

	
	/**
	 * Test if the Compressor Parameters object is able to save itself
	 * and reload from a BitStream
	 */
	@Test
	public void testComParametersRecovery() {
		ComParameters cp = new ComParameters();
		ComParameters cpr = new ComParameters();

		Random r = new Random(0);
		
		for (int i = 0; i < 100; i++) {
			ByteArrayOutputStream bais = new ByteArrayOutputStream();
			BitOutputStream output = new BitOutputStream(bais);
			BitInputStream input;
			
			cp.wavePasses = r.nextInt(0x100);
			
			try {
				cp.saveTo(output);
				output.paddingFlush();
				input = new BitInputStream(new ByteArrayInputStream(bais.toByteArray()));
				cpr.loadFrom(input);
				
				assertTrue("Compressor parameters do not equal each other", cp.equals(cpr));
				assertTrue("The bitstream still has bits left", input.available() == 0);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
}
