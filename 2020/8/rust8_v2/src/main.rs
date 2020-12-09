#![feature(box_patterns)]

use std::fs;
use std::collections::HashSet;
use regex::Regex;

#[derive(Clone, Debug, PartialEq)]
enum Operation {
    NOP,
    ACC,
    JMP,
}

#[derive(Clone, Debug, PartialEq)]
struct Instruction {
    op: Operation,
    val: isize,
}

impl Instruction {
    fn from_str(s: &str) -> Result<Self, String> {
        let re = Regex::new(r"^(nop|acc|jmp) ([\+|-]\d+)$").unwrap();
        match re.captures(s) {
            None => Err(String::from(format!("unknown instruction {}", s))),
            Some(cap) => {
                let val = cap[2].parse::<isize>().unwrap(); // can unwrap safely as matched to a numeral in regex
                match &cap[1] {
                    "nop" => Ok(Instruction{
                        op: Operation::NOP,
                        val,
                    }),
                    "acc" =>Ok(Instruction{
                        op: Operation::ACC,
                        val,
                    }),
                    "jmp" =>Ok(Instruction{
                        op: Operation::JMP,
                        val,
                    }),
                    _ => unreachable!(), // unreachable as we check all patterns matched by regex
                }
            },
        }
    }

    fn flip(&self) -> Self {
        match self.op {
            Operation::NOP => Instruction {
                op: Operation::JMP,
                val: self.val,
            },
            Operation::ACC => self.clone(),
            Operation::JMP => Instruction {
                op: Operation::NOP,
                val: self.val,
            },
        }
    }

    fn is_flippable(&self) -> bool {
        self.flip() != *self
    }
}


type Program = Vec<Instruction>;

#[derive(Debug)]
struct FlippableInstructionSet<'a> {
    source: &'a Program,
    flipped: Option<usize>,
}

impl<'a> FlippableInstructionSet<'a> {
    fn from_program(prog: &'a Program) -> Self {
        FlippableInstructionSet {
            source: prog,
            flipped: None,
        }
    }
}

trait InstructionSet<'a>: Sized {
    fn at(&self, i: usize) -> Option<Instruction>;
    fn flip(&'a self, i: usize) -> FlippableInstructionSet<'a>;
}

impl<'a> InstructionSet<'a> for Program {
    fn at(&self, i: usize) -> Option<Instruction> {
        self.get(i).map(|e| e.clone())
    }

    fn flip(&'a self, i: usize) -> FlippableInstructionSet<'a> {
        FlippableInstructionSet {
            source: self,
            flipped: Some(i),
        }

    }
}

impl<'a> InstructionSet<'a> for FlippableInstructionSet<'a> {
    fn at(&self, i: usize) -> Option<Instruction> {
        match (self.source.at(i), self.flipped) {
            (None, _) => None,
            (Some(instr), Some(j)) if j == i => Some(instr.flip()),
            (Some(instr), _) => Some(instr),
        }
    }

    fn flip(&'a self, i: usize) -> FlippableInstructionSet<'a> {
        FlippableInstructionSet {
            source: self.source,
            flipped: Some(i),
        }

    }
}

#[derive(Clone, Debug)]
struct Machine {
    acc: isize,
    pointer: isize,
    history: HashSet<isize>,
    checkpoint: Option<Box<Machine>>,

}

impl Machine {
    fn new() -> Machine {
        Machine {
            acc: 0,
            pointer: 0,
            history: HashSet::new(),
            checkpoint: None,
        }
    }

    fn execute(&mut self, instr: &Instruction) {
        self.history.insert(self.pointer);
        match instr.op {
            Operation::NOP => {
                self.pointer += 1;
            },
            Operation::ACC => {
                self.pointer += 1;
                self.acc += instr.val;
            },
            Operation::JMP => {
                self.pointer += instr.val;
            }
        }
    }

    fn run_until_end_or_loop<'b>(&mut self, program: &FlippableInstructionSet<'b>) {
        loop {
            if self.pointer < 0 {
                panic!("segfault")
            }
            match program.at(self.pointer as usize) {
                None => {
                    return;
                },
                Some(instr) => {
                    self.execute(&instr);
                    if self.history.contains(&self.pointer) {
                        return;
                    }
                }
            }
        }
    }

    fn is_terminated<'b>(&self, program: &FlippableInstructionSet<'b>) -> bool {
        program.at(self.pointer as usize).is_none()
    }

    fn unwind<'b>(&mut self, program: FlippableInstructionSet<'b>) -> isize {
        self.run_until_end_or_loop(&program);
        match (self.is_terminated(&program), &self.checkpoint) {
            (true, _) => {
                self.acc
            },
            (false, None) => panic!("no solution found"),
            (false, Some(_)) => {
                self.restore();
                self.unwind(program.flip(self.pointer as usize))
            }
        }
    }

    fn run_self_healing<'b>(&mut self, program: FlippableInstructionSet<'b>) -> isize {
        loop {
            if self.pointer < 0 {
                panic!("segfault")
            }

            if self.history.contains(&self.pointer) {
                return self.unwind(program);
            }

            match program.at(self.pointer as usize) {
                None => {
                    println!("final prog flipped {:?}", program.flipped);
                    return self.acc;
                },
                Some(instr) => {
                    if instr.is_flippable() {
                        self.check();
                    }
                    self.execute(&instr);
                }
            }
        }
    }

    fn check(&mut self) {
        self.checkpoint = Some(Box::new(self.clone()));
    }

    fn restore(&mut self) {
        match &self.checkpoint {
            None => panic!("trying to restore without checkpoint"),
            Some(box Machine {
                acc,
                pointer,
                history,
                checkpoint
            }) => {
                self.acc = *acc;
                self.pointer = *pointer;
                self.history = history.clone();
                self.checkpoint = checkpoint.clone();
            }
        }

    }


}
fn solve_part1(input: &Program) -> Result<isize, String> {
    let mut machine = Machine::new();
    machine.run_until_end_or_loop(&FlippableInstructionSet::from_program(input));
    Ok(machine.acc)
}

fn solve_part2(input: &Program) -> Result<isize, String> {
    Ok(Machine::new().run_self_healing(FlippableInstructionSet::from_program(input)))
}

fn input(s: &str) -> Program {
    s.split("\n")
     .filter(|s| *s != "")
     .map(|s| Instruction::from_str(s).unwrap()) // NOTE unchecked unwrap here, we will blow up if something goes wrong
     .collect::<Vec<_>>()
}

fn main() {
   let input = input(&fs::read_to_string("../input").expect("error reading input"));
//     let input = input("
// nop +999
// nop +1
// jmp -2
// jmp -3
// acc +999
// ");

    match solve_part1(&input) {
        Ok(res) => println!("solution for part1: {}", res),
        Err(e) => println!("error when solving part1: {}", e)
    }

    match solve_part2(&input) {
        Ok(res) => println!("solution for part2: {}", res),
        Err(e) => println!("error when solving part2: {}", e)
    }
}
