package dependencies;

import java.io.Serializable;

import enums.DependencyType;

public abstract class Dependency implements Serializable {
	protected final String classNameA;
	protected final String classNameB;
	protected final Integer lineNumberA;
	protected final Integer offset;
	protected final Integer length;

	protected Dependency(String classNameA, String classNameB, Integer lineNumberA, Integer offset, Integer length) {
		super();
		this.classNameA = classNameA;
		this.classNameB = classNameB;
		this.lineNumberA = lineNumberA;
		this.offset = offset;
		this.length = length;
	}

	public String getClassNameA() {
		return this.classNameA;
	}

	public String getClassNameB() {
		return this.classNameB;
	}

	public Integer getLineNumber() {
		return lineNumberA;
	}
	
	public Integer getOffset() {
		return this.offset;
	}
	
	public Integer getLength() {
		return this.length;
	}

	public final boolean sameType(Dependency other) {
		return (this.getDependencyType().equals(other.getDependencyType()) && this.classNameB.equals(other.classNameB));
	}

	public abstract DependencyType getDependencyType();
	
	public abstract String toShortString();
}