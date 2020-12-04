### A Pluto.jl notebook ###
# v0.12.15

using Markdown
using InteractiveUtils

# ╔═╡ 8dd247dc-3619-11eb-3607-595316d00cb7
begin
	using Pkg;Pkg.add("Match")
	using Match
end

# ╔═╡ 3f1c6b12-3618-11eb-2323-37157ce346a4
function parse_input(input)
	res = split(input, "\n\n")
	res = map(x -> replace(x, "\n" => " "), res)
	res = map(x -> split(x, " "), res)
	res = map(x -> filter(y -> y != "", x), res)
	res = map(x -> Dict([(split(i, ":")[1], split(i, ":")[2]) for i in x ]), res)
	res
end

# ╔═╡ 32b2a6c6-3615-11eb-16f7-95b5bad7f18f
function read_input()
	input = open("input")
	res = read(input, String)
	close(input)
	parse_input(res)
end

# ╔═╡ 4aa9bcd0-3615-11eb-06ee-f10aa2dc0071
input = read_input()

# ╔═╡ 5933c8b6-3617-11eb-1390-c56d8ca6107c
required_fields = [
	"byr",
	"iyr",
	"eyr",
	"hgt",
	"hcl",
	"ecl",
	"pid",
]

# ╔═╡ 8dbaf99c-3617-11eb-06e6-27177ce1ff88
function contains_required_field(entry)
	reduce(&, map(x -> haskey(entry, x), required_fields); init=true)
end

# ╔═╡ b4420e20-3617-11eb-1556-1d6046d44993
contains_required_field(input[2])

# ╔═╡ ec044b0e-3617-11eb-27be-035930e203fd
test_input = """
ecl:gry pid:860033327 eyr:2020 hcl:#fffffd
byr:1937 iyr:2017 cid:147 hgt:183cm

iyr:2013 ecl:amb cid:350 eyr:2023 pid:028048884
hcl:#cfa07d byr:1929

hcl:#ae17e1 iyr:2013
eyr:2024
ecl:brn pid:760753108 byr:1931
hgt:179cm

hcl:#cfa07d eyr:2025 pid:166559648
iyr:2011 ecl:brn hgt:59in
"""

# ╔═╡ 587fefb6-3618-11eb-390e-893f6ec0e3ea
sum(contains_required_field.(parse_input(test_input)))

# ╔═╡ 6cf5e888-3618-11eb-025d-8de7f29b75d4
sum(contains_required_field.(input))

# ╔═╡ 4ba2acec-3619-11eb-24e2-7969f8e17eba
function field_is_valid(field, value)
	function valid_byr(value)
		val_regex = r"^\d\d\d\d$"
		regmatch = match(val_regex, value)
		if isnothing(regmatch)
			return false
		end

		parsed_value = parse(Int, value)
		1920 <= parsed_value <= 2002
	end
	
	function valid_iyr(value)
		val_regex = r"^\d\d\d\d$"
		regmatch = match(val_regex, value)
		if isnothing(regmatch)
			return false
		end

		parsed_value = parse(Int, value)
		2010 <= parsed_value <= 2020
	end
	
	function valid_eyr(value)
		val_regex = r"^\d\d\d\d$"
		regmatch = match(val_regex, value)
		if isnothing(regmatch)
			return false
		end

		parsed_value = parse(Int, value)
		2020 <= parsed_value <= 2030
	end
	
	function valid_hgt(value)
		val_regex = r"^(\d+)(cm|in)$"
		regmatch = match(val_regex, value)
		if isnothing(regmatch)
			return false
		end
		
		height = parse(Int, regmatch[1])
		unit = regmatch[2]
		
		if unit == "cm"
			150 <= height <= 193
		else
			59 <= height <= 76
		end
	end
	
	function valid_hcl(value)
		val_regex = r"^#[\da-f]{6}$"
		regmatch = match(val_regex, value)
		if isnothing(regmatch)
			return false
		end
		true
	end
	
	function valid_ecl(value)
		val_regex = r"^(amb|blu|brn|gry|grn|hzl|oth)$"
		regmatch = match(val_regex, value)
		if isnothing(regmatch)
			return false
		end
		true
	end
	
	function valid_pid(value)
		val_regex = r"^\d{9}$"
		regmatch = match(val_regex, value)
		if isnothing(regmatch)
			return false
		end
		true
	end
	
	@match field begin
		"byr" => valid_byr(value)
		"iyr" => valid_iyr(value)
		"eyr" => valid_eyr(value)
		"hgt" => valid_hgt(value)
		"hcl" => valid_hcl(value)
		"ecl" => valid_ecl(value)
		"pid" => valid_pid(value)
	end
	
