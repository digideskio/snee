package uk.ac.manchester.cs.snee.operators.evaluator;

import java.util.Collection;
import java.util.Observable;

import uk.ac.manchester.cs.snee.SNEEException;
import uk.ac.manchester.cs.snee.evaluator.types.Output;
import uk.ac.manchester.cs.snee.evaluator.types.ReceiveTimeoutException;
import uk.ac.manchester.cs.snee.operators.evaluator.EvaluatorPhysicalOperator;
import uk.ac.manchester.cs.snee.operators.logical.LogicalOperator;

public class MockPhysicalOperator extends EvaluatorPhysicalOperator {

	private MockOperator mockOp;

	public MockPhysicalOperator(LogicalOperator op) {
		if (logger.isDebugEnabled()) {
			logger.debug("ENTER: " + op);
		}

		// Instantiate deliver operator
		mockOp = (MockOperator) op;

		if (logger.isDebugEnabled()) {
			logger.debug("RETURN");
		}
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}
	//
	//	@Override
	//	public Collection<Output> getNext() throws ReceiveTimeoutException,
	//			SNEEException {
	//		// TODO Auto-generated method stub
	//		return null;
	//	}

	@Override
	public void open() {
		// TODO Auto-generated method stub

	}

	@Override
	public void update(Observable obj, Object observed) {
		// TODO Auto-generated method stub

	}

}
