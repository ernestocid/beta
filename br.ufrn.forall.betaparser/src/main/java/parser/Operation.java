package parser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import parser.decorators.expressions.MyAEitherCaseExpression;
import parser.decorators.expressions.MyExpression;
import parser.decorators.expressions.MyExpressionFactory;
import parser.decorators.predicates.MyPredicate;
import parser.decorators.predicates.MyPredicateFactory;
import de.be4.classicalb.core.parser.node.AAnySubstitution;
import de.be4.classicalb.core.parser.node.AAssertionSubstitution;
import de.be4.classicalb.core.parser.node.ACaseSubstitution;
import de.be4.classicalb.core.parser.node.AIdentifierExpression;
import de.be4.classicalb.core.parser.node.AIfElsifSubstitution;
import de.be4.classicalb.core.parser.node.AIfSubstitution;
import de.be4.classicalb.core.parser.node.AOperation;
import de.be4.classicalb.core.parser.node.AParallelSubstitution;
import de.be4.classicalb.core.parser.node.APreconditionSubstitution;
import de.be4.classicalb.core.parser.node.ASelectSubstitution;
import de.be4.classicalb.core.parser.node.ASelectWhenSubstitution;
import de.be4.classicalb.core.parser.node.PExpression;
import de.be4.classicalb.core.parser.node.PPredicate;
import de.be4.classicalb.core.parser.node.PSubstitution;

public class Operation {

	
	private AOperation operation;
	private Machine machine;

	
	public Operation(AOperation operation, Machine machine) {
		this.operation = operation;
		this.machine = machine;
	}
	
	
	
	public AOperation getNode() {
		return operation;
	}

	
	
	public Machine getMachine() {
		return machine;
	}

	
	
	public void setMachine(Machine machine) {
		this.machine = machine;
	}
	
	
	
	public String getName() {
		return operation.getOpName().getFirst().getText();
	}

	
	
	public MyPredicate getPrecondition() {
		PSubstitution operationBody = operation.getOperationBody();
		if(operationBody instanceof APreconditionSubstitution) {
			APreconditionSubstitution precondition = (APreconditionSubstitution) operationBody;
			MyPredicate predicate = MyPredicateFactory.convertPredicate(precondition.getPredicate());
			return predicate;
		}
		return null;
	}
	
	
	
	public List<String> getPreconditionClausesList() {
		MyPredicate precondition = getPrecondition();
		List<String> preconditionStringList = new ArrayList<String>();

		Set<MyPredicate> clauses = new HashSet<MyPredicate>();
		precondition.createClausesList(clauses);
		
		for (MyPredicate clause : clauses) {
			preconditionStringList.add(clause.toString());
		}
		
		Collections.sort(preconditionStringList);
		return preconditionStringList;
	}
	
	
	
	public Set<MyPredicate> getPreconditionClauses() {
		Set<MyPredicate> clauses = new HashSet<MyPredicate>();
		
		if(getPrecondition() != null) {
			getPrecondition().createClausesList(clauses);
		}
		
		return clauses;
	}

	
	
	public List<String> getParameters() {
		List<String> parameters = createParametersList();
		return parameters;
	}
	
	
	
	private List<String> createParametersList() {
		LinkedList<PExpression> expressions = operation.getParameters();
		List<String> parameters = new ArrayList<String>();
		for (PExpression expression : expressions) {
			if(expression instanceof AIdentifierExpression) {
				AIdentifierExpression id = (AIdentifierExpression) expression;
				parameters.add(id.getIdentifier().getFirst().getText());
			}
		}
		return parameters;
	}


	
	public Set<Characteristic> getConditionalCharacteristics() {
		Set<Characteristic> conditions = new HashSet<Characteristic>();
		PSubstitution bodyInsides = getBodyInsides(operation.getOperationBody());
		
		if (bodyInsides instanceof AParallelSubstitution) {
			AParallelSubstitution parallel = (AParallelSubstitution) bodyInsides;
			for (PSubstitution substitution : parallel.getSubstitutions()) {
				conditions.addAll(getConditionsFromConditionals(substitution));
			}
		} else {
			conditions.addAll(getConditionsFromConditionals(bodyInsides));
		}
				
		return conditions;
	}
	
	

