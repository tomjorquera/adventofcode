use std::fs;
use std::collections::HashMap;

#[derive(Clone, Debug, PartialEq)]
enum Cell {
    FLOOR,
    FREE,
    TAKEN,
}

type Input = Vec<Vec<Cell>>;

#[derive(Copy, Clone, Debug, PartialEq, Eq, Hash)]
enum Direction {
    N,
    S,
    E,
    W,
    NW,
    NE,
    SW,
    SE,
}

impl Direction {
    fn value(&self) -> (isize, isize) {
        match self {
            Direction::N  => ( 0, -1),
            Direction::S  => ( 0,  1),
            Direction::W  => (-1,  0),
            Direction::E  => ( 1,  0),
            Direction::NW => (-1, -1),
            Direction::NE => ( 1, -1),
            Direction::SW => (-1,  1),
            Direction::SE => ( 1,  1),
        }
    }

    fn from(&self, start: &(usize, usize), n: isize) -> (isize, isize) {
        let (x, y) = self.value();
        (start.0 as isize + x * n, start.1 as isize + y * n)
    }
}


trait Grid {
    fn step_into(&self, transfo: fn(grid: &Self, x:usize, y:usize) -> Cell, target: &mut Self);
    fn transform_part1(grid: &Self, x: usize, y: usize) -> Cell;
    fn transform_part2(grid: &Self, x: usize, y: usize) -> Cell;
}

impl Grid for Input {
    fn step_into(&self, transfo: fn(grid: &Self, x:usize, y:usize) -> Cell, target: &mut Self) {
        for x in 0..self.len() {
            for y in 0..self[x].len() {
                target[x][y] = transfo(self, x, y);
            }
        }
    }

    fn transform_part1(grid: &Self, x: usize, y: usize) -> Cell {
        match &grid[x][y] {
            Cell::FLOOR => Cell::FLOOR,
            other => {
                let mut total = 0;
                for i in x as isize - 1 .. x as isize + 2 {
                    for j in y as isize - 1 .. y as isize + 2 {
                        if i >= 0
                            && (i as usize) < grid.len()
                            && j >= 0 && (j as usize) < grid[i as usize].len()
                            && (i as usize != x || j as usize != y){
                                total += match grid[i as usize][j as usize] {
                                    Cell::TAKEN => 1,
                                    _ => 0,
                                }
                            }
                    }
                }

                match other {
                    Cell::FREE => {
                        if total == 0 {
                            Cell::TAKEN
                        } else {
                            Cell::FREE
                        }
                    },
                    Cell::TAKEN => {
                        if total >= 4 {
                            Cell::FREE
                        } else {
                            Cell::TAKEN
                        }
                    },
                    Cell::FLOOR => {
                       unreachable!()
                    }
                }
            }
        }
    }


    fn transform_part2(grid: &Self, x: usize, y: usize) -> Cell {
        match grid[x][y] {
            Cell::FLOOR => Cell::FLOOR,
            _ => {
                let directions = vec![
                    Direction::N,
                    Direction::S,
                    Direction::W,
                    Direction::E,
                    Direction::NW,
                    Direction::NE,
                    Direction::SW,
                    Direction::SE,
                ];

                let mut views: HashMap<Direction, Option<usize>> = HashMap::new();
                for dir in &directions {
                    views.insert(*dir, None);
                }

                let mut dist = 0;
                let mut incomplete = true;

                while incomplete {
                    dist += 1;
                    let mut changed = false;

                    for dir in &directions {
                        if views[&dir].is_none() {
                            let (nx, ny) = dir.from(&(x, y), dist);
                            if nx >= 0
                                && (nx as usize) < grid.len()
                                && ny >= 0
                                && (ny as usize) < grid[nx as usize].len(){
                                    match grid[nx as usize][ny as usize] {
                                        Cell::FLOOR => {},
                                        Cell::FREE => {
                                            views.insert(*dir, Some(0));
                                            changed = true;
                                        },
                                        Cell::TAKEN => {
                                            views.insert(*dir, Some(1));
                                            changed = true;
                                        },
                                    };
                                } else {
                                    views.insert(*dir, Some(0));
                                    changed = true;
                                }
                        }
                    }

                    if changed {
                        incomplete = false;
                        for dir in &directions {
                            if views[&dir].is_none() {
                                incomplete = true;
                                break;
                            }
                        }
                    }
                }

                let total: usize = views.values().map(|v| v.unwrap()).sum();

                match grid[x][y] {
                    Cell::FREE => {
                        if total == 0 {
                            Cell::TAKEN
                        } else {
                            Cell::FREE
                        }

                    },
                    Cell::TAKEN => {
                        if total >= 5 {
                            Cell::FREE
                        } else {
                            Cell::TAKEN
                        }
                    },
                    Cell::FLOOR => {
                        unreachable!();
                    },
                }
            },
        }
    }
}


fn solve_part1(input: &Input) -> Result<usize, String> {
    let mut buffer0 = input.clone();
    let mut buffer1 = input.clone();
    let mut flipflop = true;

    buffer0.step_into(Grid::transform_part1, &mut buffer1);
    while buffer0 != buffer1 {
        flipflop = !flipflop;
        if flipflop {
            buffer1.step_into(Grid::transform_part1, &mut buffer0);
        } else {
            buffer0.step_into(Grid::transform_part1, &mut buffer1);
        }
    }

    Ok(buffer0.iter().map(|row| row.iter().map(|v| match v {
        Cell::TAKEN => 1,
        _ => 0,
    }).sum::<usize>()).sum())
}

fn solve_part2(input: &Input) -> Result<usize, String> {
    let mut buffer0 = input.clone();
    let mut buffer1 = input.clone();
    let mut flipflop = true;

    buffer0.step_into(Grid::transform_part2, &mut buffer1);
    while buffer0 != buffer1 {
        flipflop = !flipflop;
        if flipflop {
            buffer1.step_into(Grid::transform_part2, &mut buffer0);
        } else {
            buffer0.step_into(Grid::transform_part2, &mut buffer1);
        }
    }

    Ok(buffer0.iter().map(|row| row.iter().map(|v| match v {
        Cell::TAKEN => 1,
        _ => 0,
    }).sum::<usize>()).sum())
}

fn input(s: &str) -> Input {
    fn to_cell(s: char) -> Cell {
        match s {
            '#' => Cell::TAKEN,
            'L' => Cell::FREE,
            '.' => Cell::FLOOR,
            _ => panic!("unexpected char {}", s)
        }
    }

    s.split("\n")
     .filter(|s| *s != "")
     .map(|s| s.chars().map(to_cell).collect::<Vec<Cell>>())
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
