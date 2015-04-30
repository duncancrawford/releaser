/*
 * Copyright 2015 HM Revenue & Customs
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

package uk.gov.hmrc.releaser.domain


trait BintrayPaths{

  val bintrayRepoRoot = "https://bintray.com/artifact/download/hmrc"
  val bintrayApiRoot = "https://bintray.com/api/v1"

  def metadata(repo:String, artefactName: String): String = {
    s"$bintrayApiRoot/packages/hmrc/$repo/$artefactName"
  }

  def publishUrlFor(v: VersionDescriptor): String = {
    s"$bintrayApiRoot/content/hmrc/${v.repo}/${v.artefactName}/${v.version}/publish"
  }
}

object BintrayPaths extends BintrayPaths

trait PathBuilder extends BintrayPaths{

  def jarFilenameFor(v: VersionDescriptor): String

  def jarDownloadFor(v: VersionDescriptor): String

  def jarUploadFor(v: VersionDescriptor): String

  def pomUploadFor(v: VersionDescriptor): String

  def pomFilenameFor(v: VersionDescriptor): String

  def pomDownloadUrlFor(v: VersionDescriptor): String
}

trait BintrayIvyPaths extends PathBuilder {

  val sbtVersion = "sbt_0.13"
  def scalaVersion:String

  override def jarFilenameFor(v:VersionDescriptor):String={
    s"${v.artefactName}.jar"
  }

  override def jarDownloadFor(v:VersionDescriptor):String={
    val fileName = jarFilenameFor(v)
    s"$bintrayRepoRoot/${v.repo}/uk.gov.hmrc/${v.artefactName}/scala_$scalaVersion/$sbtVersion/${v.version}/jars/$fileName"
  }

  override def pomDownloadUrlFor(v: VersionDescriptor): String = {
    val fileName = pomFilenameFor(v)
    s"$bintrayRepoRoot/${v.repo}/uk.gov.hmrc/${v.artefactName}/scala_$scalaVersion/$sbtVersion/${v.version}/ivys/$fileName"
  }

  override def jarUploadFor(v:VersionDescriptor):String={
    val fileName = jarFilenameFor(v)
    s"$bintrayApiRoot/content/hmrc/${v.repo}/uk.gov.hmrc/${v.artefactName}/scala_$scalaVersion/$sbtVersion/${v.version}/jars/$fileName"
  }

  override def pomUploadFor(v: VersionDescriptor): String = {
    val fileName = pomFilenameFor(v)
    s"$bintrayApiRoot/content/hmrc/${v.repo}/uk.gov.hmrc/${v.artefactName}/scala_$scalaVersion/$sbtVersion/${v.version}/ivys/$fileName"
  }

  override def pomFilenameFor(v: VersionDescriptor): String = "ivy.xml"

}

trait BintrayMavenPaths extends PathBuilder{

  def scalaVersion:String

  def jarFilenameFor(v:VersionDescriptor):String={
    s"${v.artefactName}_$scalaVersion-${v.version}.jar"
  }

  def jarDownloadFor(v:VersionDescriptor):String={
    val fileName = jarFilenameFor(v)
    s"$bintrayRepoRoot/${v.repo}/uk/gov/hmrc/${v.artefactName}_$scalaVersion/${v.version}/$fileName"
  }

  def pomDownloadUrlFor(v: VersionDescriptor): String = {
    val fileName = pomFilenameFor(v)
    s"$bintrayRepoRoot/${v.repo}/uk/gov/hmrc/${v.artefactName}_$scalaVersion/${v.version}/$fileName"
  }

  def jarUploadFor(v:VersionDescriptor):String={
    val fileName = jarFilenameFor(v)
    s"$bintrayApiRoot/maven/hmrc/${v.repo}/${v.artefactName}/uk/gov/hmrc/${v.artefactName}_$scalaVersion/${v.version}/$fileName"
  }

  def pomFilenameFor(v: VersionDescriptor) = s"${v.artefactName}_$scalaVersion-${v.version}.pom"

  def pomUploadFor(v: VersionDescriptor): String={
    val fileName = pomFilenameFor(v)
    s"$bintrayApiRoot/maven/hmrc/${v.repo}/${v.artefactName}/uk/gov/hmrc/${v.artefactName}_$scalaVersion/${v.version}/$fileName"
  }

}
