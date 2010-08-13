/****************************************************************************\ 
 *                                                                            *
 *  SNEE (Sensor NEtwork Engine)                                              *
 *  http://code.google.com/p/snee                                             *
 *  Release 1.0, 24 May 2009, under New BSD License.                          *
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

package uk.ac.manchester.cs.snee.compiler;

import java.io.IOException;
import java.io.StringReader;

import org.apache.log4j.Logger;

import uk.ac.manchester.cs.snee.SNEEException;
import uk.ac.manchester.cs.snee.common.Constants;
import uk.ac.manchester.cs.snee.common.SNEEConfigurationException;
import uk.ac.manchester.cs.snee.common.SNEEProperties;
import uk.ac.manchester.cs.snee.common.SNEEPropertyNames;
import uk.ac.manchester.cs.snee.common.Utils;
import uk.ac.manchester.cs.snee.compiler.metadata.Metadata;
import uk.ac.manchester.cs.snee.compiler.metadata.schema.ExtentDoesNotExistException;
import uk.ac.manchester.cs.snee.compiler.metadata.schema.SchemaMetadataException;
import uk.ac.manchester.cs.snee.compiler.metadata.schema.TypeMappingException;
import uk.ac.manchester.cs.snee.compiler.metadata.source.SourceDoesNotExistException;
import uk.ac.manchester.cs.snee.compiler.parser.ParserException;
import uk.ac.manchester.cs.snee.compiler.parser.SNEEqlLexer;
import uk.ac.manchester.cs.snee.compiler.parser.SNEEqlParser;
import uk.ac.manchester.cs.snee.compiler.queryplan.LAF;
import uk.ac.manchester.cs.snee.compiler.translator.ParserValidationException;
import uk.ac.manchester.cs.snee.compiler.translator.Translator;
import uk.ac.manchester.cs.snee.operators.logical.Operator;
import antlr.CommonAST;
import antlr.RecognitionException;
import antlr.TokenStreamException;

/**
 * Main class for SNEEql Query Optimizer.
 * Compiles a declarative SNEEql query into a query execution plan.
 */
public class QueryCompiler {

	/**
	 * Logger for this class.
	 */
	private Logger logger = 
		Logger.getLogger(QueryCompiler.class.getName());

	/**
	 * The logical schema being used. 
	 */
	private Metadata _schemaMetadata;

	public QueryCompiler(Metadata schema) 
	throws TypeMappingException {
		if (logger.isDebugEnabled()) 
			logger.debug("ENTER");
		_schemaMetadata = schema;
		if (logger.isDebugEnabled())
			logger.debug("RETURN");
	}
	
	/**
	 * The single-site optimizer phase.
	 * @param queryID Number assigned to this query
	 * @param outputDir The directory generated for output files
	 * @param query The string representation of the query
	 * @return LAF representation of this query.
	 * @throws ParserException 
	 * @throws OptimizationException 
	 * @throws  
	 * @throws ParserValidationException 
	 * @throws SchemaMetadataException 
	 * @throws TypeMappingException 
	 * @throws SourceDoesNotExistException 
	 * @throws ExtentDoesNotExistException 
	 * @throws TokenStreamException 
	 * @throws RecognitionException 
	 * @throws SNEEConfigurationException 
	 */
	//	private PAF doSingleSitePhase(int queryID, String fullQueryPath, 
	//			String queryName) 
	private LAF doSingleSitePhase(int queryID, String outputDir, 
			String query) 
	throws SourceDoesNotExistException, TypeMappingException, 
	SchemaMetadataException, ParserValidationException, 
	OptimizationException, ParserException, ExtentDoesNotExistException,
	RecognitionException, TokenStreamException, SNEEConfigurationException 
	{
		if (logger.isTraceEnabled())
			logger.trace("ENTER: queryID: " + queryID + 
					" dir: " + outputDir + "\n\tquery: " + query);
		
		if (logger.isInfoEnabled()) 
			logger.info("Starting parsing for queryID " + queryID);
		CommonAST ast = doParsing(queryID, outputDir, query);

		if (logger.isInfoEnabled()) 
			logger.info("Starting Translation for query " + 
					queryID);
		LAF laf = doTranslation(queryID, outputDir, ast);
		
		if (logger.isInfoEnabled()) 
			logger.info("Starting Rewriting for query " + 
					queryID);
		LAF lafPrime = doLogicalRewriting(laf);
		
		//TODO: Invoke Source Allocator
		
		//TODO: Invoke Source Planner
		
			// Physical Optimisation
			//		logger.info("Starting Physical Optimization");
			//		PAF paf = PhysicalOptimization.doPhysicalOptimization(
			//				laf, 
			//				queryName);
			//		return paf;

		//TODO: Return a query evaluation plan
		if (logger.isTraceEnabled())
			logger.trace("RETURN: " + lafPrime);
		return lafPrime;
	}

