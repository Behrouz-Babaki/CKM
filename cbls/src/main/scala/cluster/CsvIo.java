/*************************************************************************
 * @author Jason Altschuler
 * 
 * PURPOSE: Read CSV files
 ************************************************************************/
package cluster;

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

public class CsvIo {

	/**
	 * Reads double[][] from csv file
	 */
	public static double[][] read(File inFile) {

		BufferedReader bf = null;
		ArrayList<ArrayList<Double>> data = new ArrayList<ArrayList<Double>>();

		String line = "";
		int c, columns = -1;
		Boolean first = true;

		try {
			bf = new BufferedReader(new FileReader(inFile));

			while ((line = bf.readLine()) != null) {
				String[] x = line.split("\\s");
				if (first) {
					columns = x.length;
					first = false;
				} else if (x.length != columns)
					throw new IllegalArgumentException("File has invalid dimensions (columns)");

				ArrayList<Double> currentRow = new ArrayList<Double>(columns);
				for (c = 0; c < x.length; c++)
					currentRow.add(c, Double.parseDouble(x[c] + "\t"));
				data.add(currentRow);
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (bf != null) {
				try {
					bf.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		int rows = data.size();
		double[][] arr = new double[rows][columns];
		for (int i = 0; i < rows; i++)
			for (int j = 0; j < columns; j++)
				arr[i][j] = data.get(i).get(j);
		return arr;
	}

	public static void write(String outFile, double[][] arr) throws FileNotFoundException {
		PrintStream out = new PrintStream(new FileOutputStream(outFile));

		int rows = arr.length;
		int columns = arr[0].length;

		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < columns; c++) {
				out.print(arr[r][c]);

				if (c != columns - 1)
					out.print(',');
			}
			out.println();
		}

		out.close();
	}

	public static void write(String outFile, int[] arr) throws FileNotFoundException {
		PrintStream out = new PrintStream(new FileOutputStream(outFile));
		for (int r = 0; r < arr.length; r++)
			out.println(arr[r]);
		out.close();
	}

	public static double[] flatten(double[][] arr2d) {
		int rows = arr2d.length;
		if (arr2d[0].length > 1)
			throw new IllegalArgumentException("Each entry should be an array with size one");

		double[] arr = new double[rows];
		for (int i = 0; i < rows; i++)
			arr[i] = arr2d[i][0];

		return arr;
	}
}