Automatic Headline Generation
=============================

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
