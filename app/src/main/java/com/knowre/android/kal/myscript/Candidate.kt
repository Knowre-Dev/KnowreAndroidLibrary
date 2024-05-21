package com.knowre.android.kal.myscript


internal sealed class Candidate {

    class Data(val itemId: String, val label: String) : Candidate()

    class Exit() : Candidate()
}