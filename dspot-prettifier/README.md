# post-dspot

Post-dspot processes [DSpot](http://github.com/STAMP-project/dspot.git)'s output to make it readable.

The process has 3 steps: 

1. Minimize the test method by removing all the "useless" statements.
2. Rename the test method. This is done using [code2vec](https://github.com/tech-srl/code2vec).
3. Rename the local variable used in the the test method. This is done based on [context2name](https://github.com/rbavishi/Context2Name).

## Command Line Usage
```
java -jar /path/to/dspot-prettifier-LATEST-jar-with-dependencies.jar --apply-all-prettifiers --absolute-path-to-project-root=<path> --path-to-amplified-test-class=<path>
```

The prettifier uses DSpot in the background, you can pass all arguments that you can also pass to DSpot.

### Configure Prettifiers
To select which prettifiers should be applied to the amplified test case you can pass these options:
```
--apply-all-prettifiers
                       Apply all available prettifiers. This overrides options that turn off specific
                         prettifiers. Default value: false
--apply-general-minimizer
                       Apply the general minimizer to remove redundant assertions and inline local variables.
                         Default value: false
--apply-pit-minimizer  Apply the pit minimizer to remove assertions that do not improve the mutation score.
                         Default value: false
--rename-local-variables
                       Apply Context2Vec to give the local variables more expressive names. Default value: false
--rename-test-methods  Apply Code2Vec to give the test methods more expressive names. Default value: false
```

To learn about the prettifiers based on Code2Vec and Context2Name, please consult the [readme-code2vec-context2name](readme-code2vec-context2name.md).
