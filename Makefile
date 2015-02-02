.SUFFIXES: .class .java

JAR=/opt/Oracle_Java/jdk1.7/bin/jar
JAVAC=/opt/Oracle_Java/jdk1.7/bin/javac

JAVAC_FLAGS=-g -classpath .:lib/cplex.jar:lib/applib.jar:lib/dom4j-1.6.1.jar:lib/jaxen-1.1-beta-10.jar:lib/log4j-1.2.8.jar:lib/mtj.jar:lib/commons-logging-1.0.1.jar:lib/efm-prepareout.jar:lib/junit-3.8.1.jar:lib/metabolic-efm-all.jar:lib/poi-3.1-FINAL-20080629.jar

.java.class:
	$(JAVAC) $(JAVAC_FLAGS) $<

##############################################################################
# definition of objects and their directories                                #
##############################################################################
# thermodynamic

DIR_THERMODYNAMIC = at/acib/thermodynamic
OBJ_THERMODYNAMIC = $(DIR_THERMODYNAMIC)/Thermodynamic.class \
                    $(DIR_THERMODYNAMIC)/ThermodynamicParameters.class

DIR_THERMO_CHECK = at/acib/thermodynamic/check
OBJ_THERMO_CHECK = $(DIR_THERMO_CHECK)/ActiveReaction.class \
                   $(DIR_THERMO_CHECK)/CplexHandler.class \
		   $(DIR_THERMO_CHECK)/GibbsCalculator.class \
                   $(DIR_THERMO_CHECK)/InfoWriter.class \
                   $(DIR_THERMO_CHECK)/InputHandler.class \
		   $(DIR_THERMO_CHECK)/LpFormation.class \
		   $(DIR_THERMO_CHECK)/LpReaction.class \
		   $(DIR_THERMO_CHECK)/Metabolite.class \
		   $(DIR_THERMO_CHECK)/PatternConverter.class \
		   $(DIR_THERMO_CHECK)/PredefinedReaction.class \
		   $(DIR_THERMO_CHECK)/ReactionBuildHelper.class \
		   $(DIR_THERMO_CHECK)/Species.class \
		   $(DIR_THERMO_CHECK)/SpeciesDataReader.class \
		   $(DIR_THERMO_CHECK)/StaticPatternContainer.class \
		   $(DIR_THERMO_CHECK)/ThermoChecker.class \
		   $(DIR_THERMO_CHECK)/ThermoEfmCheck.class \
		   $(DIR_THERMO_CHECK)/ThermoMemCheckerThread.class

DIR_THERMO_EXCEPTION = at/acib/thermodynamic/exception
OBJ_THERMO_EXCEPTION = $(DIR_THERMO_EXCEPTION)/ConcentrationErrorException.class

# metabolic

DIR_METABOLIC_ROOT = ch/javasoft/metabolic
OBJ_METABOLIC_ROOT = $(DIR_METABOLIC_ROOT)/Annotateable.class \
                     $(DIR_METABOLIC_ROOT)/AnnotateableMetabolicNetwork.class \
                     $(DIR_METABOLIC_ROOT)/Annotation.class \
                     $(DIR_METABOLIC_ROOT)/FluxDistribution.class \
                     $(DIR_METABOLIC_ROOT)/MetabolicNetwork.class \
                     $(DIR_METABOLIC_ROOT)/MetabolicNetworkVisitor.class \
                     $(DIR_METABOLIC_ROOT)/Metabolite.class \
                     $(DIR_METABOLIC_ROOT)/MetaboliteRatio.class \
                     $(DIR_METABOLIC_ROOT)/Norm.class \
                     $(DIR_METABOLIC_ROOT)/ReactionConstraints.class \
                     $(DIR_METABOLIC_ROOT)/Reaction.class

DIR_METABOLIC_COMPARTMENT = ch/javasoft/metabolic/compartment
OBJ_METABOLIC_COMPARTMENT = $(DIR_METABOLIC_COMPARTMENT)/CompartmentMetabolicNetwork.class \
                            $(DIR_METABOLIC_COMPARTMENT)/CompartmentMetabolite.class \
                            $(DIR_METABOLIC_COMPARTMENT)/CompartmentMetaboliteRatio.class \
                            $(DIR_METABOLIC_COMPARTMENT)/CompartmentReaction.class

DIR_METABOLIC_COMPRESS_ROOT = ch/javasoft/metabolic/compress
OBJ_METABOLIC_COMPRESS_ROOT = $(DIR_METABOLIC_COMPRESS_ROOT)/CompressedMetabolicNetwork.class \
                              $(DIR_METABOLIC_COMPRESS_ROOT)/CompressionMethod.class \
                              $(DIR_METABOLIC_COMPRESS_ROOT)/CompressionStatistics.class \
                              $(DIR_METABOLIC_COMPRESS_ROOT)/CompressionUtil.class \
                              $(DIR_METABOLIC_COMPRESS_ROOT)/DuplicateGeneCompressor.class \
                              $(DIR_METABOLIC_COMPRESS_ROOT)/LogPkg.class \
                              $(DIR_METABOLIC_COMPRESS_ROOT)/StoichMatrixCompressedMetabolicNetwork.class \
                              $(DIR_METABOLIC_COMPRESS_ROOT)/StoichMatrixCompressor.class

DIR_METABOLIC_COMPRESS_CONFIG = ch/javasoft/metabolic/compress/config
OBJ_METABOLIC_COMPRESS_CONFIG = $(DIR_METABOLIC_COMPRESS_CONFIG)/MetabolicCompressionConfig.class \
                                $(DIR_METABOLIC_COMPRESS_CONFIG)/XmlAttribute.class \
                                $(DIR_METABOLIC_COMPRESS_CONFIG)/XmlConfigException.class \
                                $(DIR_METABOLIC_COMPRESS_CONFIG)/XmlElement.class

DIR_METABOLIC_COMPRESS_GENERATE = ch/javasoft/metabolic/compress/generate
OBJ_METABOLIC_COMPRESS_GENERATE = $(DIR_METABOLIC_COMPRESS_GENERATE)/LogPkg.class \
                                  $(DIR_METABOLIC_COMPRESS_GENERATE)/MatlabGenerator.class

DIR_METABOLIC_CONVERT = ch/javasoft/metabolic/convert
OBJ_METABOLIC_CONVERT = $(DIR_METABOLIC_CONVERT)/Convert2Matlab.class \
                        $(DIR_METABOLIC_CONVERT)/Convert2Sbml.class \
                        $(DIR_METABOLIC_CONVERT)/Convert.class \
                        $(DIR_METABOLIC_CONVERT)/LogPkg.class

DIR_METABOLIC_EFM_ROOT = ch/javasoft/metabolic/efm
OBJ_METABOLIC_EFM_ROOT = $(DIR_METABOLIC_EFM_ROOT)/AnneMatthias.class \
                         $(DIR_METABOLIC_EFM_ROOT)/AnneTest.class \
                         $(DIR_METABOLIC_EFM_ROOT)/CddExtTest.class \
                         $(DIR_METABOLIC_EFM_ROOT)/CddHelper.class \
                         $(DIR_METABOLIC_EFM_ROOT)/CddIneTest.class \
                         $(DIR_METABOLIC_EFM_ROOT)/ColiTest.class \
                         $(DIR_METABOLIC_EFM_ROOT)/ElementaryFluxModes.class \
                         $(DIR_METABOLIC_EFM_ROOT)/ExcelTest.class \
                         $(DIR_METABOLIC_EFM_ROOT)/LogPkg.class \
                         $(DIR_METABOLIC_EFM_ROOT)/LpTest.class \
                         $(DIR_METABOLIC_EFM_ROOT)/PalssonTest.class \
                         $(DIR_METABOLIC_EFM_ROOT)/SantosTest.class \
                         $(DIR_METABOLIC_EFM_ROOT)/SbmlTest.class \
                         $(DIR_METABOLIC_EFM_ROOT)/SmallTest.class

DIR_METABOLIC_EFM_ADJ_ROOT = ch/javasoft/metabolic/efm/adj
OBJ_METABOLIC_EFM_ADJ_ROOT = $(DIR_METABOLIC_EFM_ADJ_ROOT)/AbstractAdjEnum.class \
                             $(DIR_METABOLIC_EFM_ADJ_ROOT)/AbstractModIntPrimeAdjEnum.class \
                             $(DIR_METABOLIC_EFM_ADJ_ROOT)/AdjEnum.class \
                             $(DIR_METABOLIC_EFM_ADJ_ROOT)/AdjMethodFactory.class \
                             $(DIR_METABOLIC_EFM_ADJ_ROOT)/LogPkg.class \
                             $(DIR_METABOLIC_EFM_ADJ_ROOT)/ModIntPrimeInCoreAdjEnum.class \
                             $(DIR_METABOLIC_EFM_ADJ_ROOT)/ModIntPrimeOutCoreAdjEnum.class \
                             $(DIR_METABOLIC_EFM_ADJ_ROOT)/SearchInCoreAdjEnum.class

DIR_METABOLIC_EFM_ADJ_INCORE_ROOT = ch/javasoft/metabolic/efm/adj/incore
OBJ_METABOLIC_EFM_ADJ_INCORE_ROOT = $(DIR_METABOLIC_EFM_ADJ_INCORE_ROOT)/AbstractAdjacencyEnumerator.class \
                                    $(DIR_METABOLIC_EFM_ADJ_INCORE_ROOT)/AbstractSearchAdjacencyEnumerator.class \
                                    $(DIR_METABOLIC_EFM_ADJ_INCORE_ROOT)/AbstractStoichMappingAdjacencyEnumerator.class \
                                    $(DIR_METABOLIC_EFM_ADJ_INCORE_ROOT)/DefaultRankTestAdjacencyEnumerator.class \
                                    $(DIR_METABOLIC_EFM_ADJ_INCORE_ROOT)/FastRankTestAdjacencyEnumerator.class \
                                    $(DIR_METABOLIC_EFM_ADJ_INCORE_ROOT)/LinearSearchAdjacencyEnumerator.class \
                                    $(DIR_METABOLIC_EFM_ADJ_INCORE_ROOT)/ModRankTestAdjacencyEnumerator.class \
                                    $(DIR_METABOLIC_EFM_ADJ_INCORE_ROOT)/NewRankTestAdjacencyEnumerator.class \
                                    $(DIR_METABOLIC_EFM_ADJ_INCORE_ROOT)/PatternTreeSearchAdjacencyEnumerator.class \
                                    $(DIR_METABOLIC_EFM_ADJ_INCORE_ROOT)/RankAdjacencyEnumerator.class \
                                    $(DIR_METABOLIC_EFM_ADJ_INCORE_ROOT)/SvdRankTestAdjacencyEnumerator.class

DIR_METABOLIC_EFM_ADJ_INCORE_TREE_ROOT = ch/javasoft/metabolic/efm/adj/incore/tree
OBJ_METABOLIC_EFM_ADJ_INCORE_TREE_ROOT = $(DIR_METABOLIC_EFM_ADJ_INCORE_TREE_ROOT)/AbstractPatternTreeRankAdjacencyEnumerator.class \
                                         $(DIR_METABOLIC_EFM_ADJ_INCORE_TREE_ROOT)/AbstractRoot.class \
                                         $(DIR_METABOLIC_EFM_ADJ_INCORE_TREE_ROOT)/AbstractTreeFactory.class \
                                         $(DIR_METABOLIC_EFM_ADJ_INCORE_TREE_ROOT)/DefaultTreeFactory.class \
                                         $(DIR_METABOLIC_EFM_ADJ_INCORE_TREE_ROOT)/EmptyLeaf.class \
                                         $(DIR_METABOLIC_EFM_ADJ_INCORE_TREE_ROOT)/InterNode.class \
                                         $(DIR_METABOLIC_EFM_ADJ_INCORE_TREE_ROOT)/JobQueue.class \
                                         $(DIR_METABOLIC_EFM_ADJ_INCORE_TREE_ROOT)/JobScheduleMultiThreadTreeFactory.class \
                                         $(DIR_METABOLIC_EFM_ADJ_INCORE_TREE_ROOT)/Leaf.class \
                                         $(DIR_METABOLIC_EFM_ADJ_INCORE_TREE_ROOT)/LogLogInterNode.class \
                                         $(DIR_METABOLIC_EFM_ADJ_INCORE_TREE_ROOT)/LogPkg.class \
                                         $(DIR_METABOLIC_EFM_ADJ_INCORE_TREE_ROOT)/Node.class \
                                         $(DIR_METABOLIC_EFM_ADJ_INCORE_TREE_ROOT)/PoolToken.class \
                                         $(DIR_METABOLIC_EFM_ADJ_INCORE_TREE_ROOT)/PoolTreeFactory.class \
                                         $(DIR_METABOLIC_EFM_ADJ_INCORE_TREE_ROOT)/Root.class \
                                         $(DIR_METABOLIC_EFM_ADJ_INCORE_TREE_ROOT)/SemIncMultiThreadTreeFactory.class \
                                         $(DIR_METABOLIC_EFM_ADJ_INCORE_TREE_ROOT)/ThreadPoolToken.class \
                                         $(DIR_METABOLIC_EFM_ADJ_INCORE_TREE_ROOT)/Traverser.class \
                                         $(DIR_METABOLIC_EFM_ADJ_INCORE_TREE_ROOT)/TreeFactory.class \
                                         $(DIR_METABOLIC_EFM_ADJ_INCORE_TREE_ROOT)/UnaryLeaf.class

DIR_METABOLIC_EFM_ADJ_INCORE_TREE_RANK = ch/javasoft/metabolic/efm/adj/incore/tree/rank
OBJ_METABOLIC_EFM_ADJ_INCORE_TREE_RANK = $(DIR_METABOLIC_EFM_ADJ_INCORE_TREE_RANK)/PatternTreeModRankAdjacencyEnumerator.class \
                                         $(DIR_METABOLIC_EFM_ADJ_INCORE_TREE_RANK)/PatternTreeRankAdjacencyEnumerator.class \
                                         $(DIR_METABOLIC_EFM_ADJ_INCORE_TREE_RANK)/RankRoot.class

DIR_METABOLIC_EFM_ADJ_INCORE_TREE_SEARCH = ch/javasoft/metabolic/efm/adj/incore/tree/search
OBJ_METABOLIC_EFM_ADJ_INCORE_TREE_SEARCH = $(DIR_METABOLIC_EFM_ADJ_INCORE_TREE_SEARCH)/LinearSearchRoot.class \
                                           $(DIR_METABOLIC_EFM_ADJ_INCORE_TREE_SEARCH)/PatternTreeLinearSearchAdjacencyEnumerator.class \
                                           $(DIR_METABOLIC_EFM_ADJ_INCORE_TREE_SEARCH)/PatternTreeLogLogAdjacencyEnumerator.class \
                                           $(DIR_METABOLIC_EFM_ADJ_INCORE_TREE_SEARCH)/PatternTreeMinZerosAdjacencyEnumerator.class \
                                           $(DIR_METABOLIC_EFM_ADJ_INCORE_TREE_SEARCH)/SearchRoot.class \
                                           $(DIR_METABOLIC_EFM_ADJ_INCORE_TREE_SEARCH)/TestMethod.class

DIR_METABOLIC_EFM_ADJ_INCORE_TREE_URANK_ROOT = ch/javasoft/metabolic/efm/adj/incore/tree/urank
OBJ_METABOLIC_EFM_ADJ_INCORE_TREE_URANK_ROOT = $(DIR_METABOLIC_EFM_ADJ_INCORE_TREE_URANK_ROOT)/RankUpdateJobScheduleTreeFactory.class \
                                               $(DIR_METABOLIC_EFM_ADJ_INCORE_TREE_URANK_ROOT)/RankUpdateRoot.class \
                                               $(DIR_METABOLIC_EFM_ADJ_INCORE_TREE_URANK_ROOT)/RankUpdateTreeFactory.class \
                                               $(DIR_METABOLIC_EFM_ADJ_INCORE_TREE_URANK_ROOT)/SearchAndRankUpdateRoot.class

DIR_METABOLIC_EFM_ADJ_INCORE_TREE_URANK_DBL = ch/javasoft/metabolic/efm/adj/incore/tree/urank/dbl
OBJ_METABOLIC_EFM_ADJ_INCORE_TREE_URANK_DBL = $(DIR_METABOLIC_EFM_ADJ_INCORE_TREE_URANK_DBL)/DoublePatternTreeRankUpdateAdjacencyEnumerator.class \
                                              $(DIR_METABOLIC_EFM_ADJ_INCORE_TREE_URANK_DBL)/DoublePreprocessedMatrix.class \
                                              $(DIR_METABOLIC_EFM_ADJ_INCORE_TREE_URANK_DBL)/DoubleRankUpdateTreeFactory.class

DIR_METABOLIC_EFM_ADJ_INCORE_TREE_URANK_DBL2 = ch/javasoft/metabolic/efm/adj/incore/tree/urank/dbl2
OBJ_METABOLIC_EFM_ADJ_INCORE_TREE_URANK_DBL2 = $(DIR_METABOLIC_EFM_ADJ_INCORE_TREE_URANK_DBL2)/Double2PatternTreeRankUpdateAdjacencyEnumerator.class \
                                               $(DIR_METABOLIC_EFM_ADJ_INCORE_TREE_URANK_DBL2)/Double2PreprocessedMatrix.class \
                                               $(DIR_METABOLIC_EFM_ADJ_INCORE_TREE_URANK_DBL2)/Double2RankUpdateTreeFactory.class

