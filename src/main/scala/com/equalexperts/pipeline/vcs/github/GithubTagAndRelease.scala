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

import org.joda.time.DateTime

import scala.util.Try

trait GithubTagAndRelease {

  import GithubTagAndRelease.CommitSha

  def verifyTagExists(repo: Repo, sha: CommitSha): Try[Unit]

  def createTagAndGitRelease(tagDate: DateTime,
                             commitSha: CommitSha,
                             commitAuthor: String,
                             commitDate: DateTime,
                             artefactName: String,
                             gitRepo: Repo,
                             releaseCandidateVersion: String,
                             version: String): Try[Unit]
}

object GithubTagAndRelease {
  type CommitSha = String
}