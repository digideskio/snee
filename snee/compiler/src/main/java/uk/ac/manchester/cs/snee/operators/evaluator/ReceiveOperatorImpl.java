/****************************************************************************\
 *                                                                            *
 *  SNEE (Sensor NEtwork Engine)                                              *
 *  http://snee.cs.manchester.ac.uk/                                          *
 *  http://code.google.com/p/snee                                             *
 *                                                                            *
 *  Release 1.x, 2009, under New BSD License.                                 *
 *                                                                            *
 *  Copyright (c) 2009, University of Manchester                              *
 *  All rights reserved.                                                      *
 *                                                                            *
 *  Redistribution and use in source and binary forms, with or without        *
 *  modification, are permitted provided that the following conditions are    *
 *  met: Redistributions of source code must retain the above copyright       *
 *  notice, this list of conditions and the following disclaimer.             *
 *  Redistributions in binary form must reproduce the above copyright notice, *
 *  this list of conditions and the following disclaimer in the documentation *
 *  and/or other materials provided with the distribution.                    *
 *  Neither the name of the University of Manchester nor the names of its     *
 *  contributors may be used to endorse or promote products derived from this *
 *  software without specific prior written permission.                       *
 *                                                                            *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS   *
 *  IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, *
 *  THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR    *
 *  PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR          *
 *  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,     *
 *  EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,       *
 *  PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR        *
 *  PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF    *
 *  LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING      *
 *  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS        *
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.              *
 *                                                                            *
\****************************************************************************/
package uk.ac.manchester.cs.snee.operators.evaluator;

import java.util.List;
import java.util.Observable;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import uk.ac.manchester.cs.snee.EvaluatorException;
import uk.ac.manchester.cs.snee.SNEEDataSourceException;
import uk.ac.manchester.cs.snee.SNEEException;
import uk.ac.manchester.cs.snee.common.SNEEConfigurationException;
import uk.ac.manchester.cs.snee.compiler.metadata.schema.ExtentDoesNotExistException;
import uk.ac.manchester.cs.snee.compiler.metadata.schema.SchemaMetadataException;
import uk.ac.manchester.cs.snee.compiler.metadata.schema.TypeMappingException;
import uk.ac.manchester.cs.snee.compiler.metadata.source.PullSourceMetadata;
import uk.ac.manchester.cs.snee.compiler.metadata.source.SourceMetadata;
import uk.ac.manchester.cs.snee.compiler.metadata.source.SourceMetadataException;
import uk.ac.manchester.cs.snee.compiler.metadata.source.SourceType;
import uk.ac.manchester.cs.snee.compiler.metadata.source.UDPSourceMetadata;
import uk.ac.manchester.cs.snee.compiler.metadata.source.WebServiceSourceMetadata;
import uk.ac.manchester.cs.snee.compiler.queryplan.expressions.Attribute;
import uk.ac.manchester.cs.snee.evaluator.EndOfResultsException;
import uk.ac.manchester.cs.snee.evaluator.types.ReceiveTimeoutException;
import uk.ac.manchester.cs.snee.evaluator.types.TaggedTuple;
import uk.ac.manchester.cs.snee.evaluator.types.Tuple;
import uk.ac.manchester.cs.snee.operators.evaluator.receivers.PullServiceReceiver;
import uk.ac.manchester.cs.snee.operators.evaluator.receivers.SourceReceiver;
import uk.ac.manchester.cs.snee.operators.evaluator.receivers.UDPStreamReceiver;
import uk.ac.manchester.cs.snee.operators.logical.LogicalOperator;
import uk.ac.manchester.cs.snee.operators.logical.ReceiveOperator;

public class ReceiveOperatorImpl extends EvaluatorPhysicalOperator {
	//XXX: Write test for receive operator

	Logger logger = 
		Logger.getLogger(ReceiveOperatorImpl.class.getName());

	private SourceReceiver _streamReceiver;

	// streamName is the variable that stores the local extent name.
	private String _streamName;

	private boolean executing = false;

	/**
	 * Indicates when a tuple has been received by the 
	 * background thread
	 */
	private boolean receive = true;

	/**
	 * Once a tuple is received it is processed and this indicates 
	 * that it is ready for the parent operator
	 */
	private boolean newTupleReceived = false;

	/**
	 * Maintains a count of all the tuples received in the stream
	 */
	private long totalTuplesReceived;

	List<Attribute> attributes ;

	private int _tupleIndex = 0;
	private int currentTupleIndex = 0;

	private ReceiveOperator receiveOp;

	ReceiveTimeoutException rte;

	private Timer _receiverTaskTimer;

	private long _longSleepPeriod = 1000;

	private long _shortSleepPeriod;
	
