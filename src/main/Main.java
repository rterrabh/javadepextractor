package main;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.CoreException;

import core.Architecture;
import exception.DCLException;
import exception.ParseException;
import util.TxtFileWriter;

public class Main {

	public static void main(String[] args) throws CoreException, ParseException, IOException, DCLException, InterruptedException {
		String root = args[0];
		File filePath = new File("").getAbsoluteFile();
		String path = filePath.getAbsolutePath();
		String projectPath = "";
		
		File f = new File(path+"/"+root);
		
		if(!f.exists()){
			File f2 = new File(root);
			if(!f2.exists()){
				System.out.println("Wrong Specified Path!");
			}else{
				projectPath = root;
			}
		}else{
			projectPath = path+"/"+root;
		}
		
		if(!projectPath.equals("")){
			Architecture architecture = new Architecture(projectPath);
			TxtFileWriter.writeTxtFile(architecture.getDependencies(), projectPath);
		}
		
	}
}
