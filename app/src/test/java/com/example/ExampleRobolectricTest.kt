package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.api.GeminiSymptomService
import com.example.data.local.AppDatabase
import com.example.data.model.MedicationRecord
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("E-Health SATUSEHAT", appName)
    }

    @Test
    fun `insert and retrieve medication from Room`() = runBlocking {
        val med = MedicationRecord(
            name = "Amlodipine",
            dosage = "5 mg",
            frequency = "1x Sehari",
            scheduleTimes = "08:00",
            instruction = "Sesudah Makan"
        )
        val id = db.medicationDao().insertMedication(med)
        assertNotNull(id)

        val list = db.medicationDao().getAllMedications().first()
        assertEquals(1, list.size)
        assertEquals("Amlodipine", list[0].name)
    }

    @Test
    fun `test symptom analysis service fallback and structure`() = runBlocking {
        val result = GeminiSymptomService.analyzeSymptoms(
            symptomsText = "Demam tinggi, batuk kering, tenggorokan sakit",
            duration = "1-3 Hari",
            patientConditions = "Hipertensi",
            patientAllergies = "Amoxicillin"
        )
        assertTrue(result.isSuccess)
        val analysis = result.getOrNull()
        assertNotNull(analysis)
        assertTrue(analysis!!.possibleConditions.isNotEmpty())
        assertTrue(analysis.recommendedActions.isNotEmpty())
        assertNotNull(analysis.suggestedPoli)
    }
}
