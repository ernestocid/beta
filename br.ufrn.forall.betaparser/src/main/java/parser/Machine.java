package parser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import parser.decorators.predicates.MyPredicate;
import utils.MachineClausesTool;
import de.be4.classicalb.core.parser.BParser;
import de.be4.classicalb.core.parser.exceptions.BException;
import de.be4.classicalb.core.parser.node.AAbstractConstantsMachineClause;
import de.be4.classicalb.core.parser.node.AAbstractMachineParseUnit;
import de.be4.classicalb.core.parser.node.AConstantsMachineClause;
import de.be4.classicalb.core.parser.node.ADefinitionsMachineClause;
import de.be4.classicalb.core.parser.node.AExtendsMachineClause;
import de.be4.classicalb.core.parser.node.AIncludesMachineClause;
import de.be4.classicalb.core.parser.node.AInitialisationMachineClause;
import de.be4.classicalb.core.parser.node.AInvariantMachineClause;
import de.be4.classicalb.core.parser.node.AMachineHeader;
import de.be4.classicalb.core.parser.node.AOperation;
import de.be4.classicalb.core.parser.node.AOperationsMachineClause;
import de.be4.classicalb.core.parser.node.APromotesMachineClause;
import de.be4.classicalb.core.parser.node.APropertiesMachineClause;
import de.be4.classicalb.core.parser.node.ASeesMachineClause;
import de.be4.classicalb.core.parser.node.ASetsMachineClause;
import de.be4.classicalb.core.parser.node.AUsesMachineClause;
import de.be4.classicalb.core.parser.node.AVariablesMachineClause;
import de.be4.classicalb.core.parser.node.Start;

public class Machine {
	

	private File machine;
	private AAbstractMachineParseUnit parsedMachine;
	private Sees sees;
	private Uses uses;
	private Includes includes;
	private Extends extendss;
	private Promotes promotes;
	private Sets sets;
	private Constants constants;
	private AbstractConstants abstractConstats;
	private Properties properties;
	private Definitions definitions;
	private Variables variables;
	private Invariant invariant;
	private Initialisation initialisation;
	private List<Operation> operations;
	private boolean parsed;
	
	
	public Machine(File machine) {
		this.machine = machine;
		Start startNode = parseMachine();
		
		if(startNode == null) {
			// If machine was not parsed
			this.parsed = false;
		} else if(startNode.getPParseUnit() instanceof AAbstractMachineParseUnit) {
			// If machine was parsed
			this.parsedMachine = (AAbstractMachineParseUnit) startNode.getPParseUnit();
			setUpMachine();
		}
	}
	
	
	
