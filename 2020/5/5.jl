### A Pluto.jl notebook ###
# v0.12.16

using Markdown
using InteractiveUtils

# ╔═╡ 3df6ff5a-36e9-11eb-1915-bda4b250e538
function to_entry(s)
	(s[1:7], s[8:10])
end

# ╔═╡ 2356fec6-36e3-11eb-3dc9-430220c711a9
function parse_input(input)
	res = split(input, "\n")
	res = filter(y -> y != "", res)
	res = to_entry.(res)
    res
end

# ╔═╡ 3b925940-36e3-11eb-17bb-45bd12d3161b
function read_input()
	input = open("input")
	res = read(input, String)
	close(input)
	parse_input(res)
end

# ╔═╡ c0c504a8-36ea-11eb-244e-b73bff32a10d
function adjust(bounds, c)
	if c == 'F' || c =='L'
		(bounds[1], (bounds[1] + bounds[2] -1) / 2)
	else
		((bounds[1] + bounds[2] + 1) / 2, bounds[2])
	end

end

# ╔═╡ cae535c0-36ea-11eb-2e88-cf044f91b7cd
function reduce(bounds, chars)
	res = foldl((b, c) -> adjust(b, c), chars; init=bounds)
	
	if res[1] != res[2]
		error("bounds are not equal after reduce")
	else
		res[1]
	end
end

# ╔═╡ ab596b08-36eb-11eb-1537-f70842af1bb0
function to_id(entry)
	x = reduce([0, 127], entry[1])
	y = reduce([0, 7], entry[2])
	return x * 8 + y
end

# ╔═╡ 51332978-36e3-11eb-1ae0-7b8f760dfc25
input = read_input()

# ╔═╡ 0571040a-36e9-11eb-1aaa-5f0f92dce702
test_input = input[1]

# ╔═╡ 99c24236-36eb-11eb-0615-c3e142a08a9d
to_id(to_entry("BFFFBBFRRR"))

# ╔═╡ 06f30980-36ec-11eb-060d-b37975c68d9b
to_id(to_entry("FFFBBBFRRR"))

# ╔═╡ 0a7ccfda-36ec-11eb-1646-d1ea839d2485
to_id(to_entry("BBFFBBFRLL"))

# ╔═╡ 78ceeff2-36ec-11eb-28a1-cd66afc04152
entries = to_id.(input)

# ╔═╡ 122d8594-36ec-11eb-2c97-5368383c899d
max(entries...)

# ╔═╡ e3edf686-36ec-11eb-1636-03c01675ab77
function find_missing(entries)
	min_bound = min(entries...)
	entries_rank = sort(entries .- min_bound .+ 1)
	for (i, e) in enumerate(entries_rank)
		if i != e
			return i + min_bound - 1
			break
		end
	end
	error("no anomalous entry found")
end

# ╔═╡ 22011e3a-36ed-11eb-1fc9-f959845af515
find_missing(entries)

# ╔═╡ Cell order:
# ╠═3df6ff5a-36e9-11eb-1915-bda4b250e538
# ╠═2356fec6-36e3-11eb-3dc9-430220c711a9
# ╠═3b925940-36e3-11eb-17bb-45bd12d3161b
# ╠═c0c504a8-36ea-11eb-244e-b73bff32a10d
# ╠═cae535c0-36ea-11eb-2e88-cf044f91b7cd
# ╠═ab596b08-36eb-11eb-1537-f70842af1bb0
# ╠═51332978-36e3-11eb-1ae0-7b8f760dfc25
# ╠═0571040a-36e9-11eb-1aaa-5f0f92dce702
# ╠═99c24236-36eb-11eb-0615-c3e142a08a9d
# ╠═06f30980-36ec-11eb-060d-b37975c68d9b
# ╠═0a7ccfda-36ec-11eb-1646-d1ea839d2485
# ╠═78ceeff2-36ec-11eb-28a1-cd66afc04152
# ╠═122d8594-36ec-11eb-2c97-5368383c899d
# ╠═e3edf686-36ec-11eb-1636-03c01675ab77
# ╠═22011e3a-36ed-11eb-1fc9-f959845af515
