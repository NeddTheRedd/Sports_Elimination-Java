/*
   To conveniently test the algorithm with a large input, create a text file
   containing one or more test divisions (in the format described below) and run
   the program with
	java -cp .;algs4.jar BaseballElimination file.txt (Windows)
   or
	java -cp .:algs4.jar BaseballElimination file.txt (Linux or Mac)
   where file.txt is replaced by the name of the text file.
   
   The input consists of an integer representing the number of teams in the division and then
   for each team, the team name (no whitespace), number of wins, number of losses, and a list
   of integers represnting the number of games remaining against each team (in order from the first
   team to the last). That is, the text file looks like:
   
	<number of teams in division>
	<team1_name wins losses games_vs_team1 games_vs_team2 ... games_vs_teamn>
	...
	<teamn_name wins losses games_vs_team1 games_vs_team2 ... games_vs_teamn>

	
   An input file can contain an unlimited number of divisions but all team names are unique, i.e.
   no team can be in more than one division.
*/

import edu.princeton.cs.algs4.*;
import java.util.*;
import java.io.File;

//Do not change the name of the BaseballElimination class
public class BaseballElimination {

	// We use an ArrayList to keep track of the eliminated teams.
	public ArrayList<String> eliminated = new ArrayList<String>();

	/*
	 * BaseballElimination(s)
	 * Given an input stream connected to a collection of baseball division
	 * standings we determine for each division which teams have been eliminated
	 * from the playoffs. For each team in each division we create a flow network
	 * and determine the maxflow in that network. If the maxflow exceeds the number
	 * of inter-divisional games between all other teams in the division, the
	 * current team is eliminated.
	 */
	public BaseballElimination(Scanner s) {
		int numRows = s.nextInt();
		int numColumns = numRows + 3;
		// Skip rest of first line
		s.nextLine();

		// Check if there is only one team in the division
		if (numRows == 1) {
			return;
		}

		Map<Integer, String> countryRowMap = new HashMap<>();
		// Track currently eliminated
		ArrayList<String> curEliminated = new ArrayList<String>();

		// Populate a league matrix
		int[][] matrix = new int[numRows][numColumns];
		for (int i = 0; i < numRows; i++) {
			for (int j = 0; j < numColumns; j++) {
				if (j == 0) {
					matrix[i][j] = i + 1;
					// Populate hashmap with (country -> row number)
					String countryName = s.next();
					countryRowMap.put(i + 1, countryName);
				} else {
					matrix[i][j] = s.nextInt();
				}
			}
			if (s.hasNextLine()) {
				s.nextLine();
			}
		}
		// Create an array of games played + games remaining
		ArrayList<Integer> numGames = new ArrayList<Integer>();
		for (int i = 0; i < numRows; i++) {
			numGames.add(matrix[i][1] + matrix[i][2]);
		}

		// Add to eliminated if W < wi
		for (int i = 0; i < numRows; i++) {
			for (int j = 0; j < numRows; j++) {
				if (numGames.get(j) < matrix[i][1] && !curEliminated.contains(countryRowMap.get(j + 1))) {
					curEliminated.add(countryRowMap.get(j + 1));
				}
			}
		}

		// Determines formula for the number match vertices
		int teamCombinations = chooseOperation((numRows - 1), 2);

		// Definitions:
		int source = 0;
		int sink = teamCombinations + numRows + 1;
		int vsTeam = 1;
		int teams = 1;

		// Arrays and maps
		Map<String, FlowNetwork> leagueNetworks = new HashMap<>();
		List<int[][]> teamMatrices = new ArrayList<>();
		int[][] teamMatrix = new int[numRows - 1][numColumns - 1];

		// Populate teamMatrices with current team data removed
		for (int i = 0; i < numRows; i++) {
			teamMatrix = removeRowAndColumn(matrix, i, i + 3);
			teamMatrices.add(teamMatrix);
		}

		// Create flow edges and flow networks
		for (int teamIndex = 0; teamIndex < teamMatrices.size(); teamIndex++) {
			if (!curEliminated.contains(countryRowMap.get(teamIndex + 1))) {
				FlowNetwork teamNetwork = new FlowNetwork(sink);
				// Create network for country i
				for (int i = 0; i < numRows - 2; i++) {
					for (int j = i; j < numRows - 2; j++) {
						FlowEdge divisionalGame = new FlowEdge(source, vsTeam,
								(double) teamMatrices.get(teamIndex)[i][j + 4], 0.0);
						FlowEdge team_a = new FlowEdge(vsTeam, teamCombinations + i + 1, Double.POSITIVE_INFINITY, 0.0);
						if (teams <= i) {
							teams = i + 1;
						}
						FlowEdge team_b = new FlowEdge(vsTeam, teamCombinations + teams + 1, Double.POSITIVE_INFINITY,
								0.0);
						vsTeam++;
						teams++;
						if (vsTeam > teamCombinations) {
							vsTeam = 1;
						}
						if (teams >= numRows - 1) {
							teams = 1;
						}
						// Add to teamNetwork
						teamNetwork.addEdge(divisionalGame);
						teamNetwork.addEdge(team_a);
						teamNetwork.addEdge(team_b);
					}
				}
				// Add edges that connect to the sink
				for (int k = 0; k < numRows - 1; k++) {
					double capacity = (double) (matrix[teamIndex][1] + matrix[teamIndex][2]
							- teamMatrices.get(teamIndex)[k][1]);
					if (capacity <= 0) {
						capacity = 0;
					}
					FlowEdge teamToSink = new FlowEdge(teamCombinations + k + 1, sink - 1, capacity, 0.0);
					teamNetwork.addEdge(teamToSink);
				}
				leagueNetworks.put(countryRowMap.get(teamIndex + 1), teamNetwork);
				String mapAsString = leagueNetworks.entrySet().toString();
			}
		}
		// Check if the flow value for each network matches the sum capacity of the
		// edges leaving the source
		for (int i = 0; i < countryRowMap.size(); i++) {
			if (leagueNetworks.containsKey(countryRowMap.get(i + 1))) {
				FlowNetwork curFlowNet = leagueNetworks.get(countryRowMap.get(i + 1));
				FordFulkerson curTeamNetwork = new FordFulkerson(curFlowNet, source, sink - 1);
				double value = curTeamNetwork.value();

				// Get the adjacency list of the source vertex
				Iterable<FlowEdge> sourceEdges = curFlowNet.adj(source);
				double totalCap = 0;
				for (FlowEdge edge : sourceEdges) {
					totalCap += edge.capacity();
				}
				// Add to eliminated array
				if (value != totalCap) {
					curEliminated.add(countryRowMap.get(i + 1));
				}
			}
		}
		// Sort them according to their key value in countryRowMap
		for (int i = 1; i <= numRows; i++) {
			if (curEliminated.contains(countryRowMap.get(i))) {
				eliminated.add(countryRowMap.get(i));
			}
		}
	}

