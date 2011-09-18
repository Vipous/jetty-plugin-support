package org.eclipse.jetty.util;

import java.io.IOException;

public abstract class Utf8AppendableNewAlgo
{
    private static final int UTF8_ACCEPT = 0;
    private static final int UTF8_REJECT = 12;
    
    protected final Appendable _appendable;
    protected int _state = UTF8_ACCEPT;
    
    private static final byte[] BYTE_TABLE =
    {
            // The first part of the table maps bytes to character classes that
            // to reduce the size of the transition table and create bitmasks.
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 9, 9, 9,
            9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 8, 8, 2, 2,
            2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 10, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 4, 3, 3, 11, 6, 6, 6,
            5, 8, 8, 8, 8, 8, 8, 8, 8,
            8,
            8,
            8,

            // The second part is a transition table that maps a combination
            // of a state of the automaton and a character class to a state.
            0, 12, 24, 36, 60, 96, 84, 12, 12, 12, 48, 72, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 0, 12, 12, 12, 12, 12, 0, 12, 0, 12, 12, 12, 24,
            12, 12, 12, 12, 12, 24, 12, 24, 12, 12, 12, 12, 12, 12, 12, 12, 12, 24, 12, 12, 12, 12, 12, 24, 12, 12, 12, 12, 12, 12, 12, 24, 12, 12, 12, 12, 12,
            12, 12, 12, 12, 36, 12, 36, 12, 12, 12, 36, 12, 12, 12, 12, 12, 36, 12, 36, 12, 12, 12, 36, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12 };

    private int _codep;

    public Utf8AppendableNewAlgo(Appendable appendable)
    {
        _appendable = appendable;
    }

    public abstract int length();
    
    protected void reset(){
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
        decode(b & 0xFF);
        String string = new String(Character.toChars(_codep));
        if (_state == UTF8_ACCEPT)
            _appendable.append(string);
    }

    private boolean decode(int i)
    {
        int type = BYTE_TABLE[i];
        _codep = _state != UTF8_ACCEPT?(i & 0x3F) | (_codep << 6):(0xFF >> type) & i;
        _state = BYTE_TABLE[256 + _state + type];
        if (_state == UTF8_REJECT)
        {
            _state = UTF8_ACCEPT;
            throw new IllegalArgumentException("Not valid UTF8!");
        }
        return _state == UTF8_ACCEPT;
    }
}