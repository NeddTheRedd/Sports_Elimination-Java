# Sports_Elimination-Java

Java program that determines the teams that have been mathematically eliminated from a tournament, leveraging information such as remaining games, wins, and losses. [BaseballElimination](https://github.com/NeddTheRedd/Sports_Elimination-Java/blob/main/BaseballElimination.java) utilizes the "Algorithms, 4th Edition" algs4 package to perform an analysis of a league of teams. The primary algorithm employed is the Ford-Fulkerson algorithm, which is utilized to calculate the maximum flow in a flow network.

---
### RUN THE PROGRAM

1. Compile the program: javac BaseballElimination.java
2. Run the tester:
   * java -cp .;algs4.jar BaseballElimination file.txt (Windows)
   * java -cp .:algs4.jar BaseballElimination file.txt (Linux or Mac) <br>

file.txt is replaced with the path to a test file from the [test_files](https://github.com/NeddTheRedd/Sports_Elimination-Java/tree/main/test_files) directory.

#### EXAMPLE: 

Input:<br>

<img width="375" alt="Screen Shot 2024-05-15 at 6 55 13 PM" src="https://github.com/NeddTheRedd/Sports_Elimination-Java/assets/153869055/08d426b5-0e78-49e8-9a04-dd9dd147a94e"><br>

Output:<br>

<img width="1116" alt="Screen Shot 2024-05-15 at 6 55 44 PM" src="https://github.com/NeddTheRedd/Sports_Elimination-Java/assets/153869055/209d32d7-dee8-43ef-afa4-46919acd6715"><br>

