package uk.ac.manchester.cs.snee.compiler.queryplan;

import org.apache.log4j.Logger;

/**
 * Query Plan for execution on SNEE Evaluator.
 */
public class EvaluatorQueryPlan extends QueryExecutionPlan {

    /**
     * Logger for this class.
     */
    private static  Logger logger = Logger.getLogger(
    		EvaluatorQueryPlan.class.getName());
	
    /**
     * Constructor for EvaluatorQueryPlan.
     * @param dlaf
     * @param queryName
     */
	public EvaluatorQueryPlan(DLAF dlaf, String queryName) {
		super(dlaf, queryName);
		if (logger.isDebugEnabled())
			logger.debug("ENTER EvaluatorQueryPlan()");	
		if (logger.isDebugEnabled())
			logger.debug("RETURN EvaluatorQueryPlan()");	
	}
}
