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

package uk.ac.manchester.cs.snee;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import uk.ac.manchester.cs.snee.common.SNEEConfigurationException;
import uk.ac.manchester.cs.snee.common.SNEEProperties;
import uk.ac.manchester.cs.snee.common.SNEEPropertyNames;
import uk.ac.manchester.cs.snee.compiler.QueryCompiler;
import uk.ac.manchester.cs.snee.compiler.metadata.Metadata;
import uk.ac.manchester.cs.snee.compiler.metadata.schema.ExtentDoesNotExistException;
import uk.ac.manchester.cs.snee.compiler.metadata.schema.ExtentMetadata;
import uk.ac.manchester.cs.snee.compiler.metadata.schema.SchemaMetadataException;
import uk.ac.manchester.cs.snee.compiler.metadata.schema.TypeMappingException;
import uk.ac.manchester.cs.snee.compiler.metadata.schema.UnsupportedAttributeTypeException;
import uk.ac.manchester.cs.snee.compiler.metadata.source.SourceMetadataException;
import uk.ac.manchester.cs.snee.compiler.metadata.source.SourceType;
import uk.ac.manchester.cs.snee.compiler.metadata.source.sensornet.TopologyReaderException;
import uk.ac.manchester.cs.snee.compiler.params.QueryParameters;
import uk.ac.manchester.cs.snee.compiler.params.qos.QoSException;
import uk.ac.manchester.cs.snee.compiler.queryplan.LAF;
import uk.ac.manchester.cs.snee.evaluator.Dispatcher;
import uk.ac.manchester.cs.snee.evaluator.StreamResultSet;
import uk.ac.manchester.cs.snee.evaluator.StreamResultSetImpl;

/**
 * Controller class for SNEEql query compilation and evaluation
 * Compiles a declarative SNEEql query and controls the execution of it 
 * over the available sensor network and robust networks.
 */
public class SNEEController implements SNEE {

	private static Logger logger = 
		Logger.getLogger(SNEEController.class.getName());
	
	/**
	 * Metadata stored about extents and data sources
	 */
	private Metadata _schema;
	
	/**
	 * The compiler object for compiling queries
	 */
	private QueryCompiler _compiler;
	
	/**
	 * The evaluator object for running queries
	 */
	private Dispatcher _dispatcher;
	
	private Map<Integer, StreamResultSet> _queryResults = 
		new HashMap<Integer, StreamResultSet>();
	
	/**
	 * Stores the query plan for the registered query
	 */
	private Map<Integer, LAF> _queryPlans = new HashMap<Integer, LAF>();
	
	private int _nextQueryID = 1;

	/**
	 * Initialise SNEE based on the contents of the configuration files
	 * @throws SNEEException 
	 * @throws SNEEConfigurationException 
	 */
	public SNEEController() 
	throws SNEEException, SNEEConfigurationException {
		if (logger.isInfoEnabled())
			logger.info("ENTER SNEEController()");
		InputStream propsStream = 
			this.getClass().getResourceAsStream("snee.properties");
		Properties props = new Properties();
		try {
			props.load(propsStream);
			propsStream.close();
			SNEEProperties.initialise(props);
			initialise();
		} catch (IOException e) {
			logger.error("Could not load properties file.");
			throw new SNEEConfigurationException(e);
		} 
		if (logger.isInfoEnabled())
			logger.info("RETURN SNEEController()");
	}
	
	public SNEEController(String propertiesFilename) 
	throws SNEEException, SNEEConfigurationException {
		if (logger.isInfoEnabled())
			logger.info("ENTER SNEEController() properties file " + propertiesFilename);
		logger.info(this.getClass().getClassLoader().getResource("/"));
		InputStream propsStream = 
			this.getClass().getClassLoader().getResourceAsStream(propertiesFilename);
		logger.trace("Creating properties table");
		Properties props = new Properties();
		try {
			logger.trace("Loading properties");
			props.load(propsStream);
			propsStream.close();
			SNEEProperties.initialise(props);
			initialise();
		} catch (IOException e) {
			logger.error("Could not load properties file.");
			throw new SNEEConfigurationException(e);
		} 
		if (logger.isInfoEnabled())
			logger.info("RETURN SNEEController()");
	}
	
