# tscc
Source code for the Time Series aware Conformance Checking (TSCC) algorithm, 
described in the article "Conformance checking for time series aware processes", 
submitted to IEEE Transactions on Industrial Informatics. 

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
in terms of the size of the process model and the size of the log can be found 
in the file `org.uam.aida.tascc.examples.TSCC_Scalability`. Algorithm 4 from the
manuscript was run using, on the one hand, synthetic process models with a number 
of tasks ranging from 4 to 200, all of them with trivial ts-guards (they always 
return true), and on the other hand, univariate time series logs of different 
size, ranging from 100 to 10000 records:

![asd](https://i.imgur.com/ssLuWNd.png)

As it can be seen in the above figure, the computational time does not seem to 
be linear with respect to the number of procedural steps (tasks). However, it is 
remarkable that, regardless the log size, the computational time is stable for 
process models with less than 100 tasks, which are the most common in realistic 
environments. This experiment has been run on a machine with a Intel Core 
i5-4670K 3.4GHz (4 cores), 8GB RAM, running Windows 10 x64 and Java 1.8.
