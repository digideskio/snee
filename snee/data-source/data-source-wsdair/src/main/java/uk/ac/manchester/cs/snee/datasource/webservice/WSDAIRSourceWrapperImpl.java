package uk.ac.manchester.cs.snee.datasource.webservice;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.cxf.ws.addressing.ReferenceParametersType;
import org.apache.log4j.Logger;
import org.ggf.namespaces._2005._12.ws_dai.DataResourceAddressType;
import org.ggf.namespaces._2005._12.ws_dai.DataResourceUnavailableFault;
import org.ggf.namespaces._2005._12.ws_dai.GetDataResourcePropertyDocumentRequest;
import org.ggf.namespaces._2005._12.ws_dai.GetResourceListRequest;
import org.ggf.namespaces._2005._12.ws_dai.GetResourceListResponse;
import org.ggf.namespaces._2005._12.ws_dai.InvalidResourceNameFault;
import org.ggf.namespaces._2005._12.ws_dai.NotAuthorizedFault;
import org.ggf.namespaces._2005._12.ws_dai.ServiceBusyFault;
import org.ggf.namespaces._2005._12.ws_dair.SQLDatasetType;
import org.ggf.namespaces._2005._12.ws_dair.SQLExecuteRequest;
import org.ggf.namespaces._2005._12.ws_dair.SQLExecuteResponse;
import org.ggf.namespaces._2005._12.ws_dair.SQLExpressionType;
import org.ggf.namespaces._2005._12.ws_dair.SQLPropertyDocumentType;
import org.ggf.namespaces._2005._12.ws_dair.SchemaDescription;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import uk.ac.manchester.cs.snee.SNEEDataSourceException;
import uk.ac.manchester.cs.snee.SNEEException;
import uk.ac.manchester.cs.snee.evaluator.types.Tuple;
import uk.ac.manchester.cs.snee.metadata.schema.ExtentMetadata;
import uk.ac.manchester.cs.snee.metadata.schema.ExtentType;
import uk.ac.manchester.cs.snee.metadata.schema.SchemaMetadataException;
import uk.ac.manchester.cs.snee.metadata.schema.TypeMappingException;
import uk.ac.manchester.cs.snee.metadata.schema.Types;

