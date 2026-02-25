package com.adrig.lasertag.A1_Sesion

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.adrig.lasertag.R
import com.adrig.lasertag.databinding.FragmentRegistreBinding

class F_Registre : Fragment() {

    private var _binding: FragmentRegistreBinding? = null
    private val binding get() = _binding!!

    private val viewModel: VM_Registro by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegistreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupValidation()
        observeViewModel()

        binding.btnRegistre.setOnClickListener {
            if (validateFields()) {
                viewModel.registerUser(
                    binding.etNom.text.toString(),
                    binding.etCognoms.text.toString(),
                    binding.etEmail.text.toString(),
                    binding.etContrasenya.text.toString()
                )
            }
        }
    }

    private fun observeViewModel() {
        viewModel.registrationState.observe(viewLifecycleOwner) { state ->
            binding.progressBar.isVisible = state is RegistrationState.Loading
            binding.btnRegistre.isEnabled = state !is RegistrationState.Loading

            when (state) {
                is RegistrationState.Success -> {
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.f_Login)
                }
                is RegistrationState.Error -> {
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                }
                else -> {}
            }
        }
    }

    private fun setupValidation() {
        binding.etNomUsuari.addTextChangedListener { binding.nomUsuariInputLayout.error = null }
        binding.etNom.addTextChangedListener { binding.usuariInputLayout.error = null }
        binding.etCognoms.addTextChangedListener { binding.cognomsInputLayout.error = null }
        binding.etEmail.addTextChangedListener { binding.emailInputLayout.error = null }
        binding.etContrasenya.addTextChangedListener { binding.contrasenyaInputLayout.error = null }
    }

    private fun validateFields(): Boolean {
        var isValid = true
        val email = binding.etEmail.text.toString().trim()

        if (binding.etNomUsuari.text.isNullOrBlank()) {
            binding.nomUsuariInputLayout.error = "El nom d'usuari no pot estar buit"
            isValid = false
        }
        if (binding.etNom.text.isNullOrBlank()) {
            binding.usuariInputLayout.error = "El nom no pot ser buit"
            isValid = false
        }
        if (binding.etCognoms.text.isNullOrBlank()) {
            binding.cognomsInputLayout.error = "El cognom no pot estar buit"
            isValid = false
        }
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailInputLayout.error = "Introdueix un email vàlid"
            isValid = false
        }
        if (binding.etContrasenya.text.isNullOrBlank() || binding.etContrasenya.text!!.length < 8) {
            binding.contrasenyaInputLayout.error = "La contrasenya ha de tenir almenys 8 caràcters"
            isValid = false
        }

        return isValid
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
