use std::collections::HashSet;
use std::fs::File;
use std::io::{BufRead, BufReader, Result};

fn part1() -> Result<i32> {
    let f = File::open("input")?;

    let freq: i32 = BufReader::new(f).lines()
        .map(|l| l.unwrap().parse::<i32>().unwrap())
        .sum();

    println!("part1 {}", freq);

    Ok(freq)
}

fn part2() -> Result<i32> {
    let f = File::open("input")?;

    let lines = BufReader::new(f).lines()
        .map(|l| l.unwrap().parse::<i32>().unwrap());

    let mut deltas = Vec::new();
    for line in lines {
        deltas.push(line);
    } 

    let mut freq = 0;
    let mut seen = HashSet::new();

    for delta in deltas.iter().cycle() {
        freq += delta;

        if seen.contains(&freq) {
            break;
        } else {
            seen.insert(freq);
        }
    }

    println!("part2 {}", freq);

    Ok(freq)
}

fn main() -> () {
    part1().expect("failed part1");
    part2().expect("failed part2");
}
