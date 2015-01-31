package anabolicandroids.chanobol.ui.images;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import java.util.List;

import anabolicandroids.chanobol.R;
import anabolicandroids.chanobol.api.ApiModule;
import anabolicandroids.chanobol.ui.UiFragment;
import anabolicandroids.chanobol.util.Util;
import butterknife.InjectView;
import uk.co.senab.photoview.PhotoView;

public class ImageFragment extends UiFragment {
    @InjectView(R.id.image) PhotoView imageView;

    private String boardName;
    private String threadId;
    private Drawable image;
    private int index;
    private List<ImgIdExt> imagePointers;

    private String url;

    public static ImageFragment create(String boardName, String threadId,
                                        Drawable image, int index, List<ImgIdExt> imageIdAndExts) {
        ImageFragment f = new ImageFragment();
        f.boardName = boardName;
        f.threadId = threadId;
        f.image = image;
        f.index = index;
        f.imagePointers = imageIdAndExts;
        return f;
    }

    @Override protected int getLayoutResource() { return R.layout.fragment_image; }

    @Override protected boolean shouldAddPaddingForToolbar() { return false; }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ImgIdExt imagePointer = imagePointers.get(index);
        url = ApiModule.imgUrl(boardName, imagePointer.id, imagePointer.ext);
        // TODO: Makes application break after a while of zooming in and out
        //imageView.setMaximumScale(25); // Default value is too small for some images
        // TODO: Would be great if deepZoom was more configurable. Too early too low resolution.
        ion.build(imageView).placeholder(image).deepZoom().load(url);
    }

    @Override
    public void onResume() {
        super.onResume();
        activity.setTitle("");
        toolbar.getBackground().setAlpha(0);
        toolbar.post(new Runnable() {
            @Override public void run() {
                updateToolbarShadow();
            }
        });
    }

    @Override public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        toolbar.postDelayed(new Runnable() {
            @Override public void run() {
                updateToolbarShadow();
            }
        }, 100);
    }

    @Override public void onDestroy() {
        super.onDestroy();
        toolbar.getBackground().setAlpha(255);
        toolbarShadow.setImageBitmap(null);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.image, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch(item.getItemId()) {
            case R.id.openExternal:
                intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse(url), "image/*");
                if(intent.resolveActivity(activity.getPackageManager()) != null)
                    startActivity(intent);
                else
                    showToast("No suitable app");
                break;
            case R.id.chooseExternal:
                intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse(url), "image/*");
                startActivity(Intent.createChooser(intent, "Open image with"));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateToolbarShadow() {
        toolbar.setDrawingCacheEnabled(true);
        toolbar.buildDrawingCache();
        Bitmap bm = toolbar.getDrawingCache();
        if (Build.VERSION.SDK_INT < 12) bm = Util.setHasAlphaCompat(bm);
        else bm.setHasAlpha(true);
        toolbarShadow.setImageBitmap(Util.blur(activity, bm, 1f));
        toolbarShadow.setColorFilter(Color.argb(255, 0, 0, 0));
        toolbar.setDrawingCacheEnabled(false);
    }
}