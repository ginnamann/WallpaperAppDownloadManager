package com.example.downloadcoroutines.ui.fragments

import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.downloadcoroutines.R
import com.example.downloadcoroutines.databinding.FragmentProfileBinding


class ProfileFragment : Fragment() {
    lateinit var binding: FragmentProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile, container, false)
        setAnim()
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        val window: Window = requireActivity().getWindow()
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = resources.getColor(R.color.deep_purple_900)
    }

    fun setAnim() {
        binding.animeProfile.setAnimationFromUrl("https://assets7.lottiefiles.com/packages/lf20_dgsA0b.json")
        binding.animeProfilePic.setAnimationFromUrl("https://assets7.lottiefiles.com/datafiles/6deVuMSwjYosId3/data.json")

    }

}