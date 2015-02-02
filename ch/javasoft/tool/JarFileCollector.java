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
package ch.javasoft.tool;

import java.io.File;
import java.io.FilenameFilter;

/**
 * Java Classpath Collector.
 * 
 * Collects jar (and on demand zip files) in one or more directories and outputs 
 * the classpath as to be used with the java virtual machine parameter -cp
 * 
 * Use --help option to get exact syntax.
 */
public class JarFileCollector {

	public static void main(String[] args) {
		//args = new String[] {"--base", "..", "../metabolic-parse/lib"};
		try {
			int start = 0;
			String		baseDir		= null;
			String[]	fileEndings = {"jar"};//the default
			for (int i = 0; i < args.length; ) {
				if (!args[i].startsWith("-")) break;
				if (i == 0 && args[i].equals("--help")) {
					showUsage();
					System.exit(0);
				}
				if ("--base".equals(args[i])) {
					i++;
					start++;
					if (i == args.length) {
						showUsage();
						System.exit(4);						
					}
					baseDir = new File(args[i]).getCanonicalPath();
					i++;
					start++;
				}
				else {
					i++;
					start++;
					if (start > fileEndings.length) {
						String[] old = fileEndings;
						fileEndings = new String[fileEndings.length + 1];
						System.arraycopy(old, 0, fileEndings, 0, old.length);
					}
					fileEndings[start - 1] = args[i].substring(1);
				}
			}
			if (start == args.length) {
				showUsage();
				System.exit(3);
			}
			String path = null;
			for (int i = start; i < args.length; i++) {
				File file = new File(args[i]);
				if (!file.exists() || !file.isDirectory() || !file.canRead()) {
					System.err.println("file does not exist, is not a directory or not readable: " + file.getAbsolutePath());
					System.exit(1);
				}
				if (baseDir == null) {
					baseDir = file.getCanonicalPath();
				}
				if (baseDir.endsWith(File.separator)) baseDir = baseDir.substring(0, baseDir.length() - 1);
				for (int j = 0; j < fileEndings.length; j++) {
					final String fileEnding = "." + fileEndings[j];
					File[] files = file.listFiles(new FilenameFilter() {
						public boolean accept(File dir, String name) {
							return name.endsWith(fileEnding);
						}
					});
					for (int k = 0; k < files.length; k++) {
						String fileName = files[k].getCanonicalPath();
						if (!fileName.startsWith(baseDir)) {
							System.err.println("file '" + fileName + "' does not start with base directory: " + baseDir);
							System.exit(5);
						}						
						String relFileName = fileName.substring(baseDir.length() + (baseDir.length() == 0 ? 0 : 1));
						if (path == null) path = relFileName;
						else path = path + ":" + relFileName;
					}
				}
			}
			System.out.println(path);
			System.exit(0);
		}
		catch (Exception ex) {
			ex.printStackTrace();
			System.exit(2);
		}
	}
	private static void showUsage() {
		System.out.println("usage: JarFileCollector [options] dir [dir ...]");
		System.out.println("         options    --help          show this help message");
		System.out.println("         options    --base basedir  base directory, needed if multiple directories are given");
		System.out.println("         options    -ending         one or more file endings for archives");
		System.out.println("                                    e.g. -jar -zip, default is -jar");
		System.out.println();
		System.out.println("samples:");
		System.out.println("         JarFileCollector lib                             collects all jar files in the lib directory");
		System.out.println("         JarFileCollector -jar -zip lib                   collects all jar and zip files in lib");
		System.out.println("         JarFileCollector -base . lib/int lib/ext lib     collects all jar files in lib/int and lib/ext");
	}
}