	private Set<Characteristic> getConditionsFromConditionals(PSubstitution substitution) {
		Set<Characteristic> conditions = new HashSet<Characteristic>();
		
		if (substitution instanceof AIfSubstitution) {
			AIfSubstitution ifSubstitution = (AIfSubstitution) substitution;
			addIfElseConditions(conditions, ifSubstitution);
		} else if (substitution instanceof ACaseSubstitution) {
			ACaseSubstitution caseSubstitution = (ACaseSubstitution) substitution;
			addCaseOrConditions(conditions, caseSubstitution);
		} else if (substitution instanceof ASelectSubstitution) {
			ASelectSubstitution selectSubstitution = (ASelectSubstitution) substitution;
			addSelectWhenConditions(conditions, selectSubstitution);
		}
		
		return conditions;
	}

	
	
	private void addSelectWhenConditions(Set<Characteristic> conditions, ASelectSubstitution selectSubstitution) {
		String selectCondition = MyPredicateFactory.convertPredicate(selectSubstitution.getCondition()).toString();
		
		Characteristic characteristic = new PredicateCharacteristic(MyPredicateFactory.convertPredicate(selectSubstitution.getCondition()), CharacteristicType.CONDITIONAL);
		conditions.add(characteristic);
//		conditions.add(selectCondition);
		addWhenConditions(conditions, selectSubstitution);
	}

	
	
	private void addWhenConditions(Set<Characteristic> conditions, ASelectSubstitution selectSubstitution) {
		for (PSubstitution subs : selectSubstitution.getWhenSubstitutions()) {
			if (subs instanceof ASelectWhenSubstitution) {
				ASelectWhenSubstitution when = (ASelectWhenSubstitution) subs;
				String whenText = MyPredicateFactory.convertPredicate(when.getCondition()).toString();
				Characteristic characteristic = new PredicateCharacteristic(MyPredicateFactory.convertPredicate(when.getCondition()), CharacteristicType.CONDITIONAL);
				conditions.add(characteristic);
//				conditions.add(whenText);
			}
			
		}
	}

	
	
	private void addCaseOrConditions(Set<Characteristic> conditions, ACaseSubstitution caseSubstitution) {
		MyExpression caseExpression = MyExpressionFactory.convertExpression(caseSubstitution.getExpression());
		String caseVar = caseExpression.toString();
		LinkedList<PExpression> eitherExpr = caseSubstitution.getEitherExpr();
		
		for (PExpression exp : eitherExpr) {
			MyExpression eitherExpression = MyExpressionFactory.convertExpression(exp);
			Characteristic ch = new ExpressionCharacteristic(new MyAEitherCaseExpression(eitherExpression, caseVar), CharacteristicType.CONDITIONAL);
			conditions.add(ch);
		}
	}

	
	
	private void addIfElseConditions(Set<Characteristic> conditions, AIfSubstitution ifSubstitution) {
		Characteristic characteristic = new PredicateCharacteristic(MyPredicateFactory.convertPredicate(ifSubstitution.getCondition()), CharacteristicType.CONDITIONAL);
		
//		String ifCondition = MyPredicateFactory.convertPredicate(ifSubstitution.getCondition()).toString();
		conditions.add(characteristic);
		addElseConditions(conditions, ifSubstitution);
	}

	
	
	private void addElseConditions(Set<Characteristic> conditions, AIfSubstitution ifSubstitution) {
		for (PSubstitution substitution : ifSubstitution.getElsifSubstitutions()) {
			if(substitution instanceof AIfElsifSubstitution) {
				AIfElsifSubstitution elsifSubstitution = (AIfElsifSubstitution) substitution;
				Characteristic characteristic = new PredicateCharacteristic(MyPredicateFactory.convertPredicate(elsifSubstitution.getCondition()), CharacteristicType.CONDITIONAL);
//				conditions.add(MyPredicateFactory.convertPredicate(elsifSubstitution.getCondition()).toString());
				conditions.add(characteristic);
			}
		}
	}

	
	
	private PSubstitution getBodyInsides(PSubstitution operationBody) {
		if(operationBody instanceof APreconditionSubstitution) {
			APreconditionSubstitution precondition = (APreconditionSubstitution) operationBody;
			return precondition.getSubstitution();
		} else {
			return operationBody;
		}
	}

	
	
