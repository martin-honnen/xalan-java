/*
 * @(#)$Id$
 *
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Xalan" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation and was
 * originally based on software copyright (c) 2001, Sun
 * Microsystems., http://www.sun.com.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * @author Jacek Ambroziak
 * @author Santiago Pericas-Geertsen
 *
 */

package org.apache.xalan.xsltc.compiler;

import org.w3c.dom.*;

import org.apache.xalan.xsltc.compiler.util.Type;
import org.apache.xalan.xsltc.compiler.util.ReferenceType;
import de.fub.bytecode.classfile.JavaClass;
import de.fub.bytecode.generic.*;
import de.fub.bytecode.classfile.Field;

import org.apache.xalan.xsltc.compiler.util.*;

final class Number extends Instruction {
    private static final int LEVEL_SINGLE   = 0;
    private static final int LEVEL_MULTIPLE = 1;
    private static final int LEVEL_ANY      = 2;

    private Pattern _from = null;
    private Pattern _count = null;
    private Expression _value = null;

    private AttributeValueTemplate _lang = null;
    private AttributeValueTemplate _format = null;
    private AttributeValueTemplate _letterValue = null;
    private AttributeValueTemplate _groupingSeparator = null;
    private AttributeValueTemplate _groupingSize = null;

    private int _level = LEVEL_SINGLE;
    private boolean _formatNeeded = false;

    static final private String[] ClassNames = { 
	"org.apache.xalan.xsltc.dom.SingleNodeCounter",	   // LEVEL_SINGLE
	"org.apache.xalan.xsltc.dom.MultipleNodeCounter",	   // LEVEL_MULTIPLE
	"org.apache.xalan.xsltc.dom.AnyNodeCounter"	   // LEVEL_ANY
    };

    static final private String[] FieldNames = { 
	"___single_node_counter",		   // LEVEL_SINGLE
	"___multiple_node_counter",		   // LEVEL_MULTIPLE
	"___any_node_counter"			   // LEVEL_ANY
    };

    public void parseContents(Element element, Parser parser) {
	NamedNodeMap attributes = element.getAttributes();
	final int nAttributes = attributes.getLength();

	for (int i = 0; i < nAttributes; i++) {
	    final Attr attribute = (Attr) attributes.item(i);
	    final String name = attribute.getName();
	    final String value = attribute.getValue();

	    if (name.equals("value")) {
		_value = parser.parseExpression(this, element, name);
	    }
	    else if (name.equals("count")) {
		_count = parser.parsePattern(this, element, name);
	    }
	    else if (name.equals("from")) {
		_from = parser.parsePattern(this, element, name);
	    }
	    else if (name.equals("level")) {
		if (value.equals("single")) {
		    _level = LEVEL_SINGLE;
		}
		else if (value.equals("multiple")) {
		    _level = LEVEL_MULTIPLE;
		}
		else if (value.equals("any")) {
		    _level = LEVEL_ANY;
		}
	    }
	    else if (name.equals("format")) {
		_format = new AttributeValueTemplate(value, parser);
		_formatNeeded = true;
	    }
	    else if (name.equals("lang")) {
		_lang = new AttributeValueTemplate(value, parser);
		_formatNeeded = true;
	    }
	    else if (name.equals("letter-value")) {
		_letterValue = new AttributeValueTemplate(value, parser);
		_formatNeeded = true;
	    }
	    else if (name.equals("grouping-separator")) {
		_groupingSeparator = new AttributeValueTemplate(value, parser);
		_formatNeeded = true;
	    }
	    else if (name.equals("grouping-size")) {
		_groupingSize = new AttributeValueTemplate(value, parser);
		_formatNeeded = true;
	    }
	}
    }

    public Type typeCheck(SymbolTable stable) throws TypeCheckError {
	if (_value != null) {
	    Type tvalue = _value.typeCheck(stable);
	    if (tvalue instanceof RealType == false) {
		_value = new CastExpr(_value, Type.Real);
	    }
	}
	if (_count != null) {
	    _count.typeCheck(stable);
	}
	if (_from != null) {
	    _from.typeCheck(stable);
	}
	if (_format != null) {
	    _format.typeCheck(stable);
	}
	if (_lang != null) {
	    _lang.typeCheck(stable);
	}
	if (_letterValue != null) {
	    _letterValue.typeCheck(stable);
	}
	if (_groupingSeparator != null) {
	    _groupingSeparator.typeCheck(stable);
	}
	if (_groupingSize != null) {
	    _groupingSize.typeCheck(stable);
	}
	return Type.Void;
    }

