package com.knowre.android.myscript.iink.view.candidate

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.knowre.android.myscript.iink.databinding.ViewCandidateBinding


internal class CandidateAdapter(
    private val onCandidateClicked: (Candidate.Data) -> Unit,
    private val onExitClicked: () -> Unit
): RecyclerView.Adapter<CandidateViewHolder>() {

    private var candidates = listOf<Candidate>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CandidateViewHolder {
        return CandidateViewHolder.newInstance(parent, onCandidateClicked) {
            setCandidates(listOf())
            onExitClicked()
        }
    }

    override fun onBindViewHolder(holder: CandidateViewHolder, position: Int) {
        holder.bind(candidates[position])
    }

    override fun getItemCount() = candidates.size

    fun setCandidates(candidates: List<Candidate>) {
        this.candidates = candidates
        notifyDataSetChanged()
    }
}

internal class CandidateViewHolder(
    private val binding: ViewCandidateBinding
): RecyclerView.ViewHolder(binding.root) {

    private lateinit var candidate: Candidate

    fun bind(candidate: Candidate) {
        this.candidate = candidate
        if (candidate is Candidate.Data) {
            binding.candidateText.text = candidate.string
        }
    }

    companion object {
        fun newInstance(
            parent: ViewGroup,
            onCandidateClicked: (Candidate.Data) -> Unit,
            onExitClicked: () -> Unit
        ): CandidateViewHolder {
            return CandidateViewHolder(ViewCandidateBinding.inflate(LayoutInflater.from(parent.context), parent, false)).apply {
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