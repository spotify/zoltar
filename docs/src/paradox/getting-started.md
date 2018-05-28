# Getting Started

To start using `Zoltar` you need to add this dependency to your project.

@@dependency[Maven,Gradle,sbt] {
  group="com.spotify"
  artifact="zoltar-api"
  version="$project.version$"
}

@@@ note {.help title="Using Maven?" }

You might want to include as it might help you resolve some dependency conflicts.

Maven
:   @@snip [pom.xml](../../../examples/apollo-service-example/pom.xml) { #bom_example }

@@@


See @ref:[Modules](modules/index.md) for extra integrations.