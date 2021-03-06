package uk.ac.manchester.cs.snee.operators.evaluator;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Stack;

import org.apache.log4j.Logger;

import uk.ac.manchester.cs.snee.SNEEException;
import uk.ac.manchester.cs.snee.common.SNEEConfigurationException;
import uk.ac.manchester.cs.snee.compiler.queryplan.expressions.Attribute;
import uk.ac.manchester.cs.snee.compiler.queryplan.expressions.DataAttribute;
import uk.ac.manchester.cs.snee.compiler.queryplan.expressions.Expression;
import uk.ac.manchester.cs.snee.compiler.queryplan.expressions.FloatLiteral;
import uk.ac.manchester.cs.snee.compiler.queryplan.expressions.IntLiteral;
import uk.ac.manchester.cs.snee.compiler.queryplan.expressions.MultiExpression;
import uk.ac.manchester.cs.snee.compiler.queryplan.expressions.MultiType;
import uk.ac.manchester.cs.snee.compiler.queryplan.expressions.StringLiteral;
import uk.ac.manchester.cs.snee.evaluator.types.EvaluatorAttribute;
import uk.ac.manchester.cs.snee.evaluator.types.Output;
import uk.ac.manchester.cs.snee.evaluator.types.TaggedTuple;
import uk.ac.manchester.cs.snee.evaluator.types.Tuple;
import uk.ac.manchester.cs.snee.evaluator.types.Window;
import uk.ac.manchester.cs.snee.metadata.schema.SchemaMetadataException;
import uk.ac.manchester.cs.snee.metadata.schema.TypeMappingException;
import uk.ac.manchester.cs.snee.operators.logical.LogicalOperator;
import uk.ac.manchester.cs.snee.operators.logical.ProjectOperator;

public class ProjectOperatorImpl extends EvaluationOperator {
	//XXX: Write test for project operator
	//FIXME: Testing not working

	//FIXME: Add constants as outputs

	/*
	 * Testing of this class is not having the desired effect.
	 * The test does not fail even though the project is not doing its
	 * job 	
	 */
	Logger logger = Logger.getLogger(this.getClass().getName());

	ProjectOperator project;
	List<Attribute> attributes;

	public ProjectOperatorImpl(LogicalOperator op, int qid) 
	throws SNEEException, SchemaMetadataException,
	SNEEConfigurationException {
		super(op, qid);
		if (logger.isDebugEnabled()) {
			logger.debug("ENTER ProjectOperatorImpl() " + op.getText());
			logger.debug("Project List: " + op.getAttributes());
			logger.debug("Expression List: " + op.getExpressions());
		}

		// Instantiate this as a project operator
		project = (ProjectOperator) op;
		attributes = project.getAttributes();

		if (logger.isDebugEnabled()) {
			logger.debug("RETURN ProjectOperatorImpl()");
		}
	}

	/**
	 * Constructor for testing purposes only. 
	 * Stops the recursive nature of the constructor
	 * @param op
	 */
	public ProjectOperatorImpl(ProjectOperator op)
	{
		logger.debug("ENTER ProjectOperatorImpl()");
		project = op;
		attributes = project.getAttributes();
		logger.debug("RETURN ProjectOperatorImpl()");
	}

	@Override
	public void update(Observable obj, Object observed) {
		if (logger.isDebugEnabled())
			logger.debug("ENTER update() for query " + m_qid + " " +
					" with " + observed);
		List<Output> result = new ArrayList<Output>();
		try {
			if (observed instanceof List<?>) {
				List<Output> outputList = (List<Output>) observed;
				for (Output output : outputList) {
					if (output instanceof Window) {
						result.add(processWindow(output));
					} else if (output instanceof TaggedTuple) {
						result.add(processTuple(output));
					}
				}
			} else if (observed instanceof Window) {
				result.add(processWindow(observed));
			} else if (observed instanceof TaggedTuple) {
				result.add(processTuple(observed));
			}
			setChanged();
			notifyObservers(result);
		} catch (Exception e) {
			logger.error(e);
		}
		if (logger.isDebugEnabled())
			logger.debug("RETURN update()");
	}

	private Output processTuple(Object observed)
	throws SNEEException, SchemaMetadataException, TypeMappingException 
	{
		if (logger.isTraceEnabled()) {
			logger.trace("ENTER processTuple() with " + observed);
		}
		TaggedTuple tuple = (TaggedTuple) observed;
		if (logger.isTraceEnabled()) {
			logger.trace("Processing tuple: " + tuple);
		}
		Tuple projectedTuple = getProjectedTuple(tuple.getTuple());
		// Replace the tuple in the tagged tuple
		tuple.setTuple(projectedTuple);
		if (logger.isTraceEnabled()) {
			logger.trace("RETURN processTuple() with " + tuple);
		}
		return tuple;
	}

