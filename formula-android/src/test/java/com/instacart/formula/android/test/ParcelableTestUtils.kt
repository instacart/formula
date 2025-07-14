package com.instacart.formula.android.test

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import org.junit.Assert.assertEquals

object ParcelableTestUtils {

    inline fun <reified T : Parcelable> recreateAndCompare(original: T): T {
        val copy = recreate(original)
        assertEquals(original, copy)
        return copy
    }

    inline fun <reified T : Parcelable> recreate(original: T): T {
        val bundle = Bundle().apply { putParcelable(T::class.java.name, original) }
        val bytes = Parcel.obtain().run {
            writeBundle(bundle)
            val array = marshall()
            recycle()
            array
        }

        return unmarshallParcelable(bytes)
    }

    inline fun <reified T : Parcelable> unmarshallParcelable(bytes: ByteArray): T {
        val parcel = Parcel.obtain().apply {
            unmarshall(bytes, 0, bytes.size)
            setDataPosition(0)
        }

        val value: T = parcel
            .readBundle(T::class.java.classLoader)!!
            .getParcelable(T::class.java.name)!!
        parcel.recycle()
        return value
    }
}
