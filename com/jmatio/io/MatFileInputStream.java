package com.jmatio.io;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.jmatio.common.MatDataTypes;

/**
 * MAT-file input stream class. 
 * 
 * @author Wojciech Gradkowski <wgradkowski@gmail.com>
 */
class MatFileInputStream
{
    private int type;
    private ByteBuffer buf;
    
    /**
     * Attach MAT-file input stream to <code>InputStream</code>
     * 
     * @param buf 	input stream
     * @param type	type of data in the stream
     * @see com.jmatio.common.MatDataTypes
     */
    public MatFileInputStream( ByteBuffer buf, int type )
    {
        this.type = type;
        this.buf = buf;
    }
    
    /**
     * Reads data (number of bytes red is determined by <i>data type</i>)
     * from the stream to <code>int</code>.
     * 
     * @throws IOException			if an i/o exception occurs, for instance 
     * 								caused by file access
     */
    public int readInt() throws IOException
    {
        switch ( type )
        {
            case MatDataTypes.miUINT8:
                return ( buf.get() & 0xFF);
            case MatDataTypes.miINT8:
                return buf.get();
            case MatDataTypes.miUINT16:
                return ( buf.getShort() & 0xFFFF);
            case MatDataTypes.miINT16:
                return buf.getShort();
            case MatDataTypes.miUINT32:
                return ( buf.getInt() & 0xFFFFFFFF);
            case MatDataTypes.miINT32:
                return buf.getInt();
            case MatDataTypes.miDOUBLE:
                return (int) buf.getDouble();
            default:
                throw new IllegalArgumentException("Unknown data type: " + type);
        }
    }
    /**
     * Reads data (number of bytes red is determined by <i>data type</i>)
     * from the stream to <code>char</code>.
     * 
     * @return - char
     * @throws IOException			if an i/o exception occurs, for instance 
     * 								caused by file access
     */
    public char readChar() throws IOException
    {
        switch ( type )
        {
            case MatDataTypes.miUINT8:
                return (char)( buf.get() & 0xFF);
            case MatDataTypes.miINT8:
                return (char) buf.get();
            case MatDataTypes.miUINT16:
                return (char)( buf.getShort() & 0xFFFF);
            case MatDataTypes.miINT16:
                return (char) buf.getShort();
            case MatDataTypes.miUINT32:
                return (char)( buf.getInt() & 0xFFFFFFFF);
            case MatDataTypes.miINT32:
                return (char) buf.getInt();
            case MatDataTypes.miDOUBLE:
                return (char) buf.getDouble();
            case MatDataTypes.miUTF8:
                return (char) buf.get();
            default:
                throw new IllegalArgumentException("Unknown data type: " + type);
        }
    }
    /**
     * Reads data (number of bytes red is determined by <i>data type</i>)
     * from the stream to <code>double</code>.
     * 
     * @return - double
     * @throws IOException			if an i/o exception occurs, for instance 
     * 								caused by file access
     */
    public double readDouble() throws IOException
    {
        switch ( type )
        {
            case MatDataTypes.miUINT8:
                return ( buf.get() & 0xFF);
            case MatDataTypes.miINT8:
                return buf.get();
            case MatDataTypes.miUINT16:
                return ( buf.getShort() & 0xFFFF);
            case MatDataTypes.miINT16:
                return buf.getShort();
            case MatDataTypes.miUINT32:
                return ( buf.getInt() & 0xFFFFFFFF);
            case MatDataTypes.miINT32:
                return buf.getInt();
            case MatDataTypes.miDOUBLE:
                return buf.getDouble();
            default:
                throw new IllegalArgumentException("Unknown data type: " + type);
        }
    }

}
