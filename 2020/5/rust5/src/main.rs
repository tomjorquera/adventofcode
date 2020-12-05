use std::fs;

type Entry = (String, String);
type Input = Vec<Entry>;
type Bounds = (usize, usize);

trait Boundary: Sized {
    fn adjust(&self, c: char) -> Result<Self, String>;
    fn reduce(&self, chars: &str) -> Result<usize, String>;
}

impl Boundary for Bounds {
    fn adjust(&self, c: char) -> Result<Self, String>{
        match c {
            'F' | 'L' => {
                Ok((self.0, (self.0 + self.1 - 1) / 2))
            }
            'B' | 'R' => {
                Ok(((self.0 + self.1 + 1) / 2, self.1))
            }
            _ => Err(format!("unknown bound adjustment {}", c))
        }
    }

    fn reduce(&self, chars: &str) -> Result<usize, String> {
        let res = chars.chars().fold(Ok(*self), |bounds, c| bounds.and_then(|acc| acc.adjust(c)));
        match res {
            Err(e) => Err(e),
            Ok((x, y)) if x != y => Err(String::from(format!("final bounds are not equal {}, {}", x, y))),
            Ok((x, _)) => Ok(x)
        }
    }
}

fn to_id(entry: &Entry) -> Result<usize, String> {
    (0, 127).reduce(&entry.0).and_then(|x| (0, 7).reduce(&entry.1).and_then(|y| Ok(x * 8 + y)))
}

fn solve_part1(input: &Input) -> Result<usize, String> {
    let ids = input.iter().map(to_id).collect::<Result<Vec<_>, _>>();
    match ids {
        Err(e) => Err(e),
        Ok(ids) => {
            match ids.iter().max() {
                None => Err(String::from("no maximum found! (empty list?)")),
                Some(v) => Ok(*v)
            }
        }
    }
}

fn solve_part2(input: &Input) -> Result<usize, String> {

    let ids = input.iter().map(to_id).collect::<Result<Vec<_>, _>>();
    match ids {
        Err(e) => Err(e),
        Ok(mut ids) => {
            ids.sort();
            match ids.get(0) {
                None => Err(String::from("empty ids list")),
                Some(min) => {
                    for (i, e) in ids.iter()
                                     .map(|x| x - min)
                                     .enumerate() {
                                         if i != e {
                                             return Ok(i + min)
                                         }
                                     }
                    Err(String::from(""))
                }
            }
        }
    }
}

fn parse_entry(entry: &str) -> Entry {
    (String::from(&entry[..7]), String::from(&entry[7..]))
}

fn input(s: &str) -> Input {
    let input = s
        .split("\n")
        .filter(|s| *s != "")
        .map(|entry| parse_entry(&entry))
        .collect::<Vec<_>>();

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
