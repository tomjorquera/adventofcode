### A Pluto.jl notebook ###
# v0.12.16

using Markdown
using InteractiveUtils

# ╔═╡ b7f0ad84-38b4-11eb-2c94-753253f7c8c3
function parse_input(input)
	res = split(input, "\n")
	res =  filter(y -> y != "", res)
	#res = map(x -> Dict([(split(i, ":")[1], split(i, ":")[2]) for i in x ]), res)
	res
end

# ╔═╡ bb6bffa4-38b4-11eb-2740-2370f9617903
function read_input()
	input = open("input")
	res = read(input, String)
	close(input)
	parse_input(res)
end

# ╔═╡ e80a45c6-38b6-11eb-02ec-513346cda4e6
container_regex = r"^(.+?) bags contain"

# ╔═╡ c0c8c546-38b5-11eb-1fb7-c9b95141516a
item_regex = r"(\d+) (.+?) bags?"

# ╔═╡ 4e8a7250-38b7-11eb-3f9e-550be8bb531c
function to_entry(s)
	container = match(container_regex, s).captures[1]
	contained = [r.captures[2] for r in eachmatch(item_regex, s)]
	(container, contained)
end

# ╔═╡ 9b9cb8c8-38b7-11eb-157e-f16223a9f49a
function part1_dict(entries)
	res = Dict()
	
	for (container, contained) in entries
		for c in contained
			if !haskey(res, c)
				push!(res, c => Set())
			end
			push!(res[c], container)
		end
	end
	res
end

# ╔═╡ 4f6a75ba-38b9-11eb-14db-4d79f0ab825a
function find_recurr_containers(c, c_dict)
	res = Set()
	
	if haskey(c_dict, c)
		for e in c_dict[c]
			push!(res, e)
			for e_r in find_recurr_containers(e, c_dict)
				push!(res, e_r)
			end
		end
	end
	res
end

# ╔═╡ bedb5f66-38b9-11eb-359a-f7126c2ff2bf
function part1(input)
	c_dict = part1_dict(to_entry.(input))
	length(find_recurr_containers("shiny gold", c_dict))
end

# ╔═╡ ee419a9c-38b9-11eb-00af-d78f4bd51c12
test_input = parse_input("""
light red bags contain 1 bright white bag, 2 muted yellow bags.
dark orange bags contain 3 bright white bags, 4 muted yellow bags.
bright white bags contain 1 shiny gold bag.
muted yellow bags contain 2 shiny gold bags, 9 faded blue bags.
shiny gold bags contain 1 dark olive bag, 2 vibrant plum bags.
dark olive bags contain 3 faded blue bags, 4 dotted black bags.
vibrant plum bags contain 5 faded blue bags, 6 dotted black bags.
faded blue bags contain no other bags.
dotted black bags contain no other bags.
""")

# ╔═╡ 00925af6-38ba-11eb-0d08-65caf2fa89ba
input = read_input()

# ╔═╡ cfa13dec-38b9-11eb-21c2-b36e7f93a22c
part1(test_input)

# ╔═╡ daa77f42-38b9-11eb-0276-53250e78d85f
part1(input)

# ╔═╡ 48746128-38ba-11eb-16c8-ed36d5e8f994
function to_entry_part2(s)
	container = match(container_regex, s).captures[1]
	contained = [(r.captures[2], parse(Int, r.captures[1])) for r in eachmatch(item_regex, s)]
	(container, contained)
end

# ╔═╡ 42e6e020-38ba-11eb-2cf4-dfb77a17a45c
function part2_dict(entries_part2)
	res = Dict()
	
	for (container, contained) in entries_part2
		if !haskey(res, container)
			push!(res, container => Set())
		end
		for c in contained
			push!(res[container], c)
		end
	end
	res
end

# ╔═╡ a83b5f8c-38ba-11eb-0be1-b741176b97cf
function find_recurr_value(c, c_dict)
	if !haskey(c_dict, c)
		return 0
	end
	res = 0
	
	for e in c_dict[c]
		res += e[2] + e[2] * find_recurr_value(e[1], c_dict)
	end
	res
end

# ╔═╡ 49ffee5a-38bb-11eb-23a5-af5fea8c1d05
function part2(input)
	c_dict = part2_dict(to_entry_part2.(input))
	find_recurr_value("shiny gold", c_dict)
end

# ╔═╡ 4cc4792c-38ba-11eb-148a-8f0717d82a3c
test_input_part2 = parse_input("""
shiny gold bags contain 2 dark red bags.
dark red bags contain 2 dark orange bags.
dark orange bags contain 2 dark yellow bags.
dark yellow bags contain 2 dark green bags.
dark green bags contain 2 dark blue bags.
dark blue bags contain 2 dark violet bags.
dark violet bags contain no other bags.
""")

# ╔═╡ 157ee712-38bb-11eb-14b2-61707a022003
part2(test_input_part2)

# ╔═╡ 475fd3b8-38bb-11eb-1bc0-01b78556e029
part2(input)

# ╔═╡ Cell order:
# ╠═b7f0ad84-38b4-11eb-2c94-753253f7c8c3
# ╠═bb6bffa4-38b4-11eb-2740-2370f9617903
# ╠═e80a45c6-38b6-11eb-02ec-513346cda4e6
# ╠═c0c8c546-38b5-11eb-1fb7-c9b95141516a
# ╠═4e8a7250-38b7-11eb-3f9e-550be8bb531c
# ╠═9b9cb8c8-38b7-11eb-157e-f16223a9f49a
# ╠═4f6a75ba-38b9-11eb-14db-4d79f0ab825a
# ╠═bedb5f66-38b9-11eb-359a-f7126c2ff2bf
# ╠═ee419a9c-38b9-11eb-00af-d78f4bd51c12
# ╠═00925af6-38ba-11eb-0d08-65caf2fa89ba
# ╠═cfa13dec-38b9-11eb-21c2-b36e7f93a22c
# ╠═daa77f42-38b9-11eb-0276-53250e78d85f
# ╠═48746128-38ba-11eb-16c8-ed36d5e8f994
# ╠═42e6e020-38ba-11eb-2cf4-dfb77a17a45c
# ╠═a83b5f8c-38ba-11eb-0be1-b741176b97cf
# ╠═49ffee5a-38bb-11eb-23a5-af5fea8c1d05
# ╠═4cc4792c-38ba-11eb-148a-8f0717d82a3c
# ╠═157ee712-38bb-11eb-14b2-61707a022003
# ╠═475fd3b8-38bb-11eb-1bc0-01b78556e029
