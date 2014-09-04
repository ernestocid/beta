package criteria;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import parser.Operation;
import parser.decorators.predicates.MyPredicate;

public class ActiveClauseCoverage extends LogicalCoverage {

	
	public static final String TRUE = "1=1";
	public static final String FALSE = "1=2";
	
	
	public ActiveClauseCoverage(Operation operationUnderTest) {
		super(operationUnderTest);
	}

	
	
	/**
	 * This methods creates test formulas that satisfy the Active Clause Coverage (ACC) criterion.
	 * To do this, for each predicate, it defines one clause of the predicate as a major clause at a time.
	 * Once a major clause is chosen we find values for the minor clauses in a way that the major clause
	 * will define the output of the predicate. To find this values for the minor clauses we use an intermediate
	 * formula. Then we replace the values for the minor clauses in the actual test formulas.
	 * 
	 * @return a Set with test formulas that satisfy  Active Clause Coverage.
	 */
	public Set<String> getTestFormulas() {
		Set<String> testFormulas = new HashSet<String>();

		for(MyPredicate predicate : getPredicates()) {
			if(operationHasPrecondition()) {
				if(!comparePredicates(predicate, getOperationUnderTest().getPrecondition())) {
					testFormulas.addAll(createACCFormulasForPredicate(predicate));
				}
			} else {
				testFormulas.addAll(createACCFormulasForPredicate(predicate));
			}
		}
		
		return testFormulas;
	}



	private Set<String> createACCFormulasForPredicate(MyPredicate predicate) {
		Set<String> testFormulas = new HashSet<String>();
		List<MyPredicate> clauses = new ArrayList<MyPredicate>(getPredicateClauses(predicate));
		
		if(clauses.size() > 1) {
			for(int i = 0; i < clauses.size(); i++) {
				MyPredicate majorClause = clauses.get(i);
				testFormulas.addAll(createTestFormulas2(majorClause, clauses, predicate));
			}
		} else {
			testFormulas.add(invariant() + precondition() + clauses.get(0));
			testFormulas.add(invariant() + precondition() + "not(" + clauses.get(0) + ")");
		}
		
		return testFormulas;
	}

	

	private List<String> createTestFormulas2(MyPredicate majorClause, List<MyPredicate> clauses, MyPredicate predicate) {
		List<String> testFormulas = new ArrayList<String>();
		
		StringBuffer majorClauseTrueFormula = new StringBuffer("");
		
		majorClauseTrueFormula.append(invariant());
		majorClauseTrueFormula.append(precondition());
		majorClauseTrueFormula.append(majorClause.toString() + " & ");
		majorClauseTrueFormula.append(createFormulaToFindValuesForMinorClauses(majorClause, predicate));
		
		StringBuffer majorClauseFalseFormula = new StringBuffer("");
		
		majorClauseFalseFormula.append(invariant());
		majorClauseFalseFormula.append(precondition());
		majorClauseFalseFormula.append("not(" + majorClause.toString() + ")" + " & ");
		majorClauseFalseFormula.append(createFormulaToFindValuesForMinorClauses(majorClause, predicate));

		testFormulas.add(majorClauseTrueFormula.toString());
		testFormulas.add(majorClauseFalseFormula.toString());
		
		return testFormulas;
	}

	

	public String createFormulaToFindValuesForMinorClauses(MyPredicate majorClause, MyPredicate predicate) {
		StringBuffer majorClauseFormula = new StringBuffer("");
		
		String initialFormula = predicate.toString(); 
		
		String trueFormula = initialFormula.replace(majorClause.toString(), TRUE);
		String falseFormula = initialFormula.replace(majorClause.toString(), FALSE);
		
		majorClauseFormula.append("(" + trueFormula + ") <=> not(" + falseFormula + ")");
		
//		majorClauseFormula.append("((" + "(" + trueFormula + ")" + " or " + "(" + falseFormula + ")" + ")");
//		majorClauseFormula.append(" & ");
//		majorClauseFormula.append("not(" + "(" + trueFormula + ")" + " & " + "(" + falseFormula + ")" + "))");
		
		return majorClauseFormula.toString();
	}

}