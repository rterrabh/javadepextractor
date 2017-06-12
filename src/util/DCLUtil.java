package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jface.text.Document;
import ast.DCLDeepDependencyVisitor;
import exception.DCLException;
import exception.ParseException;

//import hudson.FilePath;

public final class DCLUtil {
	public static final String NOME_APLICACAO = ".: archici :.";
	public static final String DC_FILENAME = "architecture.dcl";
	public static final String DCLDATA_FOLDER = "dcldata";
	
	private DCLUtil() {
	}
	
	/*public static Collection<String> getPathFromFile(Path classpath) throws IOException, InterruptedException{
		Collection<String> classEntriesPathFromFile = new LinkedList<String>();
		
		String sCurrentLine;
		InputStream is = classpath.read();
		 
		BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));

		while ((sCurrentLine = br.readLine()) != null) {
			Pattern findPath = Pattern.compile("\\bpath=\"(.*?)\"");
			
			Matcher matcher = findPath.matcher(sCurrentLine);
			while (matcher.find()) {
				String result = matcher.group(1);
				if (result.endsWith(".jar")){
					if(result.startsWith("/")){
						classEntriesPathFromFile.add(result);
					}
					else {
						classEntriesPathFromFile.add(classpath.getParent().getRemote()+"/"+result);
					}
				}
			}
		}
		
		return classEntriesPathFromFile;
		
	}*/
	
	
	public static Collection<String> getPath(String folder) throws IOException, InterruptedException{
		Collection<String> classEntriesPath = new LinkedList<String>();
		
		File file = new File(folder);
		
		Stack<File> stack = new Stack<File>();
		stack.push(file);
			while(!stack.isEmpty()) {
				File childpath = stack.pop();
				if (childpath.isDirectory()) {
					for(File f : childpath.listFiles()) stack.push(f);
				} else if(childpath.isDirectory() && childpath.getName().equals("bin")) {
					classEntriesPath.add(childpath.getAbsolutePath());
				} else if (!childpath.isDirectory() && childpath.getName().endsWith(".jar")) {
					classEntriesPath.add(childpath.getAbsolutePath());
				}
			}
			
			String javaHome = System.getProperty("java.home");

			File javaHomeFile = new File(javaHome);
			
			
		Stack<File> stack2 = new Stack<File>();
			stack2.push(javaHomeFile);
				while(!stack2.isEmpty()) {
					File childpath = stack2.pop();
					if (childpath.isDirectory()) {
						for(File f : childpath.listFiles()) stack2.push(f);
					} else if(childpath.isDirectory() && childpath.getName().equals("bin")) {
						classEntriesPath.add(childpath.getAbsolutePath());
					} else if (!childpath.isDirectory() && childpath.getName().endsWith(".jar")) {
						classEntriesPath.add(childpath.getAbsolutePath());
					}
				}
				
				String maven = System.getProperty("user.home") + "/.m2";
				
				File mavenFile = new File(maven);
				
				if(mavenFile.exists()){
				
					Stack<File> stack3 = new Stack<File>();
					stack3.push(mavenFile);
						while(!stack3.isEmpty()) {
							File childpath = stack3.pop();
							if (childpath.isDirectory()) {
								for(File f : childpath.listFiles()) stack3.push(f);
							} else if(childpath.isDirectory() && childpath.getName().equals("bin")) {
								classEntriesPath.add(childpath.getAbsolutePath());
							} else if (!childpath.isDirectory() && childpath.getName().endsWith(".jar")) {
								classEntriesPath.add(childpath.getAbsolutePath());
							}
						}
				}
			
		return classEntriesPath;
		}
	
