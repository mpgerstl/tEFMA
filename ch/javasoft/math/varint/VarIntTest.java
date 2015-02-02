package ch.javasoft.math.varint;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.TestCase;
import ch.javasoft.math.varint.VarInt;
import ch.javasoft.math.varint.VarIntFactory;

public class VarIntTest extends TestCase {
	
	private final Random rnd = new Random();
	
	private final boolean trace	= false;
	private final int n_mixed 	= 100000; 
	private final int n_compare = 100000; 
	private final int maxbytelen = 50; 
	
	public void testCreate() {
		outTyped(VarIntFactory.create(0));
		outTyped(VarIntFactory.create(-1));
		outTyped(VarIntFactory.create(1));
		outTyped(VarIntFactory.create(10));
		outTyped(VarIntFactory.create(-10));
		outTyped(VarIntFactory.create(77));
		outTyped(VarIntFactory.create(-77));
		outTyped(VarIntFactory.create(Integer.MAX_VALUE));
		outTyped(VarIntFactory.create(Integer.MIN_VALUE));
		outTyped(VarIntFactory.create(Integer.MIN_VALUE).negate());
		outTyped(VarIntFactory.create(Long.MAX_VALUE));
		outTyped(VarIntFactory.create(Long.MIN_VALUE));
		outTyped(VarIntFactory.create(Long.MIN_VALUE).negate());
	}
	
	public void testBitLength() {
		doBitLength(0);
		doBitLength(1);
		doBitLength(2);
		doBitLength(10);
		doBitLength(77);
		doBitLength(Integer.MAX_VALUE);
		doBitLength(Long.MAX_VALUE);
		doBitLength(-1);
		doBitLength(-2);
		doBitLength(-10);
		doBitLength(-77);
		doBitLength(Integer.MIN_VALUE);
		doBitLength(Long.MIN_VALUE);
	}
	private void doBitLength(long value) {
		final VarInt val = VarIntFactory.create(value);
		assertEquals(val.toBigInteger().bitLength(), val.bitLength());
	}
	public void testBitCount() {
		testBitCount(0);
		testBitCount(1);
		testBitCount(2);
		testBitCount(10);
		testBitCount(77);
		testBitCount(Integer.MAX_VALUE);
		testBitCount(Long.MAX_VALUE);
		testBitCount(-1);
		testBitCount(-2);
		testBitCount(-10);
		testBitCount(-77);
		testBitCount(Integer.MIN_VALUE);
		testBitCount(Long.MIN_VALUE);
	}
	private void testBitCount(long value) {
		final VarInt val = VarIntFactory.create(value);
		assertEquals(val.toBigInteger().bitCount(), val.bitCount());
	}

	public void testToByteArray() throws IOException {
		doToByteArray(0);
		doToByteArray(1);
		doToByteArray(-1);
		doToByteArray(2);
		doToByteArray(-2);
		doToByteArray(127);
		doToByteArray(128);
		doToByteArray(-127);
		doToByteArray(-128);
		doToByteArray(-129);
		
		doToByteArray(Integer.MAX_VALUE);
		doToByteArray(Integer.MAX_VALUE+1L);
		doToByteArray(Integer.MIN_VALUE);
		doToByteArray(Integer.MIN_VALUE-1L);
		doToByteArray(Long.MAX_VALUE);
		doToByteArray(Long.MAX_VALUE+1);
		doToByteArray(Long.MIN_VALUE);
		doToByteArray(Long.MIN_VALUE-1);
	}
	public void doToByteArray(long value) throws IOException {
		final VarInt val1 = VarIntFactory.create(value);
		final byte[] bytes = val1.toByteArray();
		if (trace) System.out.println(value + " = " + Arrays.toString(bytes));
		final VarInt val2 = VarIntFactory.create(bytes);
		assertEquals(val1, val2);

		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		val1.writeTo(out);
		final byte[] buf = out.toByteArray();
		if (trace) System.out.println(value + " = " + Arrays.toString(buf));
		final VarInt val3 = VarIntFactory.readFrom(new ByteArrayInputStream(buf));
		assertEquals(val1, val3);
		
		final VarInt val4 = VarIntFactory.readFrom(buf, null);
		assertEquals(val1, val4);
	}

