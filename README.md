# Scheduling CW

Hi! 

This program was written for the Scheduling and Resource Allocation course in Imperial. It is attempting to solve a scheduling problem of minimizing total tardiness of a workflow with precedences, using a single machine. 

For Question 2 (in folder /q2), a branch-and-bound algorithm is implemented.
The code for Question 2 is found in ../q2/Scheduler.java.  
The output of q2 is written to q2Output.txt, and the schedule obtained from running the code is in bnb.csv and bnb.json. 
The schedule derived from Hu's algorithm is found in hu.csv and hu.json.

For Question 3 (in folder /q3), further improvements are made to the algorithm to improve its speed and memory usage efficiency. 
Q3: The code for Q3 is found in ../q3/Scheduler.java.  
The output of q3 is written to q3Output.txt, and the schedule obtained from running the code is in bnbImproved.csv and bnbImproved.json. 

To run the code yourself, please either run it in a suitable IDE (e.g. IntelliJ) or run the following commands in the scheduling-cw directory:
```
cd src/q2
java Scheduler.java
cd ..
cd q3
java Scheduler.java
```