DIR_METABOLIC_EFM_ADJ_INCORE_TREE_URANK_FRAC = ch/javasoft/metabolic/efm/adj/incore/tree/urank/frac
OBJ_METABOLIC_EFM_ADJ_INCORE_TREE_URANK_FRAC = $(DIR_METABOLIC_EFM_ADJ_INCORE_TREE_URANK_FRAC)/FractionalPatternTreeRankUpdateAdjacencyEnumerator.class \
                                               $(DIR_METABOLIC_EFM_ADJ_INCORE_TREE_URANK_FRAC)/FractionalPreprocessedMatrix.class \
                                               $(DIR_METABOLIC_EFM_ADJ_INCORE_TREE_URANK_FRAC)/FractionalRankUpdateTreeFactory.class

DIR_METABOLIC_EFM_ADJ_INCORE_TREE_URANK_FRAC2 = ch/javasoft/metabolic/efm/adj/incore/tree/urank/frac2
OBJ_METABOLIC_EFM_ADJ_INCORE_TREE_URANK_FRAC2 = $(DIR_METABOLIC_EFM_ADJ_INCORE_TREE_URANK_FRAC2)/Fractional2PatternTreeRankUpdateAdjacencyEnumerator.class \
                                                $(DIR_METABOLIC_EFM_ADJ_INCORE_TREE_URANK_FRAC2)/Fractional2PreprocessedMatrix.class \
                                                $(DIR_METABOLIC_EFM_ADJ_INCORE_TREE_URANK_FRAC2)/Fractional2RankUpdateTreeFactory.class

DIR_METABOLIC_EFM_ADJ_INCORE_TREE_URANK_MODP = ch/javasoft/metabolic/efm/adj/incore/tree/urank/modp
OBJ_METABOLIC_EFM_ADJ_INCORE_TREE_URANK_MODP = $(DIR_METABOLIC_EFM_ADJ_INCORE_TREE_URANK_MODP)/ModPrimePatternTreeRankUpdateAdjacencyEnumerator.class \
                                               $(DIR_METABOLIC_EFM_ADJ_INCORE_TREE_URANK_MODP)/ModPrimePreprocessedMatrix.class \
                                               $(DIR_METABOLIC_EFM_ADJ_INCORE_TREE_URANK_MODP)/ModPrimeRankUpdateTreeFactory.class

DIR_METABOLIC_EFM_ADJ_INCORE_TREE_URANK_MODPI = ch/javasoft/metabolic/efm/adj/incore/tree/urank/modpi
OBJ_METABOLIC_EFM_ADJ_INCORE_TREE_URANK_MODPI = $(DIR_METABOLIC_EFM_ADJ_INCORE_TREE_URANK_MODPI)/ModIntPrimePatternTreeRankUpdateAdjacencyEnumerator.class \
                                                $(DIR_METABOLIC_EFM_ADJ_INCORE_TREE_URANK_MODPI)/ModIntPrimeRankUpdateTreeFactory.class

DIR_METABOLIC_EFM_BORNDIE_ROOT = ch/javasoft/metabolic/efm/borndie
OBJ_METABOLIC_EFM_BORNDIE_ROOT = $(DIR_METABOLIC_EFM_BORNDIE_ROOT)/BornDieController.class \
                                 $(DIR_METABOLIC_EFM_BORNDIE_ROOT)/BornDieDoubleDescriptionImpl.class \
                                 $(DIR_METABOLIC_EFM_BORNDIE_ROOT)/LogPkg.class

DIR_METABOLIC_EFM_BORNDIE_DEBUG = ch/javasoft/metabolic/efm/borndie/debug
OBJ_METABOLIC_EFM_BORNDIE_DEBUG = $(DIR_METABOLIC_EFM_BORNDIE_DEBUG)/Debugger.class \
                                  $(DIR_METABOLIC_EFM_BORNDIE_DEBUG)/DebuggerTableCellRenderer.class \
                                  $(DIR_METABOLIC_EFM_BORNDIE_DEBUG)/NullDebugger.class \
                                  $(DIR_METABOLIC_EFM_BORNDIE_DEBUG)/SwingDebugger.class

DIR_METABOLIC_EFM_BORNDIE_JOB = ch/javasoft/metabolic/efm/borndie/job
OBJ_METABOLIC_EFM_BORNDIE_JOB = $(DIR_METABOLIC_EFM_BORNDIE_JOB)/DefaultPairingJob.class \
                                $(DIR_METABOLIC_EFM_BORNDIE_JOB)/EnumJob.class \
                                $(DIR_METABOLIC_EFM_BORNDIE_JOB)/JobFailedException.class \
                                $(DIR_METABOLIC_EFM_BORNDIE_JOB)/JobManager.class \
                                $(DIR_METABOLIC_EFM_BORNDIE_JOB)/LogPkg.class \
                                $(DIR_METABOLIC_EFM_BORNDIE_JOB)/PairingJob.class

DIR_METABOLIC_EFM_BORNDIE_MATRIX = ch/javasoft/metabolic/efm/borndie/matrix
OBJ_METABOLIC_EFM_BORNDIE_MATRIX = $(DIR_METABOLIC_EFM_BORNDIE_MATRIX)/BornDieMatrix.class \
                                   $(DIR_METABOLIC_EFM_BORNDIE_MATRIX)/CellStage.class \
                                   $(DIR_METABOLIC_EFM_BORNDIE_MATRIX)/ConcurrentBornDieMatrix.class \
                                   $(DIR_METABOLIC_EFM_BORNDIE_MATRIX)/LogPkg.class \
                                   $(DIR_METABOLIC_EFM_BORNDIE_MATRIX)/PairingRule.class

DIR_METABOLIC_EFM_BORNDIE_MEMORY = ch/javasoft/metabolic/efm/borndie/memory
OBJ_METABOLIC_EFM_BORNDIE_MEMORY = $(DIR_METABOLIC_EFM_BORNDIE_MEMORY)/ColumnDemuxAppendableMemory.class \
                                   $(DIR_METABOLIC_EFM_BORNDIE_MEMORY)/FilteredSortablePosMemory.class

DIR_METABOLIC_EFM_BORNDIE_MODEL = ch/javasoft/metabolic/efm/borndie/model
OBJ_METABOLIC_EFM_BORNDIE_MODEL = $(DIR_METABOLIC_EFM_BORNDIE_MODEL)/BornDieColumnToFluxDistributionConverter.class \
                                  $(DIR_METABOLIC_EFM_BORNDIE_MODEL)/BornDieEfmModelFactory.class \
                                  $(DIR_METABOLIC_EFM_BORNDIE_MODEL)/BornDieEfmModel.class \
                                  $(DIR_METABOLIC_EFM_BORNDIE_MODEL)/BornDieIterationStepModel.class

DIR_METABOLIC_EFM_BORNDIE_RANGE = ch/javasoft/metabolic/efm/borndie/range
OBJ_METABOLIC_EFM_BORNDIE_RANGE = $(DIR_METABOLIC_EFM_BORNDIE_RANGE)/AbstractCellRange.class \
                                  $(DIR_METABOLIC_EFM_BORNDIE_RANGE)/AbstractRectangularRange.class \
                                  $(DIR_METABOLIC_EFM_BORNDIE_RANGE)/CellRange.class \
                                  $(DIR_METABOLIC_EFM_BORNDIE_RANGE)/ColumnRange.class \
                                  $(DIR_METABOLIC_EFM_BORNDIE_RANGE)/DefaultCellRange.class \
                                  $(DIR_METABOLIC_EFM_BORNDIE_RANGE)/DefaultColumnRange.class \
                                  $(DIR_METABOLIC_EFM_BORNDIE_RANGE)/DefaultRectangularRange.class \
                                  $(DIR_METABOLIC_EFM_BORNDIE_RANGE)/ForwardRowColumnRectangleIterator.class \
                                  $(DIR_METABOLIC_EFM_BORNDIE_RANGE)/LowerTriangularMatrix.class \
                                  $(DIR_METABOLIC_EFM_BORNDIE_RANGE)/Range.class \
                                  $(DIR_METABOLIC_EFM_BORNDIE_RANGE)/RectangularRange.class

DIR_METABOLIC_EFM_COLUMN_ROOT = ch/javasoft/metabolic/efm/column
OBJ_METABOLIC_EFM_COLUMN_ROOT = $(DIR_METABOLIC_EFM_COLUMN_ROOT)/AbstractColumn.class \
                                $(DIR_METABOLIC_EFM_COLUMN_ROOT)/AbstractHome.class \
                                $(DIR_METABOLIC_EFM_COLUMN_ROOT)/AdjCandidates.class \
                                $(DIR_METABOLIC_EFM_COLUMN_ROOT)/BigIntegerColumn.class \
                                $(DIR_METABOLIC_EFM_COLUMN_ROOT)/ColumnFactories.class \
                                $(DIR_METABOLIC_EFM_COLUMN_ROOT)/ColumnFactory.class \
                                $(DIR_METABOLIC_EFM_COLUMN_ROOT)/ColumnHome.class \
                                $(DIR_METABOLIC_EFM_COLUMN_ROOT)/Column.class \
                                $(DIR_METABOLIC_EFM_COLUMN_ROOT)/ColumnPair.class \
                                $(DIR_METABOLIC_EFM_COLUMN_ROOT)/DoubleColumn.class \
                                $(DIR_METABOLIC_EFM_COLUMN_ROOT)/FractionalColumn.class \
                                $(DIR_METABOLIC_EFM_COLUMN_ROOT)/RawBigIntegerColumn.class \
                                $(DIR_METABOLIC_EFM_COLUMN_ROOT)/VarIntColumn.class \
                                $(DIR_METABOLIC_EFM_COLUMN_ROOT)/VarIntMatrix.class

DIR_METABOLIC_EFM_COLUMN_FILTER = ch/javasoft/metabolic/efm/column/filter
OBJ_METABOLIC_EFM_COLUMN_FILTER = $(DIR_METABOLIC_EFM_COLUMN_FILTER)/ColumnFilter.class \
                                  $(DIR_METABOLIC_EFM_COLUMN_FILTER)/CompoundColumnFilter.class \
                                  $(DIR_METABOLIC_EFM_COLUMN_FILTER)/EnforcedFluxColumnFilter.class \
                                  $(DIR_METABOLIC_EFM_COLUMN_FILTER)/FutileCycleColumnFilter.class

DIR_METABOLIC_EFM_CONCURRENT = ch/javasoft/metabolic/efm/concurrent
OBJ_METABOLIC_EFM_CONCURRENT = $(DIR_METABOLIC_EFM_CONCURRENT)/AbstractConcurrentToken.class \
                               $(DIR_METABOLIC_EFM_CONCURRENT)/ConcurrentToken.class \
                               $(DIR_METABOLIC_EFM_CONCURRENT)/ImmediateReleasePolicy.class \
                               $(DIR_METABOLIC_EFM_CONCURRENT)/LogPkg.class \
                               $(DIR_METABOLIC_EFM_CONCURRENT)/RankUpdateToken.class \
                               $(DIR_METABOLIC_EFM_CONCURRENT)/ReleasePolicy.class \
                               $(DIR_METABOLIC_EFM_CONCURRENT)/SemaphoreConcurrentToken.class \
                               $(DIR_METABOLIC_EFM_CONCURRENT)/ThreadFinalizer.class \
                               $(DIR_METABOLIC_EFM_CONCURRENT)/TimeoutWaitingReleasePolicy.class \
                               $(DIR_METABOLIC_EFM_CONCURRENT)/WaitForHalfReleasePolicy.class

DIR_METABOLIC_EFM_CONFIG = ch/javasoft/metabolic/efm/config
OBJ_METABOLIC_EFM_CONFIG = $(DIR_METABOLIC_EFM_CONFIG)/Arithmetic.class \
                           $(DIR_METABOLIC_EFM_CONFIG)/Config.class \
                           $(DIR_METABOLIC_EFM_CONFIG)/DistributedConfig.class \
                           $(DIR_METABOLIC_EFM_CONFIG)/Generator.class \
                           $(DIR_METABOLIC_EFM_CONFIG)/Normalize.class \
                           $(DIR_METABOLIC_EFM_CONFIG)/XmlAttribute.class \
                           $(DIR_METABOLIC_EFM_CONFIG)/XmlElement.class

DIR_METABOLIC_EFM_DIST_ROOT = ch/javasoft/metabolic/efm/dist
OBJ_METABOLIC_EFM_DIST_ROOT = $(DIR_METABOLIC_EFM_DIST_ROOT)/DistributedAdjEnum.class \
                              $(DIR_METABOLIC_EFM_DIST_ROOT)/DistributedInfo.class \
                              $(DIR_METABOLIC_EFM_DIST_ROOT)/PartIterator.class

DIR_METABOLIC_EFM_DIST_IMPL_ROOT = ch/javasoft/metabolic/efm/dist/impl
OBJ_METABOLIC_EFM_DIST_IMPL_ROOT = $(DIR_METABOLIC_EFM_DIST_IMPL_ROOT)/ClientServerMemory.class \
                                   $(DIR_METABOLIC_EFM_DIST_IMPL_ROOT)/DistClient.class \
                                   $(DIR_METABOLIC_EFM_DIST_IMPL_ROOT)/DistJobController.class \
                                   $(DIR_METABOLIC_EFM_DIST_IMPL_ROOT)/DistributableAdjEnum.class \
                                   $(DIR_METABOLIC_EFM_DIST_IMPL_ROOT)/DistServer.class \
                                   $(DIR_METABOLIC_EFM_DIST_IMPL_ROOT)/LogPkg.class \
                                   $(DIR_METABOLIC_EFM_DIST_IMPL_ROOT)/RunningJob.class

DIR_METABOLIC_EFM_DIST_IMPL_ADJ = ch/javasoft/metabolic/efm/dist/impl/adj
OBJ_METABOLIC_EFM_DIST_IMPL_ADJ = $(DIR_METABOLIC_EFM_DIST_IMPL_ADJ)/AbstractDistributedAdjEnum.class \
                                  $(DIR_METABOLIC_EFM_DIST_IMPL_ADJ)/DistModIntPrimeInCoreAdjEnum.class \
                                  $(DIR_METABOLIC_EFM_DIST_IMPL_ADJ)/DistModIntPrimeOutCoreAdjEnum.class \
                                  $(DIR_METABOLIC_EFM_DIST_IMPL_ADJ)/LogPkg.class \
                                  $(DIR_METABOLIC_EFM_DIST_IMPL_ADJ)/MultiProcessedAdjEnum.class \
                                  $(DIR_METABOLIC_EFM_DIST_IMPL_ADJ)/MultiThreadedAdjEnum.class \
                                  $(DIR_METABOLIC_EFM_DIST_IMPL_ADJ)/PseudoDistributingAdjEnum.class

DIR_METABOLIC_EFM_DIST_IMPL_FILE = ch/javasoft/metabolic/efm/dist/impl/file
OBJ_METABOLIC_EFM_DIST_IMPL_FILE = $(DIR_METABOLIC_EFM_DIST_IMPL_FILE)/Args.class \
                                   $(DIR_METABOLIC_EFM_DIST_IMPL_FILE)/ClientServerModelPersister.class \
                                   $(DIR_METABOLIC_EFM_DIST_IMPL_FILE)/FileBasedDistJobController.class \
                                   $(DIR_METABOLIC_EFM_DIST_IMPL_FILE)/FileBasedDistJob.class \
                                   $(DIR_METABOLIC_EFM_DIST_IMPL_FILE)/FileBasedDistributableAdjEnum.class \
                                   $(DIR_METABOLIC_EFM_DIST_IMPL_FILE)/FileBasedModelPersister.class \
                                   $(DIR_METABOLIC_EFM_DIST_IMPL_FILE)/LogPkg.class

DIR_METABOLIC_EFM_IMPL = ch/javasoft/metabolic/efm/impl
OBJ_METABOLIC_EFM_IMPL = $(DIR_METABOLIC_EFM_IMPL)/AbstractDoubleDescriptionImpl.class \
                         $(DIR_METABOLIC_EFM_IMPL)/LogPkg.class \
                         $(DIR_METABOLIC_EFM_IMPL)/RecoverableSequentialDoubleDescriptionImpl.class \
                         $(DIR_METABOLIC_EFM_IMPL)/SequentialDoubleDescriptionImpl.class

DIR_METABOLIC_EFM_MAIN = ch/javasoft/metabolic/efm/main
OBJ_METABOLIC_EFM_MAIN = $(DIR_METABOLIC_EFM_MAIN)/CalculateFluxModes.class

