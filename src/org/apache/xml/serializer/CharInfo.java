/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 1999-2003 The Apache Software Foundation.  All rights 
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
 * originally based on software copyright (c) 1999, Lotus
 * Development Corporation., http://www.lotus.com.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package org.apache.xml.serializer;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Hashtable;
import java.util.PropertyResourceBundle;
import java.util.Enumeration;
import java.util.ResourceBundle;

import javax.xml.transform.TransformerException;

import org.apache.xml.res.XMLErrorResources;
import org.apache.xml.res.XMLMessages;
import org.apache.xml.utils.CharKey;
import org.apache.xml.utils.ObjectFactory;
import org.apache.xml.utils.SystemIDResolver;
import org.apache.xml.utils.WrappedRuntimeException;

/**
 * This class provides services that tell if a character should have
 * special treatement, such as entity reference substitution or normalization
 * of a newline character.  It also provides character to entity reference
 * lookup.
 *
 * DEVELOPERS: See Known Issue in the constructor.
 */
public class CharInfo
{
    /** Lookup table for characters to entity references. */
    private Hashtable m_charToEntityRef = new Hashtable();

    /**
     * The name of the HTML entities file.
     * If specified, the file will be resource loaded with the default class loader.
     */
    public static String HTML_ENTITIES_RESOURCE = "org.apache.xml.serializer.HTMLEntities";

    /**
     * The name of the XML entities file.
     * If specified, the file will be resource loaded with the default class loader.
     */
    public static String XML_ENTITIES_RESOURCE = "org.apache.xml.serializer.XMLEntities";

    /** The linefeed character, which the parser should always normalize. */
    public static final char S_LINEFEED = 0x0A;

    /** The carriage return character, which the parser should always normalize. */
    public static char S_CARRIAGERETURN = 0x0D;
    
    /** This flag is an optimization for HTML entities. It false if entities 
     * other than quot (34), amp (38), lt (60) and gt (62) are defined
     * in the range 0 to 127.
     */
    
    public final boolean onlyQuotAmpLtGt;
    
    /** Copy the first 0,1 ... ASCII_MAX values into an array */
    private static final int ASCII_MAX = 128;
    
    /** Array of values is faster access than a set of bits */
    private boolean[] quickASCII = new boolean[ASCII_MAX];

    private boolean[] isCleanASCII = new boolean[ASCII_MAX];

    /** An array of bits to record if the character is in the set.
     * Although information in this array is complete, the
     * quickASCII array is used first because access to its values
     * is common and faster.
     */   
    private int array_of_bits[] = createEmptySetOfIntegers(65535);
     
    
    // 5 for 32 bit words,  6 for 64 bit words ...
    /*
     * This constant is used to shift an integer to quickly
     * calculate which element its bit is stored in.
     * 5 for 32 bit words (int) ,  6 for 64 bit words (long)
     */
    private static final int SHIFT_PER_WORD = 5;
    
    /*
     * A mask to get the low order bits which are used to
     * calculate the value of the bit within a given word,
     * that will represent the presence of the integer in the 
     * set.
     * 
     * 0x1F for 32 bit words (int),
     * or 0x3F for 64 bit words (long) 
     */
    private static final int LOW_ORDER_BITMASK = 0x1f;
    
    /*
     * This is used for optimizing the lookup of bits representing
     * the integers in the set. It is the index of the first element
     * in the array array_of_bits[] that is not used.
     */
    private int firstWordNotUsed;


    /**
     * Constructor that reads in a resource file that describes the mapping of
     * characters to entity references.
     * This constructor is private, just to force the use
     * of the getCharInfo(entitiesResource) factory
     *
     * Resource files must be encoded in UTF-8 and can either be properties
     * files with a .properties extension assumed.  Alternatively, they can
     * have the following form, with no particular extension assumed:
     *
     * <pre>
     * # First char # is a comment
     * Entity numericValue
     * quot 34
     * amp 38
     * </pre>
     *    
     * @param entitiesResource Name of properties or resource file that should
     * be loaded, which describes that mapping of characters to entity
     * references.
     */
    private CharInfo(String entitiesResource)
    {
        this(entitiesResource, false);
    }