    /**
     * True if the has specified a value for this instance of number.
     */
    public boolean hasValue() {
	return _value != null;
    }

    /**
     * Returns <tt>true</tt> if this instance of number has neither
     * a from nor a count pattern.
     */
    public boolean isDefault() {
	return _from == null && _count == null;
    }

    private void compileDefault(ClassGenerator classGen,
			        MethodGenerator methodGen) {
	int index;
	ConstantPoolGen cpg = classGen.getConstantPool();
	InstructionList il = methodGen.getInstructionList();

	int[] fieldIndexes = getXSLTC().getNumberFieldIndexes();

	if (fieldIndexes[_level] == -1) {
	    Field defaultNode = new Field(ACC_PRIVATE, 
					  cpg.addUtf8(FieldNames[_level]),
					  cpg.addUtf8(NODE_COUNTER_SIG),
					  null, 
					  cpg.getConstantPool());

	    // Add a new private field to this class
	    classGen.addField(defaultNode);

	    // Get a reference to the newly added field
	    fieldIndexes[_level] = cpg.addFieldref(classGen.getClassName(), 
						   FieldNames[_level],
						   NODE_COUNTER_SIG);
	}

	// Check if field is initialized (runtime)
	il.append(classGen.loadTranslet());
	il.append(new GETFIELD(fieldIndexes[_level]));
	final BranchHandle ifBlock1 = il.append(new IFNONNULL(null));

	// Create an instance of DefaultNodeCounter
	index = cpg.addMethodref(ClassNames[_level],
				 "getDefaultNodeCounter", 
				 "(" + TRANSLET_INTF_SIG
				 + DOM_INTF_SIG
				 + NODE_ITERATOR_SIG 
				 + ")" + NODE_COUNTER_SIG);
	il.append(classGen.loadTranslet());
	il.append(methodGen.loadDOM());
	il.append(methodGen.loadIterator());
	il.append(new INVOKESTATIC(index));
	il.append(DUP);

	// Store the node counter in the field
	il.append(classGen.loadTranslet());
	il.append(SWAP);
	il.append(new PUTFIELD(fieldIndexes[_level]));
	final BranchHandle ifBlock2 = il.append(new GOTO(null));

	// Backpatch conditionals
	ifBlock1.setTarget(il.append(classGen.loadTranslet()));
	il.append(new GETFIELD(fieldIndexes[_level]));
	
	ifBlock2.setTarget(il.append(NOP));
    }

    /**
     * Compiles a constructor for the class <tt>className</tt> that
     * inherits from {Any,Single,Multiple}NodeCounter. This constructor
     * simply calls the same constructor in the super class.
     */
    private void compileConstructor(ClassGenerator classGen,
                                    String className) {
	MethodGenerator cons;
	final InstructionList il = new InstructionList();
	final ConstantPoolGen cpg = classGen.getConstantPool();

	cons = new MethodGenerator(ACC_PUBLIC,
				   de.fub.bytecode.generic.Type.VOID, 
				   new de.fub.bytecode.generic.Type[] {
				       Util.getJCRefType(TRANSLET_INTF_SIG),
				       Util.getJCRefType(DOM_INTF_SIG),
				       Util.getJCRefType(NODE_ITERATOR_SIG)
				   },
				   new String[] {
				       "dom",
				       "translet",
				       "iterator"
				   },
				   "<init>", className, il, cpg);

	il.append(ALOAD_0);     // this
	il.append(ALOAD_1);     // translet
	il.append(ALOAD_2);     // DOM
	il.append(new ALOAD(3));// iterator

	int index = cpg.addMethodref(ClassNames[_level],
				     "<init>", 
				     "(" + TRANSLET_INTF_SIG
				     + DOM_INTF_SIG
				     + NODE_ITERATOR_SIG 
				     + ")V");
	il.append(new INVOKESPECIAL(index));
	il.append(RETURN);
	
	cons.stripAttributes(true);
	cons.setMaxLocals();
	cons.setMaxStack();
	classGen.addMethod(cons.getMethod());
    }

