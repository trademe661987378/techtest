package nz.co.trademe.techtest.ui.main

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import nz.co.trademe.techtest.R
import nz.co.trademe.techtest.core.TMApplication
import nz.co.trademe.techtest.domain.repository.CategoriesRepository
import nz.co.trademe.wrapper.models.Category
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_CATEGORY_ID = "extra_category_id"

        fun getIntent(activity: Activity, categoryId: String): Intent {
            val intent = Intent(activity, MainActivity::class.java)
            intent.putExtra(EXTRA_CATEGORY_ID, categoryId)
            return intent
        }
    }

    private var categoriesRepository: CategoriesRepository = TMApplication.instance.categoriesRepository

    private var disposable: DisposableSingleObserver<Category>? = null

    private var categories: MutableList<Category> = mutableListOf()
    private lateinit var adapter: CategoryListAdapter
    @BindView(R.id.recycler_view)
    lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ButterKnife.bind(this)

        var categoryId: String = intent.getStringExtra(EXTRA_CATEGORY_ID) ?: CategoriesRepository.ROOT_CATEGORY

        adapter = CategoryListAdapter(categories)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        disposable = categoriesRepository.getCategory(categoryId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : DisposableSingleObserver<Category>() {
                override fun onSuccess(category: Category) {
                    category.subcategories?.let {
                        categories.clear()
                        categories.addAll(it)

                        updateView()
                    }
                }

                override fun onError(e: Throwable) {
                    Timber.e(e)
                    // todo error handling
                }
            })
    }

    fun updateView() {
        // todo loading indicator
        adapter.notifyDataSetChanged()
    }
}