package ast;

import java.util.Stack;

import models.Method;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;

import impact.Resources;

import db.DatabaseConnector;

public class Visitor extends ASTVisitor {

	private Stack<Integer> methodStack;
	private String file;
	CompilationUnit cu;
	DatabaseConnector db;

	public Visitor(String file, CompilationUnit cu, DatabaseConnector db) {
		methodStack = new Stack<Integer>();
		this.file = file;
		this.db = db;
		this.cu = cu;
	}

	/**
	 * This function overrides what to do when we reach
	 * a method declaration.
	 */
	@Override
	public boolean visit(MethodDeclaration node) {
		// Insert the method into the DB
		IMethodBinding methodBinding = node.resolveBinding();
		Method method = createMethodFromBinding(node, methodBinding);

		if(method != null) {
			// Insert
			int id = db.insertMethod(method);

			// Push onto stack
			if(id != -1)
				methodStack.push(id);
		}

		return super.visit(node);
	}

	/**
	 * This function overrides what to do when we reach
	 * the end of a method declaration.
	 */
	@Override
	public void endVisit(MethodDeclaration node) {
		if(!methodStack.isEmpty())
			methodStack.pop();
	}

	/**
	 * This function overrides what to do when we reach
	 * a method invocation statement.
	 */
	@Override
	public boolean visit(MethodInvocation node) {
		// Insert the method into the DB
		IMethodBinding methodBinding = node.resolveMethodBinding();
		Method method = createMethodFromBinding(null, methodBinding);

		// Insert
		if(method != null) {
			int id = db.insertMethod(method);
			if(!methodStack.isEmpty())
				db.insertInvokes(methodStack.peek(), id);
		}

		return super.visit(node);
	}

	/**
	 * This function overrides what to do when we reach
	 * a constructor invocation.
	 */
	@Override
	public boolean visit(ClassInstanceCreation node) {
		// Resolve the constructor
		IMethodBinding methodBinding = node.resolveConstructorBinding();
		Method method = createMethodFromBinding(null, methodBinding);

		// Insert
		if(method != null) {
			int id = db.insertMethod(method);
			if(!methodStack.isEmpty())
				db.insertInvokes(methodStack.peek(), id);
		}

		return super.visit(node);
	}

	private Method createMethodFromBinding(MethodDeclaration node, IMethodBinding methodBinding) {
		if(methodBinding != null) {
			Method method = new Method();
			method.setName(methodBinding.getName());
			if(node != null) {
				// Handle working directory
				if(file.startsWith(Resources.repositoryName+"/"))
					method.setFile(file.replaceFirst(Resources.repositoryName+"/", ""));
				else
					method.setFile(file);
				method.setStart(cu.getLineNumber(node.getStartPosition()));
				method.setEnd(cu.getLineNumber(node.getStartPosition() + node.getLength()));
			}

			// Parameters
			ITypeBinding[] parameters = methodBinding.getParameterTypes();
			if(parameters.length > 0) {
				for(int i = 0; i < parameters.length; i++) {
					if(parameters[i] != null)
						method.addParameter(parameters[i].getQualifiedName());
				}
			}

			// Class and Package
			ITypeBinding clazz = methodBinding.getDeclaringClass();
			if(clazz != null) {
				method.setClazz(clazz.getName());

				IPackageBinding pkg = clazz.getPackage();
				if(pkg != null)
					method.setPkg(pkg.getName());
			}

			return method;
		}
		else
			return null;
	}
}