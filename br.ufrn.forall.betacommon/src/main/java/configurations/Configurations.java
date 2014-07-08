package configurations;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

public class Configurations {

	
	private static String PATH_TO_PROPERTIES_FILE = "config.properties";
	
	
	/**
	 * Gets the path to the ProB directory stored on the config.properties file
	 * @return path to ProB directory
	 */
	public static String getProBPath() {
		String probPath = null;

		try {
			PropertiesConfiguration config = new PropertiesConfiguration(PATH_TO_PROPERTIES_FILE);
			probPath = config.getString("prob_path");
		} catch (ConfigurationException e) {
			System.err.println("Could not get ProB path: " + e.getMessage());
		}
		
		return probPath;
	}

	
	
	/**
	 * Sets the path to the ProB directory on the config.properties file
	 * @param probPath path to ProB directory
	 */
	public static void setProBPath(String probPath) {
		try {
			PropertiesConfiguration config = new PropertiesConfiguration(PATH_TO_PROPERTIES_FILE);
			config.setProperty("prob_path", probPath);
			config.save();
		} catch (ConfigurationException e) {
			System.err.println("Could not set ProB path: " + e.getMessage());
		}
	}

	

	/**
	 * Sets the boolean values for the tool oracle strategies on the config.properties file
	 * @param stateVariables value for State Variables oracle strategy
	 * @param returnVariables value for Return Variables oracle strategy
	 * @param invariantOK value for Invariant OK oracle strategy
	 */
	public static void setOracleStrategy(boolean stateVariables, boolean returnVariables, boolean invariantOK) {
		try {
			PropertiesConfiguration config = new PropertiesConfiguration(PATH_TO_PROPERTIES_FILE);
			config.setProperty("oracle_strategy_state_variables", stateVariables);
			config.setProperty("oracle_strategy_return_variables", returnVariables);
			config.setProperty("oracle_strategy_invariant_ok", invariantOK);
			config.save();
		} catch (ConfigurationException e) {
			System.err.println("Could not set oracles strategies: " + e.getMessage());
		}
	}

	

	/**
	 * Gets the value for the States Variables oracle strategy stored on 
	 * the config.properties file
	 * @return boolean value for State Variables Oracle Strategy
	 */
	public static boolean isStateVariablesActive() {
		boolean isStateVariablesActive = false;
		
		try {
			PropertiesConfiguration config = new PropertiesConfiguration(PATH_TO_PROPERTIES_FILE);
			isStateVariablesActive = config.getBoolean("oracle_strategy_state_variables");
		} catch (ConfigurationException e) {
			System.err.println("Could not get value for oracle_strategy_state_variables: " + e.getMessage());
		}
		
		return isStateVariablesActive;
	}
	
	
	
	/**
	 * Gets the value for the Return Variables oracle strategy stored on 
	 * the config.properties file
	 * @return boolean value for Return Variables Oracle Strategy
	 */
	public static boolean isReturnVariablesActive() {
		boolean isReturnVariablessActive = false;
		
		try {
			PropertiesConfiguration config = new PropertiesConfiguration(PATH_TO_PROPERTIES_FILE);
			isReturnVariablessActive = config.getBoolean("oracle_strategy_return_variables");
		} catch (ConfigurationException e) {
			System.err.println("Could not get value for oracle_strategy_return_variables: " + e.getMessage());
		}
		
		return isReturnVariablessActive;
	}
	
	
	
	/**
	 * Gets the value for the Invariant OK oracle strategy stored on 
	 * the config.properties file
	 * @return boolean value for Invariant OK Oracle Strategy
	 */
	public static boolean isInvariantOKActive() {
		boolean isInvariantOKActive = false;
		
		try {
			PropertiesConfiguration config = new PropertiesConfiguration(PATH_TO_PROPERTIES_FILE);
			isInvariantOKActive = config.getBoolean("oracle_strategy_invariant_ok");
		} catch (ConfigurationException e) {
			System.err.println("Could not get value for oracle_strategy_invariant_ok: " + e.getMessage());
		}
		
		return isInvariantOKActive;
	}

	
	
	/**
	 * Sets the value for the Delete Temporary Files configuration on 
	 * the config.properties file
	 * @param deleteTempFiles boolean value for the Delete Temporary Files
	 */
	public static void setDeleteTempFiles(boolean deleteTempFiles) {
		try {
			PropertiesConfiguration config = new PropertiesConfiguration(PATH_TO_PROPERTIES_FILE);
			config.setProperty("delete_temp_files", deleteTempFiles);
			config.save();
		} catch (ConfigurationException e) {
			System.err.println("Could not set value for delete_temp_files: " + e.getMessage());
		}
	}

	

	/**
	 * Gets the value for the Delete Temporary Files configuration on 
	 * the config.properties file
	 * @return boolean value for Delete Temporary Files
	 */
	public static boolean isDeleteTempFiles() {
		boolean deleteTempFiles = true;

		try {
			PropertiesConfiguration config = new PropertiesConfiguration(PATH_TO_PROPERTIES_FILE);
			deleteTempFiles = config.getBoolean("delete_temp_files");
		} catch (ConfigurationException e) {
			System.err.println("Could not get value for delete_temp_files: " + e.getMessage());
		}
		
		return deleteTempFiles;
	}
	
	
	
	/**
	 * Gets the value for the MINTINT on the config.properties file
	 * @return int value for minint
	 */
	public static int getMinIntProperties() {
		int minint = -5;

		try {
			PropertiesConfiguration config = new PropertiesConfiguration(PATH_TO_PROPERTIES_FILE);
			minint = config.getInt("minint");
		} catch (ConfigurationException e) {
			System.err.println("Could not get value for minint: " + e.getMessage());
		}
		
		return minint;
	}
	
	
	
	/**
	 * Gets the value for the MAXINT on the config.properties file
	 * @return int value for maxint
	 */
	public static int getMaxIntProperties() {
		int maxint = 5;

		try {
			PropertiesConfiguration config = new PropertiesConfiguration(PATH_TO_PROPERTIES_FILE);
			maxint = config.getInt("maxint");
		} catch (ConfigurationException e) {
			System.err.println("Could not get value for maxint: " + e.getMessage());
		}
		
		return maxint;
	}



	/**
	 * Sets the value for the minint properties on the config.properties file
	 * @param minint int value for the minint properties
	 */
	public static void setMinIntProperties(int minint) {
		try {
			PropertiesConfiguration config = new PropertiesConfiguration(PATH_TO_PROPERTIES_FILE);
			config.setProperty("minint", minint);
			config.save();
		} catch (ConfigurationException e) {
			System.err.println("Could not set value for minint: " + e.getMessage());
		}
	}
	
	
	
	/**
	 * Sets the value for the maxint properties on the config.properties file
	 * @param maxint int value for the maxint properties
	 */
	public static void setMaxIntProperties(int maxint) {
		try {
			PropertiesConfiguration config = new PropertiesConfiguration(PATH_TO_PROPERTIES_FILE);
			config.setProperty("maxint", maxint);
			config.save();
		} catch (ConfigurationException e) {
			System.err.println("Could not set value for maxint: " + e.getMessage());
		}
	}

}