    /**
     * This method compiles code that is common to matchesFrom() and
     * matchesCount() in the auxillary class.
     */
    private void compileLocals(NodeCounterGenerator nodeCounterGen,
			       MatchGenerator matchGen,
			       InstructionList il) {

	ConstantPoolGen cpg = nodeCounterGen.getConstantPool();
	final String className = matchGen.getClassName();
	final String DOM_SIG = nodeCounterGen.getDOMClassSig();
	final String DOM_CLASS = nodeCounterGen.getDOMClass();

	LocalVariableGen local;
	int field;

	// Get NodeCounter._iterator and store locally
	local = matchGen.addLocalVariable("iterator", 
					  Util.getJCRefType(NODE_ITERATOR_SIG),
					  null, null);
	field = cpg.addFieldref(NODE_COUNTER, "_iterator",
				ITERATOR_FIELD_SIG);
	il.append(ALOAD_0); // 'this' pointer on stack
	il.append(new GETFIELD(field));
	il.append(new ASTORE(local.getIndex()));
	matchGen.setIteratorIndex(local.getIndex());
	
	// Get NodeCounter._translet and store locally
	local = matchGen.addLocalVariable("translet", 
				  Util.getJCRefType("Lorg/apache/xalan/xsltc/Translet;"),
				  null, null);
	field = cpg.addFieldref(NODE_COUNTER, "_translet",
				"Lorg/apache/xalan/xsltc/Translet;");
	il.append(ALOAD_0); // 'this' pointer on stack
	il.append(new GETFIELD(field));
	il.append(new ASTORE(local.getIndex()));
	nodeCounterGen.setTransletIndex(local.getIndex());

	// Get NodeCounter._document and store locally
	local = matchGen.addLocalVariable("document", 
					  Util.getJCRefType(DOM_SIG),
					  null, null);
	field = cpg.addFieldref(className, "_document", DOM_INTF_SIG);
	il.append(ALOAD_0); // 'this' pointer on stack
	il.append(new GETFIELD(field));
	// Make sure we have the correct DOM type on the stack!!!
	il.append(new CHECKCAST(cpg.addClass(DOM_CLASS)));
	il.append(new ASTORE(local.getIndex()));
	matchGen.setDomIndex(local.getIndex());
    }

    private void compilePatterns(ClassGenerator classGen,
				 MethodGenerator methodGen) {
	//!!!  local variables?
	int current;
	int field;
	LocalVariableGen local;
	MatchGenerator matchGen;
	NodeCounterGenerator nodeCounterGen;

	final String className = getXSLTC().getHelperClassName();
	nodeCounterGen = new NodeCounterGenerator(className,
						  ClassNames[_level],
						  toString(), 
						  ACC_PUBLIC | ACC_SUPER,
						  null,
						  classGen.getStylesheet());
	InstructionList il = null;
	ConstantPoolGen cpg = nodeCounterGen.getConstantPool();

	// Add a single constructor to the class
	compileConstructor(nodeCounterGen, className);

	/*
	 * Compile method matchesFrom()
	 */
	if (_from != null) {
	    il = new InstructionList();
	    matchGen =
		new MatchGenerator(ACC_PUBLIC | ACC_FINAL,
				   de.fub.bytecode.generic.Type.BOOLEAN, 
				   new de.fub.bytecode.generic.Type[] {
				       de.fub.bytecode.generic.Type.INT,
				   },
				   new String[] {
				       "node",
				   },
				   "matchesFrom", className, il, cpg);

	    compileLocals(nodeCounterGen,matchGen,il);

	    // Translate Pattern
	    il.append(matchGen.loadContextNode());
	    _from.translate(nodeCounterGen, matchGen);
	    _from.synthesize(nodeCounterGen, matchGen);
	    il.append(IRETURN);
		    
	    matchGen.stripAttributes(true);
	    matchGen.setMaxLocals();
	    matchGen.setMaxStack();
	    matchGen.removeNOPs();
	    nodeCounterGen.addMethod(matchGen.getMethod());
	}

	/*
	 * Compile method matchesCount()
	 */
	if (_count != null) {
	    il = new InstructionList();
	    matchGen = new MatchGenerator(ACC_PUBLIC | ACC_FINAL,
					  de.fub.bytecode.generic.Type.BOOLEAN, 
					  new de.fub.bytecode.generic.Type[] {
					      de.fub.bytecode.generic.Type.INT,
					  },
					  new String[] {
					      "node",
					  },
					  "matchesCount", className, il, cpg);

	    compileLocals(nodeCounterGen,matchGen,il);
	    
	    // Translate Pattern
	    il.append(matchGen.loadContextNode());
	    _count.translate(nodeCounterGen, matchGen);
	    _count.synthesize(nodeCounterGen, matchGen);
	    
	    il.append(IRETURN);
		    
	    matchGen.stripAttributes(true);
	    matchGen.setMaxLocals();
	    matchGen.setMaxStack();
	    matchGen.removeNOPs();
	    nodeCounterGen.addMethod(matchGen.getMethod());
	}
	
	getXSLTC().dumpClass(nodeCounterGen.getJavaClass());

	// Push an instance of the newly created class
	cpg = classGen.getConstantPool();
	il = methodGen.getInstructionList();

	final int index = cpg.addMethodref(className, "<init>", 
					   "(" + TRANSLET_INTF_SIG
					   + DOM_INTF_SIG 
					   + NODE_ITERATOR_SIG
					   + ")V");
	il.append(new NEW(cpg.addClass(className)));
	il.append(DUP);
	il.append(classGen.loadTranslet());
	il.append(methodGen.loadDOM());
	il.append(methodGen.loadIterator());
	il.append(new INVOKESPECIAL(index));
    }

