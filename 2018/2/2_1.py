def letter_counts(line):
    counts = {}
    for value in line:
        counts[value] = counts.setdefault(value, 0) + 1
    return counts


with open('./input') as lines:
    count_2 = 0
    count_3 = 0
    for line in lines:
        counts = letter_counts(line)
        if [v for v in counts.values() if v == 2]:
            count_2 += 1
        if [v for v in counts.values() if v == 3]:
            count_3 += 1

    print(count_2 * count_3)
