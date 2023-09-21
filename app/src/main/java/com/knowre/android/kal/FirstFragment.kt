package com.knowre.android.kal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.knowre.android.kal.databinding.FragmentFirstBinding
import com.knowre.android.myscript.iink.copyAssetFileTo
import java.io.File


class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonFirst.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }

        val file = File(requireContext().filesDir, "n_digit_exp.res")
        requireContext().copyAssetFileTo(assetFileName = "n_digit_exp.res", outputFile = file)!!

        binding.myScript.setGrammar(file)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}