def commons(lines):
    size = len(lines)

    for i in range(size):
        line_1 = lines[i].strip()
        for j in range(i + 1, size):
            line_2 = lines[j].strip()

            diff = 0
            common = []
            for k in range(len(line_1)):
                if line_1[k] == line_2[k]:
                    common.append(line_1[k])
                else:
                    diff += 1

            if diff == 1:
                return common

    raise Exception('No common found')


with open('./input') as lines:
    print(''.join(commons(list(lines))))
