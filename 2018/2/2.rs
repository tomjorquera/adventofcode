use std::collections::HashMap;

const INPUT: &str = include_str!("input");

fn checksum(id: &str) -> (bool, bool) {
    let mut char_count = HashMap::<char, isize>::new();
    for c in id.chars() {
        *(char_count.entry(c).or_insert(0)) += 1;
    };

    char_count.iter()
        .fold((false, false),
              |(twos, threes), (_, &v)| match v {
                  2 => (true, threes),
                  3 => (twos, true),
                  _ => (twos, threes),
              })
}

fn part1() -> isize {
    let (twos, threes) = INPUT.lines()
        .map(checksum)
        .fold((0, 0),
              |(twos, threes), (has_two, has_three)| (twos + (if has_two {1} else {0}),
                                                      threes + (if has_three {1} else {0})));

    twos * threes
}

fn common(s1: &str, s2: &str) -> String {
    s1.chars()
        .zip(s2.chars())
        .filter_map(|(c1, c2)| if c1 == c2 {Some(c1)} else {None})
        .collect()
}

fn couples<'a, T:Clone>(elements: &'a [T]) -> Vec<(&'a T, &'a T)> {
    if elements.len() < 2  {
        vec![]
    } else {
        let head = &elements[0];
        let tail = &elements[1..];

        let mut res = tail.iter().map(|t| (head, t)).collect::<Vec<_>>();
        res.extend(couples(tail));

        res
    }
}

fn part2() -> Option<String> {
    let lines = INPUT.lines().collect::<Vec<_>>();
    let id_couples = couples(&lines[..]);

    id_couples.iter().find_map(|(l1, l2)|{
        let c = common(l1, l2);
        if c.len() == (l1.len() - 1) {Some(c)} else {None}
    })
}

fn main() -> () {
    println!("part1 {}", part1());
    println!("part2 {}", part2().unwrap())
}
