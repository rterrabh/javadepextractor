package enums;

import dependencies.AccessDependency;
import dependencies.AnnotateDependency;
import dependencies.CreateDependency;
import dependencies.DeclareDependency;
import dependencies.Dependency;
import dependencies.DeriveDependency;
import dependencies.ExtendDependency;
import dependencies.HandleDependency;
import dependencies.ImplementDependency;
import dependencies.ThrowDependency;

public enum DependencyType {
	ACCESS("access", AccessDependency.class), USEANNOTATION("useannotation", AnnotateDependency.class), CREATE("create",
			CreateDependency.class), DECLARE("declare", DeclareDependency.class), DERIVE("derive",
			DeriveDependency.class), EXTEND("extend", ExtendDependency.class), HANDLE("handle", HandleDependency.class), IMPLEMENT(
			"implement", ImplementDependency.class), THROW("throw", ThrowDependency.class), DEPEND("depend",
			Dependency.class);

	private final String value;
	private final Class<? extends Dependency> dependencyClass;

	private DependencyType(String value, Class<? extends Dependency> dependencyClass) {
		this.value = value;
		this.dependencyClass = dependencyClass;
	}

	public String getValue() {
		return this.value;
	}

	public Class<? extends Dependency> getDependencyClass() {
		return this.dependencyClass;
	}

	public final Dependency createGenericDependency(String classNameA, String classNameB) {
		if (this == ACCESS) {
			return new AccessDependency(classNameA, classNameB, null, null, null);
		} else if (this == USEANNOTATION) {
			return new AnnotateDependency(classNameA, classNameB, null, null, null);
		} else if (this == CREATE) {
			return new CreateDependency(classNameA, classNameB, null, null, null);
		} else if (this == DECLARE) {
			return new DeclareDependency(classNameA, classNameB, null, null, null);
		} else if (this == EXTEND) {
			return new ExtendDependency(classNameA, classNameB, null, null, null);
		} else if (this == IMPLEMENT) {
			return new ImplementDependency(classNameA, classNameB, null, null, null);
		} else if (this == THROW) {
			return new ThrowDependency(classNameA, classNameB, null, null, null, null);
		}
		return null;
	}
}