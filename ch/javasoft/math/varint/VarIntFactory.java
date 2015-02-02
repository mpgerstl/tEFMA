package ch.javasoft.math.varint;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A factory creating {@link VarInt} instances of the most compact length
 * that is possible. 
 */
public class VarIntFactory {
	
	/**
	 * The {@link VarIntCache}. The class name for the cache implementation can
	 * be set using the system property {@link VarIntCache#SYSTEM_PROPERTY}.
	 * The implementation must have a default constructor without arguments.
	 */
	public static final VarIntCache CACHE = DefaultVarIntCache.createCache();
	
	/**
	 * Calls {@code IntVarInt#valueOf(int)} if the specified value fits 
	 * into the int range. Otherwise, {@link LongVarInt#valueOf(long)} is 
	 * called.
	 * 
     * @param  	value value of the VarInt to return.
	 * @return 	an instance of {@link IntVarInt} or {@link LongVarInt}
	 * 			depending on the size of the specified value
	 */
	public static VarIntNumber create(long value) {
		if (Integer.MIN_VALUE <= value && value <= Integer.MAX_VALUE) {
			return IntVarInt.valueOf((int)value);
		}
		return LongVarInt.valueOf(value);
	}

	/**
	 * Calls {@code IntVarInt#valueOf(int)} if the specified value fits 
	 * into the int range. If the value fits into the long range, 
	 * {@link LongVarInt#valueOf(long)} is called. Otherwise,
	 * {@link BigIntegerVarInt#valueOf(BigInteger)} is called.
	 * 
     * @param  	value value of the VarInt to return.
	 * @return 	an VarInt instance tightest possible to store the value
	 */
	public static VarIntNumber create(BigInteger value) {
		final int bitlen = value.bitLength();
		if (bitlen <= 31) {
			return IntVarInt.valueOf(value.intValue());
		}
		if (bitlen <= 63) {
			return LongVarInt.valueOf(value.longValue());
		}
		return BigIntegerVarInt.valueOf(value);
	}
	
	/**
	 * Creates a {@link VarInt} value as small as possible, using the most
	 * appropriate constructor or create method. If the number value was
	 * fractional, it is truncated.
	 * 
     * @param  	value value of the VarInt to return.
	 * @return 	an VarInt instance tightest possible to store the value
	 */
	public static VarIntNumber create(Number value) {
		if (value instanceof VarIntNumber) {
			return (VarIntNumber)value;
		}
		if (value instanceof Integer || value instanceof Short || 
			value instanceof Byte || value instanceof AtomicInteger) {
			return IntVarInt.valueOf(value.intValue());
		}
		if (value instanceof Long || value instanceof AtomicLong) {
			return create(value.longValue());
		}
		if (value instanceof BigInteger) {
			return create((BigInteger)value);
		}
		if (value instanceof VarInt) {
			return create(((VarInt)value).toBigInteger());
		}
		if (value instanceof BigDecimal) {
			return create(((BigDecimal)value).toBigInteger());
		}
		return create(value.toString());				
	}

	/**
	 * Casts the value into a {@link VarIntNumber} if possible, or creates a
	 * new one by converting {@code value} to a 
	 * {@link VarInt#toBigInteger() big integer} first.
	 * 
	 * @param value	the value to convert
	 * @return the {@link VarIntNumber} instance, casted if possible
	 */
	public static VarIntNumber convert(VarInt value) {
		return value instanceof VarIntNumber ? (VarIntNumber)value : create(value.toBigInteger());
	}

    /**
     * Translates the String representation of an integer in decimal radix into
     * a VarInt.  The String representation consists of an optional sign 
     * followed by a sequence of one or more digits (0-9). The String may not 
     * contain any extraneous characters (whitespace, for example).
     * <p>
     * This method is compatible with {@link VarInt#toString()}.
     *
     * @param val String representation of an integer
	 * @return 	a VarInt instance tightest possible to store the value
     * @throws NumberFormatException <tt>val</tt> is not a valid representation
     *	       of an integer in decimal radix
     * @see	   VarInt#toString()
     * @see	   VarInt#toString(int)
     * @see	   #create(String, int)
	 */
	public static VarIntNumber create(String val) {
		return create(val, 10);
	}
	
