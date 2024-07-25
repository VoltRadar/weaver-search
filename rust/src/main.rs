use std::{
    cmp::Ordering,
    collections::{HashMap, VecDeque},
    env::current_dir,
    fs::{read_dir, File},
    io::{self, BufRead, BufReader, Error, ErrorKind, Result},
    path::PathBuf,
    time::{Duration, Instant},
    vec,
};

use clap::{self, command, Arg, ArgAction, ArgGroup};

const ALPHABET: &str = "abcdefghijklmnopqrstuvwxyz";

fn get_wordlist_filename(words_length: u32) -> String {
    return format!("weaver_words_{words_length}.txt");
}

fn get_wordlist_path(words_length: u32) -> Result<PathBuf> {
    // Search each parent direcotry for the filename expected, then return
    // the path to that file.
    // Returns a string if failed

    let mut path = current_dir()
        .expect("We can read current path")
        .to_path_buf();
    let expected_filename = get_wordlist_filename(words_length);

    loop {
        let entries = Vec::from_iter(read_dir(&path).unwrap());

        for entry_result in entries {
            match entry_result {
                // If we've found the words file in this directory, return the path
                Ok(entry) => {
                    if entry
                        .file_name()
                        .into_string()
                        .expect("OS str to str shouldn't fail")
                        == expected_filename
                    {
                        path.push(expected_filename);
                        return Ok(path);
                    }
                }

                Err(_) => continue,
            }
        }

        match path.parent() {
            Some(new_path) => {
                path = new_path.to_path_buf();
            }
            None => {
                return Err(Error::new(
                    io::ErrorKind::NotFound,
                    format!("Could not find {}", expected_filename),
                ));
            }
        }
    }
}

fn get_words(word_length: u32) -> Result<HashMap<String, Vec<String>>> {
    let path_to_words_file = get_wordlist_path(word_length)?;
    let words_file = File::open(path_to_words_file)?;

    // Map of words and their next valid words.
    let mut linked_words: HashMap<String, Vec<String>> = HashMap::new();

    // Fill the keys of the map from the file
    for line in BufReader::new(words_file).lines() {
        linked_words.insert(
            match line {
                Ok(word) => word.to_lowercase(),
                Err(error) => {
                    panic!(
                        "Could not read word of length {} due to {}",
                        word_length, error
                    )
                }
            },
            Vec::new(),
        );
    }

    // Fill the values of the hashmap with words differing by one character
    let valid_words: Vec<String> = linked_words.keys().map(|x: &String| x.clone()).collect();
    for word in valid_words {
        // Change one character from each word. Add it to the vector if valid word
        for letter_index in 0..word.len() {
            let mut new_word = word.clone();
            for alphabet_index in 0..ALPHABET.len() {
                // Replace the letter in the new word
                new_word.replace_range(
                    letter_index..letter_index + 1,
                    &ALPHABET[alphabet_index..alphabet_index + 1],
                );

                if linked_words.contains_key(&new_word) && !word.contains(&new_word) {
                    linked_words
                        .get_mut(&word)
                        .expect("Word from HashMap")
                        .push(new_word.clone());
                }
            }
        }
    }

    Ok(linked_words)
}

fn full_search<'a>(
    word: &'a String,
    linked_words: &'a HashMap<String, Vec<String>>,
) -> HashMap<&'a String, Vec<&'a String>> {
    // Return a map where each key 'x' corrisponds to the shortest path
    // from 'word' to 'x'
    // Uses Dijkstra's algorithm

    // Shouldn't need to reallocate, but will waste space. This space will be references, so it should be alright
    let mut shortest_paths = HashMap::with_capacity(linked_words.len());

    let mut queue: VecDeque<Vec<&String>> = VecDeque::new();
    let first_path = vec![word];
    queue.push_back(first_path);

    loop {
        let path_to_extend = match queue.pop_front() {
            Some(path) => path,
            None => {
                break;
            } // Finished search
        };

        let last_word = *path_to_extend.last().expect("Never empty");

        // add the shortest path to the map
        if !shortest_paths.contains_key(last_word) {
            shortest_paths.insert(last_word, path_to_extend.clone());

            // Add new paths to the queue
            match linked_words.get(last_word) {
                Some(next_words) => {
                    for next_word in next_words {
                        // Add the new path to the queue only if a path hasn't been found
                        if !shortest_paths.contains_key(next_word) {
                            let mut new_path = path_to_extend.clone();
                            new_path.push(next_word);
                            queue.push_back(new_path)
                        }
                    }
                }
                None => {}
            }
        }
    }

    shortest_paths
}