	/**
	 * Instantiates the receive operator
	 * @param op
	 * @throws SchemaMetadataException 
	 * @throws SNEEConfigurationException 
	 */
	public ReceiveOperatorImpl(LogicalOperator op, int qid) 
	throws SchemaMetadataException {
		if (logger.isDebugEnabled()) {
			logger.debug("ENTER ReceiveOperatorImpl() for query " + 
					qid + " " +
					" with " + op);
		}
		
		// Instantiate this as a receive operator
		receiveOp = (ReceiveOperator) op;
		m_qid = qid;
		try {
			attributes = receiveOp.getAllReceivedAttributes();
		} catch (TypeMappingException e) {
			logger.error(e);
			throw new SchemaMetadataException("", e);
		}
		_streamName = receiveOp.getExtentName();
	
		if (logger.isTraceEnabled()) {
			logger.trace("Receiver for stream: " + _streamName);
		}
		if (logger.isDebugEnabled()) {
			logger.debug("RETURN ReceiveOperatorImpl()");
		}
	}

	@Override
	public void open() throws EvaluatorException {
		if (logger.isDebugEnabled()) {
			logger.debug("ENTER open()");
		}
		try {
			initializeStreamReceiver();

			// Start the receive operator to run immediately
			executing = true;
			_receiverTaskTimer = new Timer();
			ReceiverTask task = new ReceiverTask();
			_receiverTaskTimer.schedule(task, 0 //initial delay
					,_longSleepPeriod);  //subsequent rate
		} catch (ExtentDoesNotExistException e) {
			throw new EvaluatorException(e);
		} catch (SourceMetadataException e) {
			throw new EvaluatorException(e);
		}

		if (logger.isDebugEnabled()) {
			logger.debug("RETURN open()");
		}
	}

	private void initializeStreamReceiver() 
	throws ExtentDoesNotExistException, SourceMetadataException {
		if(logger.isTraceEnabled()) {
			logger.trace("ENTER initializeStreamReceiver()");
		}
		List<SourceMetadata> sources = receiveOp.getSources();
		for (SourceMetadata source : sources) {
			calculateSleepPeriods((PullSourceMetadata) source);
			SourceType sourceType = source.getSourceType();
			switch (sourceType) {
			case UDP_SOURCE:
				instantiateUdpDataSource(source);
				break;
			case PULL_STREAM_SERVICE:
				instantiatePullServiceDataSource(source);
				break;
			default:
				String msg = "Unknown data source type " + sourceType;
				logger.warn(msg);
				throw new SourceMetadataException(msg);
			}
		}
		//XXX: Mechanism above allows for more than one source, but streamReceiver is singular!
		_streamReceiver.open();
		if (logger.isTraceEnabled()) {
			logger.trace("RETURN initializeStreamReceiver()");
		}
	}

	private void calculateSleepPeriods(PullSourceMetadata source) 
	throws SourceMetadataException 
	{
		if (logger.isTraceEnabled()) {
			logger.trace("ENTER calculateSleepPeriod() with " + 
					source +
					" " + _streamName);
		}
		double rate = source.getRate(_streamName);
		double period = (1 / rate) * 1000;
		_shortSleepPeriod = (long) (period * 0.1);
		_longSleepPeriod = (long) (period - _shortSleepPeriod);
		if (logger.isTraceEnabled()) {
			logger.trace(_streamName + " long sleep set to " +
					_longSleepPeriod + ", short sleep set to " +
					_shortSleepPeriod);
			logger.trace("RETURN calculateSleepPeriod()");
		}
	}

	private void instantiateUdpDataSource(SourceMetadata source)
	throws ExtentDoesNotExistException {
		if (logger.isTraceEnabled())
			logger.trace("ENTER instantiateUdpDataSource() with " + 
					source.getSourceName());
		UDPSourceMetadata udpSource = (UDPSourceMetadata) source;
		_streamReceiver = new UDPStreamReceiver(
				udpSource.getHost(), 
				udpSource.getPort(_streamName), _shortSleepPeriod, 
				_streamName, attributes);
		if (logger.isTraceEnabled())
			logger.trace("RETURN instantiateUdpDataSource()");
	}

	private void instantiatePullServiceDataSource(SourceMetadata source) 
	throws ExtentDoesNotExistException {
		if (logger.isTraceEnabled())
			logger.trace("ENTER instantiatePullServiceDataSource() with " + 
					source.getSourceName());
		WebServiceSourceMetadata webSource = 
			(WebServiceSourceMetadata) source;
		_streamReceiver = new PullServiceReceiver(
				_streamName, webSource, _shortSleepPeriod);
		if (logger.isTraceEnabled())
			logger.trace("RETURN instantiatePullServiceDataSource()");
	}

