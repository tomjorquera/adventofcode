use std::fs;
use std::collections::{HashMap, HashSet};
use regex::Regex;

type Input = Vec<String>;

fn solve_part1(input: &Input) -> Result<usize, String> {
    fn to_entry(s: &str) -> Result<(String, HashSet<String>), String> {
        let re_container: Regex = Regex::new(r"^(.+?) bags contain").unwrap();
        let re_item: Regex = Regex::new(r"(\d+) (.+?) bags?").unwrap();

        match re_container.captures(s) {
            None => Err(String::from(format!("cannot parse {}", s))),
            container => {
                let mut contained = HashSet::new();
                for cap in re_item.captures_iter(s) {
                    contained.insert(String::from(&cap[2]));
                }
                Ok((String::from(&container.unwrap()[1]), contained))
            },
        }
    }

    fn find_recurr(color: &str, c_dict: &HashMap<String, HashSet<String>>) -> HashSet<String> {
        let mut res = HashSet::new();
        if c_dict.contains_key(color) {
            for e in c_dict.get(color).unwrap() {
                res.insert(e.clone());
                for erec in find_recurr(e, c_dict) {
                    res.insert(erec.clone());
                }
            }
        }
        res
    }

    input.iter().fold(Ok(vec!()), |acc, elem| acc.and_then(|mut v| match to_entry(elem) {
        Err(e) => Err(e),
        Ok(content) => {
            v.push(content);
            Ok(v)
        },
    })).and_then(|res| {
        let mut c_dict = HashMap::new();
        for (container, contained) in res {
            for c in contained {
                if !c_dict.contains_key(&c) {
                    c_dict.insert(c.clone(), HashSet::new());
                }
                c_dict.get_mut(&c).unwrap().insert(container.clone());
            }
        }

        Ok(find_recurr(&"shiny gold", &c_dict).len())
    })
}

fn solve_part2(input: &Input) -> Result<usize, String> {
    fn to_entry(s: &str) -> Result<(String, HashSet<(String, usize)>), String> {
        let re_container: Regex = Regex::new(r"^(.+?) bags contain").unwrap();
        let re_item: Regex = Regex::new(r"(\d+) (.+?) bags?").unwrap();

        match re_container.captures(s) {
            None => Err(String::from(format!("cannot parse {}", s))),
            container => {
                let mut contained = HashSet::new();
                for cap in re_item.captures_iter(s) {
                    contained.insert((String::from(&cap[2]), cap[1].parse::<usize>().unwrap()));
                }
                Ok((String::from(&container.unwrap()[1]), contained))
            },
        }
    }

    fn find_recurr(c: &str, c_dict: &HashMap<String, HashSet<(String, usize)>>) -> usize {
        match c_dict.get(c) {
            None => 0,
            Some(set) =>  {
                let mut res = 0;
                for (contained, count) in set {
                   res += count + count * find_recurr(contained, c_dict);
                }
                res
            }
        }
    }

    input.iter().fold(Ok(vec!()), |acc, elem| acc.and_then(|mut v| match to_entry(elem) {
        Err(e) => Err(e),
        Ok(content) => {
            v.push(content);
            Ok(v)
        },
    })).and_then(|res| {
        let mut c_dict = HashMap::new();
        for (container, contained) in res {
            if !c_dict.contains_key(&container) {
                c_dict.insert(container.clone(), HashSet::new());
            }
            for c in contained {
                c_dict.get_mut(&container).unwrap().insert(c.clone());
            }
        }

        Ok(find_recurr(&"shiny gold", &c_dict))
    })
}

fn input(s: &str) -> Input {
    s.split("\n")
     .filter(|s| *s != "")
     .map(|s| String::from(s))
     .collect::<Vec<_>>()
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
