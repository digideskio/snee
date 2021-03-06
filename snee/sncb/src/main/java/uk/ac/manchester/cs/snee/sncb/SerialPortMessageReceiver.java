package uk.ac.manchester.cs.snee.sncb;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import net.tinyos.message.Message;
import net.tinyos.message.MoteIF;
import net.tinyos.packet.BuildSource;
import net.tinyos.util.PrintStreamMessenger;

import org.apache.log4j.Logger;

import uk.ac.manchester.cs.snee.metadata.schema.AttributeType;
import uk.ac.manchester.cs.snee.metadata.schema.SchemaMetadataException;
import uk.ac.manchester.cs.snee.metadata.schema.TypeMappingException;
import uk.ac.manchester.cs.snee.common.Constants;
import uk.ac.manchester.cs.snee.compiler.queryplan.expressions.Attribute;
import uk.ac.manchester.cs.snee.compiler.queryplan.expressions.EvalTimeAttribute;
import uk.ac.manchester.cs.snee.evaluator.types.EvaluatorAttribute;
import uk.ac.manchester.cs.snee.evaluator.types.Output;
import uk.ac.manchester.cs.snee.evaluator.types.TaggedTuple;
import uk.ac.manchester.cs.snee.evaluator.types.Tuple;
import uk.ac.manchester.cs.snee.operators.logical.DeliverOperator;
import uk.ac.manchester.cs.snee.operators.sensornet.SensornetDeliverOperator;
import uk.ac.manchester.cs.snee.sncb.tos.CodeGenUtils;

//Based on TinyOS MsgReader.java class
public class SerialPortMessageReceiver extends Observable 
implements net.tinyos.message.MessageListener, SNCBSerialPortReceiver {

	private Logger logger = 
		Logger.getLogger(TinyOS_SNCB.class.getName());
	
	private MoteIF moteIF;
	  
	private SensornetDeliverOperator delOp;
	
	private Message _msg;
	
	private String _source;
	
	public SerialPortMessageReceiver(String source, SensornetDeliverOperator delOp) throws Exception {
		_source = source;
		
		if (_source != null) {
			moteIF = new MoteIF(BuildSource.makePhoenix(_source, PrintStreamMessenger.err));
	    }
	    else {
	    	moteIF = new MoteIF(BuildSource.makePhoenix(PrintStreamMessenger.err));
	    }
	    this.delOp = delOp;
	}

	@Override
	public void messageReceived(int to, Message message) {
		long t = System.currentTimeMillis();
	    //    Date d = new Date(t);
	   // System.out.print("" + t + ": ");
	   // System.out.println(message);
	    
	    try {
			List<Output> resultList = new ArrayList<Output>();
			int tuplesPerMessage = getTuplesPerMessage(message);
			for (int i=0; i<tuplesPerMessage; i++) {
				Tuple newTuple = new Tuple();
				boolean dummyTuple = false;
				for (Attribute attr : this.delOp.getAttributes()) {
					EvaluatorAttribute evalAttr = getAttribute(attr, message, i);
					newTuple.addAttribute(evalAttr);
					if (attr instanceof EvalTimeAttribute) {
						if (evalAttr.getData().equals(65535)) {
							dummyTuple = true;
							break;
						}
					}
				} 
				if (dummyTuple) {
					continue; //do not add to result list
				}
				logger.trace("Tuple received at time " + t + ": "+newTuple.toString());
				TaggedTuple newTaggedTuple = new TaggedTuple(newTuple);
				resultList.add(newTaggedTuple);
			}
			//TODO: make the ResultStore an observer of this object.
			if (!resultList.isEmpty()) {
				setChanged();
				notifyObservers(resultList);
			}			
	    } catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	  }

	private int getTuplesPerMessage(Message message) 
	throws SecurityException, NoSuchMethodException, IllegalArgumentException, 
	IllegalAccessException, InvocationTargetException {
		//This bit of code is based on http://java.sun.com/developer/technicalArticles/ALT/Reflection/
		Class msgClass = message.getClass();
		Method meth = msgClass.getMethod("numElements_tuples_evalEpoch", new Class[0]);
		//Method meth = msgClass.getMethod("numElements_tuples_evalEpoch", new Class[]{Integer.TYPE});
		Integer tuplesPerMessage = (Integer) meth.invoke(message, new Object[0]);		
		//Integer tuplesPerMessage = (Integer) meth.invoke(message, new Object[]{new Integer(0)});
		return tuplesPerMessage;
	}
	
	
	/**
	 * For each attribute, deliver operator and derive getElementXXXXXX method 
	 * name and obtain value.
	 * @param attr
	 * @param message
	 * @param index
	 * @return
	 * @throws SchemaMetadataException
	 * @throws TypeMappingException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 */
	private EvaluatorAttribute getAttribute(Attribute attr, Message message, int index) 
	throws SchemaMetadataException, TypeMappingException, IllegalArgumentException, 
	IllegalAccessException, InvocationTargetException, SecurityException, NoSuchMethodException {
		if (logger.isTraceEnabled())
				logger.trace("ENTER getAttribute()");
		String nesCAttrName = CodeGenUtils.getNescAttrName(attr);
		String methodName = "getElement_tuples_"+nesCAttrName;
		Class paramTypes[] = new Class[1];
		paramTypes[0] = Integer.TYPE;
		//This bit of code is based on http://java.sun.com/developer/technicalArticles/ALT/Reflection/
		Class msgClass = message.getClass();
		Method meth = msgClass.getMethod(methodName, paramTypes);
		Object argList[] = new Object[1];
		argList[0] = new Integer(index);
		Object dataObj = meth.invoke(message, argList);
//Integer data = (Integer)retObj;
			
		String extentName = attr.getExtentName();
		//TODO: Had to change this after merging revs 269-271. Hopefully did right thing.
		String attrName = attr.getAttributeDisplayName();
		AttributeType attrType = attr.getType();
			
		EvaluatorAttribute evalAttr = new EvaluatorAttribute(extentName, attrName, attrType, dataObj);
		if (logger.isTraceEnabled())
			logger.trace("ENTER getAttribute()");
		return evalAttr;
	  }
	  
	protected void addMsgType(Message msg) {
		this.moteIF.registerListener(msg, this);
		this._msg = msg;
	}
	
	public void close() {
		this.moteIF.deregisterListener(_msg, this);
		this.moteIF.getSource().shutdown();
		super.deleteObservers();		
	}
	
	public void pause() {
		this.moteIF.deregisterListener(_msg, this);
		this.moteIF.getSource().shutdown();
		super.deleteObservers();
	}
	
	public void resume() {
		if (_source != null) {
			moteIF = new MoteIF(BuildSource.makePhoenix(_source, PrintStreamMessenger.err));
	    }
	    else {
	    	moteIF = new MoteIF(BuildSource.makePhoenix(PrintStreamMessenger.err));
	    }
		this.moteIF.registerListener(_msg, this);
	}

}
