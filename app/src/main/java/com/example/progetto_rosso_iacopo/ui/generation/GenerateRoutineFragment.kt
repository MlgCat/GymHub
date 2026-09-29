package com.example.progetto_rosso_iacopo.ui.generation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.progetto_rosso_iacopo.databinding.FragmentGenerateRoutineBinding

class GenerateRoutineFragment : Fragment() {
    val viewModel: GenerateRoutineViewModel by viewModels()
    private var _binding: FragmentGenerateRoutineBinding? = null
    val binding: FragmentGenerateRoutineBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentGenerateRoutineBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // OBIETTIVO (RadioGroup -> singola selezione)
        binding.radioGroupObiettivo.setOnCheckedChangeListener { _, checkedId ->
            val obiettivo = when (checkedId) {
                binding.radioIpertrofia.id -> "ipertrofia"
                binding.radioForza.id -> "forza"
                binding.radioDimagrimento.id -> "dimagrimento"
                binding.radioResistenza.id -> "resistenza"
                binding.radioTonificazione.id -> "tonificazione"
                else -> null
            }
            obiettivo?.let { viewModel.updateGoal(it) }
        }

        // LIVELLO DI ESPERIENZA (RadioGroup -> singola selezione)
        binding.radioGroupLivello.setOnCheckedChangeListener { _, checkedId ->
            val livello = when (checkedId) {
                binding.radioPrincipiante.id -> "principiante"
                binding.radioIntermedio.id -> "intermedio"
                binding.radioAvanzato.id -> "avanzato"
                else -> null
            }
            livello?.let { viewModel.updateExperienceLevel(it) }
        }

        // ATTREZZATURA (CheckBox multiple -> lista di stringhe)
        val attrezzaturaListener = View.OnClickListener {
            val selezionati = mutableListOf<String>()
            if (binding.checkCorpoLibero.isChecked) selezionati.add("corpo_libero")
            if (binding.checkManubri.isChecked) selezionati.add("manubri")
            if (binding.checkBilanciere.isChecked) selezionati.add("bilanciere")
            if (binding.checkMacchine.isChecked) selezionati.add("macchine")
            if (binding.checkElastici.isChecked) selezionati.add("elastici")
            if (binding.checkKettlebell.isChecked) selezionati.add("kettlebell")
            if (binding.checkPalestraCompleta.isChecked) selezionati.add("palestra_completa")
            viewModel.updateEquipment(selezionati)
        }
        binding.checkCorpoLibero.setOnClickListener(attrezzaturaListener)
        binding.checkManubri.setOnClickListener(attrezzaturaListener)
        binding.checkBilanciere.setOnClickListener(attrezzaturaListener)
        binding.checkMacchine.setOnClickListener(attrezzaturaListener)
        binding.checkElastici.setOnClickListener(attrezzaturaListener)
        binding.checkKettlebell.setOnClickListener(attrezzaturaListener)
        binding.checkPalestraCompleta.setOnClickListener(attrezzaturaListener)

        // LIMITAZIONI (CheckBox multiple -> lista di stringhe)
        val limitazioniListener = View.OnClickListener {
            val selezionate = mutableListOf<String>()
            if (binding.checkNessunaLimitazione.isChecked) selezionate.add("nessuna")
            if (binding.checkGinocchio.isChecked) selezionate.add("ginocchio")
            if (binding.checkSchiena.isChecked) selezionate.add("schiena")
            if (binding.checkSpalla.isChecked) selezionate.add("spalla")
            if (binding.checkAnca.isChecked) selezionate.add("anca")
            if (binding.checkCardiovascolare.isChecked) selezionate.add("cardiovascolare")
            if (binding.checkAltraLimitazione.isChecked) selezionate.add("altro")
            viewModel.updateLimitations(selezionate)
        }
        binding.checkNessunaLimitazione.setOnClickListener(limitazioniListener)
        binding.checkGinocchio.setOnClickListener(limitazioniListener)
        binding.checkSchiena.setOnClickListener(limitazioniListener)
        binding.checkSpalla.setOnClickListener(limitazioniListener)
        binding.checkAnca.setOnClickListener(limitazioniListener)
        binding.checkCardiovascolare.setOnClickListener(limitazioniListener)
        binding.checkAltraLimitazione.setOnClickListener(limitazioniListener)

        // RICHIESTE ADDIZIONALI (testo libero)
        binding.editRichiesteAddizionali.doAfterTextChanged { testo ->
            viewModel.updateAdditionalRequests(testo?.toString().orEmpty())
        }

        // PULSANTE GENERA
        binding.btnGeneraScheda.setOnClickListener {
            viewModel.onGenerate()
        }

        // STATO DI CARICAMENTO
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBarGenerazione.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnGeneraScheda.isEnabled = !isLoading
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}