package app;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Scanner;

import ac.ArithmeticEncoder;
import io.OutputStreamBitSink;

public class AdaptiveACEncodeTextFile {

	public static void main(String[] args) throws IOException {
		String input_file_name = "out.dat"; //read in video file instead
		String output_file_name = "compressed.dat";

		FileInputStream fis = new FileInputStream(input_file_name);
		int range_bit_width = 40;
		
		//storing the first pixel value
		//storing how much they differ from the prior pixel
		//so maybe make a for loop that goes through the video file
		//goes through the length of the video file
		//get the first pixel value, store that in a variable
		//get the next pixel value, subtract the next minus the one before it
		//make sure you keep the reference to the first pixel value
		//where am I doing this in the encoder/decoder?
		

		System.out.println("Encoding text file: " + input_file_name);
		System.out.println("Output file: " + output_file_name);
		System.out.println("Range Register Bit Width: " + range_bit_width);

		int num_symbols = (int) new File(input_file_name).length();
		
		//replace with pixel values array, read in the pixel values
		
		Integer[] pixelvalues = new Integer[num_symbols];
				
		Integer[] symbols = new Integer[512];
		for (int i = -255; i<256; i++) {
			symbols[i+255] = i;
		}
		for (int i=0; i<pixelvalues.length; i++) {
			pixelvalues[i] = fis.read();
		}
		
		
		for (int i=0; i<pixelvalues.length; i++) {
			int firstpixelvalue = pixelvalues[0];
				
				if (i == 0) {
					pixelvalues[0] = firstpixelvalue;
				}
				if (i != 0) {
					int difference = pixelvalues[i] - pixelvalues[i-1];
					pixelvalues[i] = difference;
				}	
		}

		// Create new model with default count of 1 for all symbols
		FreqCountIntegerSymbolModel model = new FreqCountIntegerSymbolModel(symbols);

		ArithmeticEncoder<Integer> encoder = new ArithmeticEncoder<Integer>(range_bit_width);

		FileOutputStream fos = new FileOutputStream(output_file_name);
		OutputStreamBitSink bit_sink = new OutputStreamBitSink(fos);

		// First 4 bytes are the number of symbols encoded
		bit_sink.write(num_symbols, 32);		

		// Next byte is the width of the range registers
		bit_sink.write(range_bit_width, 8);

		// Now encode the input
//		FileInputStream fis = new FileInputStream(input_file_name);
		
		for (int i=0; i<pixelvalues.length; i++) {
			int next_symbol = pixelvalues[i];
			encoder.encode(next_symbol, model, bit_sink);
			
			// Update model
			model.addToCount(next_symbol);
		}
		fis.close();

		// Finish off by emitting the middle pattern 
		// and padding to the next word
		
		encoder.emitMiddle(bit_sink);
		bit_sink.padToWord();
		fos.close();
		
		System.out.println("Done");
	}
}
