package com.example.ailens

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.browser.customtabs.CustomTabsIntent
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.ailens.databinding.FragmentSuccessBinding

class SuccessFragment : Fragment() {
    private val args:SuccessFragmentArgs by navArgs()
    private var _binding:FragmentSuccessBinding?=null
    private val binding get() = _binding!!
    private var searchResultList:List<SearchResult> =ArrayList()
    private lateinit var srAdapter: SearchResultAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentSuccessBinding.inflate(inflater, container, false)

        searchResultList=args.searchResultList

        searchResultList?.let {
            srAdapter= SearchResultAdapter(requireContext(),searchResultList,createSearchItemClickListener())
            binding.rvSearchResult.layoutManager=LinearLayoutManager(requireActivity())
            binding.rvSearchResult.setHasFixedSize(true)
            binding.rvSearchResult.adapter=srAdapter
        }

        return binding.root
    }

    private fun createSearchItemClickListener() = object : SearchResultAdapter.OnSearchItemClickListener{
        override fun onClickListener(position: Int) {
            val url = searchResultList[position].link
            val builder: CustomTabsIntent.Builder=CustomTabsIntent.Builder()
            val customTabsIntent = builder.build();
            customTabsIntent.launchUrl(requireContext(), Uri.parse(url));
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        this._binding =null
    }
}