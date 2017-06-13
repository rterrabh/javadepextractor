package util;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;

public class TxtFileWriter {

	private static final String NEW_LINE_SEPARATOR = "\n";

	public static void writeTxtFile(Collection<String> deps, String projectPath) {

		FileWriter fileWriter = null;

		try {
			fileWriter = new FileWriter(projectPath + "/dependencies.txt");

			for (String dep : deps) {
				fileWriter.append(dep);
				fileWriter.append(NEW_LINE_SEPARATOR);
			}

			System.out.println("dependencies.txt file was created successfully in project root folder!");

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