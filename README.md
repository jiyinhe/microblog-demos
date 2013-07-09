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


Invoking Sample Runs
--------------------
After building, you can run any of the sample programs via somthing like this:

```
$ ./target/appassembler/bin/RunQueriesQL ./config/params_run.json 
```

which will run a simple baseline query likelihood retrieval.  All runnable programs are in ./target/appassembler/bin/ .  Also, all programs take a single argument: a JSON-formatted file that will look something like this:
```
{
"queries"      :  "./data/topics.microblog2012.txt",
"qrel_times"   :  "./data/oracle_epochs.txt",
"qrels"        :  "./data/qrels.microblog2012.txt",
"oracle_epochs":  "./data/oracle_epochs.txt",
"use_oracle"   :  false,
"host"         :  "ec2-54-234-186-72.compute-1.amazonaws.com",
"port"         :  9090,
"num_results"  :  1000,
"num_rerank"   :  5000,
"group"        :  "<your_group_here>",
"token"        :  "<your_token_here>",
"runtag"       :  "uiucGSLIS_01"
}
```

Most of these variables are self-explanatory.  A few aren't.  For instance ``qrel_times`` is used only for an oracle condition experiment and is probably not of interest for most people--just leave it blank.  The variable ``num_rerank`` specifies the maximum number of documents to consider for re-ranking per query.  The program will print no more than ``num_results`` per query.

License
-------

Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
