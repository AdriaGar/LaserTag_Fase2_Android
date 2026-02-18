package com.adrig.lasertag.A1_Sesion

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.adrig.lasertag.R
import com.adrig.lasertag.databinding.FragmentInvitadoBinding
import com.adrig.lasertag.A2_BuscarPartida.A2_BuscarPartida

class F_Invitado : Fragment() {
    private var _binding: FragmentInvitadoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInvitadoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnContinueAsGuest.setOnClickListener {
            val intent = Intent(requireActivity(), A2_BuscarPartida::class.java)
            startActivity(intent)
            requireActivity().finish()
        }

        binding.btnGoToLogin.setOnClickListener {
            findNavController().navigate(R.id.f_Login)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
