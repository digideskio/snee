package uk.ac.manchester.cs.snee.operators.evaluator;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;

import org.apache.log4j.Logger;

import uk.ac.manchester.cs.snee.EvaluatorException;
import uk.ac.manchester.cs.snee.SNEEException;
import uk.ac.manchester.cs.snee.common.SNEEConfigurationException;
import uk.ac.manchester.cs.snee.common.SNEEProperties;
import uk.ac.manchester.cs.snee.common.SNEEPropertyNames;
import uk.ac.manchester.cs.snee.compiler.queryplan.expressions.Attribute;
import uk.ac.manchester.cs.snee.compiler.queryplan.expressions.Expression;
import uk.ac.manchester.cs.snee.compiler.queryplan.expressions.MultiExpression;
import uk.ac.manchester.cs.snee.evaluator.types.Output;
import uk.ac.manchester.cs.snee.metadata.schema.SchemaMetadataException;
import uk.ac.manchester.cs.snee.operators.logical.JoinOperator;
import uk.ac.manchester.cs.snee.operators.logical.LogicalOperator;

public abstract class JoinOperatorAbstractImpl extends EvaluationOperator {
	Logger logger = Logger.getLogger(JoinOperatorAbstractImpl.class.getName());

	protected EvaluatorPhysicalOperator leftOperator, rightOperator;
	protected JoinOperator join;

	protected MultiExpression leftExpr;
	protected MultiExpression rightExpr;

	protected Expression joinPredicate;

	protected List<Attribute> returnAttrs;
	protected int maxBufferSize;
	protected double leftOperatorRate, rightOperatorRate;

	/**
	 * This abstract constructor would initialise all the needed
	 * variables required for the working of the associated join
	 * operator implementation
	 * 
	 * @param op
	 * @param qid
	 * @throws SNEEException
	 * @throws SchemaMetadataException
	 * @throws SNEEConfigurationException
	 */
	public JoinOperatorAbstractImpl(LogicalOperator op, int qid)
			throws SNEEException, SchemaMetadataException,
			SNEEConfigurationException {
		super(op, qid);
		if (logger.isDebugEnabled()) {
			logger.debug("ENTER JoinOperatorAbstractImpl() " + op);
		}

		// Create connections to child operators
		Iterator<LogicalOperator> iter = op.childOperatorIterator();
		LogicalOperator operator = iter.next();
		leftOperator = getEvaluatorOperator(operator);
		leftOperatorRate = operator.getSourceRate();
		operator = iter.next();
		rightOperator = getEvaluatorOperator(operator);
		rightOperatorRate = operator.getSourceRate();
		
		// XXX: Join could be speeded up by working out once which attribute
		// numbers are required from each tuple
		// Instantiate this as a join operator
		join = (JoinOperator) op;
		maxBufferSize = SNEEProperties
				.getIntSetting(SNEEPropertyNames.RESULTS_HISTORY_SIZE_TUPLES);
		if (logger.isTraceEnabled()) {
			logger.trace("Buffer size: " + maxBufferSize);
		}

		joinPredicate = join.getPredicate();
		//System.out.println("joinPredicate" + joinPredicate);
		returnAttrs = join.getAttributes();

		if (logger.isDebugEnabled()) {
			logger.debug("RETURN JoinOperatorAbstractImpl()");
		}
	}

	@Override
	public abstract void update(Observable obj, Object observed);
	
	/**
	 * This method is intended to make the operator work in an iterator
	 * model. If this mode is enabled, the operator would pull the next 
	 * set of data (A window or a tuple) by calling the getNext() method
	 * on the the child operator. Since this is a join operator, it would
	 * call the getNext() method on the left and the right child, perform
	 * join and then again call the getNext() method to get the next data
	 * to operate on.
	 * 
	 * TODO For the current implementation, this mode would be enabled only
	 * if there is a valve operator between the join operator and the child
	 * operator. Also there are only going to be a single implementation of
	 * the iterator model of join operator. This would be a simple hash join
	 * for now. So for the Hash join implementation, there would be only
	 * this method implemented, and not update method
	 * 
	 * @return 
	 */
	public abstract void generateAndUpdate();
	
	@Override
	public void open() throws EvaluatorException {
		if (logger.isDebugEnabled()) {
			logger.debug("ENTER open()");
		}
		/*
		 * Open right child first as it may be a relation!
		 */
		startChildReceiver(rightOperator);
		startChildReceiver(leftOperator);
		if (logger.isDebugEnabled()) {
			logger.debug("RETURN open()");
		}
		if (join.isGetDataByPullModeOperator()) {
			evaluateJoinForIntervals();
		}
	}

	/**
	 * This method checks for the rates of the input operands and evoke
	 * the generateAndUpdate method in a timely manner. The time to evoke
	 * is determined by the source rates of both inputs
	 */
	private void evaluateJoinForIntervals() {
		long currentTime = System.currentTimeMillis();
		long nextEvalTime = getNextEvalTime(currentTime);
		
		do {
			currentTime = System.currentTimeMillis();
			if (nextEvalTime <= currentTime) {
				generateAndUpdate();
				nextEvalTime = getNextEvalTime(currentTime);
			}
		} while (true);
		
	}

	private long getNextEvalTime(long currentTime) {
		if ((currentTime + leftOperatorRate) < (currentTime + rightOperatorRate)) {
			return (long) (currentTime + leftOperatorRate);
		} else {
			return (long) (currentTime + rightOperatorRate);
		}
	}

	private void startChildReceiver(EvaluatorPhysicalOperator op)
			throws EvaluatorException {
		if (logger.isTraceEnabled()) {
			logger.trace("ENTER startChildReceiver() " + op.toString());
		}
		op.setSchema(getSchema());
		op.addObserver(this);
		op.open();
		if (logger.isTraceEnabled()) {
			logger.trace("RETURN startChildReceiver()");
		}
	}

	public void close() {
		if (logger.isDebugEnabled()) {
			logger.debug("ENTER close()");
		}
		leftOperator.close();
		rightOperator.close();
		if (logger.isDebugEnabled()) {
			logger.debug("RETURN close()");
		}
	}

}