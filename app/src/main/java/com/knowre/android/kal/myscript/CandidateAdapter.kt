package com.knowre.android.kal.myscript

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.knowre.android.kal.databinding.ViewCandidateItemBinding


internal class CandidateAdapter(
    private val onCandidateClicked: (Candidate.Data) -> Unit,
    private val onExitClicked: () -> Unit
) : RecyclerView.Adapter<CandidateViewHolder>() {

    private var candidates = listOf<Candidate>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CandidateViewHolder {
        return CandidateViewHolder.newInstance(parent,
            onCandidateClicked = {
                clear()
                onCandidateClicked(it)
            },
            onExitClicked = {
                clear()
                onExitClicked()
            }
        )
    }

    override fun onBindViewHolder(holder: CandidateViewHolder, position: Int) {
        holder.bind(candidates[position])
    }

    override fun getItemCount() = candidates.size

    fun setCandidates(candidates: List<Candidate>) {
        this.candidates = candidates
            .toMutableList()
            .apply { if (isNotEmpty()) add(Candidate.Exit()) }
            .also { notifyDataSetChanged() }
    }

    fun clear() {
        this.candidates = listOf()
        notifyDataSetChanged()
    }
}

internal class CandidateViewHolder(
    private val binding: ViewCandidateItemBinding
) : RecyclerView.ViewHolder(binding.root) {

    private lateinit var candidate: Candidate

    fun bind(candidate: Candidate) {
        this.candidate = candidate
        when (candidate) {
            is Candidate.Data -> binding.candidateText.text = candidate.label
            is Candidate.Exit -> {
                binding.candidateText.text = "X"
            }
        }
    }

    companion object {
        fun newInstance(
            parent: ViewGroup,
            onCandidateClicked: (Candidate.Data) -> Unit,
            onExitClicked: () -> Unit
        ): CandidateViewHolder {
            return CandidateViewHolder(ViewCandidateItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)).apply {
                binding.root.setOnClickListener {
                    if (candidate is Candidate.Data) {
                        onCandidateClicked(candidate as Candidate.Data)
                    } else {
                        onExitClicked()
                    }
                }
            }
        }
    }
}