	private Start parseMachine() {
		BParser parser = new BParser();
		Start start = null;
		try {
			start = parser.parseFile(machine, false);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (BException e) {
			System.err.println("Error while parsing the machine: " + e.getMessage());
			return null;
		} 
		return start;
	}
	
	
	
	private void setUpMachine() {
		this.parsed = true;
		
		ASeesMachineClause seesClause = MachineClausesTool.getSeesClause(parsedMachine);
		AUsesMachineClause usesClause = MachineClausesTool.getUsesClause(parsedMachine);
		AIncludesMachineClause includesClause = MachineClausesTool.getIncludesClause(parsedMachine);
		AExtendsMachineClause extendsClause = MachineClausesTool.getExtendsClause(parsedMachine);
		APromotesMachineClause promotesClause = MachineClausesTool.getPromotesClause(parsedMachine);
		ASetsMachineClause setsClause = MachineClausesTool.getSetsClause(parsedMachine);
		AConstantsMachineClause constantsClause = MachineClausesTool.getConstantsClause(parsedMachine);
		AAbstractConstantsMachineClause abstractConstantsClause = MachineClausesTool.getAbstractConstantsClause(parsedMachine);
		APropertiesMachineClause propertiesClause = MachineClausesTool.getPropertiesClause(parsedMachine);
		ADefinitionsMachineClause definitionsClause = MachineClausesTool.getDefinitionsClause(parsedMachine);
		AVariablesMachineClause variablesClause = MachineClausesTool.getVariablesClause(parsedMachine);
		AInvariantMachineClause invariantClause = MachineClausesTool.getInvariantClause(parsedMachine);
		AInitialisationMachineClause initialisationClause = MachineClausesTool.getInitialisationClause(parsedMachine);
		AOperationsMachineClause operationsClause = MachineClausesTool.getOperationsClause(parsedMachine);
		
		if (seesClause != null) 				this.sees = new Sees(seesClause, this);
		if (usesClause != null) 				this.uses = new Uses(usesClause, this);
		if (includesClause != null) 			this.includes = new Includes(includesClause, this);
		if (extendsClause != null) 				this.extendss = new Extends(extendsClause, this);
		if (promotesClause != null)				this.promotes = new Promotes(promotesClause, this);
		if (setsClause != null) 				this.sets = new Sets(setsClause);
		if (constantsClause != null) 			this.constants = new Constants(constantsClause, this);
		if (abstractConstantsClause != null)	this.abstractConstats = new AbstractConstants(abstractConstantsClause, this);
		if (propertiesClause != null)			this.properties = new Properties(propertiesClause);
		if (definitionsClause != null)			this.definitions = new Definitions(definitionsClause);
		if (variablesClause != null)			this.variables = new Variables(variablesClause);
		if (invariantClause != null)			this.invariant = new Invariant(invariantClause);
		if (initialisationClause != null) 		this.initialisation = new Initialisation(initialisationClause);
		
		this.operations = new ArrayList<Operation>();

		if (operationsClause != null) {
			List<AOperation> aOperations = MachineClausesTool.getAOperations(parsedMachine);
			
			for (AOperation aOperation : aOperations) {
				this.operations.add(new Operation(aOperation, this));
			}
			
			this.operations.addAll(getPromotedOperations());
			this.operations.addAll(getExtendedOperations());
		}
	}

	
	
	public AAbstractMachineParseUnit getParsedUnit() {
		return this.parsedMachine;
	}

	

	public String getName() {
		AMachineHeader machineHeader = MachineClausesTool.getMachineHeader(this.parsedMachine);
		return machineHeader.getName().getFirst().getText();
	}
	
	
	
	public List<Operation> getOperations() {
		return this.operations;
	}
	
	
	
	private List<Operation> getPromotedOperations() {
		List<Operation> promotedOperations = new ArrayList<Operation>();
		if(this.promotes != null) {
			promotedOperations.addAll(this.promotes.getPromotedOperations());
		}
		return promotedOperations;
	}
	
	
	
	private List<Operation> getExtendedOperations() {
		List<Operation> extendedOperations = new ArrayList<Operation>();
		
		if (this.extendss != null) {
			for (Machine machineExtended : this.extendss.getMachinesExtended()) {
				extendedOperations.addAll(machineExtended.getOperations());
			}
		}
		
		return extendedOperations;
	}
	
	
	
	public Operation getOperation(int operationIndex) {
		return this.operations.get(operationIndex);
	}

	
	
	public Variables getVariables() {
		return this.variables;
	}
	
	
	
	public Set<String> getConstantValues() {
		Set<String> constantValues = new HashSet<String>();
		
		if(this.getSets() != null) {
			constantValues.addAll(this.getSets().getElementsFromAllSets());
		}
		
		return constantValues;
	}
	
	
	
	public Invariant getInvariant() {
		return this.invariant;
	}
	
	
	
	public Constants getConstants() {
		return this.constants;
	}
	
	
	
	public AbstractConstants getAbstractConstants() {
		return this.abstractConstats;
	}
	
	
	
	public Properties getProperties() {
		return this.properties;
	}
	
	
	
	public Definitions getDefinitions() {
		return this.definitions;
	}
	
	
	
	public Sets getSets() {
		return this.sets;
	}
	
	
	
	public Initialisation getInitialisation() {
		return this.initialisation;
	}
	
	
	
	public Uses getUses() {
		return this.uses;
	}
	
	
	
	public Sees getSees() {
		return this.sees;
	}
	
	
	
	public Includes getIncludes() {
		return this.includes;
	}
	
	
	
	public Extends getExtends() {
		return this.extendss;
	}
	
	
	public File getFile() {
		return machine;
	}
	
	
	
	public Set<MyPredicate> getPropertiesClauses() {
		if (this.properties != null) {
			return this.properties.getPropertiesClauses();
		} else {
			return new HashSet<MyPredicate>();
		}
	}
	
	
	
	public Set<MyPredicate> getCondensedInvariantFromAllMachines() {
		Set<MyPredicate> invariant = new HashSet<MyPredicate>(); 

		invariant.addAll(getInvariantClauses());
		invariant.addAll(getInvariantClausesFromInclude());
		invariant.addAll(getInvariantClausesFromSees());
		invariant.addAll(getInvariantClausesFromUses());
		invariant.addAll(getInvariantClausesFromExtends());
		
		return invariant;
	}
	
	
	
	private Set<MyPredicate> getInvariantClauses() {
		Set<MyPredicate> invariantClauses = new HashSet<MyPredicate>();
		if (this.getInvariant() != null) invariantClauses.addAll(this.invariant.getClausesList());
		return invariantClauses;
	}
	
	
	
	private Set<MyPredicate> getInvariantClausesFromInclude() {
		Set<MyPredicate> invariant = new HashSet<MyPredicate>();
		if (this.includes != null) invariant.addAll(this.includes.getInvariantFromAllMachinesIncluded());
		return invariant;
	}
	
	
	
	private Set<MyPredicate> getInvariantClausesFromSees() {
		Set<MyPredicate> invariant = new HashSet<MyPredicate>();
		if (this.sees != null) invariant.addAll(this.sees.getInvariantFromAllMachinesSeen());
		return invariant;
	}
	
	
	
	private Set<MyPredicate> getInvariantClausesFromUses() {
		Set<MyPredicate> invariant = new HashSet<MyPredicate>();
		if (this.uses != null) invariant.addAll(this.uses.getInvariantFromAllMachinesUses());
		return invariant;
	}
	
	
	
	private Set<MyPredicate> getInvariantClausesFromExtends() {
		Set<MyPredicate> invariant = new HashSet<MyPredicate>();
		if (this.extendss != null) invariant.addAll(this.extendss.getInvariantFromAllMachinesExtended());
		return invariant;
	}



	public boolean isParsed() {
		return parsed;
	}



	public Set<String> getAllConstants() {
		Set<String> allConstants = new HashSet<String>();

		if(this.constants != null) {
			allConstants.addAll(this.constants.getAll());
		}
		
		if(this.abstractConstats != null) {
			allConstants.addAll(this.abstractConstats.getAll());
		}
		
		return allConstants;
	}

}
