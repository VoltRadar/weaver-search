#include <iostream>
#include <fstream>
#include <filesystem>
#include <exception>
#include <unordered_map>
#include <vector>
#include <string.h>
#include <list>
#include <chrono>

using namespace std;

string alphabet = "abcdefghijklmnopqrstuvwxyz";

filesystem::path find_weaver_filepath(string filepath = "weaver_words_4.txt") {
    // Returns the filepath of the words file that you need to read from
    filesystem::path wordsPath = filesystem::current_path();

    string filename = "weaver_words_4.txt";
    
    while (wordsPath.compare(wordsPath.parent_path()) != 0) {
        wordsPath.append(filename);

        if (filesystem::exists(wordsPath)) {
            return wordsPath;
        }
        
        wordsPath = wordsPath.parent_path().parent_path();
    }

    throw runtime_error("File not Found!");
}

vector<string> possible_connected_words(string word) {
    // Returns a vector of all strings differing from the input word by exactly
    // one letter

    vector<string> possible_connected_words;
    
    int reqired_list_length = (alphabet.length() - 1) * word.length();
    possible_connected_words.reserve(reqired_list_length);

    for (int letter_index = 0; letter_index < word.length(); letter_index++) {
        for (char c : alphabet) {

            string new_word = word;
            new_word[letter_index] = c;

            if (word != new_word) {
                possible_connected_words.push_back(new_word);
            }
        
        }
    }

    return possible_connected_words;
}

unordered_map<string, vector<string>> read_weaver_words(filesystem::path filepath) {
    // Returns an a map of words to a vector of other words differing by
    // exactly one letter. These words are read from the filepath input
    
    auto connected_words = unordered_map<string, vector<string>>();
    vector<string> blank_str_vector;
    
    ifstream file(filepath);
    if (file.is_open()) {
        string line;
        while (getline(file, line)) {
            connected_words[line] = blank_str_vector;
        }
    }
    else {
        throw runtime_error("Couldn't open file!");
    }

    for (auto pair : connected_words) {
        vector<string> possible_words = possible_connected_words(pair.first);
        vector<string> valid_connected_words;
        
        for (string word : possible_words) {
            if (connected_words.contains(word)) {
                valid_connected_words.push_back(word);
            }
        }

        connected_words[pair.first] = valid_connected_words;
    }

    return connected_words;
}

void print_vector(vector<string> vec) {
    
    for (int i = 0; i < vec.size(); i++) {
        cout << vec[i];
        
        if (i < vec.size() - 1) {
            cout << ", ";
        }
    }
    cout << endl;
}

class Searcher {
    public:
    unordered_map<string, vector<string>> connected_words; 
    
    Searcher(filesystem::path absoulte_words_filepath) {
        // Create a map of words and other words that differ by exactly one
        // letter
        this->connected_words = read_weaver_words(absoulte_words_filepath);
    }

    Searcher(int word_length) {
        // Assumes the filename of the list of words to find, then search for
        // it in the parent dictionary recurssivly
        string words_filename = "weaver_words_" + to_string(word_length) + ".txt";

        this->connected_words = read_weaver_words(find_weaver_filepath(words_filename));
    }

    unordered_map<string, vector<string>> deskera(string starting_word) {
        // Preform deskera search of the connected words.
        // Finds the shortest list of words between

        cout << "STARTING... ";
        unordered_map<string, vector<string>> optimal_paths;
        
        // Reserve the maxamum space required to store the paths
        // Remove for optimiseation?
        optimal_paths.reserve(this->connected_words.size());

        
        vector<string> starting_path;
        starting_path.push_back(starting_word);

        list<vector<string>> queue;
        queue.push_back(starting_path);

        while (!queue.empty()) {
            // Extend the path at the frount of the queue, adding it to the
            // list of known optimal paths
            vector<string> path_to_extend = queue.front();
            queue.pop_front();

            string end_word_of_path = path_to_extend[path_to_extend.size() - 1];
            optimal_paths[end_word_of_path] = path_to_extend;

            for (string next_word : this->connected_words[end_word_of_path]) {
                if (!optimal_paths.contains(next_word)) {
                    // Deep copy (?) of path to extend. Only add it to queue if
                    // optimal path to next_word from starting word hasn't
                    // already been found

                    vector<string> new_path = path_to_extend;
                    new_path.push_back(next_word);
                    queue.push_back(new_path);
                }
                
            }
        }

        cout << "ENDED!" << endl;
        return optimal_paths;
    }

    vector<string> get_connected_words(string word) {
        return this->connected_words[word];
    }
};

int main() {
    
    Searcher search(4);

    auto start = chrono::steady_clock::now();
    for (int i = 0; i < 100; i++) {
        auto paths = search.deskera("test");
        cout << i << endl;
    }

    cout << "Time taken (ms) : ";
    cout << chrono::duration_cast<chrono::milliseconds>(chrono::steady_clock::now() - start).count();
    cout << endl;
    

    return 0;
}