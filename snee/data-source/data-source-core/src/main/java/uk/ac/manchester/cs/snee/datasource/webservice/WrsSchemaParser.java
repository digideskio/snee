package uk.ac.manchester.cs.snee.datasource.webservice;

import java.io.StringReader;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.sql.rowset.WebRowSet;

import org.apache.log4j.Logger;

import uk.ac.manchester.cs.snee.compiler.queryplan.expressions.Attribute;
import uk.ac.manchester.cs.snee.compiler.queryplan.expressions.DataAttribute;
import uk.ac.manchester.cs.snee.metadata.schema.AttributeType;
import uk.ac.manchester.cs.snee.metadata.schema.SchemaMetadataException;
import uk.ac.manchester.cs.snee.metadata.schema.TypeMappingException;
import uk.ac.manchester.cs.snee.metadata.schema.Types;

import com.sun.rowset.WebRowSetImpl;

public class WrsSchemaParser extends SchemaParserAbstract {

	private Logger logger = 
		Logger.getLogger(WrsSchemaParser.class.getName());

	private ResultSetMetaData metadata;
	
	public WrsSchemaParser(String schemaDoc, Types types) 
	throws SchemaMetadataException {
		super(types);
		//Only print schema doc if trace level set
		if (logger.isTraceEnabled()) {
			logger.debug("ENTER WrsSchemaParser() with " + schemaDoc);
		} else if (logger.isDebugEnabled()) {
			logger.debug("ENTER WrsSchemaParser()");
		}
		try {
			WebRowSet wrs = new WebRowSetImpl();
			wrs.readXml(new StringReader(schemaDoc));
			metadata = wrs.getMetaData();
		} catch (SQLException e) {
			String msg = "Problem processing webrowset schema.";
			logger.warn(msg, e);
			throw new SchemaMetadataException(msg, e);
		}
		if (logger.isDebugEnabled())
			logger.debug("RETURN WrsSchemaParser()");
	}

	@Override
	public Collection<String> getExtentNames() 
	throws SchemaMetadataException {
		if (logger.isDebugEnabled())
			logger.debug("ENTER getExtentNames()");
		String extentName;
		try {
			extentName = metadata.getTableName(1).toLowerCase();
		} catch (SQLException e) {
			String msg = "Problem reading extent name.";
			logger.warn(msg, e);
			throw new SchemaMetadataException(msg, e);
		}
		Collection<String> extentNames = new HashSet<String>();
		extentNames.add(extentName);
		if (logger.isDebugEnabled())
			logger.debug("RETURN getExtentNames() #extents=" + 
					extentNames.size());
		return extentNames;
	}

	@Override
	public List<Attribute> getColumns(String extentName) 
	throws TypeMappingException, SchemaMetadataException {
		if (logger.isDebugEnabled())
			logger.debug("ENTER getColumns()");
		List<Attribute> response = new ArrayList<Attribute>();
		try {
			for (int i = 1; i <= metadata.getColumnCount(); i++) {
				String colName = metadata.getColumnName(i);
				AttributeType colType = 
					inferType(metadata.getColumnType(i));
				Attribute attr = 
					new DataAttribute(extentName, colName, colType);
				response.add(attr);
			}
		} catch (SQLException e) {
			String msg = "Problem reading column information.";
			logger.warn(msg, e);
			throw new SchemaMetadataException(msg, e);
		}
		if (logger.isDebugEnabled())
			logger.debug("RETURN getColumns()");
		return response;
	}

}
