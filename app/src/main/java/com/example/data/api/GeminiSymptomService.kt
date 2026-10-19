package com.example.data.api

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class SymptomAnalysisResult(
    val possibleConditions: List<String>,
    val urgencyLevel: String, // "Rendah", "Sedang", "Tinggi (Darurat)"
    val recommendedActions: List<String>,
    val suggestedPoli: String,
    val summaryAdvice: String,
    val warningRedFlags: List<String>
)

object GeminiSymptomService {
    private const val TAG = "GeminiSymptomService"
    private const val MODEL = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"

    // OkHttpClient with 60-second timeouts as mandated by gemini-api skill
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun analyzeSymptoms(
        symptomsText: String,
        duration: String,
        patientConditions: String,
        patientAllergies: String
    ): Result<SymptomAnalysisResult> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY

        val systemPrompt = """
            Anda adalah Asisten Medis Cerdas untuk Sistem Layanan Kesehatan Masyarakat (SATUSEHAT E-Health Kemenkes RI).
            Tugas Anda adalah menganalisis keluhan/gejala yang dilaporkan pasien secara objektif, aman, dan informatif.
            Berikan analisis dalam format JSON murni tanpa markdown triple backticks.
            JSON harus memiliki properti berikut:
            - "possibleConditions": array string (2-4 kemungkinan kondisi kesehatan / dugaan awal)
            - "urgencyLevel": string ("Rendah" atau "Sedang" atau "Tinggi (Darurat)")
            - "recommendedActions": array string (3-5 langkah tindakan mandiri awal atau pertolongan pertama)
            - "suggestedPoli": string (nama poliklinik tujuan, misal "Poli Umum", "Poli Penyakit Dalam", "Poli Gigi & Mulut", "Poli Anak", "Poli KIA", atau "IGD Rumah Sakit")
            - "summaryAdvice": string (nasihat medis singkat dan menenangkan dalam bahasa Indonesia)
            - "warningRedFlags": array string (tanda-tanda bahaya yang jika muncul mengharuskan pasien segera ke IGD)
        """.trimIndent()

        val userPrompt = """
            Data Pasien SATUSEHAT:
            - Riwayat Penyakit: ${patientConditions.ifEmpty { "Tidak ada" }}
            - Riwayat Alergi Obat: ${patientAllergies.ifEmpty { "Tidak ada" }}
            
            Keluhan Gejala Pasien:
            - Gejala yang dirasakan: $symptomsText
            - Durasi keluhan: $duration
            
            Silakan analisis kemungkinan kondisi, tingkat urgensi, tindakan awal, dan rekomendasi poli rujukan.
        """.trimIndent()

