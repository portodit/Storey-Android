package com.example.storey.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.storey.databinding.ItemLoaderStateBinding

class LoadingStateAdapter(private val retry: () -> Unit) :
    LoadStateAdapter<LoadingStateAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState) = ViewHolder(
        ItemLoaderStateBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        ), retry
    )

    override fun onBindViewHolder(holder: ViewHolder, loadState: LoadState) {
        holder.bind(loadState)
    }

    class ViewHolder(private val binding: ItemLoaderStateBinding, private val retry: () -> Unit) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(loadState: LoadState) {
            with(binding) {
                btnRetry.setOnClickListener { retry.invoke() }

                if (loadState is LoadState.Error) {
                    tvMessage.text = loadState.error.localizedMessage
                }

                progressbar.isVisible = loadState is LoadState.Loading
                btnRetry.isVisible = loadState is LoadState.Error
                tvMessage.isVisible = loadState is LoadState.Error
            }
        }
    }
}