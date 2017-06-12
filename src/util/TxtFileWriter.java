package util;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import dependencies.Dependency;

public class TxtFileWriter {

	// Delimiter used in CSV file
	private static final String COMMA_DELIMITER = ",";
	private static final String NEW_LINE_SEPARATOR = "\n";

	public static void writeTxtFile(Collection<Dependency> deps, String projectPath) {

		FileWriter fileWriter = null;

		try {
			fileWriter = new FileWriter(projectPath + "/dependencies.txt");

			// Write the CSV file header
			// fileWriter.append(FILE_HEADER.toString());

			// Add a new line separator after the header
			// fileWriter.append(NEW_LINE_SEPARATOR);

			// Write a new student object list to the CSV file
			for (Dependency dep : deps) {
				fileWriter.append(dep.getClassNameA());
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(dep.getDependencyType().getValue());
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(dep.getClassNameB());
				fileWriter.append(NEW_LINE_SEPARATOR);
			}

			// System.out.println("CSV file was created successfully !!!");

		} catch (

		Exception e) {
			System.out.println("Error in CsvFileWriter !!!");
			e.printStackTrace();
		} finally {

			try {
				fileWriter.flush();
				fileWriter.close();
			} catch (IOException e) {
				System.out.println("Error while flushing/closing fileWriter !!!");
				e.printStackTrace();
			}

		}
	}
}