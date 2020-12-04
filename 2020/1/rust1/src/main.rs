use std::fs;

fn solve_part1(input:&Vec<usize>) -> Result<usize, &'static str> {
    for i in input {
        for j in input {
            if i + j == 2020 {
                return Ok(i * j)
            }
        }
    }
    Err("not found")
}

fn solve_part2(input:&Vec<usize>) -> Result<usize, &'static str> {
    for (i, vi) in input.iter().enumerate() {
        for (j, vj) in input.iter().enumerate() {
            for (k, vk) in input.iter().enumerate() {
                if (i != j) && (j != k) && (k != i) && vi + vj + vk == 2020 {
                    return Ok(vi * vj * vk)
                }
            }
        }
    }
    Err("not found")
}

fn main() {
    let input = fs::read_to_string("../input")
        .expect("error reading input")
        .split("\n")
        .filter(|entry| *entry != "")
        .map(|entry| entry.parse::<usize>().unwrap())
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
