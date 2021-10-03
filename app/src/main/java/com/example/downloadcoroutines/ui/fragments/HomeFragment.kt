package com.example.downloadcoroutines.ui.fragments

import android.Manifest
import android.app.DownloadManager
import android.app.WallpaperManager
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.downloadcoroutines.App
import com.example.downloadcoroutines.R
import com.example.downloadcoroutines.adapters.DataBindingAdapter
import com.example.downloadcoroutines.adapters.GenericAdapter
import com.example.downloadcoroutines.base.BaseFragment
import com.example.downloadcoroutines.modelClasses.PicsModel
import com.example.downloadcoroutines.utils.*
import com.example.downloadcoroutines.viewModel.PicsViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.bottomsheet_layout.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.layout_dialog.*
import kotlinx.coroutines.*
import java.io.File
import javax.inject.Inject


@AndroidEntryPoint
class HomeFragment : BaseFragment(), GenericAdapter.OnItemClickListener<Any> {

    var page = 1

    @Inject
    lateinit var app: App
    lateinit var gridLayoutManager: RecyclerView.LayoutManager

    val viewmodel: PicsViewModel by viewModels()

    lateinit var genericAdapter: GenericAdapter
    lateinit var imageList: ArrayList<PicsModel>


    lateinit var bottomSheetView: View
    private lateinit var bottomSheetDialog: BottomSheetDialog

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        initViews()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onStart() {
        super.onStart()
        val window: Window = requireActivity().getWindow()
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = resources.getColor(R.color.blue_50)
    }


    private fun initViews() {
        gridLayoutManager = StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL)

        setBottomSheet()

        imageList = ArrayList()

        setPicsAdapter(page)


        nestedHome.setOnScrollChangeListener(nestedScrollListener)

