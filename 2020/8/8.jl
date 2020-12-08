### A Pluto.jl notebook ###
# v0.12.16

using Markdown
using InteractiveUtils

# ╔═╡ 6e79b484-3988-11eb-05e0-5fb149596208
begin
	using Pkg;Pkg.add("Match")
	using Match
end

# ╔═╡ c6a4e830-3989-11eb-2fc2-27bef7c4727e
begin
	abstract type Instruction end
	struct NOP <: Instruction
		arg::Int
	end
	struct ACC <: Instruction
		arg::Int
	end
	struct JMP <: Instruction
		arg::Int
	end
end

# ╔═╡ 8993dae4-3989-11eb-3bdd-411660b07bb1
struct Machine
	acc::Int
	ptr::Int
end

# ╔═╡ 50773040-3988-11eb-0572-119a4a22c3ef
begin
	function exec(mach::Machine, instr::NOP)
		Machine(mach.acc, mach.ptr + 1)
	end
	function exec(mach::Machine, instr::ACC)
		Machine(mach.acc + instr.arg, mach.ptr + 1)
	end
	function exec(mach::Machine, instr::JMP)
		Machine(mach.acc, mach.ptr + instr.arg)
	end
end


# ╔═╡ 5c3a4aa0-398a-11eb-0250-99647773f084
function to_instr(s)
	re = r"^(nop|acc|jmp) ([\+|-]\d+)$"
	regmatch = match(re, s)
	if isnothing(regmatch)
			return error("unknown instruction $(s)")
	end
	@match regmatch[1] begin
		"nop" => NOP(parse(Int, regmatch[2]))
		"acc" => ACC(parse(Int, regmatch[2]))
		"jmp" => JMP(parse(Int, regmatch[2]))
	end
end

# ╔═╡ 42bb6f00-398a-11eb-01f3-a956ce7920ec
function parse_input(input)::Array{Instruction}
	res = split(input, "\n")
	res =  filter(y -> y != "", res)
	res = map(to_instr, res)
	res
end

# ╔═╡ 3f4ba5f8-398a-11eb-1d76-317e891e82d3
function read_input()
	input = open("input")
	res = read(input, String)
	close(input)
	parse_input(res)
end

# ╔═╡ a48d1282-398b-11eb-253c-814f5558a8ad
function part1(prog)
	visited = Set()
	machine = Machine(0, 1)
	
	while true
		push!(visited, machine.ptr)
		machine = exec(machine, prog[machine.ptr])
		if machine.ptr in visited
			return machine.acc
		end
	end
end

# ╔═╡ 7bf8a0f2-398b-11eb-1689-893ee14ea8c8
test_input = parse_input("""
nop +0
acc +1
jmp +4
acc +3
jmp -3
acc -99
acc +1
jmp -4
acc +6
""")

# ╔═╡ 75f6d51e-398b-11eb-13c7-dfcee5a17f4f
input = read_input()

# ╔═╡ 37d0285e-398c-11eb-3fef-09042ed9271e
part1(test_input)

# ╔═╡ 6550d3ac-398c-11eb-23a3-0140adae964a
part1(input)

# ╔═╡ 58c67f6c-398d-11eb-0793-f5bb2b41191c
begin
	swap_instr(instr::NOP) = JMP(instr.arg)
	swap_instr(instr::ACC) = nothing
	swap_instr(instr::JMP) = NOP(instr.arg)

	function swaps(prog::Array{Instruction})::Array{Array{Instruction}}
		if length(prog) == 0
			return [[]]
			
		end
		
		unswapped::Array{Instruction} = [prog[1]]
		res::Array{Array{Instruction}} = map(x -> append!(copy(unswapped), x),
											 swaps(prog[2:end]))
		
		if !isnothing(swap_instr(prog[1]))
			swapped::Array{Instruction} = [swap_instr(prog[1])]
			push!(res, append!(swapped, prog[2:end]))
		end
		
		return res
	end
	
end

# ╔═╡ 46634302-3995-11eb-0b68-03b8dca903d1
function finishes(prog)
	visited = Set()
	machine = Machine(0, 1)
	states = []
	
	while true
		if machine.ptr > length(prog)
			return machine.acc
		end
		push!(states, (machine, prog[machine.ptr]))
		last_instr = machine.ptr
		push!(visited, machine.ptr)
		machine = exec(machine, prog[machine.ptr])
		if machine.ptr in visited
			return false
		end
	end
end

# ╔═╡ 90c968ae-3995-11eb-2a53-97d1a6601264
function part2(program)
	for s in swaps(program)
		res = finishes(s)
		if res != false
			return res
		end
	end
	error("could not find a version that finishes")
end

# ╔═╡ b2161570-3995-11eb-2180-21a49987af8a
part2(test_input)

# ╔═╡ bfc7a508-3995-11eb-3756-2303c9cd4692
part2(input)

# ╔═╡ Cell order:
# ╠═6e79b484-3988-11eb-05e0-5fb149596208
# ╠═c6a4e830-3989-11eb-2fc2-27bef7c4727e
# ╠═8993dae4-3989-11eb-3bdd-411660b07bb1
# ╠═50773040-3988-11eb-0572-119a4a22c3ef
# ╠═5c3a4aa0-398a-11eb-0250-99647773f084
# ╠═42bb6f00-398a-11eb-01f3-a956ce7920ec
# ╠═3f4ba5f8-398a-11eb-1d76-317e891e82d3
# ╠═a48d1282-398b-11eb-253c-814f5558a8ad
# ╠═7bf8a0f2-398b-11eb-1689-893ee14ea8c8
# ╠═75f6d51e-398b-11eb-13c7-dfcee5a17f4f
# ╠═37d0285e-398c-11eb-3fef-09042ed9271e
# ╠═6550d3ac-398c-11eb-23a3-0140adae964a
# ╠═58c67f6c-398d-11eb-0793-f5bb2b41191c
# ╠═46634302-3995-11eb-0b68-03b8dca903d1
# ╠═90c968ae-3995-11eb-2a53-97d1a6601264
# ╠═b2161570-3995-11eb-2180-21a49987af8a
# ╠═bfc7a508-3995-11eb-3756-2303c9cd4692
