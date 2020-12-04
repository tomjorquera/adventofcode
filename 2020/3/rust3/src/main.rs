use std::fs;

type Env = Vec<Vec<char>>;
type Coord = [usize; 2];

fn slide(pos: &mut Coord, slope: &Coord, env: &Env) {
    pos[0] = (pos[0] + slope[1]) % env.len();
    pos[1] = (pos[1] + slope[0]) % (env[pos[0]].len());
}

fn value(pos: &Coord, env: &Env) -> usize {
    match env[pos[0]][pos[1]] {
        '#' => 1,
        _ => 0
    }
}

fn score(env: &Env, slope: &Coord) -> usize {
    let mut pos = [0, 0];
    let mut total = value(&pos, env);

    while pos[0] < env.len() - 1 {
        slide(&mut pos, slope, env);
        total += value(&pos, env);
    }

    total
}

fn input(s: &str) -> Env {
    let input = s
        .split("\n")
        .filter(|entry| *entry != "")
        .map(|s| s.chars())
        .map(|cs| cs.collect())
        .collect::<Vec<_>>();

    return input;
}

fn solve_part1(input: &Env) -> Result<usize, &'static str> {
    Ok(score(&input, &[3, 1]))
}

fn solve_part2(input: &Env) -> Result<usize, &'static str> {
    let candidates = [
        [1, 1],
        [3, 1],
        [5, 1],
        [7, 1],
        [1, 2],
    ];

    Ok(candidates.iter().map(|c| score(input, c)).fold(1, |a, c| a * c))
}

fn main() {

//     let input = input("..##.......
// #...#...#..
// .#....#..#.
// ..#.#...#.#
// .#...##..#.
// ..#.##.....
// .#.#.#....#
// .#........#
// #.##...#...
// #...##....#
// .#..#...#.#");

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
