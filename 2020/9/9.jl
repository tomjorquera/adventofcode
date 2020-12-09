### A Pluto.jl notebook ###
# v0.12.16

using Markdown
using InteractiveUtils

# ╔═╡ f69c48d8-3a15-11eb-292b-c95ac830e469
function parse_input(input)
	res = split(input, "\n")
	res =  filter(x -> x != "", res)
	res = map(x -> parse(Int, x), res)
	res
end

# ╔═╡ 08f05aa6-3a16-11eb-0caa-97ff3161065e
function read_input()
	input = open("input")
	res = read(input, String)
	close(input)
	parse_input(res)
end

# ╔═╡ 2c6389fe-3a16-11eb-3cca-f91251a370d3
mutable struct State
	memory::Array{Pair{Int, Set{Int}}}
	input
end

# ╔═╡ 7c7ef6b2-3a16-11eb-3c0c-2d2db8ebfe5f
function set_preambule(state, n)
	if n <= 0
		return state;
	end
	
	curr_input = popfirst!(state.input)
	curr_mem = Set()
	
	for (v, sums) in state.memory
		push!(sums, v + curr_input)
	end
	
	push!(state.memory, curr_input => curr_mem)
	
	set_preambule(state, n - 1)
end

# ╔═╡ 19b6bc66-3a18-11eb-0f6e-cb30825c8ae5
function head_is_valid(state)
	head = state.input[1]
	
	for (_, values) in state.memory
		if in(head, values)
			return true;
		end
	end
	
	return false;
end

# ╔═╡ 1b6fa04e-3a19-11eb-2454-7b06689aee50
function step(state)
	curr_input = popfirst!(state.input)
	curr_mem = Set()
	
	popfirst!(state.memory)
	
	for (v, sums) in state.memory
		push!(sums, v + curr_input)
	end
	
	push!(state.memory, curr_input => curr_mem)
end

# ╔═╡ 7216391c-3a19-11eb-03de-d71141cc8343
function part1(input, preambule_size)
	state = set_preambule(State([], copy(input)), preambule_size)
	while head_is_valid(state)
		step(state);
	end
	state.input[1]
end

# ╔═╡ c982a134-3a43-11eb-3ce6-5ba68437c91f
test_input = parse_input("""
35
20
15
25
47
40
62
55
65
95
102
117
150
182
127
219
299
277
309
576""")

# ╔═╡ a561d958-3a45-11eb-1a1a-fd20db8cc8c0
input = read_input()

# ╔═╡ 66376c6c-3a19-11eb-3840-63d9956f8669
part1(test_input, 5)

# ╔═╡ b5eb8b24-3a19-11eb-161d-89cda6dac468
part1(input, 25)

# ╔═╡ bb8c1ed0-3a42-11eb-03ea-c5d1918bc3bc
function memoize(input)
	res = []
	for (i, v) in enumerate(input)
		push!(res, [v])
		
		for j in 1:(i - 1)
			push!(res[j], res[j][i - j] + v)
		end
	end
	
	return res
end

# ╔═╡ b42c13f0-3a44-11eb-2084-512f5046861b
function part2(input, n)
	target = part1(input, n)
	mem = memoize(input)

	for (i, values) in enumerate(mem)
		for (j, v) in enumerate(values)
			if v == target && i != j
				r = input[i:(i+j-1)]
				return min(r...) + max(r...)
			end
		end
	end
end

# ╔═╡ 24fe54ba-3a45-11eb-2daf-3b315a45918e
part2(test_input, 5)

# ╔═╡ 33876a40-3a46-11eb-1481-edee742d49ec
part2(input, 25)

# ╔═╡ Cell order:
# ╠═f69c48d8-3a15-11eb-292b-c95ac830e469
# ╠═08f05aa6-3a16-11eb-0caa-97ff3161065e
# ╠═2c6389fe-3a16-11eb-3cca-f91251a370d3
# ╠═7c7ef6b2-3a16-11eb-3c0c-2d2db8ebfe5f
# ╠═19b6bc66-3a18-11eb-0f6e-cb30825c8ae5
# ╠═1b6fa04e-3a19-11eb-2454-7b06689aee50
# ╠═7216391c-3a19-11eb-03de-d71141cc8343
# ╠═c982a134-3a43-11eb-3ce6-5ba68437c91f
# ╠═a561d958-3a45-11eb-1a1a-fd20db8cc8c0
# ╠═66376c6c-3a19-11eb-3840-63d9956f8669
# ╠═b5eb8b24-3a19-11eb-161d-89cda6dac468
# ╠═bb8c1ed0-3a42-11eb-03ea-c5d1918bc3bc
# ╠═b42c13f0-3a44-11eb-2084-512f5046861b
# ╠═24fe54ba-3a45-11eb-2daf-3b315a45918e
# ╠═33876a40-3a46-11eb-1481-edee742d49ec