	public static Collection<String> getSource(String folder) throws IOException, InterruptedException{
		Collection<String> sourceEntriesPath = new LinkedList<String>();
		
		File file = new File(folder);
		
		Stack<File> stack = new Stack<File>();
		stack.push(file);
			while(!stack.isEmpty()) {
				File childpath = stack.pop();
				if (childpath.isDirectory()) {
					if (childpath.getName().endsWith("src")) {
						sourceEntriesPath.add(childpath.getAbsolutePath());
					}
					else {
						for(File f : childpath.listFiles()) stack.push(f);
					}
				}
			}
		
		return sourceEntriesPath;
	}
	
	public static String adjustClassName(String className) {
		if (className.startsWith("boolean") || className.startsWith("byte") || className.startsWith("short")
				|| className.startsWith("long") || className.startsWith("double") || className.startsWith("float")) {
			return "java.lang." + className.toUpperCase().substring(0, 1) + className.substring(1);
		} else if (className.startsWith("int")) {
			return "java.lang.Integer";
		} else if (className.startsWith("int[]")) {
			return "java.lang.Integer[]";
		} else if (className.startsWith("char")) {
			return "java.lang.Character";
		} else if (className.startsWith("char[]")) {
			return "java.lang.Character[]";
		}
		return className.replaceAll("/", ".");
	}

	//RETORNA O NOME DA CLASSE DE UM ARQUIVO JAVA
	public static String getClassName(CompilationUnit cUnit, String f) throws IOException {
	    
		File file = new File(f);
		
		PackageDeclaration classPackage = cUnit.getPackage();
		
		String pack;
		if (classPackage!=null)
			pack = classPackage.getName() + ".";
		else
			pack = "";

		String clazz = FilenameUtils.removeExtension(file.getName());

		return pack + clazz;
	}

	//RETORNA UM COLLECTION DE STRING COM OS ARQUIVOS(FILES) DO UM DIRETORIO DE PROJETO
	public static Collection<String> getFilesFromProject(final String projectPath) throws IOException, InterruptedException {
		File file = new File(projectPath);
		
		final Collection<String> result = new LinkedList<String>();
		
		Stack<File> stack = new Stack<File>();
		stack.push(file);
			while(!stack.isEmpty()) {
				File childpath = stack.pop();
				if (childpath.isDirectory()) {
				  for(File f : childpath.listFiles())
					  stack.push(f);
				} else if (!childpath.isDirectory() && childpath.getName().endsWith(".java")) {
					result.add(childpath.getAbsolutePath());
				}
			}
		return result;
	}
	
		public static File getFilesFromProjectAndClass(final String projectPath, 
				final String className) throws IOException, InterruptedException {
			
			File result = null;
			
			File file = new File(projectPath);
			
			Stack<File> stack = new Stack<File>();
			stack.push(file);
				while(!stack.isEmpty()) {
					File childpath = stack.pop();
					if (childpath.isDirectory()) {
					  for(File f : childpath.listFiles())
						  stack.push(f);
					} else{ 
						//for (String className : classNames){
							if (!childpath.isDirectory() && childpath.getName().endsWith(className+".java")) {
								result = childpath;
						//	}
						}
					}
				}
			return result;
		}
	

	/**
	 * DCL2 Returns the module definition from the Java API
	 * 
	 * @return $java DCL constraint
	 */
	
	//RETORNA DEFINICAO DE MODULOS DO JAVA API 
	public static String getJavaModuleDefinition() {
		return "java.**,javax.**,org.ietf.jgss.**,org.omg.**,org.w3c.dom.**,org.xml.sax.**,boolean,char,short,byte,int,float,double,void";
	}

	/**
	 * DCL2 Checks if a className is contained in the Java API
	 * 
	 * @param className
	 *            Name of the class
	 * @return true if it is, no otherwise
	 */
	//VERIFICA SE UM CLASSNAME ESTA CONTIDO NO JAVA API
	public static boolean isFromJavaAPI(final String className) {
		for (String javaModulePkg : getJavaModuleDefinition().split(",")) {
			String prefix = javaModulePkg.substring(0, javaModulePkg.indexOf(".**"));
			if (className.startsWith(prefix)) {
				return true;
			}
		}
		return false;
	}