	public void testIO() throws IOException {
		doIO(-1);
	}
	public void testIOInt() throws IOException {
		doIO(0);
	}
	public void testIOLong() throws IOException {
		doIO(1);
	}
	public void testIOBigInteger() throws IOException {
		doIO(2);
	}
	public void doIO(int type) throws IOException {
		final VarInt[] vals = new VarInt[n_mixed];
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		for (int i = 0; i < n_mixed; i++) {
			final VarInt val = randomValue(type);
			vals[i] = val;
			val.writeTo(out);
		}
		final byte[] buf = out.toByteArray();
		final ByteArrayInputStream in = new ByteArrayInputStream(buf);
		if (trace) System.out.println("buffer  = " + Arrays.toString(buf));
		final AtomicInteger pos = new AtomicInteger();
		for (int i = 0; i < n_mixed; i++) {
			final byte[] bytes = vals[i].toByteArray();
			if (trace) System.out.println(vals[i] + " = " + Arrays.toString(bytes));
			final VarInt read1 = VarIntFactory.create(bytes);
			final VarInt read2 = VarIntFactory.readFrom(in);
			final VarInt read3 = VarIntFactory.readFrom(buf, pos);
			assertEquals("val[" + i + "]", vals[i], read1);
			assertEquals("val[" + i + "]", vals[i], read2);
			assertEquals("val[" + i + "]", vals[i], read3);
		}
	}
	/////////////////////////////////
	// add
	/////////////////////////////////
	public void testAdd() {
		doAddN(n_mixed, -1);
	}
	public void testAddOverflow() {
		doAdd(Integer.MAX_VALUE, 1);
		doAdd(Integer.MIN_VALUE, -1);
		doAdd(Long.MAX_VALUE, 1);
		doAdd(Long.MIN_VALUE, -1);
	}
	public void testAddInt() {
		doAddN(n_compare, 0);
	}
	public void testAddLong() {
		doAddN(n_compare, 1);
	}
	public void testAddBigIngeger() {
		doAddN(n_compare, 2);
	}
	private void doAddN(int n, int type) {
		for (int i = 0; i < n; i++) {
			doAdd(randomValue(type), randomValue(type));
		}
	}
	private void doAdd(long val1, long val2) {
		doAdd(VarIntFactory.create(val1), VarIntFactory.create(val2));
	}
	private void doAdd(VarInt val1, VarInt val2) {
		final BigInteger exp = val1.toBigInteger().add(val2.toBigInteger());
		final BigInteger act = val1.add(val2).toBigInteger();
		assertEquals(val1 + " + " + val2, exp, act);
	}	

	/////////////////////////////////
	// subtract
	/////////////////////////////////
	public void testSub() {
		doSubN(n_mixed, 0);
	}
	public void testSubOverflow() {
		doSub(0, Integer.MIN_VALUE);
		doSub(Integer.MIN_VALUE, 1);
		doSub(Integer.MAX_VALUE, -1);
		doSub(0, Long.MIN_VALUE);
		doSub(Long.MIN_VALUE, 1);
		doSub(Long.MAX_VALUE, -1);
	}
	public void testSubInt() {
		doSubN(n_compare, 0);
	}
	public void testSubLong() {
		doSubN(n_compare, 1);
	}
	public void testSubBigIngeger() {
		doSubN(n_compare, 2);
	}
	private void doSubN(int n, int type) {
		for (int i = 0; i < n; i++) {
			doSub(randomValue(type), randomValue(type));
		}
	}
	private void doSub(long val1, long val2) {
		doAdd(VarIntFactory.create(val1), VarIntFactory.create(val2));
	}
	private void doSub(VarInt val1, VarInt val2) {
		final BigInteger exp = val1.toBigInteger().subtract(val2.toBigInteger());
		final BigInteger act = val1.subtract(val2).toBigInteger();
		assertEquals(val1 + " - " + val2, exp, act);
	}	

