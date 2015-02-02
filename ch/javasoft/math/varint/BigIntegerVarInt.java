package ch.javasoft.math.varint;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;

/**
 * This class backs all methods with an internal
 * {@link BigInteger} number
 */
public class BigIntegerVarInt extends VarIntNumber {

	private static final long serialVersionUID = 822046479881991852L;

	private final BigInteger value;

    /**
	 * Constructor with big integer
     * <p>
     * NOTE: It is recommended to use the factory method
     * {@link VarIntFactory#create(BigInteger)} instead, as this
     * method allows the dynamic allocation of optimized instances 
     * depending on the true integer value size.
	 */
	protected BigIntegerVarInt(BigInteger value) {
		this.value = value;
	}
	
    /**
     * Returns a BigIntegerVarInt whose value is equal to that of the
     * specified <code>int</code>.  This "static factory method" is
     * provided in preference to a (<code>BigInteger</code>) constructor
     * because it allows for reuse of frequently used BigIntegerVarInts.
     * <p>
     * NOTE: It is recommended to use the factory method
     * {@link VarIntFactory#create(BigInteger)} instead, as this
     * method allows the dynamic allocation of optimized instances 
     * depending on the true integer value size.
     *
     * @param  value value of the IntVarInt to return.
     * @return a IntVarInt with the specified value.
     */
	public static BigIntegerVarInt valueOf(BigInteger value) {
		return VarIntFactory.CACHE.cacheGet(value);
	}
	
	@Override
	public VarIntNumber compact() {
		final int bitlen = value.bitLength();
		if (bitlen <= 31) {
			return IntVarInt.valueOf(value.intValue());
		}
		if (bitlen <= 63) {
			return LongVarInt.valueOf(value.longValue());
		}
		return this;
	}
	
	@Override
	public boolean isZero() {
		return value.signum() == 0;
	}
	
	public boolean isOne() {
		return BigInteger.ONE.equals(value);
	}
	
	@Override
	public boolean isNegative() {
		return value.signum() < 0;
	}
	
	@Override
	public boolean isPositive() {
		return value.signum() > 0;
	}
	
	@Override
	public boolean isNonNegative() {
		return value.signum() >= 0;
	}
	
	@Override
	public boolean isNonPositive() {
		return value.signum() <= 0;
	}
	
	public boolean isEven() {
		return !value.testBit(0);
	}
	
	public boolean testBit(int n) {
		return value.testBit(n);
	}
	
	public int bitCount() {
		return value.bitCount();
	}
	
	public int bitLength() {
		return value.bitLength();
	}
	
	@Override
	public double doubleValue() {
		return value.doubleValue();
	}
	
	@Override
	public float floatValue() {
		return value.floatValue();
	}

	@Override
	public long longValue() {
		return value.longValue();
	}
	
	@Override
	public int intValue() {
		return value.intValue();
	}
	
	@Override
	public VarIntNumber abs() {
		return value.signum() < 0 ? valueOf(value.abs()) : this;
	}

	@Override
	public VarIntNumber add(VarInt val) {
		return BigIntegerVarInt.valueOf(value.add(val.toBigInteger()));
	}

	@Override
	public VarIntNumber divide(VarInt val) {
		return BigIntegerVarInt.valueOf(value.divide(val.toBigInteger()));
	}

	@Override
	public VarIntNumber gcd(VarInt val) {
		return BigIntegerVarInt.valueOf(value.gcd(val.toBigInteger()));
	}

	@Override
	public VarIntNumber max(VarInt val) {
		return BigIntegerVarInt.valueOf(value.max(val.toBigInteger()));
	}

	@Override
	public VarIntNumber min(VarInt val) {
		return BigIntegerVarInt.valueOf(value.min(val.toBigInteger()));
	}

	@Override
	public VarIntNumber mod(VarInt m) {
		return BigIntegerVarInt.valueOf(value.mod(m.toBigInteger()));
	}

	@Override
	public VarIntNumber multiply(VarInt val) {
		return BigIntegerVarInt.valueOf(value.multiply(val.toBigInteger()));
	}

	@Override
	public VarIntNumber negate() {
		return BigIntegerVarInt.valueOf(value.negate());
	}

	@Override
	public VarIntNumber pow(int exponent) {
		return BigIntegerVarInt.valueOf(value.pow(exponent));
	}

	@Override
	public VarIntNumber remainder(VarInt val) {
		return BigIntegerVarInt.valueOf(value.remainder(val.toBigInteger()));
	}

	@Override
	public VarIntNumber shiftLeft(int n) {
		return BigIntegerVarInt.valueOf(value.shiftLeft(n));
	}

	@Override
	public VarIntNumber shiftRight(int n) {
		return BigIntegerVarInt.valueOf(value.shiftRight(n));
	}

	public int signum() {
		return value.signum();
	}

	@Override
	public VarIntNumber subtract(VarInt val) {
		return BigIntegerVarInt.valueOf(value.subtract(val.toBigInteger()));
	}

	public BigInteger toBigInteger() {
		return value;
	}

	public byte[] toByteArray() {
		return value.toByteArray();
	}
	
	public void writeTo(OutputStream out) throws IOException {
		VarIntFactory.writeBytes(out, value.toByteArray());
	}

	public String toString(int radix) {
		return value.toString(radix);
	}

	public int compareTo(VarInt o) {
		return value.compareTo(o.toBigInteger());
	}
	
	@Override
	public int hashCode() {
		return value.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (obj instanceof VarInt) {
			return value.equals(((VarInt)obj).toBigInteger());
		}
		return false;
	}

}
