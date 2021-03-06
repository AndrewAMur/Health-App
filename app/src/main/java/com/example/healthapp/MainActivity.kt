package com.example.healthapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import androidx.fragment.app.Fragment
import com.example.healthapp.Fragments.PhotoFragment
import com.example.healthapp.Fragments.SurveyFragment
import com.example.healthapp.Fragments.VoiceFragment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val photoFragment = PhotoFragment()
        val voiceFragment = VoiceFragment()
        val surveyFragment = SurveyFragment()

        makeCurrentFragment(photoFragment)

        bottom_nav_view.setOnNavigationItemSelectedListener {
            when(it.itemId){
                R.id.photo_id -> makeCurrentFragment(photoFragment)
                R.id.voice_id -> makeCurrentFragment(voiceFragment)
                R.id.survey_id -> makeCurrentFragment(surveyFragment)
            }
            true
        }
    }
    private fun makeCurrentFragment(fragment: Fragment){
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fl_wrapper, fragment)
            commit()
        }
    }
}