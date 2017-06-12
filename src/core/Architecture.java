package core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.ITypeBinding;

import ast.DCLDeepDependencyVisitor;
import dependencies.Dependency;
import enums.DependencyType;
import exception.DCLException;
import exception.ParseException;
import util.DCLUtil;

public class Architecture {
	/**
	 * String: class name Collection<Dependency>: Collection of established
	 * dependencies
	 */
	public Map<String, Collection<Dependency>> projectClasses = null;

	/**
	 * String: module name String: module description
	 */
	public Map<String, String> modules = null;

	/**
	 * Collection<DependencyConstraint>: Collection of dependency constraints
	 */
	
	/**
	 * List of all ITypeBinding
	 * 
	 */
	public List<ITypeBinding> typeBindings = null;

	
	public Architecture(String projectPath) throws CoreException, ParseException, IOException, DCLException, InterruptedException {
		this.projectClasses = new HashMap<String, Collection<Dependency>>();
		this.typeBindings = new ArrayList<ITypeBinding>();
		this.modules = new ConcurrentHashMap<String, String>();
		
		List<String> classPath = new LinkedList<String>();
		List<String> sourcePath = new LinkedList<String>();
		
		classPath.addAll(DCLUtil.getPath(projectPath));
		sourcePath.addAll(DCLUtil.getSource(projectPath));
		
		String[] classPathEntries = classPath.toArray(new String[classPath.size()]);
		String[] sourcePathEntries = sourcePath.toArray(new String[sourcePath.size()]);
		
		for (String f : DCLUtil.getFilesFromProject(projectPath)) {
			DCLDeepDependencyVisitor ddv = DCLUtil.useAST(f, classPathEntries, sourcePathEntries);
			Collection<Dependency> deps = ddv.getDependencies();
			//this.filterCommonDependencies(deps);
			this.projectClasses.put(ddv.getClassName(), deps);
			this.typeBindings.add(ddv.getITypeBinding());
		}
		
	}

	public Set<String> getProjectClasses() {
		return projectClasses.keySet();
	}

	public Collection<Dependency> getDependencies(String className) {
		return projectClasses.get(className);
	}
	
	public Collection<Dependency> getDependencies() {
		Collection<Dependency> listDep = new HashSet<Dependency>();
		for (Map.Entry<String, Collection<Dependency>> entryDep : projectClasses.entrySet()) {
			listDep.addAll(entryDep.getValue());
		}
		return listDep;
	}

	public Dependency getDependency(String classNameA, String classNameB, Integer lineNumberA, DependencyType dependencyType) {
		Collection<Dependency> dependencies = projectClasses.get(classNameA);
		for (Dependency d : dependencies) {
			if (lineNumberA == null) {

			}
			if ((lineNumberA == null) ? d.getLineNumber() == null : lineNumberA.equals(d.getLineNumber())
					&& d.getClassNameB().equals(classNameB) && d.getDependencyType().equals(dependencyType)) {
				return d;
			}
		}
		return null;
	}

	public void updateDependencies(String className, Collection<Dependency> dependencies) {
		projectClasses.put(className, dependencies);
	}

	public Map<String, String> getModules() {
		return this.modules;
	}


	public Set<String> getUsedClasses(final String className) {
		Set<String> set = new HashSet<String>();

		for (Dependency d : this.getDependencies(className)) {
			set.add(d.getClassNameB());
		}

		return set;
	}

	public Set<String> getUsedClasses(final String className, DependencyType dependencyType) {
		/* In this case, it only considers the type */
		if (dependencyType == null) {
			return getUsedClasses(className);
		}

		Set<String> set = new HashSet<String>();

		/*
		 * Here, two cases: 
		 * if (dependencyType == DEPEND) -> dep[*,T] 
		 * if (dependencyType == other) -> dep[other,T] 
		 * For example: dep[access,T]
		 */
		for (Dependency d : this.getDependencies(className)) {
			if (dependencyType.equals(DependencyType.DEPEND) || d.getDependencyType().equals(dependencyType)) {
				set.add("dep[" + d.getDependencyType().getValue() + "," + d.getClassNameB() + "]");	
			}
		}

		return set;
	}

	public Set<String> getUniverseOfUsedClasses() {
		Set<String> set = new HashSet<String>();

		for (Collection<Dependency> col : projectClasses.values()) {
			for (Dependency d : col) {
				set.add(d.getClassNameB());
			}
		}

		return set;
	}