	public SNEEController(Properties props) 
	throws SNEEException, SNEEConfigurationException {
		if (logger.isInfoEnabled())
			logger.info("ENTER SNEEController(Properties props)");
		SNEEProperties.initialise(props);
		initialise();
		if (logger.isInfoEnabled())
			logger.info("RETURN SNEEController(Properties props)");
	}

	/**
	 * Method used by public constructors to initialise the logger,
	 * and local modules (metadata, compiler, and query evaluator).
	 * 
	 * @throws SNEEException
	 * @throws SNEEConfigurationException 
	 */
	private void initialise() 
	throws SNEEException, SNEEConfigurationException {
		if (logger.isDebugEnabled())
			logger.debug("ENTER initialise()");

		try {			
			/* Process metadata */
			_schema = initialiseSchema();
			
			/* Initialise compiler */
			_compiler = initialiseQueryCompiler();
			
			/* Initialise dispatcher */
			_dispatcher = initialiseDispatcher();

		} catch (TypeMappingException e) {
			String msg = "Problem instantiating logical schema " + 
				SNEEProperties.getSetting(SNEEPropertyNames.INPUTS_LOGICAL_SCHEMA_FILE) + ". " + e;
			logger.fatal(msg);
			throw new SNEEException(msg, e);
		} catch (SchemaMetadataException e) {
			String msg = "Problem instantiating logical schema " + 
				SNEEProperties.getSetting(SNEEPropertyNames.INPUTS_LOGICAL_SCHEMA_FILE) + ". " + e;
			logger.fatal(msg);
			throw new SNEEException(msg, e);
		} catch (SourceMetadataException e) {
			String msg = "Problem instantiating physical schema " + 
				SNEEProperties.getSetting(SNEEPropertyNames.INPUTS_PHYSICAL_SCHEMA_FILE) + ". " + e;
			logger.fatal(msg);
			throw new SNEEException(msg, e);
		} catch (MetadataException e) {
			String msg = "Problem instantiating schema " +
				SNEEProperties.getSetting(SNEEPropertyNames.INPUTS_LOGICAL_SCHEMA_FILE) + ". " + e;
			logger.fatal(msg);
			throw new SNEEException(msg, e);
		} catch (UnsupportedAttributeTypeException e) {
			String msg = "Problem instantiating logical schema " + 
				SNEEProperties.getSetting(SNEEPropertyNames.INPUTS_LOGICAL_SCHEMA_FILE) + ". " + e;
			logger.fatal(msg);
			throw new SNEEException(msg, e);
		} catch (TopologyReaderException e) {
			String msg = "Problem reading topology file ";
			logger.fatal(msg);
			throw new SNEEException(msg, e);			
		} catch (MalformedURLException e) {
			String msg = "Problem creating link to external service ";
			logger.fatal(msg);
			throw new SNEEException(msg, e);			
		} catch (SNEEDataSourceException e) {
			String msg = "Problem creating link to external service ";
			logger.fatal(msg);
			throw new SNEEException(msg, e);			
		}
		
		logger.info("SNEE configured");
		if (logger.isDebugEnabled())
			logger.debug("RETURN initialise()");
	}

	protected Metadata initialiseSchema() 
	throws MetadataException, SchemaMetadataException, 
	TypeMappingException, UnsupportedAttributeTypeException, 
	SourceMetadataException, SNEEConfigurationException, 
	TopologyReaderException, MalformedURLException,
	SNEEDataSourceException 
	{
		if (logger.isTraceEnabled())
			logger.trace("ENTER initialiseSchema()");
		Metadata metadata = new Metadata();
		return metadata;
	}

