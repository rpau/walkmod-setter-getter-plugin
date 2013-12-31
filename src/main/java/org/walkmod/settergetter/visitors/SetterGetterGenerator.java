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

import java.util.Collection;
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
import org.walkmod.javalang.ast.body.ModifierSet;
import org.walkmod.javalang.ast.body.Parameter;
import org.walkmod.javalang.ast.body.TypeDeclaration;
import org.walkmod.javalang.ast.body.VariableDeclarator;
import org.walkmod.javalang.visitors.VoidVisitorAdapter;
import org.walkmod.walkers.VisitorContext;

public class SetterGetterGenerator extends VoidVisitorAdapter<VisitorContext> {

	private CompilationUnit cu;
	
	public void visit(CompilationUnit cu, VisitorContext arg){

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
			
			Collection<FieldDeclaration> fields = ASTManager.getFields(n);
			if (fields != null) {

				arg.addResultNode(cu);
				for (FieldDeclaration fd : fields) {
					List<VariableDeclarator> variables = fd.getVariables();
					for (VariableDeclarator vd : variables) {
						String fieldName = vd.getId().getName();
						Parameter parameter = ASTManager.createParameter(
								fd.getType(), fieldName);
						try {
							ASTManager.addMethodDeclaration(coid,
									ModifierSet.PUBLIC, ASTManager.VOID_TYPE,
									"set" + WordUtils.capitalize(fieldName),
									parameter, "{ this." + fieldName + " = "
											+ fieldName + "; }");
							ASTManager.addMethodDeclaration(coid,
									ModifierSet.PUBLIC, fd.getType(), "get"
											+ WordUtils.capitalize(fieldName),
									parameter, "{return " + fieldName + ";}");
						} catch (ParseException e1) {
							throw new WalkModException(e1);
						}
					}
				}
			}
		}
	}
}
