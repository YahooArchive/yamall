# yamall - Hadoop mode

# Options
yamall.ignore arg - ignores the namespaces whose letter begins with the letters in arg

yamall.bit_precision - how many bits are used for the hashing. Default = 18

yamall.parser - which parser to use, available choices: vw and tsv. Default = vw

yamall.parser_spec - location schema file on hdfs for tsv format.

The options are passed with -Doption=value. For example -Dyamall.ignore=abc

# Training
`hadoop jar yamall-hadoop-jar-with-dependencies.jar com.yahoo.labs.yamall.hadoop.Train OPTIONS TRAIN_DIR OUTPUT_DIR`

It will save a model.bin and a model.txt in the OUTPUT_DIR.

# Testing
`hadoop jar yamall-hadoop-jar-with-dependencies.jar com.yahoo.labs.yamall.hadoop.Test OPTIONS TEST_DIR OUTPUT_DIR MODEL_FILE`

It will save the scores and the AUC in the OUTPUT_DIR.