    /**
     * Translates the String representation of an integer in the specified
     * radix into a VarInt.  The String representation consists of an
     * optional sign followed by a sequence of one or more digits in the
     * specified radix.  The character-to-digit mapping is provided by
     * <tt>Character.digit</tt>.  The String may not contain any extraneous
     * characters (whitespace, for example).
     * <p>
     * This method is compatible with {@link VarInt#toString(int)}.
     *
     * @param val String representation of an integer
     * @param radix radix to be used in interpreting <tt>s</tt>.
	 * @return 	a VarInt instance tightest possible to store the value
     * @throws NumberFormatException <tt>val</tt> is not a valid representation
     *	       of an integer in the specified radix, or <tt>radix</tt> is
     *	       outside the range from {@link Character#MIN_RADIX} to
     *	       {@link Character#MAX_RADIX}, inclusive.
     * @see    Character#digit
     * @see	   VarInt#toString(int)
	 */
	public static VarIntNumber create(String val, int radix) {
		if (val.startsWith("+")) {
			val = val.substring(1);
		}
		final double digitlen = val.length();
		final double estimLen32 = 31.*Math.log(2)/Math.log(radix);
		final double estimLen64 = 63.*Math.log(2)/Math.log(radix);
		
		if (digitlen < estimLen32) {
			return IntVarInt.valueOf(Integer.parseInt(val, radix));
		}
		if (digitlen < estimLen64) {
			return create(Long.parseLong(val, radix));
		}
		return create(new BigInteger(val, radix));
	}
	
    /**
     * Translates a byte array containing the two's-complement binary
     * representation of an integer into a VarInt.  The input array is
     * assumed to be in <i>big-endian</i> byte-order: the most significant
     * byte is in the zeroth element.
     *
     * @param  bytes big-endian two's-complement binary representation
     *	       of an integer
     * @throws NumberFormatException <tt>val</tt> is zero bytes long.
     */
	public static VarIntNumber create(byte[] bytes) {
		if (bytes.length == 0) {
			throw new NumberFormatException("zero lenth byte array");
		}
		if (bytes.length <= 4) {
			int value = (0xff & bytes[0]);
			for (int i = 1; i < bytes.length; i++) {
				value <<= 8;
				value |= (0xff & bytes[i]);
			}
			final int shift = (4 - bytes.length) << 3;
			value = (value << shift) >> shift;//reestablish sign
			return IntVarInt.valueOf(value);
		}
		if (bytes.length <= 8) {
			long value = (0xff & bytes[0]);
			for (int i = 1; i < bytes.length; i++) {
				value <<= 8;
				value |= (0xff & bytes[i]);
			}
			final int shift = (8 - bytes.length) << 3;
			value = (value << shift) >> shift;//reestablish sign
			return LongVarInt.valueOf(value);
		}
		return BigIntegerVarInt.valueOf(new BigInteger(bytes));
	}

