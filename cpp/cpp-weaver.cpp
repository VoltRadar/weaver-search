#include <iostream>
#include <fstream>
#include <filesystem>
#include <exception>
#include <fstream>

using namespace std;

filesystem::path findWordleFilepath(string filepath = "weaver_words_4.txt") {
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

void readWeaverWords(filesystem::path filepath) {
    ifstream file(filepath);
    if (file.is_open()) {
        string line;
        while (getline(file, line)) {
            cout << "Line: " << line << endl;
        }
    }
    else {
        throw runtime_error("Couldn't open file!");
    }
}

int main() {
    
    readWeaverWords(findWordleFilepath());
    // unordered_map
    
    return 0;
}