	// Function to calculate C(n, r)
	public static int chooseOperation(int n, int r) {
		// Base cases
		if (r == 0 || r == n) {
			return 1;
		} else {
			return chooseOperation(n - 1, r - 1) + chooseOperation(n - 1, r);
		}
	}

	public static int[][] removeRowAndColumn(int[][] matrix, int rowIndex, int colIndex) {
		int numRows = matrix.length - 1;
		int numCols = matrix[0].length - 1;
		int[][] newMatrix = new int[numRows][numCols];

		// Make new matrix
		for (int i = 0, newRow = 0; i < matrix.length; i++) {
			// Skip rowIndex
			if (i == rowIndex) {
				continue;
			}
			for (int j = 0, newCol = 0; j < matrix[0].length; j++) {
				// Skip colIndex
				if (j == colIndex) {
					continue;
				}
				newMatrix[newRow][newCol] = matrix[i][j];
				newCol++;
			}
			newRow++;
		}
		return newMatrix;
	}

	/*
	 * main()
	 * Contains code to test the BaseballElimantion function. You may modify the
	 * testing code if needed, but nothing in this function will be considered
	 * during marking, and the testing process used for marking will not
	 * execute any of the code below.
	 */
	public static void main(String[] args) {
		Scanner s;
		if (args.length > 0) {
			try {
				s = new Scanner(new File(args[0]));
			} catch (java.io.FileNotFoundException e) {
				System.out.printf("Unable to open %s\n", args[0]);
				return;
			}
			System.out.printf("Reading input values from %s.\n", args[0]);
		} else {
			s = new Scanner(System.in);
			System.out.printf("Reading input values from stdin.\n");
		}

		BaseballElimination be = new BaseballElimination(s);

		if (be.eliminated.size() == 0)
			System.out.println("No teams have been eliminated.");
		else
			System.out.println("Teams eliminated: " + be.eliminated);
	}
}
