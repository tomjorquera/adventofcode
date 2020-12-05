### A Pluto.jl notebook ###
# v0.12.16

using Markdown
using InteractiveUtils

# ╔═╡ 01262a22-34c0-11eb-0691-fba9468bb320
function read_input()
	input = open("input")
	res = readlines(input)
	close(input)
	res
end

# ╔═╡ 99f5af3a-34bf-11eb-3ef4-9dccd607bf5c
input = parse.(Int, read_input())

# ╔═╡ 6102d664-34c0-11eb-0b06-0b61f534dad7
function solve(input, target=2020)
	for i in input
		for j in input
			if i + j == 2020
				return i * j
			end
		end
	end
end

# ╔═╡ 961eda02-34c0-11eb-11f9-e9419c6850ef
solve(input)

# ╔═╡ a31aab32-34c0-11eb-36ab-9531b2bd628b
function solve_part2(input, target=2020)
	for (i, vi) in enumerate(input)
		for (j, vj) in enumerate(input)
			for (k, vk) in enumerate(input)			
				if (i ≠ j ≠ k) && (vi + vj + vk == 2020)
					return vi * vj * vk
				end
			end
		end
	end
end

# ╔═╡ b5e43d82-34c0-11eb-1c3e-d54b1978d9db
solve_part2(input)

# ╔═╡ 19a87f7c-34c1-11eb-1404-ef45cfd7e19f
solve_part2([
		1721
		979
		366
		299
		675
		1456
		])

# ╔═╡ Cell order:
# ╠═01262a22-34c0-11eb-0691-fba9468bb320
# ╠═99f5af3a-34bf-11eb-3ef4-9dccd607bf5c
# ╠═6102d664-34c0-11eb-0b06-0b61f534dad7
# ╠═961eda02-34c0-11eb-11f9-e9419c6850ef
# ╠═a31aab32-34c0-11eb-36ab-9531b2bd628b
# ╠═b5e43d82-34c0-11eb-1c3e-d54b1978d9db
# ╠═19a87f7c-34c1-11eb-1404-ef45cfd7e19f
