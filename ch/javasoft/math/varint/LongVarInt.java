package ch.javasoft.math.varint;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;

/**
 * This class backs all methods with an internal {@code long} 
 * value.
 */
public class LongVarInt extends VarIntNumber {

	private static final long serialVersionUID = 6391220448955407010L;

	protected final long value;	
	
    /**
	 * Constructor with long value
     * <p>
     * NOTE: It is recommended to use the factory method
     * {@link VarIntFactory#create(BigInteger)} instead, as this
     * method allows the dynamic allocation of optimized instances 
     * depending on the true integer value size.
	 */
	protected LongVarInt(long value) {
		this.value = value;
	}
	
    /**
     * Returns a LongVarInt whose value is equal to that of the
     * specified <code>int</code>.  This "static factory method" is
     * provided in preference to a (<code>long</code>) constructor
     * because it allows for reuse of frequently used LongVarInts.
     * <p>
     * NOTE: It is recommended to use the factory method
     * {@link VarIntFactory#create(BigInteger)} instead, as this
     * method allows the dynamic allocation of optimized instances 
     * depending on the true integer value size.
     *
     * @param  value value of the LongVarInt to return.
     * @return a LongVarInt with the specified value.
     */
	public static LongVarInt valueOf(long value) {
		return VarIntFactory.CACHE.cacheGet(value);
	}
	
	@Override
	public VarIntNumber compact() {
		if (Integer.MIN_VALUE <= value && value <= Integer.MAX_VALUE) {
			return IntVarInt.valueOf((int)value);
		}
		return this;
	}
	
	@Override
	public boolean isZero() {
		return value == 0;
	}
	
	public boolean isOne() {
		return value == 1;
	}
	
	@Override
	public boolean isNegative() {
		return value < 0;
	}
	
	@Override
	public boolean isPositive() {
		return value > 0;
	}
	
	@Override
	public boolean isNonNegative() {
		return value >= 0;
	}
	
	@Override
	public boolean isNonPositive() {
		return value <= 0;
	}
	
	public boolean isEven() {
		return 0 == (0x1 & value);
	}	
	
	public boolean testBit(int n) {
		if (n<0)
		    throw new ArithmeticException("Negative bit address");

		return 0 != (value & (1L << n));
	}
	
	public int bitCount() {
		return Long.bitCount(value >= 0 ? value : ~value);
	}
	
	public int bitLength() {
		return 64 - Long.numberOfLeadingZeros(value >= 0 ? value : ~value);
	}

	@Override
	public double doubleValue() {
		return value;
	}
	
	@Override
	public float floatValue() {
		return value;
	}

	@Override
	public long longValue() {
		return value;
	}
	
	@Override
	public int intValue() {
		return (int)value;
	}
	
	@Override
	public VarIntNumber abs() {
		if (value == Long.MIN_VALUE) {
			return VarIntFactory.create(BigInteger.valueOf(value).negate());
		}
		return value < 0 ? VarIntFactory.create(-value) : this;
	}

	@Override
	public VarIntNumber add(VarInt val) {
		final long other;
		if (val instanceof IntVarInt) {
			other = ((IntVarInt)val).value;
		}
		else if (val instanceof LongVarInt) {
			other = ((LongVarInt)val).value;
		}
		else {
			return VarIntFactory.create(toBigInteger().add(val.toBigInteger()));
		}
		long result = value + other;
		if (((value ^ result) & (other ^ result)) < 0) {
			//overflow, fallback to big integer
			return VarIntFactory.create(toBigInteger().add(val.toBigInteger()));
		}
		return VarIntFactory.create(result);
	}

	@Override
	public VarIntNumber divide(VarInt val) {
		if (val instanceof IntVarInt) {
			final int div = ((IntVarInt)val).value;
			return div == -1 ? negate()/*might overflow*/ : VarIntFactory.create(value / div);
		}
		if (val instanceof LongVarInt) {
			final long div = ((LongVarInt)val).value;
			return div == -1 ? negate()/*might overflow*/ : VarIntFactory.create(value / div);
		}
		return VarIntFactory.create(toBigInteger().divide(val.toBigInteger()));
	}

	@Override
	public VarIntNumber gcd(VarInt val) {
		if (val instanceof IntVarInt) {
			return valueOf(gcd(value, ((IntVarInt)val).value));
		}
		if (val instanceof LongVarInt) {
			return valueOf(gcd(value, ((LongVarInt)val).value));
		}
		return VarIntFactory.convert(val.gcd(this));
	}
	
	/**
	 * Returns the greatest common divisor of iA and iB using standard euclidian
	 * algorithm
	 */
    static long gcd(long iA, long iB) {
        iA = Math.abs(iA);
        iB = Math.abs(iB);
        if (iA < 0 || iB < 0) {
        	//at least one must be Long.MIN_VALUE, which is a even number
            if (0 != ((iA | iB) & 0x1)) {
            	//the other number is not even --> GCD=1
            	return 1;
            }
        	//both are even numbers, divide by 2
            iA = Math.abs(iA >>> 1);
            iB = Math.abs(iB >>> 1);
        }
        if (iA == 0) return iB;
        if (iB == 0) return iA;
        long iMax = Math.max(iA, iB);
        long iMin = Math.min(iA, iB);
        while (iMax != iMin) {
            if (iMax % iMin == 0) return iMin;
            long tmp = iMin;
            iMin = iMax - (iMax / iMin) * iMin;
            iMax = tmp;
        }
        return iMin;
    }
	

