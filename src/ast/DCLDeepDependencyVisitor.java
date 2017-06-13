package ast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import dependencies.AccessDependency;
import dependencies.AnnotateDependency;
import dependencies.CreateDependency;
import dependencies.DeclareDependency;
import dependencies.Dependency;
import dependencies.ExtendDependency;
import dependencies.ImplementDependency;
import dependencies.ThrowDependency;
import exception.DCLException;
import util.DCLUtil;

public class DCLDeepDependencyVisitor extends ASTVisitor {
	private List<Dependency> dependencies;
	private ITypeBinding typeBinding;

	private CompilationUnit cUnit;
	private String className;

	public DCLDeepDependencyVisitor(String f, String[] classPathEntries, String[] sourcePathEntries) throws DCLException {
		try{
			
			this.dependencies = new ArrayList<Dependency>();
		    
		    this.cUnit = DCLUtil.getCompilationUnitFromAST(f, classPathEntries, sourcePathEntries);
		    this.className = DCLUtil.getClassName(this.cUnit,f);
		    
			this.cUnit.accept(this);
		    
		} catch(Exception e){
			throw new DCLException(e,this.cUnit);
		}
	}
	

	public final List<Dependency> getDependencies() {
		return this.dependencies;
	}

	public final String getClassName() {
		return this.className;
	}

	@Override
	public boolean visit(TypeDeclaration node) {
		if (!node.isLocalTypeDeclaration() && !node.isMemberTypeDeclaration()) { // Para
																					// evitar
																					// fazer
																					// v�rias
																					// vezes
			try {
				@SuppressWarnings("unchecked")
				List<AbstractTypeDeclaration> types = cUnit.types();				
				TypeDeclaration typeDeclaration  = (TypeDeclaration) types.get(0);
				ITypeBinding typeBind = typeDeclaration.resolveBinding();
				
				this.typeBinding = typeBind;
				
				Set<ITypeBinding> superTypeBind = new HashSet<ITypeBinding>();
				Set<ITypeBinding> interfaceBinds = new HashSet<ITypeBinding>();
				
				ITypeBinding superclass = typeBind.getSuperclass();
				
				//TODO: TESTAR INTERFACES INDIRETAS
				
				while(superclass!=null){ 
					superTypeBind.add(superclass);
					ITypeBinding[] indirectInterfaceBinds = superclass.getInterfaces();
					interfaceBinds.addAll(Arrays.asList(indirectInterfaceBinds));
					superclass = superclass.getSuperclass();
				}

				for (ITypeBinding t : superTypeBind) {
					if (node.getSuperclassType() != null
							&& t.getQualifiedName().equals(node.getSuperclassType().resolveBinding().getQualifiedName())) {
						this.dependencies.add(new ExtendDependency(this.className, t.getQualifiedName(), cUnit
								.getLineNumber(node.getSuperclassType().getStartPosition()), node.getSuperclassType().getStartPosition(),
								node.getSuperclassType().getLength()));
					} else {
						//this.dependencies.add(new ExtendIndirectDependency(this.className, t.getQualifiedName(), null, null, null));
					}
				}

				//List<ITypeBinding> superInterfaceBind = new ArrayList<ITypeBinding>();
				ITypeBinding[] directInterfaceBinds = typeBind.getInterfaces();
				
				interfaceBinds.addAll(Arrays.asList(directInterfaceBinds));
				
				/*while(directInterfaceBinds.length!=0){
					
					for (ITypeBinding di : directInterfaceBinds){
						interfaceBinds.add(di);
					}
					
					for (ITypeBinding dii : interfaceBinds){
						directInterfaceBinds = dii.getInterfaces();
					}
				}*/

				
				//ITypeBinding[] interfaceBinds = typeBind.getInterfaces();

				
				externo: for (ITypeBinding t : interfaceBinds) {
					for (Object it : node.superInterfaceTypes()) {
						switch (((Type) it).getNodeType()) {
						case ASTNode.SIMPLE_TYPE:
							SimpleType st = (SimpleType) it;
							if (t.getQualifiedName().equals(st.getName().resolveTypeBinding().getQualifiedName())) {
								if (!typeDeclaration.isInterface()) {
									this.dependencies.add(new ImplementDependency(this.className, t.getQualifiedName(),
											cUnit.getLineNumber(st.getStartPosition()), st.getStartPosition(), st.getLength()));
								} else {
									this.dependencies.add(new ExtendDependency(this.className, t.getQualifiedName(), cUnit
											.getLineNumber(st.getStartPosition()), st.getStartPosition(), st.getLength()));
								}
								continue externo;
							}
							break;
						case ASTNode.PARAMETERIZED_TYPE:
							ParameterizedType pt = (ParameterizedType) it;
							if (t!= null && t.getQualifiedName() != null && pt != null && pt.getType() != null && pt.getType().resolveBinding() != null &&
/*Tirar duvida (no original era BinaryName
 * mas precise mudar para QualifiedName)*/
									//t.getQualifiedName().equals(pt.getType().resolveBinding().getBinaryName())) {
									t.getQualifiedName().equals(pt.getType().resolveBinding().getQualifiedName())) {
								if (!typeDeclaration.isInterface()) {
									this.dependencies.add(new ImplementDependency(this.className, t.getQualifiedName(),
									//this.dependencies.add(new ImplementDependency(this.className, t.getBinaryName(),
											cUnit.getLineNumber(pt.getStartPosition()), pt.getStartPosition(), pt.getLength()));
								} else {
/*Tirar duvida de como entraria*/									
									this.dependencies.add(new ExtendDependency(this.className, t.getBinaryName(), cUnit
											.getLineNumber(pt.getStartPosition()), pt.getStartPosition(), pt.getLength()));
								}
								continue externo;
							}
							break;
						}
					}
					//this.dependencies.add(new ImplementIndirectDependency(this.className, t.getQualifiedName(), null, null, null));
				}
			} catch (Exception e) {
				throw new RuntimeException("AST Parser error.", e);
			}
		}
		return true;
	} 

