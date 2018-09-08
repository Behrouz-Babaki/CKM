/*************************************************************************
 * @author Jason Altschuler
 * 
 * PURPOSE: Read CSV files
 ************************************************************************/
package cluster;

import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

public class CSVreader {

	/**
	 * Reads double[][] from csv file
	 */
	public static double[][] read(String inFile) {

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

	public static void main(String[] args) {
		String testFile = null;
		boolean print = false;

		// read user's configurations for test
		if (args.length == 2) {
			testFile = args[0];
			print = Boolean.parseBoolean(args[1]);
		}

		// default test case
		else
		{
			System.out.println("usage: CSVreader FILE DOPRINT");
			System.exit(1);
		}

		double[][] test = CSVreader.read(testFile);

		if (print) {
			for (int i = 0; i < test.length; i++) {
				for (int j = 0; j < test[0].length; j++)
					System.out.print(test[i][j] + " ");
				System.out.println();
			}
		}

		System.out.println("Read " + testFile + " successfully!");
	}
}