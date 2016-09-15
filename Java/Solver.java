import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

/**
 * Credit for this algorithm goes to Peter Norvig http://norvig.com/sudoku.html
 */
public class Solver {

    private static char[] digits = { '1','2','3','4','5','6','7','8','9' };
    private static char[] cols = digits;
    private static char[] rows = { 'A','B','C','D','E','F','G','H','I' };
    private static String[] colChunks = { "123", "456", "789" };
    private static String[] rowChunks = { "ABC", "DEF", "GHI" };
    private static String puzzleDir = "Puzzles/";
    private static String solutionDir = "Solutions/";

    private static ArrayList<String> squares;
    private static ArrayList<ArrayList<String>> unitList;
    private static HashMap<String, ArrayList<ArrayList<String>>> units; // Dictionary
    private static HashMap<String, HashSet<String>> peers;

    public static void main(String args[]) {

        System.out.print("Please enter the number of puzzles you would like and press [ENTER]: ");
        Scanner sc = new Scanner(System.in);
        int in;
        while (!sc.hasNextInt()) sc.next();

        in = sc.nextInt();
        for (int i = 0; i < in; i++) {

            SolveSudoku(i + 1);
        }
    }

    /**
     * Generates a random puzzle with a minimum of 17 values
     * Recursively produces puzzles until a legal puzzle is produced
     * @return
     */
    private static char[][] RandomPuzzle() {

        int n = 17;
        HashMap<String, String> values = new HashMap<>();

        for (String s : squares) {

            values.put(s, new String(digits));
        }

        // Shuffle all squares in the board (A1 - I9) and assign values at random
        for (String s : Shuffled(squares)) {

            Random generator = new Random();
            int randomValue = generator.nextInt(9) + 1;

            if (Assign(values, s, "" + randomValue) == null) {

                break;
            }

            // If a square has one possible value, see if the puzzle is valid
            if (values.get(s).length() == 1) {

                HashMap<String, String> ds = new HashMap<>();
                for (String s1 : squares) {

                    if (values.get(s1).length() == 1) {

                        ds.put(s1, values.get(s1));
                    }
                }

                if (ds.size() >= n) {

                    HashSet<String> set = new HashSet<>();
                    for (String s1 : squares) {

                        if (set.size() >= 8) { break; }

                        if (ds.containsKey(s1) && ds.get(s1).length() == 1) {

                            set.add(ds.get(s1));
                        }
                    }

                    // Check if there are at least 8 distinct digits in the puzzle
                    if (set.size() >= 8) {

                        if (values.get(s).length() == 1) {

                            HashMap<String, Character> tmp = new HashMap<>();
                            for (String s1 : squares){

                                if (ds.containsKey(s1) && ds.get(s1).length() == 1) {

                                    char charTemp = ds.get(s1).charAt(0);
                                    tmp.put(s1, charTemp);
                                }
                                else {

                                    tmp.put(s1, '.');
                                }
                            }

                            // Convert to 2D for output
                            char[][] out = new char[9][9];
                            int indexA = 0;
                            int indexB = 0;
                            for (String s1 : squares) {

                                char input = (char) tmp.get(s1);
                                out[indexA][indexB] = input;
                                indexA++;
                                indexA = indexA % 9;
                                if (indexA == 0) {

                                    indexB++;
                                }
                            }

                            return out;
                        }
                    }
                }
            }
        }

        // If no valid puzzle was made, try again
        return RandomPuzzle();
    }

    /**
     * randomly reorders the list of squares given
     * @param seq
     * @return
     */
    private static ArrayList<String> Shuffled(ArrayList<String> seq) {

        if (seq == null) {

            return null;
        }
        else {

            // Make a copy of the thing to shuffle (so we don't mess up the originial)
            ArrayList<String> copy = new ArrayList<>();
            for (String s : seq) {

                copy.add(s);
            }
            Collections.shuffle(copy);
            return copy;
        }
    }

    /**
     * Initializes the primary variables
     * Calls to produce, solve and print the resulting puzzles
     * @param puzzleNum
     */
    public static void SolveSudoku(int puzzleNum) {

        // Instantiate static variables from the class
        squares = Cross(rows, cols);
        unitList = BuildUnitList();
        units = new HashMap<>();
        for (String s : squares) {

            units.put(s, new ArrayList<>());

            for (ArrayList<String> u : unitList) {

                if (u.contains(s)){

                    units.get(s).add(u);
                }
            }
        }

        peers = new HashMap<>();
        for (String s : squares) {
            HashSet<String> tmp = new HashSet();

            units.get(s).forEach(tmp::addAll);

            tmp.remove(s);

            peers.put(s, tmp);
        }

        // Try to make a random puzzle
        char[][] puzzle = RandomPuzzle();

        // File IO
        File puzzleFolder = new File(puzzleDir);
        puzzleFolder.mkdir();
        File solutionFolder = new File(solutionDir);
        solutionFolder.mkdir();
        try {

            String fileName = "Puzzle " + puzzleNum;

            PrintWriter puzWr = new PrintWriter(puzzleDir + fileName + ".txt");
            PrintPuzzle(ParseToPuzzle(puzzle), puzWr, puzzleNum);

            PrintWriter solWr = new PrintWriter(solutionDir + fileName + ".txt");
            PrintSolution(Search(ParseGrid(puzzle)), solWr, puzzleNum);
        } catch (FileNotFoundException e) {}
    }

