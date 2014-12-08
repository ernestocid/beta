package testgeneration;

import general.CombinatorialCriterias;
import general.PartitionStrategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import criteria.AllCombinations;
import criteria.EachChoice;
import criteria.Pairwise;
import animator.Animation;
import parser.Operation;
import parser.decorators.predicates.MyPredicate;

public class BETATestSuite {

	private List<BETATestCase> testCases;
	private List<String> unsolvableFormulas;
	private Operation operationUnderTest;
	private PartitionStrategy partitionStrategy;
	private CombinatorialCriterias combinatorialCriteria;



	public BETATestSuite(Operation operationUnderTest, PartitionStrategy partitionStrategy, CombinatorialCriterias combinatorialCriteria) {
		this.operationUnderTest = operationUnderTest;
		this.partitionStrategy = partitionStrategy;
		this.combinatorialCriteria = combinatorialCriteria;
		this.testCases = new ArrayList<BETATestCase>();
		this.unsolvableFormulas = new ArrayList<String>();
		generateTestCases();
	}



	private void generateTestCases() {

		// Generating partition blocks according to the chosen Partition
		// Strategy and then combining them according to the chosen
		// Combinatorial Criteria

		List<List<Block>> blocks = getBlocksAccordingToPartitionStrategy();
		Set<List<Block>> combinations = getCombinationsOfBlocksAccordingToCombinationCriteria(blocks);

		if (!combinations.isEmpty()) {

			// Evaluating combinations to get test case values for parameters
			// and variables

			IPredicateEvaluator predicateEvaluator = new AuxiliarMachinePredicateEvaluator(getOperationUnderTest(), combinations);

			// Identifying infeasible combinations
			setUnsolvableTestFormulas(predicateEvaluator.getInfeasiblePredicates());

			// Creating test cases
			createTestCases(predicateEvaluator.getSolutions(), predicateEvaluator.getFormulaAndTestTypeMap());

		}
	}



	private void createTestCases(List<Animation> animations, Map<String, Boolean> mappingOfPositiveAndNegativeTestFormulas) {
		for (Animation animation : animations) {

			// One animation contains a list of possible combinations of values
			// for the test. We pick the first element since they are all
			// equivalent tests.

			Map<String, String> animationValues = animation.getValues().get(0);

			// Setting up test case and adding to test suite.

			BETATestCase testCase = new BETATestCase(animation.getFormula(), generateTestFormulaWithoutInvariant(animation.getPredicate()),
					getAttributeValues(animationValues), getParamValues(animationValues), isNegativeTest(animation, mappingOfPositiveAndNegativeTestFormulas),
					this);

			testCases.add(testCase);

		}

		Collections.sort(testCases);
	}



	private List<List<Block>> getBlocksAccordingToPartitionStrategy() {
		if (this.partitionStrategy == PartitionStrategy.EQUIVALENT_CLASSES) {
			return new ECBlockBuilder(new Partitioner(this.operationUnderTest)).getBlocksAsListsOfBlocks();
		} else if (this.partitionStrategy == PartitionStrategy.BOUNDARY_VALUES) {
			return new BVBlockBuilder(new Partitioner(this.operationUnderTest)).getBlocksAsListsOfBlocks();
		} else {
			return new ArrayList<List<Block>>();
		}
	}



	private boolean isNegativeTest(Animation animation, Map<String, Boolean> mappingCombinationToTypeOfTest) {
		String formula = animation.getFormula().replaceAll("[()]", "");
		Boolean isNegative = mappingCombinationToTypeOfTest.get(formula);
		return isNegative.booleanValue();
	}



	private String generateTestFormulaWithoutInvariant(MyPredicate testFormula) {

		// Creating clauses list from test formula

		List<String> testFormulaClauses = getClausesFromFormula(testFormula);

		// Creating set of invariant clauses

		Set<String> invariantClauses = getClausesFromInvariant();

		// Removing invariant clauses from the list of clauses of the test
		// formula and sorting the test formulas again

		testFormulaClauses.removeAll(invariantClauses);
		Collections.sort(testFormulaClauses);

		// Rebuild the formula making a conjunction with the filtered clauses

		String testFormulaWithoutInvariant = createConjunctionOfClauses(testFormulaClauses);

		return testFormulaWithoutInvariant;

	}