// Time to beat: 2747812900
fn test_performance(linked_words: HashMap<String, Vec<String>>) {
    let start = Instant::now();
    let test = String::from("test");

    for _ in 0..100 {
        full_search(&test, &linked_words);
    }

    let end = start.elapsed().as_nanos();
    println!("Time(ns) {end}");
}

fn find_longest_paths<'a>(linked_words: &'a HashMap<String, Vec<String>>) -> Vec<Vec<&String>> {
    let mut output = Vec::new();
    let mut longest_path_length = 0;

    let keys = linked_words.keys().clone();
    let keys_number = linked_words.len();

    let mut last_print_time = Instant::now();

    for (index, word) in keys.enumerate() {
        let result = full_search(word, linked_words);
        let paths = result.values();

        for path in paths {
            match longest_path_length.cmp(&path.len()) {
                Ordering::Greater => {}

                Ordering::Equal => {
                    output.push(path.clone());
                }

                Ordering::Less => {
                    output.clear();
                    output = vec![path.clone()];
                    longest_path_length = path.len();
                }
            }
        }

        if Duration::from_secs(1) < last_print_time.elapsed() {
            println!("{}/{}", index + 1, keys_number);
            last_print_time = Instant::now();
        }
    }

    output
}

fn parse_cmd_args() -> Result<()> {
    let matches = command!()
        .about("Solves word ladders (or 'weavers')")
        .arg(
            // Take two words, and find a path between them
            Arg::new("search")
                .short('s')
                .long("search")
                .help("Search for the path between two words")
                .num_args(2)
                .value_names(["word_1", "word_2"]),
        )
        .arg(
            // Run a full search on test 100 times, and print the time
            Arg::new("test")
                .short('t')
                .long("test")
                .help("Test the performance of this program")
                .action(ArgAction::SetTrue),
        )
        .arg(
            // Find longest optimal path
            Arg::new("longest")
                .short('l')
                .long("longest")
                .help("Find the longest optimal path for 'n' letter words")
                .value_name("n"),
        )
        // Group of the 3 arguments to only use one at a time
        .group(
            ArgGroup::new("flags")
                .args(["search", "test", "longest"])
                .required(true),
        )
        .get_matches();

    // Process arguments

    // Run benchmarking funciton
    if *matches.get_one("test").expect("default value") {
        let linked_words = get_words(4).expect("Should contain word list of length 4");
        test_performance(linked_words);
    }
    // Print the longest optimal paths
    else if matches.contains_id("longest") {
        let word_length = matches
            .get_one::<String>("longest")
            .expect("Checked longest");

        match word_length.parse::<u32>() {
            Ok(length) => {
                let linked_words;
                match get_words(length) {
                    Ok(words) => linked_words = words,
                    Err(e) => return Err(e),
                }

                let longest_paths = find_longest_paths(&linked_words);

                match longest_paths.first() {
                    Some(path) => println!("Longest path length: {}", path.len()),
                    None => return Err(io::Error::new(ErrorKind::NotFound, "No paths found!")),
                }

                // Don't print reversed paths
                let mut printed_paths: Vec<(&String, &String)> =
                    Vec::with_capacity(longest_paths.len() / 2);

                for path in longest_paths {
                    let path_ends = (*path.first().unwrap(), *path.last().unwrap());

                    if !printed_paths.contains(&(path_ends.1, path_ends.0)) {
                        printed_paths.push(path_ends);
                        println!("{:?}", path);
                    }
                }
            }
            Err(_) => {
                return Err(io::Error::new(
                    ErrorKind::InvalidInput,
                    format!("Couldn't parse '{}' as intiger", word_length),
                ));
            }
        }
    }
    // Process search argument
    else if matches.contains_id("search") {
        let words: Vec<&String> = matches.get_many("search").expect("checked").collect();

        let first = words[0];
        let last = words[1];

        if first.len() != last.len() {
            return Err(io::Error::new(
                ErrorKind::InvalidInput,
                format!("'{}' and '{}' not the same length", first, last),
            ));
        }

        let linked_words = get_words(first.len() as u32)?;
        let paths = full_search(first, &linked_words);

        match paths.get(last) {
            Some(path) => println!("{:?}", path),
            None => eprint!("No path from {} to {}", first, last),
        }
    } else {
        panic!("clap should have enforces one of the above arguments")
    }

    return Ok(());
}

fn main() -> Result<()> {
    parse_cmd_args()?;

    Ok(())
}