	@Override
	public boolean visit(MarkerAnnotation node) {
		if (node.getParent().getNodeType() == ASTNode.FIELD_DECLARATION) {
			this.dependencies.add(new AnnotateDependency(this.className, node.getTypeName().resolveTypeBinding().getQualifiedName(),
					cUnit.getLineNumber(node.getStartPosition()), node.getStartPosition(), node.getLength()));
		} else if (node.getParent().getNodeType() == ASTNode.METHOD_DECLARATION) {
			this.dependencies.add(new AnnotateDependency(this.className, node.getTypeName().resolveTypeBinding().getQualifiedName(),
					cUnit.getLineNumber(node.getStartPosition()), node.getStartPosition(), node.getLength()));
		} else if (node.getParent().getNodeType() == ASTNode.TYPE_DECLARATION) {
			this.dependencies.add(new AnnotateDependency(this.className, node.getTypeName().resolveTypeBinding().getQualifiedName(),
					cUnit.getLineNumber(node.getStartPosition()), node.getStartPosition(), node.getLength()));
		} else if (node.getParent().getNodeType() == ASTNode.VARIABLE_DECLARATION_STATEMENT) {
			ASTNode relevantParent = this.getRelevantParent(node);
			if (relevantParent.getNodeType() == ASTNode.METHOD_DECLARATION) {
				this.dependencies.add(new AnnotateDependency(this.className, node.getTypeName().resolveTypeBinding()
						.getQualifiedName(), cUnit.getLineNumber(node.getStartPosition()), node.getStartPosition(), node.getLength()));
			}
		} else if (node.getParent().getNodeType() == ASTNode.SINGLE_VARIABLE_DECLARATION) {
			ASTNode relevantParent = this.getRelevantParent(node);
			if (relevantParent.getNodeType() == ASTNode.METHOD_DECLARATION) {
				this.dependencies.add(new AnnotateDependency(this.className, node.getTypeName().resolveTypeBinding()
						.getQualifiedName(), cUnit.getLineNumber(node.getStartPosition()), node.getStartPosition(), node.getLength()));
			}

		}
		return true;
	}

