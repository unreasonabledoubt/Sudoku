// Sudoku.cpp : Defines the entry point for the console application.
//

#include "stdafx.h"
#include <iostream>
#include <vector>
#include <map>
#include <unordered_set>
using namespace std;

char digits[9] = { '1','2','3','4','5','6','7','8','9' };
char cols[9] = { '1','2','3','4','5','6','7','8','9' };
char rows[9] = { 'A','B','C','D','E','F','G','H','I' };
string colChunks[3] = { "123", "456", "789" };
string rowChunks[3] = { "ABC", "DEF", "GHI" };

vector<string> squares;
vector<vector<string>> unitList;
map<string, vector<vector<string>>> units;
map<string, unordered_set<string>> peers;


int main()
{
	int numPuzzles = 1;

	cout <<  "Enter the number of puzzles you would like and press [ENTER]: ";

	while (!(cin >> numPuzzles)) {
		cin.clear();
		cin.ignore(numeric_limits<streamsize>::max(), '\n');
		cout << "Invalid input.  Try again: ";
	}
	cout << "You enterd: " << numPuzzles << endl;

	for (size_t i = 0; i < numPuzzles; i++)
	{
		Solver::SolveSudoku(i);
	}
}


char** RandomPuzzle() {


}


namespace Solver {

	void SolveSudoku(int puzzleNum) {


	}

	vector<string> Cross(char* c1, char* c2) {


	}

	vector<vector<string>> BuildUnitList() {


	}

	vector<string> Shuffled(vector<string> seq) {


	}

	map<string, string> Assign(map<string, string> values, string s, string d) {


	}

	map<string, string> Eliminate(map<string, string> values, string s, string d) {


	}

	map<string, string> Search(map<string, string> values) {


	}

	map<string, string> Some(vector<map<string, string>> seq) {


	}
}


namespace Parser {

	map<string, string> ParseGrid(char** grid) {


	}

	map<string, string> ParseToPuzzle(char** grid) {


	}

	map<string, string> GridValues(string grid) {


	}
}


namespace Printer {

	void PrintPuzzle(map<string, string> values, const char* fileName) {


	}

	void PrintSolution(map<string, string> values, const char* fileName) {


	}
}