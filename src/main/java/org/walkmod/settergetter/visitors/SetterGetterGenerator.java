/* 
  Copyright (C) 2013 Raquel Pau and Albert Coroleu.
 
 Walkmod is free software: you can redistribute it and/or modify
 it under the terms of the GNU Lesser General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 Walkmod is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Lesser General Public License for more details.
 
 You should have received a copy of the GNU Lesser General Public License
 along with Walkmod.  If not, see <http://www.gnu.org/licenses/>.*/
package org.walkmod.settergetter.visitors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.lang.WordUtils;
import org.walkmod.exceptions.WalkModException;
import org.walkmod.javalang.ASTManager;
import org.walkmod.javalang.ParseException;
import org.walkmod.javalang.ast.CompilationUnit;
import org.walkmod.javalang.ast.body.BodyDeclaration;
import org.walkmod.javalang.ast.body.ClassOrInterfaceDeclaration;
import org.walkmod.javalang.ast.body.FieldDeclaration;
import org.walkmod.javalang.ast.body.MethodDeclaration;
import org.walkmod.javalang.ast.body.ModifierSet;
import org.walkmod.javalang.ast.body.Parameter;
import org.walkmod.javalang.ast.body.TypeDeclaration;
import org.walkmod.javalang.ast.body.VariableDeclarator;
import org.walkmod.javalang.ast.body.VariableDeclaratorId;
import org.walkmod.javalang.ast.stmt.BlockStmt;
import org.walkmod.javalang.ast.type.Type;
import org.walkmod.javalang.comparators.MethodDeclarationComparator;
import org.walkmod.javalang.visitors.VoidVisitorAdapter;
import org.walkmod.merger.CollectionUtil;
import org.walkmod.walkers.VisitorContext;

public class SetterGetterGenerator extends VoidVisitorAdapter<VisitorContext> {

	private CompilationUnit cu;

	public void visit(CompilationUnit cu, VisitorContext arg) {

		this.cu = new CompilationUnit();
		this.cu.setPackage(cu.getPackage());
		super.visit(cu, arg);
	}

	@Override
	public void visit(ClassOrInterfaceDeclaration n, VisitorContext arg) {
		if (!n.isInterface()) {

			ClassOrInterfaceDeclaration coid = new ClassOrInterfaceDeclaration();
			coid.setName(n.getName());
			coid.setModifiers(n.getModifiers());
			coid.setInterface(false);

			List<BodyDeclaration> members = new LinkedList<BodyDeclaration>();
			coid.setMembers(members);
			List<TypeDeclaration> types = new LinkedList<TypeDeclaration>();
			types.add(coid);
			cu.setTypes(types);

			Collection<FieldDeclaration> fields = getFields(n);
			if (fields != null) {

				arg.addResultNode(cu);
				for (FieldDeclaration fd : fields) {
					List<VariableDeclarator> variables = fd.getVariables();
					for (VariableDeclarator vd : variables) {
						String fieldName = vd.getId().getName();
						Parameter parameter = createParameter(
								fd.getType(), fieldName);
						try {
							addMethodDeclaration(coid, ModifierSet.PUBLIC,
									ASTManager.VOID_TYPE,
									"set" + WordUtils.capitalize(fieldName),
									parameter, "{ this." + fieldName + " = "
											+ fieldName + "; }");
							Parameter p = null;
							addMethodDeclaration(coid, ModifierSet.PUBLIC,
									fd.getType(),
									"get" + WordUtils.capitalize(fieldName), p,
									"{return " + fieldName + ";}");
						} catch (ParseException e1) {
							throw new WalkModException(e1);
						}
					}
				}
			}
		}
	}

	private List<FieldDeclaration> getFields(TypeDeclaration td) {
		Collection<BodyDeclaration> members = td.getMembers();
		List<FieldDeclaration> fields = new LinkedList<FieldDeclaration>();
		Iterator<BodyDeclaration> it = members.iterator();
		while (it.hasNext()) {
			BodyDeclaration current = it.next();
			if (current instanceof FieldDeclaration) {
				fields.add((FieldDeclaration) current);
			}
		}
		return fields;
	}

	
	private static void addMember(TypeDeclaration type, BodyDeclaration decl) {
		List<BodyDeclaration> members = type.getMembers();
		if (members == null) {
			members = new ArrayList<BodyDeclaration>();
			type.setMembers(members);
		}
		members.add(decl);
	}

	private void addParameter(MethodDeclaration method, Parameter parameter) {
		List<Parameter> parameters = method.getParameters();
		if (parameters == null) {
			parameters = new ArrayList<Parameter>();
			method.setParameters(parameters);
		}
		parameters.add(parameter);
	}

	private MethodDeclaration getCurrentMethodDeclaration(TypeDeclaration td,
			MethodDeclaration md) {
		List<BodyDeclaration> members = td.getMembers();
		MethodDeclarationComparator mdc = new MethodDeclarationComparator();
		return (MethodDeclaration) CollectionUtil.findObject(members, md, mdc);
	}

	private void addMethodDeclaration(TypeDeclaration td,
			MethodDeclaration method) {
		MethodDeclaration md = getCurrentMethodDeclaration(td, method);
		if (md == null) {
			addMember(td, method);
		}
	}

	private void addMethodDeclaration(TypeDeclaration td, int modifiers,
			Type type, String name, Parameter parameter, String blockStmts)
			throws ParseException {
		MethodDeclaration mtd = new MethodDeclaration(ModifierSet.PUBLIC, type,
				name);
		if (parameter != null) {
			addParameter(mtd, parameter);
		}
		BlockStmt setterBlock = (BlockStmt) ASTManager.parse(BlockStmt.class, blockStmts);
		mtd.setBody(setterBlock);
		addMethodDeclaration(td, mtd);
	}
	

	private Parameter createParameter(Type type, String name) {
		return new Parameter(type, new VariableDeclaratorId(name));
	}


}