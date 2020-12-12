use std::fs;
use std::str::FromStr;

#[derive(Copy, Clone, Debug, PartialEq, Eq, Hash)]
enum Command {
    N,
    S,
    E,
    W,
    L,
    R,
    F,
}

impl FromStr for Command {
    type Err = String;
    fn from_str(input: &str) -> Result<Command, Self::Err> {
        match input {
            "N" => Ok(Command::N),
            "S" => Ok(Command::S),
            "E" => Ok(Command::E),
            "W" => Ok(Command::W),
            "L" => Ok(Command::L),
            "R" => Ok(Command::R),
            "F" => Ok(Command::F),
            _ => Err(String::from(format!("invalid command {}", input)))
        }
    }
}

#[derive(Debug)]
struct Instr {
    command: Command,
    n: isize,
}

#[derive(Debug)]
struct State {
    h: isize,
    v: isize,
}

impl State {
    fn rotate(&mut self, rads: f64, counter: bool) {
        let mut rads = rads;
        if counter {
            rads *= -1.0;
        };

        let h = rads.cos() * self.h as f64 - rads.sin() * self.v as f64;
        let v = rads.sin() * self.h as f64 + rads.cos() * self.v as f64;

        self.h = h.round() as isize;
        self.v = v.round() as isize;
    }

    fn rotate_degs(&mut self, degs: isize, counter: bool) {
        self.rotate((degs as f64).to_radians(), counter);
    }

    fn move_to(&mut self, target: &State, n: isize) {
        self.h += target.h * n;
        self.v += target.v * n;
    }

    fn move_towards(&mut self, direction: Direction, n: isize) {
        self.move_to(&direction.target(), n);
    }
}

#[derive(Copy, Clone, Debug, PartialEq, Eq, Hash)]
enum Direction {
    N,
    S,
    E,
    W,
}

impl Direction {
    fn target(&self) -> State {
        match self {
            Direction::N => State {h:  0, v: -1},
            Direction::S => State {h:  0, v:  1},
            Direction::W => State {h: -1, v:  0},
            Direction::E => State {h:  1, v:  0},
        }
    }
}

type Input = Vec<Instr>;

fn solve_part1(input: &Input) -> Result<isize, String> {
    let mut pos = State {h: 0, v: 0};
    let mut orient = State {h: 1, v: 0};

    for instr in input {
        match instr.command {
            Command::N => pos.move_towards(Direction::N, instr.n),
            Command::S => pos.move_towards(Direction::S, instr.n),
            Command::W => pos.move_towards(Direction::W, instr.n),
            Command::E => pos.move_towards(Direction::E, instr.n),
            Command::L => orient.rotate_degs(instr.n, true),
            Command::R => orient.rotate_degs(instr.n, false),
            Command::F => pos.move_to(&orient, instr.n),
        }
    }

    Ok(pos.h.abs() + pos.v.abs())
}

fn solve_part2(input: &Input) -> Result<isize, String> {
    let mut pos = State {h: 0, v: 0};
    let mut way = State {h: 10, v: -1};

    for instr in input {
        match instr.command {
            Command::N => way.move_towards(Direction::N, instr.n),
            Command::S => way.move_towards(Direction::S, instr.n),
            Command::W => way.move_towards(Direction::W, instr.n),
            Command::E => way.move_towards(Direction::E, instr.n),
            Command::L => way.rotate_degs(instr.n, true),
            Command::R => way.rotate_degs(instr.n, false),
            Command::F => pos.move_to(&way, instr.n),
        }
    }

    Ok(pos.h.abs() + pos.v.abs())
}

fn read_input(s: &str) -> Input {
    s.split("\n")
     .filter(|s| *s != "")
     .map(|s| Instr {
         command: Command::from_str(&s[0..1]).unwrap(),
         n: s[1..].parse::<isize>().unwrap() })
     .collect::<Vec<_>>()
}

fn main() {
    let input = read_input(&fs::read_to_string("../input").expect("error reading input"));
    let test_input = read_input("
F10
N3
F7
R90
F11
");
    match solve_part1(&test_input) {
        Ok(res) => println!("solution for part1 test: {}", res),
        Err(e) => println!("error when solving part1 test: {}", e)
    }

    match solve_part1(&input) {
        Ok(res) => println!("solution for part1: {}", res),
        Err(e) => println!("error when solving part1: {}", e)
    }

    match solve_part2(&test_input) {
        Ok(res) => println!("solution for part2 test: {}", res),
        Err(e) => println!("error when solving part2 test: {}", e)
    }

    match solve_part2(&input) {
        Ok(res) => println!("solution for part2: {}", res),
        Err(e) => println!("error when solving part2: {}", e)
    }
}
