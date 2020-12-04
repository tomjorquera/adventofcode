use std::fs;
use regex::Regex;

const ENTRY_REGEX: &str =  r"^(\d+)-(\d+) (\S): (\S*)$";

#[derive(Debug)]
struct Entry {
    lower: usize,
    upper: usize,
    chr: char,
    content: String,
}

pub trait Entryable {
    fn from_str(s: &str) -> Self;
    fn is_valid_part1(&self) -> bool;
    fn is_valid_part2(&self) -> bool;
}

impl Entryable for Entry {
    fn from_str(s: &str) -> Self {
        let re = Regex::new(ENTRY_REGEX).unwrap();
        let caps = re.captures(s).unwrap();

        Entry {
            lower: caps.get(1).unwrap().as_str().parse::<usize>().unwrap(),
            upper: caps.get(2).unwrap().as_str().parse::<usize>().unwrap(),
            chr: caps.get(3).unwrap().as_str().chars().nth(0).unwrap(),
            content: String::from(caps.get(4).unwrap().as_str()),
        }
    }

    fn is_valid_part1(&self) -> bool {
        let mut count = 0;
        for c in self.content.chars() {
            if c == self.chr {
                count += 1;
            }
        }
        count >= self.lower && count <= self.upper
    }

    fn is_valid_part2(&self) -> bool {
        let firstpos = self.content.chars().nth(self.lower - 1).map_or(false, |x| x == self.chr);
        let secondpos = self.content.chars().nth(self.upper - 1).map_or(false, |x| x == self.chr);
        firstpos ^ secondpos
    }
}

fn solve_part1(input:&Vec<Entry>) -> Result<usize, &'static str> {
    Ok(input.into_iter().filter(|x| x.is_valid_part1()).collect::<Vec<_>>().len())
}

fn solve_part2(input:&Vec<Entry>) -> Result<usize, &'static str> {
    Ok(input.into_iter().filter(|x| x.is_valid_part2()).collect::<Vec<_>>().len())
}

fn main() {
    let input = fs::read_to_string("../input")
        .expect("error reading input")
        .split("\n")
        .filter(|entry| *entry != "")
        .map(|entry| Entry::from_str(entry))
        .collect::<Vec<_>>();

    match solve_part1(&input) {
        Ok(res) => println!("solution for part1: {}", res),
        Err(e) => println!("error when solving part1: {}", e)
    }

    match solve_part2(&input) {
        Ok(res) => println!("solution for part2: {}", res),
        Err(e) => println!("error when solving part2: {}", e)
    }
}
