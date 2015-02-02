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
package ch.javasoft.metabolic.fa;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

import ch.javasoft.util.Arrays;

/**
 * ecoli model
 * 
 * uptake reactions:
 *   O2_up, S_up, N_up, Glc_PTS_up, Glc_ATP_up, Succ_up, Glyc_up, Ac_up
 * 
 * extract reactions:
 *   CO2_ex, Lac_ex, Eth_ex, Ac_ex, Form_ex
 */
public interface FaConstants {
	File FLUX_ANALYSER_FOLDER	= new File("../metabolic-data/FluxAnalyzer50a");
	File COLI_FOLDER			= new File(FLUX_ANALYSER_FOLDER, "coli");
	
	class Net {
		public final String		name;		
		public final boolean	allowMetaboliteAccumulation;
		public Net(String name, boolean allowMetaboliteAccumulation) {
			this.name							= name;
			this.allowMetaboliteAccumulation	= allowMetaboliteAccumulation;
		}
	}
	class SubNet extends Net {
		public final String[]	excludeReactions;
		public SubNet(String name, String[] excludeReactions) {
			this(name, excludeReactions, false);
		}
		public SubNet(String name, String[] excludeReactions, boolean allowMetaboliteAccumulation) {
			super(name, allowMetaboliteAccumulation);
			this.excludeReactions = excludeReactions;
		}
		public String getDotFileName() {
			return name + ".dot";
		}
		public String getFluxesRefFileName() {
			return name + "-fluxes-ref.txt";
		}
		public String getFluxesFileName() {
			return name + "-fluxes.txt";
		}
	}
	class SuperNet extends Net {
		public final String[][]	additionalReactions;
		public final String[]	excludeReactions;
		public SuperNet(String name, String[][] additionalReactions) {
			this(name, additionalReactions, new String[] {});
		}
		public SuperNet(String name, String[][] additionalReactions, String[] excludeReactions) {
			super(name, false);
			this.additionalReactions	= additionalReactions;
			this.excludeReactions		= excludeReactions;
		}
		@Override
		public String toString() {
			return toString(additionalReactions);
		}
		public static String toString(String[][] reactionNamesFormulas) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			for (String[] reac : reactionNamesFormulas) {
				pw.println("\"" + reac[0] + "\"\t\"" + reac[0] + "\"\t\"" + reac[1] + "\"");
			}
			pw.flush();
			return sw.toString();			
		}
	}	
	public static final String[] SUCC_EX = new String[] {
		"Succ_ex", "Succ -->"
	};
	public static final String[][] AMINO = new String[][] {
//			HSer      	Homoserine                         	0.001 	0 
//			Ala       	Alanin                             	1e-12 	0 
//			Cys       	Cystein                            	1e-12 	0 
//			Asp       	Aspartate                          	1e-12 	0 
//			Glu       	Glutamate                          	1e-12 	0 
//			Phe       	Phenylalanin                       	1e-12 	0 
//			Gly       	Glycin                             	1e-12 	0 
//			His       	Histidin                           	1e-12 	0 
//			Ile       	Isoleucin                          	1e-12 	0 
//			Lys       	Lysin                              	1e-12 	0 
//			Leu       	Leucin                             	1e-12 	0 
//			Met       	Methionin                          	1e-12 	0 
//			Asn       	Asparagin                          	1e-12 	0 
//			Pro       	Prolin                             	1e-12 	0 
//			Gln       	Glutamin                           	1e-12 	0 
//			Arg       	Arginin                            	1e-12 	0 
//			Ser       	Serin                              	1e-12 	0 
//			Thr       	Threonin                           	1e-12 	0 
//			Val       	Valin                              	1e-12 	0 
//			Trp       	Tryptophan                         	1e-12 	0 
//			Tyr       	Tyrosin                            	1e-12 	0 
			{"Asp_up", "--> Asp"},
			{"Glu_up", "--> Glu"},
			{"Ser_up", "--> Ser"},
			{"Thr_up", "--> Thr"},
			{"Trp_up", "--> Trp"},
			{"HSer_up", "--> HSer"},
			{"Ala_up", "--> Ala"},
			{"Cys_up", "--> Cys"},
			{"Phe_up", "--> Phe"},
			{"Gly_up", "--> Gly"},
			{"His_up", "--> His"},
			{"Ile_up", "--> Ile"},
			{"Lys_up", "--> Lys"},
			{"Leu_up", "--> Leu"},
			{"Met_up", "--> Met"},
			{"Asn_up", "--> Asn"},
			{"Pro_up", "--> Pro"},
			{"Gln_up", "--> Gln"},
			{"Arg_up", "--> Arg"},
			{"Val_up", "--> Val"},
			{"Tyr_up", "--> Tyr"}
		}
	;
	SuperNet SUPERALL_UP_AMINO_EX_SUCC = new SuperNet(
		"superall", Arrays.merge(
			AMINO, 
			new String[][] {SUCC_EX}
		)
	);
	SuperNet GL_UP_ALL_AMINO_EX_SUCC = new SuperNet(
		"glx_allamino", Arrays.merge(
			AMINO, 
			new String[][] {SUCC_EX}
		),
		new String[] {"Succ_up", "Glyc_up", "Ac_up"}
	);	
	SuperNet GL_PTS_UP_ALL_AMINO_EX_SUCC = new SuperNet(
		"glpts_allamino", Arrays.merge(
			AMINO, 
			new String[][] {SUCC_EX}
		),
		new String[] {"Glc_ATP_up", "Succ_up", "Glyc_up", "Ac_up"}
	);	
	SuperNet SUPERXX_UP_ASP_GLU_SER_TRP_EX_SUCC = new SuperNet(
		"superxxx", new String[][] {
			{"Asp_up", "--> Asp"},
			{"Glu_up", "--> Glu"},
			{"Ser_up", "--> Ser"},
			{"Trp_up", "--> Trp"},
			SUCC_EX
		}
	);
	SuperNet SUPERXX_UP_ASP_GLU_SER_THR_EX_SUCC = new SuperNet(
			"superxxx", new String[][] {
				{"Asp_up", "--> Asp"},
				{"Glu_up", "--> Glu"},
				{"Ser_up", "--> Ser"},
				{"Thr_up", "--> Thr"},
				SUCC_EX
			}
		);
	SuperNet SUPERXXX_UP_ASP_GLU_SER_THR_TRP_EX_SUCC = new SuperNet(
			"superxxx", new String[][] {
				{"Asp_up", "--> Asp"},
				{"Glu_up", "--> Glu"},
				{"Ser_up", "--> Ser"},
				{"Thr_up", "--> Thr"},
				{"Trp_up", "--> Trp"},
				SUCC_EX
			}
		);
	SuperNet SUPERX_UP_ASP_GLU_SER_EX_SUCC = new SuperNet(
		"superx", new String[][] {
			{"Asp_up", "--> Asp"},
			{"Glu_up", "--> Glu"},
			{"Ser_up", "--> Ser"},
			SUCC_EX
		}
	);
	SuperNet SUPER_UP_ASP_EX_SUCC = new SuperNet(
		"super", new String[][] {
			{"Asp_up", "--> Asp"},
			SUCC_EX
		}
	);
	SuperNet SUPER_EX_SUCC = new SuperNet(
		"allx", new String[][] {
			SUCC_EX
		}
	);
	SuperNet GL_EX_SUCC = new SuperNet(
		"glx", new String[][] {
			SUCC_EX
		},
		new String[] {"Succ_up", "Glyc_up", "Ac_up"}
	);
	SuperNet GL_UP_ALA_ASP_GLU_HIS_PHE_SER_THR_EX_SUCC = new SuperNet(
		"gl_xxamino", new String[][] {
			{"Ala_up", "--> Ala"},
			{"Asp_up", "--> Asp"},
			{"Glu_up", "--> Glu"},
			{"His_up", "--> His"},
			{"Phe_up", "--> Phe"},
			{"Ser_up", "--> Ser"},
			{"Thr_up", "--> Thr"},
			SUCC_EX
		},
		new String[] {"Succ_up", "Glyc_up", "Ac_up"}
	);
	SuperNet GL_UP_ALA_ASP_GLU_HIS_PHE_SER_EX_SUCC = new SuperNet(
		"gl_xamino", new String[][] {
			{"Ala_up", "--> Ala"},
			{"Asp_up", "--> Asp"},
			{"Glu_up", "--> Glu"},
			{"His_up", "--> His"},
			{"Phe_up", "--> Phe"},
			{"Ser_up", "--> Ser"},
			SUCC_EX
		},
		new String[] {"Succ_up", "Glyc_up", "Ac_up"}
	);
	SuperNet GL_UP_ALA_ASP_GLU_PHE_SER_EX_SUCC = new SuperNet(
		"gl_amino", new String[][] {
			{"Ala_up", "--> Ala"},
			{"Asp_up", "--> Asp"},
			{"Glu_up", "--> Glu"},
			{"Phe_up", "--> Phe"},
			{"Ser_up", "--> Ser"},
			SUCC_EX
		},
		new String[] {"Succ_up", "Glyc_up", "Ac_up"}
	);
	SuperNet GL_PTS_UP_ALA_ASP_GLU_HIS_PHE_SER_THR_VAL_EX_SUCC = new SuperNet(
		"glpts_xxxamino", new String[][] {
			{"Ala_up", "--> Ala"},
			{"Asp_up", "--> Asp"},
			{"Glu_up", "--> Glu"},
			{"His_up", "--> His"},
			{"Phe_up", "--> Phe"},
			{"Ser_up", "--> Ser"},
			{"Thr_up", "--> Thr"},
			{"Val_up", "--> Val"},
			SUCC_EX
		},
		new String[] {"Glc_ATP_up", "Succ_up", "Glyc_up", "Ac_up"}
	);
	SuperNet GL_PTS_UP_ALA_ASP_GLU_HIS_PHE_SER_THR_EX_SUCC = new SuperNet(
		"glpts_xxamino", new String[][] {
			{"Ala_up", "--> Ala"},
			{"Asp_up", "--> Asp"},
			{"Glu_up", "--> Glu"},
			{"His_up", "--> His"},
			{"Phe_up", "--> Phe"},
			{"Ser_up", "--> Ser"},
			{"Thr_up", "--> Thr"},
			SUCC_EX
		},
		new String[] {"Glc_ATP_up", "Succ_up", "Glyc_up", "Ac_up"}
	);
	SuperNet GL_PTS_UP_ALA_ASP_GLU_HIS_PHE_SER_EX_SUCC = new SuperNet(
		"glpts_xamino", new String[][] {
			{"Ala_up", "--> Ala"},
			{"Asp_up", "--> Asp"},
			{"Glu_up", "--> Glu"},
			{"His_up", "--> His"},
			{"Phe_up", "--> Phe"},
			{"Ser_up", "--> Ser"},
			SUCC_EX
		},
		new String[] {"Glc_ATP_up", "Succ_up", "Glyc_up", "Ac_up"}
	);
	SuperNet GL_PTS_UP_ALA_ASP_GLU_PHE_SER_EX_SUCC = new SuperNet(
		"glpts_amino", new String[][] {
			{"Ala_up", "--> Ala"},
			{"Asp_up", "--> Asp"},
			{"Glu_up", "--> Glu"},
			{"Phe_up", "--> Phe"},
			{"Ser_up", "--> Ser"},
			SUCC_EX
		},
		new String[] {"Glc_ATP_up", "Succ_up", "Glyc_up", "Ac_up"}
	);
	SubNet SUBNET_ALL = new SubNet(
		"all", new String[] {}
	);
	
	SubNet SUBNET_NOEX = new SubNet(
		"noex", new String[] {"O2_up", "S_up", "N_up", "Glc_PTS_up", "Glc_ATP_up", "Succ_up", "Glyc_up", "Ac_up", "CO2_ex", "Lac_ex", "Eth_ex", "Ac_ex", "Form_ex"}
	);
	SubNet SUBNET_ACE = new SubNet(
		"ace", new String[] {"Glc_PTS_up", "Glc_ATP_up", "Succ_up", "Glyc_up", "Glyc3P::DHAP"}
	);
	SubNet SUBNET_AC = new SubNet(
		"ac", new String[] {"Glc_PTS_up", "Glc_ATP_up", "Succ_up", "Glyc_up", "Ac_ex"}
	);
	SubNet SUBNET_GL = new SubNet(
		"gl", new String[] {"Succ_up", "Glyc_up", "Ac_up"}
		);
	SubNet SUBNET_GLY = new SubNet(
		"gly", new String[] {"Glc_PTS_up", "Glc_ATP_up", "Succ_up", "Ac_up"}
	);
	SubNet SUBNET_STANDARD = new SubNet(
		"standard", new String[] {"Succ_up", "Glyc_up", "Ac_up", "Glyc3P::DHAP"}
	);
	SubNet SUBNET_SUC = new SubNet(
		"suc", new String[] {"Glc_PTS_up", "Glc_ATP_up", "Glyc_up", "Ac_up"}
	);
}
