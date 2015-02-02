package ch.javasoft.math.varint;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;

/**
 * This class backs all methods with an internal {@code int} 
 * value.
 */
public class IntVarInt extends VarIntNumber {

	private static final long serialVersionUID = -3452554456179800083L;

	protected final int value;
	
    /**
	 * Constructor with int value
     * <p>
     * NOTE: It is recommended to use the factory method
     * {@link VarIntFactory#create(BigInteger)} instead, as this
     * method allows the dynamic allocation of optimized instances 
     * depending on the true integer value size.
	 */
	protected IntVarInt(int value) {
		this.value = value;
	}
	
    /**
     * Returns a IntVarInt whose value is equal to that of the
     * specified <code>int</code>.  This "static factory method" is
     * provided in preference to a (<code>long</code>) constructor
     * because it allows for reuse of frequently used IntVarInts.
     * <p>
     * NOTE: It is recommended to use the factory method
     * {@link VarIntFactory#create(BigInteger)} instead, as this
     * method allows the dynamic allocation of optimized instances 
     * depending on the true integer value size.
     *
     * @param  value value of the IntVarInt to return.
     * @return a IntVarInt with the specified value.
     */
	protected static IntVarInt valueOf(int value) {
		return VarIntFactory.CACHE.cacheGet(value);
	}
	
	@Override
	public VarIntNumber compact() {
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

		return 0 != (value & (1 << n));
	}
	
	public int bitCount() {
		return Integer.bitCount(value >= 0 ? value : ~value);
	}
	
	public int bitLength() {
		return 32 - Integer.numberOfLeadingZeros(value >= 0 ? value : ~value);
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
		return value;
	}
	
	@Override
	public VarIntNumber abs() {
		return value < 0 ? valueOf(-value) : this;
	}

	@Override
	public VarIntNumber add(VarInt val) {
		if (val instanceof IntVarInt) {
			final long other = ((IntVarInt)val).value;
			final long sum = value + other;
			return VarIntFactory.create(sum);
		}
		return VarIntFactory.convert(val.add(this));
	}

	@Override
	public VarIntNumber divide(VarInt val) {
		if (val instanceof IntVarInt) {
			final int div = ((IntVarInt)val).value;
			return div == -1 ? negate()/*might overflow*/ : valueOf(value / div);
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
		return VarIntFactory.convert(val.gcd(this));
	}
	
	/**
	 * Returns the greatest common divisor of iA and iB using standard euclidian
	 * algorithm
	 */
    static int gcd(int iA, int iB) {
        iA = Math.abs(iA);
        iB = Math.abs(iB);
        if (iA < 0 || iB < 0) {
        	//at least one must be Integer.MIN_VALUE, which is a even number
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
        int iMax = Math.max(iA, iB);
        int iMin = Math.min(iA, iB);
        while (iMax != iMin) {
            if (iMax % iMin == 0) return iMin;
            int tmp = iMin;
            iMin = iMax - (iMax / iMin) * iMin;
            iMax = tmp;
        }
        return iMin;
    }
	

	@Override
	public VarIntNumber max(VarInt val) {
		if (val instanceof IntVarInt) {
			final int other = ((IntVarInt)val).value;
			return value >= other ? this : VarIntFactory.convert(val);
		}
		return VarIntFactory.convert(val.max(this));
	}

	@Override
	public VarIntNumber min(VarInt val) {
		if (val instanceof IntVarInt) {
			final int other = ((IntVarInt)val).value;
			return value <= other ? this : VarIntFactory.convert(val);
		}
		return VarIntFactory.convert(val.min(this));
	}

	@Override
	public VarIntNumber mod(VarInt m) {
		if (m instanceof IntVarInt) {
			final int mod = ((IntVarInt)m).value;
			final int rem = value % mod;
			return valueOf(rem < 0 ? rem + mod : rem);
		}
		if (m instanceof LongVarInt) {
			final long mod	= ((IntVarInt)m).value;
			final long rem	= value % mod;
			return VarIntFactory.create(rem < 0 ? rem + mod : rem);
		}
		return VarIntFactory.create(toBigInteger().mod(m.toBigInteger()));  
	}

	@Override
	public VarIntNumber multiply(VarInt val) {
		if (val instanceof IntVarInt) {
			final long other = ((IntVarInt)val).value;
			long sum = value * other;
			return VarIntFactory.create(sum);
		}
		return VarIntFactory.convert(val.multiply(this));
	}

	@Override
	public VarIntNumber negate() {
		return VarIntFactory.create(-((long)value));
	}

	@Override
	public VarIntNumber pow(int exponent) {
		return VarIntFactory.create(toBigInteger().pow(exponent));//TODO not very efficient
	}

	@Override
	public VarIntNumber remainder(VarInt val) {
		if (val instanceof IntVarInt) {
			return valueOf(value % ((IntVarInt)val).value);
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
		int space = Integer.numberOfLeadingZeros(value);
		if (n < space) {
			return valueOf(value << n);
		}
		space += 32;//for long
		if (n < space) {
			return VarIntFactory.create(((long)value) << n);
		}
		return VarIntFactory.create(toBigInteger().shiftLeft(n));
	}

	@Override
	public VarIntNumber shiftRight(int n) {
		if (n < 0) {
			return shiftLeft(-n);
		}
		return IntVarInt.valueOf(value >> n);
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
		VarIntFactory.writeInt(out, value);
	}

	public String toString(int radix) {
		return Integer.toString(value, radix);
	}
	
	@Override
	public String toString() {
		return String.valueOf(value);
	}

	public int compareTo(VarInt o) {
		if (o instanceof IntVarInt) {
			return value - ((IntVarInt)o).value;
		}
		return -o.compareTo(this);
	}
	
	@Override
	public int hashCode() {
		return value;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (obj instanceof IntVarInt) {
			return value == ((IntVarInt)obj).value;
		}
		if (obj instanceof VarInt) {
			return ((VarInt)obj).equals(this);
		}
		return false;
	}

}
