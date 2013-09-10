Automatic Headline Generation
=============================

This project aims to automatically generate headlines of news articles in the
[DUC 2004](http://duc.nist.gov/duc2004/) corpus.
It's purpose was to get hands-on experience in the
[Natural Language Processing](https://www.systems.ethz.ch/courses/spring2013/intro_NLP)
course at [ETH Zurich](https://www.systems.ethz.ch/courses/spring2013/intro_NLP) in spring 2013.


Prerequisites
-------------

ROUGE depends on the XML::DOM perl module. On Ubuntu, install it using 

```bash
sudo apt-get install libxml-dom-perl
```

This project uses the Maven build system. On Ubuntu, install it using

```bash
sudo apt-get install maven
```

Usage
-----

Run the eval script to generate the headlines for all 500 documents in the DUC
2004 dataset. It computes and outputs the ROUGE score at the end.

```bash
./eval
```

Annotation Cache
----------------

Note that this distribution may not come with the cached annotations. Thus,
the initial run may take a long time (about an hour). Any subsequent runs
will only take 15 to 30 seconds.


Sentence Compression Model
--------------------------

The file 'rouge.model' in the root directory contains the model used for
sentence compression. It has been trained on the DUC 2003 data set using
SVM regression. To generate it on your own, run the RougeScoreRegression class 
in the 'learning' package.


Credits
-------

The code builds upon a range of excellent open source projects:

* [Stanford CoreNLP](http://nlp.stanford.edu/software/corenlp.shtml) serves as the underlying framework and takes care of tokenization, sentence splitting, part-of-speech tagging, named entity recognition and parsing
* [java-xmlbuilder](http://code.google.com/p/java-xmlbuilder/) for generating ROUGE configuration files
* [Google's Guava](http://code.google.com/p/guava-libraries/) provides excellent additions to Java's core libraries and encourages writing robust code
* [Logback](http://logback.qos.ch/) is a generic logging framework and the successor of the popular log4j project
* [SLF4J](http://www.slf4j.org/) serves as a simple facade for various logging frameworks like Logback
* [Apache Commons](http://commons.apache.org/)
* [kryo](http://code.google.com/p/kryo/) is a fast and efficient Java serialization library that provides an enormous performance boost by caching the parse trees of news articles
* [libsvm](http://www.csie.ntu.edu.tw/~cjlin/libsvm/) is a library for Support Vector Machines
* [ROUGE](http://www.berouge.com/Pages/default.aspx) is a software package for automated evaluation of summaries