### jackson-json-stat

An attempt to parse the JSON-Stat HTML spec at http://json-stat.org/format/ with JSoup to automatically generate
serialization and deserialization code for JSON files of the format.

Currently it doesn't look good, especially since part of the spec isn't machine readable in the HTML file,
but even more generally:

* the `unit` element's `base`, `type`, `multiplier` and `adjustment` fields are just mentioned in text
* `children` and `parents` don't match: there are elements which claim to be children
  of parents that don't know about them
* the array content data types aren't often specified or even mentioned in the spec
* there are fields where "X does not have a standard meaning nor a standard vocabulary"
* the ID "free word" fields are harmful and pointless, they could just as well be contained
  in a separate standard JSON object, which wouldn't remove the need to create custom serializers
  and deserializers for "free word" parent objects
