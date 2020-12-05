### A Pluto.jl notebook ###
# v0.12.16

using Markdown
using InteractiveUtils

# ╔═╡ 1be9d076-3593-11eb-3913-c9f11b627bcf
value(x) = x === '#'

# ╔═╡ fa46e7ea-358d-11eb-3d2c-e17a8635ebca
function read_input()
	input = open("input")
	res = readlines(input)
	close(input)
	res = collect.(res)
	res = hcat(res...)
	res = value.(res)
	res
end

# ╔═╡ 2c38ce94-358e-11eb-3e34-6b3dff3aa898
input = read_input()

# ╔═╡ 5a2989c4-358e-11eb-2a0d-fd63fd132d1d
function move(pos, env, slope)
	res = pos + slope
	

	if res[1] > size(env)[1]
		res[1] = res[1] % size(env)[1]
	end
	
	res[2] = res[2] #% size(env)[2]
	res
end 

# ╔═╡ f3fe6ed4-3593-11eb-19c4-7f3a05871cc6
function nb_hits(input, slope)
	pos = [1, 1]
	x, y = pos
	nbt = 0
	while (pos[2] <= size(input)[2])	
		x, y = pos
		nbt += input[x, y]
		pos = move(pos, input, slope)

	end
	nbt
end

# ╔═╡ b105245c-3590-11eb-00f3-f391fa07dc83
function solve_part1(input)
	nb_hits(input, [3, 1])
end

# ╔═╡ 9c486026-3590-11eb-2cab-09de8d101804
test_input = value.(hcat(collect.(split("""
..##.......
#...#...#..
.#....#..#.
..#.#...#.#
.#...##..#.
..#.##.....
.#.#.#....#
.#........#
#.##...#...
#...##....#
.#..#...#.#""", '\n'))...))

# ╔═╡ ca0b5f18-3590-11eb-07ab-e7b74deabea3
solve_part1(test_input)

# ╔═╡ 9f166b1c-3591-11eb-187c-47d19f30989c
solve_part1(input)

# ╔═╡ c24524a0-3593-11eb-20ec-f985919db1c0
function solve_part2(env, candidates)
	score_of(slope) = nb_hits(env, slope)
	prod(score_of.(candidates))
end

# ╔═╡ a4a8ea5c-3594-11eb-3ce2-8f6be8f05328
candidate_slopes = [
	[1, 1],
	[3, 1],
	[5, 1],
	[7, 1],
	[1, 2],
]

# ╔═╡ 6039dcfa-3594-11eb-3a06-9b36f8c2090f
solve_part2(test_input, candidate_slopes)

# ╔═╡ d05c30fa-3594-11eb-18c5-d9ccb4a549bb
solve_part2(input, candidate_slopes)

# ╔═╡ Cell order:
# ╠═1be9d076-3593-11eb-3913-c9f11b627bcf
# ╠═fa46e7ea-358d-11eb-3d2c-e17a8635ebca
# ╠═2c38ce94-358e-11eb-3e34-6b3dff3aa898
# ╠═5a2989c4-358e-11eb-2a0d-fd63fd132d1d
# ╠═f3fe6ed4-3593-11eb-19c4-7f3a05871cc6
# ╠═b105245c-3590-11eb-00f3-f391fa07dc83
# ╠═9c486026-3590-11eb-2cab-09de8d101804
# ╠═ca0b5f18-3590-11eb-07ab-e7b74deabea3
# ╠═9f166b1c-3591-11eb-187c-47d19f30989c
# ╠═c24524a0-3593-11eb-20ec-f985919db1c0
# ╠═a4a8ea5c-3594-11eb-3ce2-8f6be8f05328
# ╠═6039dcfa-3594-11eb-3a06-9b36f8c2090f
# ╠═d05c30fa-3594-11eb-18c5-d9ccb4a549bb
