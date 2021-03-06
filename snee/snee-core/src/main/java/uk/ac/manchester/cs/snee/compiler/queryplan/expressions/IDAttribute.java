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
package uk.ac.manchester.cs.snee.compiler.queryplan.expressions;

import uk.ac.manchester.cs.snee.metadata.schema.AttributeType;
import uk.ac.manchester.cs.snee.metadata.schema.SchemaMetadataException;

/** 
 * Represent attributes that hold the site id data 
 * was acquired at.
 */
public class IDAttribute extends Attribute {

	/**
	 * Construct a IDAttribute instance
	 * 
	 * @param extentName name of the extent the attribute appears in
	 * @param attrName name of the attribute as it appears in the schema
	 * @param attrType type of the attribute
	 * @throws SchemaMetadataException
	 */
	public IDAttribute(String extentName, String attrName,
			AttributeType attrType) 
	throws SchemaMetadataException {
		super(extentName, attrName, attrType);
	}

	/**
	 * Construct a IDAttribute instance
	 * 
	 * @param extentName name of the extent the attribute appears in
	 * @param attrName name of the attribute as it appears in the schema
	 * @param attrLabel display label for the attribute
	 * @param attrType type of the attribute
	 * @throws SchemaMetadataException
	 */
	public IDAttribute(String extentName, String attrName,
			String attrLabel, AttributeType attrType) 
	throws SchemaMetadataException {
		super(extentName, attrName, attrLabel, attrType);
	}

	public IDAttribute(Attribute attribute) throws SchemaMetadataException {
		super(attribute);
	}

	/**
	 * Finds the minimum value that this expression can return.
	 * @return The minimum value for this expressions
	 * @throws AssertionError If Expression returns a boolean.
	 */
	public double getMinValue() {
    	throw new AssertionError("Illegal call to getMinValue");
		//return 0;
	}
	
	/**
	 * Finds the maximum value that this expression can return.
	 * @return The maximum value for this expressions
	 * @throws AssertionError If Expression returns a boolean.
	 */
	public double getMaxValue() {
    	throw new AssertionError("Illegal call to getMaxValue");
	}
	
	/**
	 * Finds the expected selectivity of this expression can return.
	 * @return The expected selectivity
	 * @throws AssertionError If Expression does not return a boolean.
	 */
	public double getSelectivity() {
    	throw new AssertionError("Illegal call to getSelectivity");
	}

	/**
	 * Checks if the Expression can be directly used in an Aggregation Operator.
	 * Expressions such as attributes that can only be used inside a aggregation expression return false.
	 * 
	 * @return false as this expression is only valid in an aggregation if wrapped in an aggregate.. 
	 */
	public boolean allowedInAggregationOperator() {
		return false;
	}

	/**
	 * Checks if the Expression can be used in a Project Operator.
	 * 
	 * @return true  
	 */
	public boolean allowedInProjectOperator(){
		return true;
	}

	/**
	 * Converts this Expression to an Attribute.
	 * 
	 * @return This Expression.
	 */
	public Attribute toAttribute() {
		return this;
	}

}
