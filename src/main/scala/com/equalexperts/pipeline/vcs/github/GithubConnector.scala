/*
 * Copyright 2017 Equal Experts
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.equalexperts.pipeline.vcs.github

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.equalexperts.pipeline.Logger
import org.joda.time.DateTime
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.WSResponse
import uk.gov.hmrc.releaser.github._
import uk.gov.hmrc.ServiceCredentials

import scala.util.{Success, Try}

object GithubConnector extends Logger {
  type GitPost = (String, JsValue) => Try[Unit]
  type GitPostAndGet = (String, JsValue) => Try[CommitSha]

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  val gitHubApi: GitHubApi = GitHubApi(System.getProperty("github.organisation", "equalexperts"))

  def apply(githubCreds: ServiceCredentials, releaserVersion: String) = new GithubConnector(new GithubHttp(githubCreds), gitHubApi)

  def dryRun(githubCreds: ServiceCredentials, releaserVersion: String) = {
    log.info("Github : running in dry-run mode")
    new DryRunGithubConnector(releaserVersion)
  }
}

class GithubConnector(githubHttp: GithubHttp, gitHubApi: GitHubApi, comitterDetails: GithubCommitter = GithubCommitter())
  extends GithubTagAndRelease with Logger {

  import GitHubFormats._

  private val githubTagDateTimeFormatter = org.joda.time.format.ISODateTimeFormat.dateTimeNoMillis

  def verifyTagExists(repo: Repo, sha: CommitSha): Try[Unit] = {
    githubHttp.get(gitHubApi.buildCommitGetUrl(repo, sha))
  }

  override def createTagAndGitRelease(tagDate: DateTime, commitSha: CommitSha,
                                      commitAuthor: String, commitDate: DateTime,
                                      artefactName: String, gitRepo: Repo, releaseCandidateVersion: String, version: String): Try[Unit] =
    for (
      tagSha <- createTagObject(tagDate, gitRepo, version, commitSha);
      _ <- createTagRef(gitRepo, version, tagSha);
      _ <- createGitRelease(commitSha, commitAuthor, commitDate, artefactName, gitRepo, releaseCandidateVersion, version))
      yield ()

  private def createTagObject(tagDate: DateTime, repo: Repo, tag: String, commitSha: CommitSha): Try[CommitSha] = {
    log.debug("creating annotated tag object from " + tag + " version mapping " + tag)

    val url = gitHubApi.buildAnnotatedTagObjectPostUrl(repo)
    val body = buildTagObjectBody("tag of " + tag, tag, tagDate, commitSha)()

    githubHttp.post[CommitSha]((r: WSResponse) => Success(r.json.as[TagRefResponse].sha))(url, body)
  }

  private def createTagRef(repo: Repo, tag: String, commitSha: CommitSha): Try[Unit] = {
    log.debug("creating annotated tag ref from " + tag + " version mapping " + tag)

    val url = gitHubApi.buildAnnotatedTagRefPostUrl(repo)
    val body = buildTagRefBody(tag, commitSha)

    githubHttp.postUnit(url, body)
  }

  private def createGitRelease(commitSha: CommitSha, commitAuthor: String,
                            commitDate: DateTime, artefactName: String,
                            gitRepo: Repo, releaseCandidateVersion: String, version: String): Try[Unit] = {
    log.debug(s"creating release from $commitSha version " + version)

    val url = gitHubApi.buildReleasePostUrl(gitRepo)
    val message = GitMessage(artefactName, version, releaseCandidateVersion, commitSha, commitAuthor, commitDate)
    val body = buildReleaseBody(message, version)()

    githubHttp.postUnit(url, body)
  }

  private def buildTagRefBody(version: String, commitSha: CommitSha): JsValue = {
    val tagName = "refs/tags/v" + version
    Json.toJson(TagRef(tagName, commitSha))
  }

  private def buildTagObjectBody(message: String, version: String, date: DateTime, commitSha: CommitSha)(tagName : TagName = TagName(version)): JsValue = {
    Json.toJson(TagObject(tagName.toString, message, commitSha, Tagger(comitterDetails.taggerName, comitterDetails.taggerEmail, githubTagDateTimeFormatter.print(date))))
  }

  private def buildReleaseBody(message: GitMessage, version: String)(tagName : TagName = TagName(version)): JsValue = {
    Json.toJson(GitRelease(version, tagName.toString, message.toString, draft = false, prerelease = false))
  }

  case class TagName(version : String){
    override def toString: String = s"v$version"
  }
}

class DryRunGithubConnector(releaserVersion: String) extends GithubTagAndRelease with Logger {
  val emptyGitPoster: (String, JsValue) => Try[Unit] = (a, b) => {
    log.info("[DRY RUN] Github emptyPost")
    Success(Unit)
  }
  val emptyGitPosteAndGetter: (String, JsValue) => Try[CommitSha] = (a, b) => {
    log.info("[DRY RUN] Github emptyPost")
    Success("a-fake-tag-sha")
  }

  override def verifyTagExists(repo: Repo, sha: CommitSha): Try[Unit] = {
    log.info("[DRY RUN] Git tag exists")
    Success(Unit)
  }

  override def createTagAndGitRelease(tagDate: DateTime, commitSha: CommitSha,
                                      commitAuthor: String, commitDate: DateTime,
                                      artefactName: String, gitRepo: Repo,
                                      releaseCandidateVersion: String,
                                      version: String): Try[Unit] = {
    log.info("[DRY RUN] Git tagged and release executed")
    Success(Unit)
  }
}