	/**
     * Reads bytes from the array previously written to it using the
     * {@link VarInt#writeTo(java.io.OutputStream)} method passing a
     * {@link ByteArrayOutputStream}.
     * 
     * @param array		the byte array containing the raw data
     * @param offsetPtr	an IN/OUT argument with the offset pointer into the array
     * @see VarInt#writeTo(java.io.OutputStream)
     */
    public static VarIntNumber readFrom(byte[] array, AtomicInteger offsetPtr) {
    	if (offsetPtr == null) {
    		offsetPtr = new AtomicInteger();
    	}
    	final int header = 0xff & array[offsetPtr.getAndIncrement()];
    	if (0 != (header & 0x80)) {
    		//direct byte value
    		final int value = (header << 25) >> 25; /*get sign back, shift 24 (cause its a byte) + 1*/
    		return IntVarInt.valueOf(value);
    	}
    	final int len;
    	if (header == 0) {
    		//length is a 4 byte int
        	final int byte0 = 0xff & array[offsetPtr.getAndIncrement()];
        	final int byte1 = 0xff & array[offsetPtr.getAndIncrement()];
        	final int byte2 = 0xff & array[offsetPtr.getAndIncrement()];
        	final int byte3 = 0xff & array[offsetPtr.getAndIncrement()];
        	len = ((byte0 << 24) | (byte1 << 16) | (byte2 << 8) | byte3);
    	}
    	else {
    		len = header;
    	}
    	if (len <= 4) {
    		int value = 0xff & array[offsetPtr.getAndIncrement()];
    		for (int i = 1; i < len; i++) {
    			value <<= 8;
				value |= 0xff & array[offsetPtr.getAndIncrement()];
			}
			final int shift = (4 - len) << 3;
			value = (value << shift) >> shift;//reestablish sign
			return IntVarInt.valueOf(value);
    	}
    	else if (len <= 8) {
    		long value = 0xff & array[offsetPtr.getAndIncrement()];
    		for (int i = 1; i < len; i++) {
    			value <<= 8;
				value |= 0xff & array[offsetPtr.getAndIncrement()];
			}
			final long shift = (8 - len) << 3;
			value = (value << shift) >> shift;//reestablish sign
			return LongVarInt.valueOf(value);
    	}
    	final byte[] bytes = new byte[len];
    	System.arraycopy(array, offsetPtr.getAndAdd(len), bytes, 0, len);
    	return BigIntegerVarInt.valueOf(new BigInteger(bytes));
    }

    /**
     * Reads bytes from the input stream previously written to it using the
     * {@link VarInt#writeTo(java.io.OutputStream)} method.
     * 
     * @param in	the stream to read the bytes from
     * @throws IOException	an I/O exception occurrs as a consequence of the 
     * 						read operation
     * @see VarInt#writeTo(java.io.OutputStream)
     */
    public static VarIntNumber readFrom(InputStream in) throws IOException {
    	final int header = readByte(in);
    	if (0 != (header & 0x80)) {
    		//direct byte value
    		final int value = (header << 25) >> 25; /*get sign back, shift 24 (cause its a byte) + 1*/
    		return IntVarInt.valueOf(value);
    	}
    	final int len;
    	if (header == 0) {
    		//length is a 4 byte int
        	final int byte0 = readByte(in);
        	final int byte1 = readByte(in);
        	final int byte2 = readByte(in);
        	final int byte3 = readByte(in);
        	len = ((byte0 << 24) | (byte1 << 16) | (byte2 << 8) | byte3);
    	}
    	else {
    		len = header;
    	}
    	if (len <= 4) {
    		int value = readByte(in);
    		for (int i = 1; i < len; i++) {
    			value <<= 8;
				value |= readByte(in);
			}
			final int shift = (4 - len) << 3;
			value = (value << shift) >> shift;//reestablish sign
			return IntVarInt.valueOf(value);
    	}
    	else if (len <= 8) {
    		long value = readByte(in);
    		for (int i = 1; i < len; i++) {
    			value <<= 8;
				value |= readByte(in);
			}
			final long shift = (8 - len) << 3;
			value = (value << shift) >> shift;//reestablish sign
			return LongVarInt.valueOf(value);
    	}
    	final byte[] bytes = new byte[len];
    	in.read(bytes);
    	return BigIntegerVarInt.valueOf(new BigInteger(bytes));
    }
    
    private static int readByte(InputStream in) throws IOException {
    	final int val = in.read();
    	if (val == -1) {
    		throw new IOException("unexpected end of stream");
    	}
    	return 0xff & val;
    }
    
