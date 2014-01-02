/**
 * The FreeBSD Copyright
 * Copyright 1994-2008 The FreeBSD Project. All rights reserved.
 * Copyright (C) 2014 Philip Helger ph[at]phloc[dot]com
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *    1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 *    2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE FREEBSD PROJECT ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE FREEBSD PROJECT OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation
 * are those of the authors and should not be interpreted as representing
 * official policies, either expressed or implied, of the FreeBSD Project.
 */
package com.helger.as2.cmd.processor;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.StringReader;

import javax.annotation.Nullable;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * used to parse commands from the socket command processor message format
 * <command userid="abc" pasword="xyz"> the actual command </command>
 * 
 * @author joseph mcverry
 */

public class SocketCommandParser extends DefaultHandler
{
  private final SAXParser m_aParser;
  private String m_sUserID;
  private String m_sPassword;
  private String m_sCommandText;

  /** simple string processor */
  protected CharArrayWriter m_aContents = new CharArrayWriter ();

  /**
   * construct the factory with a xml parser
   * 
   * @throws Exception
   *         an xml parser exception
   */

  public SocketCommandParser () throws Exception
  {
    final SAXParserFactory spf = SAXParserFactory.newInstance ();
    m_aParser = spf.newSAXParser ();
  }

  public void parse (@Nullable final String inLine) throws SAXException, IOException
  {
    m_sUserID = "";
    m_sPassword = "";
    m_sCommandText = "";
    m_aContents.reset ();

    if (inLine != null)
    {
      final StringReader sr = new StringReader (inLine);
      m_aParser.parse (new InputSource (sr), this);
    }
  }

  /**
   * Method handles #PCDATA
   * 
   * @param ch
   *        array
   * @param start
   *        position in array where next has been placed
   * @param length
   *        int
   */
  @Override
  public void characters (final char ch[], final int start, final int length)
  {
    m_aContents.write (ch, start, length);
  }

  @Override
  public void startElement (final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException
  {
    if (qName.equals ("command"))
    {
      m_sUserID = attributes.getValue ("id");
      m_sPassword = attributes.getValue ("password");
    }
  }

  public String getCommandText ()
  {
    return m_sCommandText;
  }

  public String getPassword ()
  {
    return m_sPassword;
  }

  public String getUserid ()
  {
    return m_sUserID;
  }

  @Override
  public void endElement (final String uri, final String localName, final String qName) throws SAXException
  {
    if (qName.equals ("command"))
    {
      m_sCommandText = m_aContents.toString ();
    }
    else
      m_aContents.flush ();
  }

}