	protected QueryCompiler initialiseQueryCompiler() 
	throws TypeMappingException {
		return new QueryCompiler(_schema);
	}

	protected Dispatcher initialiseDispatcher() {
		return new Dispatcher(_schema);
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.manchester.cs.snee.SNEE#getExtents()
	 */
	public Collection<String> getExtents() {
		if (logger.isDebugEnabled())
			logger.debug("ENTER getExtents()");
		Collection<String> extentNames = _schema.getExtentNames();
		if (logger.isDebugEnabled())
			logger.debug("RETURN getExtents() #extents=" + 
					extentNames.size());
		return extentNames;
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.manchester.cs.snee.SNEE#getExtentDetails(java.lang.String)
	 */
	public ExtentMetadata getExtentDetails(String extentName) 
	throws ExtentDoesNotExistException {
		if (logger.isDebugEnabled())
			logger.debug("ENTER getExtentDetails() with " + extentName);
		ExtentMetadata extent = _schema.getExtentMetadata(extentName);
		if (logger.isDebugEnabled())
			logger.debug("RETURN getExtentDetails()");
		return extent;
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.manchester.cs.snee.SNEE#addQuery(java.lang.String)
	 */
	public int addQuery(String query, String queryParamsFile) 
	throws EvaluatorException, SNEECompilerException, SNEEException,
	MetadataException 
	{
		if (logger.isDebugEnabled()) {
			logger.debug("ENTER addQuery() with " + query);
		}
		if (query == null || query.trim().equals("")) {
			logger.warn("Null or empty query passed in");
			throw new SNEECompilerException("Null or empty query passed in.");
		}
		int queryId = getNextQueryId();
		if (logger.isInfoEnabled()) 
			logger.info("Assigned ID " + queryId + " to query\n");
		if (logger.isInfoEnabled()) 
			logger.info("Reading query " + queryId + " parameters\n");
		QueryParameters queryParams = null;
		if (queryParamsFile != null) {
			try {
				queryParams = new QueryParameters(queryId, queryParamsFile);
			} catch (QoSException e) {
				logger.warn("Throwing compilation exception. Cause " + e);
				throw new SNEECompilerException(e.getLocalizedMessage());
			}
		}
		if (logger.isInfoEnabled()) 
			logger.info("Compiling query " + queryId + "\n");
		compileQuery(queryId, query, queryParams);
		if (logger.isInfoEnabled())
			logger.info("Successfully compiled query " + queryId);
		dispatchQuery(queryId);
		if (logger.isInfoEnabled())
			logger.info("Successfully started evaluation of query " + queryId);

		if (logger.isDebugEnabled()) {
    		logger.debug("RETURN addQuery() with query id " + queryId);
    	}
		return queryId;
	}
	
	/* (non-Javadoc)
	 * @see uk.ac.manchester.cs.snee.SNEE#removeQuery(int)
	 */
	public void removeQuery(int queryId) throws SNEEException {
		if (logger.isDebugEnabled()) {
			logger.debug("ENTER removeQuery() with " + queryId);
		}
		_dispatcher.stopQuery(queryId);
		if (logger.isDebugEnabled()) {
    		logger.debug("RETURN removeQuery()");
    	}
	}
		
	/**
	 * Dispatch the query for evaluation
	 * 
	 * @return the query identifier generated for the query
	 * @throws SNEEException Problem starting the query evaluation
	 * @throws SchemaMetadataException 
	 * @throws EvaluatorException 
	 */
	private int dispatchQuery(int queryId) 
	throws SNEEException, MetadataException, EvaluatorException 
	{
		if (logger.isTraceEnabled()) {
			logger.trace("ENTER dispatchQuery() with " + queryId);
		}
		StreamResultSet resultSet = createStreamResultSet();
		
		_dispatcher.startQuery(queryId, resultSet, 
				_queryPlans.get(queryId));
		_queryResults.put(queryId, resultSet);
		
		if (logger.isTraceEnabled()) {
			logger.trace("RETURN dispatchQuery() with queryId " + queryId);
		}
		return queryId;
	}

	protected StreamResultSet createStreamResultSet() {
		StreamResultSet resultSet = new StreamResultSetImpl();
		return resultSet;
	}

	private int getNextQueryId() {
		if (logger.isTraceEnabled()) {
			logger.trace("ENTER getNextQueryId()");
		}
		int queryId = _nextQueryID;
		_nextQueryID++;
		if (logger.isTraceEnabled()) {
			logger.trace("RETURN getNextQueryId() with queryId " + queryId);
		}
		return queryId;
	}

	private void compileQuery(int queryID, String query, 
			QueryParameters queryParams) 
	throws SNEECompilerException {
		if (logger.isTraceEnabled()) {
			logger.trace("ENTER compilerQuery() with queryID " + 
					queryID + "\n\tquery: " + query);
		}
			try {
				LAF queryPlan = _compiler.compileQuery(queryID, query);
				_queryPlans.put(queryID, queryPlan);
			} catch (Exception e) {
				String msg = "Problem compiling query.";
				logger.warn(msg);
				throw new SNEECompilerException(msg, e);
			}
		if (logger.isTraceEnabled()) {
			logger.trace("RETURN compileQuery()");
		}
	}

	/* (non-Javadoc)
	 * @see uk.ac.manchester.cs.snee.SNEE#close()
	 */
	public void close() {
		if (logger.isDebugEnabled()) {
			logger.debug("ENTER close()");
		}		
		// Stop the query evaluator
		_dispatcher.close();
		if (logger.isDebugEnabled()) {
			logger.debug("RETURN close()");
		}
	}

	/**
	 * Retrieve the ResultSet for a specified query if it exists.
	 * @param queryId Identifier of the query for which the result set should be returned
	 * @return ResultSet for the query
	 * @throws SNEEException Specified queryId does not exist
	 */
	public StreamResultSet getResultSet(int queryId) 
	throws SNEEException {
		if (logger.isDebugEnabled()) {
			logger.debug("ENTER getResultStore() with query=" + queryId);
		}
		StreamResultSet resultSet;
		if (_queryResults.containsKey(queryId)) {
			resultSet = _queryResults.get(queryId);
		} else {
			String msg = "No ResultSet for query " + queryId;
			logger.warn(msg);
			throw new SNEEException(msg);
		}
		if (logger.isDebugEnabled()) {
			logger.debug("RETURN getResultStore()");
		}
		return resultSet;
	}

	/* (non-Javadoc)
	 * @see uk.ac.manchester.cs.snee.SNEE#addServiceSource(java.lang.String)
	 */
	public void addServiceSource(String name, String url, 
			SourceType interfaceType) 
	throws MalformedURLException, SNEEDataSourceException,
	MetadataException {
		if (logger.isDebugEnabled())
			logger.debug("ENTER addServiceSource() with name=" +
					name + " type=" + interfaceType + " url="+ url);
		try {
			_schema.addServiceSource(name, url, 
					SourceType.PULL_STREAM_SERVICE);
		} catch (SchemaMetadataException e) {
			logger.warn("Throwing a MetadataException. Cause " + e);
			throw new MetadataException(e.getLocalizedMessage());
		} catch (TypeMappingException e) {
			logger.warn("Throwing a MetadataException. Cause " + e);
			throw new MetadataException(e.getLocalizedMessage());
		} catch (SourceMetadataException e) {
			logger.warn("Throwing a MetadataException. Cause " + e);
			throw new MetadataException(e.getLocalizedMessage());
		}
		logger.info("Web service source added with url \n\t" + url);
		if (logger.isDebugEnabled())
			logger.debug("RETURN addServiceSource()");

	}
	
}