end

# ╔═╡ 07a59200-3619-11eb-1ef8-c7c3c1da3f8e
function entry_is_valid(entry)
	reduce(&, map(x -> haskey(entry, x) && field_is_valid(x, entry[x]), required_fields); init=true)
end

# ╔═╡ 07e157f8-361c-11eb-3cb7-dba16f92ea69
test2_ok_input = """
pid:087499704 hgt:74in ecl:grn iyr:2012 eyr:2030 byr:1980
hcl:#623a2f

eyr:2029 ecl:blu cid:129 byr:1989
iyr:2014 pid:896056539 hcl:#a97842 hgt:165cm

hcl:#888785
hgt:164cm byr:2001 iyr:2015 cid:88
pid:545766238 ecl:hzl
eyr:2022

iyr:2010 hgt:158cm hcl:#b6652a ecl:blu byr:1944 eyr:2021 pid:093154719
"""

# ╔═╡ e676546a-361b-11eb-2d90-73caafd0351e
entry_is_valid.(parse_input(test2_ok_input))

# ╔═╡ 14ee39d4-361c-11eb-39e7-6957f021172d
test2_ko_input = """
eyr:1972 cid:100
hcl:#18171d ecl:amb hgt:170 pid:186cm iyr:2018 byr:1926

iyr:2019
hcl:#602927 eyr:1967 hgt:170cm
ecl:grn pid:012533040 byr:1946

hcl:dab227 iyr:2012
ecl:brn hgt:182cm pid:021572410 eyr:2020 byr:1992 cid:277

hgt:59cm ecl:zzz
eyr:2038 hcl:74454a iyr:2023
pid:3556412378 byr:2007
"""

# ╔═╡ 1ebf6a5a-361c-11eb-311e-f98143137eea
entry_is_valid.(parse_input(test2_ko_input))

# ╔═╡ 44483a9a-361c-11eb-3d8f-63a022a81889
sum(entry_is_valid.(input))

# ╔═╡ Cell order:
# ╠═8dd247dc-3619-11eb-3607-595316d00cb7
# ╠═3f1c6b12-3618-11eb-2323-37157ce346a4
# ╠═32b2a6c6-3615-11eb-16f7-95b5bad7f18f
# ╠═4aa9bcd0-3615-11eb-06ee-f10aa2dc0071
# ╠═5933c8b6-3617-11eb-1390-c56d8ca6107c
# ╠═8dbaf99c-3617-11eb-06e6-27177ce1ff88
# ╠═b4420e20-3617-11eb-1556-1d6046d44993
# ╠═ec044b0e-3617-11eb-27be-035930e203fd
# ╠═587fefb6-3618-11eb-390e-893f6ec0e3ea
# ╠═6cf5e888-3618-11eb-025d-8de7f29b75d4
# ╠═4ba2acec-3619-11eb-24e2-7969f8e17eba
# ╠═07a59200-3619-11eb-1ef8-c7c3c1da3f8e
# ╠═07e157f8-361c-11eb-3cb7-dba16f92ea69
# ╠═e676546a-361b-11eb-2d90-73caafd0351e
# ╠═14ee39d4-361c-11eb-39e7-6957f021172d
# ╠═1ebf6a5a-361c-11eb-311e-f98143137eea
# ╠═44483a9a-361c-11eb-3d8f-63a022a81889