	@Override
	public void close(){
		if (logger.isDebugEnabled()) {
			logger.debug("ENTER close()");
		}
		_receiverTaskTimer.cancel();
		_receiverTaskTimer.purge();
		executing = false;
		// Close the stream
		_streamReceiver.close();
		if (logger.isDebugEnabled()) {
			logger.debug("RETURN close()");
		}
	}

//	/* (non-Javadoc)
//	 * @see uk.ac.manchester.cs.snee.evaluator.operators.EvaluatorOperator#getNext()
//	 * Returns the next bag of tuples received from the stream source
//	 */
//	@Override
//	public Collection<Output> getNext() 
//	throws ReceiveTimeoutException, SNEEException, EndOfResultsException 
//	{
//		if (logger.isDebugEnabled()) {
//			logger.debug("ENTER getNext()");
//		}
//		TaggedTuple t;
//
//		while (receive){
//			if (!executing) {
//				logger.warn("Execution has ceased. Throw end of results " +
//				"exception.");
//				throw new EndOfResultsException();
//			}
//			/*
//			 * Guarded logging statement needed to resolve thread timing
//			 * issue on the Mac. Does not affect other OS.
//			 */
//			if (logger.isTraceEnabled()) {
//				//				logger.trace("Waiting for tuple to arrive");
//			}
//			if (currentTupleIndex < totalTuplesReceived){
//				// Create a list in which to capture the tuples
//				Collection<Output> tupleBag = new ArrayList<Output>();
//				long offset = (totalTuplesReceived >= MAX_BUFFER_SIZE )? (totalTuplesReceived-MAX_BUFFER_SIZE)+1: 0;
//				for (int i = currentTupleIndex; i <  totalTuplesReceived; i++){
//					t = (TaggedTuple)_tupleList.get(i-(int)offset);
//					if (t == null && rte != null ) {
//						logger.warn("getNext()" + rte);
//						throw rte;
//					} else if (t == null) {
//						String message = "Null received instead of tuple.";
//						logger.warn(message);
//						throw new SNEEException(message);
//					} else {
//						tupleBag.add(t);
//						currentTupleIndex++;
//						if (logger.isTraceEnabled()) {
//							logger.trace("Received tuple: "+ t.toString());
//						}
//					}
//				}
//
//				newTupleReceived = false;
//				if (!receive &&  rte != null ) {
//					logger.warn("getNext() " + rte);
//					throw rte;
//				}
//				if (logger.isDebugEnabled()) {
//					logger.debug("RETURN getNext(): number of tuples " +
//							"in bag " + tupleBag.size());
//				}
//				return tupleBag;
//
//			}
//
//		}
//		if (!receive &&  rte != null ) {
//			logger.warn("getNext() " + rte);
//			throw rte;
//		}
//		if (logger.isDebugEnabled()) {
//			logger.debug("RETURN getNext() with null");
//		}
//		return null;
//	}

	class ReceiverTask extends TimerTask {
	
		public void run(){
			if (logger.isDebugEnabled()) {
				logger.debug("ENTER run() for query " + m_qid);
			}
			Tuple tuple = null ;
			/* 
			 * Process a tuple that has been received in the 
			 * background thread
			 */
				try {
					// receive the tuple, blocking operation
					tuple = _streamReceiver.receive();
					// create a tagged tuple from the received tuple
					TaggedTuple taggedTuple = new TaggedTuple(tuple);
		
					setChanged();
					notifyObservers(taggedTuple);
				} catch (ReceiveTimeoutException e) {
					logger.warn("Receive Timeout Exception.", e);
					receive = false;
				} catch (SNEEException e) {
					logger.warn("Received a SNEEException.", e);
					receive = false;
				} catch (EndOfResultsException e) {
					executing = false;
				} catch (SNEEDataSourceException e) {
					logger.warn("Received a SNEEDataSourceException.", e);
					receive = false;
				} catch (TypeMappingException e) {
					logger.warn("Received a TypeMappingException.", e);
					receive = false;
				} catch (SchemaMetadataException e) {
					logger.warn("Received a SchemaMetadataException.", e);
					receive = false;
				}
			if (logger.isDebugEnabled()) {
				logger.debug("RETURN run() time to next execution: " + 
						_longSleepPeriod);
			}
		}
		
	}

	@Override
	public void update(Observable obj, Object observed) {
		if (logger.isDebugEnabled()) {
			logger.debug("ENTER update() for query " + m_qid);
		}
		logger.error("Receiver cannot be the parent of another operator");
		executing = false;
		if (logger.isDebugEnabled())
			logger.debug("RETURN update()");
	}
	
}