        animeHeaderHome.setAnimationFromUrl("https://assets7.lottiefiles.com/packages/lf20_ynwbrgau.json")
    }

    private var nestedScrollListener =
        NestedScrollView.OnScrollChangeListener { v, _, scrollY, _, _ ->
            if (scrollY == v!!.getChildAt(0).measuredHeight - v.measuredHeight) {
                page++
                setPicsAdapter(page)
            }
        }


    private fun setBottomSheet() {
        GlobalScope.launch(Dispatchers.Main) {
            bottomSheetView =
                layoutInflater.inflate(R.layout.bottomsheet_layout, null)

            bottomSheetDialog = BottomSheetDialog(requireContext())
            bottomSheetDialog.setContentView(bottomSheetView)
            bottomSheetDialog.window?.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
            )
            bottomSheetDialog.window?.setGravity(Gravity.BOTTOM)
            bottomSheetDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)


        }
    }


    private fun initPicsAdapter(list: ArrayList<Any>) {
        if (!::genericAdapter.isInitialized) {
            //normal adapter initialization
            genericAdapter =
                GenericAdapter(
                    list,
                    this,
                    R.layout.row_home_pics
                )


            rvImages.apply {
                isNestedScrollingEnabled = false
                layoutManager = gridLayoutManager
                adapter = genericAdapter

            }
            if (imageList.isNotEmpty()) {
                rvImages.setItemViewCacheSize(imageList.size)
            }


        }
    }

    private fun setPicsAdapter(page: Int) {
        if (app.isNetworkConnected(requireActivity()) or imageList.isNotEmpty()) {
            viewmodel.getPics(page, 10)
            if (!viewmodel.picsResponse.hasActiveObservers()) {
                viewmodel.picsResponse.observe(requireActivity(), Observer
                {
                    when (it.status) {
                        Status.SUCCESS -> {
                            dismissDialog()

                            initPicsAdapter(it.data as ArrayList<Any>)

                            rvCat.apply {
                                isNestedScrollingEnabled = false
                                layoutManager = LinearLayoutManager(
                                    requireContext(),
                                    LinearLayoutManager.HORIZONTAL,
                                    false
                                )
                                adapter = GenericAdapter(
                                    it.data as ArrayList<Any>,
                                    this@HomeFragment,
                                    R.layout.row_categories
                                )
                                setItemViewCacheSize(500)
                            }

                            if (imageList.isEmpty()) {
                                imageList = it.data
                                genericAdapter.notifyAdapter(it.data as ArrayList<Any>)
                            } else {
                                for (i in it.data) {
                                    imageList.add(
                                        PicsModel(
                                            i.author,
                                            i.download_url,
                                            i.id,
                                            i.url
                                        )
                                    )
                                }
                                genericAdapter.notifyItemInserted(imageList.size)
                            }
                        }
                        Status.LOADING -> {
                            showDialog()
                        }
                        Status.ERROR -> {
                            dismissDialog()
                            requireActivity().showToast("${it.status.name}")
                        }
                    }


                })


            }

        } else {
            imageList.add(PicsModel("","","","","",false))
            requireContext().showToast("Internet not Connected")
            imgNoConnection.visibility = View.VISIBLE
            homeContent.visibility = View.GONE
        }

    }


    @RequiresApi(Build.VERSION_CODES.N)
    fun downloadFile(url: String, fileName: String? = null) {
        val directory = File(Environment.DIRECTORY_DOWNLOADS)

        if (!directory.exists()) {
            directory.mkdirs()
        }

        val downloadManager =
            requireActivity().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        val downloadUri = Uri.parse(url)

        val request = DownloadManager.Request(downloadUri).apply {
            setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                .setAllowedOverRoaming(false)
                .setTitle(fileName)
                .setVisibleInDownloadsUi(true)
                .setDescription(fileName)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(
                    directory.toString(),
                    url.substring(url.lastIndexOf("/") + 1)
                )
        }


        val downloadId = downloadManager.enqueue(request)
        val query = DownloadManager.Query().setFilterById(downloadId)

        GlobalScope.launch(Dispatchers.IO) {

            var downloading = true
            while (downloading) {
                val cursor: Cursor = downloadManager.query(query)
                cursor.moveToFirst()

                if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                    downloading = false
                }
                val fileStatus = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                when (fileStatus) {
                    DownloadManager.STATUS_FAILED -> {
                        dismissDialog()
                    }
                    DownloadManager.STATUS_PAUSED -> {
                        dismissDialog()
                    }
                    DownloadManager.STATUS_PENDING -> {
                    }
                    DownloadManager.STATUS_RUNNING -> {
                        showDialog()
                    }
                    DownloadManager.STATUS_SUCCESSFUL -> {
                        dismissDialog()
                    }
                }
                cursor.close()
            }
        }

    }


    @RequiresApi(Build.VERSION_CODES.N)
    fun askPermissions(url: String, imageName: String? = null) {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    requireActivity(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            ) {

                AlertDialog.Builder(requireContext())
                    .setTitle("Permission required")
                    .setMessage("Permission required to save files.")
                    .setPositiveButton("Accept") { dialog, id ->
                        ActivityCompat.requestPermissions(
                            requireActivity(),
                            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                            MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE
                        )
                    }
                    .setNegativeButton("Deny") { dialog, id -> dialog.cancel() }
                    .show()
            } else {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE
                )
                downloadFile(url, imageName)
            }
        } else {
            downloadFile(url, imageName)
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onItemClick(view: View?, position: Int, `object`: Any) {
        if (`object` is PicsModel) {
            when (view?.id) {
                //set bottom sheet adapter click listener
                R.id.cardBS -> {
                    DataBindingAdapter.let {
                        it.setSrc(bottomSheetDialog.ivImage, `object`.download_url)
                    }
                }
                R.id.cardIImage -> {

                    DataBindingAdapter.let {
                        it.setSrc(bottomSheetDialog.ivImage, `object`.download_url)
                    }

                    //set bottom sheet recyclerview
                    bottomSheetDialog.apply {

                        btDownload.setOnClickListener {
                            askPermissions(
                                `object`.download_url.toString(),
                                `object`.author
                            )
                        }

                        tvAuthorName.text = `object`.author

                        rvBottomSheet.apply {
                            layoutManager = LinearLayoutManager(
                                requireContext(),
                                LinearLayoutManager.HORIZONTAL,
                                false
                            )
                            adapter = GenericAdapter(
                                imageList.filter { it.author == `object`.author } as ArrayList<Any>,
                                this@HomeFragment,
                                R.layout.row_bottom_sheet
                            )
                        }

                        //set wallpaper
                        ivBackground.setOnClickListener {

                            val wallpaperManager = WallpaperManager.getInstance(requireContext())
                            wallpaperManager.setBitmap(ivImage.drawable.toBitmap())

                            requireContext().showToast("Wallpaper applied successfully!")

                        }
                        show()

                    }
                }
            }


        }
    }


}