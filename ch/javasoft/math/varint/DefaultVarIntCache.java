package ch.javasoft.math.varint;

import java.math.BigInteger;

/**
 * The <code>DefaultVarIntCache</code> uses a fixed array for 
 * {@link IntVarInt} instances for small integers. The cache
 * is initialized on construction, hence this is a static
 * cache that does not change after instantiation.
 */
public class DefaultVarIntCache implements VarIntCache {
	
	/**
	 * System property to specify the cache size. If missing, 
	 * {@link #DEFAULT_CACHE_SIZE} is used.
	 */
	public static final String SYSTEM_PROPERTY_SIZE = DefaultVarIntCache.class.getName() + ".size";
	/**
	 * System property to specify the cache offset. If missing, half of the 
	 * cache size is used.
	 */
	public static final String SYSTEM_PROPERTY_OFFSET = DefaultVarIntCache.class.getName() + ".offset";

	private static final int DEFAULT_CACHE_SIZE = 256; 

	private final IntVarInt[]	cache;
	private final int 			offset;
	
	public DefaultVarIntCache() {
		final int cacheSize = getFromSystemProperties(SYSTEM_PROPERTY_SIZE, DEFAULT_CACHE_SIZE);
		cache 	= new IntVarInt[cacheSize];
		offset	= getFromSystemProperties(SYSTEM_PROPERTY_OFFSET, cacheSize >> 1);
		initCache();
	}
	public DefaultVarIntCache(int cacheSize, int cacheOffset) {
		cache 	= new IntVarInt[cacheSize];
		offset	= cacheOffset;
		initCache();
	}
	
	private static int getFromSystemProperties(String property, int defaultValue) {
		final Object val = System.getProperty(property);
		if (val == null) return defaultValue;
		try {
			return Integer.parseInt(val.toString());
		}
		catch (Exception e) {
			System.err.println("Cannot read int value from system properties: " + property + "='" + val + "', e=" + e);
			e.printStackTrace();
			return defaultValue;
		}
	}
	
	private void initCache() {
		for (int i = 0; i < cache.length; i++) {
			cache[i] = new IntVarInt(i - offset);
		}		
	}
	
	public IntVarInt cacheGet(int value) {
		final int index = value + offset;
		if (index >= 0 && index < cache.length) {
			return cache[index];
		}
		return new IntVarInt(value);
	}

	public LongVarInt cacheGet(long value) {
		return new LongVarInt(value);
	}

	public BigIntegerVarInt cacheGet(BigInteger value) {
		return new BigIntegerVarInt(value);
	}

	public void reset() {
		//nothing to reset
	}

	/**
	 * Creates an instance of VarIntCache, considering the 
	 * {@link VarIntCache#SYSTEM_PROPERTY} setting for custom cache 
	 * implementations. If no configuration is found, or if the initialization 
	 * fails, a new {@link DefaultVarIntCache} instance is created and returned.
	 */
	static VarIntCache createCache() {
		final String className = System.getProperty(VarIntCache.SYSTEM_PROPERTY);
		if (className != null) {
			try {
				return (VarIntCache)Class.forName(className).newInstance();
			}
			catch (Exception e) {
				System.err.println("cannot initialize " + VarIntCache.class.getSimpleName() + 
					" with class " + className + " configured in system property '" + 
					VarIntCache.SYSTEM_PROPERTY + "', e=" + e);
			}
		}
		return new DefaultVarIntCache();
	}
	
}
