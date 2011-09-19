// ========================================================================
// Copyright (c) 2006-2009 Mort Bay Consulting Pty. Ltd.
// ------------------------------------------------------------------------
// All rights reserved. This program and the accompanying materials
// are made available under the terms of the Eclipse Public License v1.0
// and Apache License v2.0 which accompanies this distribution.
// The Eclipse Public License is available at 
// http://www.eclipse.org/legal/epl-v10.html
// The Apache License v2.0 is available at
// http://www.opensource.org/licenses/apache2.0.php
// You may elect to redistribute this code under either of these licenses. 
// ========================================================================
package org.eclipse.jetty.util;

import java.io.IOException;

/* ------------------------------------------------------------ */
/**
 * Utf8 Appendable abstract base class
 * 
 * This abstract class wraps a standard {@link java.lang.Appendable} and provides methods to append 
 * UTF-8 encoded bytes, that are converted into characters.
 * 
 * This class is stateful and up to 4 calls to {@link #append(byte)} may be needed before 
 * state a character is appended to the string buffer.
 * 
 * The UTF-8 decoding is done by this class and no additional buffers or Readers are used.
 * The UTF-8 code was inspired by http://bjoern.hoehrmann.de/utf-8/decoder/dfa/
 * 
 */
public abstract class Utf8Appendable
{
    private final char REPLACEMENT = '\ufffd';
    private static final int UTF8_ACCEPT = 0;
    private static final int UTF8_REJECT = 12;

    protected final Appendable _appendable;
    protected int _state = UTF8_ACCEPT;

    private static final byte[] BYTE_TABLE =
    {
        // The first part of the table maps bytes to character classes that
        // to reduce the size of the transition table and create bitmasks.
         0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,  0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
         0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,  0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
         0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,  0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
         0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,  0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,
         1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,  9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,
         7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,  7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,
         8,8,2,2,2,2,2,2,2,2,2,2,2,2,2,2,  2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,
        10,3,3,3,3,3,3,3,3,3,3,3,3,4,3,3, 11,6,6,6,5,8,8,8,8,8,8,8,8,8,8,8
    };

    private static final byte[] TRANS_TABLE =
    {
        // The second part is a transition table that maps a combination
        // of a state of the automaton and a character class to a state.
         0,12,24,36,60,96,84,12,12,12,48,72, 12,12,12,12,12,12,12,12,12,12,12,12,
        12, 0,12,12,12,12,12, 0,12, 0,12,12, 12,24,12,12,12,12,12,24,12,24,12,12,
        12,12,12,12,12,12,12,24,12,12,12,12, 12,24,12,12,12,12,12,12,12,24,12,12,
        12,12,12,12,12,12,12,36,12,36,12,12, 12,36,12,12,12,12,12,36,12,36,12,12,
        12,36,12,12,12,12,12,12,12,12,12,12
    };

    private int _codep;

    public Utf8Appendable(Appendable appendable)
    {
        _appendable = appendable;
    }

    public abstract int length();

    protected void reset()
    {
        _state = UTF8_ACCEPT;
    }

    public void append(byte b)
    {
        try
        {
            appendByte(b);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public void append(byte[] b, int offset, int length)
    {
        try
        {
            int end = offset + length;
            for (int i = offset; i < end; i++)
                appendByte(b[i]);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public boolean append(byte[] b, int offset, int length, int maxChars)
    {
        try
        {
            int end = offset + length;
            for (int i = offset; i < end; i++)
            {
                if (length() > maxChars)
                    return false;
                appendByte(b[i]);
            }
            return true;
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    protected void appendByte(byte b) throws IOException
    {

        if (b > 0 && _state == UTF8_ACCEPT)
        {
            _appendable.append((char)(b & 0xFF));
        }
        else
        {
            int i = b & 0xFF;
            int type = BYTE_TABLE[i];
            _codep = _state != UTF8_ACCEPT?(i & 0x3F) | (_codep << 6):(0xFF >> type) & i;
            _state = TRANS_TABLE[_state + type];

            if (_state == UTF8_ACCEPT)
            {
                if (_codep < 0xd800)
                    _appendable.append((char)_codep);
                else
                {
                    char[] chars = Character.toChars(_codep);
                    for (char c : chars)
                        _appendable.append(c);
                }
            }
            else if (_state == UTF8_REJECT)
            {
                _state = UTF8_ACCEPT;
                _appendable.append(REPLACEMENT);
                throw new IllegalArgumentException("Not valid UTF8!");
            }
        }
    }
}