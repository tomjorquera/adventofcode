use std::fs;
use std::collections::HashMap;
use regex::Regex;

type Entry = HashMap<String, String>;
type Input = Vec<Entry>;

fn parse_entry(entry: &Vec<String>) -> Entry {
    let mut res = HashMap::new();
    for e in entry {
        let es = e.split(":").collect::<Vec<&str>>();
        res.insert(String::from(*es.get(0).unwrap()), String::from(*es.get(1).unwrap()));
    }
    res
}

fn input(s: &str) -> Input {
    let input = s
        .split("\n\n")
        .map(|s| s
             .replace("\n", " ")
             .split(" ")
             .filter(|s| *s != "")
             .map(|s| String::from(s))
             .collect::<Vec<_>>())
        .map(|entry| parse_entry(&entry))
        .collect::<Vec<_>>();

    return input;
}

trait IEntry {
    fn contains_required_fields(&self) -> bool;
    fn is_valid(&self) -> bool;
}

impl IEntry for Entry {
    fn contains_required_fields(&self) -> bool {
        let required_fields = vec![
            "byr",
            "iyr",
            "eyr",
            "hgt",
            "hcl",
            "ecl",
            "pid",
        ];
        return required_fields.iter().fold(true, |acc, field| acc && self.contains_key(*field));
    }

    fn is_valid(&self) -> bool {
        self.contains_required_fields() && self.iter().fold(true, |acc, field| acc && valid_field(field))
    }
}

fn valid_field(field: (&String, &String)) -> bool {
    fn valid_byr(value: &str) -> bool {
        if !Regex::new(r"^\d\d\d\d$").unwrap().is_match(value) {
            return false;
        }
        let v = value.parse::<usize>().unwrap();
        1920 <= v && v <= 2002
    }

    fn valid_iyr(value: &str) -> bool {
        if !Regex::new(r"^\d\d\d\d$").unwrap().is_match(value) {
            return false;
        }
        let v = value.parse::<usize>().unwrap();
        2010 <= v && v <= 2020
    }

    fn valid_eyr(value: &str) -> bool {
        if !Regex::new(r"^\d\d\d\d$").unwrap().is_match(value) {
            return false;
        }
        let v = value.parse::<usize>().unwrap();
        2020 <= v && v <= 2030
    }

    fn valid_hgt(value: &str) -> bool {
        match Regex::new(r"^(\d+)(cm|in)$").unwrap().captures(value) {
            None => false,
            Some(cap) =>  {
                let v = cap[1].parse::<usize>().unwrap();
                if cap[2] == *"cm" {
                    return 150 <= v && v <= 193
                } else {
                    return 59 <= v && v <= 76
                }
            },
        }
    }

    fn valid_hcl(value: &str) -> bool {
       Regex::new(r"^#[\da-f]{6}$").unwrap().is_match(value)
    }

    fn valid_ecl(value: &str) -> bool {
       Regex::new(r"^(amb|blu|brn|gry|grn|hzl|oth)$").unwrap().is_match(value)
    }

    fn valid_pid(value: &str) -> bool {
       Regex::new(r"^\d{9}$").unwrap().is_match(value)
    }

    let (key, value) = field;
    match &key[..] {
        "byr" => valid_byr(value),
        "iyr" => valid_iyr(value),
        "eyr" => valid_eyr(value),
        "hgt" => valid_hgt(value),
        "hcl" => valid_hcl(value),
        "ecl" => valid_ecl(value),
        "pid" => valid_pid(value),
        _ => true
    }
}

fn solve_part1(input: &Input) -> Result<usize, &'static str> {
    Ok(input.iter().fold(0, |acc, entry| acc + entry.contains_required_fields() as usize))
}

fn solve_part2(input: &Input) -> Result<usize, &'static str> {
    Ok(input.iter().fold(0, |acc, entry| acc + entry.is_valid() as usize))
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
