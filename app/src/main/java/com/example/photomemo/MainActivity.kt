package com.example.photomemo

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {
    private val addActivityRequestCode = 1
    private lateinit var viewModel: PhotoViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerview)
        val adapter = PhotoListAdapter(this)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        viewModel = ViewModelProvider(this).get(PhotoViewModel::class.java)
        viewModel.allPhotos.observe(this, Observer { photos ->
            photos?.let { adapter.setPhotos(it) }
        })
        val fab = findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener {
            val intent = Intent(this@MainActivity, AddPhotoActivity::class.java)
            startActivityForResult(intent, addActivityRequestCode)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == addActivityRequestCode) {
            if(resultCode != Activity.RESULT_OK) {
                Toast.makeText(
                        applicationContext,
                        "Photo additions have been cancelled.",
                Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}

class PhotoViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PhotoRepository
    val allPhotos: LiveData<List<Photo>>
    init {
        val photoDao = PhotoRoomDatabase.getPhotoDatabase(application).photoDao()
        repository = PhotoRepository(photoDao)
        allPhotos = repository.allPhotos
    }
}
class PhotoListAdapter internal constructor(context: Context)
    : RecyclerView.Adapter<PhotoListAdapter.PhotoViewHolder>() {
    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var photos = emptyList<Photo>()

    inner class PhotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val photoItemView: ImageView = itemView.findViewById(R.id.imageView)
        val memoItemView: TextView = itemView.findViewById(R.id.textView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val itemView = inflater.inflate(R.layout.recyclerview_item, parent, false)
        return PhotoViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val current = photos[position]
        holder.memoItemView.text = current.memo
        holder.photoItemView.setImageURI(Uri.parse(current.uri))
    }

    internal fun setPhotos(photos: List<Photo>) {
        this.photos = photos
        notifyDataSetChanged()
    }

    override fun getItemCount() = photos.size
}
