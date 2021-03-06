package parser.decorators.predicates;

import java.util.HashSet;
import java.util.Set;

import de.be4.classicalb.core.parser.node.AConjunctPredicate;
import de.be4.classicalb.core.parser.node.PPredicate;

public class MyAConjunctPredicate extends MyPredicateDecorator {

	
	private AConjunctPredicate conjunctPredicate;

	
	public MyAConjunctPredicate(AConjunctPredicate conjunctPredicate) {
		this.conjunctPredicate = conjunctPredicate;
	}

	
	
	public MyPredicate getLeft() {
		return MyPredicateFactory.convertPredicate(conjunctPredicate.getLeft());
	}
	
	
	
	public MyPredicate getRight() {
		return MyPredicateFactory.convertPredicate(conjunctPredicate.getRight());
	}

	
	
	@Override
	public PPredicate getNode() {
		return conjunctPredicate;
	}
	
	
	
	@Override
	public String toString() {
		return getLeft().toString() + " & " + getRight();
	}

	
	
	@Override
	public Set<MyPredicate> getClauses() {
		Set<MyPredicate> clauses = new HashSet<MyPredicate>();
		
		clauses.addAll(getLeft().getClauses());
		clauses.addAll(getRight().getClauses());
		
		return clauses;
	}
	
	
	
	@Override
	public Set<String> getVariables() {
		Set<String> variables = new HashSet<String>();
		variables.addAll(getLeft().getVariables());
		variables.addAll(getRight().getVariables());
		return variables;
	}
	
	
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof MyAConjunctPredicate) {
			MyAConjunctPredicate predicate = (MyAConjunctPredicate) obj;
			if(predicate.toString().equals(this.toString())) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
	
	
	
	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}
}