    public void translate(ClassGenerator classGen, MethodGenerator methodGen) {
	int index;
	final ConstantPoolGen cpg = classGen.getConstantPool();
	final InstructionList il = methodGen.getInstructionList();

	// Push "this" for the call to characters()
	il.append(classGen.loadTranslet());

	if (hasValue()) {
	    compileDefault(classGen, methodGen);
	    _value.translate(classGen, methodGen);

	    // Round the number to the nearest integer
	    index = cpg.addMethodref(MATH_CLASS, "round", "(D)J");
	    il.append(new INVOKESTATIC(index));
	    il.append(new L2I());

	    // Call setValue on the node counter
	    index = cpg.addMethodref(NODE_COUNTER, 
				     "setValue", 
				     "(I)" + NODE_COUNTER_SIG);
	    il.append(new INVOKEVIRTUAL(index));
	}
	else if (isDefault()) {
	    compileDefault(classGen, methodGen);
	}
	else {
	    compilePatterns(classGen, methodGen);
	}

	// Call setStartNode() 
	if (!hasValue()) {
	    il.append(methodGen.loadContextNode());
	    index = cpg.addMethodref(NODE_COUNTER, 
				     SET_START_NODE, 
				     "(I)" + NODE_COUNTER_SIG);
	    il.append(new INVOKEVIRTUAL(index));
	}

	// Call getCounter() with or without args
	if (_formatNeeded) {
	    if (_format != null) {
		_format.translate(classGen, methodGen);
	    }
	    else {
		il.append(new PUSH(cpg, "1"));
	    }

	    if (_lang != null) {
		_lang.translate(classGen, methodGen);
	    }
	    else {
		il.append(new PUSH(cpg, "en")); 	// TODO ??
	    }

	    if (_letterValue != null) {
		_letterValue.translate(classGen, methodGen);
	    }
	    else {
		il.append(new PUSH(cpg, ""));
	    }

	    if (_groupingSeparator != null) {
		_groupingSeparator.translate(classGen, methodGen);
	    }
	    else {
		il.append(new PUSH(cpg, ""));
	    }

	    if (_groupingSize != null) {
		_groupingSize.translate(classGen, methodGen);
	    }
	    else {
		il.append(new PUSH(cpg, "0"));
	    }

	    index = cpg.addMethodref(NODE_COUNTER, "getCounter", 
				     "(" + STRING_SIG + STRING_SIG 
				     + STRING_SIG + STRING_SIG 
				     + STRING_SIG + ")" + STRING_SIG);
	    il.append(new INVOKEVIRTUAL(index));
	}
	else {
	    index = cpg.addMethodref(NODE_COUNTER, "setDefaultFormatting", 
				     "()" + NODE_COUNTER_SIG);
	    il.append(new INVOKEVIRTUAL(index));

	    index = cpg.addMethodref(NODE_COUNTER, "getCounter", 
				     "()" + STRING_SIG);
	    il.append(new INVOKEVIRTUAL(index));
	}

	// Output the resulting string to the handler
	il.append(methodGen.loadHandler());
	index = cpg.addMethodref(TRANSLET_CLASS,
				 CHARACTERSW,
				 CHARACTERSW_SIG);
	il.append(new INVOKEVIRTUAL(index));
    }
}