	public List<String> getReturnVariables() {
		List<String> returnVariables = new ArrayList<String>();
		
		for(PExpression varExpression : operation.getReturnValues()) {
			MyExpression myExpression = MyExpressionFactory.convertExpression(varExpression);
			returnVariables.add(myExpression.toString());
		}
		
		return returnVariables;
	}

	
	
	public Set<String> getIfCommandVariables() {
		Set<String> ifVariables = new HashSet<String>();
		PSubstitution bodyInsides = getBodyInsides(operation.getOperationBody());
		
		getIfCommandVariablesHelper(ifVariables, bodyInsides);
		
		// Remove possible constants like set values
		ifVariables.removeAll(this.machine.getConstantValues());
		
		return ifVariables;
	}



	private void getIfCommandVariablesHelper(Set<String> ifVariables, PSubstitution bodyInsides) {
		if (bodyInsides instanceof AIfSubstitution) {
			AIfSubstitution ifSubstitution = (AIfSubstitution) bodyInsides;
			ifVariables.addAll(getIfCmdVariablesHelper(ifSubstitution));
			
			for (PSubstitution subs : ifSubstitution.getElsifSubstitutions()) {
				if (subs instanceof AIfElsifSubstitution) {
					AIfElsifSubstitution ifElsif = (AIfElsifSubstitution) subs;
					ifVariables.addAll(MyPredicateFactory.convertPredicate(ifElsif.getCondition()).getVariables());
				}
			}
			
		} else if (bodyInsides instanceof ACaseSubstitution) {
			ACaseSubstitution caseSubs = (ACaseSubstitution) bodyInsides;
			MyExpression caseExp = MyExpressionFactory.convertExpression(caseSubs.getExpression());
			ifVariables.add(caseExp.toString());
		} else if (bodyInsides instanceof ASelectSubstitution) {
			ASelectSubstitution selectSubs = (ASelectSubstitution) bodyInsides;
			ifVariables.addAll(MyPredicateFactory.convertPredicate(selectSubs.getCondition()).getVariables());
			
			for (PSubstitution subs : selectSubs.getWhenSubstitutions()) {
				if (subs instanceof ASelectWhenSubstitution) {
					ASelectWhenSubstitution when = (ASelectWhenSubstitution) subs;
					ifVariables.addAll(MyPredicateFactory.convertPredicate(when.getCondition()).getVariables());
				}
			}
		}
	}

	
	private Set<String> getIfCmdVariablesHelper(AIfSubstitution ifSubstitution) {
		if(ifSubstitution.getThen() instanceof AIfSubstitution) {
			Set<String> ifVariables = new HashSet<String>();
			ifVariables.addAll(getIfCmdVariablesHelper((AIfSubstitution) ifSubstitution.getThen()));
			ifVariables.addAll(MyPredicateFactory.convertPredicate(ifSubstitution.getCondition()).getVariables());
			return ifVariables;
		} else {
			Set<String> ifVariables = new HashSet<String>();
			ifVariables.addAll(MyPredicateFactory.convertPredicate(ifSubstitution.getCondition()).getVariables());
			return ifVariables;
		}
	}
	

	
	public Set<String> getStateVariablesUsedOnBody() {
		Set<String> usedStateVariablesOnBody = new HashSet<String>();
		
		Variables variables = getMachine().getVariables();
		
		if(variables != null) {
			for(String variable : variables.getAll()) {
				Pattern pattern = Pattern.compile("\\b"+variable+"\\b");
				String matches = matchesPattern(pattern, this.operation.toString());
				
				if(matches != null) {
					usedStateVariablesOnBody.add(matches);
				}
			}
		}
		
		return usedStateVariablesOnBody;
	}
	
	
	
	private  String matchesPattern(Pattern p,String sentence) {
		Matcher m = p.matcher(sentence);

		if (m.find()) {
			return m.group();
		}

		return null;
	}

	
	
	/**
	 * This method creates a list containing predicates from all substitutions
	 * present on the body of the operation. It recursively searches for substitutions
	 * nested inside other substitutions. The type of substitutions it searches for are:
	 *		- IF-ELSIF-ELSE substitution;
	 *		- ANY substitution;
	 *		- SELECT-WHEN substitution;
	 *		- ASSERT substitution;
	 *		- CASE-EITHER-OR substitution
	 * 
	 * @return
	 */
	public List<MyPredicate> getPredicatesFromOperationBody() {
		List<MyPredicate> predicates = new ArrayList<MyPredicate>();
		PSubstitution substitution = getBodyInsides(operation.getOperationBody());

		return getPredicatesFromAllSubstitutions(substitution, predicates);
	}



