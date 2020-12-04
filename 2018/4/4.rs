use std::cmp::{Ord, Ordering, PartialEq, PartialOrd};
use std::error::Error;
use std::usize;

const INPUT: &str = include_str!("input");

#[derive(Debug, Eq)]
struct Instant {
    year: usize,
    month: usize,
    day: usize,
    hour: usize,
    min: usize,
}

impl Ord for Instant {
    fn cmp(&self, other: &Instant) -> Ordering {
        if self.year != other.year {
            self.year.cmp(&other.year)
        } else if self.month != other.month {
            self.month.cmp(&other.month)
        } else if self.day != other.day {
            self.day.cmp(&other.day)
        } else if self.hour != other.hour {
            self.hour.cmp(&other.hour)
        } else {
            self.min.cmp(&other.min)
        }
    }    
}

impl PartialOrd for Instant {
    fn partial_cmp(&self, other: &Instant) -> Option<Ordering> {
        Some(self.cmp(other))
    }
}

impl PartialEq for Instant {
    fn eq(&self, other: &Instant) -> bool {
        self.year == other.year && 
            self.month == other.month && 
            self.day == other.day && 
            self.hour == other.hour && 
            self.min == other.min
    }
}

#[derive(Debug)]
enum EventType {
    Begin(usize),
    Asleep,
    Wakeup,
}

#[derive(Debug)]
struct Event {
    date: Instant,
    etype: EventType,
}

fn parse_event(s:&str) -> Result<Event, Box<dyn Error>> {
    let s_vec = s.chars().collect::<Vec<_>>();
    let year = s_vec[1..5].into_iter().collect::<String>().parse::<usize>()?;
    let month = s_vec[6..8].into_iter().collect::<String>().parse::<usize>()?;
    let day = s_vec[9..11].into_iter().collect::<String>().parse::<usize>()?;
    let hour = s_vec[12..14].into_iter().collect::<String>().parse::<usize>()?;
    let min = s_vec[15..17].into_iter().collect::<String>().parse::<usize>()?;

    let etype = match s_vec[19] {
        'w' => EventType::Wakeup, 
        'f' => EventType::Asleep,
        'G' => EventType::Begin(s_vec[26..30].into_iter().collect::<String>().trim().parse::<usize>()?),
        _ => EventType::Begin("0".parse::<usize> ()?)// TODO watdo?
    };

    Ok(Event { date : Instant {year, month, day, hour, min},
               etype})
}

fn split_shifts(events: Vec<Event>) -> Vec<Vec<Event>> {
    let mut shifts = Vec::new();
    let mut current_shift = 0;

    for e in events {
        match e.etype {
            EventType::Begin(_) => {
                if shifts.len() > 0 {
                    current_shift += 1;
                }
                shifts.push(Vec::new());
                shifts[current_shift].push(e)
            },
            _ => shifts[current_shift].push(e),
        }; 
    };

    shifts
}

fn sleepy_time(shift: &Vec<Event>) -> usize {
    let mut sleepy_time = 0; 

    let mut last_asleep = None;
    for event in shift {
        match event.etype {
            EventType::Asleep => last_asleep = Some(event),
            EventType::Wakeup => {
                let start = last_asleep.unwrap();
                sleepy_time += (event.date.year - start.date.year) * 365 * 24 * 60;
                sleepy_time += (event.date.month - start.date.month) * 30 * 24 * 60; // TODO not correct in general but good enought
                sleepy_time += (event.date.day - start.date.day) * 24 * 60;
                sleepy_time += (event.date.hour - start.date.hour) * 60;
                sleepy_time += event.date.min - start.date.min;
            },
            _ => (),
        }
    }

    sleepy_time
}

fn main() {
    let mut claims = INPUT.lines()
        .map(|l| parse_event(l).unwrap())
        .collect::<Vec<_>>();
    claims.sort_unstable_by(|e1, e2| e1.date.cmp(&e2.date));

    let shifts = split_shifts(claims);
    for shift in &shifts {
        println!("{:?}", (shift, sleepy_time(shift))) 
    }

    println!("---");
    println!("{:?}", {
        let max_shift = shifts.iter().max_by(|s1, s2| (sleepy_time(s1).cmp(&sleepy_time(s2)))).unwrap();
        let s_time = sleepy_time(max_shift);
        (max_shift, s_time)
    })
}
