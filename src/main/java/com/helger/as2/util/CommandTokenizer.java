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
package com.helger.as2.util;

import com.helger.as2lib.exception.WrappedOpenAS2Exception;

/**
 * emulates StringTokenizer
 *
 * @author joseph mcverry
 */
public class CommandTokenizer
{
  private final String workString;
  private int pos = 0;
  private int len = -1;

  /**
   * constructor
   *
   * @param inString
   *        in string
   */
  public CommandTokenizer (final String inString)
  {
    workString = inString;
    len = workString.length ();
  }

  /**
   * any more tokens in String
   *
   * @return true if there are any more tokens
   * @throws WrappedOpenAS2Exception
   *         in case another exception occurs
   */
  public boolean hasMoreTokens () throws WrappedOpenAS2Exception
  {
    try
    {
      while (pos < len - 1 && workString.charAt (pos) == ' ')
        pos++;

      if (pos < len)
        return true;

      return false;
    }
    catch (final RuntimeException e)
    {
      throw new WrappedOpenAS2Exception (e);
    }
  }

  /**
   * returns the next token, this handles spaces and quotes
   *
   * @return a string
   * @throws WrappedOpenAS2Exception
   *         In case a RuntimeException occurs
   */
  public String nextToken () throws WrappedOpenAS2Exception
  {

    try
    {
      while (pos < len - 1 && workString.charAt (pos) == ' ')
        pos++;

      final StringBuilder sb = new StringBuilder ();

      while (pos < len && workString.charAt (pos) != ' ')
      {

        if (workString.charAt (pos) == '"')
        {
          pos++;
          while (pos < len && workString.charAt (pos) != '"')
          {
            sb.append (workString.charAt (pos));
            pos++;
          }
          pos++;
          return sb.toString ();
        }
        sb.append (workString.charAt (pos));
        pos++;
      }

      return sb.toString ();
    }
    catch (final RuntimeException e)
    {
      throw new WrappedOpenAS2Exception (e);
    }
  }
}