	public static String getNumberWithExactDigits(int originalNumber, int numDigits) {
		String s = "" + originalNumber;
		while (s.length() < numDigits) {
			s = "0" + s;
		}
		return s;
	}

	/**
	 * DCL2 Returns all dependencies from the class class
	 * 
	 * @param classes
	 *            List of classes
	 * @return List of dependencies
	 * @throws ParseException 
	 */
	
	//RETORNA TODOS OS OBJETOS DODCLDEEPDEPENDENCYVISITOR (AST)
	public static DCLDeepDependencyVisitor useAST(String f, String[] classPathEntries, String[] sourcePathEntries) throws IOException, DCLException, ParseException {
		return new DCLDeepDependencyVisitor(f, classPathEntries, sourcePathEntries);
	}
	
	
	public static CompilationUnit getCompilationUnitFromAST(String f, String[] classPathEntries, String[] sourcePathEntries) throws IOException, InterruptedException{
	
		String[] encodings = new String[sourcePathEntries.length];
		for(int i=0; i < sourcePathEntries.length; i++){
			encodings[i] = "UTF-8";
		}
		
		String source = readFileToString(f);
	    Document document = new Document(source);
	    
	    ASTParser parser = ASTParser.newParser(AST.JLS4);
	    
	    @SuppressWarnings("unchecked")
		Map<String, String> options = JavaCore.getDefaultOptions();
		options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);
		options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM,
				JavaCore.VERSION_1_8);
		options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_8);
	    parser.setCompilerOptions(options);
	    
	    parser.setKind(ASTParser.K_COMPILATION_UNIT);
	    parser.setSource(document.get().toCharArray());
	    parser.setResolveBindings(true);
	    
	    parser.setEnvironment(classPathEntries, sourcePathEntries, encodings, true);
	    parser.setUnitName("Dependency-Tool");
	    parser.setBindingsRecovery(true);
	    
	    return (CompilationUnit) parser.createAST(null);
	}
	
	public static Set<ITypeBinding> getSubTypes(List<ITypeBinding> typeBindings, String desc){
		Set<ITypeBinding> subTypes = new HashSet<ITypeBinding>();
		
		for (ITypeBinding typeBind : typeBindings){
			
			if(typeBind.getQualifiedName().equals(desc)){
				subTypes.addAll(Arrays.asList(typeBind.getDeclaredTypes()));
			}
			else{
				Set<ITypeBinding> superTypeBind = new HashSet<ITypeBinding>();
				
				ITypeBinding superclass = typeBind;
				boolean superMatch = false;
				
				while(superclass!=null && !superMatch){ 
					
					superTypeBind.add(superclass);
					
					ITypeBinding[] indirectInterfaceBinds = superclass.getInterfaces();
					
					for(ITypeBinding iib: indirectInterfaceBinds){
						if(iib.getQualifiedName().equals(desc)){
							subTypes.addAll(superTypeBind);
							superTypeBind.clear();
						}
					}
					
					superclass = superclass.getSuperclass();
					
					if(superclass.getQualifiedName().equals(desc)) 
						superMatch = true;
				}
				
				if(superMatch) subTypes.addAll(superTypeBind);
				
			}
		}	
		return subTypes;
	}
	
	 public static String readFileToString(String filePath) throws IOException { 
         FileReader fileReader = new FileReader(filePath); 
         BufferedReader reader = new BufferedReader(fileReader); 
          
         String fileContent = ""; 
         String l = null; 
         while ((l = reader.readLine()) != null) { 
                 fileContent += l; 
         } 

         reader.close(); 
         return fileContent;         
 } 
	
	public static String getPackageFromClassName(final String className) {
		if (className.contains(".")) {
			return className.substring(0, className.lastIndexOf('.'));
		}
		return className;
	}

	public static String getSimpleClassName(final String qualifiedClassName) {
		return qualifiedClassName.substring(qualifiedClassName.lastIndexOf(".") + 1);
	}

}
