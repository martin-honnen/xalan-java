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
 * @author Morten Jorgensen
 *
 */

package org.apache.xalan.xsltc.compiler;

import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Vector;

import javax.xml.parsers.*;

import org.xml.sax.*;

import org.apache.xalan.xsltc.compiler.util.Type;
import de.fub.bytecode.generic.*;
import org.apache.xalan.xsltc.compiler.util.*;

final class LiteralElement extends Instruction {
    private String _name;
    private Hashtable _accessedPrefixes = null;
    private LiteralElement _parent;
    private Vector _attributeElements = null;

    private final static String XMLNS_STRING = "xmlns";

    /**
     * Returns the QName for this literal element
     */
    public QName getName() {
	return _qname;
    }
 
    /**
     * Displays the contents of this literal element
     */
    public void display(int indent) {
	indent(indent);
	Util.println("LiteralElement name = " + _name);
	displayContents(indent + IndentIncrement);
    }

    /**
     * Returns the namespace URI for which a prefix is pointing to
     */
    public String accessedNamespace(String prefix) {
	if (_accessedPrefixes == null)
	    return(null);
	else
	    return((String)_accessedPrefixes.get(prefix));
    }

    /**
     * Method used to keep track of what namespaces that are references by
     * this literal element and its attributes. The output must contain a
     * definition for each namespace, so we stuff them in a hashtable.
     */
    public void registerNamespace(String prefix, String uri,
				  SymbolTable stable, boolean declared) {

	// Check if the parent has a declaration for this namespace
	if (_parent != null) {
	    final String parentUri = _parent.accessedNamespace(prefix);
	    if (parentUri == null) {
		_parent.registerNamespace(prefix, uri, stable, declared);
		return;
	    }
	    if (parentUri.equals(uri)) return;
	}

	// Check if we have any declared namesaces
	if (_accessedPrefixes == null) {
	    _accessedPrefixes = new Hashtable();
	}
	else {
	    if (!declared) {
		// Check if this node has a declaration for this namespace
		final String old = (String)_accessedPrefixes.get(prefix);
		if (old != null) {
		    if (old.equals(uri))
			return;
		    else
			prefix = stable.generateNamespacePrefix();
		}
	    }
	}

	if (!prefix.equals("xml")) {
	    _accessedPrefixes.put(prefix,uri);
	}
    }

    /**
     * Translates the prefix of a QName according to the rules set in
     * the attributes of xsl:stylesheet. Also registers a QName to assure
     * that the output element contains the necessary namespace declarations.
     */
    private String translateQName(QName qname, SymbolTable stable) {
	// Break up the QName and get prefix:localname strings
	String localname = qname.getLocalPart();
	String prefix = qname.getPrefix();

	// Treat default namespace as "" and not null
	if (prefix == null)
	    prefix = Constants.EMPTYSTRING;
	else if (prefix.equals(XMLNS_STRING))
	    return(XMLNS_STRING);
	
	// Check if we must translate the prefix
	final String alternative = stable.lookupPrefixAlias(prefix);
	if (alternative != null) {
	    stable.excludeNamespaces(prefix);
	    prefix = alternative;
	}

	// Get the namespace this prefix refers to
	String uri = lookupNamespace(prefix);
	if (uri == null) return(localname);

	// Register the namespace as accessed
	registerNamespace(prefix, uri, stable, false);

	// Construct the new name for the element (may be unchanged)
	if (prefix != Constants.EMPTYSTRING)
	    return(prefix+":"+localname);
	else
	    return(localname);
    }

    /**
     *
     */
    private void addAttribute(SyntaxTreeNode attribute) {
	if (_attributeElements == null) {
	    _attributeElements = new Vector(2);
	}
	_attributeElements.add(attribute);
    }

    /**
     *
     */
    public void addLocalAttribute(SyntaxTreeNode attribute) {
	if (_attributeElements == null) {
	    _attributeElements = new Vector(2);
	}
	_attributeElements.insertElementAt(attribute,0);
    }


    public Type typeCheck(SymbolTable stable) throws TypeCheckError {
	// Type-check all attributes
	if (_attributeElements != null) {
	    final int count = _attributeElements.size();
	    for (int i = 0; i < count; i++) {
		SyntaxTreeNode node = 
		    (SyntaxTreeNode)_attributeElements.elementAt(i);
		node.typeCheck(stable);
	    }
	}
	typeCheckContents(stable);
	return Type.Void;
    }

    /**
     * This method starts at a given node, traverses all namespace mappings,
     * and assembles a list of all prefixes that (for the given node) maps
     * to _ANY_ namespace URI. Used by literal result elements to determine
     */
    public Enumeration getNamespaceScope(SyntaxTreeNode node) {
	Hashtable all = new Hashtable();
	
	while (node != null) {
	    Hashtable mapping = node.getPrefixMapping();
	    if (mapping != null) {
		Enumeration prefixes = mapping.keys();
		while (prefixes.hasMoreElements()) {
		    String prefix = (String)prefixes.nextElement();
		    if (!all.containsKey(prefix)) {
			all.put(prefix, mapping.get(prefix));
		    }
		}
	    }
	    node = node.getParent();
	}
	return(all.keys());
    }

