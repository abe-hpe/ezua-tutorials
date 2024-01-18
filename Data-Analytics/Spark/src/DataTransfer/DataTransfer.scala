package com.mapr.sparkdemo

import org.apache.spark.sql.{SaveMode, SparkSession}
import org.apache.spark.sql.functions.col
import org.apache.spark.sql.DataFrame

import scala.reflect.io.Directory

import java.io.File
import java.nio.file.{Paths, Files, Path, StandardCopyOption}


object DataTransfer {
  def main(args: Array[String]): Unit = {
    if (args.length != 4) {
      printUsage()
      System.exit(1)
    }

    val srcPath = args(0)
    val srcFormat = args(1)
    val destPath = args(2)
    val destFormat = args(3)

    validateDestinationPath(destPath)

    val session = SparkSession.builder().getOrCreate()

    println(s"Reading from $srcPath; src format is $srcFormat")
    var sourceDF: DataFrame = null
    try {
      sourceDF = session.read.format(srcFormat).load(srcPath)

      if (sourceDF == null || sourceDF.count() == 0) {
        throw new Exception("sourceDF is null after reading or has no rows")
      }
    } catch {
      case e: Exception => {
        println(s"Can not read from $srcPath: ${e.getMessage}")
        System.exit(1)
      }
    }
    println("Read complete")

    println(s"Writing to $destPath; dest format is $destFormat")
    try {
      sourceDF.write
        .format(destFormat)
        .mode(SaveMode.Overwrite)
        .save(destPath)
      println("Write complete")
    } catch {
      case e: Exception => {
        println(s"Can not write to $destPath: ${e.getMessage}")
        System.exit(1)
      }
    }

    session.stop()
  }

  private def printUsage(): Unit = {
    println(
      """App expects exactly 4 arguments:
        |- source path
        |- source format
        |- destination path
        |- destination format
        |""".stripMargin)
  }

  private def validateDestinationPath(destPath: String): Unit = {
    val internalFsMode = "file://"
    if (!(destPath.contains(internalFsMode))) {
      println(s"Can not write to PV. Destination path is not '$internalFsMode'")
      System.exit(1)
    }
  }
}