    static byte[] toByteArray(long value) {
    	if (value < 0) {
    		if (value <= 0xffffffff80000000L) {
    			if (value <= 0xffff800000000000L) {
        			if (value <= 0xff80000000000000L) {
        				return new byte[] {
        					(byte)(value >> 56), (byte)(value >> 48), (byte)(value >> 40), (byte)(value >> 32),
        					(byte)(value >> 24), (byte)(value >> 16), (byte)(value >> 8), (byte)value
        				};
        			}
    				return new byte[] {
    					(byte)(value >> 48), (byte)(value >> 40), (byte)(value >> 32),
    					(byte)(value >> 24), (byte)(value >> 16), (byte)(value >> 8), (byte)value
       				};    				
    			}
    			if (value <= 0xffffff8000000000L) {
    				return new byte[] {
    					(byte)(value >> 40), (byte)(value >> 32),
    					(byte)(value >> 24), (byte)(value >> 16), (byte)(value >> 8), (byte)value
    				};
    			}
				return new byte[] {
					(byte)(value >> 32),
					(byte)(value >> 24), (byte)(value >> 16), (byte)(value >> 8), (byte)value
				};
    		}
			return toByteArray((int)value);
    	}
		if (value >= 0x000000007fffffffL) {
			if (value >= 0x00007fffffffffffL) {
    			if (value >= 0x007fffffffffffffL) {
    				return new byte[] {
    					(byte)(value >> 56), (byte)(value >> 48), (byte)(value >> 40), (byte)(value >> 32),
    					(byte)(value >> 24), (byte)(value >> 16), (byte)(value >> 8), (byte)value
    				};
    			}
				return new byte[] {
					(byte)(value >> 48), (byte)(value >> 40), (byte)(value >> 32),
					(byte)(value >> 24), (byte)(value >> 16), (byte)(value >> 8), (byte)value
   				};    				
			}
			if (value >= 0x0000007fffffffffL) {
				return new byte[] {
					(byte)(value >> 40), (byte)(value >> 32),
					(byte)(value >> 24), (byte)(value >> 16), (byte)(value >> 8), (byte)value
				};
			}
			return new byte[] {
				(byte)(value >> 32),
				(byte)(value >> 24), (byte)(value >> 16), (byte)(value >> 8), (byte)value
			};
		}
		return toByteArray((int)value);
    }
    static byte[] toByteArray(int value) {
    	if (value < 0) {
    		if (value <= 0xffff8000) {
    			if (value <= 0xff800000) {
        			return new byte[] {(byte)(value >> 24), (byte)(value >> 16), (byte)(value >> 8), (byte)value};    				
    			}
    			return new byte[] {(byte)(value >> 16), (byte)(value >> 8), (byte)value};    				
    		}
			if (value <= 0xffffff80) {
    			return new byte[] {(byte)(value >> 8), (byte)value};    				
			}
			return new byte[] {(byte)value};
    	}
		if (value >= 0x00007fff) {
			if (value >= 0x007fffff) {
    			return new byte[] {(byte)(value >> 24), (byte)(value >> 16), (byte)(value >> 8), (byte)value};    				
			}
			return new byte[] {(byte)(value >> 16), (byte)(value >> 8), (byte)value};    				
		}
		if (value >= 0x0000007f) {
			return new byte[] {(byte)(value >> 8), (byte)value};    				
		}
		return new byte[] {(byte)value};
    }
    
