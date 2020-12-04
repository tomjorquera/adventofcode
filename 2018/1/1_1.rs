use std::fs::File;
use std::io::{BufRead, BufReader, Result};

fn main() -> Result<()> {
    let f = File::open("input")?;

    let freq: i32 = BufReader::new(f).lines()
        .map(|l| l.unwrap().parse::<i32>().unwrap())
        .sum();

    print!("{}", freq);

    Ok(())
}
