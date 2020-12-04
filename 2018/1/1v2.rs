use std::collections::HashSet;

const INPUT: &str = include_str!("input");

fn part1() -> isize {
    let freq = INPUT.lines()
        .filter_map(|l| l.parse::<isize>().ok())
        .sum::<isize>();

    freq
}

fn part2() -> isize {
    let lines = INPUT.lines()
        .filter_map(|l| l.parse::<isize>().ok());

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

    freq
} 

fn main() -> () {
    println!("part1 {}", part1());
    println!("part2 {}", part2());
}
