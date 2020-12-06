use std::fs;
use std::collections::HashSet;

type Input = Vec<Entry>;
type Entry = Vec<String>;

fn to_set(entry: &Entry) -> HashSet<char> {
    let mut res = HashSet::new();
    for s in entry {
        for c in s.chars() {
            res.insert(c);
        }
    }
    res
}

fn to_set_part2(entry: &Entry) -> HashSet<char> {
    let mut res: Option<HashSet<char>> = None;
    for s in entry {
        let mut entry_set = HashSet::new();
        for c in s.chars() {
            entry_set.insert(c);
        }
        res = match res {
            None => Some(entry_set),
            Some(s) =>  Some(s.intersection(&entry_set).map(|&c| c).collect()),
        }
    }
    res.unwrap()
}

fn solve_part1(input: &Input) -> Result<usize, &'static str> {
    Ok(input.iter().map(|entry| to_set(entry).len()).sum())

}

fn solve_part2(input: &Input) -> Result<usize, &'static str> {
    Ok(input.iter().map(|entry| to_set_part2(entry).len()).sum())
}

fn input(s: &str) -> Input {
    let input = s
        .split("\n\n")
        .filter(|s| *s != "")
        .map(|entry| entry
             .split('\n')
             .filter(|s| *s != "")
             .map(|s| String::from(s))
             .collect())
        .collect();

    return input;
}

fn main() {
    let input = input(&fs::read_to_string("../input").expect("error reading input"));

    match solve_part1(&input) {
        Ok(res) => println!("solution for part1: {}", res),
        Err(e) => println!("error when solving part1: {}", e)
    }

    match solve_part2(&input) {
        Ok(res) => println!("solution for part2: {}", res),
        Err(e) => println!("error when solving part2: {}", e)
    }
}
