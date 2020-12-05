### A Pluto.jl notebook ###
# v0.12.16

using Markdown
using InteractiveUtils

# ╔═╡ d3a7b564-34c1-11eb-10fb-fbb890cd9f91
input_regex = r"^(\d+)-(\d+) (\S): (\S*)$"

# ╔═╡ 2ead2f0c-34c2-11eb-1a01-f1b308e1e838
struct Entry
	lower::Int
	upper::Int
	char::Char
	content::String
end

# ╔═╡ 09d1e81c-34c2-11eb-2475-75db55ef2921
function from_str(s::String)
	regmatch = match(input_regex, s)
	Entry(parse(Int, regmatch[1]), parse(Int, regmatch[2]), regmatch[3][1], regmatch[4])
end

# ╔═╡ 7dc56710-34c2-11eb-186c-2371b9416649
function read_input()
	input = open("input")
	res = readlines(input)
	res = from_str.(res)
	close(input)
	res
end

# ╔═╡ 8584dde8-34c2-11eb-064a-2bf1e41f40cd
input = read_input()

# ╔═╡ 1548fcb0-34c4-11eb-2deb-374b901d5506
function validator(entry::Entry)
	otherchars = "[^$(entry.char)]*"

	match = "$(otherchars)$(entry.char)"
	optmatch = "($(match))?"
	nbopts = entry.upper - entry.lower
	
	res = "^"
	for i in 1:entry.lower
		res = res * match
	end
	for i in 1:nbopts
		res = res * optmatch
	end
	res = res * otherchars
	res = res * "\$"
	Regex(res)
end

# ╔═╡ d3878d1c-34c5-11eb-3077-4942250f796e
test = from_str("3-6 a: abcaaadaaaaaaaaaaa")

# ╔═╡ f98cafa8-34c3-11eb-2fd9-df41f6f0f779
validator(test)

# ╔═╡ 495f74e2-34c5-11eb-2e46-1d9d20e02957
function entryvalid(entry)
	m = match(validator(entry), entry.content)
	!isnothing(m)
end

# ╔═╡ 54933c9c-34c5-11eb-178a-7dcdde4fe4de
test_input = from_str.([
	"1-3 a: abcde"
	"1-3 b: cdefg"
	"2-9 c: ccccccccc"
])

# ╔═╡ 66cfe69c-34c5-11eb-2b68-19cd2308b212
filter(entryvalid, test_input)

# ╔═╡ a541591a-34c5-11eb-3472-e1ce8eab2ebf
length(input)

# ╔═╡ 824c7040-34c5-11eb-0e29-01faeb633dec
length(filter(entryvalid, input))

# ╔═╡ b5aae13a-34c6-11eb-0ff9-c3a12b96967b
function entryvalid_part2(entry)
	firstopt = entry.content[entry.lower] == entry.char
	secondopt = entry.content[entry.upper] == entry.char
	firstopt ⊻ secondopt
end

# ╔═╡ e399cfa2-34c6-11eb-24d0-f9e3cd5fbae7
filter(entryvalid_part2, test_input)

# ╔═╡ ed1772b6-34c6-11eb-0d1d-b766063c02f8
length(filter(entryvalid_part2, input))

# ╔═╡ Cell order:
# ╠═d3a7b564-34c1-11eb-10fb-fbb890cd9f91
# ╠═2ead2f0c-34c2-11eb-1a01-f1b308e1e838
# ╠═09d1e81c-34c2-11eb-2475-75db55ef2921
# ╠═7dc56710-34c2-11eb-186c-2371b9416649
# ╠═8584dde8-34c2-11eb-064a-2bf1e41f40cd
# ╠═1548fcb0-34c4-11eb-2deb-374b901d5506
# ╠═d3878d1c-34c5-11eb-3077-4942250f796e
# ╠═f98cafa8-34c3-11eb-2fd9-df41f6f0f779
# ╠═495f74e2-34c5-11eb-2e46-1d9d20e02957
# ╠═54933c9c-34c5-11eb-178a-7dcdde4fe4de
# ╠═66cfe69c-34c5-11eb-2b68-19cd2308b212
# ╠═a541591a-34c5-11eb-3472-e1ce8eab2ebf
# ╠═824c7040-34c5-11eb-0e29-01faeb633dec
# ╠═b5aae13a-34c6-11eb-0ff9-c3a12b96967b
# ╠═e399cfa2-34c6-11eb-24d0-f9e3cd5fbae7
# ╠═ed1772b6-34c6-11eb-0d1d-b766063c02f8