DIR_METABOLIC_EFM_MEMORY_ROOT = ch/javasoft/metabolic/efm/memory
OBJ_METABOLIC_EFM_MEMORY_ROOT = $(DIR_METABOLIC_EFM_MEMORY_ROOT)/AppendableMemory.class \
                                $(DIR_METABOLIC_EFM_MEMORY_ROOT)/ComposedIterableMemory.class \
                                $(DIR_METABOLIC_EFM_MEMORY_ROOT)/DefaultMemoryPart.class \
                                $(DIR_METABOLIC_EFM_MEMORY_ROOT)/IndexableMemory.class \
                                $(DIR_METABOLIC_EFM_MEMORY_ROOT)/IterableMemory.class \
                                $(DIR_METABOLIC_EFM_MEMORY_ROOT)/MappedSortableMemory.class \
                                $(DIR_METABOLIC_EFM_MEMORY_ROOT)/MemoryFactory.class \
                                $(DIR_METABOLIC_EFM_MEMORY_ROOT)/MemoryPart.class \
                                $(DIR_METABOLIC_EFM_MEMORY_ROOT)/PartId.class \
                                $(DIR_METABOLIC_EFM_MEMORY_ROOT)/ReadWriteMemory.class \
                                $(DIR_METABOLIC_EFM_MEMORY_ROOT)/SortableMemory.class

DIR_METABOLIC_EFM_MEMORY_INCORE = ch/javasoft/metabolic/efm/memory/incore
OBJ_METABOLIC_EFM_MEMORY_INCORE = $(DIR_METABOLIC_EFM_MEMORY_INCORE)/InCoreAppendableMemory.class \
                                  $(DIR_METABOLIC_EFM_MEMORY_INCORE)/InCoreMemoryFactory.class \
                                  $(DIR_METABOLIC_EFM_MEMORY_INCORE)/InCoreMemory.class \
                                  $(DIR_METABOLIC_EFM_MEMORY_INCORE)/LogPkg.class

DIR_METABOLIC_EFM_MEMORY_OUTCORE = ch/javasoft/metabolic/efm/memory/outcore
OBJ_METABOLIC_EFM_MEMORY_OUTCORE = $(DIR_METABOLIC_EFM_MEMORY_OUTCORE)/Cache.class \
                                   $(DIR_METABOLIC_EFM_MEMORY_OUTCORE)/LogPkg.class \
                                   $(DIR_METABOLIC_EFM_MEMORY_OUTCORE)/OutOfCoreMemoryFactory.class \
                                   $(DIR_METABOLIC_EFM_MEMORY_OUTCORE)/OutOfCoreMemory.class \
                                   $(DIR_METABOLIC_EFM_MEMORY_OUTCORE)/Recovery.class \
                                   $(DIR_METABOLIC_EFM_MEMORY_OUTCORE)/SortInCoreOutOfCoreMemoryFactory.class

DIR_METABOLIC_EFM_MODEL_ROOT = ch/javasoft/metabolic/efm/model
OBJ_METABOLIC_EFM_MODEL_ROOT = $(DIR_METABOLIC_EFM_MODEL_ROOT)/AbstractColumnInspectorModifier.class \
                               $(DIR_METABOLIC_EFM_MODEL_ROOT)/AbstractColumnToFluxDistributionConverter.class \
                               $(DIR_METABOLIC_EFM_MODEL_ROOT)/AbstractModelPersister.class \
                               $(DIR_METABOLIC_EFM_MODEL_ROOT)/AbstractNetworkEfmModel.class \
                               $(DIR_METABOLIC_EFM_MODEL_ROOT)/AdjEnumModel.class \
                               $(DIR_METABOLIC_EFM_MODEL_ROOT)/ColumnInspectorModifierFactory.class \
                               $(DIR_METABOLIC_EFM_MODEL_ROOT)/ColumnInspectorModifier.class \
                               $(DIR_METABOLIC_EFM_MODEL_ROOT)/ColumnToFluxDistributionConverter.class \
                               $(DIR_METABOLIC_EFM_MODEL_ROOT)/DefaultEfmModel.class \
                               $(DIR_METABOLIC_EFM_MODEL_ROOT)/DefaultIterationStateModel.class \
                               $(DIR_METABOLIC_EFM_MODEL_ROOT)/DefaultIterationStepModel.class \
                               $(DIR_METABOLIC_EFM_MODEL_ROOT)/EfmModelFactory.class \
                               $(DIR_METABOLIC_EFM_MODEL_ROOT)/EfmModel.class \
                               $(DIR_METABOLIC_EFM_MODEL_ROOT)/IterationStateModel.class \
                               $(DIR_METABOLIC_EFM_MODEL_ROOT)/IterationStepModel.class \
                               $(DIR_METABOLIC_EFM_MODEL_ROOT)/LogPkg.class \
                               $(DIR_METABOLIC_EFM_MODEL_ROOT)/MemoryAccessor.class \
                               $(DIR_METABOLIC_EFM_MODEL_ROOT)/ModelPersister.class \
                               $(DIR_METABOLIC_EFM_MODEL_ROOT)/NetworkEfmModel.class

DIR_METABOLIC_EFM_MODEL_CANONICAL = ch/javasoft/metabolic/efm/model/canonical
OBJ_METABOLIC_EFM_MODEL_CANONICAL = $(DIR_METABOLIC_EFM_MODEL_CANONICAL)/AbstractCanonicalColumnInspectorModifier.class \
                                    $(DIR_METABOLIC_EFM_MODEL_CANONICAL)/CanonicalBigIntegerColumnInspectorModifier.class \
                                    $(DIR_METABOLIC_EFM_MODEL_CANONICAL)/CanonicalColumnToFluxDistributionConverter.class \
                                    $(DIR_METABOLIC_EFM_MODEL_CANONICAL)/CanonicalDoubleColumnInspectorModifier.class \
                                    $(DIR_METABOLIC_EFM_MODEL_CANONICAL)/CanonicalEfmModelFactory.class \
                                    $(DIR_METABOLIC_EFM_MODEL_CANONICAL)/CanonicalEfmModel.class \
                                    $(DIR_METABOLIC_EFM_MODEL_CANONICAL)/CanonicalFractionalColumnInspectorModifier.class \
                                    $(DIR_METABOLIC_EFM_MODEL_CANONICAL)/CanonicalVarIntColumnInspectorModifier.class \
                                    $(DIR_METABOLIC_EFM_MODEL_CANONICAL)/LogPkg.class

DIR_METABOLIC_EFM_MODEL_NULLSPACE = ch/javasoft/metabolic/efm/model/nullspace
OBJ_METABOLIC_EFM_MODEL_NULLSPACE = $(DIR_METABOLIC_EFM_MODEL_NULLSPACE)/AbstractNullspaceColumnInspectorModifier.class \
                                    $(DIR_METABOLIC_EFM_MODEL_NULLSPACE)/CannotReconstructFluxException.class \
                                    $(DIR_METABOLIC_EFM_MODEL_NULLSPACE)/LogPkg.class \
                                    $(DIR_METABOLIC_EFM_MODEL_NULLSPACE)/NullspaceBigIntegerColumnInspectorModifier.class \
                                    $(DIR_METABOLIC_EFM_MODEL_NULLSPACE)/NullspaceColumnToFluxDistributionConverter.class \
                                    $(DIR_METABOLIC_EFM_MODEL_NULLSPACE)/NullspaceDoubleColumnInspectorModifier.class \
                                    $(DIR_METABOLIC_EFM_MODEL_NULLSPACE)/NullspaceEfmModelFactory.class \
                                    $(DIR_METABOLIC_EFM_MODEL_NULLSPACE)/NullspaceEfmModel.class \
                                    $(DIR_METABOLIC_EFM_MODEL_NULLSPACE)/NullspaceFractionalColumnInspectorModifier.class \
                                    $(DIR_METABOLIC_EFM_MODEL_NULLSPACE)/NullspaceVarIntColumnInspectorModifier.class

DIR_METABOLIC_EFM_OUTPUT_ROOT = ch/javasoft/metabolic/efm/output
OBJ_METABOLIC_EFM_OUTPUT_ROOT = $(DIR_METABOLIC_EFM_OUTPUT_ROOT)/AbstractFormattedOutputCallback.class \
                                $(DIR_METABOLIC_EFM_OUTPUT_ROOT)/AbstractOutputCallback.class \
                                $(DIR_METABOLIC_EFM_OUTPUT_ROOT)/ByteEncodedOutputCallback.class \
                                $(DIR_METABOLIC_EFM_OUTPUT_ROOT)/ByteEncodedOutputFormatter.class \
                                $(DIR_METABOLIC_EFM_OUTPUT_ROOT)/CallbackGranularity.class \
                                $(DIR_METABOLIC_EFM_OUTPUT_ROOT)/CountOutputCallback.class \
                                $(DIR_METABOLIC_EFM_OUTPUT_ROOT)/EfmOutputCallback.class \
                                $(DIR_METABOLIC_EFM_OUTPUT_ROOT)/EfmOutputEvent.class \
                                $(DIR_METABOLIC_EFM_OUTPUT_ROOT)/EfmOutputFormatter.class \
                                $(DIR_METABOLIC_EFM_OUTPUT_ROOT)/EfmProcessor.class \
                                $(DIR_METABOLIC_EFM_OUTPUT_ROOT)/LogPkg.class \
                                $(DIR_METABOLIC_EFM_OUTPUT_ROOT)/MatlabOutputFormatter.class \
                                $(DIR_METABOLIC_EFM_OUTPUT_ROOT)/NullOutputCallback.class \
                                $(DIR_METABOLIC_EFM_OUTPUT_ROOT)/OptimizerOutputCallback.class \
                                $(DIR_METABOLIC_EFM_OUTPUT_ROOT)/OutputMode.class \
                                $(DIR_METABOLIC_EFM_OUTPUT_ROOT)/RandomAccessFileOutputCallback.class \
                                $(DIR_METABOLIC_EFM_OUTPUT_ROOT)/RandomAccessFileOutputFormatter.class \
                                $(DIR_METABOLIC_EFM_OUTPUT_ROOT)/TextOutputCallback.class \
                                $(DIR_METABOLIC_EFM_OUTPUT_ROOT)/UnmappingEfmOutputFormatter.class \
                                $(DIR_METABOLIC_EFM_OUTPUT_ROOT)/UnmappingEfmProcessor.class \
                                $(DIR_METABOLIC_EFM_OUTPUT_ROOT)/Util.class

DIR_METABOLIC_EFM_OUTPUT_MAT = ch/javasoft/metabolic/efm/output/mat
OBJ_METABOLIC_EFM_OUTPUT_MAT = $(DIR_METABOLIC_EFM_OUTPUT_MAT)/DefaultPartitionedMatFileWriter.class \
                               $(DIR_METABOLIC_EFM_OUTPUT_MAT)/LogPkg.class \
                               $(DIR_METABOLIC_EFM_OUTPUT_MAT)/MatFileOutputCallback.class \
                               $(DIR_METABOLIC_EFM_OUTPUT_MAT)/MatFileOutputFormatter.class \
                               $(DIR_METABOLIC_EFM_OUTPUT_MAT)/MatFileWriter.class \
                               $(DIR_METABOLIC_EFM_OUTPUT_MAT)/MatReservedVariableEfmProcessor.class \
                               $(DIR_METABOLIC_EFM_OUTPUT_MAT)/PartitionedMatFileWriter.class

DIR_METABOLIC_EFM_OUTPUT_TEXT = ch/javasoft/metabolic/efm/output/text
OBJ_METABOLIC_EFM_OUTPUT_TEXT = $(DIR_METABOLIC_EFM_OUTPUT_TEXT)/AbstractTextOutputFormatter.class \
                                $(DIR_METABOLIC_EFM_OUTPUT_TEXT)/BinaryTextOutputFormatter.class \
                                $(DIR_METABOLIC_EFM_OUTPUT_TEXT)/DoubleTextOutputFormatter.class \
                                $(DIR_METABOLIC_EFM_OUTPUT_TEXT)/NumberTextOutputFormatter.class \
                                $(DIR_METABOLIC_EFM_OUTPUT_TEXT)/SignTextOutputFormatter.class

DIR_METABOLIC_EFM_PROGRESS = ch/javasoft/metabolic/efm/progress
OBJ_METABOLIC_EFM_PROGRESS = $(DIR_METABOLIC_EFM_PROGRESS)/AbstractStringProgressWriter.class \
                             $(DIR_METABOLIC_EFM_PROGRESS)/FileProgressWriter.class \
                             $(DIR_METABOLIC_EFM_PROGRESS)/IntProgressAggregator.class \
                             $(DIR_METABOLIC_EFM_PROGRESS)/JProgress.class \
                             $(DIR_METABOLIC_EFM_PROGRESS)/ProgressAggregator.class \
                             $(DIR_METABOLIC_EFM_PROGRESS)/ProgressMonitor.class \
                             $(DIR_METABOLIC_EFM_PROGRESS)/ProgressNotifiable.class \
                             $(DIR_METABOLIC_EFM_PROGRESS)/ProgressType.class

DIR_METABOLIC_EFM_RANKUP_ROOT = ch/javasoft/metabolic/efm/rankup
OBJ_METABOLIC_EFM_RANKUP_ROOT = $(DIR_METABOLIC_EFM_RANKUP_ROOT)/PreprocessableMatrix.class \
                                $(DIR_METABOLIC_EFM_RANKUP_ROOT)/PreprocessedMatrixFactory.class \
                                $(DIR_METABOLIC_EFM_RANKUP_ROOT)/PreprocessedMatrix.class \
                                $(DIR_METABOLIC_EFM_RANKUP_ROOT)/RankUpRoot.class

DIR_METABOLIC_EFM_RANKUP_MODPI = ch/javasoft/metabolic/efm/rankup/modpi
OBJ_METABOLIC_EFM_RANKUP_MODPI = $(DIR_METABOLIC_EFM_RANKUP_MODPI)/ModIntPrimeMatrixFactory.class \
                                 $(DIR_METABOLIC_EFM_RANKUP_MODPI)/ModIntPrimePreprocessedMatrix.class

DIR_METABOLIC_EFM_SORT = ch/javasoft/metabolic/efm/sort
OBJ_METABOLIC_EFM_SORT = $(DIR_METABOLIC_EFM_SORT)/AbsLexMinSorter.class \
                         $(DIR_METABOLIC_EFM_SORT)/CascadingSorter.class \
                         $(DIR_METABOLIC_EFM_SORT)/FewestNegPosSorter.class \
                         $(DIR_METABOLIC_EFM_SORT)/FewestZerosSorter.class \
                         $(DIR_METABOLIC_EFM_SORT)/LexMinSorter.class \
                         $(DIR_METABOLIC_EFM_SORT)/LogPkg.class \
                         $(DIR_METABOLIC_EFM_SORT)/MatrixSorter.class \
                         $(DIR_METABOLIC_EFM_SORT)/MostZerosSorter.class \
                         $(DIR_METABOLIC_EFM_SORT)/ReversibleReactionsLastSorter.class \
                         $(DIR_METABOLIC_EFM_SORT)/RowColSorter.class \
                         $(DIR_METABOLIC_EFM_SORT)/SortUtil.class \
                         $(DIR_METABOLIC_EFM_SORT)/SuppressedEnforcedNoSplitSorter.class

DIR_METABOLIC_EFM_STRESS = ch/javasoft/metabolic/efm/stress
OBJ_METABOLIC_EFM_STRESS = $(DIR_METABOLIC_EFM_STRESS)/StressTest.class

DIR_METABOLIC_EFM_TREE_ROOT = ch/javasoft/metabolic/efm/tree
OBJ_METABOLIC_EFM_TREE_ROOT = $(DIR_METABOLIC_EFM_TREE_ROOT)/AdjacencyFilter.class \
                              $(DIR_METABOLIC_EFM_TREE_ROOT)/AdjacencyPrecondition.class \
                              $(DIR_METABOLIC_EFM_TREE_ROOT)/BitPatternTree.class \
                              $(DIR_METABOLIC_EFM_TREE_ROOT)/InterNode.class \
                              $(DIR_METABOLIC_EFM_TREE_ROOT)/LeafNode.class \
                              $(DIR_METABOLIC_EFM_TREE_ROOT)/Node.class \
                              $(DIR_METABOLIC_EFM_TREE_ROOT)/Partition.class \
                              $(DIR_METABOLIC_EFM_TREE_ROOT)/TreeMemAdjEnum.class \
                              $(DIR_METABOLIC_EFM_TREE_ROOT)/TreePairTraverser.class \
                              $(DIR_METABOLIC_EFM_TREE_ROOT)/TreeTraverser.class

DIR_METABOLIC_EFM_TREE_CONCURRENT = ch/javasoft/metabolic/efm/tree/concurrent
OBJ_METABOLIC_EFM_TREE_CONCURRENT = $(DIR_METABOLIC_EFM_TREE_CONCURRENT)/ConcurrentSubtreePairTraverser.class \
                                    $(DIR_METABOLIC_EFM_TREE_CONCURRENT)/ConcurrentTreePairTraverser.class \
                                    $(DIR_METABOLIC_EFM_TREE_CONCURRENT)/JobQueue.class

