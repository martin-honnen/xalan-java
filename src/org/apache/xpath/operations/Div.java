/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the  "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * $Id$
 */
package org.apache.xpath.operations;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.apache.xalan.xslt.util.XslTransformEvaluationHelper;
import org.apache.xpath.XPathContext;
import org.apache.xpath.XPathException;
import org.apache.xpath.objects.ResultSequence;
import org.apache.xpath.objects.XNodeSet;
import org.apache.xpath.objects.XNumber;
import org.apache.xpath.objects.XObject;

import xml.xpath31.processor.types.XSDecimal;
import xml.xpath31.processor.types.XSDouble;
import xml.xpath31.processor.types.XSNumericType;
import xml.xpath31.processor.types.XSYearMonthDuration;

/**
 * The 'div' operation expression executer.
 */
public class Div extends Operation
{
    static final long serialVersionUID = 6220756595959798135L;
    
    private static final java.lang.String NON_TERMINATING_DECIMAL_EXPANSION = "Non-terminating decimal expansion";
    
    private static final java.lang.String DIVISION_BY_ZERO = "Division by zero";
    
    private static final int DEFAULT_DIV_SCALE = 18;

  /**
   * Apply the operation to two operands, and return the result.
   *
   * @param left non-null reference to the evaluated left operand.
   * @param right non-null reference to the evaluated right operand.
   *
   * @return non-null reference to the XObject that represents the result of the operation.
   *
   * @throws javax.xml.transform.TransformerException
   */
  public XObject operate(XObject left, XObject right)
                                           throws javax.xml.transform.TransformerException
  {  
     XObject result = null;
          
     if ((left instanceof XNumber) && (right instanceof XSNumericType)) {
        double lDouble = ((XNumber)left).num();
        
        java.lang.String rStrVal = ((XSNumericType)right).stringValue();
        double rDouble = (Double.valueOf(rStrVal)).doubleValue();
        
        result = new XNumber(lDouble / rDouble);
     }
     else if ((left instanceof XSNumericType) && (right instanceof XNumber)) {
         java.lang.String lStrVal = ((XSNumericType)left).stringValue();
         double lDouble = (Double.valueOf(lStrVal)).doubleValue();
         
         double rDouble = ((XNumber)right).num();
         
         result = new XNumber(lDouble / rDouble);
     }     
     else if ((left instanceof XSNumericType) && (right instanceof XSNumericType)) {
    	 if ((left instanceof XSDecimal) || (right instanceof XSDecimal)) {    		
    		BigDecimal lBigDecimal = new BigDecimal(((XSNumericType)left).stringValue()); 
    		BigDecimal rBigDecimal = new BigDecimal(((XSNumericType)right).stringValue());
     		try {
     		   result = new XSDecimal(lBigDecimal.divide(rBigDecimal));
     		}
     		catch (ArithmeticException ex) {
     		   java.lang.String exceptionMesg = ex.getMessage();
     		   if (exceptionMesg.startsWith(NON_TERMINATING_DECIMAL_EXPANSION)) {
     			  BigDecimal bigDecimal = lBigDecimal.divide(rBigDecimal, DEFAULT_DIV_SCALE, RoundingMode.HALF_EVEN);
     			  result = new XSDecimal(bigDecimal);
     		   }
     		   else if (exceptionMesg.startsWith(DIVISION_BY_ZERO)) {
     			  throw new javax.xml.transform.TransformerException("FOAR0001 : An integer division by zero error."); 
     		   }
     		} 
    	 }
    	 else {
    		 java.lang.String lStrVal = ((XSNumericType)left).stringValue();
             double lDouble = (Double.valueOf(lStrVal)).doubleValue();
             
             java.lang.String rStrVal = ((XSNumericType)right).stringValue();
             double rDouble = (Double.valueOf(rStrVal)).doubleValue();
             
             result = new XSDouble(lDouble / rDouble);
    	 }
    	 
    	 return result;
     }
     else if ((left instanceof XNumber) && (right instanceof XNumber)) {
         double lDouble = ((XNumber)left).num();
         double rDouble = ((XNumber)right).num();
         
         result = new XSDouble(lDouble / rDouble);
     }
     else if ((left instanceof XNumber) && (right instanceof XNodeSet)) {
         double lDouble = ((XNumber)left).num();
         
         XNodeSet rNodeSet = (XNodeSet)right;
         if (rNodeSet.getLength() > 1) {
            throw new javax.xml.transform.TransformerException("XPTY0004 : a sequence of more "
                                                                                  + "than one item is not allowed as the 2nd "
                                                                                  + "operand of operator 'div'.");  
         }
         else {
            java.lang.String rStrVal = rNodeSet.str();
            double rDouble = (Double.valueOf(rStrVal)).doubleValue();
            
            result = new XSDouble(lDouble / rDouble);
         }
     }
     else if ((left instanceof XNodeSet) && (right instanceof XNumber)) {
         double rDouble = ((XNumber)right).num();
         
         XNodeSet lNodeSet = (XNodeSet)left;
         if (lNodeSet.getLength() > 1) {
            throw new javax.xml.transform.TransformerException("XPTY0004 : a sequence of more "
                                                                                  + "than one item is not allowed as the 1st "
                                                                                  + "operand of operator 'div'.");  
         }
         else {
            java.lang.String lStrVal = lNodeSet.str();
            double lDouble = (Double.valueOf(lStrVal)).doubleValue();
            
            result = new XSDouble(lDouble / rDouble);
         }
     }
     else if ((left instanceof XSNumericType) && (right instanceof XNodeSet)) {
         java.lang.String lStrVal = ((XSNumericType)left).stringValue();
         double lDouble = (Double.valueOf(lStrVal)).doubleValue();
         
         XNodeSet rNodeSet = (XNodeSet)right;
         if (rNodeSet.getLength() > 1) {
            throw new javax.xml.transform.TransformerException("XPTY0004 : a sequence of more "
                                                                                  + "than one item is not allowed as the 2nd "
                                                                                  + "operand of operator 'div'.");  
         }
         else {
            java.lang.String rStrVal = rNodeSet.str();
            double rDouble = (Double.valueOf(rStrVal)).doubleValue();
            
            result = new XSDouble(lDouble / rDouble);
         }
     }
     else if ((left instanceof XNodeSet) && (right instanceof XSNumericType)) {
         java.lang.String rStrVal = ((XSNumericType)right).stringValue();
         double rDouble = (Double.valueOf(rStrVal)).doubleValue();
         
         XNodeSet lNodeSet = (XNodeSet)left;
         if (lNodeSet.getLength() > 1) {
            throw new javax.xml.transform.TransformerException("XPTY0004 : a sequence of more "
                                                                                  + "than one item is not allowed as the 1st "
                                                                                  + "operand of operator 'div'.");  
         }
         else {
            java.lang.String lStrVal = lNodeSet.str();
            double lDouble = (Double.valueOf(lStrVal)).doubleValue();
            
            result = new XSDouble(lDouble / rDouble);
         }
     }
     else if ((left instanceof XNodeSet) && (right instanceof XNodeSet)) {
         double lDouble = 0.0d;
         double rDouble = 0.0d;
         
         XNodeSet lNodeSet = (XNodeSet)left;
         if (lNodeSet.getLength() > 1) {
            throw new javax.xml.transform.TransformerException("XPTY0004 : a sequence of more "
                                                                                  + "than one item is not allowed as the 1st "
                                                                                  + "operand of operator 'div'.");  
         }
         else {
            java.lang.String lStrVal = lNodeSet.str();
            lDouble = (Double.valueOf(lStrVal)).doubleValue();
         }
         
         XNodeSet rNodeSet = (XNodeSet)right;
         if (rNodeSet.getLength() > 1) {
            throw new javax.xml.transform.TransformerException("XPTY0004 : a sequence of more "
                                                                                  + "than one item is not allowed as the 2nd "
                                                                                  + "operand of operator 'div'.");  
         }
         else {
            java.lang.String rStrVal = rNodeSet.str();
            rDouble = (Double.valueOf(rStrVal)).doubleValue();
         }
         
         result = new XSDouble(lDouble / rDouble);
     }     
     else if ((left instanceof ResultSequence) && (right instanceof XNumber)) {
         ResultSequence rsLeft = (ResultSequence)left;          
         if (rsLeft.size() > 1) {
             throw new javax.xml.transform.TransformerException("XPTY0004 : a sequence of more "
                                                                                  + "than one item is not allowed as the 1st "
                                                                                  + "operand of operator 'div'.");  
         }
         else {
            java.lang.String lStr = XslTransformEvaluationHelper.getStrVal(rsLeft.item(0));
            double lDouble = (Double.valueOf(lStr)).doubleValue();
            
            double rDouble = ((XNumber)right).num();
            
            result = new XSDouble(lDouble / rDouble);
         }
     }
     else if ((left instanceof XNumber) && (right instanceof ResultSequence)) {
         ResultSequence rsRight = (ResultSequence)right;          
         if (rsRight.size() > 1) {
             throw new javax.xml.transform.TransformerException("XPTY0004 : a sequence of more "
                                                                                  + "than one item is not allowed as the 2nd "
                                                                                  + "operand of operator 'div'.");  
         }
         else {             
            double lDouble = ((XNumber)left).num();
            
            java.lang.String rStr = XslTransformEvaluationHelper.getStrVal(rsRight.item(0));
            double rDouble = (Double.valueOf(rStr)).doubleValue();
            
            result = new XSDouble(lDouble / rDouble);
         }
     }
     else if ((left instanceof ResultSequence) && (right instanceof XSNumericType)) {
         ResultSequence rsLeft = (ResultSequence)left;          
         if (rsLeft.size() > 1) {
             throw new javax.xml.transform.TransformerException("XPTY0004 : a sequence of more "
                                                                                  + "than one item is not allowed as the 1st "
                                                                                  + "operand of operator 'div'.");  
         }
         else {
            java.lang.String lStr = XslTransformEvaluationHelper.getStrVal(rsLeft.item(0));
            double lDouble = (Double.valueOf(lStr)).doubleValue();
            
            java.lang.String rStrVal = ((XSNumericType)right).stringValue();
            double rDouble = (Double.valueOf(rStrVal)).doubleValue();
            
            result = new XSDouble(lDouble / rDouble);
         } 
     }
     else if ((left instanceof XSNumericType) && (right instanceof ResultSequence)) {
         ResultSequence rsRight = (ResultSequence)right;          
         if (rsRight.size() > 1) {
             throw new javax.xml.transform.TransformerException("XPTY0004 : a sequence of more "
                                                                                  + "than one item is not allowed as the 2nd "
                                                                                  + "operand of operator 'div'.");  
         }
         else {                          
            java.lang.String lStrVal = ((XSNumericType)left).stringValue();
            double lDouble = (Double.valueOf(lStrVal)).doubleValue();
            
            java.lang.String rStr = XslTransformEvaluationHelper.getStrVal(rsRight.item(0));
            double rDouble = (Double.valueOf(rStr)).doubleValue();
            
            result = new XSDouble(lDouble / rDouble);
         }
     }
     else if ((left instanceof ResultSequence) && (right instanceof ResultSequence)) {
         ResultSequence rsLeft = (ResultSequence)left;          
         if (rsLeft.size() > 1) {
             throw new javax.xml.transform.TransformerException("XPTY0004 : a sequence of more "
                                                                                  + "than one item is not allowed as the 1st "
                                                                                  + "operand of operator 'div'.");  
         }
         
         ResultSequence rsRight = (ResultSequence)right;          
         if (rsRight.size() > 1) {
             throw new javax.xml.transform.TransformerException("XPTY0004 : a sequence of more "
                                                                                  + "than one item is not allowed as the 2nd "
                                                                                  + "operand of operator 'div'.");  
         }
         
         java.lang.String lStr = XslTransformEvaluationHelper.getStrVal(rsLeft.item(0));
         double lDouble = (Double.valueOf(lStr)).doubleValue();
         
         java.lang.String rStr = XslTransformEvaluationHelper.getStrVal(rsRight.item(0));
         double rDouble = (Double.valueOf(rStr)).doubleValue();
         
         result = new XSDouble(lDouble / rDouble);
     }
     else if (left instanceof ResultSequence) {
    	 ResultSequence rSeq = (ResultSequence)left;
         if (rSeq.size() > 1) {
            throw new javax.xml.transform.TransformerException("XPTY0004 : a sequence of more "
                                                                                  + "than one item is not allowed as the 1st "
                                                                                  + "operand of operator 'div'.");  
         }                  
    	 
         BigDecimal lBigDecimal = null;
         BigDecimal rBigDecimal = null;
         
    	 try {
    		 java.lang.String lStrVal = XslTransformEvaluationHelper.getStrVal(rSeq.item(0));
             java.lang.String rStrVal = XslTransformEvaluationHelper.getStrVal(right);
    		 lBigDecimal = new BigDecimal(lStrVal); 
        	 rBigDecimal = new BigDecimal(rStrVal);
    		 result = new XSDecimal(lBigDecimal.divide(rBigDecimal));
    	 }
    	 catch (NumberFormatException ex) {
    		 throw new javax.xml.transform.TransformerException("XPTY0004 : One or both of the operands of operator 'div' "
    		 		                                                                             + "are not numeric.");
    	 }
    	 catch (ArithmeticException ex) {
    		 java.lang.String exceptionMesg = ex.getMessage();
    		 if (exceptionMesg.startsWith(NON_TERMINATING_DECIMAL_EXPANSION)) {
    			 BigDecimal resultBigDecimal = lBigDecimal.divide(rBigDecimal, DEFAULT_DIV_SCALE, RoundingMode.HALF_EVEN);
    			 result = new XSDecimal(resultBigDecimal);
    		 }
    		 else if (exceptionMesg.startsWith(DIVISION_BY_ZERO)) {
    			 throw new javax.xml.transform.TransformerException("FOAR0001 : An integer division by zero error."); 
    		 }
    	 }
     }
     else if (left instanceof XNodeSet) {
    	 XNodeSet lNodeSet = (XNodeSet)left;
         if (lNodeSet.getLength() > 1) {
            throw new javax.xml.transform.TransformerException("XPTY0004 : a sequence of more "
                                                                                  + "than one item is not allowed as the 1st "
                                                                                  + "operand of operator 'div'.");  
         }                  
    	 
         BigDecimal lBigDecimal = null;
         BigDecimal rBigDecimal = null;
         
    	 try {
    		 java.lang.String lStrVal = lNodeSet.str();
             java.lang.String rStrVal = XslTransformEvaluationHelper.getStrVal(right);
    		 lBigDecimal = new BigDecimal(lStrVal); 
        	 rBigDecimal = new BigDecimal(rStrVal);
    		 result = new XSDecimal(lBigDecimal.divide(rBigDecimal));
    	 }
    	 catch (NumberFormatException ex) {
    		 throw new javax.xml.transform.TransformerException("XPTY0004 : One or both of the operands of operator 'div' "
    		 		                                                                             + "are not numeric.");
    	 }
    	 catch (ArithmeticException ex) {
    		 java.lang.String exceptionMesg = ex.getMessage();
    		 if (exceptionMesg.startsWith(NON_TERMINATING_DECIMAL_EXPANSION)) {
    			 BigDecimal resultBigDecimal = lBigDecimal.divide(rBigDecimal, DEFAULT_DIV_SCALE, RoundingMode.HALF_EVEN);
    			 result = new XSDecimal(resultBigDecimal);
    		 }
    		 else if (exceptionMesg.startsWith(DIVISION_BY_ZERO)) {
    			 throw new javax.xml.transform.TransformerException("FOAR0001 : An integer division by zero error."); 
    		 }
    	 }         
     }
     else if (left instanceof XSYearMonthDuration) {
         try {
            java.lang.String rStrVal = XslTransformEvaluationHelper.getStrVal(right);
            result = ((XSYearMonthDuration)left).div(new XSDouble(rStrVal));
         }
         catch (XPathException ex) {
            throw new javax.xml.transform.TransformerException(ex.getMessage());  
         }
     }     
     else {
    	 try {
            java.lang.String lStrVal = XslTransformEvaluationHelper.getStrVal(left);
        	java.lang.String rStrVal = XslTransformEvaluationHelper.getStrVal(right);
            result = new XSDouble(Double.valueOf(lStrVal) / Double.valueOf(rStrVal));
         }
         catch (NumberFormatException ex) {
            throw new javax.xml.transform.TransformerException("XPTY0004 : Could not evaluate the operator 'div', "
            		                                                               + "due to incorrectly typed operand(s)."); 
         }
     }
      
     return result; 
  }
  
  /**
   * Evaluate this operation directly to a double.
   *
   * @param xctxt The runtime execution context.
   *
   * @return The result of the operation as a double.
   *
   * @throws javax.xml.transform.TransformerException
   */
  public double num(XPathContext xctxt)
          throws javax.xml.transform.TransformerException
  {

    return (m_left.num(xctxt) / m_right.num(xctxt));
  }

}
