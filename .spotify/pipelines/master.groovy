@Grab(group = 'com.spotify', module = 'pipeline-conventions', version = '1.0.7')
import com.spotify.pipeline.Pipeline
@Grab(group = 'com.spotify', module = 'pipeline-conventions', version = '1.0.7')

import com.spotify.pipeline.Pipeline

new Pipeline(this) {{ build {
  group(name: 'maven-verify') {
    maven.run(goal: 'clean verify checkstyle:checkstyle findbugs:findbugs')
    jenkinsPipeline.inJob {
      logRotator(-1, -1, -1, 5) // keep only last 5 artifacts
      publishers {
        checkstyle('**/checkstyle-result.xml') {
          computeNew true
          thresholds(
                  failedNew: [low: 0, normal: 0, high: 0],
                  unstableTotal: [all: 0],
                  failedTotal: [all: 0]
          )
        }
        findbugs('**/findbugsXml.xml', true) {
          thresholds(
                  failedNew: [low: 20, normal: 10, high: 5],
                  unstableTotal: [all: 25],
                  failedTotal: [all: 50]
          )
        }
      }
    }
  }
}}}
