use std::collections::HashMap;
use std::fmt::Display;
use std::num::ParseIntError;

const INPUT: &str = include_str!("input");

struct Claim {
    id: usize,
    x: usize,
    y: usize,
    w: usize,
    h: usize,
}

impl Display for Claim {
    fn fmt(&self, w: &mut std::fmt::Formatter) -> std::result::Result<(), std::fmt::Error> {
        write!(w, "Claim[id: {}, ({},{}), ({}, {})]", self.id, self.x, self.y, self.h, self.w)
    }
}

fn parse(line:&str) -> Result<Claim, ParseIntError> {
    let split_space = line.split(" ").collect::<Vec<_>>();

    let id = split_space[0].chars().collect::<Vec<_>>()[1..].iter().collect::<String>().parse::<usize>()?;

    let xy = split_space[2].split(',').collect::<Vec<_>>();
    let x = xy[0].chars().collect::<String>().parse::<usize>()?;
    let mut ys = xy[1].chars().collect::<String>();
    ys.pop();
    let y = ys.parse::<usize>()?;

    let hw = split_space[3].split('x').collect::<Vec<_>>();
    let w = hw[0].chars().collect::<String>().parse::<usize>()?;
    let h = hw[1].chars().collect::<String>().parse::<usize>()?;

    Ok(Claim { id: id, x: x, y: y, w: w, h: h})
}

fn part1(fabric: &HashMap<(usize, usize), usize>) -> usize {
    return fabric.iter().filter(|(_, &v)| v > 1).count() 
}

fn max_overlapping_patch(claim: &Claim, fabric: &HashMap<(usize, usize), usize>) -> usize {
    let mut max_overlap = 0;
    for x in claim.x..(claim.x + claim.w) {
        for y in claim.y..(claim.y + claim.h) {
            let overlap = fabric.get(&(x, y));
            if overlap.is_some()  && *overlap.unwrap() > max_overlap  {
                max_overlap = *overlap.unwrap(); 
            }
        }
    }
    max_overlap
}

fn part2<'a>(claims: &'a Vec<Claim>, fabric: &HashMap<(usize, usize), usize>) -> Option<&'a Claim> {
    claims.iter()
        .map(|c| (c, max_overlapping_patch(c, fabric)))
        .filter(|(_, n)| *n == 1) // NOTE: a patch will always overlap at least with itself
        .map(|(c, _)| c)
        .next()
}

fn main() -> () {
    let mut fabric = HashMap::new();
    
    let claims = INPUT.lines().map(parse).map(|c| c.unwrap()).collect::<Vec<_>>();

    for claim in &claims {
        for x in claim.x..(claim.x + claim.w) {
            for y in claim.y..(claim.y + claim.h) {
                *fabric.entry((x, y)).or_insert(0) += 1;
            }
        }
    }

    println!("{}", part1(&fabric));
    println!("{}", part2(&claims, &fabric).unwrap());
}