DIR_METABOLIC_EFM_TREE_IMPL = ch/javasoft/metabolic/efm/tree/impl
OBJ_METABOLIC_EFM_TREE_IMPL = $(DIR_METABOLIC_EFM_TREE_IMPL)/AbstractInterNode.class \
                              $(DIR_METABOLIC_EFM_TREE_IMPL)/AbstractLeafNode.class \
                              $(DIR_METABOLIC_EFM_TREE_IMPL)/AbstractNode.class \
                              $(DIR_METABOLIC_EFM_TREE_IMPL)/AbstractTreePairTraverser.class \
                              $(DIR_METABOLIC_EFM_TREE_IMPL)/AbstractTreeTraverser.class \
                              $(DIR_METABOLIC_EFM_TREE_IMPL)/DefaultTreePairTraverser.class \
                              $(DIR_METABOLIC_EFM_TREE_IMPL)/MinCardinalityAdjacencyPrecondition.class \
                              $(DIR_METABOLIC_EFM_TREE_IMPL)/SubtreePairTraverser.class

DIR_METABOLIC_EFM_TREE_INCORE = ch/javasoft/metabolic/efm/tree/incore
OBJ_METABOLIC_EFM_TREE_INCORE = $(DIR_METABOLIC_EFM_TREE_INCORE)/IncoreBitPatternTree.class \
                                $(DIR_METABOLIC_EFM_TREE_INCORE)/IncoreInterNode.class \
                                $(DIR_METABOLIC_EFM_TREE_INCORE)/IncoreLeafNode.class \
                                $(DIR_METABOLIC_EFM_TREE_INCORE)/InCoreNode.class

DIR_METABOLIC_EFM_TREE_OUTCORE = ch/javasoft/metabolic/efm/tree/outcore
OBJ_METABOLIC_EFM_TREE_OUTCORE = $(DIR_METABOLIC_EFM_TREE_OUTCORE)/PersistentBitPatternTree.class \
                                 $(DIR_METABOLIC_EFM_TREE_OUTCORE)/PersistentInterNode.class \
                                 $(DIR_METABOLIC_EFM_TREE_OUTCORE)/PersistentLeafNode.class \
                                 $(DIR_METABOLIC_EFM_TREE_OUTCORE)/PersistentNodeEntity.class \
                                 $(DIR_METABOLIC_EFM_TREE_OUTCORE)/PersistentNodeEntityMarshaller.class \
                                 $(DIR_METABOLIC_EFM_TREE_OUTCORE)/PersistentNode.class

DIR_METABOLIC_EFM_TREE_RANKUP = ch/javasoft/metabolic/efm/tree/rankup
OBJ_METABOLIC_EFM_TREE_RANKUP = $(DIR_METABOLIC_EFM_TREE_RANKUP)/DefaultRankUpAdjacencyFilter.class \
                                $(DIR_METABOLIC_EFM_TREE_RANKUP)/DefaultRankUpAdjacencyPrecondition.class

DIR_METABOLIC_EFM_TREE_SEARCH = ch/javasoft/metabolic/efm/tree/search
OBJ_METABOLIC_EFM_TREE_SEARCH = $(DIR_METABOLIC_EFM_TREE_SEARCH)/DefaultSearchAdjacencyFilter.class \
                                $(DIR_METABOLIC_EFM_TREE_SEARCH)/SuperSetSearch.class

DIR_METABOLIC_EFM_UTIL = ch/javasoft/metabolic/efm/util
OBJ_METABOLIC_EFM_UTIL = $(DIR_METABOLIC_EFM_UTIL)/BitSetUtil.class \
                         $(DIR_METABOLIC_EFM_UTIL)/CanonicalUtil.class \
                         $(DIR_METABOLIC_EFM_UTIL)/ColumnUtil.class \
                         $(DIR_METABOLIC_EFM_UTIL)/DualKey.class \
                         $(DIR_METABOLIC_EFM_UTIL)/EfmHelper.class \
                         $(DIR_METABOLIC_EFM_UTIL)/LogPkg.class \
                         $(DIR_METABOLIC_EFM_UTIL)/MappingUtil.class \
                         $(DIR_METABOLIC_EFM_UTIL)/MatrixUtil.class \
                         $(DIR_METABOLIC_EFM_UTIL)/ModUtil.class \
                         $(DIR_METABOLIC_EFM_UTIL)/PreconditionUtil.class \
                         $(DIR_METABOLIC_EFM_UTIL)/ReactionMapping.class \
                         $(DIR_METABOLIC_EFM_UTIL)/TempDir.class

DIR_METABOLIC_FA = ch/javasoft/metabolic/fa
OBJ_METABOLIC_FA = $(DIR_METABOLIC_FA)/FaConstants.class

DIR_METABOLIC_GENERATE = ch/javasoft/metabolic/generate$
OBJ_METABOLIC_GENERATE = $(DIR_METABOLIC_GENERATE)/CddGenerator.class \
                         $(DIR_METABOLIC_GENERATE)/ConfiguredGenerator.class \
                         $(DIR_METABOLIC_GENERATE)/ExcelGenerator.class \
                         $(DIR_METABOLIC_GENERATE)/ExcelTest.class \
                         $(DIR_METABOLIC_GENERATE)/FaColiTest.class \
                         $(DIR_METABOLIC_GENERATE)/LogPkg.class \
                         $(DIR_METABOLIC_GENERATE)/MatlabGenerator.class \
                         $(DIR_METABOLIC_GENERATE)/MatlabTest.class \
                         $(DIR_METABOLIC_GENERATE)/PalssonTest.class \
                         $(DIR_METABOLIC_GENERATE)/SbmlGenerator.class \
                         $(DIR_METABOLIC_GENERATE)/SbmlTest.class

DIR_METABOLIC_IMPL = ch/javasoft/metabolic/impl
OBJ_METABOLIC_IMPL = $(DIR_METABOLIC_IMPL)/AbstractFluxDistribution.class \
                     $(DIR_METABOLIC_IMPL)/AbstractMetabolicNetwork.class \
                     $(DIR_METABOLIC_IMPL)/AbstractNamedReaction.class \
                     $(DIR_METABOLIC_IMPL)/AbstractNestedReaction.class \
                     $(DIR_METABOLIC_IMPL)/AbstractReaction.class \
                     $(DIR_METABOLIC_IMPL)/ConstrainedReversibilitiesMetabolicNetwork.class \
                     $(DIR_METABOLIC_IMPL)/DefaultFluxDistribution.class \
                     $(DIR_METABOLIC_IMPL)/DefaultMetabolicNetwork.class \
                     $(DIR_METABOLIC_IMPL)/DefaultMetabolicNetworkVisitor.class \
                     $(DIR_METABOLIC_IMPL)/DefaultMetabolite.class \
                     $(DIR_METABOLIC_IMPL)/DefaultMetaboliteRatio.class \
                     $(DIR_METABOLIC_IMPL)/DefaultReactionConstraints.class \
                     $(DIR_METABOLIC_IMPL)/DefaultReaction.class \
                     $(DIR_METABOLIC_IMPL)/FilteredMetabolicNetwork.class \
                     $(DIR_METABOLIC_IMPL)/FractionNumberFluxDistribution.class \
                     $(DIR_METABOLIC_IMPL)/FractionNumberStoichMetabolicNetwork.class \
                     $(DIR_METABOLIC_IMPL)/NormalizedRatiosReaction.class \
                     $(DIR_METABOLIC_IMPL)/StoichMatrixMetabolicNetwork.class

DIR_METABOLIC_PARSE_ROOT = ch/javasoft/metabolic/parse
OBJ_METABOLIC_PARSE_ROOT = $(DIR_METABOLIC_PARSE_ROOT)/AnneMatthias.class \
                           $(DIR_METABOLIC_PARSE_ROOT)/AnneTest.class \
                           $(DIR_METABOLIC_PARSE_ROOT)/ConfiguredParser.class \
                           $(DIR_METABOLIC_PARSE_ROOT)/ExcelParser.class \
                           $(DIR_METABOLIC_PARSE_ROOT)/ExcelTest.class \
                           $(DIR_METABOLIC_PARSE_ROOT)/FaColiTest.class \
                           $(DIR_METABOLIC_PARSE_ROOT)/FluxAnalyserParser.class \
                           $(DIR_METABOLIC_PARSE_ROOT)/GamsParser.class \
                           $(DIR_METABOLIC_PARSE_ROOT)/JUnitTestCaseParser.class \
                           $(DIR_METABOLIC_PARSE_ROOT)/LogPkg.class \
                           $(DIR_METABOLIC_PARSE_ROOT)/PalssonParser.class \
                           $(DIR_METABOLIC_PARSE_ROOT)/PalssonTest.class \
                           $(DIR_METABOLIC_PARSE_ROOT)/SantosTest.class \
                           $(DIR_METABOLIC_PARSE_ROOT)/SbmlParser.class \
                           $(DIR_METABOLIC_PARSE_ROOT)/SbmlTest.class \
                           $(DIR_METABOLIC_PARSE_ROOT)/SmallTest.class \
                           $(DIR_METABOLIC_PARSE_ROOT)/StoichParser.class

DIR_METABOLIC_PARSE_JUNIT = ch/javasoft/metabolic/parse/junit
OBJ_METABOLIC_PARSE_JUNIT = $(DIR_METABOLIC_PARSE_JUNIT)/AbstractParseTestCase.class \
                            $(DIR_METABOLIC_PARSE_JUNIT)/TestDelegate.class

DIR_METABOLIC_SBML = ch/javasoft/metabolic/sbml
OBJ_METABOLIC_SBML = $(DIR_METABOLIC_SBML)/SbmlConstants.class

DIR_METABOLIC_UTIL = ch/javasoft/metabolic/util
OBJ_METABOLIC_UTIL = $(DIR_METABOLIC_UTIL)/FluxComparatorPerc.class \
                     $(DIR_METABOLIC_UTIL)/FluxComparatorPrec.class \
                     $(DIR_METABOLIC_UTIL)/FluxNormalizer.class \
                     $(DIR_METABOLIC_UTIL)/LinearProgramming.class \
                     $(DIR_METABOLIC_UTIL)/LogPkg.class \
                     $(DIR_METABOLIC_UTIL)/MetabolicNetworkUtil.class \
                     $(DIR_METABOLIC_UTIL)/Output.class \
                     $(DIR_METABOLIC_UTIL)/StoichiometricMatrices.class

# smx
DIR_SMX_EXCEPTION = ch/javasoft/smx/exception
OBJ_SMX_EXCEPTION = $(DIR_SMX_EXCEPTION)/SingularMatrixException.class

DIR_SMX_IFACE = ch/javasoft/smx/iface
OBJ_SMX_IFACE = $(DIR_SMX_IFACE)/BigIntegerMatrix.class \
                $(DIR_SMX_IFACE)/MatrixBase.class \
                $(DIR_SMX_IFACE)/ReadableLongRationalMatrix.class \
                $(DIR_SMX_IFACE)/WritableIntRationalMatrix.class \
                $(DIR_SMX_IFACE)/BigIntegerRationalMatrix.class \
                $(DIR_SMX_IFACE)/RationalMatrix.class \
                $(DIR_SMX_IFACE)/ReadableMatrix.class \
                $(DIR_SMX_IFACE)/WritableLongMatrix.class \
                $(DIR_SMX_IFACE)/DoubleMatrix.class \
                $(DIR_SMX_IFACE)/ReadableBigIntegerMatrix.class \
                $(DIR_SMX_IFACE)/ReadableVector.class \
                $(DIR_SMX_IFACE)/WritableLongRationalMatrix.class \
                $(DIR_SMX_IFACE)/DoubleVector.class \
                $(DIR_SMX_IFACE)/ReadableBigIntegerRationalMatrix.class \
                $(DIR_SMX_IFACE)/VectorBase.class \
                $(DIR_SMX_IFACE)/WritableMatrix.class \
                $(DIR_SMX_IFACE)/IntMatrix.class \
                $(DIR_SMX_IFACE)/ReadableDoubleMatrix.class \
                $(DIR_SMX_IFACE)/WritableBigIntegerMatrix.class \
                $(DIR_SMX_IFACE)/WritableVector.class \
                $(DIR_SMX_IFACE)/IntRationalMatrix.class \
                $(DIR_SMX_IFACE)/ReadableIntMatrix.class \
                $(DIR_SMX_IFACE)/WritableBigIntegerRationalMatrix.class \
                $(DIR_SMX_IFACE)/LongMatrix.class \
                $(DIR_SMX_IFACE)/ReadableIntRationalMatrix.class \
                $(DIR_SMX_IFACE)/WritableDoubleMatrix.class \
                $(DIR_SMX_IFACE)/LongRationalMatrix.class \
                $(DIR_SMX_IFACE)/ReadableLongMatrix.class \
                $(DIR_SMX_IFACE)/WritableIntMatrix.class

DIR_SMX_IMPL = ch/javasoft/smx/impl
OBJ_SMX_IMPL = $(DIR_SMX_IMPL)/AbstractDoubleMatrix.class \
               $(DIR_SMX_IMPL)/DefaultBigIntegerRationalMatrix.class \
               $(DIR_SMX_IMPL)/DefaultDoubleVector.class \
               $(DIR_SMX_IMPL)/DefaultLongMatrix.class \
               $(DIR_SMX_IMPL)/AbstractVector.class \
               $(DIR_SMX_IMPL)/DefaultBigIntegerVector.class \
               $(DIR_SMX_IMPL)/DefaultIntMatrix.class \
               $(DIR_SMX_IMPL)/DynamicDoubleMatrix.class \
               $(DIR_SMX_IMPL)/DefaultBigIntegerMatrix.class \
               $(DIR_SMX_IMPL)/DefaultDoubleMatrix.class \
               $(DIR_SMX_IMPL)/DefaultIntRationalMatrix.class \
               $(DIR_SMX_IMPL)/SubDoubleMatrix.class

DIR_SMX_OPS_ROOT = ch/javasoft/smx/ops
OBJ_SMX_OPS_ROOT = $(DIR_SMX_OPS_ROOT)/Add.class \
                   $(DIR_SMX_OPS_ROOT)/Gauss.class \
                   $(DIR_SMX_OPS_ROOT)/HslGateway.class \
                   $(DIR_SMX_OPS_ROOT)/Invert.class \
                   $(DIR_SMX_OPS_ROOT)/Mul.class \
                   $(DIR_SMX_OPS_ROOT)/NullspaceRank.class \
                   $(DIR_SMX_OPS_ROOT)/Sub.class \
                   $(DIR_SMX_OPS_ROOT)/ExtendedMatrixOperations.class \
                   $(DIR_SMX_OPS_ROOT)/GaussPivoting.class \
                   $(DIR_SMX_OPS_ROOT)/Hsl.class \
                   $(DIR_SMX_OPS_ROOT)/MatrixOperations.class \
                   $(DIR_SMX_OPS_ROOT)/Neg.class \
                   $(DIR_SMX_OPS_ROOT)/ScalarOps.class \
                   $(DIR_SMX_OPS_ROOT)/Transpose.class

DIR_SMX_OPS_EXT = ch/javasoft/smx/ops/ext
OBJ_SMX_OPS_EXT = $(DIR_SMX_OPS_EXT)/ExternalOpsImpl.class \
                  $(DIR_SMX_OPS_EXT)/ExternalOps.class

DIR_SMX_OPS_JLAPACK = ch/javasoft/smx/ops/jlapack
OBJ_SMX_OPS_JLAPACK = $(DIR_SMX_OPS_JLAPACK)/JLapackImpl.class

DIR_SMX_OPS_MATRIX = ch/javasoft/smx/ops/matrix
OBJ_SMX_OPS_MATRIX = $(DIR_SMX_OPS_MATRIX)/BigIntegerMatrixOperations.class \
                     $(DIR_SMX_OPS_MATRIX)/DoubleMatrixOperations.class \
                     $(DIR_SMX_OPS_MATRIX)/ExtendedIntDoubleOperations.class \
                     $(DIR_SMX_OPS_MATRIX)/IntMatrixOperations.class \
                     $(DIR_SMX_OPS_MATRIX)/BigIntegerRationalMatrixOperations.class \
                     $(DIR_SMX_OPS_MATRIX)/ExtendedDoubleOperations.class \
                     $(DIR_SMX_OPS_MATRIX)/ExtendedLongDoubleOperations.class \
                     $(DIR_SMX_OPS_MATRIX)/LongMatrixOperations.class

DIR_SMX_OPS_MT = ch/javasoft/smx/ops/mt
OBJ_SMX_OPS_MT = $(DIR_SMX_OPS_MT)/Convert.class \
                 $(DIR_SMX_OPS_MT)/LogPkg.class \
                 $(DIR_SMX_OPS_MT)/MtOpsImpl.class

DIR_SMX_OPS_SSCC = ch/javasoft/smx/ops/sscc
OBJ_SMX_OPS_SSCC = $(DIR_SMX_OPS_SSCC)/Convert.class \
                   $(DIR_SMX_OPS_SSCC)/SsccOpsImpl.class

DIR_SMX_UTIL = ch/javasoft/smx/util
OBJ_SMX_UTIL = $(DIR_SMX_UTIL)/DimensionCheck.class \
               $(DIR_SMX_UTIL)/SmxDoubleUtil.class \
               $(DIR_SMX_UTIL)/SmxIntegerUtil.class

