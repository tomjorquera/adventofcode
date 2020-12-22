### A Pluto.jl notebook ###
# v0.12.17

using Markdown
using InteractiveUtils

# ╔═╡ 70c3f800-3d2b-11eb-0c92-199a2a224aee
function value(s)
	if s == "x"
		missing
	else
		parse(Int, s)
	end
end

# ╔═╡ 596d3612-3d2b-11eb-26ab-2d14ae244ccf
function parse_input(res)
	res = split(res, "\n")
	res = filter(x -> x != "", res)
	(parse(Int, res[1]), value.(split(res[2], ",")))
end

# ╔═╡ 67b50010-3d2b-11eb-2f2e-67c699326c91
function read_input()
	input = open("input")
	res = read(input, String)
	close(input)
	parse_input(res)
end

# ╔═╡ 6c92840e-3d2b-11eb-33e6-7b93c4cc80ae
input = read_input()

# ╔═╡ e957dbf6-3d2b-11eb-17ac-e32c936c46ac
test_input = parse_input("""
939
7,13,x,x,59,x,31,19""")

# ╔═╡ 2da116a6-3d2c-11eb-3fb0-3d399e563953
function missing_to_fill(value, cycle)
	cycle - value % cycle
end

# ╔═╡ 13874dde-3d2d-11eb-2e22-d1a9b0638f63
missing_to_fill.(test_input[1], test_input[2])

# ╔═╡ 5f16ed46-3d2c-11eb-03f9-cb9e46306b64
function part1(input)
	time = map(x -> if ismissing(x) Inf else x end, missing_to_fill.(input[1], input[2]))
	best = argmin(time)
	input[2][best] * time[best]
end

# ╔═╡ 41f99628-3d2c-11eb-11d1-912e1f9b423a
part1(test_input)

# ╔═╡ 4629a1d8-3d2d-11eb-2103-0f39b4d06ebf
part1(input)

# ╔═╡ 767e528c-444b-11eb-0765-7f555bb77292
function inverse(a, b)
	m = b
	x = 0
	y = 1
	if b == 1
		return 0
	end
      
	while (a > 1) 
		q = div(a, b)
		t = b
		b = a % b
		a = t
		t = x
		x = y - q * x
		y = t
	end

	if (y < 0) 
		y += m
	end

	return y
end

# ╔═╡ 75b68912-4443-11eb-1b6f-63d6a40a4c8d
function chinese_remainder_theorem(values)
	product = prod(filter(x -> !ismissing(x), values))
	sum = 0
	for (i, v) in enumerate(values)
		if !ismissing(v)
			pp = div(product, v)
			inv = inverse(pp, v)
			sum += pp * inv * (v - i + 1)
		end
	end
	sum % product
end

# ╔═╡ 92993f02-444d-11eb-3fa0-6bf6059bd2fe
function part2(input)
	chinese_remainder_theorem(input[2])
end

# ╔═╡ f3405f36-4441-11eb-2393-47d3e616d48c
part2(test_input)

# ╔═╡ 1462cb0e-444c-11eb-359d-ebf9e0821b4d
part2(input)

# ╔═╡ Cell order:
# ╠═70c3f800-3d2b-11eb-0c92-199a2a224aee
# ╠═596d3612-3d2b-11eb-26ab-2d14ae244ccf
# ╠═67b50010-3d2b-11eb-2f2e-67c699326c91
# ╠═6c92840e-3d2b-11eb-33e6-7b93c4cc80ae
# ╠═e957dbf6-3d2b-11eb-17ac-e32c936c46ac
# ╠═2da116a6-3d2c-11eb-3fb0-3d399e563953
# ╠═13874dde-3d2d-11eb-2e22-d1a9b0638f63
# ╠═5f16ed46-3d2c-11eb-03f9-cb9e46306b64
# ╠═41f99628-3d2c-11eb-11d1-912e1f9b423a
# ╠═4629a1d8-3d2d-11eb-2103-0f39b4d06ebf
# ╠═767e528c-444b-11eb-0765-7f555bb77292
# ╠═75b68912-4443-11eb-1b6f-63d6a40a4c8d
# ╠═92993f02-444d-11eb-3fa0-6bf6059bd2fe
# ╠═f3405f36-4441-11eb-2393-47d3e616d48c
# ╠═1462cb0e-444c-11eb-359d-ebf9e0821b4d
