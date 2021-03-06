package uk.ac.manchester.cs.snee.client;

import java.io.IOException;

import org.apache.log4j.PropertyConfigurator;

import uk.ac.manchester.cs.snee.MetadataException;
import uk.ac.manchester.cs.snee.SNEEException;
import uk.ac.manchester.cs.snee.common.SNEEConfigurationException;
import uk.ac.manchester.cs.snee.data.generator.ConstantRatePushStreamGenerator;
import uk.ac.manchester.cs.snee.metadata.schema.SchemaMetadataException;

public class SNEEClientUsingTupleGeneratorSource extends SNEEClient {
	
	private static ConstantRatePushStreamGenerator _myDataSource;

	public SNEEClientUsingTupleGeneratorSource(String query, 
			double duration, String csvFile) 
	throws SNEEException, IOException, SNEEConfigurationException, 
	MetadataException, SchemaMetadataException {
		super(query, duration, csvFile);
		if (logger.isDebugEnabled()) 
			logger.debug("ENTER SNEEClietnUsingTupleGeneratorSource()");		
		displayAllExtents();
		if (logger.isDebugEnabled())
			logger.debug("RETURN SNEEClietnUsingTupleGeneratorSource()");
	}

	/**
	 * The main entry point for the SNEE controller
	 * @param args
	 * @throws IOException
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) {
		// Configure logging
		PropertyConfigurator.configure(
				SNEEClientUsingTupleGeneratorSource.class.
				getClassLoader().getResource("etc/log4j.properties"));
		Long duration;
		String query;
		String csvFile=null;
		//This method represents the web server wrapper
		if (args.length != 2 && args.length != 3) {
			System.out.println("Usage: \n" +
					"\t\"query statement\"\n" +
					"\t\"query duration in seconds\"\n"+
					"\t[\"csv file to log results\"]\n");
			//XXX: Use default query

			query = "SELECT * FROM PushStream;";
			
//			query = "SELECT intattr, intattr * 2 FROM PushStream;";
//			query = "SELECT \'const\', intattr FROM PushStream;";
//			query = "SELECT 42, intattr FROM PushStream;";
//			query = "SELECT \'const\' as StringConstant, intattr FROM PushStream;";

//			query = "SELECT * FROM PushStream WHERE stream_name = \'pushstream\';";
			
			/* The following queries should run */
			//SELECT-PROJECT
//			query = "SELECT intattr " +
//				"FROM PushStream " +
//				"WHERE intattr <= 5;";

			//SELECT-PROJECT-ALIAS
//			query = "SELECT p.intattr " +
//				"FROM PushStream p " +
//				"WHERE p.intattr <= 5;";

			//SELECT-PROJECT-ALIAS-RENAME
//			query = "SELECT p.intattr AS IntegerValue " +
//				"FROM PushStream p " +
//				"WHERE p.intattr <= 5;";

			//JOIN
//			query = "SELECT * " +
//					"FROM PushStream[NOW] p, HerneBay_Tide[NOW] h " +
//					"WHERE p.intattr <= h.Tz;";

//			query = 
//				"SELECT p.intattr, s.integerColumn, s.floatColumn " +
//				"FROM PushStream[NOW] p, " +
//				"	(SELECT intattr as integerColumn, floatattr as floatColumn FROM PushStream[NOW]) s;";

//			query = "SELECT ps.myint " +
//					"FROM ( SELECT ts.intattr AS myint FROM PushStream ts) ps;";
//			query = "SELECT avg(*) FROM ( SELECT ps.intattr AS myint FROM PushStream ps);";
//			query = "SELECT COUNT(intattr) FROM PushStream GROUP BY intattr;";
//			query = "SELECT * FROM ( SELECT * FROM PushStream ps);";
			duration = Long.valueOf("20");
			csvFile = null;
//			System.exit(1);
		} else {	
			query = args[0];
			duration = Long.valueOf(args[1]);
			if (args.length==3) {
				csvFile = args[2];
			}
		}
			
			try {
				/* Initialise SNEEClient */
				SNEEClientUsingTupleGeneratorSource client = 
					new SNEEClientUsingTupleGeneratorSource(query, duration, csvFile);
				/* Initialise and run data source */
				_myDataSource = new ConstantRatePushStreamGenerator();
				_myDataSource.startTransmission();
				/* Run SNEEClient */
				client.run();
				/* Stop the data source */
				_myDataSource.stopTransmission();
			} catch (Exception e) {
				System.out.println("Execution failed. See logs for detail.");
				logger.fatal("Execution failed", e);
				System.exit(1);
			}
//		}
		System.out.println("Success!");
		System.exit(0);
	}
	
}
