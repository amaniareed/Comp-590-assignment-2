package app;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Scanner;

import ac.ArithmeticDecoder;
import io.InputStreamBitSource;
import io.InsufficientBitsLeftException;

public class AdaptiveACDecodeTextFile {

	public static void main(String[] args) throws InsufficientBitsLeftException, IOException {
		String input_file_name = "out.dat";
		String output_file_name = "out.txt";

		FileInputStream fis = new FileInputStream(input_file_name);

		InputStreamBitSource bit_source = new InputStreamBitSource(fis);

		Integer[] symbols = new Integer[512];
		
		for (int i=-255; i<symbols.length; i++) {
			symbols[i+255] = i;
		}
		
		int pixelvalues = fis.read();
		Integer[] pixelvalue = new Integer[pixelvalues];
		
		for (int i=0; i<pixelvalue.length; i++) {
			pixelvalue[i] = fis.read();
		}
		
		
		for (int i=0; i<pixelvalue.length; i++) {
			int firstpixelvalue = pixelvalue[0];
				
				if (i == 0) {
					pixelvalue[0] = firstpixelvalue;
				}
				if (i != 0) {
					int difference = pixelvalue[i] - pixelvalue[i-1];
					pixelvalue[i] = difference;
				}	
		}
		
		

		FreqCountIntegerSymbolModel model = new FreqCountIntegerSymbolModel(symbols);
		
		// Read in number of symbols encoded

		int num_symbols = bit_source.next(32);

		// Read in range bit width and setup the decoder

		int range_bit_width = bit_source.next(8);
		ArithmeticDecoder<Integer> decoder = new ArithmeticDecoder<Integer>(range_bit_width);

		// Decode and produce output.
		
		System.out.println("Uncompressing file: " + input_file_name);
		System.out.println("Output file: " + output_file_name);
		System.out.println("Range Register Bit Width: " + range_bit_width);
		System.out.println("Number of encoded symbols: " + num_symbols);
		
		FileOutputStream fos = new FileOutputStream(output_file_name);

		for (int i=0; i<pixelvalue.length; i++) {
			pixelvalue[i] = decoder.decode(model, bit_source);
			fos.write(pixelvalue[i]);
			model.addToCount(pixelvalue[i]);
		}

		System.out.println("Done.");
		fos.flush();
		fos.close();
		fis.close();
	}
}