    private CharInfo(String entitiesResource, boolean internal)
    {
        ResourceBundle entities = null;
        boolean noExtraEntities = true;

        // Make various attempts to interpret the parameter as a properties
        // file or resource file, as follows:
        //
        //   1) attempt to load .properties file using ResourceBundle
        //   2) try using the class loader to find the specified file a resource
        //      file
        //   3) try treating the resource a URI

        if (internal) { 
            try {
                // Load entity property files by using PropertyResourceBundle,
                // cause of security issure for applets
                entities = PropertyResourceBundle.getBundle(entitiesResource);
            } catch (Exception e) {}
        }

        if (entities != null) {
            Enumeration enum = entities.getKeys();
            while (enum.hasMoreElements()){
                String name = (String) enum.nextElement();
                String value = entities.getString(name);
                int code = Integer.parseInt(value);
                defineEntity(name, (char) code);
                if (extraEntity(code))
                    noExtraEntities = false;
            }
            set(S_LINEFEED);
            set(S_CARRIAGERETURN);
        } else {
            InputStream is = null;

            // Load user specified resource file by using URL loading, it
            // requires a valid URI as parameter
            try {
                if (internal) {
                    is = CharInfo.class.getResourceAsStream(entitiesResource);
                } else {
                    ClassLoader cl = ObjectFactory.findClassLoader();
                    if (cl == null) {
                        is = ClassLoader.getSystemResourceAsStream(entitiesResource);
                    } else {
                        is = cl.getResourceAsStream(entitiesResource);
                    }

                    if (is == null) {
                        try {
                            URL url = new URL(entitiesResource);
                            is = url.openStream();
                        } catch (Exception e) {}
                    }
                }

                if (is == null) {
                    throw new RuntimeException(
                        XMLMessages.createXMLMessage(
                            XMLErrorResources.ER_RESOURCE_COULD_NOT_FIND,
                            new Object[] {entitiesResource, entitiesResource}));
                }

                // Fix Bugzilla#4000: force reading in UTF-8
                //  This creates the de facto standard that Xalan's resource 
                //  files must be encoded in UTF-8. This should work in all
                // JVMs.
                //
                // %REVIEW% KNOWN ISSUE: IT FAILS IN MICROSOFT VJ++, which
                // didn't implement the UTF-8 encoding. Theoretically, we should
                // simply let it fail in that case, since the JVM is obviously
                // broken if it doesn't support such a basic standard.  But
                // since there are still some users attempting to use VJ++ for
                // development, we have dropped in a fallback which makes a
                // second attempt using the platform's default encoding. In VJ++
                // this is apparently ASCII, which is subset of UTF-8... and
                // since the strings we'll be reading here are also primarily
                // limited to the 7-bit ASCII range (at least, in English
                // versions of Xalan), this should work well enough to keep us
                // on the air until we're ready to officially decommit from
                // VJ++.

                BufferedReader reader;
                try {
                    reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    reader = new BufferedReader(new InputStreamReader(is));
                }

                String line = reader.readLine();

                while (line != null) {
                    if (line.length() == 0 || line.charAt(0) == '#') {
                        line = reader.readLine();

                        continue;
                    }

                    int index = line.indexOf(' ');

                    if (index > 1) {
                        String name = line.substring(0, index);

                        ++index;

                        if (index < line.length()) {
                            String value = line.substring(index);
                            index = value.indexOf(' ');

                            if (index > 0) {
                                value = value.substring(0, index);
                            }

                            int code = Integer.parseInt(value);

                            defineEntity(name, (char) code);
                            if (extraEntity(code))
                                noExtraEntities = false;
                        }
                    }

                    line = reader.readLine();
                }

                is.close();
                set(S_LINEFEED);
                set(S_CARRIAGERETURN);
            } catch (Exception e) {
                throw new RuntimeException(
                    XMLMessages.createXMLMessage(
                        XMLErrorResources.ER_RESOURCE_COULD_NOT_LOAD,
                        new Object[] { entitiesResource,
                                       e.toString(),
                                       entitiesResource,
                                       e.toString()}));
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (Exception except) {}
                }
            }
        }