	private List<MyPredicate> getPredicatesFromAllSubstitutions(PSubstitution substitution, List<MyPredicate> predicates) {
		
		if(substitution instanceof AIfSubstitution) {
			
			AIfSubstitution ifSubstitution = (AIfSubstitution) substitution;
			getPredicatesFromIfSubstitution(predicates, ifSubstitution);
			
		} else if(substitution instanceof AIfElsifSubstitution) {
			
			AIfElsifSubstitution ifElsifSubstitution = (AIfElsifSubstitution) substitution;
			getPredicatesFromElsifSubstitution(predicates, ifElsifSubstitution);
			
		} else if(substitution instanceof AAnySubstitution) {
			
			AAnySubstitution anySubstitution = (AAnySubstitution) substitution;
			getPredicatesFromAnySubstitution(predicates, anySubstitution);
			
		} else if (substitution instanceof AAssertionSubstitution) {
			
			AAssertionSubstitution assertionSubstitution = (AAssertionSubstitution) substitution;
			getPredicatesFromAssertSubstitution(predicates, assertionSubstitution);
			
		} else if (substitution instanceof ASelectSubstitution) {
			
			ASelectSubstitution selectSubstitution = (ASelectSubstitution) substitution;
			getPredicatesFromSelectSubstitution(predicates, selectSubstitution);
			
		} if (substitution instanceof ASelectWhenSubstitution) {
			
			ASelectWhenSubstitution selectWhenSubstitution = (ASelectWhenSubstitution) substitution;
			getPredicatesFromSelectWhenSubstitution(predicates, selectWhenSubstitution);
			
		} else if(substitution instanceof ACaseSubstitution) {

			ACaseSubstitution caseSubstitution = (ACaseSubstitution) substitution;
			getPredicatesFromCaseSubstitution(predicates, caseSubstitution);
			
		} else {
			
			return predicates;
			
		}
		
		return predicates;
	}

	
	
	private void getPredicatesFromIfSubstitution(List<MyPredicate> predicates, AIfSubstitution ifSubstitution) {
		MyPredicate ifCondition = MyPredicateFactory.convertPredicate(ifSubstitution.getCondition());
		predicates.add(ifCondition);
		
		for(PSubstitution sub : ifSubstitution.getElsifSubstitutions()) {
			getPredicatesFromAllSubstitutions(sub, predicates);
		}
		
		getPredicatesFromAllSubstitutions(ifSubstitution.getElse(), predicates);
	}
	
	
	
	private void getPredicatesFromElsifSubstitution(List<MyPredicate> predicates, AIfElsifSubstitution ifElsifSubstitution) {
		MyPredicate elsifPredicate = MyPredicateFactory.convertPredicate(ifElsifSubstitution.getCondition());
		predicates.add(elsifPredicate);
		
		getPredicatesFromAllSubstitutions(ifElsifSubstitution.getThenSubstitution(), predicates);
	}
	
	
	
	private void getPredicatesFromAnySubstitution(List<MyPredicate> predicates, AAnySubstitution anySubstitution) {
		MyPredicate anyPredicate = MyPredicateFactory.convertPredicate(anySubstitution.getWhere());
		predicates.add(anyPredicate);
		
		getPredicatesFromAllSubstitutions(anySubstitution.getThen(), predicates);
	}
	
	
	
	private void getPredicatesFromAssertSubstitution(List<MyPredicate> predicates, AAssertionSubstitution assertionSubstitution) {
		MyPredicate assertionPredicate = MyPredicateFactory.convertPredicate(assertionSubstitution.getPredicate());
		predicates.add(assertionPredicate);
		
		getPredicatesFromAllSubstitutions(assertionSubstitution.getSubstitution(), predicates);
	}
	
	
	
	private void getPredicatesFromSelectSubstitution(List<MyPredicate> predicates, ASelectSubstitution selectSubstitution) {
		MyPredicate selectPredicate = MyPredicateFactory.convertPredicate(selectSubstitution.getCondition());
		predicates.add(selectPredicate);
		
		for (PSubstitution subs : selectSubstitution.getWhenSubstitutions()) {
			getPredicatesFromAllSubstitutions(subs, predicates);
		}
		
		getPredicatesFromAllSubstitutions(selectSubstitution.getThen(), predicates);
	}



