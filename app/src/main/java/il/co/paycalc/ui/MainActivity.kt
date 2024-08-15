package il.co.paycalc.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import il.co.paycalc.MyApplication
import il.co.paycalc.R
import il.co.paycalc.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var recordViewModel: RecordViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val repository = (application as MyApplication).recordRepository
        recordViewModel = ViewModelProvider(
            this,
            RecordViewModelFactory(repository)
        ).get(RecordViewModel::class.java)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        val bottomNavigationView = binding.bottomNavigationView
//        val navController = findNavController(R.id.nav_host_fragment_main)
//
//        bottomNavigationView.setupWithNavController(navController)
//
//        navController.addOnDestinationChangedListener { _, nd: NavDestination, _ ->
//            if (nd.id == R.id.departuresFlightsFragment || nd.id == R.id.arrivalsFlightsFragment || nd.id == R.id.favoriteFragment) {
//                bottomNavigationView.visibility = View.VISIBLE
//            } else {
//                bottomNavigationView.visibility = View.GONE
//            }
//        }
    }
}