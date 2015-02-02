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
package ch.javasoft.metabolic.efm;


import java.io.File;
import java.io.FileNotFoundException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import ch.javasoft.metabolic.MetabolicNetwork;
import ch.javasoft.metabolic.Norm;
import ch.javasoft.metabolic.compress.CompressionMethod;
import ch.javasoft.metabolic.efm.adj.ModIntPrimeInCoreAdjEnum;
import ch.javasoft.metabolic.efm.config.Arithmetic;
import ch.javasoft.metabolic.efm.config.Config;
import ch.javasoft.util.logging.Loggers;

/**
 * <tt>PalssonTest</tt> contains unit test methods which start efm computation 
 * for palsson-style models, that is a text file with reactions like those found
 * in <tt>../metabolic-data/palsson/<i>xxx</i>/</tt>.
 */
public class PalssonTest extends ch.javasoft.metabolic.parse.PalssonTest {
	static {
		/* Works for coli!!!*/
//		final CompressionMethod[] compression = CompressionMethod.methods(CompressionMethod.UniqueFlows, CompressionMethod.DeadEnd, CompressionMethod.CoupledZero, CompressionMethod.CoupledContradicting, CompressionMethod.DuplicateGene, CompressionMethod.Recursive);
//		final CompressionMethod[] compression = CompressionMethod.STANDARD_NO_COMBINE;
//		final CompressionMethod[] compression = CompressionMethod.STANDARD_NO_NULL_COMBINE;
		final CompressionMethod[] compression = CompressionMethod.STANDARD_NO_DUPLICATE;

//		final CompressionMethod[] compression = CompressionMethod.STANDARD_NO_DUPLICATE;
//		final CompressionMethod[] compression = CompressionMethod.methods(CompressionMethod.UniqueFlows, CompressionMethod.CoupledZero, CompressionMethod.CoupledContradicting, CompressionMethod.DuplicateGene, CompressionMethod.Recursive);
//		final CompressionMethod[] compression = CompressionMethod.methods(CompressionMethod.CoupledZero, CompressionMethod.CoupledContradicting, CompressionMethod.DuplicateGene, CompressionMethod.Recursive);
//		final CompressionMethod[] compression = CompressionMethod.ALL;
		
//		if (Config.initForJUnitTest(PatternTreeMinZerosAdjacencyEnumerator.NAME, "MostZerosOrAbsLexMin", compression, false, Arithmetic.fractional, Norm.norm2)) {

//		if (Config.initForJUnitTest(PatternTreeMinZerosAdjacencyEnumerator.NAME, compression, Arithmetic.fractional)) {
//		if (Config.initForJUnitTest(ModIntPrimePatternTreeRankUpdateAdjacencyEnumerator.NAME, compression, Arithmetic.fractional)) {
//		if (Config.initForJUnitTest(ModIntPrimePatternTreeRankUpdateAdjacencyEnumerator.NAME, "MostZerosOrAbsLexMin", compression, false, Arithmetic.fractional, Norm.norm2)) {

//		if (Config.initForJUnitTest(PatternTreeModRankAdjacencyEnumerator.NAME, "FewestNegPos", compression, Arithmetic.fractional)) {

//		if (Config.initForJUnitTest(ModIntPrimePatternTreeRankUpdateAdjacencyEnumerator.NAME, compression, Arithmetic.fractional)) {

//		if (Config.initForJUnitTest(PatternTreeModRankAdjacencyEnumerator.NAME, compression, Arithmetic.fractional)) {
//		if (Config.initForJUnitTest(ModIntPrimePatternTreeRankUpdateAdjacencyEnumerator.NAME, compression, Arithmetic.fractional)) {
//		if (Config.initForJUnitTest(PatternTreeModRankAdjacencyEnumerator.NAME, compression, Arithmetic.double_)) {
//		if (Config.initForJUnitTest(PatternTreeMinZerosAdjacencyEnumerator.NAME, compression, Arithmetic.fractional)) {
//		if (Config.initForJUnitTest(FractionalPatternTreeRankUpdateAdjacencyEnumerator.NAME, compression, Arithmetic.fractional)) {
		if (Config.initForJUnitTest(ModIntPrimeInCoreAdjEnum.NAME, "FewestNegPosOrMostZeros", compression, false, Arithmetic.fractional, Norm.norm2)) {
//		if (Config.initForJUnitTest(ModIntPrimeInCoreAdjEnum.NAME, "MostZerosOrFewestNegPos", compression, false, Arithmetic.fractional, Norm.norm2)) {
//		if (Config.initForJUnitTest(PatternTreeMinZerosAdjacencyEnumerator.NAME, "MostZerosOrFewestNegPos", compression, false, Arithmetic.fractional, Norm.norm2)) {
//		if (Config.initForJUnitTest(ModIntPrimeOutCoreAdjEnum.NAME, "FewestNegPosOrMostZeros", compression, false, Arithmetic.fractional, Norm.norm2)) {
//		if (Config.initForJUnitTest(ModIntPrimeOutCoreAdjEnum.NAME, "MostZerosOrAbsLexMin", compression, false, Arithmetic.fractional, Norm.norm2)) {
//	        ElementaryFluxModes.setImpl(new SequentialDoubleDescriptionImpl(Config.getConfig(), new NullspaceEfmModelFactory(), new OutOfCoreMemoryFactory()));       		
			Loggers.getRootLogger().setLevel(Level.FINER);
			Logger.getLogger("compress.data").setLevel(Level.INFO);
		}
	}
	