	private void getPredicatesFromSelectWhenSubstitution(List<MyPredicate> predicates, ASelectWhenSubstitution selectWhenSubstitution) {
		MyPredicate selectWhenCondition = MyPredicateFactory.convertPredicate(selectWhenSubstitution.getCondition());
		predicates.add(selectWhenCondition);
		
		getPredicatesFromAllSubstitutions(selectWhenSubstitution.getSubstitution(), predicates);
	}

	
	
	private void getPredicatesFromCaseSubstitution(List<MyPredicate> predicates, ACaseSubstitution caseSubstitution) {
		predicates.addAll(getEitherGuards(caseSubstitution));
		getPredicatesFromAllSubstitutions(caseSubstitution.getEitherSubst(), predicates);
		
		predicates.addAll(getOrGuards(caseSubstitution));
		
		for(PSubstitution orSubs : caseSubstitution.getOrSubstitutions()) {
			getPredicatesFromAllSubstitutions(orSubs, predicates);
		}
		
		getPredicatesFromAllSubstitutions(caseSubstitution.getElse(), predicates);
	}
	
	
	
	private List<MyPredicate> getOrGuards(ACaseSubstitution caseSubstitution) {
		List<MyPredicate> predicates = new ArrayList<MyPredicate>();
		MyExpression caseExpression = MyExpressionFactory.convertExpression(caseSubstitution.getExpression());
		
		for(PSubstitution orSubs : caseSubstitution.getOrSubstitutions()) {
			MyExpression orExpression = createOrExpressionFromOrSubstitution(orSubs);
			predicates.add(createPredicateForCase(caseExpression, orExpression));
		}
		
		return predicates;
	}



	private List<MyPredicate> getEitherGuards(ACaseSubstitution caseSubstitution) {
		List<MyPredicate> predicates = new ArrayList<MyPredicate>();
		MyExpression caseExpression = MyExpressionFactory.convertExpression(caseSubstitution.getExpression());
		
		for(PExpression eitherExpr : caseSubstitution.getEitherExpr()) {
			MyExpression eitherExpression = MyExpressionFactory.convertExpression(eitherExpr);
			predicates.add(createPredicateForCase(caseExpression, eitherExpression));
		}
		
		return predicates;
	}



	private MyPredicate createPredicateForCase(MyExpression caseExpression, MyExpression eitherOrExpression) {
		final MyExpression caseExp = caseExpression;
		final MyExpression eitherOrExp = eitherOrExpression;
		
		MyPredicate casePredicate = new MyPredicate() {

			@Override
			public PPredicate getNode() {	return null;	}
			@Override
			public boolean isTypingClause() {	return false;	}
			@Override
			public boolean isInterval() {	return false;	}
			
			@Override
			public Set<String> getVariables() {
				Set<String> variables = new HashSet<String>();
				variables.add(caseExp.toString());
				return variables;
			}
			
			@Override
			public void createClausesList(Set<MyPredicate> clauses) {
				clauses.add(this);
			}
			
			@Override
			public String toString() {
				return caseExp.toString() + " = " + eitherOrExp.toString();
			}
		};
		
		return casePredicate;
	}



	private MyExpression createOrExpressionFromOrSubstitution(PSubstitution orSubstitution) {
		final PSubstitution orSubs = orSubstitution;
		
		MyExpression orExpression = new MyExpression() {
			
			@Override
			public PExpression getNode() {	return null;	}
			@Override
			public boolean isInterval() {	return false;	}
			@Override
			public boolean isBasicType() {	return false;	}
			
			@Override
			public Set<String> getVariables() {
				Set<String> variables = new HashSet<String>();
				String variable = getOrExpression(orSubs);
				
				variables.add(variable);
				
				return variables;
			}

			private String getOrExpression(final PSubstitution orSubs) {
				int indexOfFirtWhitespace = orSubs.toString().indexOf(" ");
				String variable = orSubs.toString().substring(0, indexOfFirtWhitespace);
				return variable;
			}
			
			@Override
			public String toString() {
				return getOrExpression(orSubs);
			}
		};
		
		return orExpression;
	}
	
}
