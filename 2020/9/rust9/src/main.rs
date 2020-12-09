use std::fs;
use std::collections::{HashSet, VecDeque};
use std::iter::FromIterator;

#[derive(Debug)]
struct State {
    memory: Vec<(usize, HashSet<usize>)>,
    input: VecDeque<usize>,
}

impl State {
    fn new(input: &Input) -> State {
        State {
            memory: vec![],
            input: VecDeque::from_iter(input.iter().map(|v| *v)),
        }
    }


    fn set_preambule(&mut self, n: usize) {
        if n <= 0 {
            return
        }

        let curr_input = self.input.pop_front().expect("input too short for preambule lenght!");
        let current_mem = HashSet::new();

        for i in 0..self.memory.len() {
            let v = self.memory[i].0;
            self.memory[i].1.insert(v + curr_input);
        }
        self.memory.push((curr_input, current_mem));

        self.set_preambule(n - 1);
    }

    fn valid_head(&self) -> bool {
        let head = self.input[0];
        for (_, values) in &self.memory {
            if values.contains(&head) {
                return true;
            }
        }

        false
    }

    fn step(&mut self) {
        let curr_input = self.input.pop_front().expect("input too short for preambule lenght!");
        let current_mem = HashSet::new();

        self.memory.drain(0..1);

        for i in 0..self.memory.len() {
            let v = self.memory[i].0;
            self.memory[i].1.insert(v + curr_input);
        }
        self.memory.push((curr_input, current_mem));
    }
}


type Input = Vec<usize>;

fn solve_part1(input: &Input, n: usize) -> Result<usize, String> {
    let mut state = State::new(input);
    state.set_preambule(n);
    while state.valid_head() {
        state.step()
    }
    Ok(state.input[0])
}

fn solve_part2(input: &Input, n:usize) -> Result<usize, String> {
    let target = solve_part1(input, n).expect("no solution for part1");
    let mut mem = vec![];
    for (i, v) in input.iter().enumerate() {
        mem.push(vec![*v]);
        for j in 0..i {
            let prev_v = mem[j][i - j - 1];
            if prev_v == target && i != j {
                let input_range = &input[j..i];
                let min = input_range.iter().map(|v| *v).fold(usize::MAX, usize::min);
                let max = input_range.iter().map(|v| *v).fold(0, usize::max);
                return Ok(min + max);
            }
            mem[j].push(prev_v + v);
        }
    }

    Err(String::from("not found"))
}

fn input(s: &str) -> Input {
    s.split("\n")
     .filter(|s| *s != "")
     .map(|s| s.parse::<usize>().unwrap()) // NOTE unchecked unwrap here, we will blow up if something goes wrong
     .collect::<Vec<_>>()
}

fn main() {
    let input = input(&fs::read_to_string("../input").expect("error reading input"));
    match solve_part1(&input, 25) {
        Ok(res) => println!("solution for part1: {}", res),
        Err(e) => println!("error when solving part1: {}", e)
    }

    match solve_part2(&input, 25) {
        Ok(res) => println!("solution for part2: {}", res),
        Err(e) => println!("error when solving part2: {}", e)
    }
}