	private CommonAST doParsing(int queryID, String outputDir, 
			String query) 
	throws ParserException, RecognitionException, TokenStreamException {
		if (logger.isTraceEnabled())
			logger.trace("ENTER parseQuery() with queryID: " + queryID +
					" dir: " + outputDir + " query:\n\t" + query);
		SNEEqlLexer lexer = new SNEEqlLexer(new StringReader(query)); 
		SNEEqlParser parser = new SNEEqlParser(lexer);
		parser.parse();
		CommonAST parseTree = (CommonAST)parser.getAST();
		if (logger.isTraceEnabled())
			logger.trace("Parse tree: " + parseTree.toStringList());
		return parseTree;
	}
	
	private LAF doTranslation(int queryID, 
			String queryPlanOutputDir, CommonAST parseTree) 
	throws TypeMappingException, SourceDoesNotExistException, 
	SchemaMetadataException, ParserValidationException, 
	OptimizationException, ParserException, ExtentDoesNotExistException,
	RecognitionException, SNEEConfigurationException 
	{
		if (logger.isTraceEnabled())
			logger.trace("ENTER queryID: " + queryID + "\n\tquery: " + 
					parseTree);
		Translator translator = new Translator(_schemaMetadata);
		LAF laf = translator.translate(parseTree, queryID);    

//				qosCollection.get(queryID).getMaxAcquisitionInterval());
		laf.exportGraph(queryPlanOutputDir, laf.getName(), "");
		if (logger.isTraceEnabled())
			logger.trace("RETURN: "+laf);
		return laf;

	}