        onlyQuotAmpLtGt = noExtraEntities;

        // initialize the array with a cache of the BitSet values
        for (int i=0; i<ASCII_MAX; i++)
            quickASCII[i] = get(i);    
          
        // initialize the array with a cache of values
        // for use by ToStream.character(char[], int , int)
        for (int ch = 0; ch <ASCII_MAX; ch++)
        if((((0x20 <= ch || (0x0A == ch || 0x0D == ch || 0x09 == ch)))
             && (!get(ch))) || ('"' == ch))
        {
            isCleanASCII[ch] = true;
        }
        else {
            isCleanASCII[ch] = false;     
        }
    }

    /**
     * Defines a new character reference. The reference's name and value are
     * supplied. Nothing happens if the character reference is already defined.
     * <p>Unlike internal entities, character references are a string to single
     * character mapping. They are used to map non-ASCII characters both on
     * parsing and printing, primarily for HTML documents. '&lt;amp;' is an
     * example of a character reference.</p>
     *
     * @param name The entity's name
     * @param value The entity's value
     */
    protected void defineEntity(String name, char value)
    {
        CharKey character = new CharKey(value);

        m_charToEntityRef.put(character, name);
        set(value);
    }

    private CharKey m_charKey = new CharKey();

    /**
     * Resolve a character to an entity reference name.
     *
     * This is reusing a stored key object, in an effort to avoid
     * heap activity. Unfortunately, that introduces a threading risk.
     * Simplest fix for now is to make it a synchronized method, or to give
     * up the reuse; I see very little performance difference between them.
     * Long-term solution would be to replace the hashtable with a sparse array
     * keyed directly from the character's integer value; see DTM's
     * string pool for a related solution.
     *
     * @param value character value that should be resolved to a name.
     *
     * @return name of character entity, or null if not found.
     */
    synchronized public String getEntityNameForChar(char value)
    {
        // CharKey m_charKey = new CharKey(); //Alternative to synchronized
        m_charKey.setChar(value);
        return (String) m_charToEntityRef.get(m_charKey);
    }

    /**
     * Tell if the character argument should have special treatment.
     *
     * @param value character value.
     *
     * @return true if the character should have any special treatment, such as
     * when writing out attribute values, or entity references.
     */
    public final boolean isSpecial(int value)
    {
        // for performance try the values in the boolean array first,
        // this is faster access than the BitSet for common ASCII values

        if (value < ASCII_MAX)
            return quickASCII[value];

        // rather than java.util.BitSet, our private
        // implementation is faster (and less general).
        return get(value);
    }
    
    /**
     * This method is used to determine if an ASCII character is "clean"
     * @param value the character to check (0 to 127).
     * @return true if the character can go to the writer as-is
     */
    public final boolean isASCIIClean(int value)
    {
        return isCleanASCII[value];
    }
    
