microblog-demos
===============

Examples of using the [2013 TREC microblog API](http://twittertools.cc/). Includes a few temporal reranking feature.

Getting Stated
--------------

Once you've cloned the repository, build the package with Maven:

```
$ mvn clean package appassembler:assemble
```

Appassembler will automatically generate launch scripts for three classes:

+ `target/appassembler/bin/RunQueriesQL`: baseline run
+ `target/appassembler/bin/RunQueriesRecency`: recency prior
+ `target/appassembler/bin/RunQueriesKernelDensity`: kernel density estimation

To automatically generate project files for Eclipse:

```
$ mvn eclipse:clean
$ mvn eclipse:eclipse
```

You can then use Eclipse's Import "Existing Projects into Workspace" functionality to import the project.

License
-------

Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