    /**
     * Prints the unsolved puzzle
     *
     * @param values
     * @param pw
     * @param puzzleNum
     * @return
     */
    private static boolean PrintPuzzle(HashMap<String, String> values, PrintWriter pw, int puzzleNum) {

        if (values == null) {
            return false;
        }

        pw.println("Puzzle number " + puzzleNum);

        int max = 0;
        for (String s : squares) {

            int tmp = values.get(s).length();
            if (tmp > max) {

                max = tmp;
            }
        }

        for (char r : rows) {

            for (char c : cols) {

                if (c == '1') {

                    pw.print(" ");
                }

                String coord = "" + r + c;

                pw.print(values.get(coord) + " ");

                if (c == '3' || c == '6') {

                    pw.print("| ");
                }
            }

            pw.println();

            if (r == 'C' || r == 'F') {

                pw.println("-------+-------+-------");
            }
        }
        pw.println();

        pw.close();

        return true;
    }

    /**
     *
     * Prints the solved puzzle
     * @param values
     * @param pw
     * @param puzzleNum
     * @return
     */
    private static boolean PrintSolution(HashMap<String, String> values, PrintWriter pw, int puzzleNum) {

        if (values == null) {
            return false;
        }

        pw.println("Solution to puzzle number " + puzzleNum);

        int max = 0;
        for (String s : squares) {

            int tmp = values.get(s).length();
            if (tmp > max) {

                max = tmp;
            }
        }

        for (char r : rows) {

            for (char c : cols) {

                if (c == '1') {

                    pw.print(" ");
                }

                String coord = "" + r + c;
                pw.print(values.get(coord) + " ");

                if (c == '3' || c == '6') {

                    pw.print("| ");
                }
            }

            pw.println();

            if (r == 'C' || r == 'F') {

                pw.println("-------+-------+-------");
            }
        }
        pw.println();

        pw.close();

        return true;
    }

    /**
     * Produces the square identifier names
     * @param c1
     * @param c2
     * @return
     */
    private static ArrayList<String> Cross(char[] c1, char[] c2) {

        ArrayList<String> out = new ArrayList<>(c1.length * c2.length);

        for (char i : c1) {

            for (char j : c2) {

                out.add("" + i + j);
            }

        }

        return out;
    }

    /**
     * Produces the list of units:
     * rows, columns, and 3x3 squares
     * @return
     */
    private static ArrayList<ArrayList<String>> BuildUnitList(){

        ArrayList<ArrayList<String>> u1 = new ArrayList<ArrayList<String>>();

        for (char c : cols){

            u1.add(Cross(rows, new char[] { c }));
        }

        for (char r : rows) {

            u1.add(Cross(new char[] { r }, cols));
        }

        for (String cc : colChunks){

            for (String rc : rowChunks) {

                u1.add(Cross(rc.toCharArray(), cc.toCharArray()));
            }
        }

        return u1;
    }

    /**
     * Produces the array of possible correct values for each square
     * @param grid
     * @return
     */
    private static HashMap<String, String> ParseGrid(char[][] grid) {

        // Creates the array before applying the known puzzle values
        char[] tmp = new char[grid.length * grid[0].length];

        // Flatten the board
        int index = 0;
        for (int i = 0; i < grid.length; i++) {

            for (int j = 0; j < grid[0].length; j++) {

                tmp[index] = grid[i][j];
                index++;
            }
        }
        String gridString = new String(tmp);

        // Store the board in a hashmap with the sqare IDs (A1 - I9) as keys
        HashMap<String, String> values = new HashMap<>();
        for (String s : squares) {

            values.put(s, new String(digits));
        }

        // trims the possible values based on known puzzle values
        HashMap<String, String> gridVals = GridValues(gridString);

        // Try to assign values and check for contradictions
        for (String s : squares) {
            boolean tmpbool = String.valueOf(digits).contains(gridVals.get(s).toString());
            if (tmpbool && Assign(values, s, gridVals.get(s)) == null) {

                return null;
            }
        }

        return values;
    }

    /**
     * Creates a printable version of the unsolved puzzle
     * @param grid
     * @return
     */
    private static HashMap<String, String> ParseToPuzzle(char[][] grid) {

        // Flatten 2D grid to string
        char[] tmp = new char[grid.length * grid[0].length];
        int index = 0;
        for (int i = 0; i < grid.length; i++) {

            for (int j = 0; j < grid[0].length; j++) {

                tmp[index] = grid[i][j];
                index++;
            }
        }
        String gridString = new String(tmp);

        // Parsing for printing empty puzzle with '.'
        HashMap<String, String> values = new HashMap<>();
        index = 0;
        for (String s : squares) {

            values.put(s, gridString.charAt(index) + "");
            index++;
        }

        return values;
    }

