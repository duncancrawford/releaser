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

import com.equalexperts.pipeline.Logger
import uk.gov.hmrc.releaser.github.CommitSha

case class GitHubApi(gitHubOrganisation: String) {

  private val githubOrgUri = s"https://api.github.com/repos/$gitHubOrganisation"

  def buildCommitGetUrl(repo: Repo, sha: CommitSha) = s"$githubOrgUri/${repo.value}/git/commits/$sha"

  def buildAnnotatedTagRefPostUrl(repo: Repo) = s"$githubOrgUri/${repo.value}/git/refs"

  def buildAnnotatedTagObjectPostUrl(repo: Repo) = s"$githubOrgUri/${repo.value}/git/tags"

  def buildReleasePostUrl(repo: Repo) = s"$githubOrgUri/${repo.value}/releases"
}

case class GithubCommitter(taggerName: String = System.getProperty("github.tagger.name"),
                           taggerEmail: String = System.getProperty("github.tagger.email")) extends Logger {
  log.info(s"Github organisation details : taggerName : '$taggerName', taggerEmail : '$taggerEmail'")
}

case class Tagger(name: String, email: String, date: String)

case class TagObject(tag: String, message: String, `object`: String, tagger: Tagger, `type`: String = "commit")

case class TagRef(ref: String, sha: String)

case class TagRefResponse(sha: String)

case class GitRelease(name: String, tag_name: String, body: String, draft: Boolean, prerelease: Boolean)

object GitHubFormats {
  import play.api.libs.json.Json

  implicit val taggerFormats = Json.format[Tagger]
  implicit val tagObjectFormats = Json.format[TagObject]
  implicit val tagRefFormats = Json.format[TagRef]
  implicit val tagRefResponseFormats = Json.format[TagRefResponse]
  implicit val gitReleaseFormats = Json.format[GitRelease]
}