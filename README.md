# tEFMA
tEFMA calculates thermodynamically feasible elementary flux modes

If you use this software please cite 

> Matthias P. Gerstl, Christian Jungreuthmayer, and Jürgen Zanghellini
> tEFMA: computing thermodynamically feasible elementary flux modes in metabolic networks
> Bioinformatics 2015 : btv111v1-btv111. 

This README covers the following sections:

1. DESCRIPTION
2. PREPARATION
3. INSTALLATION
4. COMPILATION
5. HOW TO RUN THE PROVIDED TEST CASES
6. MISCELLANEOUS

## 1. DESCRIPTION

tEFMA calculates with metabolite concentration data theryodynamically consistent elementary flux modes. tEFMA is an extension of the original efmtool by Marco Terzer [http://www.csb.ethz.ch/tools/efmtool/](http://www.csb.ethz.ch/tools/efmtool/ "efmtool").

tEFMA is written in Java and, hence, a multi-platform program (Linux, MacOS X, Windows,...). This README only covers installation, compilation and operation under Linux.  We tested tEFMA on Ubuntu Linux 12.04, 14.04 and MacOS X 10.7. However, installation and usage of tEFMA on other platforms is supposed to be easy.

### Important:
The implementation of tEFMA is only a proof of concept. We tried to implement the filtering of modes as non-invasive as possible. Hence, parts of the new code disobey the standard best practice guidelines, but might assist a potential programmer in incorporating her/his own new ideas. Additionally, a more thorough implementation will definitely result in a performance gain. Of course, the results obtained by this version of tEFMA were double-checked for plausibility and correctness. We are not aware of any bugs or that the extension to the original efmtool produces incorrect results. But as any humble programmer we are sure tEFMA contains bugs. If you find bugs please report them to matthias.gerstl@boku.ac.at or christian.jungreuthmayer@acib.at.

## 2. PREPARATION

If you want to use the thermodynamic options of this tool IBM CPLEX is needed. Information can be found at [http://www-03.ibm.com/software/products/en/ibmilogcpleopti](http://www-03.ibm.com/software/products/en/ibmilogcpleopti "IBM cplex"). Academic licenses are available by IBM on request.
Please install CPLEX according to IBM's instructions first. 


## 3. INSTALLATION

tEFMA can be downloaded from github [https://github.com/mpgerstl/tEFMA.git](https://github.com/mpgerstl/tEFMA.git "tEFMA.git").

## 4. COMPILATION

As tEFMA needs to be linked to CPLEX we do not provide a pre-compiled version.
Therefore it is necessary to compile tEFMA. 
Open Makefile with your favorite editor and 

* adapt the variables JAR and JAVAC according to your system installation.
* adapt the variable JAVAC\_FLAGS and set the correct path to cplex.jar.

Open META-INF MANIFEST.MF with your favorite editor and 

* adapt the path to cplex.jar.

Save the changes and simply execute:

```
make
make jarfile
```

### Note:

1. We only tested tEFMA with JDK-1.7.
2. tEFMA does not work with Java version less than JDK-1.7.
3. Other java versions have not been used by us, yet.

In order to delete all compiled class files execute:

```
make clean
```

## 5. HOW TO RUN THE PROVIDED TEST CASES

This software also comes with some test cases which can be found in the folder 'examples'. To run the test cases adapt the start scripts by changing
the library path for cplex.

If you experience problems make sure that the program 'java' is installed on your system and that the PATH variable is set accordingly.

## 6. MISCELLANEOUS

Further information on thermodynamics can be found in the file README\_THERMODYNAMIC