    /**
     *  Converts the grid to a HashMap of possible values,
     *  Maps squares to digits
     *  Return false if any illegal characters are detected
     * @param grid
     * @return
     */
    private static HashMap<String, String> GridValues(String grid) {

        char[] empty = { '0', '.' };

        // Checks for legal digits (and blanks)
        ArrayList<Character> chars = new ArrayList<>();
        GridLoop:
        for (int i = 0; i < grid.length(); i++) {

            char g = grid.charAt(i);

            for (int j = 0; j < digits.length; j++) {
                if (digits[j] == g) {

                    chars.add((Character) g);
                    continue GridLoop;
                }
            }

            for (int j = 0; j < empty.length; j++) {

                if (empty[j] == g) {

                    chars.add((Character) g);
                    continue GridLoop;
                }
            }
        }

        // If we don't have a puzzle with 81 squares, we don't have a real puzzle
        if (chars.size() != 81) {

            return null;
        }

        // Save Character ArrayList into a char[]
        char[] charTemp = new char[81];
        int index = 0;
        for (Character c : chars) {

            charTemp[index] = c;
            index++;
        }

        // Associate sqare values with squares
        HashMap<String, String> out = new HashMap<>();
        index = 0;
        for (String s : squares) {

            out.put(s, charTemp[index] + "");
            index++;
        }

        return out;
    }

    /**
     * Eliminate all values except d from values; propagate.
     * Return values. Return false if illegal.
      * @param values
     * @param s
     * @param d
     * @return
     */
    private static HashMap<String, String> Assign(HashMap<String, String> values, String s, String d) {

        // Separate the values we want to eliminate (seems backwards, I know.)
        String altVals = values.get(s);
        StringBuilder sb = new StringBuilder(altVals);
        if (altVals.contains(d.toString())) {

            sb.deleteCharAt(altVals.indexOf(d));
        }
        else {

            return null;
        }
        altVals = sb.toString();

        // If we get a contradiction, go back and try another starting value
        for (char d2 : altVals.toCharArray()) {

            if (Eliminate(values, s, d2 + "") == null) {

                return null;
            }
        }

        return values;
    }

    /**
     * Eliminates d from values(s); propagate
     * Return values or return false if illegal
     * @param values
     * @param s
     * @param d
     * @return
     */
    private static HashMap<String, String> Eliminate(HashMap<String, String> values, String s, String d) {

        if (!values.get(s).contains(d)) {

            return values;
        }

        values.put(s, values.get(s).replace(d, ""));

        if (values.get(s).length() == 0) {

            return null;
        }
        else if (values.get(s).length() == 1) {

            String d2 = values.get(s);

            for (String s2 : peers.get(s)) {

                if (Eliminate(values, s2, d2) == null) {

                    return null;
                }
            }
        }

        // List of all the places that digit d could have potentially been
        ArrayList<String> dplaces;
        for (ArrayList<String> u : units.get(s)) {

            dplaces = new ArrayList<>();
            for (String s1 : u) {

                // If the square s1 has d as a possibility, add it to the list
                if (values.get(s1).contains(d)) {

                    dplaces.add(s1);
                }
            }

            // If somehow the value can't go anywhere, try again somewhere else
            if (dplaces.size() == 0) {

                return null;
            } else if (dplaces.size() == 1) {

                // If there is exactly one place it can go, try to assign it to that spot and eliminate other values
                if (Assign(values, dplaces.get(0), d) == null) {

                    return null;
                }
            }
        }

        return values;
    }

    /**
     * Recurse through unfilled spots, choosing a possible value for each
     * Continue until the puzzle is solved or determined to be unsolvable
     * @param values
     * @return
     */
    private static HashMap<String, String> Search(HashMap<String, String> values) {

        if (values == null) {

            return null;
        }

        // If we have 81 squares with one digit each, we have a completed puzzle
        int tally = 0;
        for (String s : squares) {

            if (values.get(s).length() == 1) {

                tally++;
            }
        }
        if (tally == 81) { //solution

            return values;
        }

        // Search for the sqare with the least possibilities (but more than one possible digit)
        String sq = "A1";
        int min = 9;
        for (String s : squares) {

            int tmp = values.get(s).length();
            if (tmp <= min && tmp > 1) {

                sq = s;
                min = tmp;
            }
        }

        // Make a copy of all the values and try one of the digits in the least populated square
        ArrayList<HashMap<String, String>> searchList = new ArrayList<HashMap<String, String>>();
        for (char d : values.get(sq).toCharArray()) {

            HashMap<String, String> valCopy = new HashMap<String, String>((HashMap) values.clone());

            searchList.add(Search(Assign(valCopy, sq, d + "")));
            return Some(searchList);
        }

        return Some(searchList);
    }

    /**
     * Look for a non-empty/null hashmap from the arraylist, return it if it exists
     *      (Somehow I still have no idea why this is necessary)
     * @param seq
     * @return
     */
    private static HashMap<String, String> Some(ArrayList<HashMap<String, String>> seq) {

        for (HashMap<String, String> e : seq) {

            if (e != null) {

                return e;
            }
        }

        return null;
    }
}
