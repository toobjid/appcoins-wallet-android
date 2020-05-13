package com.asfoundation.wallet.backup

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.core.content.FileProvider
import androidx.documentfile.provider.DocumentFile
import com.asf.wallet.BuildConfig
import io.reactivex.Completable
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class FileInteract(private val contentResolver: ContentResolver) {

  private var cachedFile: File? = null

  fun createTmpFile(walletAddress: String, content: String, path: File?): Completable {
    val fileName = getDefaultBackupFileName(walletAddress)

    //createTempFile adds numbers in front of filename
    if (path == null) return Completable.error(Throwable("Null path"))
    val file = File.createTempFile("$fileName-", getDefaultBackupFileExtension(), path)

    val fileOutputStream = FileOutputStream(file, true)
    try {
      fileOutputStream.write(content.toByteArray())
      cachedFile = file
    } catch (e: IOException) {
      e.printStackTrace()
      return Completable.error(Throwable(e))
    } finally {
      fileOutputStream.close()
    }
    return Completable.complete()
  }

  //Use this method for android P and below
  fun createAndSaveFile(content: String, path: File?, fileName: String): Completable {
    val stringPath = path?.path ?: return Completable.error(Throwable("Null path"))
    val file = File("$stringPath/$fileName")

    val fileOutputStream = FileOutputStream(file, false)
    try {
      fileOutputStream.write(content.toByteArray())
    } catch (e: IOException) {
      e.printStackTrace()
      return Completable.error(Throwable(e))
    } finally {
      fileOutputStream.close()
    }
    return Completable.complete()
  }

  //Use this method for Android Q and above
  fun createAndSaveFile(content: String, documentFile: DocumentFile,
                        fileName: String): Completable {
    val file = documentFile.createFile("text/plain", fileName) ?: return Completable.error(
        Throwable("Error creating file"))

    val outputStream = contentResolver.openOutputStream(file.uri)
    try {
      outputStream?.run { write(content.toByteArray()) } ?: return Completable.error(
          Throwable("Null outputStream"))
    } catch (e: IOException) {
      e.printStackTrace()
      return Completable.error(Throwable(e))
    } finally {
      outputStream?.close()
    }
    return Completable.complete()
  }

  fun deleteFile() {
    try {
      cachedFile?.delete()
      cachedFile = null
    } catch (e: SecurityException) {
      e.printStackTrace()
    }
  }

  fun getCachedFile(): File? = cachedFile

  fun getDefaultBackupFileFullName(walletAddress: String) =
      getDefaultBackupFileName(walletAddress) + getDefaultBackupFileExtension()

  private fun getDefaultBackupFileName(walletAddress: String) = "walletbackup$walletAddress"

  private fun getDefaultBackupFileExtension() = ".txt"

  //If android Q or above, the user must choose the directory so that the file isn't deleted when the app is uninstall
  fun getDownloadPath(): File? {
    return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) Environment.getExternalStoragePublicDirectory(
        Environment.DIRECTORY_DOWNLOADS)
    else null
  }

  fun getTemporaryPath(context: Context?): File? {
    return context?.externalCacheDir
  }

  fun getUriFromFile(context: Context, file: File): Uri {
    return FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file)
  }
}