### A Pluto.jl notebook ###
# v0.12.17

using Markdown
using InteractiveUtils

# ╔═╡ f3455b02-446a-11eb-14f3-878ec4660ecc
function parse_input(input)
	res = split(input, "\n")[1]
	res = parse.(Int, split(res, ","))
	res
end

# ╔═╡ feb2ed42-446a-11eb-1388-158a0d8df96f
function read_input()
	input = open("input")
	res = read(input, String)
	close(input)
	parse_input(res)
end

# ╔═╡ 02db7c0e-446b-11eb-124d-0b7402d89277
input = read_input()

# ╔═╡ 3071a86e-446b-11eb-36e5-fd850afe67e3
test_input = parse_input("0,3,6")

# ╔═╡ 8d9bc2ac-446d-11eb-3d04-9576c457fcdd
function play_to(input, last_turn)
	mem = Dict()
	
	turn = 1
	spoken_num = -1
	
	for v in input
		mem[spoken_num] = turn
		spoken_num = v
		turn += 1
	end
	
	while turn <= last_turn
		prev_turn = get(mem, spoken_num, 0)
		mem[spoken_num] = turn
		if prev_turn == 0
			spoken_num = 0
		else
			spoken_num = turn - prev_turn
		end
		turn += 1
	end
	spoken_num
end

# ╔═╡ 3c2404a6-446b-11eb-313c-99a48592a210
function part1(input)
	play_to(input, 2020)
end

# ╔═╡ b81511fc-446b-11eb-2532-1934c8ab9d3b
part1(test_input)

# ╔═╡ 3729b120-446d-11eb-3320-f5c512d7754f
part1(input)

# ╔═╡ 41d06102-446d-11eb-36f0-79fa21aa94f0
function part2(input)
	play_to(input, 30000000)
end

# ╔═╡ 4a25e9bc-446d-11eb-05b7-b333a9dc00ce
part2(test_input)

# ╔═╡ 5864c0b6-446d-11eb-2024-c957c1bafa64
part2(input)

# ╔═╡ Cell order:
# ╠═f3455b02-446a-11eb-14f3-878ec4660ecc
# ╠═feb2ed42-446a-11eb-1388-158a0d8df96f
# ╠═02db7c0e-446b-11eb-124d-0b7402d89277
# ╠═3071a86e-446b-11eb-36e5-fd850afe67e3
# ╠═8d9bc2ac-446d-11eb-3d04-9576c457fcdd
# ╠═3c2404a6-446b-11eb-313c-99a48592a210
# ╠═b81511fc-446b-11eb-2532-1934c8ab9d3b
# ╠═3729b120-446d-11eb-3320-f5c512d7754f
# ╠═41d06102-446d-11eb-36f0-79fa21aa94f0
# ╠═4a25e9bc-446d-11eb-05b7-b333a9dc00ce
# ╠═5864c0b6-446d-11eb-2024-c957c1bafa64