	public Set<String> getUniverseOfUsedClasses(DependencyType dependencyType) {
		if (dependencyType == null) {
			return getUniverseOfUsedClasses();
		}
		Set<String> set = new HashSet<String>();

		/*
		 * Here, two cases: 
		 * if (dependencyType == DEPEND) -> dep[*,T] 
		 * if (dependencyType == other) -> dep[other,T] 
		 * For example: dep[access,T]
		 */
		for (Collection<Dependency> col : projectClasses.values()) {
			for (Dependency d : col) {
				if (dependencyType.equals(DependencyType.DEPEND) || d.getDependencyType().equals(dependencyType)) {
					set.add("dep[" + d.getDependencyType().getValue() + "," + d.getClassNameB() + "]");	
				}
			}
		}

		return set;
	}
	
	
	
	
	public List<String> getUsedClasses2(final String className) {
		List<String> list = new ArrayList<String>();

		for (Dependency d : this.getDependencies(className)) {
			list.add(d.getClassNameB());
		}

		return list;
	}

	public List<String> getUsedClasses2(final String className, DependencyType dependencyType) {
		/* In this case, it only considers the type */
		if (dependencyType == null) {
			return getUsedClasses2(className);
		}

		List<String> list = new ArrayList<String>();

		/*
		 * Here, two cases: 
		 * if (dependencyType == DEPEND) -> dep[*,T] 
		 * if (dependencyType == other) -> dep[other,T] 
		 * For example: dep[access,T]
		 */
		for (Dependency d : this.getDependencies(className)) {
			if (dependencyType.equals(DependencyType.DEPEND) || d.getDependencyType().equals(dependencyType)) {
				list.add("dep[" + d.getDependencyType().getValue() + "," + d.getClassNameB() + "]");	
			}
		}

		return list;
	}

	public List<String> getUniverseOfUsedClasses2() {
		List<String> list = new ArrayList<String>();

		for (Collection<Dependency> col : projectClasses.values()) {
			for (Dependency d : col) {
				list.add(d.getClassNameB());
			}
		}

		return list;
	}
	
	private void filterCommonDependencies(Collection<Dependency> dependencies) {
		try{
			String typesToDisregard[] = new String[] { "boolean", "char", "byte", "short", "int", "long", "float", "double",
		
				"java.lang.Boolean", "java.util.Vector", "java.util.Iterator", "java.lang.Class", "java.lang.Character", "java.lang.Byte", "java.lang.Short", "java.lang.Integer", "java.lang.Long",
				"java.lang.Float", "java.lang.Double", "java.lang.String", "java.lang.Object", "java.lang.Boolean[]", "java.lang.Boolean[][]",
				"java.lang.Character[]", "java.lang.Character[][]", "java.lang.Byte[]", "java.lang.Byte[][]", "java.lang.Short[]", 
				"java.lang.Short[][]", "java.lang.Integer[]", "java.lang.Integer[][]", "java.lang.Long[]", "java.lang.Long[][]",  
				"java.lang.Float[]", "java.lang.Float[][]", "java.lang.Double[]", "java.lang.Double[][]", "java.lang.String[]", "java.lang.String[][]",
				"java.lang.Object[]", "java.lang.Object[][]", "java.lang.Deprecated", "java.util.ArrayList", "java.util.ArrayList[]", "java.util.ArrayList[][]",
				"java.util.ArrayList<^[a-zA-Z]>", "java.util.ArrayList<^[a-zA-Z]>[]", "java.util.ArrayList<^.*>[][]",
				"java.util.ArrayList[]<.*>", "java.util.ArrayList[][]<.*>",
				"java.lang.SuppressWarnings", "java.lang.Override", "java.lang.SafeVarargs", "java.util.ArrayList<SQLData>", "java.util.ArrayList<String>", "java.util.ArrayList<JPanel>", "java.util.ArrayList<Long>", "java.util.ArrayList<Double>", "java.util.ArrayList<Integer>" };

			for (Iterator<Dependency> it = dependencies.iterator(); it.hasNext();) {
				Dependency d = it.next();
				if (contains(d.getClassNameA(), typesToDisregard) || contains(d.getClassNameB(), typesToDisregard)) {
					it.remove();
				}
			}
		}catch(NullPointerException npe){
			System.out.println("Error in Filtering Common Dependencies");
		}

	}

	private boolean contains(Object value, Object[] array) {
		for (Object o : array) {
			if (o.equals(value) || value.toString().startsWith("java.util.ArrayList")) {
				return true;
			}
		}
		return false;
	}

	

	public List<String> getUniverseOfUsedClasses2(DependencyType dependencyType) {
		if (dependencyType == null) {
			return getUniverseOfUsedClasses2();
		}
		List<String> list = new ArrayList<String>();

		/*
		 * Here, two cases: 
		 * if (dependencyType == DEPEND) -> dep[*,T] 
		 * if (dependencyType == other) -> dep[other,T] 
		 * For example: dep[access,T]
		 */
		for (Collection<Dependency> col : projectClasses.values()) {
			for (Dependency d : col) {
				if (dependencyType.equals(DependencyType.DEPEND) || d.getDependencyType().equals(dependencyType)) {
					list.add("dep[" + d.getDependencyType().getValue() + "," + d.getClassNameB() + "]");	
				}
			}
		}

		return list;
	}
	
	public List<ITypeBinding> getITypeBindings(){
		return typeBindings;
	}
	

}
