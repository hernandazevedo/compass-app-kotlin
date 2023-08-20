package org.mockito.configuration

@Suppress("unused")
class MockitoConfiguration : DefaultMockitoConfiguration() {

    override fun enableClassCache(): Boolean = false
}