# xml
DIR_XML_CONFIG = ch/javasoft/xml/config
OBJ_XML_CONFIG = $(DIR_XML_CONFIG)/ConstConfigParser.class \
                 $(DIR_XML_CONFIG)/MissingReferableException.class \
                 $(DIR_XML_CONFIG)/URLConfigParser.class \
                 $(DIR_XML_CONFIG)/XmlConfigException.class \
                 $(DIR_XML_CONFIG)/XmlNode.class \
                 $(DIR_XML_CONFIG)/XmlUtil.class \
                 $(DIR_XML_CONFIG)/FileConfigParser.class \
                 $(DIR_XML_CONFIG)/StreamConfigParser.class \
                 $(DIR_XML_CONFIG)/XmlArgException.class \
                 $(DIR_XML_CONFIG)/XmlConfig.class \
                 $(DIR_XML_CONFIG)/XmlPrint.class

DIR_XML_FACTORY = ch/javasoft/xml/factory
OBJ_XML_FACTORY = $(DIR_XML_FACTORY)/XmlConfiguredFactory.class \
                  $(DIR_XML_FACTORY)/XmlFactoryUtil.class

# jbase
DIR_JBASE_ROOT = ch/javasoft/jbase
OBJ_JBASE_ROOT = $(DIR_JBASE_ROOT)/BufferedRandomAccessPersister.class \
                 $(DIR_JBASE_ROOT)/FixedTableRow.class \
                 $(DIR_JBASE_ROOT)/MemoryTable.class \
                 $(DIR_JBASE_ROOT)/Table.class \
                 $(DIR_JBASE_ROOT)/ByteArray.class \
                 $(DIR_JBASE_ROOT)/FixedWidthMarshaller.class \
                 $(DIR_JBASE_ROOT)/RandomAccessFilePersistor.class \
                 $(DIR_JBASE_ROOT)/VariableWidthTable.class \
                 $(DIR_JBASE_ROOT)/EntityMarshaller.class \
                 $(DIR_JBASE_ROOT)/FixedWidthTable.class \
                 $(DIR_JBASE_ROOT)/RandomAccessPersister.class

DIR_JBASE_CONCURRENT = ch/javasoft/jbase/concurrent
OBJ_JBASE_CONCURRENT = $(DIR_JBASE_CONCURRENT)/ConcurrentTable.class \
                       $(DIR_JBASE_CONCURRENT)/MultiplexedAppendTable.class \
                       $(DIR_JBASE_CONCURRENT)/Stateful.class

DIR_JBASE_MARHSAL = ch/javasoft/jbase/marshal
OBJ_JBASE_MARHSAL = $(DIR_JBASE_MARHSAL)/BigDecimalMarshaller.class \
                    $(DIR_JBASE_MARHSAL)/PrimitiveArrayMarshallers.class \
                    $(DIR_JBASE_MARHSAL)/StringMarshaller.class \
                    $(DIR_JBASE_MARHSAL)/BigIntegerMarshaller.class \
                    $(DIR_JBASE_MARHSAL)/PrimitiveMarshallers.class \
                    $(DIR_JBASE_MARHSAL)/UtfStringMarshaller.class

DIR_JBASE_UTIL = ch/javasoft/jbase/util
OBJ_JBASE_UTIL = $(DIR_JBASE_UTIL)/AbstractDataInput.class \
                 $(DIR_JBASE_UTIL)/CachedTableSoftReference.class \
                 $(DIR_JBASE_UTIL)/TableList.class \
                 $(DIR_JBASE_UTIL)/UnsupportedOperationException.class \
                 $(DIR_JBASE_UTIL)/AbstractDataOutput.class \
                 $(DIR_JBASE_UTIL)/CachedTableWeakReference.class \
                 $(DIR_JBASE_UTIL)/Tables.class


# junit
DIR_JUNIT_TEXTUI = ch/javasoft/junit/textui
OBJ_JUNIT_TEXTUI = $(DIR_JUNIT_TEXTUI)/TestRunner.class

# math
DIR_MATH_ROOT = ch/javasoft/math
OBJ_MATH_ROOT = $(DIR_MATH_ROOT)/BigFraction.class \
                $(DIR_MATH_ROOT)/BigMath.class \
                $(DIR_MATH_ROOT)/NumberMatrixConverter.class \
                $(DIR_MATH_ROOT)/NumberOperations.class \
                $(DIR_MATH_ROOT)/Prime.class

DIR_MATH_ARRAY = ch/javasoft/math/array
OBJ_MATH_ARRAY = $(DIR_MATH_ARRAY)/ArrayOperations.class \
                 $(DIR_MATH_ARRAY)/Converter.class \
                 $(DIR_MATH_ARRAY)/ExpressionComposer.class \
                 $(DIR_MATH_ARRAY)/NumberArrayOperations.class \
                 $(DIR_MATH_ARRAY)/NumberOperators.class

DIR_MATH_ARRAY_IMPL = ch/javasoft/math/array/impl
OBJ_MATH_ARRAY_IMPL = $(DIR_MATH_ARRAY_IMPL)/AbstractArrayOperations.class \
                      $(DIR_MATH_ARRAY_IMPL)/DefaultArrayOperations.class \
                      $(DIR_MATH_ARRAY_IMPL)/DefaultNumberArrayOperations.class \
                      $(DIR_MATH_ARRAY_IMPL)/DoubleArrayOperations.class

DIR_MATH_ARRAY_PARSE = ch/javasoft/math/array/parse
OBJ_MATH_ARRAY_PARSE = $(DIR_MATH_ARRAY_PARSE)/DataType.class \
                       $(DIR_MATH_ARRAY_PARSE)/DefaultMatrixData.class \
                       $(DIR_MATH_ARRAY_PARSE)/MatrixData.class \
                       $(DIR_MATH_ARRAY_PARSE)/MatrixParser.class

DIR_MATH_ARRAY_SORT = ch/javasoft/math/array/sort
OBJ_MATH_ARRAY_SORT = $(DIR_MATH_ARRAY_SORT)/AbsLexMinArrayComparator.class \
                      $(DIR_MATH_ARRAY_SORT)/LexMinArrayComparator.class \
                      $(DIR_MATH_ARRAY_SORT)/MostZerosArrayComparator.class \
                      $(DIR_MATH_ARRAY_SORT)/FewestNegPosArrayComparator.class \
                      $(DIR_MATH_ARRAY_SORT)/MatrixSortUtil.class

DIR_MATH_LINALG = ch/javasoft/math/linalg
OBJ_MATH_LINALG = $(DIR_MATH_LINALG)/BasicLinAlgOperations.class \
                  $(DIR_MATH_LINALG)/DefaultLinAlgOperations.class \
                  $(DIR_MATH_LINALG)/GaussPivoting.class \
                  $(DIR_MATH_LINALG)/SupportBasicLinAlgOperations.class \
                  $(DIR_MATH_LINALG)/DefaultBasicLinAlgOperations.class \
                  $(DIR_MATH_LINALG)/GaussPivotingFactory.class \
                  $(DIR_MATH_LINALG)/LinAlgOperations.class

DIR_MATH_LINALG_IMPL = ch/javasoft/math/linalg/impl
OBJ_MATH_LINALG_IMPL = $(DIR_MATH_LINALG_IMPL)/BigFractionGaussPivoting.class \
                       $(DIR_MATH_LINALG_IMPL)/BigIntegerGaussPivoting.class \
                       $(DIR_MATH_LINALG_IMPL)/DoubleGaussPivoting.class

DIR_MATH_OPERATOR = ch/javasoft/math/operator
OBJ_MATH_OPERATOR = $(DIR_MATH_OPERATOR)/AbstractBinaryOperator.class \
                    $(DIR_MATH_OPERATOR)/AbstractQuaternaryOperator.class \
                    $(DIR_MATH_OPERATOR)/BooleanBinaryOperator.class \
                    $(DIR_MATH_OPERATOR)/IntUnaryOperator.class \
                    $(DIR_MATH_OPERATOR)/AbstractBooleanBinaryOperator.class \
                    $(DIR_MATH_OPERATOR)/AbstractTernaryOperator.class \
                    $(DIR_MATH_OPERATOR)/BooleanUnaryOperator.class \
                    $(DIR_MATH_OPERATOR)/NAryOperator.class \
                    $(DIR_MATH_OPERATOR)/AbstractBooleanUnaryOperator.class \
                    $(DIR_MATH_OPERATOR)/AbstractUnaryOperator.class \
                    $(DIR_MATH_OPERATOR)/ConvertingBinaryOperator.class \
                    $(DIR_MATH_OPERATOR)/NullaryOperator.class \
                    $(DIR_MATH_OPERATOR)/AbstractIntBinaryOperator.class \
                    $(DIR_MATH_OPERATOR)/AggregatingBinaryOperator.class \
                    $(DIR_MATH_OPERATOR)/ConvertingUnaryOperator.class \
                    $(DIR_MATH_OPERATOR)/QuaternaryOperator.class \
                    $(DIR_MATH_OPERATOR)/AbstractIntUnaryOperator.class \
                    $(DIR_MATH_OPERATOR)/AggregatingUnaryOperator.class \
                    $(DIR_MATH_OPERATOR)/DivisionSupport.class \
                    $(DIR_MATH_OPERATOR)/TernaryOperator.class \
                    $(DIR_MATH_OPERATOR)/AbstractNullaryOperator.class \
                    $(DIR_MATH_OPERATOR)/BinaryOperator.class \
                    $(DIR_MATH_OPERATOR)/IntBinaryOperator.class \
                    $(DIR_MATH_OPERATOR)/UnaryOperator.class


DIR_MATH_OPERATOR_COMPOSE = ch/javasoft/math/operator/compose
OBJ_MATH_OPERATOR_COMPOSE = $(DIR_MATH_OPERATOR_COMPOSE)/BinaryBinaryOperator.class \
                            $(DIR_MATH_OPERATOR_COMPOSE)/ConcatQuaternaryOperator.class \
                            $(DIR_MATH_OPERATOR_COMPOSE)/NullaryQuaternaryOperator.class \
                            $(DIR_MATH_OPERATOR_COMPOSE)/TernaryUnaryOperator.class \
                            $(DIR_MATH_OPERATOR_COMPOSE)/BinaryNullaryOperator.class \
                            $(DIR_MATH_OPERATOR_COMPOSE)/ConcatTernaryOperator.class \
                            $(DIR_MATH_OPERATOR_COMPOSE)/NullaryTernaryOperator.class \
                            $(DIR_MATH_OPERATOR_COMPOSE)/UnaryBinaryOperator.class \
                            $(DIR_MATH_OPERATOR_COMPOSE)/BinaryUnaryOperator.class \
                            $(DIR_MATH_OPERATOR_COMPOSE)/ConcatUnaryOperator.class \
                            $(DIR_MATH_OPERATOR_COMPOSE)/NullaryUnaryOperator.class \
                            $(DIR_MATH_OPERATOR_COMPOSE)/UnaryNullaryOperator.class \
                            $(DIR_MATH_OPERATOR_COMPOSE)/ConcatBinaryOperator.class \
                            $(DIR_MATH_OPERATOR_COMPOSE)/NAryNAryOperator.class \
                            $(DIR_MATH_OPERATOR_COMPOSE)/QuaternaryNullaryOperator.class \
                            $(DIR_MATH_OPERATOR_COMPOSE)/UnaryTernaryOperator.class \
                            $(DIR_MATH_OPERATOR_COMPOSE)/ConcatNAryOperator.class \
                            $(DIR_MATH_OPERATOR_COMPOSE)/NullaryBinaryOperator.class \
                            $(DIR_MATH_OPERATOR_COMPOSE)/TempArray.class \
                            $(DIR_MATH_OPERATOR_COMPOSE)/UnaryUnaryOperator.class \
                            $(DIR_MATH_OPERATOR_COMPOSE)/ConcatNullaryOperator.class \
                            $(DIR_MATH_OPERATOR_COMPOSE)/NullaryNullaryOperator.class \
                            $(DIR_MATH_OPERATOR_COMPOSE)/TernaryNullaryOperator.class


DIR_MATH_OPERATOR_IMPL = ch/javasoft/math/operator/impl
OBJ_MATH_OPERATOR_IMPL = $(DIR_MATH_OPERATOR_IMPL)/BigFractionOperators.class \
                         $(DIR_MATH_OPERATOR_IMPL)/BigIntegerOperators.class \
                         $(DIR_MATH_OPERATOR_IMPL)/DoubleOperators.class

DIR_MATH_OPS = ch/javasoft/math/ops
OBJ_MATH_OPS = $(DIR_MATH_OPS)/AbstractNumberOps.class \
               $(DIR_MATH_OPS)/BigFractionOperations.class \
               $(DIR_MATH_OPS)/DoubleOperations.class \
               $(DIR_MATH_OPS)/LongOperations.class \
               $(DIR_MATH_OPS)/BigDecimalOperations.class \
               $(DIR_MATH_OPS)/BigIntegerOperations.class \
               $(DIR_MATH_OPS)/IntegerOperations.class

DIR_MATH_VARINT = ch/javasoft/math/varint
OBJ_MATH_VARINT = $(DIR_MATH_VARINT)/BigIntegerVarInt.class \
                  $(DIR_MATH_VARINT)/IntVarInt.class \
                  $(DIR_MATH_VARINT)/VarIntCache.class \
                  $(DIR_MATH_VARINT)/VarInt.class \
                  $(DIR_MATH_VARINT)/VarIntTest.class \
                  $(DIR_MATH_VARINT)/DefaultVarIntCache.class \
                  $(DIR_MATH_VARINT)/LongVarInt.class \
                  $(DIR_MATH_VARINT)/VarIntFactory.class \
                  $(DIR_MATH_VARINT)/VarIntNumber.class \
                  $(DIR_MATH_VARINT)/VarIntUtil.class


DIR_MATH_VARINT_ARRAY = ch/javasoft/math/varint/array
OBJ_MATH_VARINT_ARRAY = $(DIR_MATH_VARINT_ARRAY)/VarIntGaussPivoting.class \
                        $(DIR_MATH_VARINT_ARRAY)/VarIntOperators.class

DIR_MATH_VARINT_OPS = ch/javasoft/math/varint/ops
OBJ_MATH_VARINT_OPS = $(DIR_MATH_VARINT_OPS)/VarIntOperations.class


# polymake
DIR_POLYMAKE_PARSE = ch/javasoft/polymake/parse
OBJ_POLYMAKE_PARSE = $(DIR_POLYMAKE_PARSE)/DefinitionType.class \
                     $(DIR_POLYMAKE_PARSE)/PolymakeMatrixDataImpl.class \
                     $(DIR_POLYMAKE_PARSE)/PolymakeMatrixData.class \
                     $(DIR_POLYMAKE_PARSE)/PolymakeParser.class

# jsmat
DIR_JSMAT_ROOT = ch/javasoft/jsmat
OBJ_JSMAT_ROOT = $(DIR_JSMAT_ROOT)/MatFileHeader.class \
                 $(DIR_JSMAT_ROOT)/MatOutputStreamWriter.class \
                 $(DIR_JSMAT_ROOT)/ReservedComplexWriter.class \
                 $(DIR_JSMAT_ROOT)/ReservedWriter.class \
                 $(DIR_JSMAT_ROOT)/MatFileWriter.class \
                 $(DIR_JSMAT_ROOT)/MatWriter.class \
                 $(DIR_JSMAT_ROOT)/ReservedMatrixWriter.class \
                 $(DIR_JSMAT_ROOT)/VariableWriter.class

DIR_JSMAT_COMMON = ch/javasoft/jsmat/common
OBJ_JSMAT_COMMON = $(DIR_JSMAT_COMMON)/MatClass.class \
                   $(DIR_JSMAT_COMMON)/MatFlag.class \
                   $(DIR_JSMAT_COMMON)/MatType.class

DIR_JSMAT_PRIMITIVE = ch/javasoft/jsmat/primitive
OBJ_JSMAT_PRIMITIVE = $(DIR_JSMAT_PRIMITIVE)/MatDouble.class \
                      $(DIR_JSMAT_PRIMITIVE)/MatInt32.class \
                      $(DIR_JSMAT_PRIMITIVE)/MatInt8.class \
                      $(DIR_JSMAT_PRIMITIVE)/MatSingle.class \
                      $(DIR_JSMAT_PRIMITIVE)/MatUInt32.class \
                      $(DIR_JSMAT_PRIMITIVE)/MatUInt8.class \
                      $(DIR_JSMAT_PRIMITIVE)/MatUtf32.class \
                      $(DIR_JSMAT_PRIMITIVE)/MatInt16.class \
                      $(DIR_JSMAT_PRIMITIVE)/MatInt64.class \
                      $(DIR_JSMAT_PRIMITIVE)/MatPrimitive.class \
                      $(DIR_JSMAT_PRIMITIVE)/MatUInt16.class \
                      $(DIR_JSMAT_PRIMITIVE)/MatUInt64.class \
                      $(DIR_JSMAT_PRIMITIVE)/MatUtf16.class \
                      $(DIR_JSMAT_PRIMITIVE)/MatUtf8.class

