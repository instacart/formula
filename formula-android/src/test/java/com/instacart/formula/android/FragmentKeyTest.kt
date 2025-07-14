package com.instacart.formula.android

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.instacart.formula.android.internal.EmptyFragmentKey
import com.instacart.formula.android.test.ParcelableTestUtils
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FragmentKeyTest {

    @Test fun `parcelize does not save instance id`() {
        val key = EmptyFragmentKey("my-tag")
        key.setInstanceId("my-instance-id")

        val savedKey = ParcelableTestUtils.recreate(key)
        assertThat(savedKey.getInstanceId()).isNull()
    }

    @Test fun `getOrInitInstanceId always returns the same value`() {

        val key = EmptyFragmentKey("tag-1")
        val value = key.getOrInitInstanceId()
        assertThat(value).isNotNull()
        assertThat(key.getOrInitInstanceId()).isEqualTo(value)
        assertThat(key.getOrInitInstanceId()).isEqualTo(value)
    }

    @Test fun `getOrInitInstanceId generates a random value if no value exists`() {
        val key = EmptyFragmentKey("tag-1")
        assertThat(key.getOrInitInstanceId()).isNotNull()

        val key2 = EmptyFragmentKey("tag-2")
        assertThat(key2.getOrInitInstanceId()).isNotEqualTo(key.getOrInitInstanceId())
    }

    @Test fun `getOrInitInstance uses existing value if already set`() {
        val key = EmptyFragmentKey("my-tag")
        key.setInstanceId("my-instance-id")

        assertThat(key.getOrInitInstanceId()).isEqualTo("my-instance-id")
    }
}