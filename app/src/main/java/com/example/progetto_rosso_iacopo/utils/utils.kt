package com.example.progetto_rosso_iacopo.utils

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.progetto_rosso_iacopo.data.model.WorkoutRoutine
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot

class UniversalBindingDelegate<T>(
    private val liveData: MutableLiveData<T>,
    private val typeCheck: (String) -> T,
    private val defaultString: String = ""
) : ReadWriteProperty<Any?, String> {

    override fun getValue(thisRef: Any?, property: KProperty<*>): String {
        return liveData.value?.toString() ?: defaultString
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
        val convertedValue = try {
            typeCheck(value)
        } catch (e: Exception) {
            return // Ignora se la conversione fallisce
        }

        // AGGIORNATO: Rimosso value.isEmpty(). Confronta solo i valori reali.
        if (liveData.value != convertedValue) {
            liveData.value = convertedValue
        }
    }
}

inline fun <reified T> MutableLiveData<T>.asBindingProperty(defaultString: String? = null): UniversalBindingDelegate<T> {
    return when (T::class) {
        String::class -> {
            UniversalBindingDelegate(
                liveData = this,
                // CORREZIONE: Forza il compilatore a passare la stringa pulita senza cast generici instabili
                typeCheck = { it as T },
                defaultString = defaultString ?: ""
            )
        }
        Int::class -> {
            val default = defaultString ?: ""
            val defaultInt = default.toIntOrNull() ?: 0
            UniversalBindingDelegate(
                liveData = this,
                typeCheck = { text ->
                    val parsed = text.toIntOrNull() ?: defaultInt
                    parsed as T
                },
                defaultString = default
            )
        }
        else -> throw IllegalArgumentException("Tipo non supportato dal delegato")
    }
}

fun Query.fetch(onSuccess: (QuerySnapshot)->Unit, result: MutableLiveData<FetchResult>){
    this.get().addOnSuccessListener(
        { querySnapShot ->
            result.value = FetchResult.Success
            onSuccess(querySnapShot)
        }
    ).addOnFailureListener ({exception->
        Log.e("FIRESTORE_QUERY", "Errore nel recupero dati", exception)
        result.value = FetchResult.FirebaseError(exception.localizedMessage?: "")
    })
}

fun DocumentReference.fetch(onSuccess: (DocumentSnapshot)->Unit,  result: MutableLiveData<FetchResult>){
    this.get().addOnSuccessListener(
        { document ->
            result.value = FetchResult.Success
            onSuccess(document)
        }
    ).addOnFailureListener ({exception->
        Log.e("FIRESTORE_CRITICAL", "Errore reale rilevato:", exception)
        result.value = FetchResult.FirebaseError(exception.localizedMessage ?: "Errore sconosciuto")
    })
}



sealed class FetchResult {
    data object Success : FetchResult()
    data class FirebaseError(val message: String) : FetchResult()
    data class GenericError(val message: String) : FetchResult()
}