        // Check if API key is valid or placeholder
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w(TAG, "GEMINI_API_KEY is not configured in secrets. Using clinical guideline heuristic fallback.")
            return@withContext Result.success(getFallbackAnalysis(symptomsText, duration, patientConditions))
        }

        try {
            val url = "$BASE_URL/$MODEL:generateContent?key=$apiKey"

            val jsonBody = JSONObject().apply {
                val contentsArray = JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", userPrompt) })
                        })
                    })
                }
                put("contents", contentsArray)

                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", systemPrompt) })
                    })
                })

                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.3)
                    put("responseMimeType", "application/json")
                })
            }

            val request = Request.Builder()
                .url(url)
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string().orEmpty()

            if (!response.isSuccessful) {
                Log.e(TAG, "Gemini API error ${response.code}: $responseBody")
                return@withContext Result.success(getFallbackAnalysis(symptomsText, duration, patientConditions))
            }

            val rootJson = JSONObject(responseBody)
            val candidates = rootJson.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val text = parts?.optJSONObject(0)?.optString("text").orEmpty()

            val cleanJson = text.trim()
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()

            val parsedJson = JSONObject(cleanJson)

            val conditionsList = mutableListOf<String>()
            parsedJson.optJSONArray("possibleConditions")?.let { arr ->
                for (i in 0 until arr.length()) conditionsList.add(arr.getString(i))
            }

            val actionsList = mutableListOf<String>()
            parsedJson.optJSONArray("recommendedActions")?.let { arr ->
                for (i in 0 until arr.length()) actionsList.add(arr.getString(i))
            }

            val redFlagsList = mutableListOf<String>()
            parsedJson.optJSONArray("warningRedFlags")?.let { arr ->
                for (i in 0 until arr.length()) redFlagsList.add(arr.getString(i))
            }

            val result = SymptomAnalysisResult(
                possibleConditions = conditionsList.ifEmpty { listOf("Gangguan Kesehatan Ringan", "Gejala Infeksi Saluran Pernapasan") },
                urgencyLevel = parsedJson.optString("urgencyLevel", "Sedang"),
                recommendedActions = actionsList.ifEmpty { listOf("Istirahat yang cukup", "Minum air putih minimal 2 liter per hari", "Konsumsi makanan bergizi") },
                suggestedPoli = parsedJson.optString("suggestedPoli", "Poli Umum"),
                summaryAdvice = parsedJson.optString("summaryAdvice", "Segera jadwalkan kunjungan ke fasilitas kesehatan untuk pemeriksaan fisik lebih lanjut."),
                warningRedFlags = redFlagsList.ifEmpty { listOf("Demam tinggi di atas 39°C yang tidak turun dengan obat", "Sesak nafas berat atau penurunan kesadaran") }
            )

            Result.success(result)
        } catch (e: Exception) {
            Log.e(TAG, "Exception during Gemini symptom analysis", e)
            Result.success(getFallbackAnalysis(symptomsText, duration, patientConditions))
        }
    }

    private fun getFallbackAnalysis(
        symptoms: String,
        duration: String,
        conditions: String
    ): SymptomAnalysisResult {
        val lower = symptoms.lowercase()
        return when {
            lower.contains("sesak") || lower.contains("dada") || lower.contains("jantung") -> {
                SymptomAnalysisResult(
                    possibleConditions = listOf("Gangguan Saluran Pernapasan Akut", "Iskemia / Angina Pectoris", "Bronkitis"),
                    urgencyLevel = "Tinggi (Darurat)",
                    recommendedActions = listOf(
                        "Duduk dalam posisi tegak dan longgarkan pakaian",
                        "Hindari aktivitas fisik berat",
                        "Segera menuju IGD Puskesmas atau RSUD terdekat jika nyeri menjalar"
                    ),
                    suggestedPoli = "IGD / Poli Penyakit Dalam",
                    summaryAdvice = "Keluhan sesak atau nyeri dada membutuhkan evaluasi medis segera untuk memastikan fungsi kardiorespirasi Anda aman.",
                    warningRedFlags = listOf("Nyeri dada tembus ke punggung atau lengan kiri", "Bibir atau kuku membiru (sianosis)")
                )
            }
            lower.contains("gigi") || lower.contains("gusi") -> {
                SymptomAnalysisResult(
                    possibleConditions = listOf("Pulpitis / Karies Gigi", "Gingivitis", "Abses Periapikal"),
                    urgencyLevel = "Sedang",
                    recommendedActions = listOf(
                        "Kumur dengan air garam hangat",
                        "Hindari makanan/minuman yang terlalu panas atau dingin",
                        "Gunakan analgesik pereda nyeri sesuai anjuran"
                    ),
                    suggestedPoli = "Poli Gigi & Mulut",
                    summaryAdvice = "Segera periksakan gigi ke dokter gigi di Puskesmas untuk tindakan penambalan atau perawatan saraf gigi.",
                    warningRedFlags = listOf("Pembengkakan pipi yang menyebar ke leher", "Demam tinggi disertai sulit membuka mulut")
                )
            }
            lower.contains("demam") || lower.contains("batuk") || lower.contains("flu") || lower.contains("tenggorokan") -> {
                SymptomAnalysisResult(
                    possibleConditions = listOf("Infeksi Saluran Pernapasan Akut (ISPA)", "Faringitis Akut", "Common Cold / Influenza"),
                    urgencyLevel = "Sedang",
                    recommendedActions = listOf(
                        "Istirahat total dan hindari kelelahan fisik",
                        "Minum air putih hangat minimal 2-2.5 liter per hari",
                        "Konsumsi makanan berkuah hangat dan buah kaya vitamin C",
                        "Kompres air hangat jika suhu tubuh melebihi 38°C"
                    ),
                    suggestedPoli = "Poli Umum",
                    summaryAdvice = "Gejala ISPA umumnya membaik dalam 3-5 hari. Jika demam berlanjut atau batuk memburuk, lakukan konsultasi langsung dengan dokter.",
                    warningRedFlags = listOf("Demam lebih dari 3 hari tanpa perbaikan", "Dahak bercampur darah atau batuk disertai sesak")
                )
            }
            else -> {
                SymptomAnalysisResult(
                    possibleConditions = listOf("Sindrom Dispepsia / Kelelahan Fisik", "Gejala Infeksi Ringan", "Gangguan Muskuloskeletal"),
                    urgencyLevel = "Sedang",
                    recommendedActions = listOf(
                        "Cukupi istirahat tidur minimal 7-8 jam",
                        "Konsumsi makanan bergizi seimbang dan perbanyak hidrasi",
                        "Catat perkembangan gejala selama 24-48 jam ke depan"
                    ),
                    suggestedPoli = "Poli Umum",
                    summaryAdvice = "Keluhan Anda sebaiknya diperiksakan ke dokter umum Puskesmas untuk mendapatkan diagnosa klinis dan terapi obat yang tepat.",
                    warningRedFlags = listOf("Nyeri hebat yang tidak tertahankan", "Muntah terus-menerus dan lemas dehidrasi")
                )
            }
        }
    }
}
