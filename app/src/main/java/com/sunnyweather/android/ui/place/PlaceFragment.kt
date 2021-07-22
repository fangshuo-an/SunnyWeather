package com.sunnyweather.android.ui.place

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sunnyweather.android.MainActivity
import com.sunnyweather.android.R
import com.sunnyweather.android.ui.weather.WeatherActivity

class PlaceFragment : Fragment() {
    val viewModel by lazy { ViewModelProvider(this).get(PlaceViewModel::class.java) }

    private lateinit var adapter: PlaceAdapter

    private lateinit var fpView: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fpView = inflater.inflate(R.layout.fragment_place, container, false)
        return fpView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (viewModel.isPlaceSaved() && activity is MainActivity) {
            val place = viewModel.getSavePlace()
            val intent = Intent(context, WeatherActivity::class.java).apply {
                putExtra("location_lng", place.location.lng)
                putExtra("location_lat", place.location.lat)
                putExtra("place_name", place.name)
            }
            startActivity(intent)
            activity?.finish()
            return
        }
        val layoutManager = LinearLayoutManager(activity)
        val recyclerView: RecyclerView? = fpView?.findViewById(R.id.recyclerView)
        val bgImageView: ImageView? = fpView?.findViewById(R.id.bgImageView)
        recyclerView?.layoutManager = layoutManager
        adapter = PlaceAdapter(this, viewModel.placeList)
        recyclerView?.adapter = adapter

        val searchPlaceEdit: EditText? = fpView?.findViewById(R.id.searchPlaceEdit)
        searchPlaceEdit?.addTextChangedListener { editable ->   //监听搜索框内辩护情况
            val content = editable.toString()
            if (content.isNotEmpty()) {
                viewModel.searchPlaces(content)     //发起网络请求
            } else {                                 //
                recyclerView?.visibility = View.GONE
//                bgImageView= fpView?.findViewById(R.id.bgImageView)
                bgImageView?.visibility = View.VISIBLE
                viewModel.placeList.clear()
                adapter.notifyDataSetChanged()  //数据更新
            }
        }
        viewModel.placeLiveData.observe(viewLifecycleOwner, Observer { result ->
            val places = result.getOrNull()     //发起网络请求回调得到的响应数据
            if (places != null) {          //数据不为空添加到placeList集合，并刷新界面
                recyclerView?.visibility = View.VISIBLE
                bgImageView?.visibility = View.GONE
                viewModel.placeList.apply {
                    clear()
                    addAll(places)
                }
                adapter.notifyDataSetChanged()
            } else {
                Toast.makeText(activity, "未能找到任何地点", Toast.LENGTH_SHORT).show()
                result.exceptionOrNull()?.printStackTrace()
            }
        })
    }
}