DIR_JSMAT_VARIABLE = ch/javasoft/jsmat/variable
OBJ_JSMAT_VARIABLE = $(DIR_JSMAT_VARIABLE)/MatAllocated.class \
                     $(DIR_JSMAT_VARIABLE)/MatCharMatrix.class \
                     $(DIR_JSMAT_VARIABLE)/MatMatrix.class \
                     $(DIR_JSMAT_VARIABLE)/MatReserved.class \
                     $(DIR_JSMAT_VARIABLE)/MatReservedStructure.class \
                     $(DIR_JSMAT_VARIABLE)/MatVariable.class \
                     $(DIR_JSMAT_VARIABLE)/MatCell.class \
                     $(DIR_JSMAT_VARIABLE)/MatDoubleMatrix.class \
                     $(DIR_JSMAT_VARIABLE)/MatReservedComplex.class \
                     $(DIR_JSMAT_VARIABLE)/MatReservedMatrix.class \
                     $(DIR_JSMAT_VARIABLE)/MatStructure.class


# tool
DIR_TOOL_ROOT = ch/javasoft/tool
OBJ_TOOL_ROOT = $(DIR_TOOL_ROOT)/JarFileCollector.class

# job
DIR_JOB_ROOT = ch/javasoft/job
OBJ_JOB_ROOT = $(DIR_JOB_ROOT)/AbstractJob.class \
               $(DIR_JOB_ROOT)/ExecJobProcessor.class \
               $(DIR_JOB_ROOT)/JobProcessor.class \
               $(DIR_JOB_ROOT)/NewThreadJobProcessor.class \
               $(DIR_JOB_ROOT)/AbstractJobProcessor.class \
               $(DIR_JOB_ROOT)/Executable.class \
               $(DIR_JOB_ROOT)/JobResultFactory.class \
               $(DIR_JOB_ROOT)/PipeJob.class \
               $(DIR_JOB_ROOT)/CurrentThreadJobProcessor.class \
               $(DIR_JOB_ROOT)/Executables.class \
               $(DIR_JOB_ROOT)/JobResult.class \
               $(DIR_JOB_ROOT)/StdErrNonEmptyException.class \
               $(DIR_JOB_ROOT)/ExecException.class \
               $(DIR_JOB_ROOT)/ExitValueException.class \
               $(DIR_JOB_ROOT)/Jobs.class \
               $(DIR_JOB_ROOT)/UncaughtJobTerminationHandlerException.class \
               $(DIR_JOB_ROOT)/ExecJob.class \
               $(DIR_JOB_ROOT)/Job.class \
               $(DIR_JOB_ROOT)/JobTerminationHandler.class \
               $(DIR_JOB_ROOT)/ExecJobMonitor.class \
               $(DIR_JOB_ROOT)/JobMonitor.class \
               $(DIR_JOB_ROOT)/MultiJobExecutable.class 

# io
DIR_IO_ROOT = ch/javasoft/io
OBJ_IO_ROOT = $(DIR_IO_ROOT)/AbstractDataInput.class \
              $(DIR_IO_ROOT)/DataOutputOutputStream.class \
              $(DIR_IO_ROOT)/LengthTrackingOutputStream.class \
              $(DIR_IO_ROOT)/PrintDataOutputStream.class \
              $(DIR_IO_ROOT)/Streams.class \
              $(DIR_IO_ROOT)/AbstractDataOutput.class \
              $(DIR_IO_ROOT)/FileEndingFileFilter.class \
              $(DIR_IO_ROOT)/NullInputStream.class \
              $(DIR_IO_ROOT)/Print.class \
              $(DIR_IO_ROOT)/WriterOutputStream.class \
              $(DIR_IO_ROOT)/DataInputInputStream.class \
              $(DIR_IO_ROOT)/Files.class \
              $(DIR_IO_ROOT)/NullOutputStream.class \
              $(DIR_IO_ROOT)/ReaderInputStream.class


# factory
DIR_FACTORY_ROOT = ch/javasoft/factory
OBJ_FACTORY_ROOT = $(DIR_FACTORY_ROOT)/ConfigException.class \
                   $(DIR_FACTORY_ROOT)/FactoryException.class \
                   $(DIR_FACTORY_ROOT)/Factory.class \
                   $(DIR_FACTORY_ROOT)/FactoryNotFoundException.class \
                   $(DIR_FACTORY_ROOT)/FactoryUtil.class \
                   $(DIR_FACTORY_ROOT)/IllegalFactoryException.class

# cdd
DIR_CDD_PARSER = ch/javasoft/cdd/parser
OBJ_CDD_PARSER = $(DIR_CDD_PARSER)/CddFileType.class \
                 $(DIR_CDD_PARSER)/CddNumberFormat.class \
                 $(DIR_CDD_PARSER)/CddParser.class

# lang
DIR_LANG_ROOT = ch/javasoft/lang
OBJ_LANG_ROOT = $(DIR_LANG_ROOT)/SystemProperties.class

DIR_LANG_MANAGEMENT = ch/javasoft/lang/management
OBJ_LANG_MANAGEMENT = $(DIR_LANG_MANAGEMENT)/JVMTimer.class

DIR_LANG_REFLECT = ch/javasoft/lang/reflect
OBJ_LANG_REFLECT = $(DIR_LANG_REFLECT)/Array.class \
                   $(DIR_LANG_REFLECT)/StackTrace.class

# util
DIR_UTIL_ROOT = ch/javasoft/util
OBJ_UTIL_ROOT = $(DIR_UTIL_ROOT)/Arrays.class \
                $(DIR_UTIL_ROOT)/DoubleArray.class \
                $(DIR_UTIL_ROOT)/ExceptionUtil.class \
                $(DIR_UTIL_ROOT)/IntArray.class \
                $(DIR_UTIL_ROOT)/Iterables.class \
                $(DIR_UTIL_ROOT)/Null.class \
                $(DIR_UTIL_ROOT)/StringUtil.class \
                $(DIR_UTIL_ROOT)/Unsigned.class \
                $(DIR_UTIL_ROOT)/ByteArray.class \
                $(DIR_UTIL_ROOT)/Env.class \
                $(DIR_UTIL_ROOT)/FilteredListCollection.class \
                $(DIR_UTIL_ROOT)/IterableIterable.class \
                $(DIR_UTIL_ROOT)/LongArray.class \
                $(DIR_UTIL_ROOT)/Sort.class \
                $(DIR_UTIL_ROOT)/Timer.class

DIR_UTIL_CONCURRENT = ch/javasoft/util/concurrent
OBJ_UTIL_CONCURRENT = $(DIR_UTIL_CONCURRENT)/ConcurrentBitSet.class

DIR_UTIL_GENARR = ch/javasoft/util/genarr
OBJ_UTIL_GENARR = $(DIR_UTIL_GENARR)/AbstractArrayIterable.class \
                  $(DIR_UTIL_GENARR)/ArrayIterable.class \
                  $(DIR_UTIL_GENARR)/GenericArray.class \
                  $(DIR_UTIL_GENARR)/GenericFixSizeArray.class \
                  $(DIR_UTIL_GENARR)/KnownLengthIterable.class \
                  $(DIR_UTIL_GENARR)/AbstractGenericArray.class \
                  $(DIR_UTIL_GENARR)/CloneableTyped.class \
                  $(DIR_UTIL_GENARR)/GenericDynamicArray.class \
                  $(DIR_UTIL_GENARR)/KnownLengthIterableIterable.class \
                  $(DIR_UTIL_GENARR)/ListArrayIterable.class

DIR_UTIL_INTS = ch/javasoft/util/ints
OBJ_UTIL_INTS = $(DIR_UTIL_INTS)/AbstractIntCollection.class \
                $(DIR_UTIL_INTS)/AbstractIntList.class \
                $(DIR_UTIL_INTS)/DefaultIntListIterator.class \
                $(DIR_UTIL_INTS)/IntCollections.class \
                $(DIR_UTIL_INTS)/IntIterator.class \
                $(DIR_UTIL_INTS)/IntSet.class \
                $(DIR_UTIL_INTS)/AbstractIntIntMap.class \
                $(DIR_UTIL_INTS)/AbstractSortedIntSet.class \
                $(DIR_UTIL_INTS)/DefaultIntList.class \
                $(DIR_UTIL_INTS)/IntHashMap.class \
                $(DIR_UTIL_INTS)/IntListIterator.class \
                $(DIR_UTIL_INTS)/KeyRangeIntIntMap.class \
                $(DIR_UTIL_INTS)/AbstractIntIterator.class \
                $(DIR_UTIL_INTS)/BitSetIntSet.class \
                $(DIR_UTIL_INTS)/DefaultIntSet.class \
                $(DIR_UTIL_INTS)/IntIntMap.class \
                $(DIR_UTIL_INTS)/IntList.class \
                $(DIR_UTIL_INTS)/RangeIntSet.class \
                $(DIR_UTIL_INTS)/AbstractIntListIterator.class \
                $(DIR_UTIL_INTS)/DefaultIntIntMap.class \
                $(DIR_UTIL_INTS)/IntCollection.class \
                $(DIR_UTIL_INTS)/IntIterable.class \
                $(DIR_UTIL_INTS)/IntMap.class \
                $(DIR_UTIL_INTS)/SortedIntSet.class

DIR_UTIL_LOGGING = ch/javasoft/util/logging
OBJ_UTIL_LOGGING = $(DIR_UTIL_LOGGING)/AbstractStandardHandler.class \
                   $(DIR_UTIL_LOGGING)/LevelFilter.class \
                   $(DIR_UTIL_LOGGING)/LogFragmenter.class \
                   $(DIR_UTIL_LOGGING)/LogPrintStream.class \
                   $(DIR_UTIL_LOGGING)/LogWriter.class \
                   $(DIR_UTIL_LOGGING)/StandardOutHandler.class \
                   $(DIR_UTIL_LOGGING)/AutoFlushStreamHandler.class \
                   $(DIR_UTIL_LOGGING)/LogFormatter.class \
                   $(DIR_UTIL_LOGGING)/Loggers.class \
                   $(DIR_UTIL_LOGGING)/LogPrintWriter.class \
                   $(DIR_UTIL_LOGGING)/StandardErrHandler.class \
                   $(DIR_UTIL_LOGGING)/SystemProperties.class
                   
DIR_UTIL_LOGGING_MATLAB = ch/javasoft/util/logging/matlab
OBJ_UTIL_LOGGING_MATLAB = $(DIR_UTIL_LOGGING_MATLAB)/LogConfiguration.class \
                          $(DIR_UTIL_LOGGING_MATLAB)/LogConfigurationReader.class

DIR_UTIL_LONGS = ch/javasoft/util/longs
OBJ_UTIL_LONGS = $(DIR_UTIL_LONGS)/AbstractBitSet.class \
                 $(DIR_UTIL_LONGS)/AbstractLongListIterator.class \
                 $(DIR_UTIL_LONGS)/ByteSet.class \
                 $(DIR_UTIL_LONGS)/LongIterable.class \
                 $(DIR_UTIL_LONGS)/LongSet.class \
                 $(DIR_UTIL_LONGS)/AbstractExactMembershipLongSet.class \
                 $(DIR_UTIL_LONGS)/AbstractLongList.class \
                 $(DIR_UTIL_LONGS)/ExactMembershipAllCountLongSet.class \
                 $(DIR_UTIL_LONGS)/LongIterator.class \
                 $(DIR_UTIL_LONGS)/SortedLongSet.class \
                 $(DIR_UTIL_LONGS)/AbstractLongCollection.class \
                 $(DIR_UTIL_LONGS)/AbstractSortedLongSet.class \
                 $(DIR_UTIL_LONGS)/ExactMembershipSkipCountLongSet.class \
                 $(DIR_UTIL_LONGS)/LongListIterator.class \
                 $(DIR_UTIL_LONGS)/AbstractLongIterator.class \
                 $(DIR_UTIL_LONGS)/BitSetByteSet.class \
                 $(DIR_UTIL_LONGS)/LongCollection.class \
                 $(DIR_UTIL_LONGS)/LongList.class

DIR_UTIL_MAP = ch/javasoft/util/map
OBJ_UTIL_MAP = $(DIR_UTIL_MAP)/AbstractMultiValueMap.class \
               $(DIR_UTIL_MAP)/DefaultIntIntMultiValueMap.class \
               $(DIR_UTIL_MAP)/IntIntMultiValueMap.class \
               $(DIR_UTIL_MAP)/MultiValueMap.class \
               $(DIR_UTIL_MAP)/AbstractMutableMultiValueMap.class \
               $(DIR_UTIL_MAP)/DefaultMultiValueMap.class \
               $(DIR_UTIL_MAP)/JoinedMultiValueMap.class \
               $(DIR_UTIL_MAP)/SingleValueMap.class

DIR_UTIL_NUMERIC = ch/javasoft/util/numeric
OBJ_UTIL_NUMERIC = $(DIR_UTIL_NUMERIC)/BigIntegerUtil.class \
                   $(DIR_UTIL_NUMERIC)/DoubleUtil.class \
                   $(DIR_UTIL_NUMERIC)/IntegerUtil.class \
                   $(DIR_UTIL_NUMERIC)/Zero.class

# bitset
DIR_BITSET_ROOT = ch/javasoft/bitset
OBJ_BITSET_ROOT = $(DIR_BITSET_ROOT)/BitSetFactory.class \
                  $(DIR_BITSET_ROOT)/ByteBitSet.class \
                  $(DIR_BITSET_ROOT)/DefaultBitSet.class \
                  $(DIR_BITSET_ROOT)/IBitSet.class \
                  $(DIR_BITSET_ROOT)/IntBitSet.class \
                  $(DIR_BITSET_ROOT)/LongBitSet.class

DIR_BITSET_SEARCH = ch/javasoft/bitset/search
OBJ_BITSET_SEARCH = $(DIR_BITSET_SEARCH)/ListSearch.class \
                    $(DIR_BITSET_SEARCH)/SortedSetSearch.class \
                    $(DIR_BITSET_SEARCH)/SubSetSearch.class \
                    $(DIR_BITSET_SEARCH)/SuperSetSearch.class \
                    $(DIR_BITSET_SEARCH)/TreeSearch.class 

DIR_BITSET_SEARCH_TREE = ch/javasoft/bitset/search/tree
OBJ_BITSET_SEARCH_TREE = $(DIR_BITSET_SEARCH_TREE)/InterNode.class \
                         $(DIR_BITSET_SEARCH_TREE)/LeafNode.class \
                         $(DIR_BITSET_SEARCH_TREE)/Node.class

# jmatio
DIR_JMATIO_COMMON = com/jmatio/common
OBJ_JMATIO_COMMON = $(DIR_JMATIO_COMMON)/MatDataTypes.class

DIR_JMATIO_IO = com/jmatio/io
OBJ_JMATIO_IO = $(DIR_JMATIO_IO)/MatFileFilter.class \
                $(DIR_JMATIO_IO)/MatFileInputStream.class \
                $(DIR_JMATIO_IO)/MatFileWriter.class \
                $(DIR_JMATIO_IO)/MatTag.class \
                $(DIR_JMATIO_IO)/MatFileHeader.class \
                $(DIR_JMATIO_IO)/MatFileReader.class \
                $(DIR_JMATIO_IO)/MatlabIOException.class

DIR_JMATIO_TYPES = com/jmatio/types
OBJ_JMATIO_TYPES = $(DIR_JMATIO_TYPES)/GenericArrayCreator.class \
                   $(DIR_JMATIO_TYPES)/MLCell.class \
                   $(DIR_JMATIO_TYPES)/MLDouble.class \
                   $(DIR_JMATIO_TYPES)/MLNumericArray.class \
                   $(DIR_JMATIO_TYPES)/MLStructure.class \
                   $(DIR_JMATIO_TYPES)/MLArray.class \
                   $(DIR_JMATIO_TYPES)/MLChar.class \
                   $(DIR_JMATIO_TYPES)/MLEmptyArray.class \
                   $(DIR_JMATIO_TYPES)/MLSparse.class
##############################################################################


##############################################################################
# compile java files                                                         #
##############################################################################
all: bitset jmatio util lang cdd factory io job tool jsmat polymake math \
     junit jbase xml smx metabolic thermodynamic_exception thermodynamic_check

jarfile:
	$(JAR) -cmf META-INF/MANIFEST.MF tEFMA.jar ch/ com/ config/ lib/ at/

thermodynamic_exception: $(OBJ_THERMO_EXCEPTION)
thermodynamic_check: $(OBJ_THERMO_CHECK)

metabolic: metabolic_root metabolic_compartment \
           metabolic_convert metabolic_efm metabolic_fa metabolic_generate \
           metabolic_impl metabolic_parse metabolic_sbml metabolic_util \
           metabolic_compress
metabolic_root: $(OBJ_METABOLIC_ROOT)
metabolic_compartment: $(OBJ_METABOLIC_COMPARTMENT)
metabolic_compress: metabolic_compress_root metabolic_compress_config \
                    metabolic_compress_generate
metabolic_compress_root: $(OBJ_METABOLIC_COMPRESS_ROOT)
metabolic_compress_config: $(OBJ_METABOLIC_COMPRESS_CONFIG)
metabolic_compress_generate: $(OBJ_METABOLIC_COMPRESS_GENERATE)
metabolic_convert: $(OBJ_METABOLIC_CONVERT)
metabolic_efm: metabolic_efm_root metabolic_efm_adj metabolic_efm_borndie \
               metabolic_efm_column metabolic_efm_concurrent metabolic_efm_config \
               metabolic_efm_dist metabolic_efm_impl metabolic_efm_main \
               metabolic_efm_memory metabolic_efm_model metabolic_efm_output \
               metabolic_efm_progress metabolic_efm_rankup metabolic_efm_sort \
               metabolic_efm_stress metabolic_efm_tree metabolic_efm_util \
	       thermodynamic