	/////////////////////////////////
	// multiply
	/////////////////////////////////
	public void testMul() {
		doMulN(n_mixed, -1);
	}
	public void testMulOverflow() {
		doMul(Integer.MAX_VALUE, Integer.MAX_VALUE);
		doMul(Integer.MIN_VALUE, Integer.MIN_VALUE);
		doMul(Integer.MAX_VALUE, Integer.MIN_VALUE);
		doMul(Long.MAX_VALUE, Long.MAX_VALUE);
		doMul(Long.MIN_VALUE, Long.MIN_VALUE);
		doMul(Long.MAX_VALUE, Long.MIN_VALUE);
	}
	public void testMulInt() {
		doMulN(n_compare, 0);
	}
	public void testMulLong() {
		doMulN(n_compare, 1);
	}
	public void testMulBigIngeger() {
		doMulN(n_compare, 2);
	}
	private void doMulN(int n, int type) {
		for (int i = 0; i < n; i++) {
			doMul(randomValue(type), randomValue(type));
		}
	}
	private void doMul(long val1, long val2) {
		doMul(VarIntFactory.create(val1), VarIntFactory.create(val2));
	}
	private void doMul(VarInt val1, VarInt val2) {
		final BigInteger exp = val1.toBigInteger().multiply(val2.toBigInteger());
		final BigInteger act = val1.multiply(val2).toBigInteger();
		assertEquals(val1 + " * " + val2, exp, act);
	}	
	/////////////////////////////////
	// divide
	/////////////////////////////////
	public void testDiv() {
		doDivN(n_mixed, -1);
	}
	public void testDivOverflow() {
		doDiv(Integer.MIN_VALUE, -1);
		doDiv(Long.MIN_VALUE, -1);
	}
	public void testDivByZero() {
		doDiv(rnd.nextInt(), 0);
		doDiv(rnd.nextLong(), 0L);
		doDiv(randomValue(-1), VarIntFactory.create(BigInteger.ZERO));
	}
	public void testDivRound() {
		//use exactly the samples in RoundingMode

		final VarInt[] nums = new VarInt[] {
			VarIntFactory.create(55),
			VarIntFactory.create(25),
			VarIntFactory.create(16),
			VarIntFactory.create(11),
			VarIntFactory.create(10),
			VarIntFactory.create(-10),
			VarIntFactory.create(-11),
			VarIntFactory.create(-16),
			VarIntFactory.create(-25),
			VarIntFactory.create(-55)
		};
		
		//NOTE: must be in same order as RoundintMode constants!!!
		final VarInt[][] results = new VarInt[][] {
			/**
			 * Rounding mode to round away from zero.  Always increments the
			 * digit prior to a non-zero discarded fraction.  Note that this
			 * rounding mode never decreases the magnitude of the calculated
			 * value.
			 *
			 *<p>Example:
			 *<table border>
			 *<tr valign=top><th>Input Number</th>
			 *    <th>Input rounded to one digit<br> with <tt>UP</tt> rounding
			 *<tr align=right><td>5.5</td>	<td>6</td>
			 *<tr align=right><td>2.5</td>	<td>3</td>
			 *<tr align=right><td>1.6</td>	<td>2</td>
			 *<tr align=right><td>1.1</td>	<td>2</td>
			 *<tr align=right><td>1.0</td>	<td>1</td>
			 *<tr align=right><td>-1.0</td>	<td>-1</td>
			 *<tr align=right><td>-1.1</td>	<td>-2</td>
			 *<tr align=right><td>-1.6</td>	<td>-2</td>
			 *<tr align=right><td>-2.5</td>	<td>-3</td>
			 *<tr align=right><td>-5.5</td>	<td>-6</td>
			 *</table>
		    UP(BigDecimal.ROUND_UP),
			 */
			{
				VarIntFactory.create(6),
				VarIntFactory.create(3),
				VarIntFactory.create(2),
				VarIntFactory.create(2),
				VarIntFactory.create(1),
				VarIntFactory.create(-1),
				VarIntFactory.create(-2),
				VarIntFactory.create(-2),
				VarIntFactory.create(-3),
				VarIntFactory.create(-6)
			},
			/**
			 * Rounding mode to round towards zero.  Never increments the digit
			 * prior to a discarded fraction (i.e., truncates).  Note that this
			 * rounding mode never increases the magnitude of the calculated value.
			 *
			 *<p>Example:
			 *<table border>
			 *<tr valign=top><th>Input Number</th>
			 *    <th>Input rounded to one digit<br> with <tt>DOWN</tt> rounding
			 *<tr align=right><td>5.5</td>	<td>5</td>
			 *<tr align=right><td>2.5</td>	<td>2</td>
			 *<tr align=right><td>1.6</td>	<td>1</td>
			 *<tr align=right><td>1.1</td>	<td>1</td>
			 *<tr align=right><td>1.0</td>	<td>1</td>
			 *<tr align=right><td>-1.0</td>	<td>-1</td>
			 *<tr align=right><td>-1.1</td>	<td>-1</td>
			 *<tr align=right><td>-1.6</td>	<td>-1</td>
			 *<tr align=right><td>-2.5</td>	<td>-2</td>
			 *<tr align=right><td>-5.5</td>	<td>-5</td>
			 *</table>
		    DOWN(BigDecimal.ROUND_DOWN),
			 */
			{
				VarIntFactory.create(5),
				VarIntFactory.create(2),
				VarIntFactory.create(1),
				VarIntFactory.create(1),
				VarIntFactory.create(1),
				VarIntFactory.create(-1),
				VarIntFactory.create(-1),
				VarIntFactory.create(-1),
				VarIntFactory.create(-2),
				VarIntFactory.create(-5)
			},
			/**
			 * Rounding mode to round towards positive infinity.  If the
			 * result is positive, behaves as for <tt>RoundingMode.UP</tt>;
			 * if negative, behaves as for <tt>RoundingMode.DOWN</tt>.  Note
			 * that this rounding mode never decreases the calculated value.
			 *
			 *<p>Example:
			 *<table border>
			 *<tr valign=top><th>Input Number</th>
			 *    <th>Input rounded to one digit<br> with <tt>CEILING</tt> rounding
			 *<tr align=right><td>5.5</td>	<td>6</td>
			 *<tr align=right><td>2.5</td>	<td>3</td>
			 *<tr align=right><td>1.6</td>	<td>2</td>
			 *<tr align=right><td>1.1</td>	<td>2</td>
			 *<tr align=right><td>1.0</td>	<td>1</td>
			 *<tr align=right><td>-1.0</td>	<td>-1</td>
			 *<tr align=right><td>-1.1</td>	<td>-1</td>
			 *<tr align=right><td>-1.6</td>	<td>-1</td>
			 *<tr align=right><td>-2.5</td>	<td>-2</td>
			 *<tr align=right><td>-5.5</td>	<td>-5</td>
			 *</table>
		    CEILING(BigDecimal.ROUND_CEILING),
			 */
			{
				VarIntFactory.create(6),
				VarIntFactory.create(3),
				VarIntFactory.create(2),
				VarIntFactory.create(2),
				VarIntFactory.create(1),
				VarIntFactory.create(-1),
				VarIntFactory.create(-1),
				VarIntFactory.create(-1),
				VarIntFactory.create(-2),
				VarIntFactory.create(-5)
			},
			/**
			 * Rounding mode to round towards negative infinity.  If the
			 * result is positive, behave as for <tt>RoundingMode.DOWN</tt>;
			 * if negative, behave as for <tt>RoundingMode.UP</tt>.  Note that
			 * this rounding mode never increases the calculated value.
			 *
			 *<p>Example:
			 *<table border>
			 *<tr valign=top><th>Input Number</th>
			 *    <th>Input rounded to one digit<br> with <tt>FLOOR</tt> rounding
			 *<tr align=right><td>5.5</td>	<td>5</td>
			 *<tr align=right><td>2.5</td>	<td>2</td>
			 *<tr align=right><td>1.6</td>	<td>1</td>
			 *<tr align=right><td>1.1</td>	<td>1</td>
			 *<tr align=right><td>1.0</td>	<td>1</td>
			 *<tr align=right><td>-1.0</td>	<td>-1</td>
			 *<tr align=right><td>-1.1</td>	<td>-2</td>
			 *<tr align=right><td>-1.6</td>	<td>-2</td>
			 *<tr align=right><td>-2.5</td>	<td>-3</td>
			 *<tr align=right><td>-5.5</td>	<td>-6</td>
			 *</table>
		    FLOOR(BigDecimal.ROUND_FLOOR),
			 */
			{
				VarIntFactory.create(5),
				VarIntFactory.create(2),
				VarIntFactory.create(1),
				VarIntFactory.create(1),
				VarIntFactory.create(1),
				VarIntFactory.create(-1),
				VarIntFactory.create(-2),
				VarIntFactory.create(-2),
				VarIntFactory.create(-3),
				VarIntFactory.create(-6)
			},
			/**
			 * Rounding mode to round towards &quot;nearest neighbor&quot;
			 * unless both neighbors are equidistant, in which case round up.
			 * Behaves as for <tt>RoundingMode.UP</tt> if the discarded
			 * fraction is &gt;= 0.5; otherwise, behaves as for
			 * <tt>RoundingMode.DOWN</tt>.  Note that this is the rounding
			 * mode commonly taught at school.
			 *
			 *<p>Example:
			 *<table border>
			 *<tr valign=top><th>Input Number</th>
			 *    <th>Input rounded to one digit<br> with <tt>HALF_UP</tt> rounding
			 *<tr align=right><td>5.5</td>	<td>6</td>
			 *<tr align=right><td>2.5</td>	<td>3</td>
			 *<tr align=right><td>1.6</td>	<td>2</td>
			 *<tr align=right><td>1.1</td>	<td>1</td>
			 *<tr align=right><td>1.0</td>	<td>1</td>
			 *<tr align=right><td>-1.0</td>	<td>-1</td>
			 *<tr align=right><td>-1.1</td>	<td>-1</td>
			 *<tr align=right><td>-1.6</td>	<td>-2</td>
			 *<tr align=right><td>-2.5</td>	<td>-3</td>
			 *<tr align=right><td>-5.5</td>	<td>-6</td>
			 *</table>
		    HALF_UP(BigDecimal.ROUND_HALF_UP),
			 */
			{
				VarIntFactory.create(6),
				VarIntFactory.create(3),
				VarIntFactory.create(2),
				VarIntFactory.create(1),
				VarIntFactory.create(1),
				VarIntFactory.create(-1),
				VarIntFactory.create(-1),
				VarIntFactory.create(-2),
				VarIntFactory.create(-3),
				VarIntFactory.create(-6)
			},
			/**
			 * Rounding mode to round towards &quot;nearest neighbor&quot;
			 * unless both neighbors are equidistant, in which case round
			 * down.  Behaves as for <tt>RoundingMode.UP</tt> if the discarded
			 * fraction is &gt; 0.5; otherwise, behaves as for
			 * <tt>RoundingMode.DOWN</tt>.
			 *
			 *<p>Example:
			 *<table border>
			 *<tr valign=top><th>Input Number</th>
			 *    <th>Input rounded to one digit<br> with <tt>HALF_DOWN</tt> rounding
			 *<tr align=right><td>5.5</td>	<td>5</td>
			 *<tr align=right><td>2.5</td>	<td>2</td>
			 *<tr align=right><td>1.6</td>	<td>2</td>
			 *<tr align=right><td>1.1</td>	<td>1</td>
			 *<tr align=right><td>1.0</td>	<td>1</td>
			 *<tr align=right><td>-1.0</td>	<td>-1</td>
			 *<tr align=right><td>-1.1</td>	<td>-1</td>
			 *<tr align=right><td>-1.6</td>	<td>-2</td>
			 *<tr align=right><td>-2.5</td>	<td>-2</td>
			 *<tr align=right><td>-5.5</td>	<td>-5</td>
			 *</table>
		    HALF_DOWN(BigDecimal.ROUND_HALF_DOWN),
			 */
			{
				VarIntFactory.create(5),
				VarIntFactory.create(2),
				VarIntFactory.create(2),
				VarIntFactory.create(1),
				VarIntFactory.create(1),
				VarIntFactory.create(-1),
				VarIntFactory.create(-1),
				VarIntFactory.create(-2),
				VarIntFactory.create(-2),
				VarIntFactory.create(-5)
			},
			/**
			 * Rounding mode to round towards the &quot;nearest neighbor&quot;
			 * unless both neighbors are equidistant, in which case, round
			 * towards the even neighbor.  Behaves as for
			 * <tt>RoundingMode.HALF_UP</tt> if the digit to the left of the
			 * discarded fraction is odd; behaves as for
			 * <tt>RoundingMode.HALF_DOWN</tt> if it's even.  Note that this
			 * is the rounding mode that statistically minimizes cumulative
			 * error when applied repeatedly over a sequence of calculations.
			 * It is sometimes known as &quot;Banker's rounding,&quot; and is
			 * chiefly used in the USA.  This rounding mode is analogous to
			 * the rounding policy used for <tt>float</tt> and <tt>double</tt>
			 * arithmetic in Java.
			 *
			 *<p>Example:
			 *<table border>
			 *<tr valign=top><th>Input Number</th>
			 *    <th>Input rounded to one digit<br> with <tt>HALF_EVEN</tt> rounding
			 *<tr align=right><td>5.5</td>	<td>6</td>
			 *<tr align=right><td>2.5</td>	<td>2</td>
			 *<tr align=right><td>1.6</td>	<td>2</td>
			 *<tr align=right><td>1.1</td>	<td>1</td>
			 *<tr align=right><td>1.0</td>	<td>1</td>
			 *<tr align=right><td>-1.0</td>	<td>-1</td>
			 *<tr align=right><td>-1.1</td>	<td>-1</td>
			 *<tr align=right><td>-1.6</td>	<td>-2</td>
			 *<tr align=right><td>-2.5</td>	<td>-2</td>
			 *<tr align=right><td>-5.5</td>	<td>-6</td>
			 *</table>
		    HALF_EVEN(BigDecimal.ROUND_HALF_EVEN),
			 */
			{
				VarIntFactory.create(6),
				VarIntFactory.create(2),
				VarIntFactory.create(2),
				VarIntFactory.create(1),
				VarIntFactory.create(1),
				VarIntFactory.create(-1),
				VarIntFactory.create(-1),
				VarIntFactory.create(-2),
				VarIntFactory.create(-2),
				VarIntFactory.create(-6)
			},
			/**
			 * Rounding mode to assert that the requested operation has an exact
			 * result, hence no rounding is necessary.  If this rounding mode is
			 * specified on an operation that yields an inexact result, an
			 * <tt>ArithmeticException</tt> is thrown.
			 *<p>Example:
			 *<table border>
			 *<tr valign=top><th>Input Number</th>
			 *    <th>Input rounded to one digit<br> with <tt>UNNECESSARY</tt> rounding
			 *<tr align=right><td>5.5</td>	<td>throw <tt>ArithmeticException</tt></td>
			 *<tr align=right><td>2.5</td>	<td>throw <tt>ArithmeticException</tt></td>
			 *<tr align=right><td>1.6</td>	<td>throw <tt>ArithmeticException</tt></td>
			 *<tr align=right><td>1.1</td>	<td>throw <tt>ArithmeticException</tt></td>
			 *<tr align=right><td>1.0</td>	<td>1</td>
			 *<tr align=right><td>-1.0</td>	<td>-1</td>
			 *<tr align=right><td>-1.1</td>	<td>throw <tt>ArithmeticException</tt></td>
			 *<tr align=right><td>-1.6</td>	<td>throw <tt>ArithmeticException</tt></td>
			 *<tr align=right><td>-2.5</td>	<td>throw <tt>ArithmeticException</tt></td>
			 *<tr align=right><td>-5.5</td>	<td>throw <tt>ArithmeticException</tt></td>	
			 *</table>
    		UNNECESSARY(BigDecimal.ROUND_UNNECESSARY);
			 */
			{
				null,
				null,
				null,
				null,
				VarIntFactory.create(1),
				VarIntFactory.create(-1),
				null,
				null,
				null,
				null,
			}
		};

		for (int i = 0; i < results.length; i++) {
			final RoundingMode mode = RoundingMode.values()[i];
			for (int j = 0; j < nums.length; j++) {
				VarInt result = null;
				try {
					result = nums[j].divide(VarInt.TEN, mode);
				}
				catch (ArithmeticException e) {
					//ignore
				}
				if (results[i][j] == null) {
					assertNull(nums[j] + "/" + VarInt.TEN + " [" + mode + "]", result);
				}
				else {
					assertEquals(nums[j] + "/" + VarInt.TEN + " [" + mode + "]", results[i][j], result);
				}
			}
		}
	}
	public void testDivInt() {
		doDivN(n_compare, 0);
	}
	public void testDivLong() {
		doDivN(n_compare, 1);
	}
	public void testDivBigIngeger() {
		doDivN(n_compare, 2);
	}
	private void doDivN(int n, int type) {
		for (int i = 0; i < n; i++) {
			doDiv(randomValue(type), randomValue(type));
		}
	}
	private void doDiv(long val1, long val2) {
		doDiv(VarIntFactory.create(val1), VarIntFactory.create(val2));
	}
	private void doDiv(VarInt val1, VarInt val2) {
		if (val2.isZero()) {
			try {
				val1.toBigInteger().divide(val2.toBigInteger());
				fail("expected div by zero exception for " + val1 + " / " + val2);
			}
			catch (ArithmeticException e) {
				//ok, as expected
			}
		}
		else {
			final BigInteger exp = val1.toBigInteger().divide(val2.toBigInteger());
			final BigInteger act = val1.divide(val2).toBigInteger();
			assertEquals(val1 + " / " + val2, exp, act);
		}
	}	

	// helpers
	private VarInt randomValue(int type) {
		if (type < 0) {
			type = rnd.nextInt(3);
		}
		if (type == 2) {
			final byte[] bytes = new byte[1 + rnd.nextInt(maxbytelen-1)];
			for (int i = 0; i < bytes.length; i++) {
				bytes[i] = (byte)rnd.nextInt();
			}
			return VarIntFactory.create(new BigInteger(bytes));
		}
		if (type == 1) {
			return VarIntFactory.create(rnd.nextLong());			
		}
		return VarIntFactory.create(rnd.nextInt());			
	}
	
	private void outTyped(VarInt value) {
		if (trace) {
			System.out.println(value + " (" + value.getClass().getSimpleName() + ")");
		}
	}
}
