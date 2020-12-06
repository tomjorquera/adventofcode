### A Pluto.jl notebook ###
# v0.12.16

using Markdown
using InteractiveUtils

# ╔═╡ 5f45d76a-37bd-11eb-1e22-f36b730a55b6
function parse_input(input)
	res = split(input, "\n\n")
	res = filter(y -> y != "", res)
	res = map(x -> filter(y -> y != "", split(x, "\n")), res)
    res
end

# ╔═╡ 72514328-37bd-11eb-1f2d-091fbcf18637
function read_input()
	input = open("input")
	res = read(input, String)
	close(input)
	parse_input(res)
end

# ╔═╡ c8cb512e-37cc-11eb-04bf-25019ca02b8a
function to_set(entry)
	res = Set([])
	for row in entry
		for element in row
			push!(res, element)
		end
	end
	res
end

# ╔═╡ 530e4c6e-37ce-11eb-0ad3-ad9dcfc455d7
function to_set_part2(entry)
	res = nothing
	for row in entry
		row_set = Set([])
		for element in row
			push!(row_set, element)
		end
		if isnothing(res)
			res = row_set
		else
			res = intersect(res, row_set)
		end
	end
	res
end

# ╔═╡ 475f5f58-37cd-11eb-30d9-35be6bd7b3bb
input = read_input()

# ╔═╡ dc5a9f2e-37bd-11eb-327e-eb3efc349535
test_input = parse_input("""
abc

a
b
c

ab
ac

a
a
a
a

b
""")

# ╔═╡ 06e0a374-37cd-11eb-3221-19bca5db833c
sum(length.(to_set.(test_input)))

# ╔═╡ 52f25d48-37cd-11eb-17a3-b53486f522dd
sum(length.(to_set.(input)))

# ╔═╡ 78f2685c-37ce-11eb-1d10-3d70adcc7b67
sum(length.(to_set_part2.(test_input)))

# ╔═╡ d5f13f6c-37ce-11eb-34a0-b966e0cf09ef
sum(length.(to_set_part2.(input)))

# ╔═╡ Cell order:
# ╠═5f45d76a-37bd-11eb-1e22-f36b730a55b6
# ╠═72514328-37bd-11eb-1f2d-091fbcf18637
# ╠═c8cb512e-37cc-11eb-04bf-25019ca02b8a
# ╠═530e4c6e-37ce-11eb-0ad3-ad9dcfc455d7
# ╠═475f5f58-37cd-11eb-30d9-35be6bd7b3bb
# ╠═dc5a9f2e-37bd-11eb-327e-eb3efc349535
# ╠═06e0a374-37cd-11eb-3221-19bca5db833c
# ╠═52f25d48-37cd-11eb-17a3-b53486f522dd
# ╠═78f2685c-37ce-11eb-1d10-3d70adcc7b67
# ╠═d5f13f6c-37ce-11eb-34a0-b966e0cf09ef
