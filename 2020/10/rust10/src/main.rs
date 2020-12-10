use std::fs;

type Input = Vec<usize>;

fn solve_part1(input: &Input) -> Result<usize, String> {
    let mut sorted = input.clone();
    sorted.push(0);
    sorted.push(*sorted.iter().max().unwrap() + 3);
    sorted.sort();

    let accs = sorted[..(sorted.len() - 1)]
        .iter()
        .zip(&sorted[1..sorted.len()])
        .map(|(x, y)| y - x)
        .fold((0, 0), |acc, v| match v {
            1 => (acc.0 + 1, acc.1),
            3 => (acc.0, acc.1 + 1),
            _ => acc,
        });

    Ok(accs.0 * accs.1)
}

fn connections(sorted: &Input) -> Vec<Vec<bool>> {
    let mut res = vec![];
    for (i, vi) in sorted.iter().enumerate() {
        res.push(vec![]);
        for vj in sorted {
            let diff = *vj as isize - *vi as isize;
            res[i].push(0 < diff && diff <= 3);
        }
    }
    res
}

fn nb_paths_mem(connected: &Vec<Vec<bool>>,
                i: usize,
                j: usize,
                mem: &mut Vec<Vec<Option<usize>>>) -> usize {

    match mem[i][j] {
        Some(v) => v,
        None => {
            if i == j {
                mem[i][j] = Some(1);
                1
            } else {
                let mut total = 0;
                for k in i+1..i+4 {
                    if k < connected.len() && connected[i][k] && k <= j {
                        total += nb_paths_mem(connected, k, j, mem);
                    }
                }
                mem[i][j] = Some(total);
                total
            }
        }
    }
}

fn solve_part2(input: &Input) -> Result<usize, String> {
    let mut sorted = input.clone();
    sorted.push(0);
    sorted.push(*sorted.iter().max().unwrap() + 3);
    sorted.sort();
    let connected = connections(&sorted);

    let mut mem = vec![];
    for i in 0..connected.len() {
        mem.push(vec![]);
        for _ in 0..connected.len() {
            mem[i].push(None);
        }
    }

    Ok(nb_paths_mem(&connected, 0, sorted.len() - 1, &mut mem))
}

fn input(s: &str) -> Input {
    s.split("\n")
     .filter(|s| *s != "")
     .map(|s| s.parse::<usize>().unwrap()) // NOTE unchecked unwrap here, we will blow up if something goes wrong
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
