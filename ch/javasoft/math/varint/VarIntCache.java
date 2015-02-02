package ch.javasoft.math.varint;

import java.math.BigInteger;

/**
 * Cache for {@link VarInt} instances. Used by {@link VarIntFactory} and 
 * <tt>valueOf(..)</tt> methods of VarInt implementors. A customized
 * cache class can be set using the system property {@link #SYSTEM_PROPERTY}.
 */
public interface VarIntCache {
	/**
	 * Name of the system property to specify a custom VarIntCache implementation
	 * class. The implementation must have a default constructor without arguments.
	 */
	String SYSTEM_PROPERTY = VarIntCache.class.getName() + ".class";
	
	/**
	 * Returns a {@link VarInt} instance, possibly from the cache, and if not, 
	 * it is possibly added to the cache.
	 * 
	 * @param value	the int value to be represented by the VarInt
	 * @return	a new or cached VarInt instance
	 */
	IntVarInt cacheGet(int value);

	/**
	 * Returns a {@link VarInt} instance, possibly from the cache, and if not, 
	 * it is possibly added to the cache.
	 * 
	 * @param value	the long value to be represented by the VarInt
	 * @return	a new or cached VarInt instance
	 */
	LongVarInt cacheGet(long value);

	/**
	 * Returns a {@link VarInt} instance, possibly from the cache, and if not, 
	 * it is possibly added to the cache.
	 * 
	 * @param value	the big integer value to be represented by the VarInt
	 * @return	a new or cached VarInt instance
	 */
	BigIntegerVarInt cacheGet(BigInteger value);
	
	/**
	 * Resets the cache to the initial state, for instance clearing all
	 * cached values.
	 */
	void reset();
}