	private LAF doLogicalRewriting(LAF laf) {
		if (logger.isTraceEnabled())
			logger.trace("ENTER laf: " + laf);
		//TODO: Placeholder for rewriting step, currently merged with translation step
		LAF lafPrime = laf;
		if (logger.isTraceEnabled())
			logger.trace("RETURN: "+lafPrime);
		return lafPrime;		
	}
	
//	/**
//	 * Invoke the multi-site optimizer phase.
//	 * @param queryID the ID of the query.
//	 * @param queryName the name of the query.
//	 * @param paf the physical operator tree from the single-site phase.
//	 * @return a complete query plan (comprising DAF, RT and agenda)
//	 * @throws OptimizationException An optimization-related error.
//	 * @throws AgendaException An agenda-related error.
//	 */
//	private  ScoredCandidateList<Agenda> doMultiSitePhase(int queryID, 
//			String queryName, PAF paf) 
//			throws OptimizationException, AgendaException {
//
//		// Routing	        
//		logger.info("Starting Routing");
//		ScoredCandidateList<RT> rtList = Routing.doRouting(
//				paf, 
//				sensornetMetadata,
//				schemaMetadata,
//				Settings.INPUTS_METADATA_SINKS.get(queryID),
//				queryName,
//				qosCollection.get(queryID));
//
//		// Partitioning
//		logger.info("Starting Partitioning");
//		ScoredCandidateList<FAF> fafList = Partitioning.doPartitioning(
//				paf, 
//				queryName);
//
//		// Where Scheduling
//		logger.info("Starting Where Scheduling");
//		ScoredCandidateList<DAF> dafList = WhereScheduling.
//		doWhereScheduling(fafList, 
//				rtList, 
//				schemaMetadata,
//				Settings.INPUTS_METADATA_SINKS.get(queryID),
//				queryName);
//
//		// When Scheduling
//		logger.info("Starting When Scheduling");
//		ScoredCandidateList<Agenda> agendaList = WhenScheduling.
//		doWhenScheduling(dafList, 
//				qosCollection.get(queryID), 
//				queryName);        
//
//		return agendaList;
//	}


//	private static void produceCandidateSummary(ScoredCandidateList<Agenda> agendaList) throws IOException {
//		String fname = QueryCompiler.queryPlanOutputDir + "candidates-summary.csv";
//
//		ArrayList<Double> alphaScores = new ArrayList<Double>();
//		ArrayList<Double> deltaScores = new ArrayList<Double>();
//		ArrayList<Double> epsilonScores = new ArrayList<Double>();
//		ArrayList<Double> lambdaScores = new ArrayList<Double>();
//		double maxAlpha = 0, maxDelta = 0, maxEpsilon = 0, maxLambda = 0;
//
//		Iterator<Agenda> agendaIter = agendaList.iterator();
//		while (agendaIter.hasNext()) {
//			Agenda agenda = agendaIter.next();
//			RT rtCand = agenda.getDAF().getRoutingTree();
//
//			//rt-alpha-score
//			RTAcquisitionIntervalScoringFunction rtAlphaScorer = 
//				new RTAcquisitionIntervalScoringFunction(1, false); 
//			double score = rtAlphaScorer.score(rtCand);
//			alphaScores.add(score);
//			maxAlpha = Math.max(maxAlpha, score);
//
//			//rt-delta-score
//			RTDeliveryTimeScoringFunction rtDeltaScorer = 
//				new RTDeliveryTimeScoringFunction(1, false); 
//			score = rtDeltaScorer.score(rtCand);
//			deltaScores.add(score);
//			maxDelta = Math.max(maxDelta, score);
//
//			//rt-epsilon-score
//			RTEnergyScoringFunction rtEpsilonScorer = 
//				new RTEnergyScoringFunction(1, false); 
//			score = rtEpsilonScorer.score(rtCand);			
//			epsilonScores.add(score);
//			maxEpsilon = Math.max(maxEpsilon, score);
//
//			//rt-lambda-score
//			RTLifetimeScoringFunction rtLambdaScorer = 
//				new RTLifetimeScoringFunction(1, false); 
//			score = rtLambdaScorer.score(rtCand);
//			lambdaScores.add(score);
//			maxLambda = Math.max(maxLambda, score);
//		}
//
//		PrintWriter out = new PrintWriter(new BufferedWriter(
//				new FileWriter(fname)));
//		out.println("agenda-id,rt-id,rt-alpha-score,rt-delta-score,rt-epsilon-score,rt-lambda-score,rt-final-score,beta,alpha-bms,delta-bms,pi-bms,alpha-ms,delta-ms,pi-ms");
//
//		agendaIter = agendaList.iterator();
//		int i = 0;
//		while (agendaIter.hasNext()) {
//			Agenda agenda = agendaIter.next();
//
//			//agenda-id
//			out.print(agenda.getName() + ",");
//			RT rtCand = agenda.getDAF().getRoutingTree();
//			//rt-id
//			out.print(rtCand.getName() + ",");
//
//			//rt-alpha-score,rt-delta-score,rt-epsilon-score,rt-lambda-score 
//			out.print(alphaScores.get(i)/maxAlpha + ",");
//			out.print(deltaScores.get(i)/maxDelta + ",");			
//			out.print(epsilonScores.get(i)/maxEpsilon + ",");			
//			out.print(lambdaScores.get(i)/maxLambda + ",");			
//
//			//rt-final-score
//			out.print(agenda.getDAF().getRoutingTree().getScore() + ",");
//
//			//beta
//			out.print(agenda.getBufferingFactor() + ",");
//
//			//alpha, delta, pi in bms
//			out.print(agenda.getAcquisitionInterval_bms() + ",");
//			out.print(agenda.getDeliveryTime_bms() + ",");
//			out.print(agenda.getProcessingTime_bms() + ",");
//
//			//alpha, delta, pi in ms
//			out.print(agenda.getAcquisitionInterval_ms() + ",");
//			out.print(agenda.getDeliveryTime_ms() + ",");
//			out.println(agenda.getProcessingTime_ms());
//
//			i++;
//		}
//		out.close();
//	}

//	/**
//	 * Invokes the code generation phase.
//	 * @param queryID the id of the query
//	 * @param plan the query plan
//	 * @throws OptimizationException An optimization-related exception
//	 */
//	private static void doCodeGenerationPhase(int queryID, 
//			QueryPlan plan, String nescOutputDir) throws OptimizationException {
//
//		if (Settings.CODE_GENERATION_TARGETS.contains(CodeGenTarget.TOSSIM)) {
//			logger.info("Starting TOS1 Code Generation for Tossim");
//			NesCGeneration.doNesCGeneration(plan, 
//					qosCollection.get(queryID), 
//					Settings.INPUTS_METADATA_SINKS.get(queryID),
//					nescOutputDir, 1, true);
//		}
//		if (Settings.CODE_GENERATION_TARGETS.contains(CodeGenTarget.AVRORA)) {
//			logger.info("Starting TOS1 Code Generation for Avrora");
//
//			NesCGeneration.doNesCGeneration(plan, 
//					qosCollection.get(queryID), 
//					Settings.INPUTS_METADATA_SINKS.get(queryID),
//					nescOutputDir, 1, false);			
//		}
//		if (Settings.CODE_GENERATION_TARGETS.contains(CodeGenTarget.TOSSIM2)) {
//			logger.info("Starting TOS2 Code Generation for Tossim");
//			NesCGeneration.doNesCGeneration(plan, 
//					qosCollection.get(queryID), 
//					Settings.INPUTS_METADATA_SINKS.get(queryID),
//					nescOutputDir, 2, true);			
//		}
//		if (Settings.CODE_GENERATION_TARGETS.contains(CodeGenTarget.AVRORA2)) {
//			logger.info("Starting TOS2 Code Generation for Avrora");
//			NesCGeneration.doNesCGeneration(plan, 
//					qosCollection.get(queryID), 
//					Settings.INPUTS_METADATA_SINKS.get(queryID),
//					nescOutputDir, 2, false);
//		}
//	}

