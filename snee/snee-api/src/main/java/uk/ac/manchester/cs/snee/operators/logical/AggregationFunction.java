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
package uk.ac.manchester.cs.snee.operators.logical;

/**
 * Represents the type of data that the operator will output. 
 * 
 * 
 * @author Christian
 *
 */
public enum AggregationFunction {
	/** Count of all values. */
	COUNT ("count"),	
	/** Sum of all values. */
	SUM ("sum"),
	/** Minimum of all values. */
	MIN ("min"),
	/** Maximum of all values. */
	MAX ("max"),
	/** Average of all values. */
	AVG ("avg"),
	/** Standard deviation */
	STDEV ("stdev");

	/** Hold the string representation of the selected value. */
	private String strRep;
	
	/** Constructor.
	 * 
	 * @param s One of the Enum Values
	 */
	private AggregationFunction(String s) {
		this.strRep = s;
	}	
	
	/**
	 * The Enum as a String.
	 * 
	 * @return String representation of this enum.
	 */ 
	public String toString() {
		return this.strRep;
	}
	
	/**
	 * Returns true if this is a primitive aggregation function, i.e., it cannot 
	 * be computed in terms of other aggregation functions.  Examples of this
	 * type include SUM. COUNT, MIN and MAX.
	 * Returns false if this is a derived aggregation function, i.e., it may 
	 * be computed in terms of other aggregation functions.  Examples of this
	 * type include AVG and STDEV.
	 * @return
	 */
	public boolean isPrimitiveType(AggregationFunction aggrFn) {
		if ((aggrFn==SUM) || (aggrFn==COUNT) || (aggrFn==MIN) || (aggrFn==MAX)) {
		return true;
		}
		return false;
	}
	
}
