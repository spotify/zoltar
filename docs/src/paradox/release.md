# Release Instructions

These instructions are based on the [instructions](http://central.sonatype.org/pages/ossrh-guide.html)
for deploying to the Central Repository using [Maven](http://central.sonatype.org/pages/apache-maven.html).

Note: this section only applies for internal Spotify developers.

You will need the following:
- Sign up for a Sonatype account [here](https://issues.sonatype.org/secure/Signup!default.jspa)
- Ask for permissions to push to com.spotify domain like in this [ticket](https://issues.sonatype.org/browse/OSSRH-20689)
- [GPG set up on the machine you're deploying from](http://central.sonatype.org/pages/working-with-pgp-signatures.html)

Once you've got that in place, you should be able to do deployment using the following commands:

```
# setup credentials
export SONATYPE_USERNAME=<your Sonatype username>
export SONATYPE_PASSWORD=<your Sonatype password>

# deploy snapshot version (to test signing)
mvn clean deploy -Prelease --settings settings.xml

# make and deploy a release
mvn release:clean release:prepare release:perform -Prelease --settings settings.xml
```

Then update https://github.com/spotify/zoltar/releases with release notes!
