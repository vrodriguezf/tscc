# tscc
Source code for the Time Series aware Conformance Checking (TSCC) algorithm, 
described in the article "Conformance checking for time series aware processes", 
accepted in IEEE Transactions on Industrial Informatics. 

## Requisites
This project is based on [Maven](https://maven.apache.org/), and it is written
in Java JDK 8. An easy way to have both requisites prepared in your system is to use
a docker container such as [this one](https://github.com/carlossg/docker-maven/blob/d3dd6bc261c6173c5e52e3a7a36b6a3d8d2800b4/jdk-8/Dockerfile).

## Build an executable JAR file
1. Open a terminal and go to the root folder of the project.
2. Run `mvn assembly:assembly`

The executable JAR file is `target/tscc-1.0-SNAPSHOT-jar-with-dependencies.jar`. 

NOTE: This executable will run the TSCC algorithm over the process model shown in
the experimentation of the article:
![asd](https://i.imgur.com/16XcCV4.png)

## Execution
The command to execute the time series-aware conformance checking algorithm is:
```
java -jar target `target/tscc-1.0-SNAPSHOT-jar-with-dependencies.jar` [INPUT_FILEPATH.csv] [MODEL_FILEPATH.pnml]
[OUTPUT_FILEPATH.csv] [CONFIG_FILEPATH.toml]
```

### Input file
The input of TSCC is a case of a time series log, given as a CSV file with 3 columns:
1. `time_index`: Timestamp or integer ordered.
2. `variable`: Name of the variable
3. `value`: Value of the variable `variable` at time `time_index`

An example of input file can be found in the file `example_shearer_input.csv`. 

**NOTE:** The data shown in this file does not represent the experimentation data used
in the the paper, nor the names of the variables. The real data has not been published 
due to a confidential agreement.

### Model file
The description of the process model is given as a [Petri Net Markup Language (PNML)](http://www.pnml.org/)
file. See the section `Creating your own process model` to see how to create a process model
for your needs.

An example of model file can be found in the file `example_shearer_model.pnml`.

### Output file
The output of TSCC is a CSV file where every row represents the evaluation of a 
task of the process model. There are 3 columns:
1. `type`: Conformance class assigned to the task. It can be `MATCH`, `TIME_MISMATCH` 
or `ABSENCE`. 
2. `task`: Name of the task
3. `timeOfFirstFulfillment`: Time index when the task was fulfilled for the first
time during the algorithm execution. See Time of First Fulfillment (TFF) in the article
for more information about this value.

An example of output file in the context of the experimentation shown in the paper 
can be found in the file `example_shearer_output.csv`:
```csv
type,task,timeOfFirstFulfillment
ABSENCE,Phase 1 [start],1515857545
MATCH,Phase 2 [start],1515857615
ABSENCE,Phase 2 [end],1515857615
MATCH,Phase 4 [end],1515858045
ABSENCE,Phase 3 [start],1515857615
MATCH,Phase 1 [end],1515857595
MATCH,Phase 3 [end],1515857635
MATCH,Phase 4 not present,1515861285
ABSENCE,Phase 4 [start],1515857635
```

**NOTE:** This output is not associated to the input `example_shearer_input.csv`.

### Config file
The configuration file is a TOML file where all the parameters that control the 
execution of the TSCC algorithm are set. See the file `example_config_tscc.toml` 
to see a complete example with the process model `SHEERER_SINGLE_CYCLE` used in
the experimentation of the article.

Note that, apart from being able to establish the configuration of all the time 
series guards under the tag `[TSG]`, one can override the specific configuration 
of one single TSG under the tag `[processModel.activities.transition.tsg]`.

## Creating your own process model

The GUI for creating process models is based on the [PetriNetSim](https://github.com/zamzam/PetriNetSim)repository.

1. Open the project with [Netbeans IDE](https://netbeans.org/).
2. Choose the run configuration ``GUI`` and press Run (green button). You will 
be prompted an interface for designing your TSWF-Net.

![asd](https://i.imgur.com/QvizSPb.png)

3.  Right-click anywhere in the interface and set a name for you net within the 
text box `Label`.
![asd](https://i.imgur.com/8Fb35Er.png)

For more details about how to add places, transitions and arcs to your net see [this article](https://upcommons.upc.edu/bitstream/handle/2099.1/8965/Memoria.pdf?sequence=1&isAllowed=y).
NOTE: The article is not in English.

4. To add time scopes and time series guards to the transitions of the net, right-click
on a transition an go to the `TSWF-Net` tab.
![asd](https://i.imgur.com/i1116Bq.png)

5. Time series guards are Java classes that extend the abstract class `TSG`, found 
in `src/main/java/org/uam/aida/tscc/APFE/timeseries_guards`. That folder contains also
a list of already implemented time series guards, for checking common conditions such as:
    * `=, !=, >, <, <=, >=`
    * Monotonicity: Increasing/Decreasing/Constant values.
    * Composition of atomic time series guards: `AND, OR, NOT`.
*NOTE*: All the currently implemented time series guards extend from the class `UnivariateTSG`, so they 
can only be used to check conditions over one variable of the time series log. If one would want
to check several variables at once, a `ComposedTSG` (e.g., `AND`) should be used.

6. When the TSWF-Net is finished, save it as a `.pnml` file.

7. Now you can use the PNML model as an argument of the call to the TSCC algorithm 
(See the *Execution* section of this README). If there is any error in the code of
any of the TSGs, they will be prompted when executing the algorithm.

## Synthetic models and scalability test

An empirical study of how the computational time of the proposed algorithm varies 
in terms of the size of the process model and the complexity of the time series
guards can be found in the file `org.uam.aida.tscc.examples.TSCC_Scalability`. Algorithm 4 from the
manuscript was run using, on the one hand, synthetic process models with a number 
of tasks ranging from 5 to 500, and on the other hand, time series guards whose 
fulfillment requires checking a number of records ranking from 10 to 1000. For 
each iteration of the scalability test, all the transitions in the model have the 
same time series guard, namely an instance of `EqualsTSG`, which checks that any 
record in the log is equal to 0. The log is created synthetically, with one variable and
with a size according to the number of tasks and the load of the time series guards.
The time scope of each transition is adjusted to the size of the log in each iteration of the test.

![asd](https://i.imgur.com/ssLuWNd.png)

As it can be seen in the above figure, the computational time does not seem to 
be linear with respect to the number of procedural steps (tasks). However, it is 
remarkable that, regardless the load of the time series guards, the computational 
time is stable for process models with less than 100 tasks, which are the most common in realistic 
environments. This experiment has been run on a machine with a Intel Core 
i7-6500U CPU @ 2.50 GHz (4 cores), 16GB RAM, running Ubuntu 18.04 x64 and Java 1.8.

To run an scalability experiment, following the steps below:

1. Open the project with [Netbeans IDE](https://netbeans.org/).
2. In the Projects panel, right-click on the project `tscc` and click on 
`Project properties`.
3. In the project properties window, go to `Build->Run` and select the configuration
`Scalability` on the drop-down menu.
4. Specify the arguments you want for the scalability test:
    1. Minimum load of the time series guards, defined in terms of number of log records.
    2. Step in the definition of the TSG load.
    3. Maximum load of the time series guards.
    4. Minimum number of tasks to create in the synthetic process model.
    5. Step in the definition of the number of tasks for the creation of 
        subsequent synthetic process model.
    6. Maximum number of tasks to create the synthetic process model.
    7. Output file path (`CSV` file).

As an example, with the arguments `100 100 1000 5 5 500`, the program will
run the algorithm first with a TSG load of 100 records and a process model of 5 tasks,
next with a TSG load of 100 records and a process model of 10 tasks, and it will end
after some iterations running the algorithm with a TSG load of 1000 records and a 
process model of  500 tasks.

The output file will contain 3 columns with no names:
1. Number of tasks in the process model.
2. TSG load.
3. Computational time.

That information can be used to plot figures such as the one shown above.

NOTE: In case one wants to modify the way the synthetic models/logs are created, the
corresponding Java classes are `org.uam.aida.tscc.APFE.time_series_log.SyntheticIndexedLog` and
`org.uam.aida.tscc.APFE.TSWFnet`.