package com.wmrmobile.app

import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun appVersion_isInitialDevelopmentVersion() {
        assertEquals("0.1.0", BuildConfig.VERSION_NAME)
    }
}
