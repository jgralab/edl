# EDL

The Extractor Description Language (EDL) can be used to create an extractor which creates a TGraph representation of a textual input.

## Building EDL

The project structure should be like this:

    jgsrc/               # your jgralab workspace
    +-> jgralab/         # jgralab itself
	+-> edl/             # this project

Before building you need to create a folder `lib` in the `edl` project and insert the [strategoxt.jar] (http://hydra.nixos.org/job/strategoxt-java/strc-java-trunk/build). To build `EDL` you need to have [Apache Ant](http://ant.apache.org/).

Build `jgralab` first and then `edl`.

    $ cd ../jgralab/
    $ ant
    $ cd ../jgralab/
    $ ant
    $ cd ../edl/
    $ ant

## Using `EDL`

Before edl can be used you need to have [sdf2table] (https://svn.strategoxt.org/repos/StrategoXT/spoofax-imp/trunk/org.strategoxt.imp.nativebundle/native/) and [graphviz] (http://www.graphviz.org/). Make sure that both programms can be called via command line via

    $ sdf2table -h
	$ dot -V

To generate an extractor from an `EDL` grammar call

    java de.uni_koblenz.edl.preprocessor.EDLPreprocessor -i <SearchPathOfModules> -m <StartModule> -o <OutputPath> -p <PackagePrefix> -s <UsedTGraphSchema>

The generated extractor can be used by calling

    java MyExtractor -o <OutputTGFile> (<InputFile> {<InputFile>})

For more options, use --help.

## License

Copyright (C) 2007-2012 The JGraLab Team <ist@uni-koblenz.de>

Distributed under the General Public License (Version 3), with the following
additional grant:

    Additional permission under GNU GPL version 3 section 7

    If you modify this Program, or any covered work, by linking or combining it
    with Eclipse (or a modified version of that program or an Eclipse plugin),
    containing parts covered by the terms of the Eclipse Public License (EPL),
    the licensors of this Program grant you additional permission to convey the
    resulting work.  Corresponding Source for a non-source form of such a
    combination shall include the source code for the parts of JGraLab used as
    well as that of the covered work.


<!-- Local Variables:        -->
<!-- mode: markdown          -->
<!-- indent-tabs-mode: nil   -->
<!-- End:                    -->
