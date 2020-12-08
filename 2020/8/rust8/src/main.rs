use std::fs;
use std::collections::HashSet;
use regex::Regex;

#[derive(Clone)]
enum Operation {
    NOP,
    ACC,
    JMP,
}

#[derive(Clone)]
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

    fn swap(&self) -> Option<Self> {
        match self.op {
            Operation::NOP => Some(Instruction {
                op: Operation::JMP,
                val: self.val,
            }),
            Operation::ACC => None,
            Operation::JMP => Some(Instruction {
                op: Operation::NOP,
                val: self.val,
            }),
        }
    }
}

struct Machine {
    acc: isize,
    pointer: isize,
}

impl Machine {
    fn execute(mut self, instr: &Instruction) -> Machine {
        match instr.op {
            Operation::NOP => {
                self.pointer += 1;
                self
            },
            Operation::ACC => {
                self.pointer += 1;
                self.acc += instr.val;
                self
            },
            Operation::JMP => {
                self.pointer += instr.val;
                self
            }
        }
    }
}

type Program = Vec<Instruction>;

trait IProgram: Sized {
    fn terminal_state(&self) -> Result<Option<isize>, String>;
    fn swaps(&self) -> Vec<Program>;
}

impl IProgram for &[Instruction] {
    fn terminal_state(&self) -> Result<Option<isize>, String> {
        let mut visited: HashSet<isize> = HashSet::new();
        let mut machine = Machine {
            acc: 0,
            pointer: 0,
        };

        loop {
            if machine.pointer < 0 {
                return Err(format!("segfault {}", machine.pointer));
            }
            visited.insert(machine.pointer);
            match self.get(machine.pointer as usize) {
                None => {
                    return Ok(Some(machine.acc));
                },
                Some(instr) => {
                    machine = machine.execute(instr);
                    if visited.contains(&machine.pointer) {
                        return Ok(None);
                    }
                }
            }
        }
    }

    fn swaps(&self) -> Vec<Program> {
        match self.get(0) {
            None => vec![vec![]],
            Some(instr) => {
                let base: Program = vec![instr.clone()];
                let mut res: Vec<Program> = (&self[1..]).swaps().iter().map(|swap| {
                    let mut base_cpy: Program = base.clone();
                    let mut swap_cpy: Program = swap.to_vec();
                    base_cpy.append(&mut swap_cpy);
                    return base_cpy;
                }).collect();

                match instr.swap() {
                    None => {},
                    Some(swapped_instr) => {
                        let mut swapped_instrs = vec![swapped_instr.clone()];
                        let mut rest = self[1..].to_vec();
                        swapped_instrs.append(&mut rest);
                        res.push(swapped_instrs);
                    }
                };
                res
            },
        }
    }
}

fn solve_part1(input: &Program) -> Result<isize, String> {
    let mut visited: HashSet<isize> = HashSet::new();
    let mut machine = Machine {
        acc: 0,
        pointer: 0,
    };

    loop {
        if machine.pointer < 0 {
            return Err(format!("segfault {}", machine.pointer));
        }
        visited.insert(machine.pointer);
        match input.get(machine.pointer as usize) {
            None => return Err(format!("segfault {}", machine.pointer)),
            Some(instr) => {
                machine = machine.execute(instr);
                if visited.contains(&machine.pointer) {
                    return Ok(machine.acc);
                }
            }
        }
    }
}

fn solve_part2(input: &Program) -> Result<isize, String> {
    for swap in (&input[..]).swaps() {
        match (&swap[..]).terminal_state() {
            Err(e) => {
                return Err(e);
            },
            Ok(res) => match res {
                None => {},
                Some(v) => {
                    return Ok(v);
                }
            }
        }
    }
    Err(String::from("no terminating swap found"))
}

fn input(s: &str) -> Program {
    s.split("\n")
     .filter(|s| *s != "")
     .map(|s| Instruction::from_str(s).unwrap()) // NOTE unchecked unwrap here, we will blow up if something goes wrong
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
