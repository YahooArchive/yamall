# yamall - Local mode

Assume the yamall-local-jar-with-dependencies.jar is in the directory local/target, as by building through maven.

For printing the instruction locally: 

    java -jar local/target/yamall-local-jar-with-dependencies.jar -h
    
For a simple run on an example training set try:

    java -jar local/target/yamall-local-jar-with-dependencies.jar resources/example_data/rcv1.train.100.vw
    
You would probably prefer to save your output model. You can do that like this:

    mkdir temp
    java -jar local/target/yamall-local-jar-with-dependencies.jar resources/example_data/rcv1.train.100.vw -f temp/rcv1.100.model1.vw
 
You can now use you model to retrain on the same data (for example) like this:

    java -jar local/target/yamall-local-jar-with-dependencies.jar resources/example_data/rcv1.train.100.vw -i temp/rcv1.100.model1.vw -f temp/rcv1.100.model2.vw

To test your model, you will need to load it and not train while running. That is, ignore the labels (both test and train files contain those)

    java -jar local/target/yamall-local-jar-with-dependencies.jar resources/example_data/rcv1.test.100.vw -i temp/rcv1.100.model2.vw -t

You can also save the actual predications for each example in the input file:
        
    java -jar local/target/yamall-local-jar-with-dependencies.jar resources/example_data/rcv1.test.100.vw -i temp/rcv1.100.model2.vw -t -p temp/rcv1.predictions.vw   

    
## Detailed command line options

| yamall options | Description |
| --- | --- |
| `-h,--help` | displays this help |

| Input options  | Description |
| --- | --- |
| `--ignore <arg>` | ignore namespaces beginning with the characters in <arg> |
| `--passes <arg>` | number of training passes |
| `--fmNumberFactors <agr>` | number of factors for Factorization Machines learner|

| Output options  | Description |
| --- | --- |
| `--binary` | reports loss as binary classification with -1,1 labels |
| `--link <arg>` | specify the link function used in the output of the predictions. Currently available ones are: identity (default), logistic |
| `--max_prediction <arg>` | smallest prediction to output, before the link function, default = 50 |
| `--min_prediction <arg>` | smallest prediction to output, before the link function, default = -50 |
| `-p,--predictions <arg>` | file to output predictions to |
| `-P,--progress <arg>` | progress update frequency, integer: additive; float: multiplicative, default = 2.0 |

| Weight options  | Description |
| --- | --- |
| `-b,--bit_precision <arg>` | number of bits in the feature table, default = 18 |
| `-f,--final_regressor <arg>` | final regressor to save |
| `--invert_hash <arg>` | output human-readable final regressor with feature names |
| `-i,--initial_regressor <arg>` | initial regressor(s) to load into memory |

| Training options | Description |
| --- | --- |
| `--cocob` | (EXPERIMENTAL) uses COCOB optimizer |
| `--holdout_period <arg>` | holdout period for test only, default = 10 |
| `--kt` | (EXPERIMENTAL) uses KT optimizer |
| `-l,--learning_rate <arg>` | set (initial) learning Rate, default = 1.0 |
| `--loss_function <arg>` | specify the loss function to be used. Currently available ones are: absolute, squared (default), hinge, logistic |
| `--pccocob` | (EXPERIMENTAL) uses Per Coordinate COCOB optimizer |
| `--pcsolo` | uses Per Coordinate SOLO optimizer |
| `--pistol` | uses PiSTOL optimizer |
| `--solo` | uses SOLO optimizer | 
| `--fm` | uses Two-way Factorization Machines |    
| `-t` | ignore label information and just test | 