	@Override
	public void testPalsson_aureus_iMH556() throws Exception {
		super.testPalsson_aureus_iMH556();
	}
	@Override
	public void testPalsson_aureus_iSB619_xnone() throws Exception {
		super.testPalsson_aureus_iSB619_xnone();
	}
	@Override
	public void testPalsson_aureus_iSB619_xrev() throws Exception {
		super.testPalsson_aureus_iSB619_xrev();
	}
	@Override
	public void testPalsson_aureus_iSB619_xspec() throws Exception {
		super.testPalsson_aureus_iSB619_xspec();
	}
	@Override
	public void testPalsson_aureus_iSB619_opt() throws Exception {
		super.testPalsson_aureus_iSB619_opt();
	}
	@Override
	public void testPalsson_barkeri_iAF692_xspec() throws Exception {
		super.testPalsson_barkeri_iAF692_xspec();
	}
	@Override
	public void testPalsson_barkeri_iAF692_opt() throws Exception {
		super.testPalsson_barkeri_iAF692_opt();
	}
	/**
	 * Runs faster with STANDARD_NO_DUPLICATE compression
	 */
	@Override
	public void testPalsson_coli_iJR904_xnone_nobio() throws Exception {
		super.testPalsson_coli_iJR904_xnone_nobio();
	}
	/**
	 * Runs faster with STANDARD_NO_DUPLICATE compression
	 */
	@Override
	public void testPalsson_coli_iJR904_xnone() throws Exception {
		super.testPalsson_coli_iJR904_xnone();
	}
	@Override
	public void testPalsson_coli_iJR904_minimal() throws Exception {
		super.testPalsson_coli_iJR904_minimal();
	}
	@Override
	public void testPalsson_coli_iJR904_xrev() throws Exception {
		super.testPalsson_coli_iJR904_xrev();
	}
	@Override
	public void testPalsson_coli_iJR904_xspec() throws Exception {
		super.testPalsson_coli_iJR904_xspec();
	}
	@Override
	public void testPalsson_coli_iJR904_fba_reduced() throws Exception {
		super.testPalsson_coli_iJR904_fba_reduced();
	}
	@Override
	public void testPalsson_coli_iJR904_fba_reduced_ac() throws Exception {
		super.testPalsson_coli_iJR904_fba_reduced_ac();
	}
	@Override
	public void testPalsson_coli_iJR904_fba_reduced_xnone() throws Exception {
		super.testPalsson_coli_iJR904_fba_reduced_xnone();
	}
	@Override
	public void testPalsson_coli_Robert_reduced() throws Exception {
		super.testPalsson_coli_Robert_reduced();
	}
	@Override
	public void testPalsson_coli_Robert() throws Exception {
		super.testPalsson_coli_Robert();
	}
	@Override
	public void testPalsson_pylori_iIT341_xnone() throws Exception {
		super.testPalsson_pylori_iIT341_xnone();
	}
	@Override
	public void testPalsson_pylori_iIT341_redin() throws Exception {
		Config.getConfig().getReactionsToEnforce().add("mue");
		super.testPalsson_pylori_iIT341_redin();
	}
	@Override
	public void testPalsson_pylori_iIT341_xspec() throws Exception {
		super.testPalsson_pylori_iIT341_xspec();
	}
	@Override
	public void testPalsson_pylori_iIT341_glc() throws Exception {
		super.testPalsson_pylori_iIT341_glc();
	}
	@Override
	public void testPalsson_pylori_iIT341_glcmin() throws Exception {
		super.testPalsson_pylori_iIT341_glcmin();
	}

