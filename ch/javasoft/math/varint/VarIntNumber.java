package ch.javasoft.math.varint;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Implements the methods that are common to most implementations
 * of {@link VarInt}. 
 * <p>
 * Sometimes this class must be used if the numbers have to be a subclass of 
 * {@link Number} (this is a design fault of the Java library---Number should be 
 * an interface). All methods of {@link VarInt} that return a {@link VarInt}
 * instance are redefined to return a {@link VarIntNumber} instead.
 */
abstract public class VarIntNumber extends Number implements VarInt {

	private static final long serialVersionUID = -2250528652440794726L;

	public VarInt[] divideAndRemainder(VarInt val) {
		final BigInteger[] divRem = toBigInteger().divideAndRemainder(val.toBigInteger());
		return new VarInt[] {
			VarIntFactory.create(divRem[0]), VarIntFactory.create(divRem[1])
		};
	}

	public VarIntNumber divide(VarInt val, RoundingMode roundingMode) {
		if (RoundingMode.DOWN.equals(roundingMode)) {
			//standard integer rounding towards zero
			return divide(val);
		}
		final VarInt[] divRem = divideAndRemainder(val);
		final VarIntNumber intval = VarIntFactory.convert(divRem[0]); 
		if (divRem[1].isZero()) {
			return intval;
		}
		final VarIntNumber remval = VarIntFactory.convert(divRem[1]); 
		switch (roundingMode) {
			case UP:
				return intval.isNegative() ? intval.subtract(VarInt.ONE) : intval.add(VarInt.ONE);
			case DOWN:
				throw new RuntimeException("internal error: " + roundingMode + " is handled above");
			case CEILING:
				return intval.isNegative() ? intval : intval.add(VarInt.ONE);
			case FLOOR:
				return intval.isNegative() ? intval.subtract(VarInt.ONE) : intval;
			case HALF_UP:	//fallthrough				
			case HALF_DOWN:	//fallthrough
			case HALF_EVEN:	//fallthrough
				final VarIntNumber remTwice = remval.shiftLeft(1);
				final int cmp = intval.signum() == val.signum() ?
						intval.signum() * remTwice.compareTo(val) :
						remTwice.abs().compareTo(val.abs());
				if (RoundingMode.HALF_DOWN.equals(roundingMode) || RoundingMode.HALF_EVEN.equals(roundingMode) && intval.isEven()) {
					return cmp > 0 ? 
						intval.isNegative() ? intval.subtract(VarInt.ONE) : intval.add(VarInt.ONE)
						: intval;
				}
				return cmp >= 0 ? 
					intval.isNegative() ? intval.subtract(VarInt.ONE) : intval.add(VarInt.ONE)
					: intval;
			case UNNECESSARY:
				throw new ArithmeticException("rounding necessary: " + this + "/" + val);
			default:
				throw new RuntimeException("unknown rounding mode: " + roundingMode);
		}
	}
	
	abstract public VarIntNumber compact();
	
	abstract public VarIntNumber abs();
	
	abstract public VarIntNumber add(VarInt val);
	
	abstract public VarIntNumber divide(VarInt val);
	
	abstract public VarIntNumber gcd(VarInt val);
	
	abstract public VarIntNumber max(VarInt val);
	
	abstract public VarIntNumber min(VarInt val);
	
	abstract public VarIntNumber pow(int exponent);
	
	abstract public VarIntNumber remainder(VarInt val);
	
	abstract public VarIntNumber mod(VarInt m);
	
	abstract public VarIntNumber multiply(VarInt val);
	
	abstract public VarIntNumber negate();
	
	abstract public VarIntNumber shiftLeft(int n);
	
	abstract public VarIntNumber shiftRight(int n);
	
	abstract public VarIntNumber subtract(VarInt val);

	public boolean isZero() {
		return signum() == 0;
	}
	
	public boolean isNegative() {
		return signum() < 0;
	}
	
	public boolean isPositive() {
		return signum() > 0;
	}
	
	public boolean isNonNegative() {
		return signum() >= 0;
	}
	
	public boolean isNonPositive() {
		return signum() <= 0;
	}

	@Override
	public byte byteValue() {
		return (byte)longValue();
	}

	@Override
	public float floatValue() {
		return (float)doubleValue();
	}

	@Override
	public int intValue() {
		return (int)longValue();
	}

	public VarIntNumber numberValue() {
		return this;
	}

	@Override
	public short shortValue() {
		return (short)longValue();
	}
	
	@Override
	public String toString() {
		return toString(10);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (obj instanceof VarInt) {
			return 0 ==compareTo((VarInt)obj);
		}
		return false;
	}
	
