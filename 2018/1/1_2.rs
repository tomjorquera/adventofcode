use std::collections::HashSet;
use std::fs::File;
use std::io::{BufRead, BufReader, Result};

fn main() -> Result<()> {
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

    print!("{}", freq);

    Ok(())
}
