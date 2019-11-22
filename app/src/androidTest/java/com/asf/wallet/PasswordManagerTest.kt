package com.asf.wallet

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.asfoundation.wallet.repository.InternalPasswordManager.getPassword
import com.asfoundation.wallet.repository.InternalPasswordManager.setPassword
import org.hamcrest.Matchers
import org.junit.Assert
import org.junit.Test

class PasswordManagerTest {
  @Test
  fun setGetPassword() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    setPassword(ADDRESS, PASSWORD, context)
    val password = getPassword(ADDRESS, context)
    Assert.assertThat(password, Matchers.`is`(PASSWORD))
  }

  companion object {
    private const val PASSWORD = "mypassword"
    private const val ADDRESS = "myaddress"
  }
}