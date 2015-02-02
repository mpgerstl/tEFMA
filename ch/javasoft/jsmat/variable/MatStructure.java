/*
 * =============================================================================
 * Simplified BSD License, see http://www.opensource.org/licenses/
 * -----------------------------------------------------------------------------
 * Copyright (c) 2008-2009, Marco Terzer, Zurich, Switzerland
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 * 
 *     * Redistributions of source code must retain the above copyright notice, 
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright 
 *       notice, this list of conditions and the following disclaimer in the 
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Swiss Federal Institute of Technology Zurich 
 *       nor the names of its contributors may be used to endorse or promote 
 *       products derived from this software without specific prior written 
 *       permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 * =============================================================================
 */
package ch.javasoft.jsmat.variable;

import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import ch.javasoft.jsmat.common.MatClass;
import ch.javasoft.jsmat.common.MatType;
import ch.javasoft.jsmat.primitive.MatInt8;

public class MatStructure extends MatAllocated {
	
	private final Map<String, ? extends MatAllocated>[] mFields;
	
	@SuppressWarnings("unchecked")
	public MatStructure(Map<String, ? extends MatAllocated> fields) {
		this(new Map[] {fields}, new int[] {1, 1});
	}

	public MatStructure(Map<String, ? extends MatAllocated>[] fields, int[] dims) {
		super(MatClass.STRUCT, dims);
		mFields = fields;
		checkMapSizesEqual();
		checkValueLength(fields.length);
	}

	private void checkMapSizesEqual() {
		if (mFields.length == 0) return;
		final int unit = mFields[0].size();
        for (int i = 1; i < mFields.length; i++) {
			if (unit != mFields[i].size()) {
				throw new IllegalArgumentException("not all field maps have same size");
			}
		}
	}

	@Override
	public int getRawDataSize() {
		return getRawDataSize(mFields);
	}
	
	static int getRawDataSize(Map<String, ? extends MatVariable>[] fields) {
		int size = 0;
        size += 4;//itag
        size += 4;//max field name length
        size += new MatInt8(getFieldNamesAsByteArray(fields)).getSize();
        for (int i = 0; i < fields.length; i++) {
			for (String name : fields[i].keySet()) {
				MatVariable mx = fields[i].get(name);
				size += mx.getSize(name);//or real field name?
			}
		}
        return size;
	}

	@Override
	public void writeRawData(DataOutput out) throws IOException {
		writeStructStart(out, mFields);
        for (int i = 0; i < mFields.length; i++) {
			for (String name : mFields[i].keySet()) {
				MatAllocated mx = mFields[i].get(name);
				mx.write(name, out);
			}
		}
	}
	
	static void writeStructStart(DataOutput out, Map<String, ? extends MatVariable>[] fields) throws IOException {
        int itag = 4 << 16 | MatType.INT32.type & 0xffff;
        out.writeInt(itag);
        out.writeInt(getMaxFieldNameLength(fields));
        new MatInt8(getFieldNamesAsByteArray(fields)).write(out);
	}
	

    /**
     * Dumps field names to byte array. Field names are written as Zero End Strings
     */
    private static byte[] getFieldNamesAsByteArray(Map<String, ? extends MatVariable>[] fields) 
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        
        char[] buffer = new char[getMaxFieldNameLength(fields)];
        
        try {
            for (int i = 0; i < fields.length; i++) {
    			for (String name : fields[i].keySet()) {
                    Arrays.fill(buffer, (char)0);
                    System.arraycopy(name.toCharArray(), 0, buffer, 0, name.length());
                    dos.writeBytes(new String(buffer));
    			}
    		}
        }
        catch  (IOException ex) {
           throw new RuntimeException(ex);
        }
        return baos.toByteArray();        
    }

	private static int getMaxFieldNameLength(Map<String, ? extends MatVariable>[] fields) {
		int len = Integer.MIN_VALUE;
        for (int i = 0; i < fields.length; i++) {
			for (String name : fields[i].keySet()) {
				len = Math.max(len, name.length());
			}
		}
        return len + 1;
	}

//	private static byte[] getNameBytes(String name) {
//		try {
//			return name.getBytes("UTF-8");
//		}
//		catch (UnsupportedEncodingException ex) {
//			throw new RuntimeException(ex);
//		}
//	}


}