	@Override
	public boolean visit(SingleMemberAnnotation node) {
		if (node.getParent().getNodeType() == ASTNode.FIELD_DECLARATION) {
			this.dependencies.add(new AnnotateDependency(this.className, node.getTypeName().resolveTypeBinding().getQualifiedName(),
					cUnit.getLineNumber(node.getStartPosition()), node.getStartPosition(), node.getLength()));
		} else if (node.getParent().getNodeType() == ASTNode.METHOD_DECLARATION) {
			this.dependencies.add(new AnnotateDependency(this.className, node.getTypeName().resolveTypeBinding().getQualifiedName(),
					cUnit.getLineNumber(node.getStartPosition()), node.getStartPosition(), node.getLength()));
		} else if (node.getParent().getNodeType() == ASTNode.TYPE_DECLARATION) {
			this.dependencies.add(new AnnotateDependency(this.className, node.getTypeName().resolveTypeBinding().getQualifiedName(),
					cUnit.getLineNumber(node.getStartPosition()), node.getStartPosition(), node.getLength()));
		}
		return true;
	}

	@Override
	public boolean visit(ClassInstanceCreation node) {
		ASTNode relevantParent = getRelevantParent(node);

		switch (relevantParent.getNodeType()) {
		case ASTNode.FIELD_DECLARATION:
			this.dependencies.add(new CreateDependency(this.className, this.getTargetClassName(node.getType().resolveBinding()),
					cUnit.getLineNumber(node.getStartPosition()), node.getStartPosition(), node.getLength()));
			break;
		case ASTNode.METHOD_DECLARATION:
			this.dependencies.add(new CreateDependency(this.className, this.getTargetClassName(node.getType().resolveBinding()),
					cUnit.getLineNumber(node.getStartPosition()), node.getStartPosition(), node.getLength()));
			break;
		case ASTNode.INITIALIZER:
			this.dependencies
					.add(new CreateDependency(this.className, this.getTargetClassName(node.getType().resolveBinding()), cUnit
							.getLineNumber(node.getStartPosition()), node.getStartPosition(), node.getLength()));
			break;
		}

		return true;
	}

	@Override
	public boolean visit(FieldDeclaration node) {
		this.dependencies.add(new DeclareDependency(this.className, this.getTargetClassName(node.getType().resolveBinding()),
				cUnit.getLineNumber(node.getType().getStartPosition()), node.getType().getStartPosition(), node.getType().getLength()));
		return true;
	}

	@Override
	public boolean visit(MethodDeclaration node) {
		for (Object o : node.parameters()) {
			if (o instanceof SingleVariableDeclaration) {
				SingleVariableDeclaration svd = (SingleVariableDeclaration) o;
				this.dependencies.add(new DeclareDependency(this.className, this
						.getTargetClassName(svd.getType().resolveBinding()), cUnit.getLineNumber(svd.getStartPosition()), svd
						.getStartPosition(), svd.getLength()));
				if (svd.getType().getNodeType() == Type.PARAMETERIZED_TYPE) {
					// TODO: Adjust the way that we handle parameter types
					for (Object t : ((ParameterizedType) svd.getType()).typeArguments()) {
						if (t instanceof SimpleType) {
							SimpleType st = (SimpleType) t;
							this.dependencies.add(new DeclareDependency(this.className, this.getTargetClassName(st
									.resolveBinding()), cUnit.getLineNumber(st.getStartPosition()), st.getStartPosition(), st
									.getLength()));
						} else if (t instanceof ParameterizedType) {
							ParameterizedType pt = (ParameterizedType) t;
							this.dependencies.add(new DeclareDependency(this.className, this.getTargetClassName(pt.getType()
									.resolveBinding()), cUnit.getLineNumber(pt.getStartPosition()), pt.getStartPosition(), pt
									.getLength()));
						}
					}
				}

			}
		}
		for (Object o : node.thrownExceptions()) {
			Name name = (Name) o;
			this.dependencies.add(new ThrowDependency(this.className, this.getTargetClassName(name.resolveTypeBinding()), cUnit
					.getLineNumber(name.getStartPosition()), name.getStartPosition(), name.getLength(), node.getName().getIdentifier()));
		}

		if (node.getReturnType2() != null
				&& !(node.getReturnType2().isPrimitiveType() && ((PrimitiveType) node.getReturnType2()).getPrimitiveTypeCode() == PrimitiveType.VOID)) {
			if (!node.getReturnType2().resolveBinding().isTypeVariable()) {
				this.dependencies.add(new DeclareDependency(this.className, this.getTargetClassName(node.getReturnType2()
						.resolveBinding()), cUnit.getLineNumber(node.getReturnType2().getStartPosition()), node.getReturnType2()
						.getStartPosition(), node.getReturnType2().getLength()));
			} else {
				if (node.getReturnType2().resolveBinding().getTypeBounds().length >= 1) {
					this.dependencies.add(new DeclareDependency(this.className, this.getTargetClassName(node.getReturnType2()
							.resolveBinding().getTypeBounds()[0]), cUnit.getLineNumber(node.getReturnType2().getStartPosition()), node
							.getReturnType2().getStartPosition(), node.getReturnType2().getLength()));
				}
			}

		}
		return true;
	}