	/**
	 * Compile a query into the logical algebraic form for processing
	 * by the out of network query evaluation engine.
	 * 
	 * @param queryID The identifier for the query
	 * @param query The query text
	 * @return logical algebraic form of the query
	 * @throws SNEEException 
	 * @throws ParserException 
	 * @throws OptimizationException 
	 * @throws  
	 * @throws ParserValidationException 
	 * @throws SchemaMetadataException 
	 * @throws TypeMappingException 
	 * @throws SourceDoesNotExistException 
	 * @throws ExtentDoesNotExistException 
	 * @throws TokenStreamException 
	 * @throws RecognitionException 
	 * @throws SNEEConfigurationException 
	 */
//TODO: Change to return a query plan that is interpretable by a dispatcher
	public LAF compileQuery(int queryID, String query) 
	throws SNEEException, SourceDoesNotExistException, 
	TypeMappingException, SchemaMetadataException, 
	ParserValidationException, OptimizationException, 
	ParserException, ExtentDoesNotExistException,
	RecognitionException, TokenStreamException, 
	SNEEConfigurationException 
	 {
		if (logger.isDebugEnabled())
			logger.debug("ENTER: queryID: " + queryID + "\n\tquery: " + query);
		logger.info("Compiling queryID " + queryID);
		String outputDir = createQueryDirectory(queryID);
		//Invoke the Single-site Phase of query optimization
		LAF laf = doSingleSitePhase(queryID, outputDir, query);
		if (logger.isDebugEnabled())
			logger.debug("RETURN: " + laf.getName());
		return laf;
	}