    private static boolean writeByteLength(OutputStream out, int length) throws IOException {
    	if (length < 0x7f) {
        	out.write((byte)length);    		
    	}
    	else {
	    	out.write((byte)0);//indicates that the length is a 4 byte field
	    	out.write((byte)(length >>> 24));
	    	out.write((byte)(length >>> 16));
	    	out.write((byte)(length >>> 8));
	    	out.write((byte)length);
    	}
    	return true;
    }
    static void writeBytes(OutputStream out, byte[] bytes) throws IOException {
		if (bytes.length == 0) {
			out.write(0x80 /*set sign bit*/);
		}
		else if (bytes.length == 1 && 0xc0 <= bytes[0] && bytes[0] <= 0x3f) {
			out.write(0x80 | bytes[0] /*set sign bit*/);
		}
		else {
			VarIntFactory.writeByteLength(out, bytes.length);
			for (int i = 0; i < bytes.length; i++) {
				out.write(bytes[i]);
			}
		}
    	
    }
    static void writeLong(OutputStream out, long value) throws IOException {
    	boolean lenWritten = false;
    	if (value < 0) {
    		if (value <= 0xffffffff80000000L) {
    			if (value <= 0xffff800000000000L) {
        			if (value <= 0xff80000000000000L) {
                		lenWritten = lenWritten || writeByteLength(out, 8);
                    	out.write((byte)(value >>> 56));
        			}
            		lenWritten = lenWritten || writeByteLength(out, 7);
                	out.write((byte)(value >>> 48));
    			}
    			if (value <= 0xffffff8000000000L) {
            		lenWritten = lenWritten || writeByteLength(out, 6);
                	out.write((byte)(value >>> 40));
    			}
        		lenWritten = lenWritten || writeByteLength(out, 5);
            	out.write((byte)(value >>> 32));
            	out.write((byte)(value >>> 24));
            	out.write((byte)(value >>> 16));
            	out.write((byte)(value >>> 8));
            	out.write((byte)value);
    		}
    		else {
    			writeInt(out, (int)value, lenWritten);
    		}
    	}
    	else {
			if (value >= 0x000000007fffffffL) {
				if (value >= 0x00007fffffffffffL) {
	    			if (value >= 0x007fffffffffffffL) {
	            		lenWritten = lenWritten || writeByteLength(out, 8);
	                	out.write((byte)(value >>> 56));
	    			}
	        		lenWritten = lenWritten || writeByteLength(out, 7);
	            	out.write((byte)(value >>> 48));
				}
				if (value >= 0x0000007fffffffffL) {
	        		lenWritten = lenWritten || writeByteLength(out, 6);
	            	out.write((byte)(value >>> 40));
				}
	    		lenWritten = lenWritten || writeByteLength(out, 5);
	        	out.write((byte)(value >>> 32));
            	out.write((byte)(value >>> 24));
            	out.write((byte)(value >>> 16));
            	out.write((byte)(value >>> 8));
            	out.write((byte)value);
			}
			else {
				writeInt(out, (int)value, lenWritten);
			}
    	}
    }
    static void writeInt(OutputStream out, int value) throws IOException {
    	writeInt(out, value, false);
    }
    private static void writeInt(OutputStream out, int value, boolean lenWritten) throws IOException {
    	if (value < 0) {
    		if (value <= 0xffff8000) {
    			if (value <= 0xff800000) {
            		lenWritten = lenWritten || writeByteLength(out, 4);
                	out.write((byte)(value >>> 24));
    			}
        		lenWritten = lenWritten || writeByteLength(out, 3);
            	out.write((byte)(value >>> 16));
    		}
			if (value <= 0xffffff80) {
        		lenWritten = lenWritten || writeByteLength(out, 2);
            	out.write((byte)(value >>> 8));
			}
			if (!lenWritten && value >= 0xffffffc0) {
				//write only value, no length
				out.write(0x80 | (0xff & value));
			}
			else {
				lenWritten = lenWritten || writeByteLength(out, 1);
				out.write((byte)value);
			}
    	}
    	else {
    		if (value >= 0x00007fff) {
    			if (value >= 0x007fffff) {
            		lenWritten = lenWritten || writeByteLength(out, 4);
                	out.write((byte)(value >>> 24));
    			}
        		lenWritten = lenWritten || writeByteLength(out, 3);
            	out.write((byte)(value >>> 16));
    		}
    		if (value >= 0x0000007f) {
        		lenWritten = lenWritten || writeByteLength(out, 2);
            	out.write((byte)(value >>> 8));
    		}
			if (!lenWritten && value <= 0x0000003f) {
				out.write(0x80 | (0xff & value));
			}
			else {
				lenWritten = lenWritten || writeByteLength(out, 1);
				out.write((byte)value);
			}
    	}
    }
}