	private Output processWindow(Object observed) 
	throws SNEEException, SchemaMetadataException, TypeMappingException {
		if (logger.isTraceEnabled()) {
			logger.trace("ENTER processWindow() with " + observed);
		}
		Window window = (Window) observed;
		List<Tuple> tupleList = new ArrayList<Tuple>();
		// Process each tuple in the window
		for (Tuple t : window.getTuples()){
			if (logger.isTraceEnabled()) {
				logger.trace("Processing tuple: " + t);
			}
			Tuple projectedTuple = getProjectedTuple(t);
			tupleList.add(projectedTuple);
		}
		/*
		 * Create new window with projected tuples. 
		 * The index and evaluation time remain unchanged
		 */
		//TODO: Do we need a replace tupleList operation?
		Window newWindow = new Window(tupleList);
		if (logger.isTraceEnabled()) {
			logger.trace("RETURN processWindow() with " + newWindow);
		}
		return newWindow;
	}	

	private Tuple getProjectedTuple(Tuple tuple) 
	throws SNEEException, SchemaMetadataException, 
	TypeMappingException 
	{
		if (logger.isTraceEnabled()) {
			logger.debug("ENTER getProjectedTuple() with " + tuple);
		}		
		Tuple projectedTuple = new Tuple();
		List<Expression> expressions = project.getExpressions();
		logger.trace("Number of expressions: " + expressions.size());
		//Iterate with index so that we can do renames
		for (Expression exp : expressions) {
			if (logger.isTraceEnabled()) {
				logger.trace("Expression: " + exp);
			}
			EvaluatorAttribute attr = null;
			if (exp instanceof MultiExpression) {
				attr = evaluateMultiExpression(
						(MultiExpression)exp, tuple);
			} else {
				attr = evaluateSingleExpression(exp, tuple);
			}
			// Add project field to the result tuple
			if (logger.isTraceEnabled()) {
				logger.trace("Attribute: " + attr);
			}
			if (attr != null) {
				projectedTuple.addAttribute(attr);
			}
		}

		if (logger.isTraceEnabled()) {
			logger.trace("RETURN getProjectedTuple() with " + projectedTuple);
		}
		return projectedTuple;
	}

	/*
	 * Protected method to enable testing.
	 */
	protected EvaluatorAttribute evaluateSingleExpression(
			Expression exp, Tuple t) 
	throws SNEEException {
		if (logger.isTraceEnabled()) {
			logger.trace("ENTER evaluateSingleExpression() with " + 
					exp.toString() + " and tuple: " + t);
		}
		EvaluatorAttribute returnField = null;
		// Only a single expression, get attribute details
		if (exp.isConstant()) {
			try {
				if (exp instanceof IntLiteral) {
					IntLiteral intLit = (IntLiteral) exp; 
					returnField = new EvaluatorAttribute(exp.toAttribute(), 
							intLit.getValue());
				} else if (exp instanceof FloatLiteral) {
					FloatLiteral floatLit = (FloatLiteral) exp; 
					returnField = new EvaluatorAttribute(exp.toAttribute(), 
							floatLit.getValue());
				} else if (exp instanceof StringLiteral) {
					StringLiteral strLit = (StringLiteral) exp; 
					returnField = new EvaluatorAttribute(exp.toAttribute(), 
							strLit.getValue());
				}
			} catch (SchemaMetadataException e) {
				String message = "Could not create representation of " +
					"constant value " + exp;
				logger.warn(message, e);
			} catch (TypeMappingException e) {
				String message = "Could not create representation of " +
					"constant value " + exp;
				logger.warn(message, e);
			}
		} else {
			List<Attribute> attributes = exp.getRequiredAttributes();
			Attribute attr = attributes.get(0);
			String extentName = attr.getExtentName();
			String attributeName = attr.getAttributeSchemaName();
			try {
				if (logger.isTraceEnabled()) {
					logger.trace("getting attribute " + attributeName +
							" from extent " + extentName);
				}
				returnField = 
					t.getAttribute(extentName, attributeName);
			} catch (SNEEException e) {
				String message = "Attribute " + attributeName + 
						" does not exist.";
				logger.warn(message, e);
				throw new SNEEException(message, e);
			}
		}
		if (logger.isTraceEnabled()) {
			logger.trace("RETURN evaluateSingleExpression() with " + 
					returnField);
		}
		return returnField;
	}

