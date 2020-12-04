import sys

freqs = {}

freq = 0
with open("./input") as input:
    input = list(input)
    while True:
        for l in input:
            freq += int(l)

            if freq in freqs:
                print(freq)
                sys.exit(0)
            else:
                freqs[freq] = 1
