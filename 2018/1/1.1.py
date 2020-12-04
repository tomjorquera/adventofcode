freq = 0

freqs = []

with open("./input") as input:
    for l in input:
        freq += int(l)
        freqs.append(freq)

print(freq)