    /**
     * Determines the final QName for the element and its attributes.
     * Registers all namespaces that are used by the element/attributes
     */
    public void parseContents(Parser parser) {

	final SymbolTable stable = parser.getSymbolTable();
	stable.setCurrentNode(this);

	// Find the closest literal element ancestor (if there is one)
	SyntaxTreeNode _parent = getParent();
	while ((_parent != null) && !(_parent instanceof LiteralElement))
	    _parent = _parent.getParent();
	if (!(_parent instanceof LiteralElement))
	    _parent = null;

	_name = translateQName(_qname, stable);

	// Process all attributes and register all namespaces they use
	final int count = _attributes.getLength();
	for (int i = 0; i < count; i++) {

	    final QName qname = parser.getQName(_attributes.getQName(i));
	    final String val = _attributes.getValue(i);

	    // Handle xsl:use-attribute-sets
	    if (qname == parser.getUseAttributeSets()) {
		addAttribute(new UseAttributeSets(val, parser));
	    }
	    // Ignore other attributes in XSL namespace
	    else if (qname.getNamespace().equals(XSLT_URI)) {

	    }
	    // Handle xsl:extension-element-prefixes
	    else if (qname == parser.getExtensionElementPrefixes()) {
		stable.excludeNamespaces(val);
	    }
	    // Handle xsl:exclude-result-prefixes
	    else if (qname == parser.getExcludeResultPrefixes()) {
		stable.excludeNamespaces(val);
	    }
	    // Handle literal attributes (attributes not in XSL namespace)
	    else {
		final String name = translateQName(qname, stable);
		LiteralAttribute attr = new LiteralAttribute(name, val, parser);
		addAttribute(attr);
		attr.setParent(this);
		attr.parseContents(parser);
	    }
	}

	// Register all namespaces that are in scope, except for those that
	// are listed in the xsl:stylesheet element's *-prefixes attributes
	final Enumeration include = getNamespaceScope(this);
	while (include.hasMoreElements()) {
	    final String prefix = (String)include.nextElement();
	    if (!prefix.equals("xml")) {
		final String uri = lookupNamespace(prefix);
		if ((uri != null) && (!uri.equals(XSLT_URI))) {
		    if (!stable.isExcludedNamespace(uri))
			registerNamespace(prefix,uri,stable,true);
		}
	    }
	}

	parseChildren(parser);

	// Process all attributes and register all namespaces they use
	for (int i = 0; i < count; i++) {

	    final QName qname = parser.getQName(_attributes.getQName(i));
	    final String val = _attributes.getValue(i);

	    // Handle xsl:extension-element-prefixes
	    if (qname == parser.getExtensionElementPrefixes()) {
		stable.unExcludeNamespaces(val);
	    }
	    // Handle xsl:exclude-result-prefixes
	    else if (qname == parser.getExcludeResultPrefixes()) {
		stable.unExcludeNamespaces(val);
	    }
	}
    }

    /**
     * 
     */
    public boolean contextDependent() {
	return dependentContents();
    }

    /**
     * Compiles code that emits the literal element to the output handler,
     * first the start tag, then namespace declaration, then attributes,
     * then the element contents, and then the element end tag.
     */
    public void translate(ClassGenerator classGen, MethodGenerator methodGen) {

	final ConstantPoolGen cpg = classGen.getConstantPool();
	final InstructionList il = methodGen.getInstructionList();

	// Compile code to emit element start tag
	il.append(methodGen.loadHandler());
	il.append(new PUSH(cpg, _name));
	il.append(DUP2); // duplicate these 2 args for endElement
	il.append(methodGen.startElement());

	// Output all attributes
	if (_attributeElements != null) {
	    final int count = _attributeElements.size();
	    for (int i = 0; i < count; i++) {
		SyntaxTreeNode node = 
		    (SyntaxTreeNode)_attributeElements.elementAt(i);
		node.translate(classGen, methodGen);
	    }
	}

	// Compile code to emit namespace attributes
	if (_accessedPrefixes != null) {
	    Enumeration e = _accessedPrefixes.keys();
	    while (e.hasMoreElements()) {
		final String prefix = (String)e.nextElement();
		final String uri = (String)_accessedPrefixes.get(prefix);
		if ((uri != Constants.EMPTYSTRING) ||
		    (prefix != Constants.EMPTYSTRING)) {
		    il.append(methodGen.loadHandler());
		    il.append(new PUSH(cpg,prefix));
		    il.append(new PUSH(cpg,uri));
		    il.append(methodGen.namespace());
		}
	    }
	}
	
	// Compile code to emit attributes and child elements
	translateContents(classGen, methodGen);

	// Compile code to emit element end tag
	il.append(methodGen.endElement());
    }
}
