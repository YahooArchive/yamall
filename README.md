# yamall
Yet Another MAchine Learning Library AKA Yahoo's Amazing MAchine Learning Library

## What is yamall?

yamall is a Java machine learning library, a fast black-box local learner, and a hadoop implementation.
It implements most of the state-of-the-art features used in machine learning algorithms, e.g. namespaces, hashing of the features, single and multipass stochastic gradient descent.


## Why yamall?

yamall comes from the necessity to have a secure Java implementation of state-of-the-art machine learning algorithms.
The local version tries to imitate the interface of [Vowpal Wabbit](http://github.com/JohnLangford/vowpal_wabbit), in order to facilitate the migration. At the same time, to harness the full power of yamall you can directly call the java functions in your code. Also, there is a Hadoop version to be able to scale to big datasets.

With very few lines of code you will be able to
* Parse VW and Tab Separated Value files.
* Instante a Stochastic Gradient Descent, choose optimization algorithm and loss function.
* Run multiple epochs on a dataset.
* Train your model locally and deploy it on the grid.


## Build instructions

yamall uses [Maven](http://maven.apache.org) as the build system. You will need
to install Maven on your machine.  The installation steps depend on your
operating system. On Linux, you can use your package manager; e.g. on Fedora,
run `sudo yum install maven`.

Once Maven is installed, you can use Maven to build it:

    cd yamall/
    mvn package


## Running locally

See the directory 'local'.


## Running on Hadoop

See the directory 'hadoop'.


## License

The use and distribution terms for this software are covered by the Apache 2.0 license. See LICENSE file for terms.