	private String createQueryDirectory(int queryID) 
	throws SNEEException, SNEEConfigurationException {
		if (logger.isTraceEnabled())
			logger.trace("ENTER: queryID: " + queryID);
		String queryPlanOutputDir;
		try {
			String fileSeparator = System.getProperty("file.separator");
			String outputRootDir = 
				SNEEProperties.getSetting(SNEEPropertyNames.GENERAL_OUTPUT_ROOT_DIR) +
				fileSeparator + "query" + queryID + fileSeparator; 
			queryPlanOutputDir = outputRootDir + Constants.QUERY_PLAN_DIR;
			Utils.checkDirectory(queryPlanOutputDir, true);
		} catch (IOException e) {
			String msg = "Unable to create directory for query " + queryID;
			logger.warn(msg);
			throw new SNEEException(msg);
		}
		if (logger.isTraceEnabled())
			logger.trace("RETURN: " + queryPlanOutputDir);
		return queryPlanOutputDir;
	}

//	/**
//	 * 
//	 * Processes a query all the way through to NesC code.
//	 * 
//	 * @param queryFileName Name of the file that hold the query * 
//	 * @param queryID Number assigned to this query 
//	 *        for multiple query processing.
//	 * @return a query plan for the query
//	 * @throws IOException One of the required files not found.
//	 * @throws AssertionError 
//	 * @throws ParserException 
//	 * @throws ParserValidationException 
//	 * @throws SchemaMetadataException 
//	 * @throws SourceDoesNotExistException 
//	 * @throws TokenStreamException 
//	 * @throws RecognitionException 
//	 */
//	public static QueryPlan processQuery(String queryFileName,
//			int queryID) throws IOException, RecognitionException, TokenStreamException, SourceDoesNotExistException, SchemaMetadataException, ParserValidationException, ParserException, AssertionError {
//
//		QueryPlan plan = null;
//
//		try {
//			String fullQueryPath = 
//				Settings.INPUTS_QUERY_DIR + queryFileName;
//			logger.info("Processing query " + fullQueryPath);
//			String queryName = queryFileName.replaceAll("\\.txt", "");
//			//remove any directories from query name 
//			if (queryName.lastIndexOf('/') > 0) {
//				queryName = queryName.substring(queryName.lastIndexOf('/'));
//			}
//
//			String outputRootDir = Settings.GENERAL_OUTPUT_ROOT_DIR + queryName + "/"; 
//			queryPlanOutputDir =  outputRootDir + Constants.QUERY_PLAN_DIR;
//			Utils.checkDirectory(queryPlanOutputDir, true);
//
//			//Display the sensor network topology
//			if (Settings.DISPLAY_SENSOR_NETWORK_TOPOLOGY) {
//				sensornetMetadata.display(
//						QueryCompiler.queryPlanOutputDir, 
//						sensornetMetadata.getName());
//			}	
//
//			//Display Query and QoS requirements for this query
//			qosCollection.get(queryID).exportToPDF(
//					queryPlanOutputDir, "user-input.tex", fullQueryPath);
//
//			//Invoke the Single-site Phase of query optimization
//			PAF paf = doSingleSitePhase(
//					queryID, fullQueryPath, queryName);
//
//			//Invoke the Multi-site phase of query optimization
//			ScoredCandidateList<Agenda> agendaList 
//			= doMultiSitePhase(queryID, queryName, paf);	       
//
//			produceCandidateSummary(agendaList);
//
//			generateQoSMetrics(agendaList);
//
//			//Invoke Code generation phase for best plan
//			Agenda bestAgenda = agendaList.getBest();
//			plan = new QueryPlan(bestAgenda.getDAF().getName(), 
//					bestAgenda.getDAF(), bestAgenda);
//			doCodeGenerationPhase(queryID, plan, outputRootDir);
//			//Produce a text file characterising the query plan generated
//			plan.produceQueryPlanSummary();
//			plan.generateTrafficPatternsXML(qosCollection.get(queryID));
//
//			Iterator<Agenda> agendaIter = agendaList.iterator();
//			while (agendaIter.hasNext()) {
//				Agenda agenda = agendaIter.next();
//				if (agenda != bestAgenda) 
//				{
//					QueryPlan qp = new QueryPlan(agenda.getDAF().getName(), 
//							agenda.getDAF(), agenda);
//					String nescOutputDir = outputRootDir 
//					+ "/alt/" + agenda.getName() + "/";
//					doCodeGenerationPhase(queryID, qp, nescOutputDir);
//				}
//			}
//
//		} catch (OptimizationException e) {
//			Utils.handleQueryException(e);
//		} catch (AgendaException e) {
//			Utils.handleCriticalException(e);
//		} catch (FileNotFoundException e) {
//			Utils.handleCriticalException(e);
//		} 
//
//		return plan;    
//	}


//	private static void generateQoSMetrics(ScoredCandidateList<Agenda> agendaList) throws IOException {
//		String fname = queryPlanOutputDir+"model-qos-metrics.csv";
//		PrintWriter out = new PrintWriter(new BufferedWriter(
//				new FileWriter(fname)));
//		out.println("agenda-id,rt-id,beta,alpha-ms,delta-ms,total-energy,network-lifetime,pi-ms");
//
//		Iterator<Agenda> agendaIter = agendaList.iterator();
//		while (agendaIter.hasNext()) {
//			Agenda agenda = agendaIter.next();
//			String agendaId = agenda.getName();
//			String rtId = agenda.getDAF().getRoutingTree().getName();
//			String beta = new Long(agenda.getBufferingFactor()).toString();
//
//			double alpha = agenda.getAcquisitionInterval_ms();
//			double delta = agenda.getDeliveryTime_ms();
//			double epsilon = agenda.getTotalEnergy();
//			double lambda = agenda.getLifetime();
//			double lambdaDays = Utils.convertSecondsToDays(lambda);
//			double pi = agenda.getProcessingTime_ms();
//
//			out.println(agendaId+","+rtId+","+beta+","+alpha+","+delta+","+epsilon+","+lambdaDays+","+pi);
//		}
//		out.close();
//	}

}