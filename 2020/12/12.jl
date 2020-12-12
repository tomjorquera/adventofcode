### A Pluto.jl notebook ###
# v0.12.16

using Markdown
using InteractiveUtils

# ╔═╡ 403cca70-3c56-11eb-3d55-af0ff17188a1
begin
	using Pkg;Pkg.add("Match")
	using Match
end

# ╔═╡ 7d9f2472-3c55-11eb-17fc-61a8c79f75ca
function value(s)
	(s[1], parse(Int, s[2:end]))
end

# ╔═╡ 65b7c8c4-3c54-11eb-0b6b-414b007a78a7
function parse_input(res)
	res = split(res, "\n")
	res = filter(x -> x != "", res)
	res = value.(res)
	res
end

# ╔═╡ 713894da-3c54-11eb-13aa-eb2590ec9f70
function read_input()
	input = open("input")
	res = read(input, String)
	close(input)
	parse_input(res)
end

# ╔═╡ 7e78fe34-3c54-11eb-1d16-8f76291501f9
input = read_input()

# ╔═╡ 70d3fdbc-3c55-11eb-159e-8d970c7bf3e6
begin
	struct State
		orientation::Char
		x::Int
		y::Int
	end
	
	State() = State('E', 0, 0)
end

# ╔═╡ a6f465f0-3c56-11eb-0fba-7f852243b4f1
function rotate(orientation, counter, degree)
	directions = ['N', 'E', 'S', 'W']
	
	initial_pos = findfirst(==(orientation), directions)
	rotation = degree / 90
	if counter rotation = rotation * -1 end
	new_pos = trunc(Int, mod1(initial_pos + rotation, length(directions)))
	
	directions[new_pos]
end

# ╔═╡ a239cff0-3c58-11eb-3eb3-ab042d97eac6
function forward(state, n)
	directions = Dict(
		'N'  => [+0 -1],
		'S'  => [+0 +1],
		'W'  => [-1 +0],
		'E'  => [+1 +0],
	)
	
	pos = [state.x state.y]
	pos += directions[state.orientation] * n
	
	State(state.orientation, pos[1], pos[2])
end

# ╔═╡ 083b4246-3c56-11eb-1c10-2bde9d1a8a55
function step_part1(state, action)
	@match action[1] begin
		'N' => State(state.orientation, state.x, state.y - action[2])
		'S' => State(state.orientation, state.x, state.y + action[2])
		'W' => State(state.orientation, state.x - action[2], state.y)
		'E' => State(state.orientation, state.x + action[2], state.y)
		'L' => State(rotate(state.orientation, true, action[2]), state.x, state.y)
		'R' => State(rotate(state.orientation, false, action[2]), state.x, state.y)
		'F' => forward(state, action[2])
	end
end

# ╔═╡ 56f6fc04-3c5b-11eb-0de5-bfecf62ae7e9
function part1(input)
	final_step = foldl(step_part1, input; init=State())
	abs(final_step.x) + abs(final_step.y)
end

# ╔═╡ 76f2d9ce-3c5b-11eb-17da-47e745abadab
test_input = parse_input("""
F10
N3
F7
R90
F11""")

# ╔═╡ 1e8a6b96-3c59-11eb-3d14-0194d22e48c8
part1(test_input)

# ╔═╡ 74c05c12-3c5b-11eb-3d53-099db54e44e2
part1(input)

# ╔═╡ 8b8881ba-3c5e-11eb-1942-374136a2f35c
begin
	struct State2
		x::Int
		y::Int
	end
end

# ╔═╡ 7d7f5a86-3c5d-11eb-18c4-69411840de5a
function rotate_around(way_state, rads, counter)
	if counter rads = -1 * rads end
	x = round(cos(rads) * way_state.x - sin(rads) * way_state.y)
	y = round(sin(rads) * way_state.x + cos(rads) * way_state.y)
	State2(x, y)
end

# ╔═╡ d6c4b03c-3c5d-11eb-0d2b-c306da4e5878
function move_towards(ship_state, way_state, n)
	State2(ship_state.x + way_state.x * n,
		   ship_state.y + way_state.y * n)
end

# ╔═╡ 33702796-3c5c-11eb-1061-b3b1704209e4
function step_part2((ship_state, way_state), action)
	@match action[1] begin
		'N' => (ship_state,
				State2(way_state.x, way_state.y - action[2]))
		'S' => (ship_state,
				State2(way_state.x, way_state.y + action[2]))
		'W' => (ship_state,
				State2(way_state.x - action[2], way_state.y))
		'E' => (ship_state,
				State2(way_state.x + action[2], way_state.y))
		'L' => (ship_state,
				rotate_around(way_state, deg2rad(action[2]), true))
		'R' => (ship_state,
				rotate_around(way_state, deg2rad(action[2]), false))
		'F' => (move_towards(ship_state, way_state, action[2]),
				way_state)
	end
end

# ╔═╡ 2fb0ac88-3c64-11eb-0cfc-2b7addd51a49
function part2(input)
	final_step = foldl(step_part2, input; init=(State2(0, 0),  State2(10, -1)))
	abs(final_step[1].x) + abs(final_step[1].y)
end

# ╔═╡ 43f931e0-3c64-11eb-21e6-8ddc27c4a12a
part2(test_input)

# ╔═╡ d3899a38-3c65-11eb-0a0a-27d24be11e22
part2(input)

# ╔═╡ Cell order:
# ╠═403cca70-3c56-11eb-3d55-af0ff17188a1
# ╠═7d9f2472-3c55-11eb-17fc-61a8c79f75ca
# ╠═65b7c8c4-3c54-11eb-0b6b-414b007a78a7
# ╠═713894da-3c54-11eb-13aa-eb2590ec9f70
# ╠═7e78fe34-3c54-11eb-1d16-8f76291501f9
# ╠═70d3fdbc-3c55-11eb-159e-8d970c7bf3e6
# ╠═a6f465f0-3c56-11eb-0fba-7f852243b4f1
# ╠═a239cff0-3c58-11eb-3eb3-ab042d97eac6
# ╠═083b4246-3c56-11eb-1c10-2bde9d1a8a55
# ╠═56f6fc04-3c5b-11eb-0de5-bfecf62ae7e9
# ╠═76f2d9ce-3c5b-11eb-17da-47e745abadab
# ╠═1e8a6b96-3c59-11eb-3d14-0194d22e48c8
# ╠═74c05c12-3c5b-11eb-3d53-099db54e44e2
# ╠═8b8881ba-3c5e-11eb-1942-374136a2f35c
# ╠═7d7f5a86-3c5d-11eb-18c4-69411840de5a
# ╠═d6c4b03c-3c5d-11eb-0d2b-c306da4e5878
# ╠═33702796-3c5c-11eb-1061-b3b1704209e4
# ╠═2fb0ac88-3c64-11eb-0cfc-2b7addd51a49
# ╠═43f931e0-3c64-11eb-21e6-8ddc27c4a12a
# ╠═d3899a38-3c65-11eb-0a0a-27d24be11e22
