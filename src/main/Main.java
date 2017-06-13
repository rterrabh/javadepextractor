package main;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.CoreException;

import core.Architecture;
import exception.DCLException;
import util.TxtFileWriter;

public class Main {

	public static void main(String[] args) throws CoreException, IOException, DCLException, InterruptedException {
		String root = args[0];
		File filePath = new File("").getAbsoluteFile();
		String path = filePath.getAbsolutePath();
		String projectPath = "";
		
		File f = new File(root);
		
		if(!f.exists()){
			File f2 = new File(path+"/"+root);
			if(!f2.exists()){
				File f3 = new File(path);
				if(!f3.exists()){
					System.out.println("Wrong Specified Path!");
				}else{
					projectPath = path;
				}
			}else{
				projectPath = path+"/"+root;
			}
		}else{
			projectPath = root;
		}
		
		if(!projectPath.equals("")){
			Architecture architecture = new Architecture(projectPath);
			TxtFileWriter.writeTxtFile(architecture.getDependencies(), projectPath);
		}
		
	}
}