	@Override
	public boolean visit(VariableDeclarationStatement node) {
		ASTNode relevantParent = getRelevantParent(node);

		switch (relevantParent.getNodeType()) {
		case ASTNode.METHOD_DECLARATION:

			this.dependencies.add(new DeclareDependency(this.className, this.getTargetClassName(node.getType()
					.resolveBinding()), cUnit.getLineNumber(node.getStartPosition()), node.getType().getStartPosition(), node.getType()
					.getLength()));

			break;
		case ASTNode.INITIALIZER:
			this.dependencies.add(new DeclareDependency(this.className, this.getTargetClassName(node.getType()
					.resolveBinding()), cUnit.getLineNumber(node.getStartPosition()), node.getType().getStartPosition(), node.getType()
					.getLength()));
			break;
		}

		return true;
	}
	
	@Override
	public boolean visit(VariableDeclarationExpression node) {
		this.dependencies.add(new DeclareDependency(this.className, this.getTargetClassName(node.getType()
				.resolveBinding()), cUnit.getLineNumber(node.getStartPosition()), node.getType().getStartPosition(), node.getType()
				.getLength()));
		return true;
	}

	@Override
	public boolean visit(MethodInvocation node) {
		ASTNode relevantParent = getRelevantParent(node);

		switch (relevantParent.getNodeType()) {
		case ASTNode.METHOD_DECLARATION:
			if (node.getExpression() != null) {
				this.dependencies.add(new AccessDependency(this.className, this.getTargetClassName(node.getExpression()
						.resolveTypeBinding()), cUnit.getLineNumber(node.getStartPosition()), node.getStartPosition(),
						node.getLength()));
			}
			break;
		case ASTNode.INITIALIZER:
			if (node.getExpression() != null) {
				this.dependencies.add(new AccessDependency(this.className, this.getTargetClassName(node.getExpression()
						.resolveTypeBinding()), cUnit.getLineNumber(node.getStartPosition()), node.getStartPosition(),
						node.getLength()));
			}
			break;
		}
		return true;
	}

	@Override
	public boolean visit(FieldAccess node) {
		ASTNode relevantParent = getRelevantParent(node);

		switch (relevantParent.getNodeType()) {
		case ASTNode.METHOD_DECLARATION:
			this.dependencies.add(new AccessDependency(this.className, this.getTargetClassName(node.getExpression()
					.resolveTypeBinding()), cUnit.getLineNumber(node.getStartPosition()), node.getStartPosition(), node.getLength()));
			break;
		case ASTNode.INITIALIZER:			
			this.dependencies.add(new AccessDependency(this.className, this.getTargetClassName(node.getExpression()
					.resolveTypeBinding()), cUnit.getLineNumber(node.getStartPosition()), node.getStartPosition(), node.getLength()));
			break;
		}
		return true;
	}

	@Override
	public boolean visit(QualifiedName node) {
		if ((node.getParent().getNodeType() == ASTNode.METHOD_INVOCATION || node.getParent().getNodeType() == ASTNode.INFIX_EXPRESSION
				|| node.getParent().getNodeType() == ASTNode.VARIABLE_DECLARATION_FRAGMENT || node.getParent().getNodeType() == ASTNode.ASSIGNMENT)
				&& node.getQualifier().getNodeType() != ASTNode.QUALIFIED_NAME) {
			ASTNode relevantParent = getRelevantParent(node);
			
			switch (relevantParent.getNodeType()) {
			case ASTNode.METHOD_DECLARATION:
				this.dependencies.add(new AccessDependency(this.className, this.getTargetClassName(node.getQualifier()
						.resolveTypeBinding()), cUnit.getLineNumber(node.getStartPosition()), node.getStartPosition(),
						node.getLength()));
				break;
			case ASTNode.INITIALIZER:
				this.dependencies.add(new AccessDependency(this.className, this.getTargetClassName(node.getQualifier()
						.resolveTypeBinding()), cUnit.getLineNumber(node.getStartPosition()), node.getStartPosition(),
						node.getLength()));
				break;
			}

		}

		return true;
	}