	/*
		"EX-aa(e)"	"Acetamide exchange"	"[e] : aa <-->"
		"EX-ac(e)"	"Acetate exchange"	"[e] : ac <-->"
		"EX-acac(e)"	"Acetoacetate exchange"	"[e] : acac <-->"
		"EX-acald(e)"	"Acetaldehyde exchange"	"[e] : acald <-->"
		"EX-ad(e)"	"Acrylamide exchange"	"[e] : ad <-->"
		"EX-ade(e)"	"Adenine exchange"	"[e] : ade <-->"
		"EX-adn(e)"	"Adenosine exchange"	"[e] : adn <-->"
		"EX-akg(e)"	"2-Oxoglutarate exchange"	"[e] : akg <-->"
		"EX-ala-D(e)"	"D-Alanine exchange"	"[e] : ala-D <-->"
		"EX-ala-L(e)"	"L-Alanine exchange"	"[e] : ala-L <-->"
		"EX-arg-L(e)"	"L-Arginine exchange"	"[e] : arg-L <-->"
		"EX-asn-L(e)"	"L-Asparagine exchange"	"[e] : asn-L <-->"
		"EX-asp-L(e)"	"L-Aspartate exchange"	"[e] : asp-L <-->"
		"EX-cit(e)"	"Citrate exchange"	"[e] : cit <-->"
		"EX-co2(e)"	"CO2 exchange"	"[e] : co2 <-->"
		"EX-cys-L(e)"	"L-Cysteine exchange"	"[e] : cys-L <-->"
		"EX-cytd(e)"	"Cytidine exchange"	"[e] : cytd <-->"
		"EX-dad-2(e)"	"Deoxyadenosine exchange"	"[e] : dad-2 <-->"
		"EX-dcyt(e)"	"Deoxycytidine exchange"	"[e] : dcyt <-->"
		"EX-duri(e)"	"Deoxyuridine exchange"	"[e] : duri <-->"
		"EX-etoh(e)"	"Ethanol exchange"	"[e] : etoh <-->"
		"EX-fe2(e)"	"Fe2+ exchange"	"[e] : fe2 <-->"
		"EX-fe3(e)"	"Fe3+ exchange"	"[e] : fe3 <-->"
		"EX-for(e)"	"Formate exchange"	"[e] : for <-->"
		"EX-fum(e)"	"Fumarate exchange"	"[e] : fum <-->"
		"EX-gal(e)"	"D-Galactose exchange"	"[e] : gal <-->"
		"EX-glc(e)"	"D-Glucose exchange"	"[e] : glc-D <-->"
		"EX-gln-L(e)"	"L-Glutamine exchange"	"[e] : gln-L <-->"
		"EX-glu-L(e)"	"L-Glutamate exchange"	"[e] : glu-L <-->"
		"EX-gly(e)"	"Glycine exchange"	"[e] : gly <-->"
		"EX-gsn(e)"	"Guanosine exchange"	"[e] : gsn <-->"
		"EX-gua(e)"	"Guanine exchange"	"[e] : gua <-->"
		"EX-h(e)"	"H+ exchange"	"[e] : h <-->"
		"EX-h2(e)"	"H2 exchange"	"[e] : h2 <-->"
		"EX-h2co3(e)"	"carbonic acid exchange"	"[e] : h2co3 <-->"
		"EX-h2o(e)"	"H2O exchange"	"[e] : h2o <-->"
		"EX-his-L(e)"	"L-Histidine exchange"	"[e] : his-L <-->"
		"EX-hxan(e)"	"Hypoxanthine exchange"	"[e] : hxan <-->"
		"EX-ile-L(e)"	"L-Isoleucine exchange"	"[e] : ile-L <-->"
		"EX-lac-L(e)"	"L-Lactate exchange"	"[e] : lac-L <-->"
		"EX-leu-L(e)"	"L-Leucine exchange"	"[e] : leu-L <-->"
		"EX-lys-L(e)"	"L-Lysine exchange"	"[e] : lys-L <-->"
		"EX-mal-L(e)"	"L-Malate exchange"	"[e] : mal-L <-->"
		"EX-met-L(e)"	"L-Methionine exchange"	"[e] : met-L <-->"
		"EX-na1(e)"	"Sodium exchange"	"[e] : na1 <-->"
		"EX-nh4(e)"	"Ammonia exchange"	"[e] : nh4 <-->"
		"EX-ni2(e)"	"Ni2+ exchange"	"[e] : ni2 <-->"
		"EX-nmn(e)"	"NMN exchange"	"[e] : nmn <-->"
		"EX-no(e)"	"Nitric oxide exchange"	"[e] : no <-->"
		"EX-no2(e)"	"Nitrite exchange"	"[e] : no2 <-->"
		"EX-no3(e)"	"Nitrate exchange"	"[e] : no3 <-->"
		"EX-o2(e)"	"O2 exchange"	"[e] : o2 <-->"
		"EX-orn(e)"	"Ornithine exchange"	"[e] : orn <-->"
		"EX-orot(e)"	"Orotate exchange"	"[e] : orot <-->"
		"EX-phe-L(e)"	"L-Phenylalanine exchange"	"[e] : phe-L <-->"
		"EX-pheme(e)"	"(proto) heme exchange"	"[e] : pheme <-->"
		"EX-pi(e)"	"Phosphate exchange"	"[e] : pi <-->"
		"EX-pime(e)"	"Pimelate exchange"	"[e] : pime <-->"
		"EX-pro-L(e)"	"L-Proline exchange"	"[e] : pro-L <-->"
		"EX-pyr(e)"	"Pyruvate exchange"	"[e] : pyr <-->"
		"EX-ser-D(e)"	"D-Serine exchange"	"[e] : ser-D <-->"
		"EX-ser-L(e)"	"L-Serine exchange"	"[e] : ser-L <-->"
		"EX-so4(e)"	"Sulfate exchange"	"[e] : so4 <-->"
		"EX-succ(e)"	"Succinate exchange"	"[e] : succ <-->"
		"EX-thm(e)"	"Thiamin exchange"	"[e] : thm <-->"
		"EX-thr-L(e)"	"L-Threonine exchange"	"[e] : thr-L <-->"
		"EX-thymd(e)"	"Thymidine exchange"	"[e] : thymd <-->"
		"EX-trp-L(e)"	"L-Tryptophan exchange"	"[e] : trp-L <-->"
		"EX-tyr-L(e)"	"L-Tyrosine exchange"	"[e] : tyr-L <-->"
		"EX-ura(e)"	"Uracil exchange"	"[e] : ura <-->"
		"EX-urea(e)"	"Urea exchange"	"[e] : urea <-->"
		"EX-uri(e)"	"Uridine exchange"	"[e] : uri <-->"
		"EX-val-L(e)"	"L-Valine exchange"	"[e] : val-L <-->"
		"EX-xan(e)"	"Xanthine exchange"	"[e] : xan <-->"
		"mue"	"biomass formation"	"0.05 5mthf[c] + 0.00005 accoa[c] + 0.488 ala-L[c] + 0.001 amp[c] + 0.281 arg-L[c] + 0.229 asn-L[c] + 0.229 asp-L[c] + 45.7318 atp[c] + 0.000006 btn[c] + 0.027907 clpn-HP[c] + 0.000006 coa[c] + 0.126 ctp[c] + 0.087 cys-L[c] + 0.0247 datp[c] + 0.0254 dctp[c] + 0.0254 dgtp[c] + 0.0247 dttp[c] + 0.00001 fad[c] + 0.25 gln-L[c] + 0.25 glu-L[c] + 0.582 gly[c] + 0.203 gtp[c] + 45.5608 h2o[c] + 0.09 his-L[c] + 0.276 ile-L[c] + 0.428 leu-L[c] + 0.0084 lps-HP[c] + 0.326 lys-L[c] + 0.146 met-L[c] + 0.000006 mqn6[c] + 0.00215 nad[c] + 0.00005 nadh[c] + 0.00013 nadp[c] + 0.0004 nadph[c] + 0.0748946 pe-HP[c] + 0.0276 peptido-EC[c] + 0.0163548 pg-HP[c] + 0.176 phe-L[c] + 0.000006 pheme[c] + 0.21 pro-L[c] + 0.0033748 ps-HP[c] + 0.035 ptrc[c] + 0.205 ser-L[c] + 0.007 spmd[c] + 0.000003 succoa[c] + 0.000006 thm[c] + 0.241 thr-L[c] + 0.054 trp-L[c] + 0.131 tyr-L[c] + 0.003 udpg[c] + 0.136 utp[c] + 0.402 val-L[c] --> mue[e]"
		"EX-mue(e)"	"biomass extract"	"[e] : mue -->"
	 */
	@Override
	public void testPalsson_pylori_iIT341_aminomin() throws Exception {
//		final String exnames = "EX-aa(e) EX-ac(e) EX-acac(e) EX-acald(e) EX-ad(e) EX-ade(e) EX-adn(e) EX-akg(e) EX-ala-D(e) EX-ala-L(e) EX-arg-L(e) EX-asn-L(e) EX-asp-L(e) EX-cit(e) EX-co2(e) EX-cys-L(e) EX-cytd(e) EX-dad-2(e) EX-dcyt(e) EX-duri(e) EX-etoh(e) EX-fe2(e) EX-fe3(e) EX-for(e) EX-fum(e) EX-gal(e) EX-glc(e) EX-gln-L(e) EX-glu-L(e) EX-gly(e) EX-gsn(e) EX-gua(e) EX-h(e) EX-h2(e) EX-h2co3(e) EX-h2o(e) EX-his-L(e) EX-hxan(e) EX-ile-L(e) EX-lac-L(e) EX-leu-L(e) EX-lys-L(e) EX-mal-L(e) EX-met-L(e) EX-na1(e) EX-nh4(e) EX-ni2(e) EX-nmn(e) EX-no(e) EX-no2(e) EX-no3(e) EX-o2(e) EX-orn(e) EX-orot(e) EX-phe-L(e) EX-pheme(e) EX-pi(e) EX-pime(e) EX-pro-L(e) EX-pyr(e) EX-ser-D(e) EX-ser-L(e) EX-so4(e) EX-succ(e) EX-thm(e) EX-thr-L(e) EX-thymd(e) EX-trp-L(e) EX-tyr-L(e) EX-ura(e) EX-urea(e) EX-uri(e) EX-val-L(e) EX-xan(e) EX-mue(e)";
//		//inputs
//		Config.getConfig().getReactionsToSuppress().addAll(Arrays.asList(exnames.split("\\s+")));
//		Config.getConfig().getReactionsToSuppress().remove("EX-ala-D(e)");
//		Config.getConfig().getReactionsToSuppress().remove("EX-ala-L(e)");
//		Config.getConfig().getReactionsToSuppress().remove("EX-arg-L(e)");
//		Config.getConfig().getReactionsToSuppress().remove("EX-ade(e)");
//		Config.getConfig().getReactionsToSuppress().remove("EX-pi(e)");
//		Config.getConfig().getReactionsToSuppress().remove("EX-so4(e)");
//		Config.getConfig().getReactionsToSuppress().remove("EX-o2(e)");
//
//		Config.getConfig().getReactionsToSuppress().remove("EX-his-L(e)");
//		Config.getConfig().getReactionsToSuppress().remove("EX-ile-L(e)");
//		Config.getConfig().getReactionsToSuppress().remove("EX-leu-L(e)");
//		Config.getConfig().getReactionsToSuppress().remove("EX-met-L(e)");
//		Config.getConfig().getReactionsToSuppress().remove("EX-phe-L(e)");
//		Config.getConfig().getReactionsToSuppress().remove("EX-val-L(e)");
//		Config.getConfig().getReactionsToSuppress().remove("EX-thm(e)");
//		
//		
//		//outputs
//		//a) general (carbon sink)
//		Config.getConfig().getReactionsToSuppress().remove("EX-ac(e)");
//		Config.getConfig().getReactionsToSuppress().remove("EX-succ(e)");
//		Config.getConfig().getReactionsToSuppress().remove("EX-for(e)");
//		Config.getConfig().getReactionsToSuppress().remove("EX-lac-L(e)");
//
//		Config.getConfig().getReactionsToSuppress().remove("EX-nh4(e)");
//		Config.getConfig().getReactionsToSuppress().remove("EX-co2(e)");
//		
//		//b) target output: non-essential amino acids
//		Config.getConfig().getReactionsToSuppress().remove("EX-asn-L(e)");
////		Config.getConfig().getReactionsToSuppress().remove("EX-asp-L(e)");
////		Config.getConfig().getReactionsToSuppress().remove("EX-cys-L(e)");
////		Config.getConfig().getReactionsToSuppress().remove("EX-gln-L(e)");
////		Config.getConfig().getReactionsToSuppress().remove("EX-glu-L(e)");
////		Config.getConfig().getReactionsToSuppress().remove("EX-gly(e)");
////		Config.getConfig().getReactionsToSuppress().remove("EX-lys-L(e)");
////		Config.getConfig().getReactionsToSuppress().remove("EX-pro-L(e)");
////		Config.getConfig().getReactionsToSuppress().remove("EX-ser-D(e)");
////		Config.getConfig().getReactionsToSuppress().remove("EX-ser-L(e)");
////		Config.getConfig().getReactionsToSuppress().remove("EX-thr-L(e)");
////		Config.getConfig().getReactionsToSuppress().remove("EX-trp-L(e)");
////		Config.getConfig().getReactionsToSuppress().remove("EX-tyr-L(e)");
//		
//		//c) target output: ribonucleotide
////		Config.getConfig().getReactionsToSuppress().remove("EX-nmn(e)");
//		
//		super.testPalsson_pylori_iIT341_xspec();

//		Config.getConfig().getReactionsToSuppress().add("EX-asp-L(e)");
//		Config.getConfig().getReactionsToSuppress().add("EX-cys-L(e)");
//		Config.getConfig().getReactionsToSuppress().add("EX-gln-L(e)");
//		Config.getConfig().getReactionsToSuppress().add("EX-glu-L(e)");
//		Config.getConfig().getReactionsToSuppress().add("EX-gly(e)");
//		Config.getConfig().getReactionsToSuppress().add("EX-lys-L(e)");
//		Config.getConfig().getReactionsToSuppress().add("EX-pro-L(e)");
//		Config.getConfig().getReactionsToSuppress().add("EX-ser-D(e)");
//		Config.getConfig().getReactionsToSuppress().add("EX-ser-L(e)");
//		Config.getConfig().getReactionsToSuppress().add("EX-thr-L(e)");
//		Config.getConfig().getReactionsToSuppress().add("EX-trp-L(e)");
//		Config.getConfig().getReactionsToSuppress().add("EX-tyr-L(e)");
//		
//		Config.getConfig().getReactionsToSuppress().add("EX-nmn(e)");
		super.testPalsson_pylori_iIT341_aminomin();
	}
	public void testPalsson_pylori_iIT341_aminoGlnGly() throws Exception {
//		Config.getConfig().getReactionsToEnforce().add("EX-gln-L(e)");
		Config.getConfig().getReactionsToEnforce().add("EX-gly(e)");
		super.testPalsson_pylori_iIT341_aminominGlnGly();
	}
	
