package org.walkmod.settergetter.visitors;

import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;
import org.walkmod.javalang.ASTManager;
import org.walkmod.javalang.ast.CompilationUnit;
import org.walkmod.javalang.ast.body.BodyDeclaration;
import org.walkmod.javalang.ast.body.MethodDeclaration;
import org.walkmod.javalang.ast.body.TypeDeclaration;
import org.walkmod.walkers.VisitorContext;

public class SetterGetterGeneratorTest {

	@Test
	public void testFinalFieldsDoNotHaveSetter() throws Exception {
		SetterGetterGenerator ew = new SetterGetterGenerator();
		VisitorContext ctx = new VisitorContext();
		String code = "class Foo{ private final String bar =\"name\"; }"; 
		CompilationUnit cu = ASTManager.parse(code);
		ew.visit(cu, ctx);
		Collection resultNodes = ctx.getResultNodes();
		CompilationUnit generated = (CompilationUnit) resultNodes.iterator().next();
		
		TypeDeclaration type = generated.getTypes().get(0);
		Assert.assertEquals(1, type.getMembers().size());
		
		BodyDeclaration bd = type.getMembers().get(0);
		
		Assert.assertEquals(MethodDeclaration.class, bd.getClass());
		
		Assert.assertEquals(true, ((MethodDeclaration)bd).getName().startsWith("get"));
	}
	
	@Test
	public void testBooleanGettersStartsWithIs() throws Exception{
		SetterGetterGenerator ew = new SetterGetterGenerator();
		VisitorContext ctx = new VisitorContext();
		String code = "class Foo { private boolean valid = true; }"; 
		CompilationUnit cu = ASTManager.parse(code);
		ew.visit(cu, ctx);
		Collection resultNodes = ctx.getResultNodes();
		CompilationUnit generated = (CompilationUnit) resultNodes.iterator().next();
		
		TypeDeclaration type = generated.getTypes().get(0);
		Assert.assertEquals(2, type.getMembers().size());
		
		BodyDeclaration bd = type.getMembers().get(1);
		
		Assert.assertEquals(MethodDeclaration.class, bd.getClass());
		
		Assert.assertEquals(true, ((MethodDeclaration)bd).getName().startsWith("is"));
	}
}