metabolic_efm_root: $(OBJ_METABOLIC_EFM_ROOT)

thermodynamic: $(OBJ_THERMODYNAMIC)

metabolic_efm_adj: metabolic_efm_adj_root metabolic_efm_adj_incore
metabolic_efm_adj_root: $(OBJ_METABOLIC_EFM_ADJ_ROOT)
metabolic_efm_adj_incore: metabolic_efm_adj_incore_root metabolic_efm_adj_incore_tree
metabolic_efm_adj_incore_root: $(OBJ_METABOLIC_EFM_ADJ_INCORE_ROOT)
metabolic_efm_adj_incore_tree: metabolic_efm_adj_incore_tree_root metabolic_efm_adj_incore_tree_rank \
                               metabolic_efm_adj_incore_tree_search metabolic_efm_adj_incore_tree_urank
metabolic_efm_adj_incore_tree_root: $(OBJ_METABOLIC_EFM_ADJ_INCORE_TREE_ROOT)
metabolic_efm_adj_incore_tree_rank: $(OBJ_METABOLIC_EFM_ADJ_INCORE_TREE_RANK)
metabolic_efm_adj_incore_tree_search: $(OBJ_METABOLIC_EFM_ADJ_INCORE_TREE_SEARCH)
metabolic_efm_adj_incore_tree_urank: metabolic_efm_adj_incore_tree_urank_root \
                                     metabolic_efm_adj_incore_tree_urank_dbl \
                                     metabolic_efm_adj_incore_tree_urank_dbl2 \
                                     metabolic_efm_adj_incore_tree_urank_frac \
                                     metabolic_efm_adj_incore_tree_urank_frac2 \
                                     metabolic_efm_adj_incore_tree_urank_modp \
                                     metabolic_efm_adj_incore_tree_urank_modpi
metabolic_efm_adj_incore_tree_urank_root: $(OBJ_METABOLIC_EFM_ADJ_INCORE_TREE_URANK_ROOT)
metabolic_efm_adj_incore_tree_urank_dbl: $(OBJ_METABOLIC_EFM_ADJ_INCORE_TREE_URANK_DBL)
metabolic_efm_adj_incore_tree_urank_dbl2: $(OBJ_METABOLIC_EFM_ADJ_INCORE_TREE_URANK_DBL2)
metabolic_efm_adj_incore_tree_urank_frac: $(OBJ_METABOLIC_EFM_ADJ_INCORE_TREE_URANK_FRAC)
metabolic_efm_adj_incore_tree_urank_frac2: $(OBJ_METABOLIC_EFM_ADJ_INCORE_TREE_URANK_FRAC2)
metabolic_efm_adj_incore_tree_urank_modp: $(OBJ_METABOLIC_EFM_ADJ_INCORE_TREE_URANK_MODP)
metabolic_efm_adj_incore_tree_urank_modpi: $(OBJ_METABOLIC_EFM_ADJ_INCORE_TREE_URANK_MODPI)
metabolic_efm_borndie: metabolic_efm_borndie_root metabolic_efm_borndie_debug \
                       metabolic_efm_borndie_job metabolic_efm_borndie_matrix \
                       metabolic_efm_borndie_memory metabolic_efm_borndie_model \
                       metabolic_efm_borndie_range
metabolic_efm_borndie_root: $(OBJ_METABOLIC_EFM_BORNDIE_ROOT)
metabolic_efm_borndie_debug: $(OBJ_METABOLIC_EFM_BORNDIE_DEBUG)
metabolic_efm_borndie_job: $(OBJ_METABOLIC_EFM_BORNDIE_JOB)
metabolic_efm_borndie_matrix: $(OBJ_METABOLIC_EFM_BORNDIE_MATRIX)
metabolic_efm_borndie_memory: $(OBJ_METABOLIC_EFM_BORNDIE_MEMORY)
metabolic_efm_borndie_model: $(OBJ_METABOLIC_EFM_BORNDIE_MODEL)
metabolic_efm_borndie_range: $(OBJ_METABOLIC_EFM_BORNDIE_RANGE)
metabolic_efm_column: metabolic_efm_column_root metabolic_efm_column_filter
metabolic_efm_column_root: $(OBJ_METABOLIC_EFM_COLUMN_ROOT)
metabolic_efm_column_filter: $(OBJ_METABOLIC_EFM_COLUMN_FILTER)
metabolic_efm_concurrent: $(OBJ_METABOLIC_EFM_CONCURRENT)
metabolic_efm_config: $(OBJ_METABOLIC_EFM_CONFIG)
metabolic_efm_dist: metabolic_efm_dist_root metabolic_efm_dist_impl
metabolic_efm_dist_root: $(OBJ_METABOLIC_EFM_DIST_ROOT)
metabolic_efm_dist_impl: metabolic_efm_dist_impl_root metabolic_efm_dist_impl_adj \
                         metabolic_efm_dist_impl_file
metabolic_efm_dist_impl_root: $(OBJ_METABOLIC_EFM_DIST_IMPL_ROOT)
metabolic_efm_dist_impl_adj: $(OBJ_METABOLIC_EFM_DIST_IMPL_ADJ)
metabolic_efm_dist_impl_file: $(OBJ_METABOLIC_EFM_DIST_IMPL_FILE)
metabolic_efm_impl: $(OBJ_METABOLIC_EFM_IMPL)
metabolic_efm_main: $(OBJ_METABOLIC_EFM_MAIN)
metabolic_efm_memory: metabolic_efm_memory_root metabolic_efm_memory_incore metabolic_efm_memory_outocre
metabolic_efm_memory_root: $(OBJ_METABOLIC_EFM_MEMORY_ROOT)
metabolic_efm_memory_incore: $(OBJ_METABOLIC_EFM_MEMORY_INCORE)
metabolic_efm_memory_outocre: $(OBJ_METABOLIC_EFM_MEMORY_OUTCORE)
metabolic_efm_model: metabolic_efm_model_root metabolic_efm_model_canonical metabolic_efm_model_nullspace
metabolic_efm_model_root: $(OBJ_METABOLIC_EFM_MODEL_ROOT)
metabolic_efm_model_canonical: $(OBJ_METABOLIC_EFM_MODEL_CANONICAL)
metabolic_efm_model_nullspace: $(OBJ_METABOLIC_EFM_MODEL_NULLSPACE)
metabolic_efm_output: metabolic_efm_output_root metabolic_efm_output_mat metabolic_efm_output_text
metabolic_efm_output_root: $(OBJ_METABOLIC_EFM_OUTPUT_ROOT)
metabolic_efm_output_mat: $(OBJ_METABOLIC_EFM_OUTPUT_MAT)
metabolic_efm_output_text: $(OBJ_METABOLIC_EFM_OUTPUT_TEXT)
metabolic_efm_progress: $(OBJ_METABOLIC_EFM_PROGRESS)
metabolic_efm_rankup: metabolic_efm_rankup_root metabolic_efm_rankup_modpi
metabolic_efm_rankup_root: $(OBJ_METABOLIC_EFM_RANKUP_ROOT)
metabolic_efm_rankup_modpi: $(OBJ_METABOLIC_EFM_RANKUP_MODPI)
metabolic_efm_sort: $(OBJ_METABOLIC_EFM_SORT)
metabolic_efm_stress: $(OBJ_METABOLIC_EFM_STRESS)
metabolic_efm_tree: metabolic_efm_tree_root metabolic_efm_tree_concurrent metabolic_efm_tree_impl \
                    metabolic_efm_tree_incore metabolic_efm_tree_outcore metabolic_efm_tree_rankup \
                    metabolic_efm_tree_search
metabolic_efm_tree_root: $(OBJ_METABOLIC_EFM_TREE_ROOT)
metabolic_efm_tree_concurrent: $(OBJ_METABOLIC_EFM_TREE_CONCURRENT)
metabolic_efm_tree_impl: $(OBJ_METABOLIC_EFM_TREE_IMPL)
metabolic_efm_tree_incore: $(OBJ_METABOLIC_EFM_TREE_INCORE)
metabolic_efm_tree_outcore: $(OBJ_METABOLIC_EFM_TREE_OUTCORE)
metabolic_efm_tree_rankup: $(OBJ_METABOLIC_EFM_TREE_RANKUP)
metabolic_efm_tree_search: $(OBJ_METABOLIC_EFM_TREE_SEARCH)
metabolic_efm_util: $(OBJ_METABOLIC_EFM_UTIL)
metabolic_fa: $(OBJ_METABOLIC_FA)
metabolic_generate: $(OBJ_METABOLIC_GENERATE)
metabolic_impl: $(OBJ_METABOLIC_IMPL)
metabolic_parse: metabolic_parse_root metabolic_parse_junit
metabolic_parse_root: $(OBJ_METABOLIC_PARSE_ROOT)
metabolic_parse_junit: $(OBJ_METABOLIC_PARSE_JUNIT)
metabolic_sbml: $(OBJ_METABOLIC_SBML)
metabolic_util: $(OBJ_METABOLIC_UTIL)

util: util_root util_concurrent util_genarr util_ints util_logging \
      util_longs util_map util_numeric
util_root: $(OBJ_UTIL_ROOT)
util_concurrent: $(OBJ_UTIL_CONCURRENT)
util_genarr: $(OBJ_UTIL_GENARR)
util_ints: $(OBJ_UTIL_INTS)
util_logging: $(OBJ_UTIL_LOGGING)
util_longs: $(OBJ_UTIL_LONGS)
util_map: $(OBJ_UTIL_MAP)
util_numeric: $(OBJ_UTIL_NUMERIC)

lang: lang_root lang_management lang_reflect
lang_root: $(OBJ_LANG_ROOT)
lang_management: $(OBJ_LANG_MANAGEMENT)
lang_reflect: $(OBJ_LANG_REFLECT)

cdd: cdd_parser
cdd_parser: $(OBJ_CDD_PARSER)

factory: factory_root
factory_root: $(OBJ_FACTORY_ROOT)

io: io_root
io_root: $(OBJ_IO_ROOT)

job: job_root
job_root: $(OBJ_JOB_ROOT)

tool: tool_root
tool_root: $(OBJ_TOOL_ROOT)

jsmat: jsmat_root jsmat_common jsmat_primitive jsmat_variable
jsmat_root: $(OBJ_JSMAT_ROOT)
jsmat_common: $(OBJ_JSMAT_COMMON)
jsmat_primitive: $(OBJ_JSMAT_PRIMITIVE)
jsmat_variable: $(OBJ_JSMAT_VARIABLE)

polymake: polymake_parse
polymake_parse: $(OBJ_POLYMAKE_PARSE)

math: math_root
math_root: $(OBJ_MATH_ROOT) math_array math_linalg math_operator math_ops math_varint
math_array: $(OBJ_MATH_ARRAY) math_array_impl math_array_parse math_array_sort
math_array_impl: $(OBJ_MATH_ARRAY_IMPL)
math_array_parse: $(OBJ_MATH_ARRAY_PARSE)
math_array_sort: $(OBJ_MATH_ARRAY_SORT)

math_linalg: $(OBJ_MATH_LINALG) math_linalg_impl
math_linalg_impl: $(OBJ_MATH_LINALG_IMPL)

math_operator: $(OBJ_MATH_OPERATOR) math_operator_compose math_operator_impl
math_operator_compose: $(OBJ_MATH_OPERATOR_IMPL)
math_operator_impl: $(OBJ_MATH_OPERATOR_IMPL)

math_ops: $(OBJ_MATH_OPS)

math_varint: $(OBJ_MATH_VARINT) math_varint_array math_varint_ops
math_varint_array: $(OBJ_MATH_VARINT_ARRAY)
math_varint_ops: $(OBJ_MATH_VARINT_OPS)


junit: junit_textui
junit_textui: $(OBJ_JUNIT_TEXTUI)

jbase: jbase_root jbase_concurrent jbase_marshal jbase_util
jbase_root: $(OBJ_JBASE_ROOT)
jbase_concurrent: $(OBJ_JBASE_CONCURRENT)
jbase_marshal: $(OBJ_JBASE_MARHSAL)
jbase_util: $(OBJ_JBASE_UTIL)

bitset: bitset_root bitset_search bitset_search_tree
bitset_root: $(OBJ_BITSET_ROOT)
bitset_search: $(OBJ_BITSET_SEARCH)
bitset_search_tree: $(OBJ_BITSET_SEARCH_TREE)

jmatio: jmatio_common jmatio_io jmatio_types
jmatio_common: $(OBJ_JMATIO_COMMON)
jmatio_io: $(OBJ_JMATIO_IO)
jmatio_types: $(OBJ_JMATIO_TYPES)

xml: xml_config xml_factory
xml_config: $(OBJ_XML_CONFIG)
xml_factory: $(OBJ_XML_FACTORY)

smx: smx_exception smx_iface smx_impl smx_ops smx_util
smx_exception: $(OBJ_SMX_EXCEPTION)
smx_iface: $(OBJ_SMX_IFACE)
smx_impl: $(OBJ_SMX_IMPL)
smx_ops: smx_ops_root smx_ops_ext smx_ops_jlapack smx_ops_matrix \
         smx_ops_mt smx_ops_sscc
smx_ops_root: $(OBJ_SMX_OPS_ROOT)
smx_ops_ext: $(OBJ_SMX_OPS_EXT)
smx_ops_jlapack: $(OBJ_SMX_OPS_JLAPACK)
smx_ops_matrix: $(OBJ_SMX_OPS_MATRIX)
smx_ops_mt: $(OBJ_SMX_OPS_MT)
smx_ops_sscc: $(OBJ_SMX_OPS_SSCC)
smx_util: $(OBJ_SMX_UTIL)

##############################################################################


##############################################################################
# cleaing up                                                                 #
##############################################################################
clean: clean_bitset clean_jmatio clean_util clean_lang clean_cdd \
       clean_factory clean_io clean_job clean_tool clean_jsmat \
       clean_polymake clean_math clean_junit clean_jbase clean_xml \
       clean_smx clean_metabolic clean_thermo_exception \
       clean_thermo_check clean_thermodynamic

