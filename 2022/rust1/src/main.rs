use std::arch::asm;
use std::fs;

fn solve_part2(input: &Vec<Vec<usize>>) -> Result<u64, &'static str> {
    let mut mem: Vec<usize> = vec![];
    for entry in input {
        for value in entry {
            mem.push(*value);
        }
        mem.push(0);
    }
    let mut res: u64;
    unsafe {
        asm!(// init

            "mov {topone}, 0",
            "mov {toptwo}, 0",
            "mov {topthr}, 0",
            "mov {count}, 0",

            // new candidate

            "2:",
            "mov {acc}, 0",

            "3:",
            "mov {curr}, [{mem} + {count} * 8]",
            "add {count}, 1",
            "add {acc}, {curr}",
            "cmp {curr}, 0",
            "jne 3b",
            "jmp 40f",

            // check if new top

            "41:",
            "xchg {acc}, {topone}",
            "42:",
            "xchg {acc}, {toptwo} ",
            "43:",
            "xchg {acc}, {topthr} ",
            "jmp 49f",

            "40:",
            "cmp {acc}, {topone}",
            "jg 41b",
            "cmp {acc}, {toptwo}",
            "jg 42b",
            "cmp {acc}, {topthr}",
            "jg 43b",

            "49:",
            "cmp {count}, {size}",
            "jl 2b",

            // finished

            "mov {curr}, {topone}",
            "add {curr}, {toptwo}",
            "add {curr}, {topthr}",

            mem = in(reg) mem.as_ptr(),
            size = in(reg) mem.len(),
            count = out(reg) _,
            acc = out(reg) _,
            curr = out(reg) res,
            topone = out(reg) _,
            toptwo = out(reg) _,
            topthr = out(reg) _,

        )
    }
    Result::Ok(res)
}

fn main() {
    let input = fs::read_to_string("../input1")
        .expect("error reading input")
        .split("\n\n")
        .map(|entry| {
            entry
                .split("\n")
                .filter(|entry| !entry.is_empty())
                .map(|value| value.parse::<usize>().unwrap())
                .collect::<Vec<_>>()
        })
        .collect::<Vec<_>>();

    match solve_part2(&input) {
        Ok(res) => println!("solution for part 2: {}", res),
        Err(e) => println!("error when solving part 2: {}", e),
    }
}