	/* (non-Javadoc)
	 * @see ch.javasoft.metabolic.parse.PalssonTest#testPalsson_pylori_iIT341_aminoglcmin()
	 */
	@Override
	public void testPalsson_pylori_iIT341_aminoglcmin() throws Exception {
		super.testPalsson_pylori_iIT341_aminoglcmin();
	}
	
	@Override
	public void testPalsson_pylori_iCS291() throws Exception {
		super.testPalsson_pylori_iCS291();
	} 
	
	@Override
	public void testPalsson_pylori_iCS291_amino() throws Exception {
//		Config.getConfig().getReactionsToEnforce().add("ex_FORxt");
//		Config.getConfig().getReactionsToEnforce().add("ex_ADNxt");
//		Config.getConfig().getReactionsToEnforce().add("ex_CYSxt");
//		Config.getConfig().getReactionsToEnforce().add("ex_GLNxt");
//		Config.getConfig().getReactionsToEnforce().add("ex_GLYxt");

//		Config.getConfig().getReactionsToEnforce().add("ex_ARGxt");
		super.testPalsson_pylori_iCS291_amino();
	}
	@Override
	public void testPalsson_pylori_iCS291_glcamino() throws Exception {
		super.testPalsson_pylori_iCS291_glcamino();
	}
	@Override
	public void testPalsson_pylori_iCS291_specamino() throws Exception {
		super.testPalsson_pylori_iCS291_specamino();
	}
	
