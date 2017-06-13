package util;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;

import dependencies.Dependency;

public class TxtFileWriter {

	private static final String COMMA_DELIMITER = ",";
	private static final String NEW_LINE_SEPARATOR = "\n";

	public static void writeTxtFile(Collection<Dependency> deps, String projectPath) {

		FileWriter fileWriter = null;

		try {
			fileWriter = new FileWriter(projectPath + "/dependencies.txt");

			for (Dependency dep : deps) {
				fileWriter.append(dep.getClassNameA());
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(dep.getDependencyType().getValue());
				fileWriter.append(COMMA_DELIMITER);
				fileWriter.append(dep.getClassNameB());
				fileWriter.append(NEW_LINE_SEPARATOR);
			}

			System.out.println("Dependencies.txt file was created successfully in project root folder!");

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