clean_thermo_exception:
	-rm $(DIR_THERMO_EXCEPTION)/*.class

clean_thermo_check:
	-rm $(DIR_THERMO_CHECK)/*.class

clean_thermodynamic:
	-rm $(DIR_THERMODYNAMIC)/*.class

clean_metabolic: clean_metabolic_root clean_metabolic_compartment clean_metabolic_compress \
                 clean_metabolic_config clean_metabolic_efm clean_metabolic_fa \
                 clean_metabolic_generate clean_metabolic_impl clean_metabolic_parse \
                 clean_metabolic_sbml clean_metabolic_util

clean_metabolic_root:
	- rm $(DIR_METABOLIC_ROOT)/*.class

clean_metabolic_compartment:
	- rm $(DIR_METABOLIC_COMPARTMENT)/*.class

clean_metabolic_compress: clean_metabolic_compress_root clean_metabolic_compress_config \
                          clean_metabolic_compress_generate

clean_metabolic_compress_root:
	- rm $(DIR_METABOLIC_COMPRESS_ROOT)/*.class

clean_metabolic_compress_config:
	- rm $(DIR_METABOLIC_COMPRESS_CONFIG)/*.class

clean_metabolic_compress_generate:
	- rm $(DIR_METABOLIC_COMPRESS_GENERATE)/*.class

clean_metabolic_config:
	- rm $(DIR_METABOLIC_CONVERT)/*.class

clean_metabolic_efm: clean_metabolic_efm_root clean_metabolic_efm_adj clean_metabolic_efm_borndie \
                     clean_metabolic_efm_column clean_metabolic_efm_concurrent \
                     clean_metabolic_efm_config clean_metabolic_efm_dist clean_metabolic_efm_impl \
                     clean_metabolic_efm_main clean_metabolic_efm_memory clean_metabolic_efm_model \
                     clean_metabolic_efm_output clean_metabolic_efm_progress clean_metabolic_efm_rankup \
                     clean_metabolic_efm_sort clean_metabolic_efm_stress clean_metabolic_efm_tree \
                     clean_metabolic_efm_util

clean_metabolic_efm_root:
	- rm $(DIR_METABOLIC_EFM_ROOT)/*.class

clean_metabolic_efm_adj: clean_metabolic_efm_adj_root clean_metabolic_efm_adj_incore

clean_metabolic_efm_adj_root:
	- rm $(DIR_METABOLIC_EFM_ADJ_ROOT)/*.class

clean_metabolic_efm_adj_incore: clean_metabolic_efm_adj_incore_root clean_metabolic_efm_adj_incore_tree

clean_metabolic_efm_adj_incore_root:
	- rm $(DIR_METABOLIC_EFM_ADJ_INCORE_ROOT)/*.class

clean_metabolic_efm_adj_incore_tree: clean_metabolic_efm_adj_incore_tree_root clean_metabolic_efm_adj_incore_tree_rank \
                                     clean_metabolic_efm_adj_incore_tree_search clean_metabolic_efm_adj_incore_tree_urank

clean_metabolic_efm_adj_incore_tree_root:
	- rm $(DIR_METABOLIC_EFM_ADJ_INCORE_TREE_ROOT)/*.class

clean_metabolic_efm_adj_incore_tree_rank:
	- rm $(DIR_METABOLIC_EFM_ADJ_INCORE_TREE_RANK)/*.class

clean_metabolic_efm_adj_incore_tree_search:
	- rm $(DIR_METABOLIC_EFM_ADJ_INCORE_TREE_SEARCH)/*.class

clean_metabolic_efm_adj_incore_tree_urank: clean_metabolic_efm_adj_incore_tree_urank_dbl \
                                           clean_metabolic_efm_adj_incore_tree_urank_dbl2 \
                                           clean_metabolic_efm_adj_incore_tree_urank_frac \
                                           clean_metabolic_efm_adj_incore_tree_urank_frac2 \
                                           clean_metabolic_efm_adj_incore_tree_urank_modp \
                                           clean_metabolic_efm_adj_incore_tree_urank_modpi

clean_metabolic_efm_adj_incore_tree_urank_dbl:
	- rm $(DIR_METABOLIC_EFM_ADJ_INCORE_TREE_URANK_DBL)/*.class

clean_metabolic_efm_adj_incore_tree_urank_dbl2:
	- rm $(DIR_METABOLIC_EFM_ADJ_INCORE_TREE_URANK_DBL2)/*.class

clean_metabolic_efm_adj_incore_tree_urank_frac:
	- rm $(DIR_METABOLIC_EFM_ADJ_INCORE_TREE_URANK_FRAC)/*.class

clean_metabolic_efm_adj_incore_tree_urank_frac2:
	- rm $(DIR_METABOLIC_EFM_ADJ_INCORE_TREE_URANK_FRAC2)/*.class

clean_metabolic_efm_adj_incore_tree_urank_modp:
	- rm $(DIR_METABOLIC_EFM_ADJ_INCORE_TREE_URANK_MODP)/*.class

clean_metabolic_efm_adj_incore_tree_urank_modpi:
	- rm $(DIR_METABOLIC_EFM_ADJ_INCORE_TREE_URANK_MODPI)/*.class

clean_metabolic_efm_borndie: clean_metabolic_efm_borndie_root \
                             clean_metabolic_efm_borndie_debug \
                             clean_metabolic_efm_borndie_job \
                             clean_metabolic_efm_borndie_matrix \
                             clean_metabolic_efm_borndie_memory \
                             clean_metabolic_efm_borndie_model \
                             clean_metabolic_efm_borndie_range

clean_metabolic_efm_borndie_root:
	- rm $(DIR_METABOLIC_EFM_BORNDIE_ROOT)/*.class

clean_metabolic_efm_borndie_debug:
	- rm $(DIR_METABOLIC_EFM_BORNDIE_DEBUG)/*.class

clean_metabolic_efm_borndie_job:
	- rm $(DIR_METABOLIC_EFM_BORNDIE_JOB)/*.class

clean_metabolic_efm_borndie_matrix:
	- rm $(DIR_METABOLIC_EFM_BORNDIE_MATRIX)/*.class

clean_metabolic_efm_borndie_memory:
	- rm $(DIR_METABOLIC_EFM_BORNDIE_MEMORY)/*.class

clean_metabolic_efm_borndie_model:
	- rm $(DIR_METABOLIC_EFM_BORNDIE_MODEL)/*.class

clean_metabolic_efm_borndie_range:
	- rm $(DIR_METABOLIC_EFM_BORNDIE_RANGE)/*.class

clean_metabolic_efm_column: clean_metabolic_efm_column_root clean_metabolic_efm_column_filter

clean_metabolic_efm_column_root:
	- rm $(DIR_METABOLIC_EFM_COLUMN_ROOT)/*.class

clean_metabolic_efm_column_filter:
	- rm $(DIR_METABOLIC_EFM_COLUMN_FILTER)/*.class

clean_metabolic_efm_concurrent:
	- rm $(DIR_METABOLIC_EFM_CONCURRENT)/*.class

clean_metabolic_efm_config:
	- rm $(DIR_METABOLIC_EFM_CONFIG)/*.class

clean_metabolic_efm_dist: clean_metabolic_efm_dist_root clean_metabolic_efm_dist_impl

clean_metabolic_efm_dist_root:
	- rm $(DIR_METABOLIC_EFM_DIST_ROOT)/*.class

clean_metabolic_efm_dist_impl: clean_metabolic_efm_dist_impl_root \
                               clean_metabolic_efm_dist_impl_adj \
                               clean_metabolic_efm_dist_impl_file

clean_metabolic_efm_dist_impl_root:
	- rm $(DIR_METABOLIC_EFM_DIST_IMPL_ROOT)/*.class

clean_metabolic_efm_dist_impl_adj:
	- rm $(DIR_METABOLIC_EFM_DIST_IMPL_ADJ)/*.class

clean_metabolic_efm_dist_impl_file:
	- rm $(DIR_METABOLIC_EFM_DIST_IMPL_FILE)/*.class

clean_metabolic_efm_impl:
	- rm $(DIR_METABOLIC_EFM_IMPL)/*.class

clean_metabolic_efm_main:
	- rm $(DIR_METABOLIC_EFM_MAIN)/*.class

clean_metabolic_efm_memory: clean_metabolic_efm_memory_root \
                            clean_metabolic_efm_memory_incore \
                            clean_metabolic_efm_memory_outcore

clean_metabolic_efm_memory_root:
	- rm $(DIR_METABOLIC_EFM_MEMORY_ROOT)/*.class

clean_metabolic_efm_memory_incore:
	- rm $(DIR_METABOLIC_EFM_MEMORY_INCORE)/*.class

clean_metabolic_efm_memory_outcore:
	- rm $(DIR_METABOLIC_EFM_MEMORY_OUTCORE)/*.class


clean_metabolic_efm_model: clean_metabolic_efm_model_root \
                           clean_metabolic_efm_model_canonical \
                           clean_metabolic_efm_model_nullspace

clean_metabolic_efm_model_root:
	- rm $(DIR_METABOLIC_EFM_MODEL_ROOT)/*.class

clean_metabolic_efm_model_canonical:
	- rm $(DIR_METABOLIC_EFM_MODEL_CANONICAL)/*.class

clean_metabolic_efm_model_nullspace:
	- rm $(DIR_METABOLIC_EFM_MODEL_NULLSPACE)/*.class

clean_metabolic_efm_output: clean_metabolic_efm_output_root \
                            clean_metabolic_efm_output_mat \
                            clean_metabolic_efm_output_text

clean_metabolic_efm_output_root:
	- rm $(DIR_METABOLIC_EFM_OUTPUT_ROOT)/*.class

clean_metabolic_efm_output_mat:
	- rm $(DIR_METABOLIC_EFM_OUTPUT_MAT)/*.class

clean_metabolic_efm_output_text:
	- rm $(DIR_METABOLIC_EFM_OUTPUT_TEXT)/*.class

clean_metabolic_efm_progress:
	- rm $(DIR_METABOLIC_EFM_PROGRESS)/*.class

clean_metabolic_efm_rankup: clean_metabolic_efm_rankup_root \
                            clean_metabolic_efm_rankup_modpi

clean_metabolic_efm_rankup_root:
	- rm $(DIR_METABOLIC_EFM_RANKUP_ROOT)/*.class

clean_metabolic_efm_rankup_modpi:
	- rm $(DIR_METABOLIC_EFM_RANKUP_MODPI)/*.class

clean_metabolic_efm_sort:
	- rm $(DIR_METABOLIC_EFM_SORT)/*.class

clean_metabolic_efm_stress:
	- rm $(DIR_METABOLIC_EFM_STRESS)/*.class

clean_metabolic_efm_tree: clean_metabolic_efm_tree_root \
                          clean_metabolic_efm_tree_concurrent \
                          clean_metabolic_efm_tree_impl \
                          clean_metabolic_efm_tree_incore \
                          clean_metabolic_efm_tree_outcore \
                          clean_metabolic_efm_tree_rankup \
                          clean_metabolic_efm_tree_search \
                          clean_metabolic_efm_tree_urank_root

clean_metabolic_efm_tree_root:
	- rm $(DIR_METABOLIC_EFM_TREE_ROOT)/*.class

clean_metabolic_efm_tree_concurrent:
	- rm $(DIR_METABOLIC_EFM_TREE_CONCURRENT)/*.class

clean_metabolic_efm_tree_impl:
	- rm $(DIR_METABOLIC_EFM_TREE_IMPL)/*.class

clean_metabolic_efm_tree_incore:
	- rm $(DIR_METABOLIC_EFM_TREE_INCORE)/*.class

clean_metabolic_efm_tree_outcore:
	- rm $(DIR_METABOLIC_EFM_TREE_OUTCORE)/*.class

clean_metabolic_efm_tree_rankup:
	- rm $(DIR_METABOLIC_EFM_TREE_RANKUP)/*.class

clean_metabolic_efm_tree_search:
	- rm $(DIR_METABOLIC_EFM_TREE_SEARCH)/*.class
	
clean_metabolic_efm_tree_urank_root:
	- rm $(DIR_METABOLIC_EFM_ADJ_INCORE_TREE_URANK_ROOT)/*.class

clean_metabolic_efm_util:
	- rm $(DIR_METABOLIC_EFM_UTIL)/*.class

clean_metabolic_fa:
	- rm $(DIR_METABOLIC_FA)/*.class

clean_metabolic_generate:
	- rm $(DIR_METABOLIC_GENERATE)/*.class

clean_metabolic_impl:
	- rm $(DIR_METABOLIC_IMPL)/*.class

clean_metabolic_parse: clean_metabolic_parse_root clean_metabolic_parse_junit

clean_metabolic_parse_root:
	- rm $(DIR_METABOLIC_PARSE_ROOT)/*.class

clean_metabolic_parse_junit:
	- rm $(DIR_METABOLIC_PARSE_JUNIT)/*.class

clean_metabolic_sbml:
	- rm $(DIR_METABOLIC_SBML)/*.class

clean_metabolic_util:
	- rm $(DIR_METABOLIC_UTIL)/*.class

clean_smx: clean_smx_exception clean_smx_iface clean_smx_impl \
           clean_smx_ops clean_smx_util

clean_smx_exception:
	- rm $(DIR_SMX_EXCEPTION)/*.class

clean_smx_iface:
	- rm $(DIR_SMX_IFACE)/*.class

clean_smx_impl:
	- rm $(DIR_SMX_IMPL)/*.class

clean_smx_ops: clean_smx_ops_root clean_smx_ops_ext clean_smx_ops_jlapack \
               clean_smx_ops_matrix clean_smx_ops_mt clean_smx_ops_sscc

clean_smx_ops_root:
	- rm $(DIR_SMX_OPS_ROOT)/*.class

clean_smx_ops_ext:
	- rm $(DIR_SMX_OPS_EXT)/*.class

clean_smx_ops_jlapack:
	- rm $(DIR_SMX_OPS_JLAPACK)/*.class

clean_smx_ops_matrix:
	- rm $(DIR_SMX_OPS_MATRIX)/*.class

clean_smx_ops_mt:
	- rm $(DIR_SMX_OPS_MT)/*.class

clean_smx_ops_sscc:
	- rm $(DIR_SMX_OPS_SSCC)/*.class

clean_smx_util:
	- rm $(DIR_SMX_UTIL)/*.class


clean_xml: clean_xml_config clean_xml_factory

clean_xml_config:
	- rm $(DIR_XML_CONFIG)/*.class

clean_xml_factory:
	- rm $(DIR_XML_FACTORY)/*.class


clean_jbase: clean_jbase_root clean_jbase_concurrent clean_jbase_marshal \
             clean_jbase_util

clean_jbase_root:
	- rm $(DIR_JBASE_ROOT)/*.class

clean_jbase_concurrent:
	- rm $(DIR_JBASE_CONCURRENT)/*.class

clean_jbase_marshal:
	- rm $(DIR_JBASE_MARHSAL)/*.class

clean_jbase_util:
	- rm $(DIR_JBASE_UTIL)/*.class


clean_junit:
	- rm $(DIR_JUNIT_TEXTUI)/*.class

clean_math: clean_math_root clean_math_array clean_math_linalg \
            clean_math_operator clean_math_ops clean_math_varint

clean_math_root:
	- rm $(DIR_MATH_ROOT)/*.class

clean_math_array: clean_math_array_impl clean_math_array_parse \
                  clean_math_array_sort
	- rm $(DIR_MATH_ARRAY)/*.class

clean_math_array_impl:
	- rm $(DIR_MATH_ARRAY_IMPL)/*.class

clean_math_array_parse:
	- rm $(DIR_MATH_ARRAY_PARSE)/*.class

clean_math_array_sort:
	- rm $(DIR_MATH_ARRAY_SORT)/*.class

clean_math_linalg: clean_math_impl
	- rm $(DIR_MATH_LINALG)/*.class

clean_math_impl:
	- rm $(DIR_MATH_LINALG_IMPL)/*.class

clean_math_operator: clean_math_operator_compose clean_math_operator_impl
	- rm $(DIR_MATH_OPERATOR)/*.class

clean_math_operator_compose:
	- rm $(DIR_MATH_OPERATOR_COMPOSE)/*.class

clean_math_operator_impl:
	- rm $(DIR_MATH_OPERATOR_IMPL)/*.class

clean_math_ops:
	- rm $(DIR_MATH_OPS)/*.class

clean_math_varint: clean_math_varint_array clean_math_varint_ops
	- rm $(DIR_MATH_VARINT)/*.class

clean_math_varint_array:
	- rm $(DIR_MATH_VARINT_ARRAY)/*.class

clean_math_varint_ops:
	- rm $(DIR_MATH_VARINT_OPS)/*.class



clean_polymake: clean_polymake_parse

clean_polymake_parse:
	- rm $(DIR_POLYMAKE_PARSE)/*.class


clean_jsmat: clean_jsmat_root clean_jsmat_common clean_jsmat_primitive \
             clean_jsmat_variable

clean_jsmat_root:
	- rm $(DIR_JSMAT_ROOT)/*.class

clean_jsmat_common:
	- rm $(DIR_JSMAT_COMMON)/*.class

clean_jsmat_primitive:
	- rm $(DIR_JSMAT_PRIMITIVE)/*.class

clean_jsmat_variable:
	- rm $(DIR_JSMAT_VARIABLE)/*.class


clean_tool: clean_tool_root

clean_tool_root:
	- rm $(DIR_TOOL_ROOT)/*.class


clean_job: clean_job_root

clean_job_root:
	- rm $(DIR_JOB_ROOT)/*.class


clean_io: clean_io_root

clean_io_root:
	- rm $(DIR_IO_ROOT)/*.class

clean_factory: clean_factory_root

clean_factory_root:
	-rm $(DIR_FACTORY_ROOT)/*.class


clean_cdd: clean_cdd_parser

clean_cdd_parser:
	- rm $(DIR_CDD_PARSER)/*.class

clean_util: clean_util_root clean_concurrent clean_genarr clean_ints \
            clean_logging clean_logging_matlab clean_longs clean_map clean_numeric

clean_util_root:
	- rm $(DIR_UTIL_ROOT)/*.class

clean_concurrent:
	- rm $(DIR_UTIL_CONCURRENT)/*.class

clean_genarr:
	- rm $(DIR_UTIL_GENARR)/*.class

clean_ints:
	- rm $(DIR_UTIL_INTS)/*.class

clean_logging_matlab:
	- rm $(DIR_UTIL_LOGGING_MATLAB)/*.class

clean_logging:
	- rm $(DIR_UTIL_LOGGING)/*.class

clean_longs:
	- rm $(DIR_UTIL_LONGS)/*.class

clean_map:
	- rm $(DIR_UTIL_MAP)/*.class

clean_numeric:
	- rm $(DIR_UTIL_NUMERIC)/*.class

clean_lang: clean_lang_root clean_lang_management clean_lang_reflect

clean_lang_root:
	- rm $(DIR_LANG_ROOT)/*.class

clean_lang_management:
	- rm $(DIR_LANG_MANAGEMENT)/*.class

clean_lang_reflect:
	- rm $(DIR_LANG_REFLECT)/*.class


clean_bitset: clean_bitset_root clean_bitset_search clean_bitset_search_tree

clean_bitset_root:
	- rm $(DIR_BITSET_ROOT)/*.class

clean_bitset_search:
	- rm $(DIR_BITSET_SEARCH)/*.class

clean_bitset_search_tree:
	- rm $(DIR_BITSET_SEARCH_TREE)/*.class


clean_jmatio: clean_jmatio_common clean_jmatio_io clean_jmatio_types

clean_jmatio_common:
	- rm $(DIR_JMATIO_COMMON)/*.class

clean_jmatio_io:
	- rm $(DIR_JMATIO_IO)/*.class

clean_jmatio_types:
	- rm $(DIR_JMATIO_TYPES)/*.class

##############################################################################

javadoc:
	javadoc -d javadoc -subpackages ch.javasoft