	@Override
	public void testPalsson_yeastc_iLL672_unbalancedMetas() throws Exception {
		super.testPalsson_yeastc_iLL672_unbalancedMetas();
	}
	@Override
	public void testPalsson_yeastc_iLL672() throws Exception {
		super.testPalsson_yeastc_iLL672();
	}
	@Override
	public void testPalsson_yeastc_iLL672_xnone() throws Exception {
		super.testPalsson_yeastc_iLL672_xnone();
	}
	@Override
	public void testPalsson_yeastc_iND750_xnone() throws Exception {
		super.testPalsson_yeastc_iND750_xnone();
	}
	@Override
	public void testPalsson_yeastc_iND750() throws Exception {
		super.testPalsson_yeastc_iND750();
	}

	@Override
	protected void internalTestMetabolicNetwork(MetabolicNetwork network, Set<String> suppressedReactions) throws Exception {
		if (suppressedReactions != null) {
			Config.getConfig().getReactionsToSuppress().addAll(suppressedReactions);
		}
//		ElementaryFluxModes.calculateCallback(
//			reducedNetwork, new TextOutputCallback(OutputMode.BinaryUncompressed, new FileOutputStream(outFile))
//		);
//		ElementaryFluxModes.calculateCallback(
//			reducedNetwork, new TextOutputCallback(OutputMode.DoubleUncompressed, new FileOutputStream(outFile))
//		);

//		File outFile = new File("/home/terzerm/eth/metabolic-efm-run/results-palsson/mnet_palsson.mat");
//		ElementaryFluxModes.calculateCallback(
//			reducedNetwork, new PartitionedMatFileOutputCallback(
//				originalNetwork, outFile.getParentFile(), outFile.getName()/*, 2000000*/
//			)
//		);
//		LogPkg.LOGGER.info("written modes to file: " + outFile.getAbsolutePath());

//		ElementaryFluxModes.calculateLogCountOnly(network);
		
		File outFile = new File("/local/tmp/mnet_palsson.mat");
		
		if (!outFile.getParentFile().exists()) throw new FileNotFoundException(outFile.getParentFile().getAbsolutePath());
		ElementaryFluxModes.calculateFileMatlab(network, outFile.getParentFile(), outFile.getName());
		LogPkg.LOGGER.info("written modes to file: " + outFile.getAbsolutePath());
	}
}