public class WSDAIRSourceWrapperImpl extends SourceWrapperAbstract 
implements SourceWrapper {

	private static Logger logger = 
		Logger.getLogger(WSDAIRSourceWrapperImpl.class.getName());
	
	private static String LANGUAGE_URI =
		"http://www.sqlquery.org/sql-92";
//
//	private static String WSDAI_NS =
//		"http://www.ggf.org/namespaces/2005/12/WS-DAI/";
	
	private WSDAIRAccessServiceClient _wsdairClient;

    public WSDAIRSourceWrapperImpl(String url, Types types) 
    throws MalformedURLException {
    	super(url, types);
    	if (logger.isDebugEnabled())
    		logger.debug("ENTER WSDAIRSourceWrapper() with URL " + url);
    	_wsdairClient = createServiceClient(url);
        if (logger.isDebugEnabled())
        	logger.debug("RETURN WSDAIRSourceWrapper()");
    }

	protected WSDAIRAccessServiceClient createServiceClient(String url) 
    throws MalformedURLException {
		if (logger.isTraceEnabled())
			logger.trace("ENTER createServiceClient() with " + url);
		WSDAIRAccessServiceClient wsdairClient = 
			new WSDAIRAccessServiceClient(url);
		if (logger.isTraceEnabled())
			logger.trace("RETURN createServiceClient()");
		return wsdairClient;
	}
    
    public List<String> getResourceNames() 
    throws SNEEDataSourceException 
    {
    	if (logger.isDebugEnabled())
    		logger.debug("ENTER getResourceNames()");
    	GetResourceListRequest request = new GetResourceListRequest();
    	GetResourceListResponse response;
    	List<String> resourceNames = new ArrayList<String>();
    	try {
    		response = _wsdairClient.getResourceList(request);
    		List<DataResourceAddressType> addresses = 
    			response.getDataResourceAddress();
    		String nameLog = "";
    		if (logger.isTraceEnabled()) {
    			logger.trace("Number of addresses: " + addresses.size());
    		}
    		for (DataResourceAddressType address : addresses) {
    			ReferenceParametersType refParams = 
    				address.getReferenceParameters();
    			Node xmlNode = (Node) refParams.getAny().get(0);
    			String resourceName = xmlNode.getTextContent();
    			resourceNames.add(resourceName);
				nameLog += resourceName + ", ";
    		}
    		if (logger.isTraceEnabled()) {
    			logger.trace("Resource Names: " + 
    					nameLog.substring(0, nameLog.lastIndexOf(",")));
    		}
    	} catch (NotAuthorizedFault e) {
    		String msg = "Not authorized to access service.";
    		logger.warn(msg, e);
    		throw new SNEEDataSourceException(msg, e);
    	} catch (ServiceBusyFault e) {
    		String msg = "Service currently busy.";
    		logger.warn(msg, e);
    		throw new SNEEDataSourceException(msg, e);
    	}
		if (logger.isDebugEnabled())
			logger.debug("RETURN getResourceNames(), " +
					"number of resources " + resourceNames.size());
		return resourceNames;
	}

	public List<ExtentMetadata> getSchema(String resourceName) 
    throws SNEEDataSourceException, SchemaMetadataException, 
    TypeMappingException {
    	if (logger.isDebugEnabled())
    		logger.debug("ENTER getSchema() with " + resourceName);
    	//XXX: Assumes that a source only provides a single extent
    	List<ExtentMetadata> extents = new ArrayList<ExtentMetadata>();
		try {
	    	GetDataResourcePropertyDocumentRequest request = 
	    		new GetDataResourcePropertyDocumentRequest();
	    	request.setDataResourceAbstractName(resourceName);
	    	SQLPropertyDocumentType propDoc = 
	    		_wsdairClient.getSQLPropertyDocument(request);
			if (logger.isTraceEnabled()) {
				logger.trace("Retrieving Schema element");
			}
			SchemaDescription schemaDescription = 
				propDoc.getSchemaDescription();
			if (schemaDescription != null) {					
				Element schemaDoc = 
					(Element) schemaDescription.getAny().get(0);
				SchemaParser schemaParser = 
					new OgsadaiSchemaParser(schemaDoc, _types);
				extents  = extractSchema(schemaParser, ExtentType.TABLE);
			}
		} catch (SNEEDataSourceException e) {
			if (!e.getMessage().equals("Wrong service type")) {
				throw e;
			}
    	} catch (InvalidResourceNameFault e) {
    		String msg = "Resource name " + resourceName + 
    			" unknown to service " + _url + ".";
    		logger.warn(msg, e);
    		throw new SNEEDataSourceException(msg, e);
		} catch (DataResourceUnavailableFault e) {
    		String msg = "Resource " + resourceName + 
    			" currently unavailable on " + _url + ".";
    		logger.warn(msg, e);
    		throw new SNEEDataSourceException(msg, e);
		} catch (NotAuthorizedFault e) {
    		String msg = "Not authorised to use service " + _url + ".";
    		logger.warn(msg, e);
    		throw new SNEEDataSourceException(msg, e);
		} catch (ServiceBusyFault e) {
    		String msg = "Service " + _url + " is busy.";
    		logger.warn(msg, e);
    		throw new SNEEDataSourceException(msg, e);
		}
    	if (logger.isDebugEnabled())
    		logger.debug("RETURN getSchema() with " + extents);
    	return extents;
    }

	public List<Tuple> executeQuery(String resourceName, String query) 
	throws SNEEDataSourceException, TypeMappingException,
	SchemaMetadataException, SNEEException {
		if (logger.isDebugEnabled()) {
			logger.debug("ENTER executeQuery() on resource " +
					resourceName + " query " + query);
		}
		SQLExecuteRequest request = new SQLExecuteRequest();
		request.setDataResourceAbstractName(resourceName);
		request.setDatasetFormatURI(DATASET_FORMAT);
		SQLExpressionType sqlExpression = new SQLExpressionType();
		sqlExpression.setExpression(query);
		sqlExpression.setLanguage(LANGUAGE_URI);
		request.setSQLExpression(sqlExpression);
		SQLExecuteResponse response = _wsdairClient.sqlExecute(request);
		List <Tuple> tuples = processSQLQueryResponse(response);
		if (logger.isDebugEnabled()) {
			logger.debug("RETURN executeQuery()");
		}
		return tuples;
	}
	
	/**
	 * Processes the response from the service to convert the 
	 * WebRowSet into tuples for the SNEE stream evaluation engine
	 * @param response the answer document from the service containing a WebRowSet
	 * @return list of tuples that can be used by the SNEE evaluation engine
	 * @throws SNEEDataSourceException Error processing data from data source
	 * @throws SchemaMetadataException 
	 * @throws TypeMappingException 
	 * @throws SNEEException 
	 * @throws SNEEException 
	 */
	private List<Tuple> processSQLQueryResponse(
			SQLExecuteResponse response) 
	throws SNEEDataSourceException, TypeMappingException, 
	SchemaMetadataException, SNEEException {
		if (logger.isTraceEnabled())
			logger.trace("ENTER processSQLQueryResponse() with " +
					response);
		List<Tuple> tuples = new ArrayList<Tuple>();
		SQLDatasetType sqlDataset = response.getSQLDataset();
		String formatURI = sqlDataset.getDatasetFormatURI();
		if (formatURI.trim().equalsIgnoreCase(DATASET_FORMAT)) {
			if (logger.isTraceEnabled()) {
				logger.trace("Processing dataset with format " +
						formatURI);
			}
			List<Object> datasets = 
				sqlDataset.getDatasetData().getContent();
			if (logger.isTraceEnabled()) {
				logger.trace("Number of datasets: " + datasets.size());
			}
			for (Object data : datasets) {
				tuples.addAll(processWRSDataset((String) data));
			}
		} else {
			String msg = "Unknown dataset format URI " + formatURI + 
				". Unable to process dataset.";
			logger.warn(msg);
			throw new SNEEDataSourceException(msg);
		}
		if (logger.isTraceEnabled())
			logger.trace("RETURN processSQLQueryResponse() with " +
					tuples );
		return tuples;
	}
    
	public List<Tuple> getData(String resourceName)
	throws SNEEDataSourceException, TypeMappingException, 
	SchemaMetadataException, SNEEException
	{
		if (logger.isDebugEnabled()) {
			logger.debug("ENTER getData() with " + resourceName);
		}
		List<Tuple> tuples = new ArrayList<Tuple>();
		if (logger.isDebugEnabled()) {
			logger.debug("RETURN getData() #tuples " + tuples.size());
		}
		return tuples;
	}
}