package com.knowre.android.myscript.iink.view.candidate


internal sealed class Candidate {

    class Data(val id: String, val string: String) : Candidate()

    class Exit()
}