	@Override
	public VarIntNumber max(VarInt val) {
		final long other;
		if (val instanceof IntVarInt) {
			other = ((IntVarInt)val).value;
		}
		else if (val instanceof LongVarInt) {
			other = ((LongVarInt)val).value;
		}
		else {
			return VarIntFactory.convert(val.max(this));
		}
		return value >= other ? this : VarIntFactory.convert(val);
	}

	@Override
	public VarIntNumber min(VarInt val) {
		final long other;
		if (val instanceof IntVarInt) {
			other = ((IntVarInt)val).value;
		}
		else if (val instanceof LongVarInt) {
			other = ((LongVarInt)val).value;
		}
		else {
			return VarIntFactory.convert(val.min(this));
		}
		return value <= other ? this : VarIntFactory.convert(val);
	}

	@Override
	public VarIntNumber mod(VarInt m) {
		final long mod;
		if (m instanceof IntVarInt) {
			mod = ((IntVarInt)m).value;
		}
		else if (m instanceof LongVarInt) {
			mod	= ((LongVarInt)m).value;
		}
		else {
			return VarIntFactory.create(toBigInteger().mod(m.toBigInteger()));
		}
		final long rem	= value % mod;
		return VarIntFactory.create(rem < 0 ? rem + mod : rem);
	}

	@Override
	public VarIntNumber multiply(VarInt val) {
		final long other;
		if (val instanceof IntVarInt) {
			other = ((IntVarInt)val).value;
		}
		else if (val instanceof LongVarInt) {
			other = ((LongVarInt)val).value;
		}
		else {
			return VarIntFactory.create(toBigInteger().multiply(val.toBigInteger()));
		}
		long result = value * other;
		
		//quick overflow check: sign(A) ^ sign(B) != sign(result)
		boolean overflow;
		overflow = ((value ^ other ^ result) < 0);
		overflow|= other != 0 && ((result / other) != value); 
		if (overflow) {
			//fallback to big integer
			return VarIntFactory.create(toBigInteger().multiply(val.toBigInteger()));
		}
		return VarIntFactory.create(result);
	}

	@Override
	public VarIntNumber negate() {
		if (value == Long.MIN_VALUE) {
			return VarIntFactory.create(BigInteger.valueOf(value).negate());
		}
		return VarIntFactory.create(-value);
	}

	@Override
	public VarIntNumber pow(int exponent) {
		return VarIntFactory.create(toBigInteger().pow(exponent));//TODO not very efficient
	}

	@Override
	public VarIntNumber remainder(VarInt val) {
		if (val instanceof LongVarInt) {
			return valueOf(value % ((LongVarInt)val).value);
		}
		if (val instanceof LongVarInt) {
			return VarIntFactory.create(value % ((LongVarInt)val).value);
		}
		return VarIntFactory.create(toBigInteger().remainder(val.toBigInteger()));  
	}

	@Override
	public VarIntNumber shiftLeft(int n) {
		if (n < 0) {
			return valueOf(value >> -n);
		}
		int space = Long.numberOfLeadingZeros(value);
		if (n < space) {
			return valueOf(value << n);
		}
		return VarIntFactory.create(toBigInteger().shiftLeft(n));
	}

	@Override
	public VarIntNumber shiftRight(int n) {
		if (n < 0) {
			return shiftLeft(-n);
		}
		return LongVarInt.valueOf(value >> n);
	}

	public int signum() {
		return value == 0 ? 0 : value < 0 ? -1 : 1;
	}

	@Override
	public VarIntNumber subtract(VarInt val) {
		final long other;
		if (val instanceof IntVarInt) {
			other = ((IntVarInt)val).value;
		}
		else if (val instanceof IntVarInt) {
			other = ((IntVarInt)val).value;
		}
		else {
			return VarIntFactory.create(toBigInteger().subtract(val.toBigInteger()));
		}		
		long result = value - other;
		if ((value ^ other) >= 0 && (result ^ other) < 0) {
			//overflow (value and other have same sign, but result has another one)
			//fallback to big integer
			return VarIntFactory.create(toBigInteger().subtract(val.toBigInteger()));
		}
		return VarIntFactory.create(result);
	}

	public BigInteger toBigInteger() {
		return BigInteger.valueOf(value);
	}

	public byte[] toByteArray() {
		return VarIntFactory.toByteArray(value);
	}
	
	public void writeTo(OutputStream out) throws IOException {
		VarIntFactory.writeLong(out, value);
	}

	public String toString(int radix) {
		return Long.toString(value, radix);
	}
	
	@Override
	public String toString() {
		return String.valueOf(value);
	}

	public int compareTo(VarInt o) {
		if (o instanceof LongVarInt) {
			final long cmp = value - ((LongVarInt)o).value;
			return cmp == 0 ? 0 : cmp < 0 ? -1 : 1; 
		}
		return -o.compareTo(this);
	}

	@Override
	public int hashCode() {
		return (int)((value >>> 32) ^ (0x00000000ffffffffL & value));
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (obj instanceof IntVarInt) {
			return value == ((IntVarInt)obj).value;
		}
		if (obj instanceof LongVarInt) {
			return value == ((LongVarInt)obj).value;
		}
		if (obj instanceof VarInt) {
			return ((VarInt)obj).equals(this);
		}
		return false;
	}
}
