### A Pluto.jl notebook ###
# v0.12.17

using Markdown
using InteractiveUtils

# ╔═╡ f6fe0358-4450-11eb-243f-87744b39f778
begin
	using Pkg;Pkg.add("Match")
	using Match
end

# ╔═╡ 582a70f8-4451-11eb-236e-1d505289c4ec
begin
	abstract type Instruction end
	struct MASK <: Instruction
		mask::String
	end
	struct MEM <: Instruction
		addr::Int
		val::Int
	end
end

# ╔═╡ 1f807d74-4451-11eb-36d2-cb341954bde4
re_mask = r"^mask = ([1|0|X]+)$"

# ╔═╡ 32ff53de-4451-11eb-24ac-2f012007a429
re_mem = r"^mem\[(\d+)\] = (\d+)$"

# ╔═╡ 38cda86c-4452-11eb-20d3-e320fa79c57c
function decode(s)
	mask_match = match(re_mask, s)
	if !isnothing(mask_match)
		return MASK(mask_match[1])
	end
	
	mem_match = match(re_mem, s)
	if !isnothing(mem_match)
		return MEM(parse(Int, mem_match[1]), parse(Int, mem_match[2]))
	end
end

# ╔═╡ 25057e3c-4453-11eb-33bf-898736ec8d99
function parse_input(input)::Array{Instruction}
	res = split(input, "\n")
	res =  filter(y -> y != "", res)
	res = map(decode, res)
	res
end

# ╔═╡ 2c057e76-4453-11eb-01b6-c737c2da0daa
function read_input()
	input = open("input")
	res = read(input, String)
	close(input)
	parse_input(res)
end

# ╔═╡ 3b65e194-4453-11eb-2433-37af335fd591
test_input = parse_input("""
mask = XXXXXXXXXXXXXXXXXXXXXXXXXXXXX1XXXX0X
mem[8] = 11
mem[7] = 101
mem[8] = 0
""")

# ╔═╡ 42f1f01a-4453-11eb-059c-01fa8c5e4c3d
input = read_input()

# ╔═╡ bdb24948-4459-11eb-26cf-f35e9fda9de0
function highlow_masks(mask)
	(parse.(Int, replace(mask, "X" => "1"); base=2),
	 parse.(Int, replace(mask, "X" => "0"); base=2))
end

# ╔═╡ 4833cd1e-4453-11eb-1fea-0fa08a2e167b
function part1(input)
	memory::Dict{Int, Int} = Dict()
	
	(curr_high_mask, curr_low_mask) = (0, 0)
	
	for instr in input
		if typeof(instr) == MASK
			curr_high_mask, curr_low_mask = highlow_masks(instr.mask)
		else
			memory[instr.addr] = Int((instr.val | curr_low_mask) - (instr.val & unsigned(~curr_high_mask)))
		end
	end
	
	sum(values(memory))
end

# ╔═╡ 9e6fc160-4453-11eb-0074-793d658ecc35
part1(test_input)

# ╔═╡ 8181a78e-4454-11eb-3893-678601529fab
part1(input)

# ╔═╡ 75652452-445a-11eb-24a9-0115591180db
function all_values(mask)
	if length(mask) == 0
		return [""]
	end
	
	curr_char = mask[1]
	poss = if curr_char == 'X'
               ["0", "1"]
		   else
               [string(curr_char)]
		   end
	
	res = []
	for s in all_values(mask[2:end])
		for c in poss
			push!(res, string(c, s))
		end
	end
	res
end

# ╔═╡ 2f617baa-445e-11eb-2999-1b19f533393e
function quantic_or(x::Char, y::Char)
	@match (x, y) begin
		'X', _ => 'X' 
		_, 'X' => 'X'
		'1', _ => '1'
		_, '1' => '1'
		_, _ => '0'
	end
end

# ╔═╡ 4a8ab62e-4460-11eb-3234-c3d5965b3af8
function quantic_or(x::String, y::String)
	string(map(v -> quantic_or(v[1], v[2]),
			   zip(collect(x), collect(y)))...)
end

# ╔═╡ d460d832-445f-11eb-1259-756f4d38eb06
function to_binary(x::Int; pad = 36)
	string(reverse(digits(x; base=2, pad=pad))...)
end

# ╔═╡ fd5d64fc-4460-11eb-0ac2-919e94d42fa3
function from_binary(x::String)
	parse(Int, x; base=2)
end

# ╔═╡ e37a69d0-445b-11eb-0507-3dbf6f63dd60
function part2(input)
	memory::Dict{Int, Int} = Dict()
	mask = input[1].mask
	for instr in input
		if typeof(instr) == MASK
			mask = instr.mask
		else
			masked_addr = quantic_or(to_binary(instr.addr), mask)
			for addr in from_binary.(all_values(masked_addr))
				memory[addr] = instr.val
			end
		end
	end
	sum(values(memory))
end

# ╔═╡ bbaca546-445c-11eb-357e-7522649cf294
test_input2 = parse_input("""
mask = 000000000000000000000000000000X1001X
mem[42] = 100
mask = 00000000000000000000000000000000X0XX
mem[26] = 1""")

# ╔═╡ 31337c3c-445c-11eb-0339-fd07cdb36dbe
part2(test_input2)

# ╔═╡ e3aa81bc-4461-11eb-265d-e5169ba7ecbe
part2(input)

# ╔═╡ Cell order:
# ╠═f6fe0358-4450-11eb-243f-87744b39f778
# ╠═582a70f8-4451-11eb-236e-1d505289c4ec
# ╠═1f807d74-4451-11eb-36d2-cb341954bde4
# ╠═32ff53de-4451-11eb-24ac-2f012007a429
# ╠═38cda86c-4452-11eb-20d3-e320fa79c57c
# ╠═25057e3c-4453-11eb-33bf-898736ec8d99
# ╠═2c057e76-4453-11eb-01b6-c737c2da0daa
# ╠═3b65e194-4453-11eb-2433-37af335fd591
# ╠═42f1f01a-4453-11eb-059c-01fa8c5e4c3d
# ╠═bdb24948-4459-11eb-26cf-f35e9fda9de0
# ╠═4833cd1e-4453-11eb-1fea-0fa08a2e167b
# ╠═9e6fc160-4453-11eb-0074-793d658ecc35
# ╠═8181a78e-4454-11eb-3893-678601529fab
# ╠═75652452-445a-11eb-24a9-0115591180db
# ╠═2f617baa-445e-11eb-2999-1b19f533393e
# ╠═4a8ab62e-4460-11eb-3234-c3d5965b3af8
# ╠═d460d832-445f-11eb-1259-756f4d38eb06
# ╠═fd5d64fc-4460-11eb-0ac2-919e94d42fa3
# ╠═e37a69d0-445b-11eb-0507-3dbf6f63dd60
# ╠═bbaca546-445c-11eb-357e-7522649cf294
# ╠═31337c3c-445c-11eb-0339-fd07cdb36dbe
# ╠═e3aa81bc-4461-11eb-265d-e5169ba7ecbe