	@Override
	public boolean visit(AnnotationTypeDeclaration node) {
		return super.visit(node);
	}

	@Override
	public boolean visit(AnnotationTypeMemberDeclaration node) {
		return super.visit(node);
	}

	/*tirar duvida de quando entraria*/
	public boolean visit(org.eclipse.jdt.core.dom.NormalAnnotation node) {
		if (node.getParent().getNodeType() == ASTNode.FIELD_DECLARATION) {
			this.dependencies.add(new AnnotateDependency(this.className, node.getTypeName().resolveTypeBinding().getQualifiedName(),
					cUnit.getLineNumber(node.getStartPosition()), node.getStartPosition(), node.getLength()));
		} else if (node.getParent().getNodeType() == ASTNode.METHOD_DECLARATION) {
			this.dependencies.add(new AnnotateDependency(this.className, node.getTypeName().resolveTypeBinding().getQualifiedName(),
					cUnit.getLineNumber(node.getStartPosition()), node.getStartPosition(), node.getLength()));
		} else if (node.getParent().getNodeType() == ASTNode.TYPE_DECLARATION) {
			this.dependencies.add(new AnnotateDependency(this.className, node.getTypeName().resolveTypeBinding().getQualifiedName(),
					cUnit.getLineNumber(node.getStartPosition()), node.getStartPosition(), node.getLength()));
		}
		return true;
	};

	@Override
	public boolean visit(ParameterizedType node) {
		ASTNode relevantParent = this.getRelevantParent(node);
		if (node.getNodeType() == ASTNode.PARAMETERIZED_TYPE) {
			ParameterizedType pt = (ParameterizedType) node;
			if (pt.typeArguments() != null) {
				for (Object o : pt.typeArguments()) {
					Type t = (Type) o;
					if (relevantParent.getNodeType() == ASTNode.METHOD_DECLARATION) {
						this.dependencies.add(new DeclareDependency(this.className, this.getTargetClassName(t
								.resolveBinding()), cUnit.getLineNumber(t.getStartPosition()), t.getStartPosition(), t.getLength()));
					}else{
						this.dependencies.add(new DeclareDependency(this.className, this.getTargetClassName(t
								.resolveBinding()), cUnit.getLineNumber(t.getStartPosition()), t.getStartPosition(), t.getLength()));
					}
				}
			}
		}
		return true;
	}

	private ASTNode getRelevantParent(final ASTNode node) {
		for (ASTNode aux = node; aux != null; aux = aux.getParent()) {
			switch (aux.getNodeType()) {
			case ASTNode.FIELD_DECLARATION:
			case ASTNode.METHOD_DECLARATION:
			case ASTNode.INITIALIZER:
				return aux;
			}
		}
		return node;
	}

	private String getTargetClassName(ITypeBinding type) {
		String result = "";
		if (!type.isAnonymous() && type.getQualifiedName() != null && !type.getQualifiedName().isEmpty()) {
			result = type.getQualifiedName();
		} else if (type.isLocal() && type.getName() != null && !type.getName().isEmpty()) {
			result = type.getName();
		} else if (!type.getSuperclass().getQualifiedName().equals("java.lang.Object") || type.getInterfaces() == null
				|| type.getInterfaces().length == 0) {
			result = type.getSuperclass().getQualifiedName();
		} else if (type.getInterfaces() != null && type.getInterfaces().length == 1) {
			result = type.getInterfaces()[0].getQualifiedName();
		}

		if (result.equals("")) {
			throw new RuntimeException("AST Parser error.");
		} else if (result.endsWith("[]")) {
			result = result.substring(0, result.length() - 2);
		} else if (result.matches(".*<.*>")) {
			result = result.replaceAll("<.*>", "");
		}

		return result;
	}
	
	public CompilationUnit getcUnit() {
		return cUnit;
	}
	
	public ITypeBinding getITypeBinding() {
		return typeBinding;
	}
}
