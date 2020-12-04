from itertools import cycle

with open("./input") as input:
    deltas = cycle([int(l) for l in input])
    seen = set()

    freq = 0
    for delta in deltas:
        freq += delta
        if freq in seen:
            break
        else:
            seen.add(freq)

    print(freq)