//  In the future one might want to use the array directly and avoid
//  the method call, but I think the JIT alreay inlines this well enough
//  so don't do it (for now) - bjm    
//    public final boolean[] getASCIIClean()
//    {
//        return isCleanASCII;
//    }


    /**
     * Factory that reads in a resource file that describes the mapping of
     * characters to entity references.
     *
     * Resource files must be encoded in UTF-8 and have a format like:
     * <pre>
     * # First char # is a comment
     * Entity numericValue
     * quot 34
     * amp 38
     * </pre>
     * (Note: Why don't we just switch to .properties files? Oct-01 -sc)
     *
     * @param entitiesResource Name of entities resource file that should
     * be loaded, which describes that mapping of characters to entity references.
     */
    public static CharInfo getCharInfo(String entitiesFileName)
    {
        CharInfo charInfo = (CharInfo) m_getCharInfoCache.get(entitiesFileName);
        if (charInfo != null) {
            return charInfo;
        }

        // try to load it internally - cache
        try {
            charInfo = new CharInfo(entitiesFileName, true);
            m_getCharInfoCache.put(entitiesFileName, charInfo);
            return charInfo;
        } catch (Exception e) {}

        // try to load it externally - do not cache
        try {
            return new CharInfo(entitiesFileName);
        } catch (Exception e) {}

        String absoluteEntitiesFileName;

        if (entitiesFileName.indexOf(':') < 0) {
            absoluteEntitiesFileName =
                SystemIDResolver.getAbsoluteURIFromRelative(entitiesFileName);
        } else {
            try {
                absoluteEntitiesFileName =
                    SystemIDResolver.getAbsoluteURI(entitiesFileName, null);
            } catch (TransformerException te) {
                throw new WrappedRuntimeException(te);
            }
        }

        return new CharInfo(absoluteEntitiesFileName, false);
    }

    /** Table of user-specified char infos. */
    private static Hashtable m_getCharInfoCache = new Hashtable();

    /**
     * Returns the array element holding the bit value for the
     * given integer
     * @param i the integer that might be in the set of integers
     * 
     */
    private static int arrayIndex(int i) {
        return (i >> SHIFT_PER_WORD);
    }

    /**
     * For a given integer in the set it returns the single bit
     * value used within a given word that represents whether
     * the integer is in the set or not.
     */
    private static int bit(int i) {
        int ret = (1 << (i & LOW_ORDER_BITMASK));
        return ret;
    }

    /**
     * Creates a new empty set of integers (characters)
     * @param max the maximum integer to be in the set.
     */
    private int[] createEmptySetOfIntegers(int max) {
        firstWordNotUsed = 0; // an optimization 

        int[] arr = new int[arrayIndex(max - 1) + 1];
            return arr;
 
    }

    /**
     * Adds the integer (character) to the set of integers.
     * @param i the integer to add to the set, valid values are 
     * 0, 1, 2 ... up to the maximum that was specified at
     * the creation of the set.
     */
    private final void set(int i) {        
        int j = (i >> SHIFT_PER_WORD); // this word is used
        int k = j + 1;       
        
        if(firstWordNotUsed < k) // for optimization purposes.
            firstWordNotUsed = k;
            
        array_of_bits[j] |= (1 << (i & LOW_ORDER_BITMASK));
    }


    /**
     * Return true if the integer (character)is in the set of integers.
     * 
     * This implementation uses an array of integers with 32 bits per
     * integer.  If a bit is set to 1 the corresponding integer is 
     * in the set of integers.
     * 
     * @param i an integer that is tested to see if it is the
     * set of integers, or not.
     */
    private final boolean get(int i) {

        boolean in_the_set = false;
        int j = (i >> SHIFT_PER_WORD); // wordIndex(i)
        // an optimization here, ... a quick test to see
        // if this integer is beyond any of the words in use
        if(j < firstWordNotUsed)
            in_the_set = (array_of_bits[j] & 
                          (1 << (i & LOW_ORDER_BITMASK))
            ) != 0;  // 0L for 64 bit words
        return in_the_set;
    }
    
    // record if there are any entities other than
    // quot, amp, lt, gt  (probably user defined)
    /**
     * @return true if the entity 
     * @param code The value of the character that has an entity defined
     * for it.
     */
    private boolean extraEntity(int entityValue)
    {
        boolean extra = false;
        if (entityValue < 128)
        {
            switch (entityValue)
            {
                case 34 : // quot
                case 38 : // amp
                case 60 : // lt
                case 62 : // gt
                    break;
                default : // other entity in range 0 to 127  
                    extra = true;
            }
        }
        return extra;
    }    
}