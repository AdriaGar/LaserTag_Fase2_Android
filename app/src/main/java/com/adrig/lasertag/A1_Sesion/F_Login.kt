package com.adrig.lasertag.A1_Sesion

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.adrig.lasertag.A2_BuscarPartida.A2_BuscarPartida
import com.adrig.lasertag.R
import com.adrig.lasertag.databinding.FragmentLoginBinding

class F_Login : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel: VM_Login by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupValidation()
        observeViewModel()

        binding.btnLogin.setOnClickListener {
            if (validateFields()) {
                viewModel.loginUser(
                    binding.etEmail.text.toString(),
                    binding.etContrasenya.text.toString(),
                    requireContext()
                )
            }
        }

        binding.btnGoToRegister.setOnClickListener {
            findNavController().navigate(R.id.action_f_Login_to_f_Registre)
        }
    }

    private fun observeViewModel() {
        viewModel.loginState.observe(viewLifecycleOwner) { state ->
            binding.progressBar.isVisible = state is LoginState.Loading
            binding.btnLogin.isEnabled = state !is LoginState.Loading

            when (state) {
                is LoginState.Success -> {
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                    val intent = Intent(requireActivity(), A2_BuscarPartida::class.java)
                    startActivity(intent)
                    requireActivity().finish()
                }
                is LoginState.Error -> {
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                }
                else -> {}
            }
        }
    }

    private fun setupValidation() {
        binding.etEmail.addTextChangedListener { binding.emailInputLayout.error = null }
        binding.etContrasenya.addTextChangedListener { binding.contrasenyaInputLayout.error = null }
    }

    private fun validateFields(): Boolean {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etContrasenya.text.toString().trim()
        var isValid = true

        if (email.isEmpty()) {
            binding.emailInputLayout.error = "El camp d'email no pot estar buit"
            isValid = false
        }

        if (password.isEmpty()) {
            binding.contrasenyaInputLayout.error = "La contrasenya no pot estar buita"
            isValid = false
        }

        return isValid
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
