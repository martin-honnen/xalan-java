/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights 
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
package javax.xml.transform.stream;

import javax.xml.transform.Source;

import java.io.InputStream;
import java.io.Reader;

/**
 * Acts as an holder for a transformation Source in the form 
 * of a stream of XML markup.
 *
 * @version Alpha
 * @author <a href="mailto:scott_boag@lotus.com">Scott Boag</a>
 */
public class StreamSource implements Source
{

  /**
   * Zero-argument default constructor. If this constructor 
   * is used, and no other method is called, the transformer 
   * will assume an empty input tree, with a default root node.
   */
  public StreamSource(){}

  /**
   * Construct a StreamSource from a byte stream.  Normally, 
   * a stream should be used rather than a reader, so 
   * the XML parser can resolve character encoding specified 
   * by the XML declaration.
   * 
   * <p>If this constructor is used to process a stylesheet, normally 
   * setSystemId should also be called, so that relative URI references 
   * can be resolved.</p>
   *
   * @param byteStream A valid InputStream reference to an XML stream.
   */
  public StreamSource(InputStream byteStream)
  {
    setByteStream(byteStream);
  }
  
  /**
   * Construct a StreamSource from a byte stream.  Normally, 
   * a stream should be used rather than a reader, so that 
   * the XML parser can resolve character encoding specified 
   * by the XML declaration.
   * 
   * <p>This constructor allows the systemID to be set in addition 
   * to the input stream, which allows relative URIs 
   * to be processed.</p>
   *
   * @param byteStream A valid InputStream reference to an XML stream.
   * @param systemId Must be a String that conforms to the URI syntax.
   */
  public StreamSource(InputStream byteStream, String systemId)
  {
    setByteStream(byteStream);
    setSystemId(systemId);
  }


  /**
   * Construct a StreamSource from a character reader.  Normally, 
   * a stream should be used rather than a reader, so that 
   * the XML parser can resolve character encoding specified 
   * by the XML declaration.  However, in many cases the encoding 
   * of the input stream is already resolved, as in the case of 
   * reading XML from a StringReader.
   *
   * @param characterStream A valid Reader reference to an XML character stream.
   */
  public StreamSource(Reader characterStream)
  {
    setCharacterStream(characterStream);
  }
  
  /**
   * Construct a StreamSource from a character reader.  Normally, 
   * a stream should be used rather than a reader, so that 
   * the XML parser may resolve character encoding specified 
   * by the XML declaration.  However, in many cases the encoding 
   * of the input stream is already resolved, as in the case of 
   * reading XML from a StringReader.
   *
   * @param characterStream A valid Reader reference to an XML character stream.
   * @param systemId Must be a String that conforms to the URI syntax.
   */
  public StreamSource(Reader characterStream, String systemId)
  {
    setCharacterStream(characterStream);
    setSystemId(systemId);
  }


  /**
   * Construct a StreamSource from a URL.
   *
   * @param systemId Must be a String that conforms to the URI syntax.
   */
  public StreamSource(String systemId)
  {
    this.systemId = systemId;
  }

  /**
   * Set the byte stream to be used as input.  Normally, 
   * a stream should be used rather than a reader, so that 
   * the XML parser can resolve character encoding specified 
   * by the XML declaration.
   * 
   * <p>If this Source object is used to process a stylesheet, normally 
   * setSystemId should also be called, so that relative URL references 
   * can be resolved.</p>
   *
   * @param byteStream A valid InputStream reference to an XML stream.
   */
  public void setByteStream(InputStream byteStream)
  {
    this.byteStream = byteStream;
  }

  /**
   * Get the byte stream that was set with setByteStream.
   *
   * @return The byte stream that was set with setByteStream, or null
   * if setByteStream or the ByteStream constructor was not called.
   */
  public InputStream getByteStream()
  {
    return byteStream;
  }

  /**
   * Set the input to be a character reader.  Normally, 
   * a stream should be used rather than a reader, so that 
   * the XML parser can resolve character encoding specified 
   * by the XML declaration.  However, in many cases the encoding 
   * of the input stream is already resolved, as in the case of 
   * reading XML from a StringReader.
   *
   * @param characterStream A valid Reader reference to an XML CharacterStream.   
   */
  public void setCharacterStream(Reader characterStream)
  {
    this.characterStream = characterStream;
  }

  /**
   * Get the character stream that was set with setCharacterStream.
   *
   * @return The character stream that was set with setCharacterStream, or null
   * if setCharacterStream or the CharacterStream constructor was not called.
   */
  public Reader getCharacterStream()
  {
    return characterStream;
  }

  /**
   * Set the public identifier for this Source.
   *
   * <p>The public identifier is always optional: if the application
   * writer includes one, it will be provided as part of the
   * location information.</p>
   * 
   * @param publicId The public identifier as a string.
   */
  public void setPublicId(String publicId)
  {
    this.publicId = publicId;
  }

  /**
   * Get the public identifier that was set with setPublicId.
   *
   * @return The public identifier that was set with setPublicId, or null
   * if setPublicId was not called.
   */
  public String getPublicId()
  {
    return publicId;
  }

  /**
   * Set the system identifier for this Source.
   *
   * <p>The system identifier is optional if there is a byte stream
   * or a character stream, but it is still useful to provide one,
   * since the application can use it to resolve relative URIs
   * and can include it in error messages and warnings (the parser
   * will attempt to open a connection to the URI only if
   * there is no byte stream or character stream specified).</p>
   *
   * @param systemId The system identifier as a URL string.
   */
  public void setSystemId(String systemId)
  {
    this.systemId = systemId;
  }

  /**
   * Get the system identifier that was set with setSystemId.
   *
   * @return The system identifier that was set with setSystemId, or null
   * if setSystemId was not called.
   */
  public String getSystemId()
  {
    return systemId;
  }
  
  //////////////////////////////////////////////////////////////////////
  // Internal state.
  //////////////////////////////////////////////////////////////////////

  /**
   * The public identifier for this input source, or null.
   */
  private String publicId;

  /**
   * The system identifier as a URL string, or null.
   */
  private String systemId;

  /**
   * The byte stream for this Source, or null.
   */
  private InputStream byteStream;

  /**
   * The character stream for this Source, or null.
   */
  private Reader characterStream;
  
}
