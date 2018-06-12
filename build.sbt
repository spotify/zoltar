lazy val noPublishSettings = Seq(
  publish := {},
  publishLocal := {},
  publishArtifact := false
)

lazy val root = project
  .in(file("."))
  .settings(noPublishSettings)
  .settings(name := "zoltar")
  .aggregate(docs)

lazy val docs = project
  .in(file("docs"))
  .enablePlugins(ParadoxSitePlugin, ParadoxMaterialThemePlugin, GhpagesPlugin)
  .settings(noPublishSettings)
  .settings(
    name := "docs",
    version := "0.4.0",
    paradoxProperties in Paradox ++= Map(
      "javadoc.com.spotify.zoltar.base_url" -> "http://spotify.github.com/zoltar/apidocs"
    ),
    sourceDirectory in Paradox in paradoxTheme := sourceDirectory.value / "paradox" / "_template",
    ParadoxMaterialThemePlugin.paradoxMaterialThemeSettings(Paradox),
    paradoxMaterialTheme in Paradox ~= {
      _.withColor("white", "indigo")
        .withLogo("images/logo.png")
        .withCopyright("Copyright (C) 2018 Spotify AB")
        .withRepository(uri("https://github.com/spotify/zoltar"))
        .withSocial(uri("https://github.com/spotify"),
                    uri("https://twitter.com/spotifyeng"))
    },
    scmInfo := Some(
      ScmInfo(url("https://github.com/spotify/zoltar"),
              "git@github.com:spotify/zoltar.git")),
    git.remoteRepo := scmInfo.value.get.connection
  )
