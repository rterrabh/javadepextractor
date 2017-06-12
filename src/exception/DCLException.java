package exception;

import org.eclipse.jdt.core.dom.CompilationUnit;

public class DCLException extends Exception {

	private static final long serialVersionUID = 1L;
	private Exception e;
	private CompilationUnit unit;
	
	public DCLException(Exception e, CompilationUnit unit) {
		this.e = e;
		this.unit = unit;
	}
	
	@Override
	public String toString() {
		return "DCLException: " + this.getLocalizedMessage() + "\n" + e.getLocalizedMessage(); 
	}
	
	@Override
	public String getLocalizedMessage() {
		return "Problem detected when visiting " + this.unit.toString();
	}
	
	
	@Override
	public StackTraceElement[] getStackTrace() {
		return this.e.getStackTrace();
	}
	
}