	private String createConjunctionOfClauses(List<String> testFormulaClauses) {
		StringBuffer testFormulaWithoutInvariant = new StringBuffer();

		for (int i = 0; i < testFormulaClauses.size(); i++) {
			if (i < (testFormulaClauses.size() - 1)) {
				testFormulaWithoutInvariant.append(testFormulaClauses.get(i) + " & ");
			} else {
				testFormulaWithoutInvariant.append(testFormulaClauses.get(i));
			}
		}

		String cleanTestFormula = testFormulaWithoutInvariant.toString().replaceAll("i__", "");

		return cleanTestFormula;
	}



	private Set<String> getClausesFromInvariant() {
		Set<MyPredicate> condensedInvariantFromAllMachines = this.operationUnderTest.getMachine().getCondensedInvariantFromAllMachines();
		Set<String> condesendInvariant = new HashSet<String>();

		for (MyPredicate myPredicate : condensedInvariantFromAllMachines) {
			condesendInvariant.add(myPredicate.toString());
		}

		return condesendInvariant;
	}



	private List<String> getClausesFromFormula(MyPredicate testFormula) {
		Set<MyPredicate> clauses = testFormula.getClauses();
		List<String> clausesAsStrings = new ArrayList<String>();

		for (MyPredicate clause : clauses) {
			clausesAsStrings.add(clause.toString().replaceAll("i__", ""));
		}

		return clausesAsStrings;
	}



	private HashMap<String, String> getAttributeValues(Map<String, String> animation) {
		HashMap<String, String> attributeValues = new HashMap<String, String>();

		Set<String> variables = this.operationUnderTest.getMachine().getVariablesFromAllMachines();

		for (String variable : variables) {
			if (animationHasValueForVariable(animation, variable)) {
				attributeValues.put(variable, animation.get(variable));
			}
		}

		return attributeValues;
	}



	private boolean animationHasValueForVariable(Map<String, String> animation, String variable) {
		return animation.get(variable) != null;
	}



	private HashMap<String, String> getParamValues(Map<String, String> animation) {
		HashMap<String, String> parameterValues = new HashMap<String, String>();
		List<String> parameters = operationUnderTest.getParameters();

		for (String param : parameters) {
			parameterValues.put(param, animation.get(param));
		}

		return parameterValues;
	}



	private Set<List<Block>> getCombinationsOfBlocksAccordingToCombinationCriteria(List<List<Block>> blocks) {
		if (this.combinatorialCriteria == CombinatorialCriterias.PAIRWISE) {
			return getPairwiseCombinations(blocks);
		} else if (this.combinatorialCriteria == CombinatorialCriterias.EACH_CHOICE) {
			return getEachChoiceCombinations(blocks);
		} else if (this.combinatorialCriteria == CombinatorialCriterias.ALL_COMBINATIONS) {
			return getAllCombinations(blocks);
		} else {
			return new HashSet<List<Block>>();
		}
	}



	private Set<List<Block>> getAllCombinations(List<List<Block>> blocks) {
		return new AllCombinations<Block>(blocks).getCombinations();
	}



	private Set<List<Block>> getEachChoiceCombinations(List<List<Block>> blocks) {
		return new EachChoice<Block>(blocks).getCombinations();
	}



	private Set<List<Block>> getPairwiseCombinations(List<List<Block>> blocks) {
		return new Pairwise<Block>(blocks).getCombinations();
	}



	public PartitionStrategy getPartitionStrategy() {
		return this.partitionStrategy;
	}



	public List<BETATestCase> getTestCases() {
		return this.testCases;
	}



	public Operation getOperationUnderTest() {
		return this.operationUnderTest;
	}



	public CombinatorialCriterias getCombinatorialCriteria() {
		return this.combinatorialCriteria;
	}



	public List<String> getUnsolvableFormulas() {
		return unsolvableFormulas;
	}



	/**
	 * Returns a list containing all feasible test cases formulas in the test
	 * suite. These formulas are extracted from the test cases, after they are
	 * created when building the test suite.
	 * 
	 * @return a List containing all feasbile test formulas.
	 */
	public List<String> getFeasbileTestCaseFormulasWithoutInvariant() {
		List<String> feasibleTestCaseFormulas = new ArrayList<String>();

		for (BETATestCase testCase : getTestCases()) {
			feasibleTestCaseFormulas.add(testCase.getTestFormulaWithoutInvariant());
		}

		return feasibleTestCaseFormulas;
	}



	private void setUnsolvableTestFormulas(List<String> unsolvableFormulas) {
		this.unsolvableFormulas = unsolvableFormulas;
	}
}
