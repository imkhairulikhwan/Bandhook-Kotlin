package com.antonioleiva.bandhookkotlin.ui.screens.detail

import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.support.design.widget.CollapsingToolbarLayout
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.support.v7.graphics.Palette
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import com.antonioleiva.bandhookkotlin.R
import com.antonioleiva.bandhookkotlin.di.Inject
import com.antonioleiva.bandhookkotlin.di.Injector
import com.antonioleiva.bandhookkotlin.ui.activity.BaseActivity
import com.antonioleiva.bandhookkotlin.ui.adapter.ArtistDetailPagerAdapter
import com.antonioleiva.bandhookkotlin.ui.entity.ArtistDetail
import com.antonioleiva.bandhookkotlin.ui.entity.ImageTitle
import com.antonioleiva.bandhookkotlin.ui.entity.mapper.ArtistDetailDataMapper
import com.antonioleiva.bandhookkotlin.ui.entity.mapper.ImageTitleDataMapper
import com.antonioleiva.bandhookkotlin.ui.fragment.AlbumsFragmentContainer
import com.antonioleiva.bandhookkotlin.ui.presenter.AlbumsPresenter
import com.antonioleiva.bandhookkotlin.ui.presenter.ArtistPresenter
import com.antonioleiva.bandhookkotlin.ui.screens.album.AlbumActivity
import com.antonioleiva.bandhookkotlin.ui.util.getNavigationId
import com.antonioleiva.bandhookkotlin.ui.util.navigate
import com.antonioleiva.bandhookkotlin.ui.util.supportsLollipop
import com.antonioleiva.bandhookkotlin.ui.view.ArtistView
import com.squareup.picasso.Callback
import org.jetbrains.anko.find

/**
 * @author tpom6oh@gmail.com
 *
 * 01/07/16.
 */

class ArtistActivity: BaseActivity(), ArtistView, AlbumsFragmentContainer, Injector by Inject.instance {

    override val layoutResource: Int = R.layout.activity_artist

    val image by lazy { find<ImageView>(R.id.collapse_image) }
    val collapsingToolbarLayout by lazy { find<CollapsingToolbarLayout>(R.id.collapse_toolbar) }
    val viewPager by lazy { find<ViewPager>(R.id.viewpager) }
    val tabLayout by lazy { find<TabLayout>(R.id.tabs) }

    var presenter = ArtistPresenter(this, bus, artistDetailInteractorProvider, topAlbumsInteractorProvider,
            interactorExecutor, ArtistDetailDataMapper(), ImageTitleDataMapper())

    val biographyFragment = BiographyFragment()
    val albumsFragment = AlbumsFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setUpTransition()
        setUpTopBar()
        setUpViewPager()
        setUpTabLayout()
    }

    private fun setUpTransition() {
        supportPostponeEnterTransition()
        supportsLollipop { image.transitionName = IMAGE_TRANSITION_NAME }
    }

    private fun setUpTopBar() {
        title = null
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun setUpTabLayout() {
        tabLayout.setupWithViewPager(viewPager)
    }

    private fun setUpViewPager() {
        val artistDetailPagerAdapter = ArtistDetailPagerAdapter(supportFragmentManager)
        artistDetailPagerAdapter.addFragment(biographyFragment, resources.getString(R.string.bio_fragment_title))
        artistDetailPagerAdapter.addFragment(albumsFragment, resources.getString(R.string.albums_fragment_title))
        viewPager.adapter = artistDetailPagerAdapter
    }

    override fun onResume() {
        super.onResume()
        presenter.onResume()
        presenter.init(getNavigationId())
    }

    override fun onPause() {
        super.onPause()
        presenter.onPause()
    }

    override fun showArtist(artistDetail: ArtistDetail) {
        picasso.load(artistDetail.url).fit().centerCrop().into(image, object : Callback.EmptyCallback() {
            override fun onSuccess() {
                makeStatusBarTransparent()
                supportStartPostponedEnterTransition()
                setActionBarTitle(artistDetail.name)
                biographyFragment.setBiographyText(artistDetail.bio)
                setActionBarPalette()
            }
        })
    }

    override fun showAlbums(albums: List<ImageTitle>) {
        albumsFragment.adapter.items = albums
    }

    private fun setActionBarTitle(title: String) {
        supportActionBar?.title = title
    }

    private fun makeStatusBarTransparent() {
        supportsLollipop {
            window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        }
    }

    private fun setActionBarPalette() {
        val drawable = image.drawable as BitmapDrawable?
        val bitmap = drawable?.bitmap
        if (bitmap != null) {
            Palette.from(bitmap).generate { palette ->
                val darkVibrantColor = palette.getDarkVibrantColor(R.attr.colorPrimary)
                collapsingToolbarLayout.setContentScrimColor(darkVibrantColor)
                collapsingToolbarLayout.setStatusBarScrimColor(darkVibrantColor)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item != null && item.itemId == android.R.id.home) {
            supportFinishAfterTransition()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun navigateToAlbum(albumId: String) {
        navigate<AlbumActivity>(albumId, findItemById(albumId), BaseActivity.IMAGE_TRANSITION_NAME)
    }

    private fun findItemById(id: String): View? {
        val pos = albumsFragment.adapter.findPositionById(id)
        return albumsFragment.recycler.layoutManager.findViewByPosition(pos).findViewById(R.id.image)
    }

    override fun getAlbumsPresenter(): AlbumsPresenter {
        return presenter
    }
}