	/**
	 * Creates a {@code VarIntNumber} instance delegating to 
	 * {@link VarIntFactory#create(long)}.
	 * 
     * @param  	value value from which a VarIntNumber is created and returned
	 * @return 	an instance of {@link IntVarInt} or {@link LongVarInt}
	 * 			depending on the size of the specified value
	 */
	public static VarIntNumber valueOf(long value) {
		return VarIntFactory.create(value);
	}

	/**
	 * Creates a {@code VarIntNumber} instance delegating to
	 * {@link VarIntFactory#create(BigInteger)}.
	 * 
     * @param  	value value from which a VarIntNumber is created and returned
	 * @return 	an VarInt instance tightest possible to store the value
	 */
	public static VarIntNumber valueOf(BigInteger value) {
		return VarIntFactory.create(value);
	}
	
	/**
	 * Creates a {@code VarIntNumber} instance delegating to
	 * {@link VarIntFactory#create(Number)}.
	 * 
     * @param  	value value from which a VarIntNumber is created and returned
	 * @return 	an VarInt instance tightest possible to store the value
	 */
	public static VarIntNumber valueOf(Number value) {
		return VarIntFactory.create(value);
	}

	/**
	 * Creates a {@code VarIntNumber} instance delegating to
	 * {@link VarIntFactory#convert(VarInt)}. Usually, the returned value is
	 * simply cast into a {@link VarIntNumber} instance.
	 * 
     * @param  	value value from which a VarIntNumber is created and returned
	 * @return the {@link VarIntNumber} instance, casted if possible
	 */
	public static VarIntNumber valueOf(VarInt value) {
		return VarIntFactory.convert(value);
	}

    /**
     * Translates the String representation of an integer in decimal radix into
     * a VarInt.  The String representation consists of an optional sign 
     * followed by a sequence of one or more digits (0-9). The String may not 
     * contain any extraneous characters (whitespace, for example).
     * <p>
     * This method is compatible with {@link VarInt#toString()}.
     * <p>
     * Delegates to {@link VarIntFactory#create(String)}.
     *
     * @param val String representation of an integer
	 * @return 	a VarInt instance tightest possible to store the value
     * @throws NumberFormatException <tt>val</tt> is not a valid representation
     *	       of an integer in decimal radix
     * @see	   VarInt#toString()
     * @see	   VarInt#toString(int)
     * @see	   #valueOf(String, int)
	 */
	public static VarIntNumber valueOf(String val) {
		return VarIntFactory.create(val);
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
     * <p>
     * Delegates to {@link VarIntFactory#create(String, int)}.
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
	public static VarIntNumber valueOf(String val, int radix) {
		return VarIntFactory.create(val, radix);
	}
	
    /**
     * Translates a byte array containing the two's-complement binary
     * representation of an integer into a VarInt.  The input array is
     * assumed to be in <i>big-endian</i> byte-order: the most significant
     * byte is in the zeroth element.
     * <p>
     * Delegates to {@link VarIntFactory#create(byte[])}.
     *
     * @param  bytes big-endian two's-complement binary representation
     *	       of an integer
     * @throws NumberFormatException <tt>val</tt> is zero bytes long.
     */
	public static VarIntNumber valueOf(byte[] bytes) {
		return VarIntFactory.create(bytes);
	}

	/**
     * Reads bytes from the array previously written to it using the
     * {@link VarInt#writeTo(java.io.OutputStream)} method passing a
     * {@link ByteArrayOutputStream}.
     * <p>
     * Delegates to {@link VarIntFactory#readFrom(byte[], AtomicInteger)}.
     * 
     * @param array		the byte array containing the raw data
     * @param offsetPtr	an IN/OUT argument with the offset pointer into the array
     * @see VarInt#writeTo(java.io.OutputStream)
     */
    public static VarIntNumber readFrom(byte[] array, AtomicInteger offsetPtr) {
		return VarIntFactory.readFrom(array, offsetPtr);
    }

    /**
     * Reads bytes from the input stream previously written to it using the
     * {@link VarInt#writeTo(java.io.OutputStream)} method.
     * <p>
     * Delegates to {@link VarIntFactory#readFrom(InputStream)}.
     * 
     * @param in	the stream to read the bytes from
     * @throws IOException	an I/O exception occurrs as a consequence of the 
     * 						read operation
     * @see VarInt#writeTo(java.io.OutputStream)
     */
    public static VarIntNumber readFrom(InputStream in) throws IOException {
		return VarIntFactory.readFrom(in);
    }	
}
