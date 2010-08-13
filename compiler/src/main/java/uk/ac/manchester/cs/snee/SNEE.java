package uk.ac.manchester.cs.snee;

import java.net.MalformedURLException;
import java.util.Collection;

import uk.ac.manchester.cs.snee.compiler.metadata.schema.ExtentMetadata;
import uk.ac.manchester.cs.snee.compiler.metadata.source.SourceType;
import uk.ac.manchester.cs.snee.evaluator.StreamResultSet;


public interface SNEE {

	/**
	 * Adds a service source to the set of available data for querying.
	 * @param name name of the service
	 * @param url endpoint reference for the interface
	 * @param interfaceType type of the interface
	 * @throws MalformedURLException
	 * @throws MetadataException
	 * @throws SNEEDataSourceException 
	 */
	public void addServiceSource(String name, String url, 
			SourceType interfaceType) 
	throws MalformedURLException, MetadataException,
	SNEEDataSourceException;
	
	/**
	 * Return a list of the extent names available in the schema
	 * @return list of available extents
	 */
	public Collection<String> getExtents();
	
	/**
	 * Retrieve the metadata about a specified extent.
	 * @param extentName name of the extent
	 * @return details about the extent
	 * @throws MetadataException name is not an extent in the schema
	 */
	public ExtentMetadata getExtentDetails(String extentName) 
	throws MetadataException;
	
	/**
	 * Adds a query to the set of registered queries and returns the generated
	 * query identifier. 
	 * 
	 * It takes a query statement as input, generates a query plan for its 
	 * evaluation, and adds it to the set of registered query plans.
	 * 
	 * @param query Statement of the query
	 * @throws SNEEException
	 * @throws MetadataException 
	 * @throws EvaluatorException 
	 * @throws SNEECompilerException 
	 */
	public int addQuery(String query, String parametersFile) 
	throws SNEECompilerException, MetadataException, 
	EvaluatorException, SNEEException;

	/**
	 * Retrieve the ResultSet for a specified query if it exists.
	 * @param queryId Identifier of the query for which the result set should be returned
	 * @return ResultSet for the query
	 * @throws SNEEException Specified queryId does not exist
	 */
	public StreamResultSet getResultSet(int queryId) 
	throws SNEEException;
	
	/**
	 * Removes a query from the set of registered queries. It takes a query 
	 * identifier and stops the required query evaluation if it exists.
	 * 
	 * @param queryId Identifier of the query to be stopped
	 * @throws SNEEException An exception is thrown if the query identifier does not exist
	 */
	public void removeQuery(int queryId) 
	throws SNEEException;

	/**
	 * Close SNEE down gracefully
	 */
	public void close();

}