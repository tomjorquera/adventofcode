### A Pluto.jl notebook ###
# v0.12.16

using Markdown
using InteractiveUtils

# ╔═╡ c34c06e0-3b95-11eb-0a07-bd5b7aed3e25
function value(x)
	if x == '#'
		1
	elseif x == 'L'
		0
	else
		nothing
	end
end

# ╔═╡ a65a49ae-3b93-11eb-0ed5-0db421211ccd
function parse_input(res)
	res = filter(x -> x != "", res)
	res = collect.(res)
	res = hcat(res...)
	res = value.(res)
	res
end

# ╔═╡ b575878c-3b93-11eb-2188-4b8cdac4231e
function read_input()
	input = open("input")
	res = readlines(input)
	close(input)
	parse_input(res)
end

# ╔═╡ b8e4171a-3b93-11eb-1c5d-f36813eba724
input = read_input()

# ╔═╡ 7d7e891c-3b95-11eb-0baf-0d7e0e17b396
input[1, 1]

# ╔═╡ f76da53c-3b95-11eb-072b-ab6234576cbe
function transform_part1(input, x, y)
	total = 0
	for i in x-1:x+1
		for j in y-1:y+1
			if (i > 0
					&& i <= size(input)[1]
					&& j > 0
					&& j <= size(input)[2]
					&& (i != x || j != y)
					&& !isnothing(input[i, j]))
				total += input[i, j]		
			end
		end
	end
	
	val = input[x, y]
	
	if val == 0
		if total == 0
			1
		else
			0
		end
	elseif val == 1
		if total >= 4
			0
		else
			1
		end
	else
		nothing
	end	
end

# ╔═╡ 2d2bc4aa-3b97-11eb-3a84-273063634d2f
function step(input, transform)
	res::Array{Union{Nothing, Int},2} = zeros(size(input))
	for x in 1:size(input)[1]
		for y in 1:size(input)[2]
			res[x, y] = transform(input, x, y)	
		end
	end
	res
end

# ╔═╡ e4549ad8-3b97-11eb-0d57-f5010b48ff84
function part1(input)
	old = input
	new = step(input, transform_part1)
	
	while old != new
		old = new
		new = step(old, transform_part1)
	end
	sum(map(x -> if isnothing(x) 0 else x end, new))
end

# ╔═╡ 2b5e0b64-3b98-11eb-1c7c-7593223720b9
part1(input)

# ╔═╡ 08a254ec-3b9b-11eb-2c89-4bd4934cfd8d
function transform_part2(input, x, y)
	N  = [+0 -1]
	S  = [+0 +1]
	W  = [-1 +0]
	E  = [+1 +0]
	NW = [-1 -1]
	NE = [+1 -1]
	SW = [-1 +1]
	SE = [+1 +1]
	
	views::Dict{Array{Int64,2}, Union{Missing, Int}} = Dict(
		N  => missing,
		S  => missing,
		W  => missing,
		E  => missing,
		NW => missing,
		NE => missing,
		SW => missing,
		SE => missing,
	)
	
	i = 0
	incomplete = true
	while incomplete
		i += 1
		changed = false
		for (dir, value) in views
			if ismissing(value)
				coord = [x y] + dir * i
				if (coord[1] > 0
					&& coord[1] <= size(input)[1]
					&& coord[2] > 0
					&& coord[2] <= size(input)[2])
					if !isnothing(input[coord...])
						views[dir] = input[coord...]
						changed = true
					end
				else
					views[dir] = 0
					changed = true
				end
			end
		end
		
		if changed
			incomplete = (ismissing(views[N])
							|| ismissing(views[S])
							|| ismissing(views[W])
							|| ismissing(views[E]) 
							|| ismissing(views[NW])
							|| ismissing(views[NE])
							|| ismissing(views[SW])
							|| ismissing(views[SE]))
		end
	end
	
	total = sum(values(views))
	
	val = input[x, y]
	
	if val == 0
		if total == 0
			1
		else
			0
		end
	elseif val == 1
		if total >= 5
			0
		else
			1
		end
	else
		nothing
	end	
end

# ╔═╡ 32d2f8c2-3b9c-11eb-20bc-418b8ec4e069
function part2(input)
	old = input
	new = step(input, transform_part2)
	
	while old != new
		old = new
		new = step(old, transform_part2)
	end
	sum(map(x -> if isnothing(x) 0 else x end, new))
end

# ╔═╡ 9abdd760-3b9c-11eb-1c80-33625de8e37a
part2(input)

# ╔═╡ Cell order:
# ╠═c34c06e0-3b95-11eb-0a07-bd5b7aed3e25
# ╠═a65a49ae-3b93-11eb-0ed5-0db421211ccd
# ╠═b575878c-3b93-11eb-2188-4b8cdac4231e
# ╠═b8e4171a-3b93-11eb-1c5d-f36813eba724
# ╠═7d7e891c-3b95-11eb-0baf-0d7e0e17b396
# ╠═f76da53c-3b95-11eb-072b-ab6234576cbe
# ╠═2d2bc4aa-3b97-11eb-3a84-273063634d2f
# ╠═e4549ad8-3b97-11eb-0d57-f5010b48ff84
# ╠═2b5e0b64-3b98-11eb-1c7c-7593223720b9
# ╠═08a254ec-3b9b-11eb-2c89-4bd4934cfd8d
# ╠═32d2f8c2-3b9c-11eb-20bc-418b8ec4e069
# ╠═9abdd760-3b9c-11eb-1c80-33625de8e37a
