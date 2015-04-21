# json-comparator

[![Build Status](https://travis-ci.org/agilogy/json-comparator.svg?branch=master)](https://travis-ci.org/agilogy/json-comparator)
[![Coverage Status](https://coveralls.io/repos/agilogy/json-comparator/badge.svg)](https://coveralls.io/r/agilogy/json-comparator)

A simple library to check a json document against an expected json pattern

## Installation

```
resolvers += Resolver.url("Agilogy Scala",url("http://dl.bintray.com/agilogy/scala/"))(Resolver.ivyStylePatterns)

libraryDependencies += "com.agilogy" %% "json-comparator" % "1.0.0-SNAPSHOT"
```

## Usage

```
import com.agilogy.json.JsonComparator._

diff("""{"a":[1,...]}""", """{"a":[1]}""") 
// => Seq()

diff("""{"a":[1,...,2]}""", """{"a":[1,3,4]}""") 
// => Seq(Difference("/a[3]", Some(JsNumber(2)), None))


diff("""{"b": [...,{...},...,{"a":3},...]}""", """{"b": [{"a":3}]}""") 
// => Seq(Difference("/b[1]", Some(Json.obj("a" -> 3)), None))
```

## TO-DO

- Document it
- Make it independent of Play-Json library (using a typeclass)
- Release it
- Solve or document the weakness of ... inside strings in Json

## Copyright

Copyright 2015 Agilogy

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.