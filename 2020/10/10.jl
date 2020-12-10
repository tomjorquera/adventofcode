### A Pluto.jl notebook ###
# v0.12.16

using Markdown
using InteractiveUtils

# ╔═╡ 24d2f946-3b0a-11eb-3d48-f30f5f48fdb5
function parse_input(input)
	res = split(input, "\n")
	res =  filter(x -> x != "", res)
	res = map(x -> parse(Int, x), res)
	res
end

# ╔═╡ 301ad94a-3b0a-11eb-2af4-0dc07954be27
function read_input()
	input = open("input")
	res = read(input, String)
	close(input)
	parse_input(res)
end

# ╔═╡ 43a0d99c-3b0a-11eb-3244-ef0acb6a0566
input = read_input()

# ╔═╡ 50692192-3b0b-11eb-2284-895cd610ec08
test_input = parse_input("""
28
33
18
42
31
14
46
20
48
47
24
23
49
45
19
38
39
11
1
32
25
35
8
17
7
9
4
2
34
10
3
""")

# ╔═╡ 5c05a4b0-3b0b-11eb-1a4f-b97aadeb99b6
function part1(input)
	sorted = sort(input)
	prepend!(sorted, 0)
	push!(sorted, sorted[end] + 3)
	
	function account(acc, x)
		if x == 1
			(acc[1] + 1, acc[2])
		elseif x == 3
			(acc[1], acc[2] + 1)
		else
			acc
		end
	end
	
	res = foldl(account, sorted[2:end] - sorted[1:end-1]; init=(0, 0))
	res[1] * res[2]
end

# ╔═╡ 6cf5dd6e-3b0b-11eb-1c9d-3d5f19700ee4
part1(input)

# ╔═╡ 8ecfa346-3b0d-11eb-3c22-bd0750a7134f
function connections(sorted)
	connected = zeros(length(sorted), length(sorted))
	for (i, vi) in enumerate(sorted)
		for (j, vj) in enumerate(sorted)
		 	if 0 < vj - vi <= 3
				connected[i, j] = 1
			end
		end
	end
	connected
end

# ╔═╡ d58c1b52-3b0d-11eb-3a8c-83deceb645d7
function nb_paths_mem(connected, i, j, mem::Array{Union{Missing, Int}, 2})
	if !ismissing(mem[i, j])
		return mem[i, j]
	end
	
	if i == j
		mem[i, j] = 1
		return 1
	end
	
	total = 0
	for k in i+1:i+3
		if k <= size(connected)[2] && connected[i, k] == 1 && k <= j
			total += nb_paths_mem(connected, k, j, mem)
		end
	end
	
	mem[i, j] = total
	total
end

# ╔═╡ 2e973be2-3b0d-11eb-0fe5-0529217d7ff5
function part2(input)
	sorted = sort(input)
	prepend!(sorted, 0)
	push!(sorted, sorted[end] + 3)
	connected = connections(sorted)
	
	l = length(sorted)
	
	mem::Array{Union{Missing, Int}, 2} = fill(missing, (l, l))
	
	nb_paths_mem(connected, 1, l, mem)
end

# ╔═╡ 45016ac6-3b10-11eb-25d0-ab3b55d97afd
part2(test_input)

# ╔═╡ b1eceb46-3b0c-11eb-3fa1-9b463cd50637
part2(input)

# ╔═╡ Cell order:
# ╠═24d2f946-3b0a-11eb-3d48-f30f5f48fdb5
# ╠═301ad94a-3b0a-11eb-2af4-0dc07954be27
# ╠═43a0d99c-3b0a-11eb-3244-ef0acb6a0566
# ╠═50692192-3b0b-11eb-2284-895cd610ec08
# ╠═5c05a4b0-3b0b-11eb-1a4f-b97aadeb99b6
# ╠═6cf5dd6e-3b0b-11eb-1c9d-3d5f19700ee4
# ╠═8ecfa346-3b0d-11eb-3c22-bd0750a7134f
# ╠═d58c1b52-3b0d-11eb-3a8c-83deceb645d7
# ╠═2e973be2-3b0d-11eb-0fe5-0529217d7ff5
# ╠═45016ac6-3b10-11eb-25d0-ab3b55d97afd
# ╠═b1eceb46-3b0c-11eb-3fa1-9b463cd50637