	protected EvaluatorAttribute evaluateMultiExpression(
			MultiExpression exp, Tuple t) 
	throws SNEEException, SchemaMetadataException,
	TypeMappingException 
	{
		if (logger.isTraceEnabled()) {
			logger.trace("ENTER evaluateMultiExpression() with " + 
					exp.toString() + " and tuple: " + t);
		}
		// Instantiate return item
		EvaluatorAttribute returnField = null;
		String extentName = null;
		String attrName = null;
		String attrLabel = null;
		// Setup stack for operands in the expression
		Stack<Object> operands = new Stack<Object>();
		for (Expression expr : exp.getExpressions()) {
			Object daValue;
			if (expr instanceof MultiExpression) {
				EvaluatorAttribute attr = 
					evaluateMultiExpression((MultiExpression)expr, t);
				if (extentName == null) {
					extentName = attr.getExtentName();
				}
				if (attrName == null) {
					attrName = attr.getAttributeSchemaName();
				}
				if (attrLabel == null) {
					attrLabel = attr.getAttributeDisplayName();
				}
				daValue = attr.getData(); 
				if (logger.isTraceEnabled()) {
					logger.trace("Stack push result expression: " + 
							expr +  ", " + daValue + ", type " + 
							daValue.getClass());
				}
			} else if (expr instanceof DataAttribute){
				DataAttribute da = (DataAttribute) expr;
				if (extentName == null) {
					extentName = da.getExtentName();
				}
				if (attrName == null) {
					attrName = da.getAttributeSchemaName();
				}
				if (attrLabel == null) {
					attrLabel = da.getAttributeDisplayName();
				}
				String daLabelName = da.getAttributeDisplayName();
				try {
					daValue = 
						t.getAttributeValueByDisplayName(daLabelName);
				} catch (SNEEException e) {
					try {
						daValue = t.getAttributeValue(extentName, attrName);
					} catch (SNEEException e1) {
					logger.warn("Problem getting value for " + 
							daLabelName, e1);
					throw e;
					}
				}
				if (logger.isTraceEnabled()) {
					logger.trace("Stack push attribute: " + 
							daLabelName + ", " + daValue + 
							", type " + daValue.getClass());
				}				
			} else if (expr instanceof IntLiteral){
				IntLiteral il = (IntLiteral) expr;
				daValue = new Integer(il.getValue());
				if (logger.isTraceEnabled()) {
					logger.trace("Stack push integer: " + 
							il.getValue() + ", type " + 
							daValue.getClass());
				}
			} else if (expr instanceof FloatLiteral){
				FloatLiteral fl = (FloatLiteral) expr;
				daValue = new Float(fl.getValue());
				if (logger.isTraceEnabled()) {
					logger.trace("Stack push float: " +
							fl.getValue() + ", type " + 
							daValue.getClass());
				}
			} else if (expr instanceof StringLiteral){
				StringLiteral sl = (StringLiteral) expr;
				daValue = sl.getValue();
				if (logger.isTraceEnabled()) {
					logger.trace("Stack push string: " +
							sl.getValue() + ", type " + 
							daValue.getClass());
				}
			} else {
				String msg = "Unsupported operand " + expr;
				logger.warn(msg);
				throw new SNEEException(msg);
			}
			if (logger.isTraceEnabled())
				logger.trace("Pushing " + daValue + 
						" of type " + daValue.getClass());
			operands.add(daValue);
		}
		while (operands.size() >= 2){
			// Evaluate result
			Object op1 = operands.pop();
			Object op2 = operands.pop();
			Object result;
			MultiType type = ((MultiExpression)exp).getMultiType();
			if (op1 instanceof StringLiteral && op2 instanceof StringLiteral) {
				result = evaluateString((String)op1, (String)op2, type);
			} else {
				result = evaluateNumeric((Number)op1, (Number)op2, type);
			}
			
			if (logger.isTraceEnabled())
				logger.trace("Creating new attribute: " +
						extentName + "." + attrName + " AS " +
						attrLabel + " of type " + exp.getType() + 
						" with value " + result);
			Attribute attr = new DataAttribute(extentName, attrName,
					attrLabel, exp.getType());
			returnField = 
				new EvaluatorAttribute(attr, result);
		}
		if (logger.isTraceEnabled()) {
			logger.trace("RETURN evaluateMultiExpression() with " + 
					returnField);
		}
		return returnField;
	}

}
