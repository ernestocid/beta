package parser.decorators.predicates;

import java.util.HashSet;
import java.util.Set;
import parser.decorators.expressions.MyExpression;
import parser.decorators.expressions.MyExpressionFactory;
import de.be4.classicalb.core.parser.node.ASubsetPredicate;
import de.be4.classicalb.core.parser.node.PPredicate;

public class MyASubsetPredicate extends MyPredicateDecorator{

	
	private ASubsetPredicate subsetPredicate;
	private MyExpression leftExpression;
	private MyExpression rightExpression;

	
	public MyASubsetPredicate(ASubsetPredicate subsetPredicate) {
		this.subsetPredicate = subsetPredicate;
		this.leftExpression = MyExpressionFactory.convertExpression(subsetPredicate.getLeft());
		this.rightExpression = MyExpressionFactory.convertExpression(subsetPredicate.getRight());
	}
	
	
	
	public MyExpression getLeftExpression() {
		return this.leftExpression;
	}
	
	
	
	public MyExpression getRightExpression() {
		return this.rightExpression;
	}
	
	
	
	@Override
	public PPredicate getNode() {
		return this.subsetPredicate;
	}
	
	
	
	@Override
	public String toString() {
		return getLeftExpression().toString() + " <: " + getRightExpression().toString();
	}

	
	
	@Override
	public void createClausesList(Set<MyPredicate> clauses) {
		clauses.add(this);
	}

	
	
	@Override
	public Set<String> getVariables() {
		HashSet<String> variables = new HashSet<String>();
		variables.addAll(getLeftExpression().getVariables());
		variables.addAll(getRightExpression().getVariables());
		return variables;
	}
	
	
	
	@Override
	public boolean isTypingClause() {
		if(getRightExpression().isBasicType()) {
			return true;
		} else {
			return false;
		}
	}

	
	
	@Override
	public boolean isInterval() {
		if (getRightExpression().isInterval()) {
			return true;
		} else {
			return false;
		}
	}
	
}